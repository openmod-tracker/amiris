// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast;

import agents.markets.DayAheadMarket;
import de.dlr.gitlab.fame.agent.Agent;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.communication.Product;

/** Base class for Day-ahead market forecasters defining their products
 *
 * @author Christoph Schimeczek */
public abstract class Forecaster extends Agent {
	/** Products of {@link DayAheadMarket} {@link Forecaster}s */
	@Product
	public static enum Products {
		/** Perfect foresight merit-order forecasts - issue a Request to obtain it */
		MeritOrderForecast,
		/** Price forecast - issue a Request to obtain it */
		PriceForecast
	}

	/** Creates a new instance
	 * 
	 * @param dataProvider required input data */
	public Forecaster(DataProvider dataProvider) {
		super(dataProvider);
	}
}
