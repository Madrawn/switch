package daniel.switchtrading.core;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("9ebfb13c-b7ef-4b8b-aa57-7f33527bbed0")
public class Currency {
    @objid ("aa8792f5-d538-4362-b513-d904c9cca7e3")
    private String token;

    @objid ("338fee04-103c-4a97-8cf8-d274becedeba")
    public String getToken() {
        // Automatically generated method. Please delete this comment before entering specific code.
        return this.token;
    }

    @objid ("c73f75ac-b848-4955-9ab0-456ec0fc1417")
    public Currency(final String longName) {
        this.token = longName.toLowerCase();
    }

    @objid ("aff1685b-16d1-43e3-84ac-4f6bc899a2c6")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Currency) {
            return this.token.equals(((Currency)obj).getToken());
        }
        return super.equals(obj);
    }
    
    @Override
    public String toString() {
    	return "Currency:"+token;
    }
    
    @Override
    public int hashCode() {
    	// TODO Auto-generated method stub
    	return token.hashCode();
    }

}
