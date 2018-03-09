package daniel.switchtrading.core;

import java.util.ArrayList;
import java.util.List;

public class PricedTradeStep extends TradeStep {

	private double inAmount;
	
	private List<Trade> trades = new ArrayList<>();

	public List<Trade> getTrades() {
		return trades;
	}
/*
	public double getInAmount() {
		return inAmount;
	}
*/
	public PricedTradeStep(Currency from, Currency to, CurrencyPair relevantPair, double amountIn) {
		super(from, to, relevantPair);
		this.inAmount = amountIn;
	}
	
	void addTrade(Trade t){
		trades.add(t);
	}

	public double getInAmountLeft() {
		double result = inAmount;
		for (Trade trade : trades) {
			result -= trade.in;
		}
		return result;
	}
	
	public PricedTradeStep returnInverse() {
		// TODO Auto-generated method stub
		return new PricedTradeStep(to, from, relevantPair, inAmount);
	}

	public double getOutSum() {
		double result = 0;
		for (Trade trade : trades) {
			result += trade.out;
		}
		return result;
	}
	
	@Override
	public String toString() {
		
		return super.toString() + "\ninAmount: " + inAmount+ "\n" + trades.toString(); 
	}

	public void setInAmount(double outAmount) {
		inAmount = outAmount;
	}

}
