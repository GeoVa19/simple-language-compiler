
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import threeaddr.AssignInstr;
import threeaddr.GotoInstr;
import threeaddr.LabelInstr;
import threeaddr.Program;
import threeaddr.UnaryOpInstr;
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
import ast.DoubleLiteralExpression;
import ast.IdentifierExpression;
import ast.IfStatement;
import ast.IntegerLiteralExpression;
import ast.ParenthesisExpression;
import ast.PrintStatement;
import ast.Statement;
import ast.StringLiteralExpression;
import ast.UnaryExpression;
import ast.WhileStatement;
import org.apache.commons.lang3.StringEscapeUtils;
import ast.ContinueStatement;
import ast.Expression;
import ast.ExpressionStatement;
import ast.FunctionCallExpression;
import ast.FunctionDefinition;
import ast.IfElseStatement;
import ast.Operator;
import ast.ParameterDeclaration;
import ast.ReturnStatement;
import ast.VariableDefinition;
import ast.VariableDefinitionStatement;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.Type;
import threeaddr.ArrayInitInstr;
import threeaddr.ArrayInstr;
import threeaddr.BinaryOpInstr;
import threeaddr.CondJumpInstr;
import threeaddr.FunCallInstr;
import threeaddr.FunctionDefInstr;
import threeaddr.PrintInstr;
import threeaddr.ReturnInstr;
import types.TypeException;

public class IntermediateCodeASTVisitor implements ASTVisitor {

    private final Program program;
    private final Deque<String> stack;
    private int temp;

    public IntermediateCodeASTVisitor() {
        program = new Program();
        stack = new ArrayDeque<>();
        temp = 0;
    }

    private String createTemp() {
        return "t" + Integer.toString(temp++);
    }

