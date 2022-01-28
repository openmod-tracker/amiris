package communications.message;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import agents.plantOperator.RenewablePlantOperator.SetType;
import agents.policy.SupportPolicy.SupportInstrument;
import agents.trader.ClientData;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Holds the support data needed to calculate the support pay-out by the policy agent after a request by an AggregatorTrader.
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public class SupportRequestData extends DataItem {
	static final String ERR_TIMESTAMP_LEFTOVER = "Accounting period mismatch; No support was requested for dispatch at time stamp: ";

	/** the technology set */
	public final SetType setType;
	/** the support instrument for the technology set */
	public final SupportInstrument supportInstrument;
	/** the amount of energy fed in (all except for capacity premium) or the installed capacity (capacity premium) */
	public final double amount;
	/** the accounting period for calculating the support payments */
	public final TimePeriod accountingPeriod;

	public SupportRequestData(ClientData clientData, TimePeriod accountingPeriod) {
		this.setType = clientData.getTechnologySet().setType;
		this.supportInstrument = clientData.getTechnologySet().supportInstrument;
		switch (supportInstrument) {
			case FIT:
			case MPVAR:
			case MPFIX:
			case CFD:
				this.amount = calcOverallInfeed(clientData, accountingPeriod);
				break;
			case CP:
				this.amount = clientData.getTechnologySet().installedCapacity;
				break;
			default:
				throw new RuntimeException("Support instrument " + supportInstrument + "is not a valid one.");
		}
		this.accountingPeriod = accountingPeriod;
	}

	public SupportRequestData(SupportRequestData supportData) {
		this.setType = supportData.setType;
		this.supportInstrument = supportData.supportInstrument;
		this.amount = supportData.amount;
		this.accountingPeriod = supportData.accountingPeriod;
	}

	/** Calculates and returns the overall infeed of a client's technology set */
	private double calcOverallInfeed(ClientData clientData, TimePeriod accountingPeriod) {
		double overallInfeed = 0;
		TreeMap<TimeStamp, Double> dispatch = clientData.getDispatch();
		Iterator<Entry<TimeStamp, Double>> iterator = dispatch.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<TimeStamp, Double> entry = iterator.next();
			if (entry.getKey().isLessThan(accountingPeriod.getStartTime())) {
				throw new RuntimeException(ERR_TIMESTAMP_LEFTOVER + entry.getKey());
			}
			if (entry.getKey().isLessEqualTo(accountingPeriod.getLastTime())) {
				overallInfeed += entry.getValue();
				iterator.remove();
			}
		}
		return overallInfeed;
	}

	/** Mandatory for deserialisation of {@link DataItem}s
	 * 
	 * @param proto protobuf representation */
	public SupportRequestData(ProtoDataItem proto) {
		int ordinal1 = proto.getIntValue(0);
		this.setType = SetType.values()[ordinal1];
		int ordinal2 = proto.getIntValue(1);
		this.supportInstrument = SupportInstrument.values()[ordinal2];
		this.amount = proto.getDoubleValue(0);
		TimeStamp startTime = new TimeStamp(proto.getLongValue(0));
		TimeSpan duration = new TimeSpan(proto.getLongValue(1));
		this.accountingPeriod = new TimePeriod(startTime, duration);
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addIntValue(setType.ordinal());
		builder.addIntValue(supportInstrument.ordinal());
		builder.addDoubleValue(amount);
		builder.addLongValue(accountingPeriod.getStartTime().getStep());
		builder.addLongValue(accountingPeriod.getDuration().getSteps());
	}

	@Override
	public String toString() {
		return "(" + setType + " " + supportInstrument + " " + amount + ")";
	}
}
