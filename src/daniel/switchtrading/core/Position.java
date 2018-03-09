package daniel.switchtrading.core;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("c2e47c57-756e-4eb2-8242-48144eab9dc9")
public class Position implements Comparable {
    @objid ("e81f7a28-2c22-42e0-ac29-3434256a7951")
    private float size;

    @objid ("d601c75d-2a74-48c0-8f14-082737418068")
    private float price;

    @objid ("fd2bb1df-8ee3-4ba4-a961-eceeafbb4c74")
    private boolean isBid;

    @objid ("c932f5e2-8984-43be-8367-cdd0de74ff4e")
    public float getPrice() {
        // Automatically generated method. Please delete this comment before entering specific code.
        return this.price;
    }

    @objid ("714fa691-723d-4b2a-b307-5a62a0fc5c1c")
    public int compareTo(Object p0) {
        if (p0 instanceof Position) {
            Position other = (Position) p0;
            return !isBid ? Float.compare(this.price, other.price) : Float.compare(-this.price, -other.price);
        } else {
            throw new ClassCastException();
        }
    }

    @objid ("88df2f11-6609-47e0-a42f-3d1954388542")
    public float getSize() {
        // Automatically generated method. Please delete this comment before entering specific code.
        return this.size;
    }

    @objid ("ad4e6c22-0754-4cac-8d2b-a142bc6cdcea")
    public Position(final float size, final float price, boolean isBid) {
        this.size = size;
        this.price = price;
        this.isBid = isBid;
    }
    
    @Override
    public String toString() {
    	return String.format("Price: %s Size: %s IsBid: %s", price, size, isBid);
    }

}
