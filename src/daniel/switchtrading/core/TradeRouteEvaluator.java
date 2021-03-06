package daniel.switchtrading.core;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.wrapper.ExchangeWrapper;
import daniel.switchtrading.wrapper.Yobit;

@objid("791f4eb9-4bb5-4f6d-882e-f6358b603397")
public class TradeRouteEvaluator {

	public static void main(String[] args) {
		/*
		 * Currency btc = new Currency("btc"); Currency ltc = new
		 * Currency("eth"); Currency btc2 = new Currency("btc");
		 * System.out.println(btc.equals(btc2)); Set<Currency> test = new
		 * HashSet<Currency>(); test.add(btc);
		 * System.out.println(test.contains(btc2));
		 * 
		 * PricedTradeStep start = new PricedTradeStep(btc, ltc, new
		 * CurrencyPair(ltc, btc), 1); PricedTradeStep end =
		 * start.returnInverse();
		 * 
		 * PricedTradeRoute tr = new PricedTradeRoute(start); try {
		 * tr.addStep(end); ExchangeWrapper exchangeWrapper2 = new Yobit();
		 * TradeRouteEvaluator eval = new TradeRouteEvaluator(exchangeWrapper2);
		 * 
		 * System.out.println("NON-Depth: " + eval.evaluate(tr,false)); start =
		 * new PricedTradeStep(btc, ltc, new CurrencyPair(ltc, btc), 1); end =
		 * start.returnInverse();
		 * 
		 * tr = new PricedTradeRoute(start); tr.addStep(end); eval = new
		 * DepthTradeRouteEvaluator(exchangeWrapper2);
		 * System.out.println("Depth: " + eval.evaluate(tr,false));
		 * System.out.println(tr.toString()); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
	}

	@objid("31c2674a-e675-4592-8c29-0cad2fb711af")
	public ExchangeWrapper exchangeWrapper;

	@objid("8f04f845-e1fe-4b68-90c8-7684835c7a9b")
	private TradeRoute tradeRoute;

	@objid("c788c9e0-60be-43b0-b3cd-baed7f347291")
	public TradeRouteEvaluator(final ExchangeWrapper exchangeWrapper) {
		this.exchangeWrapper = exchangeWrapper;
	}

	@objid("b5bc1023-bf75-49ea-b62f-17b04636ec7c")
	public BigDecimal evaluate(final PricedTradeRoute tradeRoute, boolean reDo)
			throws Exception {
		// System.out.println("CalcOut - Start \nAmount: " + d);
		BigDecimal outAmount = BigDecimal.ZERO;

		for (int i = 0; i < tradeRoute.getList().size(); i++) {
			PricedTradeStep curStep = (PricedTradeStep) tradeRoute.getList()
					.get(i);
			curStep.cleanTrades();
			TradeBook tradeBook;
			if (reDo) {
				//shouldnt we do this in the wrapper?
				//Thread.sleep(500);
				tradeBook = exchangeWrapper.updateAndGetTradeBook(curStep.from,
						curStep.to);
			} else {
				tradeBook = exchangeWrapper.getTradeBook(curStep.from,
						curStep.to);

			}
			outAmount = calcOut(curStep, tradeBook, curStep.from, curStep.to);
			// if out is too small abort and return 0
			if (outAmount.compareTo(new BigDecimal(0.0001)) < 0) {
				return BigDecimal.ZERO;
			}
			if (i + 1 < tradeRoute.getList().size()) {
				((PricedTradeStep) tradeRoute.getList().get(i + 1))
						.setInAmount(outAmount);

			}
			// System.out.println("CalcOut - middle \nAmount: " + outAmount);

		}
		/*
		 * System.out.println("CalcOut - Start Amount: " + outAmount); outAmount
		 * = calcOut(outAmount, exchangeWrapper.getTradeBook(curStep.from,
		 * curStep.to), curStep.from, curStep.to);
		 */

		// System.out.println("END - " + outAmount);
		return outAmount;
	}

	protected BigDecimal calcOut(PricedTradeStep step, TradeBook tradeBook,
			Currency from, Currency to) throws Exception {
		// means we're buying with the base
		boolean isFromBase = from.equals(tradeBook.getBase());
		BigDecimal result;
		if (isFromBase) {
			// we buy so get lowest ask
			BigDecimal price = tradeBook.getLowestAsk().getPrice();
			result = step.getInAmountLeft().divide(price,MathContext.DECIMAL32);
			step.addTrade(new Trade(step.getInAmountLeft(), price, isFromBase));
		} else {
			BigDecimal price = tradeBook.getHighestBid().getPrice();
			result = step.getInAmountLeft().multiply(price);
			step.addTrade(new Trade(step.getInAmountLeft(), price, isFromBase));

		}

		return result;
	}

}
