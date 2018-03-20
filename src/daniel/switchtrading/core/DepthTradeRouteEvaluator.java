package daniel.switchtrading.core;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.TreeSet;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.wrapper.ExchangeWrapper;

@objid("e2d62601-b233-4967-a30a-74b1425f53cf")
public class DepthTradeRouteEvaluator extends TradeRouteEvaluator {

	private static final BigDecimal FEE = new BigDecimal(0.998);

	public DepthTradeRouteEvaluator(ExchangeWrapper exchangeWrapper) {
		super(exchangeWrapper);
	}

	@Override
	public BigDecimal calcOut(PricedTradeStep step, TradeBook tradeBook, Currency from,
			Currency to) throws TooFewPositionsException  {
		// means we're buying with the base
		boolean isFromBase = from.equals(tradeBook.getBase());
		BigDecimal outAmount = BigDecimal.ZERO;
		TreeSet<Position> tempSet = null;
		BigDecimal newAmount;
		step.cleanTrades();
		while (step.getInAmountLeft().compareTo(BigDecimal.ZERO) > 0) {
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
				BigDecimal inLeft = step.getInAmountLeft();
				BigDecimal fillable = pos.getSize().multiply(pos.getPrice());
				newAmount = inLeft.subtract(fillable);
				//System.out.println(String.format("%s = %s - %s | %s", newAmount, inAmount, pos.getSize(), pos));
				if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
					outAmount = outAmount.add(step.getInAmountLeft().divide(pos.getPrice(), MathContext.DECIMAL32).multiply(FEE));
					step.addTrade(new Trade(step.getInAmountLeft(), pos.getPrice(),isFromBase));

				} else {
					outAmount = outAmount.add((pos.getSize().divide(pos.getPrice(),MathContext.DECIMAL32).multiply(FEE)));
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
				BigDecimal left = step.getInAmountLeft();
				newAmount = left.subtract(pos.getSize());
				//System.out.println(String.format("%s = %s - %s | %s", newAmount, inAmount, pos.getSize(), pos));
				if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
					outAmount = outAmount.add((step.getInAmountLeft().multiply(pos.getPrice()).multiply(FEE)));
					step.addTrade(new Trade(step.getInAmountLeft(), pos.getPrice(),isFromBase));

				} else {
					
					
					outAmount = outAmount.add(pos.getSize().multiply(pos.getPrice()).multiply(FEE));
					step.addTrade(new Trade(pos.getSize(), pos.getPrice(),isFromBase));
				}

			}
		}

		return step.getOutSum();
	}
}
