package daniel.switchtrading.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid("f11ce54f-7d56-4f8c-a9f3-e6a8df692444")
public class TradeRoute {

	private List<TradeStep> steps = new LinkedList<>();

	@objid("2252be15-85f5-4152-93e0-9fbb42c1bb86")
	public TradeRoute(TradeStep in) {
		steps.add(in);
	}

	public TradeRoute(List<TradeStep> list) {
		steps = new LinkedList<>(list);
	}

	public TradeStep getFirst() {
		// TODO Auto-generated method stub
		return this.steps.get(0);
	}

	public void addStep(TradeStep end) throws Exception {
		Currency tailC = steps.get(steps.size() - 1).to;

		if (tailC.equals(end.from)) {
			steps.add(end);

		} else {
			throw new Exception("Can't add step. from and to don't overlap");
		}

	}

	public Currency getTailCurrency() {

		return steps.get(steps.size() - 1).to;
	}

	@Override
	public String toString() {
		String result = "TradeRoute: " + hashCode() + "\n";
		for (TradeStep tradeStep : steps) {
			result += tradeStep.toString() + "\n";
		}

		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((steps == null) ? 0 : steps.hashCode());
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
		TradeRoute other = (TradeRoute) obj;
		if (steps == null) {
			if (other.steps != null)
				return false;
		} else if (!steps.equals(other.steps))
			return false;
		return true;
	}

	public List<TradeStep> getList() {
		// TODO Auto-generated method stub
		return steps;
	}

	public boolean contains(Set<CurrencyPair> badPairs) {
		for (CurrencyPair currencyPair : badPairs) {
			boolean contains = contains(currencyPair);
			if (contains) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param currencyPair
	 */
	public boolean contains(CurrencyPair currencyPair) {
		for (TradeStep tradeStep : steps) {

			boolean cont = tradeStep.relevantPair.equals(currencyPair);
			if (cont) {
				return true;
			}
		}
		return false;
	}


}
