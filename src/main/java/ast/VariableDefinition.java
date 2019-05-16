package ast;

import org.objectweb.asm.Type;

public class VariableDefinition extends ASTNode {

    private Type typeSpecifier;
    private String identifier;
    
    public VariableDefinition(Type typeSpecifier, String identifier){
        this.typeSpecifier = typeSpecifier;
        this.identifier = identifier;
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

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
}
