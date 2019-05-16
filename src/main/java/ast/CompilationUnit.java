package ast;

import java.util.ArrayList;
import java.util.List;

public class CompilationUnit extends ASTNode {

    private List<VariableDefinition> variableDefinition;
    private List<FunctionDefinition> functionDefinition;

    public CompilationUnit() {
        this.variableDefinition = new ArrayList<>();
        this.functionDefinition = new ArrayList<>();
    }

    public CompilationUnit(List<FunctionDefinition> functionDefinition,
            List<VariableDefinition> variableDefinition) {
        this.functionDefinition = functionDefinition;
        this.variableDefinition = variableDefinition;
    }

    public List<FunctionDefinition> getFunctionDefinition() {
        return functionDefinition;
    }

    public void setFunctionDefinition(List<FunctionDefinition> functionDefinition) {
        this.functionDefinition = functionDefinition;
    }

    public List<VariableDefinition> getVariableDefinitions() {
        return variableDefinition;
    }

    public void setVariableDefinitions(List<VariableDefinition> variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
