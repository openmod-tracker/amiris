package communications.message;

import agents.policy.SupportPolicy.EnergyCarrier;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Yield potential data associated with an energy carrier
 * 
 * @author Johannes Kochems */
public class YieldPotential extends AmountAtTime {
	/** the energy carrier */
	public final EnergyCarrier energyCarrier;

	/** Constructs a new {@link YieldPotential} */
	public YieldPotential(TimeStamp timeStamp, double amount, EnergyCarrier energyCarrier) {
		super(timeStamp, amount);
		this.energyCarrier = energyCarrier;
	}

	/** Constructs a new {@link YieldPotential} from its protobuf representation */
	public YieldPotential(ProtoDataItem proto) {
		super(proto);
		this.energyCarrier = EnergyCarrier.values()[proto.getIntValue(0)];
	}

	@Override
	protected void fillDataFields(Builder builder) {
		super.fillDataFields(builder);
		builder.addIntValue(energyCarrier.ordinal());
	}
}
