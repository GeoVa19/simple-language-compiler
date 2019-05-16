package threeaddr;

public class ArrayInstr implements Instruction {

    private String identifier;
    private String address;
    private String result;

    public ArrayInstr(String identifier, String address, String result) {
        this.identifier = identifier;
        this.address = address;
        this.result = result;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String emit() {
        return result + " = " + identifier + "[" + address + "]";
    }

}
