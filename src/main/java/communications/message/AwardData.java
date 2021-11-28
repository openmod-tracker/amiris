package communications.message;

import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Award information returned from market clearing */
public class AwardData extends DataItem {
	/** the energy awarded from a demand bid */
	public final double demandEnergyInMWH;
	/** the energy awarded from a supply bid */
	public final double supplyEnergyInMWH;
	/** the power price for the particular delivery time */
	public final double powerPriceInEURperMWH;
	/** the begin of the delivery interval (hour) */
	public final TimeStamp beginOfDeliveryInterval;

	/** Constructs a new {@link AwardData} */
	public AwardData(double supplyEnergyInMWH, double demandEnergyInMWH, double powerPriceInEURperMWH,
			TimeStamp timeStamp) {
		this.demandEnergyInMWH = demandEnergyInMWH;
		this.supplyEnergyInMWH = supplyEnergyInMWH;
		this.powerPriceInEURperMWH = powerPriceInEURperMWH;
		this.beginOfDeliveryInterval = timeStamp;
	}

	/** Constructs a new {@link AwardData} from its protobuf representation */
	public AwardData(ProtoDataItem proto) {
		demandEnergyInMWH = proto.getDoubleValue(0);
		supplyEnergyInMWH = proto.getDoubleValue(1);
		powerPriceInEURperMWH = proto.getDoubleValue(2);
		beginOfDeliveryInterval = new TimeStamp(proto.getLongValue(0));
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addDoubleValue(demandEnergyInMWH);
		builder.addDoubleValue(supplyEnergyInMWH);
		builder.addDoubleValue(powerPriceInEURperMWH);
		builder.addLongValue(beginOfDeliveryInterval.getStep());
	}
}