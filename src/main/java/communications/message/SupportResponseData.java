package communications.message;

import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;

/** Holds the response of the support policy agent to a SupportRequestData item, i.e. the actual support payed out
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public class SupportResponseData extends SupportRequestData {
	/** the support payed out */
	public final double payment;
	/** the market premium (if applicable, i.e. a MPVAR, MPFIX or CFD scheme) */
	public final double marketPremium;

	/** Creates a {@link SupportResponseData} */
	public SupportResponseData(SupportRequestData supportRequestData, double payment, double marketPremium) {
		super(supportRequestData);
		this.payment = payment;
		this.marketPremium = marketPremium;
	}

	/** Constructs a new {@link SupportRequestData} from ProtoBuffer */
	public SupportResponseData(ProtoDataItem proto) {
		super(proto);
		this.payment = proto.getDoubleValue(1);
		this.marketPremium = proto.getDoubleValue(2);
	}

	@Override
	protected void fillDataFields(Builder builder) {
		super.fillDataFields(builder);
		builder.addDoubleValue(payment);
		builder.addDoubleValue(marketPremium);
	}

}
