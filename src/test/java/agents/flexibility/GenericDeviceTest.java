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
	public void transition_noSelfDischargeFullEfficiencyNoInflow_correctNewEnergyContent() {
		device = createGenericDevice(100, 100, 1, 1, 500, -500, 0., 0, 0);
		device.transition(defaultTime, 50, oneHour);
		assertEquals(50, device.getCurrentInternalEnergyInMWH(), 1E-12);
	}

	@Test
	public void transition_noSelfDischargeFullEfficiencyNoInflowQuarterHour_correctNewEnergyContent() {
		device = createGenericDevice(100, 100, 1, 1, 500, -500, 0., 0, 0);
		device.transition(defaultTime, 100, quarterHour);
		assertEquals(25, device.getCurrentInternalEnergyInMWH(), 1E-12);
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
