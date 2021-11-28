package communications.message;

import agents.markets.FuelsMarket.FuelType;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;
import de.dlr.gitlab.fame.communication.message.DataItem;

/** Transmitting Data concerning a {@link FuelType} */
public class FuelData extends DataItem {
	public final FuelType fuelType;

	/** Constructs a new {@link FuelData} item */
	public FuelData(FuelType fuelType) {
		this.fuelType = fuelType;
	}

	/** Constructs a {@link FuelData} item from its protobuf representation */
	public FuelData(ProtoDataItem proto) {
		int ordinal = proto.getIntValue(0);
		fuelType = FuelType.values()[ordinal];
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addIntValue(fuelType.ordinal());
	}
}