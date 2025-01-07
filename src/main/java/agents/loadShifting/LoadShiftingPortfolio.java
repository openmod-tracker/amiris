// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.loadShifting;

import agents.trader.LoadShiftingTrader;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A load shifting portfolio considers of demand units which are eligible for load shifting and marketed by a
 * {@link LoadShiftingTrader}
 * 
 * @author Johannes Kochems */
public class LoadShiftingPortfolio {
	private final static double STORAGE_TOLERANCE = 1E-3;

	private final double powerInMW;
	private final TimeSeries powerUpSeries;
	private final TimeSeries powerDownSeries;
	private final double energyResolutionInMWH;
	private final int maximumShiftTimeInHours;
	private int currentShiftTimeInHours;
	private double currentEnergyShiftStorageLevelInMWH;
	private final double energyLimitUpInMWH;
	private final double energyLimitDownInMWH;
	private final TimeSeries variableShiftCostsInEURPerMWH;
	private final TimeSeries baselineLoadSeries;
	private final double baselinePeakLoadInMW;
	private final double efficiency;
	private final int interferenceTimeInHours;
	private final int maximumActivations;

	public static final Tree parameters = Make.newTree()
			.add(Make.newDouble("InitialEnergyLevelInMWH"), Make.newInt("InitialShiftTimeInHours"),
					Make.newDouble("PowerInMW"), Make.newSeries("PowerUpAvailability"),
					Make.newSeries("PowerDownAvailability"), Make.newDouble("EnergyResolutionInMWH"),
					Make.newDouble("EnergyLimitUpInMWH"), Make.newDouble("EnergyLimitDownInMWH"),
					Make.newInt("MaximumShiftTimeInHours"), Make.newSeries("VariableShiftCostsInEURPerMWH"),
					Make.newSeries("BaselineLoadTimeSeries"), Make.newDouble("BaselinePeakLoadInMW"),
					Make.newDouble("Efficiency").optional(), Make.newInt("InterferenceTimeInHours").optional(),
					Make.newInt("MaximumActivations").optional())
			.buildTree();

	public LoadShiftingPortfolio(ParameterData input) throws MissingDataException {
		powerInMW = input.getDouble("PowerInMW");
		powerUpSeries = input.getTimeSeries("PowerUpAvailability");
		powerDownSeries = input.getTimeSeries("PowerDownAvailability");
		energyLimitUpInMWH = input.getDouble("EnergyLimitUpInMWH");
		energyLimitDownInMWH = input.getDouble("EnergyLimitDownInMWH");
		energyResolutionInMWH = input.getDouble("EnergyResolutionInMWH");
		setEnergyShiftStorageLevelInMWH(input.getDouble("InitialEnergyLevelInMWH"));
		setCurrentShiftTimeInHours(input.getInteger("InitialShiftTimeInHours"));
		maximumShiftTimeInHours = input.getInteger("MaximumShiftTimeInHours");
		variableShiftCostsInEURPerMWH = input.getTimeSeries("VariableShiftCostsInEURPerMWH");
		baselineLoadSeries = input.getTimeSeries("BaselineLoadTimeSeries");
		baselinePeakLoadInMW = input.getDouble("BaselinePeakLoadInMW");
		efficiency = input.getDoubleOrDefault("Efficiency", 1.0);
		interferenceTimeInHours = input.getIntegerOrDefault("InterferenceTimeInHours", maximumShiftTimeInHours);
		maximumActivations = input.getIntegerOrDefault("MaximumActivations", 1000000);
	}

	/** SetType load shift storage level and ensure it is within bounds */
	private void setEnergyShiftStorageLevelInMWH(double energyLevelInMWH) {
		this.currentEnergyShiftStorageLevelInMWH = Math.max(-energyLimitDownInMWH,
				Math.min(energyLimitUpInMWH, energyLevelInMWH));
	}

	private void setCurrentShiftTimeInHours(Integer initialShiftTimeInHours) {
		this.currentShiftTimeInHours = Math.max(0, Math.min(initialShiftTimeInHours, maximumShiftTimeInHours - 1));
	}

	public double getPowerInMW() {
		return powerInMW;
	}

	public double getCurrentEnergyShiftStorageLevelInMWH() {
		return this.currentEnergyShiftStorageLevelInMWH;
	}

	public int getCurrentShiftTimeInHours() {
		return currentShiftTimeInHours;
	}

	public int getMaximumShiftTimeInHours() {
		return maximumShiftTimeInHours;
	}

