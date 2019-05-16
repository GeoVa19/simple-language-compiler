package ast;

public enum Operator {

    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER(">"),
    LESS("<"),
    GREATER_EQ(">="),
    LESS_EQ("<="),
    MOD("%"),
    LOGICAL_AND("&&"),
    LOGICAL_OR("||"),
    LOGICAL_NOT("!");

    private String type;

    private Operator(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }

    public boolean isUnary() {
        return this.equals(Operator.MINUS) || this.equals(Operator.LOGICAL_NOT);
    }

    public boolean isRelational() {
        return this.equals(Operator.EQUAL) || this.equals(Operator.NOT_EQUAL)
                || this.equals(Operator.GREATER) || this.equals(Operator.GREATER_EQ)
                || this.equals(Operator.LESS) || this.equals(Operator.LESS_EQ);
    }

    public boolean isArithmetic() {
        return this.equals(Operator.PLUS) || this.equals(Operator.MINUS) || this.equals(Operator.MULTIPLY)
                || this.equals(Operator.DIVIDE) || this.equals(Operator.MOD);
    }

    public boolean isLogical() {
        return this.equals(Operator.LOGICAL_OR) || this.equals(Operator.LOGICAL_AND)
                || this.equals(Operator.LOGICAL_NOT);
    }
}
