package daniel.switchtrading.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.Position;
import daniel.switchtrading.core.TradeBook;

@objid("a9d973c5-93a8-4f70-9b4d-eba72e089951")
public class Yobit extends ExchangeWrapper {
	@objid("b53df050-f7a9-4896-b7fa-ab56ef813be3")
	String infoCmd = "info/";

	@objid("c6f5f99d-3ecd-4440-993a-5c28e7ba67c9")
	String depthCmd = "depth/";

	@objid("ae5a3fa8-2f1f-4702-bd4a-7f3eef163bab")
	public Yobit() throws MalformedURLException {
		super(new URL("https://yobit.net/api/3/"));
	}

	@objid("344d6a98-63ed-40bf-b131-cfd88571f97b")
	public TradeBook getTradeBook(CurrencyPair pair) throws IOException {

		TradeBook testBook = new TradeBook(null, null, pair);
		if (tradeBooks.contains(testBook)) {
			return tradeBooks.get(tradeBooks.indexOf(testBook));
		}

		String targetString = pair.getSecondCurrency().getToken() + "_"
				+ pair.getBaseCurrency().getToken();
		String completeURL = this.apiUrl + depthCmd + targetString;

		URLConnection openConnection = connect(completeURL);

		InputStream inputStream = openConnection.getInputStream();
		JsonObject depth = Json.createReader(inputStream).readObject()
				.getJsonObject(targetString);

		JsonArray asks, bids;

		asks = depth.getJsonArray("asks");
		bids = depth.getJsonArray("bids");

		TreeSet<Position> asksSet = generateDepthSet(asks, false);
		TreeSet<Position> bidsSet = generateDepthSet(bids, true);
		TradeBook tb = new TradeBook(asksSet, bidsSet, pair);
		if (!tradeBooks.contains(tb)) {
			tradeBooks.add(tb);
		}
		return tb;
	}

	/**
	 * @param completeURL
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected URLConnection connect(String completeURL)
			throws MalformedURLException, IOException {

		try {
			Thread.sleep(300);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		URL targetURL = new URL(completeURL);
		URLConnection openConnection = targetURL.openConnection();
		openConnection.setReadTimeout(0);
		openConnection
				.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36");
		openConnection.connect();
		System.out.println(openConnection.getHeaderField("null"));
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
			openConnection = connect(completeURL);

		}
		return openConnection;
	}

	@objid("0ff4374c-d5e4-4884-934c-20f1e032b085")
	public static void main(String[] args) throws IOException {
		try {
			ExchangeWrapper yobit = new Yobit();
			Set<CurrencyPair> pairs = yobit.getAllPairs();

			TradeBook tb = yobit
					.getTradeBook((CurrencyPair) pairs.toArray()[0]);
			System.out.println(tb);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@objid("a3126395-a6d6-4e5b-b44c-e020521eadb3")
	public Set<CurrencyPair> getAllPairs() throws IOException {
		if (this.allPairs == null || this.allPairs.isEmpty()) {

			URLConnection openConnection = connect(this.apiUrl + infoCmd);
			InputStream inputStream = openConnection.getInputStream();
			JsonObject pairs = Json.createReader(inputStream).readObject()
					.getJsonObject("pairs");

			HashSet<Currency> frontSet = new HashSet<>();
			HashSet<Currency> backSet = new HashSet<>();
			HashSet<CurrencyPair> pairSet = new HashSet<>();

			pairs.forEach((textPair, value) -> {
				System.out.println(textPair);
				String[] split = textPair.split("_");
				String front = split[0], base = split[1];
				Currency frontCurrency = new Currency(front);
				frontSet.add(frontCurrency);

				Currency baseCurrency = new Currency(base);
				backSet.add(baseCurrency);

				CurrencyPair pair = new CurrencyPair(frontCurrency,
						baseCurrency);
				pairSet.add(pair);

			});

			this.allPairs = pairSet;
			this.allCurrencies = frontSet;
			this.baseCurrencies = backSet;

		}
		return this.allPairs;
	}

	@objid("92286ff6-c01a-4a87-9874-88af22b06eb2")
	private TreeSet<Position> generateDepthSet(JsonArray in, boolean isBid) {
		TreeSet<Position> result = new TreeSet<>();
		in.forEach((pos) -> {
			float size = Float.parseFloat(((JsonArray) pos).get(1).toString());
			float price = Float.parseFloat(((JsonArray) pos).get(0).toString());
			Position p = new Position(size, price, isBid);
			result.add(p);
		});
		return result;
	}

	@Override
	public List<TradeBook> getTradeBooks(Collection<CurrencyPair> pairs) throws IOException {
		List<TradeBook> result = new ArrayList<TradeBook>();

		if (pairs.size() > 50) {
			ArrayList<CurrencyPair> helperArray = new ArrayList<CurrencyPair>(
					pairs);

			List<List<CurrencyPair>> yetAnotherList = chunk(helperArray, 50);

			for (List<CurrencyPair> list : yetAnotherList) {
				result.addAll(getTradeBooks(list));
			}
			tradeBooks = result;
			return result;

		}

		String parameterList = "";
		for (CurrencyPair currencyPair : pairs) {
			parameterList += currencyPair.toString() + "-";
		}
		parameterList = parameterList.substring(0, parameterList.length() - 1);
		String completeURL = this.apiUrl + depthCmd + parameterList;
		System.out.println("Complete Url Size: " + completeURL.length());
		URLConnection openConnection = connect(completeURL);
		InputStream inputStream = openConnection.getInputStream();

		JsonObject depth;
		try {
			depth = Json.createReader(inputStream).readObject();

		} catch (Exception e) {

			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStream));
			System.out.println("why no work");
			br.lines().forEach((x) -> System.out.println(x));
			e.printStackTrace();
			return getTradeBooks(pairs);
		}

		for (CurrencyPair currencyPair : pairs) {

			JsonObject jo = depth.getJsonObject(currencyPair.toString());
			if (jo != null) {
				JsonArray asks, bids;
				asks = jo.getJsonArray("asks");
				bids = jo.getJsonArray("bids");
				TreeSet<Position> asksSet = null;

				if (asks != null) {
					asksSet = generateDepthSet(asks, false);
				} else {

					System.out.println(String.format("no one wants to sell %s",
							currencyPair));
				}

				TreeSet<Position> bidsSet = null;
				if (bids != null) {

					bidsSet = generateDepthSet(bids, true);
				} else {
					System.out.println(String.format("no one wants to buy %s",
							currencyPair));

				}
				TradeBook tb = new TradeBook(asksSet, bidsSet, currencyPair);
				result.add(tb);
			} else {
				System.out.println(String.format(
						"couldn't retrive asks and bids for %s", currencyPair));
			}
		}
		tradeBooks = result;
		return result;
	}



	public static <T> List<List<T>> chunk(List<T> input, int chunkSize) {

		int inputSize = input.size();
		int chunkCount = (int) Math.ceil(inputSize / (double) chunkSize);

		Map<Integer, List<T>> map = new HashMap<>(chunkCount);
		List<List<T>> chunks = new ArrayList<>(chunkCount);

		for (int i = 0; i < inputSize; i++) {

			map.computeIfAbsent(i / chunkSize, (ignore) -> {

				List<T> chunk = new ArrayList<>();
				chunks.add(chunk);
				return chunk;

			}).add(input.get(i));
		}
		return chunks;
	}
}
