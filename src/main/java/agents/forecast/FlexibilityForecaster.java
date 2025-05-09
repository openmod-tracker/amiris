// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import agents.markets.meritOrder.MarketClearingResult;
import communications.message.Amount;
import communications.message.AmountAtTime;
import communications.message.ClearingTimes;
import communications.message.ForecastRequest;
import communications.portable.Sensitivity;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Forecasts sensitivities for generic flexibility options
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class FlexibilityForecaster extends MarketForecaster implements SensitivityForecastProvider {
	@Input private static final Tree parameters = Make.newTree().add(FlexibilityAssessor.updateThresholdParam)
			.buildTree();

	static final String ERR_SENSITIVITY_MISSING = "Type of sensitivity not implemented: ";

	private final FlexibilityAssessor flexibilityAssessor;

	public FlexibilityForecaster(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		flexibilityAssessor = new FlexibilityAssessor(input);

		call(this::registerClients).onAndUse(SensitivityForecastClient.Products.Registration);
		call(this::updateForecastMultipliers).onAndUse(SensitivityForecastClient.Products.Bid);
		call(this::sendSensitivityForecasts).on(SensitivityForecastProvider.Products.SensitivityForecast)
				.use(SensitivityForecastClient.Products.SensitivityRequest);
	}

	private void registerClients(ArrayList<Message> input, List<Contract> contracts) {
		for (Message message : input) {
			flexibilityAssessor.registerClient(message.getSenderId(), message.getDataItemOfType(Amount.class).amount);
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
		HashMap<Long, Double> multiplierPerClient = new HashMap<>();
		for (Contract contract : contracts) {
			multiplierPerClient.put(contract.getReceiverId(), flexibilityAssessor.getMultiplier(contract.getReceiverId()));
			ArrayList<Message> requests = CommUtils.extractMessagesFrom(messages, contract.getReceiverId());
			for (Message message : requests) {
				double multiplier = multiplierPerClient.get(message.getSenderId());
				List<TimeStamp> clearingTimes = message.getDataItemOfType(ClearingTimes.class).getTimes();
				var updateTimes = flexibilityAssessor.getRequiredUpdateTimes(message.getSenderId(), clearingTimes, multiplier);
				for (TimeStamp time : updateTimes) {
					MarketClearingResult clearingResult = getResultForRequestedTime(time);
					ForecastRequest request = message.getDataItemOfType(ForecastRequest.class);
					Sensitivity sensitivity = getSensitivity(request.type, clearingResult, multiplier);
					fulfilNext(contract, sensitivity);
				}
			}
		}
		flexibilityAssessor.clearBefore(now());
	}

	private Sensitivity getSensitivity(Type type, MarketClearingResult clearingResult, double multiplier) {
		var assessor = new MeritOrderAssessor(clearingResult.getSupplyBook(), clearingResult.getDemandBook(), type);
		return new Sensitivity(assessor, multiplier);
	}
}
