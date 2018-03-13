package daniel.switchtrading.core;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("4399b375-fc4b-4285-a8c6-c76e3bd0fcee")
public class Wallet {
    @objid ("bde38191-70ca-43b5-9563-799744f21d6e")
    public double freeBalance;

    @objid ("75a67a98-e8d0-4e4a-b48e-00673bf2e910")
    public double lockedBalance;

    @objid ("58508a4f-c3ad-48c4-a4a0-a285c76df6ba")
    public double totalBalance;

    @objid ("de77bc30-b071-4b73-976f-3dbb6c45e527")
    public TypeOfWallet typeOfWallet;

    @objid ("21574c95-cf9b-4424-a6a7-4976804fbc9b")
    public Currency currency;

}
