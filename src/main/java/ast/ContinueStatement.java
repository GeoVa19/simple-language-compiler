package ast;


public class ContinueStatement extends Statement {

    public ContinueStatement() {
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
