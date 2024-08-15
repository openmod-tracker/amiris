// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Transfers but a single TimeStamp
 *
 * @author Christoph Schimeczek */
public class PointInTime extends DataItem {
	/** The transferred TimeStamp */
	public final TimeStamp validAt;

	/** Creates this {@link PointInTime}
	 * 
	 * @param timeStamp to be transferred */
	public PointInTime(TimeStamp validAt) {
		this.validAt = validAt;
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addLongValue(validAt.getStep());
	}

	/** Mandatory for deserialisation of {@link DataItem}s
	 * 
	 * @param proto protobuf representation */
	public PointInTime(ProtoDataItem proto) {
		this.validAt = new TimeStamp(proto.getLongValue(0));
	}
}