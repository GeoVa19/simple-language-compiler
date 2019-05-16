package ast;

public class IntegerLiteralExpression extends Expression {

    private Integer literal;

    public IntegerLiteralExpression(Integer literal) {
        this.literal = literal;
    }

    public Integer getLiteral() {
        return literal;
    }

    public void setLiteral(Integer literal) {
        this.literal = literal;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
