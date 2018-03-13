package daniel.switchtrading.core;

import java.util.ArrayList;
import java.util.List;
import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("df14fe88-f645-4a01-b78e-dafc0c012a4a")
public class UserBalance {
    @objid ("0a3ab817-0833-44ab-913a-625172a15128")
    public List<Wallet> wallet = new ArrayList<Wallet> ();

    @objid ("e8b3a0f9-c3b9-4d95-b0c8-5ca105898f65")
    public List<OpenOrder> openOrder = new ArrayList<OpenOrder> ();

    @objid ("054b3897-a2fe-4733-9504-1df1e6580ea7")
    public List<OpenOrder> getOpenOrders() {
        // TODO Auto-generated return
        return null;
    }

    @objid ("71845efd-f55e-4564-b8de-fb9484644641")
    public List<Wallet> getWallets() {
        // TODO Auto-generated return
        return null;
    }

	public Wallet getWallet(Currency from) {
		for (Wallet wallet : wallet) {
			
			if(wallet.currency.equals(from))
				return wallet;
		}
		return null;
	}

}
