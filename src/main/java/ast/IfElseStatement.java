package ast;

public class IfElseStatement extends Statement {

    private Expression expression;
    private Statement statement1, statement2;

    public IfElseStatement(Expression expression, Statement statement1, Statement statement2) {
        this.expression = expression;
        this.statement1 = statement1;
        this.statement2 = statement2;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Statement getStatement1() {
        return statement1;
    }

    public void setStatement1(Statement statement1) {
        this.statement1 = statement1;
    }

    public Statement getStatement2() {
        return statement2;
    }

    public void setStatement2(Statement statement2) {
        this.statement2 = statement2;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
