package threeaddr;

public class AssignInstr implements Instruction {

    private String arg1;
    private String result;

    public AssignInstr(String arg1, String result) {
        this.arg1 = arg1;
        this.result = result;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String emit() {
        return result + " = " + arg1;
    }

}
