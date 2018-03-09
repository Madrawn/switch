package daniel.switchtrading.core.tests;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.DepthTradeRouteEvaluator;
import daniel.switchtrading.core.Position;
import daniel.switchtrading.core.PricedTradeRoute;
import daniel.switchtrading.core.PricedTradeStep;
import daniel.switchtrading.core.TooFewPositionsException;
import daniel.switchtrading.core.TradeBook;
import daniel.switchtrading.wrapper.Yobit;

public class DepthTradeRouteEvaluatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCalcOut() {

		DepthTradeRouteEvaluator testobj = new DepthTradeRouteEvaluator(null);
		Currency to = new Currency("ban");
		Currency from = new Currency("nan");
		CurrencyPair relevantPair = new CurrencyPair(to, from);

		TreeSet<Position> asks = new TreeSet<>();
		asks.add(new Position(50, 10, false));
		asks.add(new Position(25, 15, false));
		asks.add(new Position(25, 20, false));

		TreeSet<Position> bids = new TreeSet<>();
		bids.add(new Position(50, 10, true));
		bids.add(new Position(25, 5, true));
		bids.add(new Position(25, 2, true));

		TradeBook tradeBook = new TradeBook(asks, bids, relevantPair);
		double amountIn = 100;
		PricedTradeStep step = new PricedTradeStep(from, to, relevantPair,
				amountIn);
		PricedTradeStep step2 = new PricedTradeStep(to, from, relevantPair,
				amountIn);
		try {
			testobj.calcOut(step, tradeBook, from, to);
			testobj.calcOut(step2, tradeBook, to, from);
			assertTrue(!step.getTrades().isEmpty());
			assertTrue(step.getTrades().size() == 3);
			assertTrue(!step2.getTrades().isEmpty());
			assertTrue(step2.getTrades().size() == 3);

		} catch (TooFewPositionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testEvaluate() {
		Currency from = new Currency("eth");
		Currency to = new Currency("xsy");
		CurrencyPair relevantPair = new CurrencyPair(to, from);
		double amountIn = 0.01333;
		PricedTradeStep s = new PricedTradeStep(from, to, relevantPair,
				amountIn);
		PricedTradeRoute t = new PricedTradeRoute(s);
		DepthTradeRouteEvaluator d;
		try {
			d = new DepthTradeRouteEvaluator(new Yobit());
			d.evaluate(t);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(t);
	}

}
