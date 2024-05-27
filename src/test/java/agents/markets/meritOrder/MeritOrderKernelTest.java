// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets.meritOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static testUtils.Exceptions.assertThrowsMessage;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import agents.markets.meritOrder.MeritOrderKernel.MeritOrderClearingException;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.OrderBookItem;
import agents.markets.meritOrder.books.SupplyOrderBook;

public class MeritOrderKernelTest {

	@Test
	public void clearMarketSimple_zeroSupply_throws() {
		SupplyOrderBook supplyBook = mock(SupplyOrderBook.class);
		DemandOrderBook demandBook = mock(DemandOrderBook.class);
		ArrayList<OrderBookItem> supplyItems = mockBookItemsPower(0., 0., 0.);
		ArrayList<OrderBookItem> demandItems = mockBookItemsPower(1, 2, 3);
		when(supplyBook.getOrderBookItems()).thenReturn(supplyItems);
		when(demandBook.getOrderBookItems()).thenReturn(demandItems);
		assertThrowsMessage(MeritOrderClearingException.class, MeritOrderKernel.ERR_NON_POSITIVE_ORDER_BOOK,
				() -> MeritOrderKernel.clearMarketSimple(supplyBook, demandBook));
	}

	private ArrayList<OrderBookItem> mockBookItemsPower(double... powerValues) {
		ArrayList<OrderBookItem> orderBookItems = new ArrayList<>();
		double total = 0;
		for (double powerValue : powerValues) {
			OrderBookItem orderBookItem = mock(OrderBookItem.class);
			when(orderBookItem.getCumulatedPowerUpperValue()).thenReturn(total + powerValue);
			orderBookItems.add(orderBookItem);
			total += powerValue;
		}
		return orderBookItems;
	}

	@Test
	public void clearMarketSimple_zeroDemand_throws() {
		SupplyOrderBook supplyBook = mock(SupplyOrderBook.class);
		DemandOrderBook demandBook = mock(DemandOrderBook.class);
		ArrayList<OrderBookItem> supplyItems = mockBookItemsPower(1, 2, 3);
		ArrayList<OrderBookItem> demandItems = mockBookItemsPower(0, 0, 0);
		when(supplyBook.getOrderBookItems()).thenReturn(supplyItems);
		when(demandBook.getOrderBookItems()).thenReturn(demandItems);
		assertThrowsMessage(MeritOrderClearingException.class, MeritOrderKernel.ERR_NON_POSITIVE_ORDER_BOOK,
				() -> MeritOrderKernel.clearMarketSimple(supplyBook, demandBook));
	}

	@Test
	public void clearMarketSimple_positiveDemandAndSupply_returnsCut() throws MeritOrderClearingException {
		SupplyOrderBook supplyBook = mock(SupplyOrderBook.class);
		DemandOrderBook demandBook = mock(DemandOrderBook.class);
		ArrayList<OrderBookItem> supplyItems = mockBookItemsPowerAndPrice(
				new double[] {100, 0}, new double[] {21, Double.MAX_VALUE});
		ArrayList<OrderBookItem> demandItems = mockBookItemsPowerAndPrice(
				new double[] {50, 0}, new double[] {3000, -Double.MAX_VALUE});
		when(supplyBook.getOrderBookItems()).thenReturn(supplyItems);
		when(demandBook.getOrderBookItems()).thenReturn(demandItems);
		ClearingDetails result = MeritOrderKernel.clearMarketSimple(supplyBook, demandBook);
		assertEquals(21, result.marketPriceInEURperMWH, 1E-10);
		assertEquals(50, result.tradedEnergyInMWH, 1E-10);
	}

	/** @return List of mocked {@link OrderBookItem}s create from given power and price value pairs */
	private ArrayList<OrderBookItem> mockBookItemsPowerAndPrice(double[] powers, double[] prices) {
		ArrayList<OrderBookItem> orderBookItems = new ArrayList<>();
		double total = 0;
		for (int index = 0; index < powers.length; index++) {
			OrderBookItem orderBookItem = mock(OrderBookItem.class);
			when(orderBookItem.getCumulatedPowerUpperValue()).thenReturn(total + powers[index]);
			when(orderBookItem.getOfferPrice()).thenReturn(prices[index]);
			orderBookItems.add(orderBookItem);
			total += powers[index];
		}
		return orderBookItems;
	}
}
