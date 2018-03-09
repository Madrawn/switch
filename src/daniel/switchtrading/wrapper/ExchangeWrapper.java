package daniel.switchtrading.wrapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.TradeBook;

@objid("5b28e196-7aef-4698-8465-23d3853c6637")
public abstract class ExchangeWrapper implements API {
	@objid("8730ce50-8f3c-46dc-9299-c9b48c5f7fd1")
	protected URL apiUrl;

	@objid("40b34e85-0fdd-471e-9e98-d3787637a0a2")
	protected Set<Currency> baseCurrencies;

	@objid("35b1f23b-a37e-4b7d-8656-b30e309e2355")
	protected Set<Currency> allCurrencies;

	@objid("ce784dc4-9aa2-4144-90bb-a6ef07e28b2c")
	protected Set<CurrencyPair> allPairs;

	protected List<TradeBook> tradeBooks = new ArrayList<>();

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
		Set<Currency> basePairs = getAvailableBaseCurrencies();
		CurrencyPair p;
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

		return getTradeBook(p);
	}

	public Set<CurrencyPair> cleanDeadPairs() {
		Set<CurrencyPair> alivePairs = new HashSet<>(allPairs);
		for (TradeBook tradeBook : tradeBooks) {
			if (tradeBook.getBids() == null) {
				CurrencyPair deadPair = tradeBook.getCurrencyPair();
				boolean removed = alivePairs.remove(deadPair);
				System.out.println(String.format("Removed dead pair %s from bids", deadPair));
			}
			if (tradeBook.getAsks() == null ) {
				CurrencyPair deadPair = tradeBook.getCurrencyPair();
				boolean removed = alivePairs.remove(deadPair);
				System.out.println(String.format("Removed dead pair %s from asks", deadPair));
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

}
