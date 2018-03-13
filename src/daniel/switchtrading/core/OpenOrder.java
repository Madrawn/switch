package daniel.switchtrading.core;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("006f4fe8-992d-422c-bc2d-f6f094317479")
public class OpenOrder {
    @Override
	public String toString() {
		return "OpenOrder [currencyPair=" + currencyPair + ", position="
				+ position + ", ID=" + ID + "]";
	}

	@objid ("35b69ca7-c719-4c3b-9204-2c5089c6abdf")
    public CurrencyPair currencyPair;

    @objid ("ac73742a-46d9-4d53-ac53-0ff37007aef4")
    public Position position;
    
    public long ID;

}