    public Program getProgram() {
        return program;
    }

    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
        for (FunctionDefinition fd : node.getFunctionDefinition()) {
            fd.accept(this);

            Statement s = null, ps;
            Iterator<Statement> it = fd.getCompoundStatement().getStatements().iterator();
            while (it.hasNext()) {
                ps = s;
                s = it.next();

                if (ps != null && !ASTUtils.getNextList(ps).isEmpty()) {
                    Program.backpatch(ASTUtils.getNextList(ps), program.addNewLabel());
                }

                s.accept(this);

                if (!ASTUtils.getBreakList(s).isEmpty()) {
                    ASTUtils.error(s, "Break detected without a loop.");
                }

                if (!ASTUtils.getContinueList(s).isEmpty()) {
                    ASTUtils.error(s, "Continue detected without a loop.");
                }

                if (s != null && !ASTUtils.getNextList(s).isEmpty()) {
                    Program.backpatch(ASTUtils.getNextList(s), program.addNewLabel());
                }
            }
        }
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);

        String t = stack.pop();
        String t1 = stack.pop();
        program.add(new AssignInstr(t, t1));
    }

    @Override
    public void visit(PrintStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        String temporary = stack.pop();
        program.add(new PrintInstr(temporary));
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        String t1 = stack.pop();
        node.getExpression2().accept(this);
        String t2 = stack.pop();

        if (ASTUtils.isBooleanExpression(node)) {

            if (!node.getOperator().isRelational() && !node.getOperator().isLogical()) {
                ASTUtils.error(node, "A not boolean expression used as boolean.");
            }

            ASTUtils.setBooleanExpression(node.getExpression1(), true);
            ASTUtils.setBooleanExpression(node.getExpression2(), true);

            if (node.getOperator().equals(Operator.LOGICAL_OR)) {
                Program.backpatch(ASTUtils.getFalseList(node.getExpression1()), program.addNewLabel());

                ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression1()));
                ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression2()));

                ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression2()));
            }

            if (node.getOperator().equals(Operator.LOGICAL_AND)) {
                Program.backpatch(ASTUtils.getTrueList(node.getExpression1()), program.addNewLabel());

                ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression2()));

                ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression1()));
                ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression2()));
            }

            CondJumpInstr condJumpInstr = new CondJumpInstr(node.getOperator(), t1, t2, null);
            GotoInstr gotoInstr = new GotoInstr(null);

            program.add(condJumpInstr);
            program.add(gotoInstr);

            ASTUtils.getTrueList(node).add(condJumpInstr);
            ASTUtils.getFalseList(node).add(gotoInstr);
        } else {
            String temporary = createTemp();
            stack.push(temporary);
            BinaryOpInstr binaryOpInstr = new BinaryOpInstr(node.getOperator(), t1, t2, temporary);
            program.add(binaryOpInstr);
        }
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        String t1 = stack.pop();

        if (ASTUtils.isBooleanExpression(node)) {
            if (node.getOperator().equals(Operator.LOGICAL_NOT)) {
                ASTUtils.getTrueList(node).addAll(ASTUtils.getFalseList(node.getExpression()));

                ASTUtils.getFalseList(node).addAll(ASTUtils.getTrueList(node.getExpression()));
            }
        } else {
            String temporary = createTemp();
            stack.push(temporary);
            program.add(new UnaryOpInstr(node.getOperator(), t1, temporary));
        }
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        stack.push(node.getIdentifier());
    }

    @Override
    public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            if (node.getLiteral() != 0) {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getTrueList(node).add(i);
            } else {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getFalseList(node).add(i);
            }
        } else {
            String temporary = createTemp();
            stack.push(temporary);
            program.add(new AssignInstr(node.getLiteral().toString(), temporary));
        }
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            if (node.getLiteral() != 0) { // if true
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getTrueList(node).add(i);
            } else {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getFalseList(node).add(i);
            }
        } else {
            String temporary = createTemp();
            stack.push(temporary);
            program.add(new AssignInstr(node.getLiteral().toString(), temporary));
        }
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            ASTUtils.error(node, "Strings cannot be used as boolean expressions");
        } else {
            String temporary = createTemp();
            stack.push(temporary);
            program.add(new AssignInstr("\"" + StringEscapeUtils.escapeJava(node.getLiteral()) + "\"", temporary));
        }
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        String t1 = stack.pop();
        String temporary = createTemp();
        stack.push(temporary);
        program.add(new AssignInstr(t1, temporary));
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        LabelInstr beginLabel = program.addNewLabel();
        node.getExpression().accept(this);
        List<GotoInstr> trueList = ASTUtils.getTrueList(node.getExpression());
        List<GotoInstr> falseList = ASTUtils.getFalseList(node.getExpression());
        LabelInstr beginStmtLabel = program.addNewLabel();
        node.getStatement().accept(this);
        Program.backpatch(trueList, beginStmtLabel);
        List<GotoInstr> nextList = ASTUtils.getNextList(node.getStatement());
        Program.backpatch(nextList, beginLabel);
        List<GotoInstr> continueList = ASTUtils.getContinueList(node.getStatement());
        Program.backpatch(continueList, beginLabel);
        program.add(new GotoInstr(beginLabel));
        ASTUtils.getNextList(node).addAll(falseList);
        ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        List<GotoInstr> trueList = ASTUtils.getTrueList(node.getExpression());

        LabelInstr beginStmtLabel = program.addNewLabel();

        Program.backpatch(trueList, beginStmtLabel);

        node.getStatement().accept(this);

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));

        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement()));

        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        List<GotoInstr> trueList = ASTUtils.getTrueList(node.getExpression());
        List<GotoInstr> falseList = ASTUtils.getFalseList(node.getExpression());

        LabelInstr beginStmt1Label = program.addNewLabel();

        Program.backpatch(trueList, beginStmt1Label);

        node.getStatement1().accept(this);

        GotoInstr gotoInstr = new GotoInstr();
        program.add(gotoInstr);

        LabelInstr beginStmt2Label = program.addNewLabel();
        Program.backpatch(falseList, beginStmt2Label);

        node.getStatement2().accept(this);

        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement1()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement2()));
        ASTUtils.getNextList(node).add(gotoInstr);

        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement1()));
        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement2()));

        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement1()));
        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement2()));
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        GotoInstr gotoInstr = new GotoInstr();
        program.add(gotoInstr);
        ASTUtils.getBreakList(node).add(gotoInstr);
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        GotoInstr gotoInstr = new GotoInstr();
        program.add(gotoInstr);
        ASTUtils.getContinueList(node).add(gotoInstr);
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        List<GotoInstr> breakList = new ArrayList<>();
        List<GotoInstr> continueList = new ArrayList<>();
        Statement s = null, ps;
        Iterator<Statement> it = node.getStatements().iterator();
        while (it.hasNext()) {
            ps = s;
            s = it.next();
            if (ps != null && !ASTUtils.getNextList(ps).isEmpty()) {
                Program.backpatch(ASTUtils.getNextList(ps), program.addNewLabel());
            }
            s.accept(this);
            breakList.addAll(ASTUtils.getBreakList(s));
            continueList.addAll(ASTUtils.getContinueList(s));
        }
        if (s != null) {
            ASTUtils.setNextList(node, ASTUtils.getNextList(s));
        }
        ASTUtils.setBreakList(node, breakList);
        ASTUtils.setContinueList(node, continueList);
    }

    @Override
    public void visit(VariableDefinitionStatement node) throws ASTVisitorException {

    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {

    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {

    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
            program.add(new ReturnInstr(stack.pop()));
        } else {
            program.add(new ReturnInstr(""));
        }
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        program.add(new FunctionDefInstr(node.getIdentifier()));
        for (ParameterDeclaration p : node.getParameters()) {
            p.accept(this);
        }
    }

    @Override
    public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            if (node.getLiteral() != 0) { // if true
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getTrueList(node).add(i);
            } else {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getFalseList(node).add(i);
            }
        } else {
            String temporary = createTemp();
            stack.push(temporary);
            program.add(new AssignInstr("'" + StringEscapeUtils.escapeJava(node.getLiteral().toString()) + "'", temporary));
        }
    }

    @Override
    public void visit(ArrayInitExpression node) throws ASTVisitorException {
        node.getIntegerLiteralExpression().accept(this);
        String temporary = createTemp();
        program.add(new ArrayInitInstr(node.getTypeSpecifier(), stack.pop(), temporary));
        stack.push(temporary);
    }

    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ArrayExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);

        /*  e.g.,

            a[i]
            
            t1 = i*4 (because it's an int)
            t2 = a[t1]
        
            x = a[i];
        
            t3 = t2
            
            b[i+1]
            
            t4 = i+1
            t5 = t4*4
            t6 = b[t5]
         */
        String temporary = stack.pop();
        String temporary1 = createTemp();

        try {
            program.add(new AssignInstr(temporary + "*" + getSize(ASTUtils.getType(node)), temporary1));
        } catch (TypeException ex) {
            Logger.getLogger(IntermediateCodeASTVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }

        String temporary2 = createTemp();
        stack.push(temporary2);
        ArrayInstr arrayInstr = new ArrayInstr(node.getIdentifier(), temporary1, temporary2);
        program.add(arrayInstr);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        List<String> paramsName = new ArrayList<>();
        String temporary;

        List<Expression> expressionsList = node.getExpressions();
        Collections.reverse(expressionsList);
        for (Expression e : expressionsList) {
            e.accept(this);
        }

        for (Expression foo : expressionsList) {
            temporary = stack.pop();
            paramsName.add(temporary);
        }

        FunCallInstr functionCallInstruction;

        if (!ASTUtils.getType(node).equals(Type.VOID_TYPE)) {
            temporary = createTemp();
            stack.push(temporary);
            functionCallInstruction = new FunCallInstr(node.getIdentifier(), paramsName, expressionsList.size(), temporary);
        } else {
            functionCallInstruction = new FunCallInstr(node.getIdentifier(), paramsName, expressionsList.size(), null);
        }

        program.add(functionCallInstruction);
    }

    public int getSize(Type type) throws TypeException {
        if (type.equals(Type.INT_TYPE)) {
            return 4;
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            return 8;
        } else if (type.equals(Type.CHAR)) {
            return 1;
        }
        throw new TypeException("Unknown type size");
    }

}
