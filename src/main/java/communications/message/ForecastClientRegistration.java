// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import agents.forecast.SensitivityForecastProvider.ForecastType;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;

public class ForecastClientRegistration extends DataItem {
	public final double amount;
	public final ForecastType type;

	public ForecastClientRegistration(ForecastType type, double amount) {
		this.type = type;
		this.amount = amount;
	}

	public ForecastClientRegistration(ProtoDataItem proto) {
		type = ForecastType.values()[proto.getIntValues(0)];
		this.amount = proto.getDoubleValues(0);
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addIntValues(type.ordinal());
		builder.addDoubleValues(amount);
	}
}
