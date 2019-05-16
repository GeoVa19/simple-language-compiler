
import symbol.SymTable;
import symbol.SymTableEntry;
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
import types.TypeUtils;
import ast.UnaryExpression;
import ast.VariableDefinition;
import ast.VariableDefinitionStatement;
import ast.WhileStatement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.objectweb.asm.Type;
import types.TypeException;

/**
 * Compute possible types for each node.
 */
public class CollectTypesASTVisitor implements ASTVisitor {

    private int loops = 0;
    private final Deque<FunctionDefinition> functionStack = new ArrayDeque<>();

    public CollectTypesASTVisitor() {
    }

    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.VOID_TYPE);
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
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);

        Type target = ASTUtils.getSafeType(node.getExpression1());
        Type source = ASTUtils.getSafeType(node.getExpression2());

        if (!ASTUtils.isLValueExpression(node.getExpression1())) {
            ASTUtils.error(node, "lvalue required as left operand of assignment");
        }

        /*In cases like:
            
            float[] array;
            array = new float[20];
            array = 10;
       
         */
        if (target.toString().contains("[") && !source.toString().contains("[")) {
            ASTUtils.error(node, "assignment to expression with array type");
        }

        /*In cases like:
            
            float[] array;
            array = new int[20]; 
        
         */
        if (node.getExpression2() instanceof ArrayInitExpression) {
            if (!target.equals(source)) {
                ASTUtils.error(node, fixClassName(source.getClassName()) + " cannot be cast to "
                        + fixClassName(target.getClassName()));
            }
        }

        if (TypeUtils.isAssignable(target, source)) {
            Type maxType = TypeUtils.maxType(target, source);
            ASTUtils.setType(node, maxType);
        } else {
            ASTUtils.error(node, "Types are not assignable!");
        }
    }

    @Override
    public void visit(PrintStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        for (Statement st : node.getStatements()) {
            st.accept(this);
        }
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
        Type type1 = ASTUtils.getType(node.getExpression1());
        Type type2 = ASTUtils.getType(node.getExpression2());

        Type typeResult = null;
        try {
            typeResult = TypeUtils.applyBinary(node.getOperator(), type1, type2);
        } catch (TypeException ex) {
            ASTUtils.error(node, ex.getMessage());
        }
        ASTUtils.setType(node, typeResult);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        try {
            ASTUtils.setType(node, TypeUtils.applyUnary(node.getOperator(), ASTUtils.getSafeType(node.getExpression())));
        } catch (TypeException ex) {
            ASTUtils.error(node, ex.getMessage());
        }
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> symbolTable = ASTUtils.getEnv(node);
        SymTableEntry entry = symbolTable.lookup(node.getIdentifier());

        if (entry == null) {
            ASTUtils.error(node, "Variable '" + node.getIdentifier() + "' has not been declared.");
        }

        /*
                In a case like:
            
                    int main(){
                        main = 10; Main is a function, so LValueExpression = false
                        ...
                    }
         */
        if (entry.getType().toString().contains("(")) {
            ASTUtils.setLValueExpression(node, false);
        } else {
            ASTUtils.setLValueExpression(node, true);
        }

        ASTUtils.setType(node, entry.getType());
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.INT_TYPE);
    }

    @Override
    public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.DOUBLE_TYPE);
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.getType("[C"));
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setType(node, ASTUtils.getSafeType(node.getExpression()));
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        loops++;

        node.getExpression().accept(this);
        if (!ASTUtils.getSafeType(node.getExpression()).equals(Type.INT_TYPE)) {
            ASTUtils.error(node.getExpression(), "Invalid expression, should be integer");
        }
        node.getStatement().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);

        loops--;
    }

    @Override
    public void visit(VariableDefinitionStatement node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.VOID_TYPE);
        if (loops == 0) {
            ASTUtils.error(node, "break statement not within loop");
        }
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.VOID_TYPE);
        if (loops == 0) {
            ASTUtils.error(node, "continue statement not within loop");
        }
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if (!ASTUtils.getSafeType(node.getExpression()).equals(Type.INT_TYPE)) {
            ASTUtils.error(node.getExpression(), "Invalid expression, should be integer");
        }
        node.getStatement().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if (!ASTUtils.getSafeType(node.getExpression()).equals(Type.INT_TYPE)) {
            ASTUtils.error(node.getExpression(), "Invalid expression, should be integer");
        }
        node.getStatement1().accept(this);
        node.getStatement2().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        FunctionDefinition currentFunction = functionStack.peek();
        Expression expression = node.getExpression();

        if (currentFunction == null) {
            ASTUtils.error(node, "Return statement outside function definition");
        }

        Type functionReturnType = currentFunction.getTypeSpecifier();

        if (expression == null) {
            if (!functionReturnType.equals(Type.VOID_TYPE)) {
                ASTUtils.error(node, "'return' with no value, in a function returning non-void");
            }

            ASTUtils.setType(node, Type.VOID_TYPE);
        } else {
            node.getExpression().accept(this);

            Type expressionType = ASTUtils.getSafeType(node.getExpression());

            if (!TypeUtils.isAssignable(functionReturnType, expressionType)) {
                ASTUtils.error(node, "Return type does not match function's return type");
            }

            ASTUtils.setType(node, functionReturnType);
        }
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        functionStack.push(node);

        node.getCompoundStatement().accept(this);

        for (ParameterDeclaration p : node.getParameters()) {
            p.accept(this);
        }

        List<Type> paramsTypeList = new ArrayList<>();
        node.getParameters().stream().map(p -> p.getTypeSpecifier()).forEach(paramsTypeList::add);
        Type[] paramsTypeArray = new Type[paramsTypeList.size()];
        paramsTypeList.toArray(paramsTypeArray);

        Type functionType = Type.getMethodType(node.getTypeSpecifier(), paramsTypeArray);

        ASTUtils.setType(node, functionType);

        functionStack.pop();
    }

    @Override
    public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.CHAR_TYPE);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        ASTUtils.setType(node, node.getTypeSpecifier());
    }

    @Override
    public void visit(ArrayInitExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, node.getTypeSpecifier());
        node.getIntegerLiteralExpression().accept(this);
    }

    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ArrayExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);

        Type expressionType = ASTUtils.getSafeType(node.getExpression());
        if (!expressionType.equals(Type.INT_TYPE)) {
            ASTUtils.error(node, "array subscript is not an integer");
        }

        SymTable<SymTableEntry> symbolTable = ASTUtils.getEnv(node);
        String identifier = node.getIdentifier();
        SymTableEntry entry = symbolTable.lookup(identifier);

        if (entry == null) {
            ASTUtils.error(node, "'" + identifier + "' undeclared!");
        } else {
            Type identifierType = entry.getType().getElementType();

            ASTUtils.setType(node, identifierType);
            ASTUtils.setLValueExpression(node, true);
        }
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> symbolTable = ASTUtils.getEnv(node);
        String identifier = node.getIdentifier() + "_f";
        SymTableEntry entry = symbolTable.lookup(identifier);

        if (entry == null) {
            ASTUtils.error(node, "'" + identifier + "' undeclared!");
        }

        for (Expression e : node.getExpressions()) {
            e.accept(this);
        }

        Type functionExpectedType = symbolTable.lookup(identifier).getType(); //type of the original function

        List<Type> paramsTypeList = new ArrayList<>();
        for (Expression e : node.getExpressions()) {
            paramsTypeList.add(ASTUtils.getSafeType(e));
        }
        Type[] paramsTypeArray = new Type[paramsTypeList.size()];
        paramsTypeList.toArray(paramsTypeArray);

        Type functionCallType = Type.getMethodType(functionExpectedType.getReturnType(), paramsTypeArray);

        if (functionExpectedType.getArgumentTypes().length > paramsTypeArray.length) {
            ASTUtils.error(node, "too few arguments to function " + node.getIdentifier());
        } else if (functionExpectedType.getArgumentTypes().length < paramsTypeArray.length) {
            ASTUtils.error(node, "too many arguments to function " + node.getIdentifier());
        } else { // they are of equal length
            for (int i = 0; i < functionCallType.getArgumentTypes().length; i++) {
                if (!TypeUtils.isAssignable(functionExpectedType.getArgumentTypes()[i], functionCallType.getArgumentTypes()[i])) {
                    ASTUtils.error(node, "Please check the types of the argument in the function call"
                            + " and the parameters in the original function definition");
                }
            }
            ASTUtils.setType(node, functionCallType.getReturnType());
        }
    }

    private static String fixClassName(String className) {

        return className.equals("double") ? "float" : className;
    }
}
