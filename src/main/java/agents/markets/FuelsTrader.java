// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets;

import communications.message.AmountAtTime;
import communications.message.ClearingTimes;
import communications.message.FuelBid;
import communications.message.FuelCost;
import communications.message.FuelData;
import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;

/** Interface for traders at the {@link FuelsMarket}
 * 
 * @author Christoph Schimeczek */
public interface FuelsTrader extends AgentAbility {
	/** Products of traders interacting with {@link FuelsMarket} */
	@Product
	public static enum Products {
		/** Request for fuel price forecast at a given time and for a given fuel */
		FuelPriceForecastRequest,
		/** Request for fuel price at a given time and for a given fuel */
		FuelPriceRequest,
		/** Total fuel offered to / requested from market */
		FuelBid,
	};

	/** Send a {@link Products#FuelPriceForecastRequest} or {@link Products#FuelPriceRequest} message to the contracted
	 * {@link FuelsMarket}
	 * 
	 * @param contract with the {@link FuelsMarket}
	 * @param fuelData specifies for which type of fuel price (forecasts) are requested
	 * @param clearingTimes specifies at which time(s) price (forecasts) are requested */
	public default void sendFuelPriceRequest(Contract contract, FuelData fuelData, ClearingTimes clearingTimes) {
		fulfilNext(contract, fuelData, clearingTimes);
	}

	/** Reads a {@link FuelsMarket.Products#FuelPriceForecast} or {@link FuelsMarket.Products#FuelPrice} message from a contracted
	 * {@link FuelsMarket}
	 * 
	 * @param message to be read
	 * @return {@link FuelCost} extracted from the message */
	public default FuelCost readFuelPriceMessage(Message message) {
		return message.getDataItemOfType(FuelCost.class);
	}

	/** Send a {@link Products#FuelBid} message to the contracted {@link FuelsMarket}
	 * 
	 * @param contract with the {@link FuelsMarket}
	 * @param fuelBid to be sent */
	public default void sendFuelBid(Contract contract, FuelBid fuelBid) {
		fulfilNext(contract, fuelBid);
	}

	/** Reads a {@link FuelsMarket.Products#FuelBill} message from a contracted {@link FuelsMarket}
	 * 
	 * @param message to be read
	 * @return total cost for fuel in last {@link FuelBid} message (negative on revenues) */
	public default double readFuelBillMessage(Message message) {
		return message.getDataItemOfType(AmountAtTime.class).amount;
	}
}
