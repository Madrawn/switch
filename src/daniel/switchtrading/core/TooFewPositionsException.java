package daniel.switchtrading.core;

public class TooFewPositionsException extends Exception {
	
	private CurrencyPair causeCurrency;

	public CurrencyPair getCauseCurrency() {
		return causeCurrency;
	}

	public TooFewPositionsException(String msg, CurrencyPair currencyPair) {
		super(msg);
		this.causeCurrency = currencyPair;
	}

}
