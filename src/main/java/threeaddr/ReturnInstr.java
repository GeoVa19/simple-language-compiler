package threeaddr;

public class ReturnInstr implements Instruction {

    private String returnVal;

    public ReturnInstr(String returnVal) {
        this.returnVal = returnVal;
    }

    public String getReturnVal() {
        return returnVal;
    }

    public void setReturnVal(String returnVal) {
        this.returnVal = returnVal;
    }

    @Override
    public String emit() {
        return "return " + returnVal;
    }

}
