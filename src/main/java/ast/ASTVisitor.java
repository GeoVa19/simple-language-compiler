package ast;

/**
 * Abstract syntax tree visitor.
 */
public interface ASTVisitor {

    void visit(CompilationUnit node) throws ASTVisitorException;

    void visit(ReturnStatement node) throws ASTVisitorException;

    void visit(AssignmentStatement node) throws ASTVisitorException;

    void visit(PrintStatement node) throws ASTVisitorException;

    void visit(CompoundStatement node) throws ASTVisitorException;

    void visit(BinaryExpression node) throws ASTVisitorException;

    void visit(UnaryExpression node) throws ASTVisitorException;

    void visit(IdentifierExpression node) throws ASTVisitorException;

    void visit(DoubleLiteralExpression node) throws ASTVisitorException;

    void visit(IntegerLiteralExpression node) throws ASTVisitorException;

    void visit(StringLiteralExpression node) throws ASTVisitorException;

    void visit(ParenthesisExpression node) throws ASTVisitorException;

    void visit(WhileStatement node) throws ASTVisitorException;

    void visit(IfStatement node) throws ASTVisitorException;

    void visit(IfElseStatement node) throws ASTVisitorException;

    void visit(VariableDefinition node) throws ASTVisitorException;

    void visit(VariableDefinitionStatement node) throws ASTVisitorException;

    void visit(FunctionDefinition node) throws ASTVisitorException;

    void visit(CharacterLiteralExpression node) throws ASTVisitorException;

    void visit(ParameterDeclaration node) throws ASTVisitorException;

    void visit(ArrayInitExpression node) throws ASTVisitorException;

    void visit(BreakStatement node) throws ASTVisitorException;

    void visit(ContinueStatement node) throws ASTVisitorException;

    void visit(ExpressionStatement node) throws ASTVisitorException;

    void visit(ArrayExpression node) throws ASTVisitorException;

    void visit(FunctionCallExpression node) throws ASTVisitorException;
}
