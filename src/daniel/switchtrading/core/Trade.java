package daniel.switchtrading.core;

public class Trade {
	
	public Trade(double inAmountLeft, float price2, boolean fromBase) {
		this.in = inAmountLeft;
		this.price = price2;
		this.fromBase = fromBase;
		out = fromBase ? in / price : in * price;
		
	}
	@Override
	public String toString() {
		return "Trade [fromBase=" + fromBase + ", in=" + in + ", price="
				+ price + ", out=" + out + "]";
	}
	boolean fromBase;
	double in;
	double price;
	double out;

}
