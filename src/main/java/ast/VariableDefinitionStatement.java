package ast;

public class VariableDefinitionStatement extends Statement {

    private VariableDefinition variableDefinition;

    public VariableDefinitionStatement(VariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

    public void setVariableDefinition(VariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
}
