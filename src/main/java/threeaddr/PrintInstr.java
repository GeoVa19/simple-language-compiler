package threeaddr;

public class PrintInstr implements Instruction {

    private String arg1;

    public PrintInstr(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    @Override
    public String emit() {
        return "param " + arg1 + "\ncall print, 1";
    }

}
