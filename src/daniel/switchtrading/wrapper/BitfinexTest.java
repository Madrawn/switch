package daniel.switchtrading.wrapper;

import static org.junit.Assert.*;

import java.beans.Transient;
import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.TradeBook;
import daniel.switchtrading.core.UserBalance;

public class BitfinexTest {

	private ExchangeWrapper bitfinex;
	private CurrencyPair testLTC_BTC = new CurrencyPair(new Currency("ltc"), new Currency("btc"));

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		bitfinex = new Bitfinex();
	}

	@Test
	public void testSetupAuth() {
		bitfinex.setupAuth();
	}
	@Test
	public void testGetAllPairs() {
		try {
			Set<CurrencyPair> result = bitfinex.getAllPairs();
			assertNotNull(result);
			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetUserBalance(){
		bitfinex.setupAuth();
	    UserBalance ub = bitfinex.getUserBalance();
	    assertNotNull(ub);
	    assertTrue(ub.wallet.size()> 0);
	    System.out.println(ub);
	}
	
	@Test
	public void testGetTradeBook() throws IOException{
		TradeBook result = bitfinex.getTradeBook(testLTC_BTC);
		assertNotNull(result);
		try {
			Set<CurrencyPair> results = bitfinex.getAllPairs();
			assertNotNull(results);
			System.out.println(results);
			for (CurrencyPair currencyPair : results) {
				bitfinex.getTradeBook(currencyPair);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
