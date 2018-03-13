package daniel.switchtrading.core.tests;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.DepthTradeRouteEvaluator;
import daniel.switchtrading.core.PricedTradeRoute;
import daniel.switchtrading.core.PricedTradeStep;
import daniel.switchtrading.core.TradeCircle;
import daniel.switchtrading.wrapper.Yobit;

public class TradeCircleTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		TradeCircle test = new TradeCircle();
		String[] args = {"btc", "3", "Yobit", "0.0001","false"};  
		
		TradeCircle.init(args);
		
		Currency from = new Currency("btc");
		Currency to = new Currency("dft");
		CurrencyPair relevantPair= new CurrencyPair(to, from);
		double amountIn = 0.00016;
		PricedTradeStep start = new PricedTradeStep(from, to, relevantPair, amountIn);
		PricedTradeRoute ptr = new PricedTradeRoute(start);
		
		DepthTradeRouteEvaluator dt = new DepthTradeRouteEvaluator(new Yobit());
		dt.evaluate(ptr);
		
		test.execute(ptr);
	}

	@Test
	public void testExecute() {
		fail("Not yet implemented");
	}

}
