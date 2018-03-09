package daniel.switchtrading.core;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("def02669-dbd9-4d31-9a51-44f8e1cc777a")
public class CurrencyPair {
    @objid ("042052a6-00b1-4147-ab9d-df7ef430f5ab")
    private Currency baseCurrency;

    @objid ("b8569fd2-9148-4dfc-b67e-d64539448198")
    private Currency secondCurrency;

    @objid ("c406a810-fe86-4441-9aa1-f10a2439fdb3")
    public CurrencyPair(Currency frontCurrency, Currency baseCurrency2) {
        this.secondCurrency = frontCurrency;
        this.baseCurrency = baseCurrency2;
    }

    @objid ("9aa7f0c4-5156-46ec-9dca-8d12fb278d49")
    public Currency getBaseCurrency() {
        // Automatically generated method. Please delete this comment before entering specific code.
        return this.baseCurrency;
    }

    @objid ("77e262de-f172-4d29-9964-a42bef556f7c")
    public Currency getSecondCurrency() {
        // Automatically generated method. Please delete this comment before entering specific code.
        return this.secondCurrency;
    }

    @objid ("6e5c171e-98b3-4822-aef9-3ca34d68ae84")
    @Override
    public String toString() {
        return String.format("%s_%s", secondCurrency.getToken(), baseCurrency.getToken());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CurrencyPair) {
        	CurrencyPair other = (CurrencyPair) obj;
            return (other.baseCurrency.equals(baseCurrency)&&other.secondCurrency.equals(secondCurrency));
        }
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
    	return 13 * baseCurrency.hashCode() * 7 + secondCurrency.hashCode();
    }

}
