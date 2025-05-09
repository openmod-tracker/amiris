package communications.portable;

import java.util.TreeMap;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;

public class Sensitivity implements Portable {

	private final TreeMap<Double, Double> valuePairs;

	/** required for {@link Portable}s */
	public Sensitivity() {
		valuePairs = new TreeMap<>();
	}

	public Sensitivity(TreeMap<Double, Double> valuePairs) {
		this.valuePairs = valuePairs;
	}

	/** Returns sensitivity value for given requested energy delta
	 * 
	 * @param requestedEnergyInMWH demand &gt; 0; supply &lt; 0
	 * @return sensitivity value */
	public double getValue(double requestedEnergyInMWH) {
		// TODO: Implement
		return 0.;
	}

	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeInts(valuePairs.size());
		for (var entry : valuePairs.entrySet()) {
			collector.storeDoubles(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void populate(ComponentProvider provider) {
		int size = provider.nextInt();
		for (int i = 0; i < size; i++) {
			valuePairs.put(provider.nextDouble(), provider.nextDouble());
		}
	}

}
