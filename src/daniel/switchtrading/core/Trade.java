package daniel.switchtrading.core;

import java.math.BigDecimal;
import java.math.MathContext;

public class Trade {
	
	private BigDecimal fee = new BigDecimal(0.998);
	
	public Trade(BigDecimal inAmountLeft, BigDecimal price2, boolean fromBase) {
		this.in = inAmountLeft;
		this.price = price2;
		this.fromBase = fromBase;
		out = fromBase ? in.divide(price,MathContext.DECIMAL32).multiply(fee) : in.multiply(price).multiply(fee);
		
	}
	@Override
	public String toString() {
		return "Trade [fromBase=" + fromBase + ", in=" + in + ", price="
				+ price + ", out=" + out + "]";
	}
	boolean fromBase;
	BigDecimal in;
	BigDecimal price;
	BigDecimal out;

}
