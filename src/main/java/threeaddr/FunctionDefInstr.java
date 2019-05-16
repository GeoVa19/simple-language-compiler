package threeaddr;

public class FunctionDefInstr implements Instruction {

    private String identifier;

    public FunctionDefInstr(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String emit() {
        return "-- " + identifier + " --";
    }
}
