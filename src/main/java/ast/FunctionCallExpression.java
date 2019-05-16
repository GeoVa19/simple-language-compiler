package ast;

import java.util.List;

public class FunctionCallExpression extends Expression {

    private String identifier;
    private List<Expression> expressions;

    public FunctionCallExpression(String identifier, List<Expression> expressions) {
        this.identifier = identifier;
        this.expressions = expressions;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpression(List<Expression> expression) {
        this.expressions = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
}
