package threeaddr;

public class LabelInstr implements Instruction {

    private String name;

    public LabelInstr(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String emit() {
        return name + ":";
    }

}
