package daniel.switchtrading.core;

import java.util.List;
import java.util.TreeSet;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("4d0fd530-8ed8-40e8-84e8-4c661c561f53")
public class TradeBook {
    @objid ("b1a44e4c-4ccd-47ca-a8c1-b84c4c99ee0f")
    private int bookDepth;

    @objid ("5bec948d-50d8-436f-a030-421eb9a87d18")
    private TreeSet<Position> bids = new TreeSet<Position>();

    @objid ("3c8b929d-fa6b-42f7-815c-2c4216c18eb3")
    private TreeSet<Position> asks = new TreeSet<Position>();

    @objid ("4b1e540d-c974-45fa-90f8-e9408e913904")
    private CurrencyPair currencyPair;

    public CurrencyPair getCurrencyPair() {
		return currencyPair;
	}

	@objid ("14c1dedc-3a98-4ea5-88f3-6453c584a026")
    public TradeBook(TreeSet<Position> asks, TreeSet<Position> bids, CurrencyPair target) {
        this.currencyPair = target;
        this.bookDepth =asks != null ? asks.size() : 0;
        this.asks = asks;
        this.bids = bids;
    }

    @objid ("4c0a5fa2-ac7f-459c-aca6-39666e46899a")
    @Override
    public String toString() {
        String start = "Asks: \n";
        
        for (Position pos : asks) {
        
            String format = String.format("Price: %s, Size: %s \n",
                    pos.getPrice(), pos.getSize());
            start += format;
        }
        
        String startB = "Bids: \n";
        for (Position pos : bids) {
            String format = String.format("Price: %s, Size: %s \n",
                    pos.getPrice(), pos.getSize());
            startB += format;
        }
        return String.format("Depth: %s\nTarget Pair: %s\n", bookDepth,
                                        currencyPair).concat(start.concat(startB));
    }

	public Currency getBase() {
		return currencyPair.getBaseCurrency();
	}

	public Position getLowestAsk() {
		return asks.first();
	}

	public Position getHighestBid() {
		return bids.first();
	}

	public TreeSet<Position> getAsks() {
		// TODO Auto-generated method stub
		return this.asks;
	}

	public TreeSet<Position> getBids() {
		// TODO Auto-generated method stub
		return this.bids;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((currencyPair == null) ? 0 : currencyPair.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TradeBook other = (TradeBook) obj;
		if (currencyPair == null) {
			if (other.currencyPair != null)
				return false;
		} else if (!currencyPair.equals(other.currencyPair))
			return false;
		return true;
	}

}
