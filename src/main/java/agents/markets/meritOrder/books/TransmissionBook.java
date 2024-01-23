// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets.meritOrder.books;

import java.util.ArrayList;
import agents.markets.DayAheadMarket;
import agents.markets.DayAheadMarketMultiZone.Region;
import communications.message.TransmissionCapacity;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;

/** Handles a list of transmission capacities of an {@link DayAheadMarket} for a single time frame of trading.
 * 
 * @author A. Achraf El Ghazi, Felix Nitsch */
public class TransmissionBook implements Portable {
	private Region origin;
	private ArrayList<TransmissionCapacity> transmissionCapacities = new ArrayList<>();

	/** required for {@link Portable}s */
	public TransmissionBook() {}

	public TransmissionBook(Region origin) {
		this.origin = origin;
	}

	/** adds the given {@link TransmissionCapacity} to the list of transmissionCapacities
	 * 
	 * @param transmissionCapacity to be added. */
	public void add(TransmissionCapacity transmissionCapacity) {
		this.transmissionCapacities.add(transmissionCapacity);
	}

	/** @return the Region of the corresponding market */
	public Region getOrigin() {
		return origin;
	}

	/** @return the list of TransmissionCapacities in transmissionBookItems */
	public ArrayList<TransmissionCapacity> getTransmissionCapacities() {
		return transmissionCapacities;
	}

	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeInts(origin.ordinal());
		for (TransmissionCapacity transmissionCapacity : transmissionCapacities) {
			collector.storeComponents(transmissionCapacity);
		}
	}

	@Override
	public void populate(ComponentProvider provider) {
		origin = Region.values()[provider.nextInt()];
		transmissionCapacities = provider.nextComponentList(TransmissionCapacity.class);
	}

	/** @return a deep copy of TransmissionBook caller */
	public TransmissionBook clone() {
		TransmissionBook clonedTransmissionBook = new TransmissionBook(origin);
		for (TransmissionCapacity transmissionCapacity : transmissionCapacities) {
			clonedTransmissionBook.add(transmissionCapacity.clone());
		}
		return clonedTransmissionBook;
	}
}
