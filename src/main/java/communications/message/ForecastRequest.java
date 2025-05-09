// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import agents.forecast.SensitivityForecastProvider.ForecastType;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;

public class ForecastRequest extends DataItem {
	public final ForecastType type;

	public ForecastRequest(ForecastType type) {
		this.type = type;
	}

	public ForecastRequest(ProtoDataItem proto) {
		type = ForecastType.values()[proto.getIntValues(0)];
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addIntValues(type.ordinal());
	}
}
