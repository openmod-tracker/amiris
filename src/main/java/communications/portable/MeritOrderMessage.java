package communications.portable;

import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Transmit a {@link DemandOrderBook} and {@link SupplyOrderBook} together with a associated timeStamp
 * 
 * @author Evelyn Sperber, Farzad Sarfarazi */
public class MeritOrderMessage implements Portable {
	private SupplyOrderBook supplyOrderBook;
	private DemandOrderBook demandOrderBook;
	private TimeStamp timeStamp;

	/** required for {@link Portable}s */
	public MeritOrderMessage() {}

	/** Creates a {@link MeritOrderMessage} */
	public MeritOrderMessage(SupplyOrderBook supplyOrderBook, DemandOrderBook demandOrderBook, TimeStamp timeStamp) {
		this.supplyOrderBook = supplyOrderBook;
		this.demandOrderBook = demandOrderBook;
		this.timeStamp = timeStamp;
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeComponents(supplyOrderBook);
		collector.storeComponents(demandOrderBook);
		collector.storeLongs(timeStamp.getStep());
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		supplyOrderBook = provider.nextComponent(SupplyOrderBook.class);
		demandOrderBook = provider.nextComponent(DemandOrderBook.class);
		timeStamp = new TimeStamp(provider.nextLong());
	}

	public SupplyOrderBook getSupplyOrderBook() {
		return supplyOrderBook;
	}

	public DemandOrderBook getDemandOrderBook() {
		return demandOrderBook;
	}

	public TimeStamp getTimeStamp() {
		return timeStamp;
	}
}