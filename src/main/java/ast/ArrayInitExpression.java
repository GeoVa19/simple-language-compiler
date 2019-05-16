package ast;

import org.objectweb.asm.Type;

public class ArrayInitExpression extends Expression {

    private Type typeSpecifier;
    private IntegerLiteralExpression integerLiteralExpression;

    public ArrayInitExpression(Type typeSpecifier, IntegerLiteralExpression integerLiteralExpression) {
        this.typeSpecifier = typeSpecifier;
        this.integerLiteralExpression = integerLiteralExpression;
    }

    public Type getTypeSpecifier() {
        return typeSpecifier;
    }

    public void setTypeSpecifier(Type typeSpecifier) {
        this.typeSpecifier = typeSpecifier;
    }

    public IntegerLiteralExpression getIntegerLiteralExpression() {
        return integerLiteralExpression;
    }

    public void setIntegerLiteral(IntegerLiteralExpression integerLiteralExpression) {
        this.integerLiteralExpression = integerLiteralExpression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
