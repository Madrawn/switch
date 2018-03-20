package daniel.switchtrading.core;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PricedTradeRoute extends TradeRoute {

	
	public PricedTradeRoute(List<PricedTradeStep> list) {
		super(list);
	}

	public PricedTradeRoute(PricedTradeStep start) {
		super(start);
	}

	public void setInAmount(BigDecimal in) {
		((PricedTradeStep)getFirst()).setInAmount(in);
	}

	public void resetSteps() {
		getList().forEach((x) -> {
			x.cleanTrades();
		});
		
	}


}
