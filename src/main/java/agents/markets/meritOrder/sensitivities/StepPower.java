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

	/** @return power in MW corresponding to number of charging / discharging steps<br />
	 *         <ul>
	 *         <li>given stepDelta > 0: charging => power > 0</li>
	 *         <li>given stepDelta < 0: discharging => power < 0</li>
	 *         </ul>
	 */
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