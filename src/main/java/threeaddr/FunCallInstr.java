package threeaddr;

import java.util.List;

public class FunCallInstr implements Instruction {

    private String functionName;
    private final List<String> params;
    private int numberOfParameters;
    private final String result;

    public FunCallInstr(String functionName, List<String> params, int numberOfParameters, String result) {
        this.functionName = functionName;
        this.params = params;
        this.numberOfParameters = numberOfParameters;
        this.result = result;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public int getNumberOfParameters() {
        return numberOfParameters;
    }

    public void setNumberOfParameters(int numberOfParameters) {
        this.numberOfParameters = numberOfParameters;
    }

    @Override
    public String emit() {
        StringBuilder sb = new StringBuilder();
        params.forEach((paramName) -> {
            sb.append("param ").append(paramName).append("\n");
        });
        if (result == null) {
            sb.append("call ").append(this.functionName).append(", ").append(this.params.size());
        } else {
            sb.append(result).append(" = ").append("call ").append(this.functionName).append(", ").append(this.params.size());
        }
        return sb.toString();
    }

}
