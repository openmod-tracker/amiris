// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import agents.forecast.MarketForecaster;
import agents.markets.meritOrder.MarketClearingResult;
import communications.message.AmountAtTime;
import communications.message.ForecastClientRegistration;
import communications.message.PointInTime;
import communications.portable.Sensitivity;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Forecasts sensitivities of merit order for generic flexibility options
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class SensitivityForecaster extends MarketForecaster implements SensitivityForecastProvider {
	static final String ERR_SENSITIVITY_MISSING = "Type of sensitivity not implemented: ";

	private final FlexibilityAssessor flexibilityAssessor;
	private final HashMap<Long, ForecastType> registeredClients = new HashMap<>();

	/** Instantiate a new {@link SensitivityForecaster}
	 * 
	 * @param dataProvider input from config
	 * @throws MissingDataException if any required data is not provided */
	public SensitivityForecaster(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		flexibilityAssessor = new FlexibilityAssessor();

		call(this::registerClients).onAndUse(SensitivityForecastClient.Products.ForecastRegistration);
		call(this::updateForecastMultipliers).onAndUse(SensitivityForecastClient.Products.NetAward);
		call(this::sendSensitivityForecasts).on(SensitivityForecastProvider.Products.SensitivityForecast)
				.use(SensitivityForecastClient.Products.SensitivityRequest);
	}

	/** Register clients that sent a registration message */
	private void registerClients(ArrayList<Message> input, List<Contract> contracts) {
		for (Message message : input) {
			long clientId = message.getSenderId();
			var registration = message.getDataItemOfType(ForecastClientRegistration.class);
			flexibilityAssessor.registerInstalledPower(clientId, registration.amount);
			registeredClients.put(clientId, registration.type);
		}
	}

	/** Save net awards sent by clients and update their forecast multiplier history */
	private void updateForecastMultipliers(ArrayList<Message> input, List<Contract> contracts) {
		for (Message message : input) {
			AmountAtTime amount = message.getDataItemOfType(AmountAtTime.class);
			flexibilityAssessor.saveAward(message.getSenderId(), amount);
		}
		flexibilityAssessor.processAwards();
	}

	/** Calculate new sensitivities, update multiplier averages, and send out new forecasts to clients */
	private void sendSensitivityForecasts(ArrayList<Message> messages, List<Contract> contracts) {
		for (Contract contract : contracts) {
			long clientId = contract.getReceiverId();
			double multiplier = flexibilityAssessor.getMultiplier(clientId);
			ArrayList<Message> requests = CommUtils.extractMessagesFrom(messages, clientId);
			for (Message message : requests) {
				TimeStamp time = message.getDataItemOfType(PointInTime.class).validAt;
				MarketClearingResult clearingResult = getResultForRequestedTime(time);
				Sensitivity sensitivity = getSensitivity(registeredClients.get(clientId), clearingResult, multiplier);
				fulfilNext(contract, sensitivity, new PointInTime(time));
			}
		}
		flexibilityAssessor.clearBefore(now());
		saveNextForecast();
	}

	/** Create the requested type of sensitivity based on a specified market clearing result using the given multiplier */
	private Sensitivity getSensitivity(ForecastType type, MarketClearingResult clearingResult, double multiplier) {
		MeritOrderAssessment assessor = MeritOrderAssessment.build(type);
		assessor.assess(clearingResult);
		return new Sensitivity(assessor, multiplier);
	}
}
