// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast;

import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;

/** Calculates and provides perfect foresight merit order forecasts;<br>
 * Functionality was moved to {@link MarketForecaster}, thus this class is obsolete and will be removed
 * 
 * @deprecated
 * @author Christoph Schimeczek, Evelyn Sperber, Farzad Sarfarazi, Kristina Nienhaus */
@Deprecated
public class MeritOrderForecaster extends MarketForecaster {
	/** Creates a {@link MeritOrderForecaster}
	 * 
	 * @param dataProvider provides input from file
	 * @throws MissingDataException if any required data is not provided */
	public MeritOrderForecaster(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
	}
}
