package daniel.switchtrading.wrapper;

import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.OpenOrder;
import daniel.switchtrading.core.TradeBook;
import daniel.switchtrading.core.UserBalance;

@objid("5b28e196-7aef-4698-8465-23d3853c6637")
public abstract class ExchangeWrapper implements API {
	/**
	 * @return
	 */
	public static long getNonce() {
		long nonce = (System.currentTimeMillis() / 100) % 1000000000;
		return nonce;
	}

	@objid("8730ce50-8f3c-46dc-9299-c9b48c5f7fd1")
	protected URL apiUrl;

	@objid("40b34e85-0fdd-471e-9e98-d3787637a0a2")
	protected Set<Currency> baseCurrencies;

	@objid("35b1f23b-a37e-4b7d-8656-b30e309e2355")
	protected Set<Currency> allCurrencies;

	@objid("ce784dc4-9aa2-4144-90bb-a6ef07e28b2c")
	protected Set<CurrencyPair> allPairs;

	protected List<TradeBook> tradeBooks = new ArrayList<>();

	protected List<OpenOrder> openOrders = new ArrayList<>();;

	@objid("02e0694b-9a05-4100-8b0b-b11e647d9922")
	public ExchangeWrapper() {
	}

	@objid("52063b6c-f0f4-49c3-a285-ebfb1b860d53")
	public ExchangeWrapper(URL apiUrl) {
		this.apiUrl = apiUrl;
	}

	@objid("cb6b45eb-417f-46ae-b191-59dc18293b6a")
	public Set<Currency> getAvailableBaseCurrencies() throws IOException {
		if (this.allPairs == null || this.allPairs.isEmpty()) {
			getAllPairs();
		}
		return this.baseCurrencies;
	}

	@objid("090d1b40-f051-4077-9ec4-36e0f1ff008f")
	public Set<CurrencyPair> getTradingPairs(Currency baseCurrency)
			throws IOException {
		if (this.allPairs == null || this.allPairs.isEmpty()) {
			getAllPairs();
		}
		HashSet<CurrencyPair> returnSet = new HashSet<CurrencyPair>();

		this.allPairs.forEach((aPair) -> {
			if (aPair.getBaseCurrency().equals(baseCurrency)) {
				returnSet.add(aPair);

			}
		});
		return returnSet;
	}

	public TradeBook getTradeBook(Currency from, Currency to) throws Exception {
		CurrencyPair p = getAccordingPair(from, to);

		return getTradeBook(p);
	}

	public TradeBook updateAndGetTradeBook(CurrencyPair pair)
			throws IOException {
		TradeBook tmp = new TradeBook(null, null, pair);
		tradeBooks.remove(tmp);

		return getTradeBook(pair);
	}

	public Set<CurrencyPair> deadPairs = new HashSet<>();

	public Set<CurrencyPair> cleanDeadPairs() {
		if (allPairs == null) {
			try {
				getAllPairs();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Set<CurrencyPair> alivePairs = new THashSet<>(allPairs);
		for (TradeBook tradeBook : tradeBooks) {
			if (tradeBook.getBids() == null) {
				CurrencyPair deadPair = tradeBook.getCurrencyPair();
				boolean removed = alivePairs.remove(deadPair);
				deadPairs.add(deadPair);
				System.out.println(String.format(
						"Removed dead pair %s from bids", deadPair));
			}
			if (tradeBook.getAsks() == null) {
				CurrencyPair deadPair = tradeBook.getCurrencyPair();
				deadPairs.add(deadPair);
				boolean removed = alivePairs.remove(deadPair);
				/*
				 * System.out.println(String.format(
				 * "Removed dead pair %s from asks", deadPair));
				 */
			}
		}
		return alivePairs;
	}

	public void refresh() {
		this.allCurrencies = null;
		this.allPairs = null;
		this.tradeBooks = null;
		this.baseCurrencies = null;
	}

	public abstract void setupAuth();

	public abstract OpenOrder doTrade(Currency from, Currency to,
			BigDecimal priceThreshold, BigDecimal amount, CurrencyPair pair);

	public abstract UserBalance getUserBalance();

	public void closeAllTrades() {
		openOrders = getOpenOrders();
		for (OpenOrder openOrder : openOrders) {
			closeOrder(openOrder.ID);
		}

	}

	public abstract boolean closeOrder(long iD);

	public List<OpenOrder> getOpenOrders() {
		return this.openOrders;
	}

	public TradeBook updateAndGetTradeBook(Currency from, Currency to)
			throws Exception {
		CurrencyPair p = getAccordingPair(from, to);

		TradeBook tmp = new TradeBook(null, null, p);
		System.out.println("Deleting TradeBook of " + p);
		tradeBooks.remove(tmp);

		return getTradeBook(p);
	}

	/**
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public CurrencyPair getAccordingPair(Currency from, Currency to)
			throws IOException, Exception {
		CurrencyPair p;
		Set<Currency> basePairs = getAvailableBaseCurrencies();
		if (basePairs.contains(from) && basePairs.contains(to)) {
			p = new CurrencyPair(to, from);
			p = allPairs.contains(p) ? p : new CurrencyPair(from, to);

		} else if (basePairs.contains(from)) {
			p = new CurrencyPair(to, from);
		} else if (basePairs.contains(to)) {
			p = new CurrencyPair(from, to);

		} else {
			throw new Exception("Couldn't find matching pair:" + from + "|"
					+ to);
		}
		return p;
	}

}
