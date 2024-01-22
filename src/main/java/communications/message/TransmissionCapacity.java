package communications.message;

import agents.markets.EnergyExchangeMulti.Region;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;

/** Specifies a transmission capacity of electricity from one region to another.
 * 
 * @author A. Achraf El Ghazi, Felix Nitsch */
public class TransmissionCapacity implements Portable {
	private Region target;
	private double amount;

	/** required for {@link Portable}s */
	public TransmissionCapacity() {}

	/** Constructs a new {@link TransmissionCapacity} object based on:
	 * 
	 * @param target {@link Region}
	 * @param amount of energy that can be maximally transfered from origin {@link Region} to target */
	public TransmissionCapacity(Region target, double amount) {
		this.target = target;
		this.amount = amount;
	}

	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeInts(target.ordinal());
		collector.storeDoubles(amount);
	}

	@Override
	public void populate(ComponentProvider provider) {
		target = Region.values()[provider.nextInt()];
		amount = provider.nextDouble();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof TransmissionCapacity)) {
			return false;
		}
		TransmissionCapacity other = (TransmissionCapacity) o;
		return other.target.equals(target) && other.amount == amount;
	}

	/** @return a deep copy of TransmissionCapacity caller */
	public TransmissionCapacity clone() {
		TransmissionCapacity transmissionCapacity = new TransmissionCapacity();
		transmissionCapacity.target = this.target;
		transmissionCapacity.amount = this.amount;
		return transmissionCapacity;
	}

	public Region getTarget() {
		return target;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
}
