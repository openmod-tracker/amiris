package communications.message;

import agents.forecast.SensitivityForecastProvider.Type;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;

public class ForecastRequest extends DataItem {
	public final Type type;

	public ForecastRequest(Type type) {
		this.type = type;
	}

	public ForecastRequest(ProtoDataItem proto) {
		type = Type.values()[proto.getIntValues(0)];
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addIntValues(type.ordinal());
	}
}
