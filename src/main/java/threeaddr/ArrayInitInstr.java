package threeaddr;

import org.objectweb.asm.Type;

public class ArrayInitInstr implements Instruction {

    private Type type;
    private String size;
    private final String result;

    public ArrayInitInstr(Type type, String size, String result) {
        this.type = type;
        this.size = size;
        this.result = result;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public String emit() {
        return result + " = " + "new " + type + " [" + size + "]";
    }

}
