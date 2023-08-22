// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import agents.plantOperator.RenewablePlantOperator.SetType;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Holds the response of the support policy agent to a SupportRequestData item, i.e. the actual support payed out
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public class SupportResponseData extends DataItem {
	/** the technology set */
	public final SetType setType;
	/** the accounting period for calculating the support payments */
	public final TimePeriod accountingPeriod;
	/** the support payed out */
	public final double payment;
	/** the market premium (if applicable, i.e. a MPVAR, MPFIX or CFD scheme) */
	public final double marketPremium;

	public SupportResponseData(SupportRequestData supportRequestData, double payment, double marketPremium) {
		this.setType = supportRequestData.setType;
		this.accountingPeriod = supportRequestData.accountingPeriod;
		this.payment = payment;
		this.marketPremium = marketPremium;
	}

	/** Mandatory for deserialisation of {@link DataItem}s
	 * 
	 * @param proto protobuf representation */
	public SupportResponseData(ProtoDataItem proto) {
		this.setType = SetType.values()[proto.getIntValue(0)];
		TimeStamp startTime = new TimeStamp(proto.getLongValue(0));
		TimeSpan duration = new TimeSpan(proto.getLongValue(1));
		this.accountingPeriod = new TimePeriod(startTime, duration);
		this.payment = proto.getDoubleValue(0);
		this.marketPremium = proto.getDoubleValue(1);
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addIntValue(setType.ordinal());
		builder.addLongValue(accountingPeriod.getStartTime().getStep());
		builder.addLongValue(accountingPeriod.getDuration().getSteps());
		builder.addDoubleValue(payment);
		builder.addDoubleValue(marketPremium);
	}
}
