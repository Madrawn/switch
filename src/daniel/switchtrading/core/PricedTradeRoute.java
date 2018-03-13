package daniel.switchtrading.core;

import java.util.List;

public class PricedTradeRoute extends TradeRoute {

	
	public PricedTradeRoute(List<PricedTradeStep> list) {
		super(list);
	}

	public PricedTradeRoute(PricedTradeStep start) {
		super(start);
	}

	public void setInAmount(double in) {
		((PricedTradeStep)getFirst()).setInAmount(in);
	}
}
