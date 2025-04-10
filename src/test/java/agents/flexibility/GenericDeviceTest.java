// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.protobuf.Input.InputData.TimeSeriesDao;
import de.dlr.gitlab.fame.time.Constants.Interval;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;
import testUtils.LogChecker;

public class GenericDeviceTest {

	@Mock private ParameterData parameterDataMock;
	private static LogChecker logChecker;
	private AutoCloseable closable;

	private GenericDevice device;
	private TimeStamp defaultTime = new TimeStamp(0L);
	private TimeSpan oneHour = new TimeSpan(1, Interval.HOURS);
	private TimeSpan twoHours = new TimeSpan(2, Interval.HOURS);
	private TimeSpan quarterHour = new TimeSpan(15, Interval.MINUTES);

	@BeforeAll
	public static void setupLogCaptor() {
		logChecker = new LogChecker(GenericDevice.class);
	}

	@BeforeEach
	public void setUp() throws MissingDataException {
		closable = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	public void clear() throws Exception {
		closable.close();
		logChecker.clear();
	}

	@AfterAll
	public static void tearDown() {
		logChecker.close();
	}

	private GenericDevice createGenericDevice(double chargingPowerInMW, double dischargingPowerInMW,
			double chargingEfficiency, double dischargingEfficiency, double energyContentUpperLimitInMWH,
			double energyContentLowerLimitInMWH, double selfDischargeRatePerHour, double netInflowPowerInMW,
			double currentEnergyContentInMWH) {
		try {
			when(parameterDataMock.getTimeSeries("GrossChargingPowerInMW")).thenReturn(createSeries(chargingPowerInMW));
			when(parameterDataMock.getTimeSeriesOrDefault(eq("NetDischargingPowerInMW"), any(TimeSeries.class)))
					.thenReturn(createSeries(dischargingPowerInMW));
			when(parameterDataMock.getTimeSeries("ChargingEfficiency")).thenReturn(createSeries(chargingEfficiency));
			when(parameterDataMock.getTimeSeries("DischargingEfficiency")).thenReturn(createSeries(dischargingEfficiency));
			when(parameterDataMock.getTimeSeries("EnergyContentUpperLimitInMWH"))
					.thenReturn(createSeries(energyContentUpperLimitInMWH));
			when(parameterDataMock.getTimeSeries("EnergyContentLowerLimitInMWH"))
					.thenReturn(createSeries(energyContentLowerLimitInMWH));
			when(parameterDataMock.getTimeSeries("SelfDischargeRatePerHour"))
					.thenReturn(createSeries(selfDischargeRatePerHour));
			when(parameterDataMock.getTimeSeries("NetInflowPowerInMW")).thenReturn(createSeries(netInflowPowerInMW));
			when(parameterDataMock.getDouble("InitialEnergyContentInMWH")).thenReturn(currentEnergyContentInMWH);
			return new GenericDevice(parameterDataMock);
		} catch (MissingDataException e) {
			throw new RuntimeException();
		}
	}

	private TimeSeries createSeries(double... values) {
		TimeSeriesDao.Builder builder = TimeSeriesDao.newBuilder();
		for (int i = 0; i < values.length; i++) {
			builder.addTimeSteps(i);
			builder.addValues(values[i]);
		}
		return new TimeSeries(builder.setSeriesId(1).build());
	}

	@Test
	public void getEnergyContentUpperLimitInMWH_returnsConfiguredValue() {
		double targetValue = 42.;
		device = createGenericDevice(0, 0, 0, 0, targetValue, 0, 0, 0, 0);
		assertEquals(targetValue, device.getEnergyContentUpperLimitInMWH(defaultTime), 1E-12);
	}

	@Test
	public void getEnergyContentLowerLimitInMWH_returnsConfiguredValue() {
		double targetValue = 42.;
		device = createGenericDevice(0, 0, 0, 0, 0, targetValue, 0, 0, 0);
		assertEquals(targetValue, device.getEnergyContentLowerLimitInMWH(defaultTime), 1E-12);
	}

	@Test
	public void getMaxTargetEnergyContentInMWH_noSelfDischargeNoInflowInitialZero_equalsNetCharging() {
		device = createGenericDevice(100, 0, 1, 1, 500, 0, 0, 0, 0);
		assertEquals(100, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, oneHour), 1E-12);
		assertEquals(200, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, twoHours), 1E-12);
		assertEquals(25, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, quarterHour), 1E-12);
	}

	@Test
	public void getMaxTargetEnergyContentInMWH_noSelfDischargeNoInflowInitialNonzero_addsInitial() {
		device = createGenericDevice(100, 0, 1, 1, 500, 0, 0, 0, 0);
		assertEquals(110, device.getMaxTargetEnergyContentInMWH(defaultTime, 10, oneHour), 1E-12);
		assertEquals(190, device.getMaxTargetEnergyContentInMWH(defaultTime, -10, twoHours), 1E-12);
		assertEquals(35, device.getMaxTargetEnergyContentInMWH(defaultTime, 10, quarterHour), 1E-12);
	}

	@Test
	public void getMaxTargetEnergyContentInMWH_noSelfDischargeWithInflowInitialZero_addsInflow() {
		device = createGenericDevice(100, 0, 1, 1, 500, 0, 0, 20, 0);
		assertEquals(120, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, oneHour), 1E-12);
		assertEquals(240, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, twoHours), 1E-12);
		assertEquals(30, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, quarterHour), 1E-12);
	}

	@Test
	public void getMaxTargetEnergyContentInMWH_withSelfDischargeNoInflow_subtractsSelfDischarge() {
		device = createGenericDevice(200, 0, 1, 1, 500, 0, 0.1, 0, 0);
		assertEquals(200, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, oneHour), 1E-2);
		assertEquals(290, device.getMaxTargetEnergyContentInMWH(defaultTime, 100, oneHour), 1E-2);
		assertEquals(440.5, device.getMaxTargetEnergyContentInMWH(defaultTime, 50, twoHours), 1E-2);
		assertEquals(244.801, device.getMaxTargetEnergyContentInMWH(defaultTime, 200, quarterHour), 1E-2);
	}

	@Test
	public void simulateTransition_useMaxTargetEnergy_returnsMaxPower() {
		device = createGenericDevice(200, 0, 1, 0, 500, 0, 0.1, 0, 0);
		double targetEnergyLevelInMWH = device.getMaxTargetEnergyContentInMWH(defaultTime, 0, twoHours);
		assertEquals(400, device.simulateTransition(defaultTime, 0, targetEnergyLevelInMWH, twoHours), 1E-12);
	}

	@Test
	public void getMaxTargetEnergyContentInMWH_withSelfDischargeInflowAndInitial_correctCalc() {
		device = createGenericDevice(200, 0, 1, 1, 500, 0, 0.1, 50, 0);
		assertEquals(232, device.getMaxTargetEnergyContentInMWH(defaultTime, -20, oneHour), 1E-2);
		device = createGenericDevice(200, 0, 1, 1, 500, 0, 0.1, -50, 0);
		assertEquals(372.9, device.getMaxTargetEnergyContentInMWH(defaultTime, 90, twoHours), 1E-2);
		assertEquals(71.590, device.getMaxTargetEnergyContentInMWH(defaultTime, 35, quarterHour), 1E-2);
	}

	@Test
	public void getMinTargetEnergyContentInMWH_noSelfDischargeNoInflowInitialZero_equalsNetCharging() {
		device = createGenericDevice(0, 100, 1, 1, 0, -500, 0, 0, 0);
		assertEquals(-100, device.getMinTargetEnergyContentInMWH(defaultTime, 0, oneHour), 1E-12);
		assertEquals(-200, device.getMinTargetEnergyContentInMWH(defaultTime, 0, twoHours), 1E-12);
		assertEquals(-25, device.getMinTargetEnergyContentInMWH(defaultTime, 0, quarterHour), 1E-12);
	}

	@Test
	public void getMinTargetEnergyContentInMWH_noSelfDischargeNoInflowInitialNonzero_addsInitial() {
		device = createGenericDevice(0, 100, 1, 1, 0, -500, 0, 0, 0);
		assertEquals(100, device.getMinTargetEnergyContentInMWH(defaultTime, 200, oneHour), 1E-12);
		assertEquals(0, device.getMinTargetEnergyContentInMWH(defaultTime, 200, twoHours), 1E-12);
		assertEquals(-15, device.getMinTargetEnergyContentInMWH(defaultTime, 10, quarterHour), 1E-12);
	}

	@Test
	public void getMinTargetEnergyContentInMWH_noSelfDischargeWithInflowInitialZero_addsInflow() {
		device = createGenericDevice(0, 100, 1, 1, 0, -500, 0, -20, 0);
		assertEquals(-120, device.getMinTargetEnergyContentInMWH(defaultTime, 0, oneHour), 1E-12);
		assertEquals(-240, device.getMinTargetEnergyContentInMWH(defaultTime, 0, twoHours), 1E-12);
		assertEquals(-30, device.getMinTargetEnergyContentInMWH(defaultTime, 0, quarterHour), 1E-12);
	}

	@Test
	public void getMinTargetEnergyContentInMWH_withSelfDischargeNoInflowInitialZero_subtractsSelfDischarge() {
		device = createGenericDevice(0, 200, 1, 1, 0, -500, 0.1, 0, 0);
		assertEquals(-209, device.getMinTargetEnergyContentInMWH(defaultTime, -10, oneHour), 1E-2);
		assertEquals(-367.600, device.getMinTargetEnergyContentInMWH(defaultTime, 40, twoHours), 1E-2);
		assertEquals(47.400, device.getMinTargetEnergyContentInMWH(defaultTime, 100, quarterHour), 1E-2);
	}

	@Test
	public void simulateTransition_useMinTargetEnergy_returnsMinPower() {
		device = createGenericDevice(0, 200, 0, 1, 0, -500, 0.1, 0, 0);
		double targetEnergyLevelInMWH = device.getMinTargetEnergyContentInMWH(defaultTime, 0, twoHours);
		assertEquals(-400, device.simulateTransition(defaultTime, 0, targetEnergyLevelInMWH, twoHours), 1E-12);

		targetEnergyLevelInMWH = device.getMinTargetEnergyContentInMWH(defaultTime, 0, quarterHour);
		assertEquals(-50, device.simulateTransition(defaultTime, 0, targetEnergyLevelInMWH, quarterHour), 1E-12);
	}

	@Test
	public void getMinTargetEnergyContentInMWH_withSelfDischargeInflowAndInitial_correctCalc() {
		device = createGenericDevice(0, 200, 1, 1, 0, -500, 0.1, 50, 0);
		assertEquals(-132, device.getMinTargetEnergyContentInMWH(defaultTime, 20, oneHour), 1E-2);
		device = createGenericDevice(0, 200, 1, 1, 0, -500, 0.1, -50, 0);
		assertEquals(-13.800, device.getMinTargetEnergyContentInMWH(defaultTime, 50, quarterHour), 1E-2);
	}

	@Test
	public void simulateTransition_noSelfDischargeFullEfficiency_correctCalc() {
		device = createGenericDevice(0, 0, 1, 1, 0, 0, 0, 10, 0);
		assertEquals(40, device.simulateTransition(defaultTime, 50, 100, oneHour), 1E-12);
		assertEquals(107.5, device.simulateTransition(defaultTime, -10, 100, quarterHour), 1E-12);
		assertEquals(-10, device.simulateTransition(defaultTime, 90, 100, twoHours), 1E-12);
	}

	@Test
	public void simulateTransition_noSelfDischargeImperfectEfficiency_correctCalc() {
		device = createGenericDevice(0, 0, 0.5, 0.8, 0, 0, 0, 10, 0);
		assertEquals(80, device.simulateTransition(defaultTime, 50, 100, oneHour), 1E-12);
		assertEquals(215, device.simulateTransition(defaultTime, -10, 100, quarterHour), 1E-12);
		assertEquals(-8, device.simulateTransition(defaultTime, 90, 100, twoHours), 1E-12);
	}

	@Test
	public void simulateTransition_withSelfDischargeImperfectEfficiency_correctCalc() {
		device = createGenericDevice(0, 0, 0.5, 0.8, 0, 0, 0.1, 10, 0);
		assertEquals(90, device.simulateTransition(defaultTime, 50, 100, oneHour), 1E-2);
		assertEquals(214.480, device.simulateTransition(defaultTime, -10, 100, quarterHour), 1E-2);
		assertEquals(14.2, device.simulateTransition(defaultTime, 90, 100, twoHours), 1E-2);
		assertEquals(-40, device.simulateTransition(defaultTime, 100, 50, oneHour), 1E-2);
	}

	@Test
	public void transition_noSelfDischargeFullEfficiencyNoInflow_correctNewEnergyContent() {
		device = createGenericDevice(100, 100, 1, 1, 500, -500, 0., 0, 0);
		device.transition(defaultTime, 50, oneHour);
		assertEquals(50, device.getCurrentInternalEnergyInMWH(), 1E-12);
	}

	@Test
	public void transition_noSelfDischargeFullEfficiencyWithInflow_correctNewEnergyContent() {
		device = createGenericDevice(100, 100, 1, 1, 500, -500, 0., 10, 0);
		device.transition(defaultTime, 200, twoHours);
		assertEquals(220, device.getCurrentInternalEnergyInMWH(), 1E-12);
	}

	@Test
	public void transition_noSelfDischargeImperfectEfficiencyNoInflow_correctNewEnergyContent() {
		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0., 0, 0);
		device.transition(defaultTime, 100, oneHour);
		assertEquals(50, device.getCurrentInternalEnergyInMWH(), 1E-12);

		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0., 0, 100);
		device.transition(defaultTime, -80, oneHour);
		assertEquals(0, device.getCurrentInternalEnergyInMWH(), 1E-12);
	}

	@Test
	public void transition_withSelfDischargeFullEfficiencyNoInflow_correctNewEnergyContent() {
		device = createGenericDevice(100, 100, 1, 1, 500, -500, 0.1, 0, 100);
		device.transition(defaultTime, 0, twoHours);
		assertEquals(81, device.getCurrentInternalEnergyInMWH(), 1E-12);

		device = createGenericDevice(100, 100, 1, 1, 500, -500, 0.1, 0, 100);
		device.transition(defaultTime, 100, twoHours);
		assertEquals(181, device.getCurrentInternalEnergyInMWH(), 1E-12);
	}

	@Test
	public void transition_withSelfDischargeImperfectEfficiencyWithInflow_correctNewEnergyContent() {
		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0.1, 10, 100);
		device.transition(defaultTime, 0, twoHours);
		assertEquals(101, device.getCurrentInternalEnergyInMWH(), 1E-12);

		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0.1, 10, 100);
		device.transition(defaultTime, -40, twoHours);
		assertEquals(51, device.getCurrentInternalEnergyInMWH(), 1E-12);

		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0.1, 10, 100);
		device.transition(defaultTime, 20, twoHours);
		assertEquals(111, device.getCurrentInternalEnergyInMWH(), 1E-12);
	}

	@Test
	public void transition_chargingPowerExceeded_returnsFixedEnergyDeltas() {
		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0, 0, 50);
		double result = device.transition(defaultTime, 300, oneHour);
		assertEquals(100., result, 1E-12);
		assertEquals(100., device.getCurrentInternalEnergyInMWH(), 1E-12);
		logChecker.assertLogsContain(GenericDevice.ERR_EXCEED_CHARGING_POWER);
	}

	@Test
	public void transition_dischargingPowerExceeded_returnsFixedEnergyDeltas() {
		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0, 0, 250);
		double result = device.transition(defaultTime, -120, oneHour);
		assertEquals(-100, result, 1E-12);
		assertEquals(125., device.getCurrentInternalEnergyInMWH(), 1E-12);
		logChecker.assertLogsContain(GenericDevice.ERR_EXCEED_DISCHARGING_POWER);
	}

	@Test
	public void transition_upperEnergyLimitExceeded_returnsFixedEnergyDeltas() {
		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0, 0, 475);
		double result = device.transition(defaultTime, 150, oneHour);
		assertEquals(50, result, 1E-12);
		assertEquals(500., device.getCurrentInternalEnergyInMWH(), 1E-12);
		logChecker.assertLogsContain(GenericDevice.ERR_EXCEED_UPPER_ENERGY_LIMIT);
	}

	@Test
	public void transition_lowerEnergyLimitExceeded_returnsFixedEnergyDeltas() {
		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0, 0, -450);
		double result = device.transition(defaultTime, -75, oneHour);
		assertEquals(-40, result, 1E-12);
		assertEquals(-500., device.getCurrentInternalEnergyInMWH(), 1E-12);
		logChecker.assertLogsContain(GenericDevice.ERR_EXCEED_LOWER_ENERGY_LIMIT);
	}

	@Test
	public void transition_negativeSelfDischarge_logsError() {
		device = createGenericDevice(100, 100, 0.5, 0.8, 500, -500, 0.1, 0, -450);
		device.transition(defaultTime, 100, oneHour);
		logChecker.assertLogsContain(GenericDevice.ERR_NEGATIVE_SELF_DISCHARGE);
	}
}
