// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import agents.markets.FuelsMarket.FuelType;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Offer or request for an amount of fuel of a certain type at a given time
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class FuelBid extends AmountAtTime {
	public enum BidType {
		Supply, Demand
	}

	public final BidType bidType;
	public final FuelType fuelType;

	/** Constructs a new {@link FuelBid}
	 * 
	 * @param timeStamp for which this fuel bid is valid at
	 * @param amount of fuel to be sold or purchased in thermal MWH
	 * @param bidType whether fuel is to be offered or purchased
	 * @param fuelType type of fuel this bid is associated with */
	public FuelBid(TimeStamp timeStamp, double amount, BidType bidType, FuelType fuelType) {
		super(timeStamp, amount);
		this.bidType = bidType;
		this.fuelType = fuelType;
	}

	/** Mandatory for deserialisation of {@link DataItem}s
	 * 
	 * @param proto protobuf representation */
	public FuelBid(ProtoDataItem proto) {
		super(proto);
		bidType = BidType.values()[proto.getIntValue(0)];
		fuelType = FuelType.values()[proto.getIntValue(1)];
	}

	@Override
	protected void fillDataFields(Builder builder) {
		super.fillDataFields(builder);
		builder.addIntValue(bidType.ordinal());
		builder.addIntValue(fuelType.ordinal());
	}
}
