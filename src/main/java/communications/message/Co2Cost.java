package communications.message;

import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.time.TimeStamp;

/** An {@link AmountAtTime} specialising on co2 cost
 *
 * @author Christoph Schimeczek */
public class Co2Cost extends AmountAtTime {
	public Co2Cost(TimeStamp timeStamp, double amount) {
		super(timeStamp, amount);
	}

	public Co2Cost(ProtoDataItem proto) {
		super(proto);
	}
}