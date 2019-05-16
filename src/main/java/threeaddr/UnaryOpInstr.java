package threeaddr;

import ast.Operator;

public class UnaryOpInstr implements Instruction {

    private Operator op;
    private String arg1;
    private String result;

    public UnaryOpInstr(Operator op, String arg1, String result) {
        this.op = op;
        this.arg1 = arg1;
        this.result = result;
    }

    public Operator getOp() {
        return op;
    }

    public void setOp(Operator op) {
        this.op = op;
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
        return result + " = " + op + " " + arg1;
    }

}
