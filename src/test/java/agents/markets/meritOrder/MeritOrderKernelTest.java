// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets.meritOrder;

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
		ArrayList<OrderBookItem> supplyItems = mockBookItems(0., 0., 0.);
		ArrayList<OrderBookItem> demandItems = mockBookItems(1, 2, 3);
		when(supplyBook.getOrderBookItems()).thenReturn(supplyItems);
		when(demandBook.getOrderBookItems()).thenReturn(demandItems);
		assertThrowsMessage(MeritOrderClearingException.class, MeritOrderKernel.ERR_NON_POSITIVE_ORDER_BOOK,
				() -> MeritOrderKernel.clearMarketSimple(supplyBook, demandBook));
	}
	
	private ArrayList<OrderBookItem> mockBookItems(double... powerValues) {
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
		ArrayList<OrderBookItem> supplyItems = mockBookItems(1, 2, 3);
		ArrayList<OrderBookItem> demandItems = mockBookItems(0, 0, 0);
		when(supplyBook.getOrderBookItems()).thenReturn(supplyItems);
		when(demandBook.getOrderBookItems()).thenReturn(demandItems);
		assertThrowsMessage(MeritOrderClearingException.class, MeritOrderKernel.ERR_NON_POSITIVE_ORDER_BOOK,
				() -> MeritOrderKernel.clearMarketSimple(supplyBook, demandBook));
	}

	
}
