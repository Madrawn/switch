package daniel.switchtrading.wrapper;

import gnu.trove.set.hash.THashSet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.BiConsumer;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexOrderBuilder;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookFrequency;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookPrecision;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.Wallet;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.OpenOrder;
import daniel.switchtrading.core.Position;
import daniel.switchtrading.core.TradeBook;
import daniel.switchtrading.core.TypeOfWallet;
import daniel.switchtrading.core.UserBalance;

public class Bitfinex extends ExchangeWrapper {
	private BitfinexApiBroker bitfinexApiBroker;

	private final String SYMBOL_DETAILS = "https://api.bitfinex.com/v1/symbols_details";

	private UserBalance userBalance;

	public Bitfinex() {

		bitfinexApiBroker = new BitfinexApiBroker();
		try {
			bitfinexApiBroker.connect();
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setupAuth();

	}

	@Override
	public void setupAuth() {
		String key = null;
		String secret = null;
		try {
			FileReader fr = new FileReader("bitfinexkeys.txt");
			BufferedReader br = new BufferedReader(fr);
			key = br.readLine();
			secret = br.readLine();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bitfinexApiBroker.close();
		bitfinexApiBroker = new BitfinexApiBroker(key, secret);
		try {
			bitfinexApiBroker.connect();
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean authenticated = bitfinexApiBroker.isAuthenticated();
		if (!authenticated) {
			System.exit(-1);
		}
		System.out.println("AUTH: " + authenticated);

	}

	@Override
	public Set<CurrencyPair> getAllPairs() throws IOException {
		// TODO Auto-generated method stub
		if (this.allPairs == null || this.allPairs.isEmpty()) {

			URLConnection openConnection = connect(SYMBOL_DETAILS);
			InputStream inputStream = openConnection.getInputStream();
			JsonArray pairs = Json.createReader(inputStream).readArray();

			Set<Currency> frontSet = new THashSet<Currency>();
			Set<Currency> backSet = new THashSet<Currency>();
			Set<CurrencyPair> pairSet = new THashSet<CurrencyPair>();

			for (JsonValue jsonValue : pairs) {

				// System.out.println(textPair);
				JsonObject asJsonObject = jsonValue.asJsonObject();
				String pairString = asJsonObject.getString("pair");
				if (pairString.contains("edr2")
						|| pairString.contains("rocket")|| pairString.contains("eur")) {

				} else {
					String front = pairString.substring(0, 3), base = pairString
							.substring(3, 6);
					Currency frontCurrency = new Currency(front);
					frontSet.add(frontCurrency);

					Currency baseCurrency = new Currency(base);
					backSet.add(baseCurrency);

					CurrencyPair pair = new CurrencyPair(frontCurrency,
							baseCurrency);
					String jsonNumber = asJsonObject.getString("minimum_order_size");
					BigDecimal minOrderSize = new BigDecimal(jsonNumber);
					pair.setMinOrderSize(minOrderSize);
					if (pair.toString().equals("rur_btc")) {
						System.out.println("Fucking bullshit");
					} else {
						pairSet.add(pair);

					}
				}

			}
			this.allPairs = pairSet;
			this.allCurrencies = frontSet;
			this.baseCurrencies = backSet;

		}
		if (this.allPairs == null) {
			return getAllPairs();
		}

		return this.allPairs;
	}

	@Override
	public UserBalance getUserBalance() {
		//setupAuth();
		if (this.userBalance == null) {
			
			this.userBalance = new UserBalance();
		}

		try {
			Collection<Wallet> wallets = bitfinexApiBroker.getWallets();
			for (Wallet wallet : wallets) {
				daniel.switchtrading.core.Wallet w = new daniel.switchtrading.core.Wallet();
				w.currency = new Currency(wallet.getCurreny().toLowerCase());
				// w.freeBalance = new BigDecimal(wallet.getBalanceAvailable());

				// TODO: is freeBalance needed? we must it request seperatly
				w.freeBalance = w.totalBalance = new BigDecimal(
						wallet.getBalance());
				// w.lockedBalance = w.totalBalance.subtract(w.freeBalance);
				w.lockedBalance = BigDecimal.ZERO;

				switch (wallet.getWalletType()) {
				case "exchange":
					w.typeOfWallet = TypeOfWallet.EXCHANGE;
					break;
				case "funding":
					w.typeOfWallet = TypeOfWallet.FUNDING;

					break;
				case "margin":
					w.typeOfWallet = TypeOfWallet.MARGIN;

					break;

				default:
					break;
				}

				if (userBalance.wallet.contains(w)) {
					userBalance.wallet.remove(w);

				}

				userBalance.wallet.add(w);

			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userBalance;
	}

	@Override
	public void refresh() {
		//don't
	}

	@Override
	public TradeBook updateAndGetTradeBook(CurrencyPair pair)
			throws IOException {
		// don't
		return getTradeBook(pair);
	}
	
	@Override
	public TradeBook getTradeBook(CurrencyPair pair) throws IOException {
		TradeBook testBook = new TradeBook(pair);
		if (tradeBooks.contains(testBook)) {
			// we already are updating
			return tradeBooks.get(tradeBooks.indexOf(testBook));
		} else {

			// first we need to set up the config
			// therefore we need the currency
			OrderbookConfiguration orderbookConfiguration = null;
			BitfinexCurrencyPair target;
			try {
				target = BitfinexCurrencyPair.fromSymbolString("t"
						+ pair.toString().replaceAll("_", ""));
				// then get the config
				orderbookConfiguration = new OrderbookConfiguration(target,
						OrderBookPrecision.P0, OrderBookFrequency.F0, 25);

			} catch (IllegalArgumentException e) {
				// TODO: handle not yet implemented currencies
				e.printStackTrace();
			}
			// now acquire the manager
			OrderbookManager manager = bitfinexApiBroker.getOrderbookManager();

			// we now need to make a callback
			// the callback should update the trade book

			final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback = (
					orderbookConfig, entry) -> {
				synchronized (testBook) {
					// System.out.format("Got entry (%s) for orderbook (%s)\n",
					// entry,
					// orderbookConfig);
					boolean isBid = entry.getAmount() > 0;
					SortedSet<Position> toUpdate = isBid ? testBook.getBids()
							: testBook.getAsks();
					BigDecimal pricePoint = new BigDecimal(entry.getPrice());
					if (entry.getCount() == 0) {
						// we need to delete a position

						Position deleteThis = new Position(BigDecimal.ZERO,
								pricePoint, isBid);
						toUpdate.remove(deleteThis);
					} else {
						// we need to add a position
						BigDecimal size = new BigDecimal(Math.abs(entry
								.getAmount()));
						Position insertThis = new Position(size, pricePoint,
								isBid);
						toUpdate.add(insertThis);
					}
				}

			};
			if (orderbookConfiguration != null) {
				try {
					manager.registerOrderbookCallback(orderbookConfiguration,
							callback);
				} catch (APIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				manager.subscribeOrderbook(orderbookConfiguration);

			}
		}
		tradeBooks.add(testBook);
		return testBook;
	}

	@Override
	public List<TradeBook> getTradeBooks(Collection<CurrencyPair> pairs)
			throws Exception {
		// TODO Auto-generated method stub
		List<TradeBook> result = new ArrayList<TradeBook>();
		for (CurrencyPair currencyPair : pairs) {
			result.add(getTradeBook(currencyPair));
		}
		return result;
	}

	@Override
	public OpenOrder doTrade(Currency from, Currency to,
			BigDecimal priceThreshold, BigDecimal amount, CurrencyPair pair) {
		
		BitfinexCurrencyPair currency = BitfinexCurrencyPair.fromSymbolString("t"
				+ pair.toString().replaceAll("_", ""));
		//if we go from the base we're buying
		System.out.println("We are doing a Trade");
		
		if (from.equals(pair.getBaseCurrency())) {
			System.out.println("From the base");
			
		} else {
			//we're selling
			System.out.println("To the base");
			amount = amount.negate();
		}
		//the amount has to be the non-base currency. so if it costs 0.5 btc and we want to sell 1 BTC worth we need to sell 1 / 0.5 -> 2
		//but out amount is always in the from currency
		//so
		System.out.println(String.format("We're trading with %.4f %s", amount, from));
		if (from.equals(pair.getBaseCurrency())) {
			//we could let bitfinex calc this or at least update the price
			System.out.println("We update our price from " + priceThreshold + " " + pair);
			try {
				priceThreshold = getTradeBook(pair).getLowestAsk().getPrice();
				System.out.println("To " + priceThreshold);
				System.out.println("highest bid: " +getTradeBook(pair).getHighestBid().getPrice() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(String.format("We also need to change %.4f %s to %s", amount, from, to));
			amount = amount.divide(priceThreshold, MathContext.DECIMAL32);
			System.out.println(String.format("Resulting in %.4f %s", amount, to));
		}
		//TODO: fix by trading less?
		System.out.println(String.format("also applying a 0.5 percent discount: %.4f", amount));
		amount = amount.multiply(new BigDecimal(0.995));
		
		final BitfinexOrder order = BitfinexOrderBuilder
				.create(currency, BitfinexOrderType.EXCHANGE_MARKET, amount.doubleValue())
				.build();
				
		try {
			System.out.println(String.format("The order placed is: %.4f %s", amount, to));
			OrderManager orderManager = bitfinexApiBroker.getOrderManager();
			orderManager.placeOrder(order);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//market order so we don't have an open order
		return null;
	}

	@Override
	public boolean closeOrder(long iD) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param completeURL
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected URLConnection connect(String completeURL)
			throws MalformedURLException, IOException {

		URLConnection openConnection = setupConnection(completeURL);
		openConnection.connect();
		openConnection = processHeaders(openConnection);
		return openConnection;
	}

	/**
	 * @param openConnection
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected URLConnection processHeaders(URLConnection openConnection)
			throws MalformedURLException, IOException {
		// System.out.println("Processing headers");
		String tmp = openConnection.getHeaderField("Retry-After");
		if (tmp != null) {
			int ttw = Integer.parseInt(tmp);
			System.out.println(String.format("Told to wait: %s seconds", ttw));
			try {
				Thread.sleep(ttw * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			openConnection = connect(openConnection.getURL().toString());

		}
		return openConnection;
	}

	/**
	 * @param completeURL
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected URLConnection setupConnection(String completeURL)
			throws MalformedURLException, IOException {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		URL targetURL = new URL(completeURL);
		URLConnection openConnection = targetURL.openConnection();
		openConnection.setReadTimeout(15000);
		openConnection
				.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36");
		return openConnection;
	}

	@Override
	public BigDecimal getFeeTolerance() {
		// TODO Auto-generated method stub
		return new BigDecimal(1.01);
	}

}
