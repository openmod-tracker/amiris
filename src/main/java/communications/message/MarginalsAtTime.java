// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import de.dlr.gitlab.fame.time.TimeStamp;

public class MarginalsAtTime implements Portable {
	/** Which power plant agent is associated with these marginals */
	private long producerUuid;
	/** Begin of the delivery interval */
	private TimeStamp deliveryTime;
	/** Marginals of electricity production associated with its producer */
	private List<Marginal> marginals;

	/** required for {@link Portable}s */
	public MarginalsAtTime() {}

	public MarginalsAtTime(long producerUuid, TimeStamp deliveryTime, List<Marginal> marginals) {
		this.producerUuid = producerUuid;
		this.deliveryTime = deliveryTime;
		this.marginals = marginals;
	}

	public MarginalsAtTime(long producerUuid, TimeStamp deliveryTime, Marginal... marginals) {
		this.producerUuid = producerUuid;
		this.deliveryTime = deliveryTime;
		this.marginals = Arrays.asList(marginals);
	}

	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeLongs(producerUuid);
		collector.storeComponents(deliveryTime);
		collector.storeInts(marginals.size());
		for (Marginal marginal : marginals) {
			collector.storeComponents(marginal);
		}
	}

	@Override
	public void populate(ComponentProvider provider) {
		producerUuid = provider.nextLong();
		deliveryTime = provider.nextComponent(TimeStamp.class);
		int numberOfMarginals = provider.nextInt();
		marginals = new ArrayList<Marginal>(numberOfMarginals);
		for (int i = 0; i < numberOfMarginals; i++) {
			marginals.add(provider.nextComponent(Marginal.class));
		}
	}

	/** @return UUID of power plant associated with the marginals herein */
	public long getProducerUuid() {
		return producerUuid;
	}

	/** @return the begin of the delivery interval */
	public TimeStamp getDeliveryTime() {
		return deliveryTime;
	}

	/** Marginals of electricity production associated with its producer */
	public List<Marginal> getMarginals() {
		return marginals;
	}

	/** @return sum of all power potentials in the marginals */
	public double getTotalPowerPotentialInMW() {
		return marginals.stream().mapToDouble(e -> e.getPowerPotentialInMW()).sum();
	}
}
