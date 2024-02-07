// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;
import de.dlr.gitlab.fame.time.TimeStamp;
import de.dlr.gitlab.fame.communication.message.DataItem;
import java.util.Comparator;
import agents.markets.meritOrder.Bid;
import agents.markets.meritOrder.Bid.Type;

/** Transfers a {@link Bid} */
public class BidData extends DataItem {
	/** Compares two {@link BidData} objects with respect to their bidding price (ascending) */
	public static final Comparator<BidData> BY_PRICE_ASCENDING = new Comparator<BidData>() {
		@Override
		public int compare(BidData b1, BidData b2) {
			return Double.compare(b1.priceInEURperMWH, b2.priceInEURperMWH);
		}
	};
	/** the amount of offered energy */
	public final double offeredEnergyInMWH;
	/** the bid price */
	public final double priceInEURperMWH;
	/** the marginal cost information of the producer resp. plant segment */
	public final double marginalCostInEURperMWH;
	/** the actual (perfect foresight) power potential of a supplier */
	public final double powerPotentialInMW;
	/** the unique Id of the trader */
	public final long traderUuid;
	/** the unique Id of the power plant operator */
	public final long producerUuid;
	/** the bid type (demand or supply) */
	public final Type type;
	/** the begin of the delivery interval */
	public final TimeStamp deliveryTime;

	/** Constructs a new {@link BidData}; using producerUuid = -1; marginalCost = powerPotential = Double.NaN
	 * 
	 * @param offeredEnergyInMWH this bids offered energy (either asked for as offered in MWh
	 * @param priceInEURperMWH this bids price in EUR per MWh
	 * @param traderUuid unique ID of the trader associated with this bid
	 * @param type {@link Type} of the bid, either {@link Type#Demand} or {@link Type#Supply}
	 * @param deliveryTime the starting time at which the associated energy is to be delivered */
	public BidData(double offeredEnergyInMWH, double priceInEURperMWH, long traderUuid, Type type,
			TimeStamp deliveryTime) {
		this.offeredEnergyInMWH = offeredEnergyInMWH;
		this.priceInEURperMWH = priceInEURperMWH;
		this.marginalCostInEURperMWH = Double.NaN;
		this.powerPotentialInMW = Double.NaN;
		this.traderUuid = traderUuid;
		this.producerUuid = -1L;
		this.type = type;
		this.deliveryTime = deliveryTime;
	}

	/** Constructs a new {@link BidData}; producerUuid = -1; powerPotential = Double.NaN
	 * 
	 * @param offeredEnergyInMWH this bids offered energy (either asked for as offered in MWh
	 * @param priceInEURperMWH this bids price in EUR per MWh
	 * @param marginalCostInEURperMWH the true marginal cost for producing the offered energy
	 * @param traderUuid unique ID of the trader associated with this bid
	 * @param type {@link Type} of the bid, either {@link Type#Demand} or {@link Type#Supply}
	 * @param deliveryTime the starting time at which the associated energy is to be delivered */
	public BidData(double offeredEnergyInMWH, double priceInEURperMWH, double marginalCostInEURperMWH, long traderUuid,
			Type type, TimeStamp deliveryTime) {
		this.offeredEnergyInMWH = offeredEnergyInMWH;
		this.priceInEURperMWH = priceInEURperMWH;
		this.marginalCostInEURperMWH = marginalCostInEURperMWH;
		this.powerPotentialInMW = Double.NaN;
		this.traderUuid = traderUuid;
		this.producerUuid = -1L;
		this.type = type;
		this.deliveryTime = deliveryTime;
	}

	/** @param offeredEnergyInMWH this bids offered energy (either asked for as offered in MWh
	 * @param priceInEURperMWH this bids price in EUR per MWh
	 * @param marginalCostInEURperMWH the true marginal cost for producing the offered energy in EUR per MWh
	 * @param powerPotentialInMW the true power that the associated generation unit could produce in perfect foresight
	 * @param traderUuid unique ID of the trader associated with this bid
	 * @param producerUuid the unique ID of the associated generation unit
	 * @param type {@link Type} of the bid, either {@link Type#Demand} or {@link Type#Supply}
	 * @param deliveryTime the starting time at which the associated energy is to be delivered */
	public BidData(double offeredEnergyInMWH, double priceInEURperMWH, double marginalCostInEURperMWH,
			double powerPotentialInMW, long traderUuid, long producerUuid, Type type, TimeStamp deliveryTime) {
		this.offeredEnergyInMWH = offeredEnergyInMWH;
		this.priceInEURperMWH = priceInEURperMWH;
		this.marginalCostInEURperMWH = marginalCostInEURperMWH;
		this.powerPotentialInMW = powerPotentialInMW;
		this.traderUuid = traderUuid;
		this.producerUuid = producerUuid;
		this.type = type;
		this.deliveryTime = deliveryTime;
	}

	/** Mandatory for deserialisation of {@link DataItem}s
	 * 
	 * @param proto protobuf representation */
	public BidData(ProtoDataItem proto) {
		this.offeredEnergyInMWH = proto.getDoubleValue(0);
		this.priceInEURperMWH = proto.getDoubleValue(1);
		this.marginalCostInEURperMWH = proto.getDoubleValue(2);
		this.powerPotentialInMW = proto.getDoubleValue(3);
		this.traderUuid = proto.getLongValue(0);
		this.producerUuid = proto.getLongValue(1);
		this.deliveryTime = new TimeStamp(proto.getLongValue(2));
		this.type = Type.values()[proto.getIntValue(0)];
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addDoubleValue(offeredEnergyInMWH);
		builder.addDoubleValue(priceInEURperMWH);
		builder.addDoubleValue(marginalCostInEURperMWH);
		builder.addDoubleValue(powerPotentialInMW);
		builder.addLongValue(traderUuid);
		builder.addLongValue(producerUuid);
		builder.addLongValue(deliveryTime.getStep());
		builder.addIntValue(type.ordinal());
	}

	/** @return {@link Bid} equivalent to this {@link BidData} */
	public Bid getBid() {
		return new Bid(offeredEnergyInMWH, priceInEURperMWH, marginalCostInEURperMWH, traderUuid, type);
	}
}