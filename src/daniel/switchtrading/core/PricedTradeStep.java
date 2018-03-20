package daniel.switchtrading.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PricedTradeStep extends TradeStep {

	private BigDecimal inAmount;
	
	private List<Trade> trades = new ArrayList<>();

	public List<Trade> getTrades() {
		return trades;
	}
/*
	public double getInAmount() {
		return inAmount;
	}
*/
	public PricedTradeStep(Currency from, Currency to, CurrencyPair relevantPair, BigDecimal amountIn) {
		super(from, to, relevantPair);
		this.inAmount = amountIn;
	}
	
	void addTrade(Trade t){
		trades.add(t);
	}

	public BigDecimal getInAmountLeft() {
		BigDecimal result = inAmount;
		for (Trade trade : trades) {
			result = result.subtract(trade.in);
		}
		return result;
	}
	
	public PricedTradeStep returnInverse() {
		// TODO Auto-generated method stub
		return new PricedTradeStep(to, from, relevantPair, inAmount);
	}

	public BigDecimal getOutSum() {
		BigDecimal result = BigDecimal.ZERO;
		for (Trade trade : trades) {
			result = result.add(trade.out);
		}
		return result;
	}
	
	@Override
	public String toString() {
		
		return super.toString() + "\ninAmount: " + inAmount+ "\n" + trades.toString(); 
	}

	public void setInAmount(BigDecimal outAmount) {
		inAmount = outAmount;
	}
	public void cleanTrades() {

		this.trades = new ArrayList<>();
	}

}
