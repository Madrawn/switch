package daniel.switchtrading.core;

import java.util.TreeSet;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.wrapper.ExchangeWrapper;

@objid("e2d62601-b233-4967-a30a-74b1425f53cf")
public class DepthTradeRouteEvaluator extends TradeRouteEvaluator {

	public DepthTradeRouteEvaluator(ExchangeWrapper exchangeWrapper) {
		super(exchangeWrapper);
	}

	@Override
	public double calcOut(PricedTradeStep step, TradeBook tradeBook, Currency from,
			Currency to) throws TooFewPositionsException  {
		// means we're buying with the base
		boolean isFromBase = from.equals(tradeBook.getBase());
		float outAmount = 0;
		TreeSet<Position> tempSet = null;
		double newAmount;
		step.cleanTrades();
		while (step.getInAmountLeft() > 0.0) {
			TooFewPositionsException exception = new TooFewPositionsException("To few positions", tradeBook.getCurrencyPair());
			if (isFromBase) {
				if (tempSet == null) {
					// we buy so get lowest ask
					TreeSet<Position> asks = tradeBook.getAsks();
					if (asks == null) {
						System.out.println("Asks seem to be missing");
						throw exception;
					}
					tempSet = new TreeSet<>(asks);
				}
				Position pos = tempSet.pollFirst();
				if (pos == null) {
					throw exception;
				}
				double inLeft = step.getInAmountLeft();
				double fillable = pos.getSize() * pos.getPrice();
				newAmount = inLeft - fillable;
				//System.out.println(String.format("%s = %s - %s | %s", newAmount, inAmount, pos.getSize(), pos));
				if (newAmount <= 0) {
					outAmount += (step.getInAmountLeft() / pos.getPrice())*0.998;
					step.addTrade(new Trade(step.getInAmountLeft(), pos.getPrice(),isFromBase));

				} else {
					outAmount += (pos.getSize() / pos.getPrice())*0.998;
					step.addTrade(new Trade(fillable, pos.getPrice(),isFromBase));
					
				}
			} else {
				if (tempSet == null) {
					TreeSet<Position> bids = tradeBook.getBids();
					if (bids == null) {
						System.out.println("Bids seem to be missing");
						throw exception;
					}
					tempSet = new TreeSet<>(bids);
				}
				Position pos = tempSet.pollFirst();
				if (pos == null) {
					throw exception;
				}
				double left = step.getInAmountLeft();
				newAmount = left - pos.getSize();
				//System.out.println(String.format("%s = %s - %s | %s", newAmount, inAmount, pos.getSize(), pos));
				if (newAmount <= 0) {
					outAmount += (step.getInAmountLeft() * pos.getPrice())*0.998;
					step.addTrade(new Trade(step.getInAmountLeft(), pos.getPrice(),isFromBase));

				} else {
					
					
					outAmount += (pos.getSize() * pos.getPrice())*0.998;
					step.addTrade(new Trade(pos.getSize(), pos.getPrice(),isFromBase));
				}

			}
		}

		return step.getOutSum();
	}
}
