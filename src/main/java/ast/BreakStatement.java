package ast;


public class BreakStatement extends Statement {

    public BreakStatement() {
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
