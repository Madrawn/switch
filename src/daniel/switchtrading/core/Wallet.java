package daniel.switchtrading.core;

import java.math.BigDecimal;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("4399b375-fc4b-4285-a8c6-c76e3bd0fcee")
public class Wallet {
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((currency == null) ? 0 : currency.hashCode());
		result = prime * result
				+ ((typeOfWallet == null) ? 0 : typeOfWallet.hashCode());
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
		Wallet other = (Wallet) obj;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		if (typeOfWallet != other.typeOfWallet)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Wallet [freeBalance=" + freeBalance + ", lockedBalance="
				+ lockedBalance + ", totalBalance=" + totalBalance
				+ ", typeOfWallet=" + typeOfWallet + ", currency=" + currency
				+ "]";
	}

	@objid ("bde38191-70ca-43b5-9563-799744f21d6e")
    public BigDecimal freeBalance;

    @objid ("75a67a98-e8d0-4e4a-b48e-00673bf2e910")
    public BigDecimal lockedBalance;

    @objid ("58508a4f-c3ad-48c4-a4a0-a285c76df6ba")
    public BigDecimal totalBalance;

    @objid ("de77bc30-b071-4b73-976f-3dbb6c45e527")
    public TypeOfWallet typeOfWallet;

    @objid ("21574c95-cf9b-4424-a6a7-4976804fbc9b")
    public Currency currency;

}
