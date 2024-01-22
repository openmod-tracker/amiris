package agents.markets.meritOrder.books;

import java.util.ArrayList;
import agents.markets.meritOrder.Bid;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;

/** Handles a list of imported/exported Bids in a {@link EnergyExchange} for a single time frame of trading.
 * 
 * @author A. Achraf El Ghazi, Felix Nitsch */
public class TransferOrderBook implements Portable {
	protected ArrayList<Bid> bids = new ArrayList<>();

	/** required for {@link Portable}s */
	public TransferOrderBook() {}

	/** Adds given {@link Bid} to this {@link TransferOrderBook}
	 * 
	 * @param bid to be added to TransferOrderBook */
	public void addBid(Bid bid) {
		bids.add(bid);
	}

	@Override
	public void addComponentsTo(ComponentCollector collector) {
		for (Bid bid : bids) {
			collector.storeComponents(bid);
		}
	}

	@Override
	public void populate(ComponentProvider provider) {
		bids = provider.nextComponentList(Bid.class);
	}

	/** @return a deep copy of TransferOrderBook caller */
	public TransferOrderBook clone() {
		TransferOrderBook transferOrderBook = new TransferOrderBook();
		for (Bid bid : this.bids) {
			transferOrderBook.addBid(bid.clone());
		}
		return transferOrderBook;
	}

	/** @return bids stored in here */
	public ArrayList<Bid> getBids() {
		return bids;
	}

	/** Calculate offered energy total across all bids of this order book for given trader
	 * 
	 * @param traderUuid UUID of trader to calculate energy amount for
	 * @return sum of total offered energy in MWH of given trader */
	public double getEnergySumForTrader(long traderUuid) {
		double energyTotalInMWH = 0;
		for (Bid bid : bids) {
			energyTotalInMWH += bid.getTraderUuid() == traderUuid ? bid.getEnergyAmountInMWH() : 0;
		}
		return energyTotalInMWH;
	}

	/** Computes the total energy of all items of this TransferOrderBook
	 * 
	 * @return the total energy of all bid block powers in MWH */
	public double getAccumulatedEnergyInMWH() {
		double total = 0.0;
		for (Bid bid : bids) {
			total += bid.getEnergyAmountInMWH();
		}
		return total;
	}
}
