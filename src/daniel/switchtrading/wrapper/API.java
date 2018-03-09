package daniel.switchtrading.wrapper;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.core.Currency;
import daniel.switchtrading.core.CurrencyPair;
import daniel.switchtrading.core.TradeBook;

@objid("df1ca247-fb8d-4519-a929-68624d014470")
public interface API {
	@objid("566d55ed-7263-4294-9633-0809aae332e8")
	Set<Currency> getAvailableBaseCurrencies() throws IOException;

	@objid("cb7540d3-241e-42d8-abc6-3eef0ddb0222")
	Set<CurrencyPair> getTradingPairs(final Currency baseCurrency)
			throws IOException;

	@objid("394485e9-509e-45ad-8cb1-b0664c1b6df8")
	TradeBook getTradeBook(final CurrencyPair pair) throws IOException;

	List<TradeBook> getTradeBooks(Collection<CurrencyPair> pairs) throws Exception;

	@objid("4a95cf39-1c63-4ebd-9117-7bdc86706293")
	Set<CurrencyPair> getAllPairs() throws IOException;
	
	void refresh();

}
