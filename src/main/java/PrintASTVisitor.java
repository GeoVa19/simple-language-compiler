
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

import org.apache.commons.lang3.StringEscapeUtils;

public class PrintASTVisitor implements ASTVisitor {

    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
        List<FunctionDefinition> functionDefinitionList = node.getFunctionDefinition();
        List<VariableDefinition> variableDefinitionList = node.getVariableDefinitions();
        if (functionDefinitionList != null) {
            for (FunctionDefinition fd : functionDefinitionList) {
                fd.accept(this);
                System.out.println();
            }
        }
        if (variableDefinitionList != null) {
            for (VariableDefinition vd : variableDefinitionList) {
                vd.accept(this);
            }
        }
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        System.out.print(" = ");
        node.getExpression2().accept(this);
        if (!(node.getExpression2() instanceof FunctionCallExpression
                | node.getExpression2() instanceof ArrayInitExpression)) {
            System.out.println(";");
        }
    }

    @Override
    public void visit(PrintStatement node) throws ASTVisitorException {
        System.out.print("print(");
        node.getExpression().accept(this);
        System.out.println(");");
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        System.out.print(" ");
        System.out.print(node.getOperator());
        System.out.print(" ");
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        System.out.print(node.getOperator());
        System.out.print(" ");
        node.getExpression().accept(this);
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier());
    }

    @Override
    public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.getLiteral());
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.getLiteral());
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        System.out.print("\"");
        System.out.print(StringEscapeUtils.escapeJava(node.getLiteral()));
        System.out.print("\"");
    }

    @Override
    public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
        System.out.print("'" + StringEscapeUtils.escapeJava(node.getLiteral().toString()) + "'");
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        System.out.print("( ");
        node.getExpression().accept(this);
        System.out.print(" )");
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        System.out.println("{ ");
        for (Statement st : node.getStatements()) {
            st.accept(this);
        }
        System.out.println("} ");
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        System.out.print("while (");
        node.getExpression().accept(this);
        System.out.print(") ");
        node.getStatement().accept(this);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        System.out.print("if (");
        node.getExpression().accept(this);
        System.out.print(") ");
        node.getStatement().accept(this);
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        System.out.print("if (");
        node.getExpression().accept(this);
        System.out.println(")");
        node.getStatement1().accept(this);
        System.out.print("else ");
        node.getStatement2().accept(this);
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        System.out.print(node.getTypeSpecifier() + " ");
        System.out.print(node.getIdentifier());
        System.out.println(";");
    }

    @Override
    public void visit(VariableDefinitionStatement node) throws ASTVisitorException {
        node.getVariableDefinition().accept(this);
    }

    @Override
    public void visit(ArrayExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier());
        System.out.print("[");
        node.getExpression().accept(this);
        System.out.print("]");
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        System.out.print("return");
        Expression expression = node.getExpression();
        if (expression != null) {
            System.out.print(" ");
            node.getExpression().accept(this);
        }
        System.out.println(";");
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        System.out.print(node.getTypeSpecifier() + " ");
        System.out.print(node.getIdentifier());
        System.out.print("(");

        int size = node.getParameters().size();
        int i = 0;
        for (ParameterDeclaration p : node.getParameters()) {
            i++;
            p.accept(this);
            if (i != size) {
                System.out.print(", ");
            }
        }
        System.out.println(")");
        node.getCompoundStatement().accept(this);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        System.out.print(node.getTypeSpecifier() + " ");
        System.out.print(node.getIdentifier());
    }

    @Override
    public void visit(ArrayInitExpression node) throws ASTVisitorException {
        System.out.print("new ");
        System.out.print(node.getTypeSpecifier());
        System.out.print("[");
        node.getIntegerLiteralExpression().accept(this);
        System.out.println("];");
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        System.out.print("break;");
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        System.out.print("continue;");
    }

    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier());
        System.out.print("(");
        int size = node.getExpressions().size();
        int i = 0;

        for (Expression e : node.getExpressions()) {
            i++;
            e.accept(this);
            if (i != size) {
                System.out.print(", ");
            }
        }
        System.out.println(");");
    }

}