	/** Update the energy shift storage level as well as the shift time considering the chargingPower:<br>
	 * <ul>
	 * <li>chargingPower &gt; 0: charging</li>
	 * <li>chargingPower &lt; 0: depleting</li>
	 * </ul>
	 * 
	 * @param chargingPower power charged (increment of fictitious load shift energy storage level) */
	public void updateEnergyShiftStorageLevelAndShiftTime(double chargingPower) {
		int shiftTime = getCurrentShiftTimeInHours();
		double initialEnergyLevel = getCurrentEnergyShiftStorageLevelInMWH();
		double finalEnergyLevel = initialEnergyLevel + chargingPower;
		if (isProlongedShift(chargingPower, shiftTime, initialEnergyLevel)) {
			shiftTime = 1;
		} else if (isZeroEnergyLevel(finalEnergyLevel)) {
			shiftTime = 0;
		} else if (isChangeOfSign(initialEnergyLevel, finalEnergyLevel)) {
			shiftTime = 1;
		} else {
			shiftTime += 1;
		}
		setCurrentShiftTimeInHours(shiftTime);
		setEnergyShiftStorageLevelInMWH(finalEnergyLevel);
	}

	/** Check for an initial reset of parts of the {@link LoadShiftingPortfolio}
	 * 
	 * @param chargingPower power charged (increment of fictitious load shift energy storage level)
	 * @param shiftTime time that the {@link LoadShiftingPortfolio} has been shifted for in one direction so far
	 * @param initialEnergyLevel start value of fictitious load shifting energy storage level
	 * @return information on whether shift is a prolonged shift */
	private boolean isProlongedShift(double chargingPower, int shiftTime, double initialEnergyLevel) {
		if (shiftTime == maximumShiftTimeInHours - 1
				&& (initialEnergyLevel < -STORAGE_TOLERANCE || initialEnergyLevel > STORAGE_TOLERANCE)) {
			if (chargingPower == 0 || (chargingPower > 0 && initialEnergyLevel > -STORAGE_TOLERANCE)
					|| (chargingPower < 0 && initialEnergyLevel < STORAGE_TOLERANCE)) {
				return true;
			}
		}
		return false;
	}

	public double getProlongingCosts(double chargingPower, TimeStamp timeStamp) {
		int shiftTime = getCurrentShiftTimeInHours();
		double initialEnergyLevel = getCurrentEnergyShiftStorageLevelInMWH();
		if (isProlongedShift(chargingPower, shiftTime, initialEnergyLevel)) {
			return Math.abs(initialEnergyLevel) * getVariableShiftCostsInEURPerMWH(timeStamp);
		} else {
			return 0.0;
		}
	}

	/** @return true if LoadShift energy level changes its sign */
	private boolean isChangeOfSign(double initialEnergyLevel, double finalEnergyLevel) {
		boolean initialStateNotZero = !isZeroEnergyLevel(initialEnergyLevel);
		boolean finalStateNotZero = !isZeroEnergyLevel(finalEnergyLevel);
		return initialStateNotZero && finalStateNotZero
				&& (Math.signum(finalEnergyLevel) != Math.signum(initialEnergyLevel));
	}

	/** @return true if state within zero storage level tolerance */
	private boolean isZeroEnergyLevel(double energyLevel) {
		return -STORAGE_TOLERANCE <= energyLevel && energyLevel <= STORAGE_TOLERANCE;
	}

	public TimeSeries getPowerUpSeries() {
		return powerUpSeries;
	}

	public TimeSeries getPowerDownSeries() {
		return powerDownSeries;
	}

	public double getEnergyResolutionInMWH() {
		return energyResolutionInMWH;
	}

	public double getEnergyLimitUpInMWH() {
		return energyLimitUpInMWH;
	}

	public double getEnergyLimitDownInMWH() {
		return energyLimitDownInMWH;
	}

	public double getVariableShiftCostsInEURPerMWH(TimeStamp timeStamp) {
		return variableShiftCostsInEURPerMWH.getValueLinear(timeStamp);
	}

	public TimeSeries getVariableShiftCostsInEURPerMWHSeries() {
		return variableShiftCostsInEURPerMWH;
	}

	public TimeSeries getBaselineLoadSeries() {
		return baselineLoadSeries;
	}

	public double getBaselinePeakLoad() {
		return baselinePeakLoadInMW;
	}

	public double getMaxPowerUp(TimePeriod timePeriod) {
		return powerUpSeries.getValueEarlierEqual(timePeriod.getStartTime()) * powerInMW;
	}

	public double getMaxPowerDown(TimePeriod timePeriod) {
		return powerDownSeries.getValueEarlierEqual(timePeriod.getStartTime()) * powerInMW;
	}

	public double getEfficiency() {
		return efficiency;
	}

	public int getInterferenceTimeInHours() {
		return interferenceTimeInHours;
	}

	public int getMaximumActivations() {
		return maximumActivations;
	}
}
