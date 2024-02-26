// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Specifies an arbitrary amount at a specific time
 * 
 * @author Leonard Willeke, Johannes Kochems */
public class PpaInformation extends DataItem {
	/** The time the DataItem is valid at */
	public final TimeStamp validAt;
	/** The price for which energy is exchanged between the contract parties */
	public final double price;
	/** The amount of energy to be exchanged between the contract parties */
	public final double yieldPotential;

	/** Creates a new {@link PpaInformation} message
	 * 
	 * @param timeStamp to which the specified amount is associated with
	 * @param price value associated with the given timeStamp
	 * @param yieldPotential energy yield value associated with the given timeStamp */
	public PpaInformation(TimeStamp timeStamp, double price, double yieldPotential) {
		this.validAt = timeStamp;
		this.price = price;
		this.yieldPotential = yieldPotential;
	}

	/** Mandatory for deserialisation of {@link DataItem}s
	 * 
	 * @param proto protobuf representation */
	public PpaInformation(ProtoDataItem proto) {
		this.validAt = new TimeStamp(proto.getLongValue(0));
		this.price = proto.getDoubleValue(0);
		this.yieldPotential = proto.getDoubleValue(1);
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addLongValue(validAt.getStep());
		builder.addDoubleValue(price);
		builder.addDoubleValue(yieldPotential);
	}
}