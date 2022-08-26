// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets.meritOrder.sensitivities;

/** Power in MW per charging / discharging step connected to a {@link MeritOrderSensitivity}
 * 
 * @author Christoph Schimeczek */
public class StepPower {
	private final double chargingStepPowerInMW;
	private final double dischargingStepPowerInMW;
	private final int numberOfTransitionSteps;

	/** Creates a {@link StepPower}
	 * 
	 * @param externalChargingPowerInMW maximum effective charging power
	 * @param externalDischargingPowerInMW maximum effective discharging power
	 * @param numberOfTransitionSteps number of modelled transition steps per direction */
	public StepPower(double externalChargingPowerInMW, double externalDischargingPowerInMW, int numberOfTransitionSteps) {
		this.chargingStepPowerInMW = externalChargingPowerInMW / numberOfTransitionSteps;
		this.dischargingStepPowerInMW = externalDischargingPowerInMW / numberOfTransitionSteps;
		this.numberOfTransitionSteps = numberOfTransitionSteps;
	}

	/** Returns power matching given step delta
	 * 
	 * @param stepDelta for charging / discharging
	 *          <ul>
	 *          <li>&gt; 0: charging &rarr; power &gt; 0</li>
	 *          <li>&lt; 0: discharging &rarr; power &lt; 0</li>
	 *          </ul>
	 * @return power in MW corresponding to number of charging / discharging steps */
	public double getPower(int stepDelta) {
		if (stepDelta >= 0 && stepDelta <= numberOfTransitionSteps) {
			return stepDelta * chargingStepPowerInMW;
		} else if (stepDelta < 0 && -stepDelta <= numberOfTransitionSteps) {
			return stepDelta * dischargingStepPowerInMW;
		} else {
			throw new RuntimeException("StepDelta out of range");
		}
	}
}