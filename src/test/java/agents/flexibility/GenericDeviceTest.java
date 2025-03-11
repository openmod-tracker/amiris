package agents.flexibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.AfterEach;
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

public class GenericDeviceTest {

	@Mock private ParameterData parameterDataMock;
	private AutoCloseable closable;
	private GenericDevice device;
	private TimeStamp defaultTime = new TimeStamp(0L);
	private TimeSpan oneHour = new TimeSpan(1, Interval.HOURS);
	private TimeSpan twoHours = new TimeSpan(2, Interval.HOURS);
	private TimeSpan quarterHour = new TimeSpan(15, Interval.MINUTES);

	@BeforeEach
	public void setUp() throws MissingDataException {
		closable = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	public void tearDown() throws Exception {
		closable.close();
	}

	private GenericDevice createGenericDevice(double chargingPowerInMW, double dischargingPowerInMW,
			double chargingEfficiency, double dischargingEfficiency, double energyContentUpperLimitInMWH,
			double energyContentLowerLimitInMWH, double selfDischargeRatePerHour, double netInflowPowerInMW,
			double currentEnergyContentInMWH) {
		try {
			when(parameterDataMock.getTimeSeries("ChargingPowerInMW")).thenReturn(createSeries(chargingPowerInMW));
			when(parameterDataMock.getTimeSeries("DischargingPowerInMW")).thenReturn(createSeries(dischargingPowerInMW));
			when(parameterDataMock.getTimeSeries("ChargingEfficiency")).thenReturn(createSeries(chargingEfficiency));
			when(parameterDataMock.getTimeSeries("DischargingEfficiency")).thenReturn(createSeries(dischargingEfficiency));
			when(parameterDataMock.getTimeSeries("EnergyContentUpperLimitInMWH"))
					.thenReturn(createSeries(energyContentUpperLimitInMWH));
			when(parameterDataMock.getTimeSeries("EnergyContentLowerLimitInMWH"))
					.thenReturn(createSeries(energyContentLowerLimitInMWH));
			when(parameterDataMock.getTimeSeries("SelfDischargeRatePerHour"))
					.thenReturn(createSeries(selfDischargeRatePerHour));
			when(parameterDataMock.getTimeSeries("NetInflowPowerInMW")).thenReturn(createSeries(netInflowPowerInMW));
			when(parameterDataMock.getDoubleOrDefault("InitialEnergyContentInMWH", 0.)).thenReturn(currentEnergyContentInMWH);
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
		device = createGenericDevice(100, 0, 0, 0, 500, 0, 0, 0, 0);
		assertEquals(100, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, oneHour), 1E-12);
		assertEquals(200, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, twoHours), 1E-12);
		assertEquals(25, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, quarterHour), 1E-12);
	}

	@Test
	public void getMaxTargetEnergyContentInMWH_noSelfDischargeNoInflowInitialNonzero_addsInitial() {
		device = createGenericDevice(100, 0, 0, 0, 500, 0, 0, 0, 0);
		assertEquals(110, device.getMaxTargetEnergyContentInMWH(defaultTime, 10, oneHour), 1E-12);
		assertEquals(190, device.getMaxTargetEnergyContentInMWH(defaultTime, -10, twoHours), 1E-12);
		assertEquals(35, device.getMaxTargetEnergyContentInMWH(defaultTime, 10, quarterHour), 1E-12);
	}

	@Test
	public void getMaxTargetEnergyContentInMWH_noSelfDischargeWithInflowInitialZero_addsInflow() {
		device = createGenericDevice(100, 0, 0, 0, 500, 0, 0, 20, 0);
		assertEquals(120, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, oneHour), 1E-12);
		assertEquals(240, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, twoHours), 1E-12);
		assertEquals(30, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, quarterHour), 1E-12);
	}

	@Test
	public void getMaxTargetEnergyContentInMWH_withSelfDischargeNoInflowInitialZero_subtractsSelfDischarge() {
		device = createGenericDevice(200, 0, 0, 0, 500, 0, 0.1, 0, 0);
		assertEquals(190.476, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, oneHour), 1E-2);
		assertEquals(363.636, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, twoHours), 1E-2);
		assertEquals(49.383, device.getMaxTargetEnergyContentInMWH(defaultTime, 0, quarterHour), 1E-2);
	}

	@Test
	public void simulateTransition_useMaxTargetEnergy_returnsMaxPower() {
		device = createGenericDevice(200, 0, 1, 0, 500, 0, 0.1, 0, 0);
		double targetEnergyLevelInMWH = device.getMaxTargetEnergyContentInMWH(defaultTime, 0, twoHours);
		assertEquals(400, device.simulateTransition(defaultTime, 0, targetEnergyLevelInMWH, twoHours), 1E-12);
	}
	
	@Test
	public void getMaxTargetEnergyContentInMWH_withSelfDischargeInflowAndInitial_correctCalc() {
		device = createGenericDevice(200, 0, 0, 0, 500, 0, 0.1, 50, 0);
		assertEquals(220, device.getMaxTargetEnergyContentInMWH(defaultTime, -20, oneHour), 1E-2);
		device = createGenericDevice(200, 0, 0, 0, 500, 0, 0.1, -50, 0);
		assertEquals(346.364, device.getMaxTargetEnergyContentInMWH(defaultTime, 90, twoHours), 1E-2);
		assertEquals(71.173, device.getMaxTargetEnergyContentInMWH(defaultTime, 35, quarterHour), 1E-2);
	}
}
