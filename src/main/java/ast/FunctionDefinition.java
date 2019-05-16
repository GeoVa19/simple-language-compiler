package ast;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Type;

public class FunctionDefinition extends ASTNode {

    private Type typeSpecifier;
    private String identifier;
    private List<ParameterDeclaration> parameters;
    private CompoundStatement compoundStatement;

    public FunctionDefinition(Type typeSpecifier, String identifier,
            List<ParameterDeclaration> parameters, CompoundStatement compoundStatement) {
        this.typeSpecifier = typeSpecifier;
        this.identifier = identifier;
        this.parameters = parameters;
        this.compoundStatement = compoundStatement;
    }

    public CompoundStatement getCompoundStatement() {
        return compoundStatement;
    }

    public void setCompoundStatement(CompoundStatement compoundStatement) {
        this.compoundStatement = compoundStatement;
    }

    public Type getTypeSpecifier() {
        return typeSpecifier;
    }

    public void setTypeSpecifier(Type typeSpecifier) {
        this.typeSpecifier = typeSpecifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<ParameterDeclaration> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<ParameterDeclaration> parameters) {
        this.parameters = parameters;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
