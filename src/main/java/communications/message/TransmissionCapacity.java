// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import agents.markets.DayAheadMarketMultiZone.Region;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;

/** Specifies a transmission capacity of electricity from one region to another.
 * 
 * @author A. Achraf El Ghazi, Felix Nitsch */
public class TransmissionCapacity implements Portable, Cloneable {
	private Region target;
	private double remainingTransferCapacityInMW;

	/** required for {@link Portable}s */
	public TransmissionCapacity() {}

	/** Constructs a new {@link TransmissionCapacity} object based on:
	 * 
	 * @param target {@link Region}
	 * @param amount of energy that can be maximally transfered from origin {@link Region} to target */
	public TransmissionCapacity(Region target, double amount) {
		this.target = target;
		this.remainingTransferCapacityInMW = amount;
	}

	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeInts(target.ordinal());
		collector.storeDoubles(remainingTransferCapacityInMW);
	}

	@Override
	public void populate(ComponentProvider provider) {
		target = Region.values()[provider.nextInt()];
		remainingTransferCapacityInMW = provider.nextDouble();
	}

	/** @return a deep copy of TransmissionCapacity caller */
	public TransmissionCapacity clone() {
		TransmissionCapacity transmissionCapacity = new TransmissionCapacity();
		transmissionCapacity.target = this.target;
		transmissionCapacity.remainingTransferCapacityInMW = this.remainingTransferCapacityInMW;
		return transmissionCapacity;
	}

	/** @return region that is the target of the transmission */
	public Region getTarget() {
		return target;
	}

	/** @return remaining amount of energy that can be transferred to the target region */
	public double getRemainingTransferCapacityInMW() {
		return remainingTransferCapacityInMW;
	}

	/** updates remaining transfer capacity to the given value
	 * 
	 * @param newRemainingTransferCapacityInMW to be used as new value */
	public void setRemainingTransferCapacityInMW(double newRemainingTransferCapacityInMW) {
		this.remainingTransferCapacityInMW = newRemainingTransferCapacityInMW;
	}
}
