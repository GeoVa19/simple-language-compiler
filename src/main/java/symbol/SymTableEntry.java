package symbol;

import org.objectweb.asm.Type;

public class SymTableEntry {

    private String id;
    private Type type;
    private Integer index;
    
    public SymTableEntry(String id) {
        this(id, null, null);
    }

    public SymTableEntry(String id, Type type){
        this(id, type, null);
    }
    
    public SymTableEntry(String id, Type type, Integer index) {
        this.id = id;
        this.type = type;
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SymTableEntry other = (SymTableEntry) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return !(this.type != other.type && (this.type == null || !this.type.equals(other.type)));
    }

}
