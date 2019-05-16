
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
import java.util.stream.Collectors;
import org.objectweb.asm.Type;
import symbol.LocalIndexPool;
import symbol.SymTable;
import symbol.SymTableEntry;

/**
 * Collect all symbols such as variables, methods, etc in symbol table.
 */
public class CollectSymbolsASTVisitor implements ASTVisitor {

    public CollectSymbolsASTVisitor() {
    }

    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
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

        SymTable<SymTableEntry> symbolTable = ASTUtils.getEnv(node);
        //symbolTable.put("main_f", new SymTableEntry("main_f", Type.getType("()V")));
        symbolTable.put("random_f", new SymTableEntry("random_f", Type.getType("(I)I")));
        symbolTable.put("scanInt_f", new SymTableEntry("scanInt_f", Type.getType("()I")));
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(PrintStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        for (Statement st : node.getStatements()) {
            st.accept(this);
        }
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        // nothing        
    }

    @Override
    public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(VariableDefinitionStatement node) throws ASTVisitorException {
        SymTable<SymTableEntry> symbolTable = ASTUtils.getEnv(node);
        String identifier = node.getVariableDefinition().getIdentifier();
        Type type = node.getVariableDefinition().getTypeSpecifier();

        if (type.equals(Type.VOID_TYPE) || type.toString().equals("[" + Type.VOID_TYPE)) {
            ASTUtils.error(node, "variable '" + identifier + "' declared void");
        }

        if (symbolTable.lookupOnlyInTop(identifier) != null) {
            ASTUtils.error(node, "identifier  '" + identifier + "' already exists");
        }

        LocalIndexPool localIndexPool = ASTUtils.getSafeLocalIndexPool(node);
        int index = localIndexPool.getLocalIndex(node.getVariableDefinition().getTypeSpecifier());
        symbolTable.put(identifier, new SymTableEntry(identifier, type, index));
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement1().accept(this);
        node.getStatement2().accept(this);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        Expression expression = node.getExpression();
        if (expression != null) {
            node.getExpression().accept(this);
        }
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        SymTable<SymTableEntry> symbolTable = ASTUtils.getEnv(node);
        String identifier = node.getIdentifier() + "_f";

        List<Type> paramsTypeList = node.getParameters().stream().map(p -> p.getTypeSpecifier()).collect(Collectors.toList());
        Type[] paramsTypeArray = paramsTypeList.toArray(new Type[paramsTypeList.size()]);
        Type functionType = Type.getMethodType(node.getTypeSpecifier(), paramsTypeArray);

        if (symbolTable.lookup(identifier) != null) {
            ASTUtils.error(node, "identifier  '" + identifier + "' already exists");
        } else if (identifier.equals("main_f") && (functionType.getArgumentTypes().length != 0
                || !functionType.getReturnType().equals(Type.VOID_TYPE))) {
            ASTUtils.error(node, "There should be only one main function with no parameters returning void.");
        } else if (identifier.equals("random_f")) {
            ASTUtils.error(node, "There should be only one random function.");
        } else if (identifier.equals("scanInt_f")) {
            ASTUtils.error(node, "There should be only one scanInt function.");
        } else {
            symbolTable.put(identifier, new SymTableEntry(identifier, functionType));
        }

        symbolTable = ASTUtils.getEnv(node.getCompoundStatement());

        for (ParameterDeclaration p : node.getParameters()) {
            p.accept(this);

            String parameterIdentifier = p.getIdentifier();
            Type parameterType = p.getTypeSpecifier();

            LocalIndexPool localIndexPool = ASTUtils.getSafeLocalIndexPool(node);
            int index = localIndexPool.getLocalIndex(parameterType);
            symbolTable.put(parameterIdentifier, new SymTableEntry(parameterIdentifier, parameterType, index));
        }
        node.getCompoundStatement().accept(this);
    }

    @Override
    public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        SymTable<SymTableEntry> symbolTable = ASTUtils.getEnv(node);
        String identifier = node.getIdentifier();
        Type type = node.getTypeSpecifier();

        if (type.equals(Type.VOID_TYPE) || type.toString().equals("[" + Type.VOID_TYPE)) {
            ASTUtils.error(node, "parameter '" + identifier + "' declared void");
        }

        if (symbolTable.lookupOnlyInTop(identifier) != null) {
            ASTUtils.error(node, "identifier  '" + identifier + "' already exists");
        }
    }

    @Override
    public void visit(ArrayInitExpression node) throws ASTVisitorException {
        node.getIntegerLiteralExpression().accept(this);
    }

    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ArrayExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        for (Expression e : node.getExpressions()) {
            e.accept(this);
        }
    }

}
