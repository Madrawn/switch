package daniel.switchtrading.core.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.UserBalance;
import daniel.switchtrading.wrapper.ExchangeWrapper;
import daniel.switchtrading.wrapper.Yobit;

public class YobitTest {

	private ExchangeWrapper yobit;
	private Currency currencyBtc;
	private Currency currencyDft;
	private CurrencyPair curPair;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		yobit = new Yobit();
		yobit.setupAuth();
		currencyBtc = new Currency("btc");
		currencyDft = new Currency("dft");
		curPair = new CurrencyPair(currencyDft, currencyBtc);
	}

	@Test
	public void testSetupAuth() {
	}

	@Test
	public void testDoTrade() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserBalance() {
		UserBalance ub = yobit.getUserBalance();
		assertFalse(ub.wallet.isEmpty());
		assertNotNull(ub.wallet.get(0).currency);
		assertNotEquals(ub.wallet.get(0).freeBalance,0);
	}
	@Test
	public void testPlaceTrade() {
		
		//yobit.doTrade(currencyBtc, currencyDft, 0.00017881, 2, curPair);
		
	}

	@Test
	public void testGetTradeBookCurrencyPair() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAllPairs() {
		try {
			Set<CurrencyPair> retrn = yobit.getAllPairs();
			Currency curRur = new Currency("rur");
			Currency curUsd = new Currency("usd");
			Currency curBtc = new Currency("btc");
			
			assertFalse(retrn.contains(new CurrencyPair(curUsd, curBtc)));
			assertFalse(retrn.contains(new CurrencyPair(curRur, curBtc)));
			for (CurrencyPair currencyPair : retrn) {
				assertFalse(currencyPair.toString().equals("rur_btc"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Test
	public void testGetTradeBooks() {
		fail("Not yet implemented");
	}

	@Test
	public void testChunk() {
		fail("Not yet implemented");
	}

}
