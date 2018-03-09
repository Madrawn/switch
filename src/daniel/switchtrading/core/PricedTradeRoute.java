package daniel.switchtrading.core;

import java.util.LinkedList;
import java.util.List;

public class PricedTradeRoute extends TradeRoute {

	
	public PricedTradeRoute(List<TradeStep> list) {
		super(list);
	}

	public PricedTradeRoute(TradeStep start) {
		super(start);
	}

	public void setInAmount(double in) {
		((PricedTradeStep)getFirst()).setInAmount(in);
	}
}
