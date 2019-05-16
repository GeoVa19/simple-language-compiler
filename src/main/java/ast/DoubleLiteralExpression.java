package ast;

public class DoubleLiteralExpression extends Expression {

    private Double literal;

    public DoubleLiteralExpression(Double literal) {
        this.literal = literal;
    }

    public Double getLiteral() {
        return literal;
    }

    public void setLiteral(Double literal) {
        this.literal = literal;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
