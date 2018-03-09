package daniel.switchtrading.core;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.wrapper.ExchangeWrapper;

@objid ("7669ce43-e133-4797-ab77-da8eaf4e2833")
public class TradeStep {
    @objid ("179548f6-b426-4d53-8afb-7f477feda429")
    public Currency from;

    @objid ("8d9a56da-a274-4f86-997c-b53677f63571")
    public Currency to;

    CurrencyPair relevantPair;
    
/*    @objid ("99ccd801-2011-4ed7-8928-ccb2e1c7fff7")
    public TradeStep nextStep;*/

    @objid ("1e66e0e1-3349-40c4-9d02-e1a6343d9e61")
    public float calculateConversion(final ExchangeWrapper wrapper) {
        return 0f;
    }

    @objid ("b11adf8c-cb9f-4ad9-b032-32d8e0e04946")
    public TradeStep returnInverse() {
        // TODO Auto-generated return
        return new TradeStep(to,from, relevantPair);
    }

    @objid ("9cdfded7-5fc5-4066-9f02-10f046c6640a")
    public TradeStep(Currency from, Currency to, CurrencyPair relevantPair) {
        this.from = from;
        this.to = to;
        this.relevantPair = relevantPair;
    }
    
    @Override
    public String toString() {
    	return String.format("From %s to %s", from, to);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		TradeStep other = (TradeStep) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

    
    
    
    /*
    @Deprecated
    @objid ("c4333dfd-2eda-43b9-8998-18c96ffd55ff")
    public void addStep(final TradeStep tradeStep) throws Exception {
        if (tradeStep.from != to) {
            throw new Exception("Can't add step. from and to don't overlap");
        } else {
        	if (nextStep == null) {
        		this.nextStep = tradeStep;
				
        	} else{
        		nextStep.addStep(tradeStep);
        	}
        }
    }

    @Deprecated
	public TradeStep getTail() {
		if (nextStep == null) {
    		return this;
			
    	} else{
    		return nextStep.getTail();
    	}
	}
*/
}
