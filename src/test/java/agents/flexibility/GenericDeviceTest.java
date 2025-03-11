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
import de.dlr.gitlab.fame.time.TimeStamp;

public class GenericDeviceTest {

	@Mock private ParameterData parameterDataMock;
	private AutoCloseable closable;
	private GenericDevice device;
	private TimeStamp defaultTime = new TimeStamp(0L);

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
		return new TimeSeries(builder.build());
	}

	@Test
	public void getEnergyContentUpperLimitInMWH_returnsConfiguredValue() {
		device = createGenericDevice(0, 0, 0, 0, 42, 0, 0, 0, 0);
		assertEquals(42, device.getEnergyContentUpperLimitInMWH(defaultTime));
	}
}
