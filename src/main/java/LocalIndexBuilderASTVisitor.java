
import java.util.ArrayDeque;
import java.util.Deque;

import ast.ASTUtils;
import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.ArrayExpression;
import ast.ArrayInitExpression;
import ast.AssignmentStatement;
import ast.BinaryExpression;
import ast.BreakStatement;
import ast.CharacterLiteralExpression;
import ast.CompilationUnit;
import ast.CompoundStatement;
import ast.ContinueStatement;
import ast.DoubleLiteralExpression;
import ast.Expression;
import ast.ExpressionStatement;
import ast.FunctionCallExpression;
import ast.FunctionDefinition;
import ast.IdentifierExpression;
import ast.IfElseStatement;
import ast.IfStatement;
import ast.IntegerLiteralExpression;
import ast.ParameterDeclaration;
import ast.ParenthesisExpression;
import ast.PrintStatement;
import ast.ReturnStatement;
import ast.Statement;
import ast.StringLiteralExpression;
import ast.UnaryExpression;
import ast.VariableDefinition;
import ast.VariableDefinitionStatement;
import ast.WhileStatement;
import java.util.List;
import symbol.LocalIndexPool;

/**
 * Build LocalIndexPool for each node of the AST.
 */
public class LocalIndexBuilderASTVisitor implements ASTVisitor {

    private final Deque<LocalIndexPool> env;

    public LocalIndexBuilderASTVisitor() {
        env = new ArrayDeque<>();
    }

    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
        env.push(new LocalIndexPool());
        ASTUtils.setLocalIndexPool(node, env.element());
        List<FunctionDefinition> functionDefinitionList = node.getFunctionDefinition();
        List<VariableDefinition> variableDefinitionList = node.getVariableDefinitions();
        if (functionDefinitionList != null) {
            for (FunctionDefinition fd : functionDefinitionList) {
                fd.accept(this);
            }
        }
        if (variableDefinitionList != null) {
            for (VariableDefinition vd : variableDefinitionList) {
                vd.accept(this);
            }
        }
        env.pop();
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(PrintStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(VariableDefinitionStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        for (Statement s : node.getStatements()) {
            s.accept(this);
        }
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement1().accept(this);
        node.getStatement2().accept(this);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        Expression expression = node.getExpression();
        if (expression != null) {
            node.getExpression().accept(this);
        }
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        env.push(new LocalIndexPool());
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getCompoundStatement().accept(this);
        for (ParameterDeclaration p : node.getParameters()) {
            p.accept(this);
        }
        env.pop();
    }

    @Override
    public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(ArrayInitExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getIntegerLiteralExpression().accept(this);
    }

    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ArrayExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        for (Expression e : node.getExpressions()) {
            e.accept(this);
        }
    }

}
