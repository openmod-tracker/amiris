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

/** Forecasts sensitivities for generic flexibility options
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class FlexibilityForecaster extends MarketForecaster implements SensitivityForecastProvider {
	static final String ERR_SENSITIVITY_MISSING = "Type of sensitivity not implemented: ";

	private final FlexibilityAssessor flexibilityAssessor;
	private final HashMap<Long, ForecastType> registeredClients = new HashMap<>();

	public FlexibilityForecaster(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		flexibilityAssessor = new FlexibilityAssessor();

		call(this::registerClients).onAndUse(SensitivityForecastClient.Products.Registration);
		call(this::updateForecastMultipliers).onAndUse(SensitivityForecastClient.Products.Award);
		call(this::sendSensitivityForecasts).on(SensitivityForecastProvider.Products.SensitivityForecast)
				.use(SensitivityForecastClient.Products.SensitivityRequest);
	}

	private void registerClients(ArrayList<Message> input, List<Contract> contracts) {
		for (Message message : input) {
			long clientId = message.getSenderId();
			var registration = message.getDataItemOfType(ForecastClientRegistration.class);
			flexibilityAssessor.registerInstalledPower(clientId, registration.amount);
			registeredClients.put(clientId, registration.type);
		}
	}

	private void updateForecastMultipliers(ArrayList<Message> input, List<Contract> contracts) {
		for (Message message : input) {
			AmountAtTime amount = message.getDataItemOfType(AmountAtTime.class);
			flexibilityAssessor.saveAward(message.getSenderId(), amount);
		}
		flexibilityAssessor.processAwards();
	}

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
	}

	private Sensitivity getSensitivity(ForecastType type, MarketClearingResult clearingResult, double multiplier) {
		var assessor = new MeritOrderAssessor(clearingResult.getSupplyBook(), clearingResult.getDemandBook(), type);
		return new Sensitivity(assessor, multiplier);
	}
}
