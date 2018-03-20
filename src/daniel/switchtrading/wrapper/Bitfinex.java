package daniel.switchtrading.wrapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.OpenOrder;
import daniel.switchtrading.core.TradeBook;
import daniel.switchtrading.core.UserBalance;

public class Bitfinex extends ExchangeWrapper {

	@Override
	public void setupAuth() {
		// TODO Auto-generated method stub
	
	}

	@Override
	public Set<CurrencyPair> getAllPairs() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserBalance getUserBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TradeBook getTradeBook(CurrencyPair pair) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TradeBook> getTradeBooks(Collection<CurrencyPair> pairs)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OpenOrder doTrade(Currency from, Currency to,
			BigDecimal priceThreshold, BigDecimal amount, CurrencyPair pair) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean closeOrder(long iD) {
		// TODO Auto-generated method stub
		return false;
	}

}
