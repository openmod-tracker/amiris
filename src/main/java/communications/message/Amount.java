// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;

public class Amount extends DataItem {
	public final double amount;

	/** Creates a new {@link Amount} message
	 * 
	 * @param amount value */
	public Amount(double amount) {
		this.amount = amount;
	}

	public Amount(ProtoDataItem proto) {
		this.amount = proto.getDoubleValues(0);
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addDoubleValues(amount);
	}
}
