package agents.forecast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import communications.message.Amount;
import communications.message.AmountAtTime;
import communications.message.ClearingTimes;
import communications.message.ForecastRequest;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
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

	private void sendSensitivityForecasts(ArrayList<Message> input, List<Contract> contracts) {
		HashMap<Long, Double> multiplierPerClient = new HashMap<>();
		for (Contract contract : contracts) {
			multiplierPerClient.put(contract.getReceiverId(), flexibilityAssessor.getMultiplier(contract.getReceiverId()));
		}
		for (Message message : input) {
			double multiplier = multiplierPerClient.get(message.getSenderId());
			ClearingTimes clearingTimes = message.getDataItemOfType(ClearingTimes.class);
			var times = flexibilityAssessor.getRequiredUpdateTimes(message.getSenderId(), clearingTimes.getTimes(),
					multiplier);
			for (TimeStamp time : times) {
				var sensitivity = getSensitivity(message.getDataItemOfType(ForecastRequest.class).type, time, multiplier);
				// TODO: send sensitivity
			}
		}
		flexibilityAssessor.clearBefore(now());
	}

	private Sensitivity getSensitivity(Type type, TimeStamp time, double multiplier) {
		switch (type) {
			case PriceSensitivity:
				return getPriceSensitivity(time, multiplier);
			default:
				throw new RuntimeException(ERR_SENSITIVITY_MISSING + type);
		}
	}

	private Sensitivity getPriceSensitivity(TimeStamp time, double multiplier) {

	}
}
