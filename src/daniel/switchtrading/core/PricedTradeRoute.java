package daniel.switchtrading.core;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PricedTradeRoute extends TradeRoute {

	public PricedTradeRoute(List<PricedTradeStep> list) {
		super(list);
	}

	public PricedTradeRoute(PricedTradeStep start) {
		super(start);
	}

	public void setInAmount(BigDecimal in) {
		((PricedTradeStep) getFirst()).setInAmount(in);
	}

	public void resetSteps() {
		getList().forEach((x) -> {
			x.cleanTrades();
		});

	}
	/*
	 * public BigDecimal getSmallestPossibleAmountToCompleteTrade(){
	 * 
	 * 
	 * BigDecimal smallestInput = null;
	 * 
	 * //iterare backwards LinkedList<PricedTradeStep> list = getList();
	 * Iterator<PricedTradeStep> it = list.descendingIterator(); for (;
	 * it.hasNext();) { PricedTradeStep pricedTradeStep = (PricedTradeStep)
	 * it.next(); if (smallestInput == null) { //we need this much input to
	 * complete this step smallestInput =
	 * pricedTradeStep.relevantPair.getMinOrderSize(); }else { //we know the
	 * next (right order) needs smallesInput BigDecimal
	 * inputNeededForSmallestInput; pricedTradeStep. inputNeededForSmallestInput
	 * = smallestInput }
	 * 
	 * }
	 * 
	 * 
	 * 
	 * //-1 when unknowable return BigDecimal.ONE.negate(); }
	 */
}
