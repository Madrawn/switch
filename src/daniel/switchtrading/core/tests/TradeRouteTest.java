package daniel.switchtrading.core.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.TradeRoute;
import daniel.switchtrading.core.TradeStep;

public class TradeRouteTest {

	private static TradeRoute test;
	private static CurrencyPair ltc_btc;
	private static CurrencyPair ltc_doge;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Currency btc, ltc, eth, doge;
		btc = new Currency("btc");
		ltc = new Currency("ltc");
		eth = new Currency("eth");
		doge = new Currency("doge");
		
		ltc_btc = new CurrencyPair(ltc, btc);
		CurrencyPair ltc_eth = new CurrencyPair(ltc, eth);
		ltc_doge = new CurrencyPair(ltc, doge);
		CurrencyPair eth_doge = new CurrencyPair(eth, doge);
		
		TradeStep in = new TradeStep(btc, ltc, ltc_btc);
		
		test = new TradeRoute(in);
		
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		assertTrue(test.contains(ltc_btc));
		assertFalse(test.contains(ltc_doge));
	}

}
