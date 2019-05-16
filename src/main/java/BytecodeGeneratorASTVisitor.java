
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ast.ASTNode;
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
import ast.Operator;
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
import symbol.LocalIndexPool;
import symbol.SymTableEntry;
import types.TypeUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import symbol.SymTable;

public class BytecodeGeneratorASTVisitor implements ASTVisitor {

    private final ClassNode cn;
    private MethodNode mn;

    public BytecodeGeneratorASTVisitor() {
        // create class
        cn = new ClassNode();
        cn.access = Opcodes.ACC_PUBLIC;
        cn.version = Opcodes.V1_5;
        cn.name = "Demo";
        cn.superName = "java/lang/Object";

        // create constructor
        mn = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        mn.maxLocals = 1;
        mn.maxStack = 1;
        cn.methods.add(mn);

        // random function
        mn = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "random", "(I)I", null, null);
        mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/util/Random"));
        mn.instructions.add(new InsnNode(Opcodes.DUP));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/Random", "<init>", "()V"));
        mn.instructions.add(new VarInsnNode(Opcodes.ASTORE, 1));
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Random", "nextInt", "(I)I"));
        mn.instructions.add(new InsnNode(Opcodes.IRETURN));
        mn.maxLocals = 10;
        mn.maxStack = 32;
        cn.methods.add(mn);

        // read int from keyboard function
        mn = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "scanInt", "()I", null, null);
        mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/util/Scanner"));
        mn.instructions.add(new InsnNode(Opcodes.DUP));
        mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;"));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V"));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I"));
        mn.instructions.add(new InsnNode(Opcodes.IRETURN));
        mn.maxLocals = 10;
        mn.maxStack = 32;
        cn.methods.add(mn);
    }

    public ClassNode getClassNode() {
        return cn;
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

                if (ps != null && !ASTUtils.getNextList_Bytecode(ps).isEmpty()) {
                    LabelNode labelNode = new LabelNode();
                    mn.instructions.add(labelNode);
                    backpatch(ASTUtils.getNextList_Bytecode(ps), labelNode);
                }

                s.accept(this);

                if (!ASTUtils.getBreakList_Bytecode(s).isEmpty()) {
                    ASTUtils.error(s, "Break detected without a loop.");
                }

                if (!ASTUtils.getContinueList_Bytecode(s).isEmpty()) {
                    ASTUtils.error(s, "Continue detected without a loop.");
                }
            }
            if (s != null && !ASTUtils.getNextList_Bytecode(s).isEmpty()) {
                LabelNode labelNode = new LabelNode();
                mn.instructions.add(labelNode);
                backpatch(ASTUtils.getNextList_Bytecode(s), labelNode);
            }
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
        }
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(jmp);
        ASTUtils.getBreakList_Bytecode(node).add(jmp);
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(jmp);
        ASTUtils.getContinueList_Bytecode(node).add(jmp);
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        List<JumpInsnNode> breakList = new ArrayList<>();
        List<JumpInsnNode> continueList = new ArrayList<>();
        Statement s = null, ps;
        Iterator<Statement> it = node.getStatements().iterator();
        while (it.hasNext()) {
            ps = s;
            s = it.next();
            if (ps != null && !ASTUtils.getNextList_Bytecode(ps).isEmpty()) {
                LabelNode labelNode = new LabelNode();
                mn.instructions.add(labelNode);
                backpatch(ASTUtils.getNextList_Bytecode(ps), labelNode);
            }
            s.accept(this);
            breakList.addAll(ASTUtils.getBreakList_Bytecode(s));
            continueList.addAll(ASTUtils.getContinueList_Bytecode(s));
        }
        if (s != null) {
            ASTUtils.setNextList_Bytecode(node, ASTUtils.getNextList_Bytecode(s));
        }
        ASTUtils.setBreakList_Bytecode(node, breakList);
        ASTUtils.setContinueList_Bytecode(node, continueList);
    }

    @Override
    public void visit(VariableDefinitionStatement node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode labelNode = new LabelNode();
        mn.instructions.add(labelNode);
        List<JumpInsnNode> trueList_Bytecode = ASTUtils.getTrueList_Bytecode(node.getExpression());
        backpatch(ASTUtils.getTrueList_Bytecode(node.getExpression()), labelNode);

        node.getStatement().accept(this);

        ASTUtils.getBreakList_Bytecode(node).addAll(ASTUtils.getBreakList_Bytecode(node.getStatement()));
        ASTUtils.getContinueList_Bytecode(node).addAll(ASTUtils.getContinueList_Bytecode(node.getStatement()));

        ASTUtils.getNextList_Bytecode(node).addAll(ASTUtils.getFalseList_Bytecode(node.getExpression()));
        ASTUtils.getNextList_Bytecode(node).addAll(ASTUtils.getNextList_Bytecode(node.getStatement()));
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode stmt1StartLabelNode = new LabelNode();
        mn.instructions.add(stmt1StartLabelNode);
        node.getStatement1().accept(this);

        JumpInsnNode skipGoto = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(skipGoto);

        LabelNode stmt2StartLabelNode = new LabelNode();
        mn.instructions.add(stmt2StartLabelNode);
        node.getStatement2().accept(this);

        backpatch(ASTUtils.getTrueList_Bytecode(node.getExpression()), stmt1StartLabelNode);
        backpatch(ASTUtils.getFalseList_Bytecode(node.getExpression()), stmt2StartLabelNode);

        ASTUtils.getNextList_Bytecode(node).addAll(ASTUtils.getNextList_Bytecode(node.getStatement1()));
        ASTUtils.getNextList_Bytecode(node).addAll(ASTUtils.getNextList_Bytecode(node.getStatement2()));
        ASTUtils.getNextList_Bytecode(node).add(skipGoto);

        ASTUtils.getBreakList_Bytecode(node).addAll(ASTUtils.getBreakList_Bytecode(node.getStatement1()));
        ASTUtils.getBreakList_Bytecode(node).addAll(ASTUtils.getBreakList_Bytecode(node.getStatement2()));

        ASTUtils.getContinueList_Bytecode(node).addAll(ASTUtils.getContinueList_Bytecode(node.getStatement1()));
        ASTUtils.getContinueList_Bytecode(node).addAll(ASTUtils.getContinueList_Bytecode(node.getStatement2()));
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        LabelNode beginLabelNode = new LabelNode();
        mn.instructions.add(beginLabelNode);

        node.getExpression().accept(this);

        LabelNode trueLabelNode = new LabelNode();
        mn.instructions.add(trueLabelNode);
        backpatch(ASTUtils.getTrueList_Bytecode(node.getExpression()), trueLabelNode);

        node.getStatement().accept(this);

        backpatch(ASTUtils.getNextList_Bytecode(node.getStatement()), beginLabelNode);
        backpatch(ASTUtils.getContinueList_Bytecode(node.getStatement()), beginLabelNode);

        mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, beginLabelNode));

        ASTUtils.getNextList_Bytecode(node).addAll(ASTUtils.getFalseList_Bytecode(node.getExpression()));
        ASTUtils.getNextList_Bytecode(node).addAll(ASTUtils.getBreakList_Bytecode(node.getStatement()));
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            JumpInsnNode i = new JumpInsnNode(Opcodes.GOTO, null);
            mn.instructions.add(i);
            if (node.getLiteral() != 0) {
                ASTUtils.getTrueList_Bytecode(node).add(i);
            } else {
                ASTUtils.getFalseList_Bytecode(node).add(i);
            }
        } else {
            mn.instructions.add(new LdcInsnNode(node.getLiteral()));
        }
    }

    @Override
    public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            JumpInsnNode i = new JumpInsnNode(Opcodes.GOTO, null);
            mn.instructions.add(i);
            if (node.getLiteral() != 0d) {
                ASTUtils.getTrueList_Bytecode(node).add(i);
            } else {
                ASTUtils.getFalseList_Bytecode(node).add(i);
            }
        } else {
            mn.instructions.add(new LdcInsnNode(node.getLiteral()));
        }
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            ASTUtils.error(node, "String cannot be a boolean expression");
        }
        mn.instructions.add(new LdcInsnNode(node.getLiteral()));
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(PrintStatement node) throws ASTVisitorException {
        Type expressionType = ASTUtils.getSafeType(node.getExpression());

        if (expressionType.equals(Type.getMethodType("[C"))) {
            expressionType = TypeUtils.STRING_TYPE;
        }

        mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        node.getExpression().accept(this);
        if (node.getExpression() instanceof ArrayExpression) {
            mn.instructions.add(new InsnNode(Opcodes.IALOAD));
        }
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(" + expressionType + ")V"));
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);

        Type type = ASTUtils.getSafeType(node.getExpression());

        if (node.getOperator().equals(Operator.MINUS)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.INEG)));
        } else if (ASTUtils.isBooleanExpression(node)) {
            if (node.getOperator().equals(Operator.LOGICAL_NOT)) {
                ASTUtils.getTrueList_Bytecode(node).addAll(ASTUtils.getFalseList_Bytecode(node.getExpression()));

                ASTUtils.getFalseList_Bytecode(node).addAll(ASTUtils.getTrueList_Bytecode(node.getExpression()));
            }
        } else {
            ASTUtils.error(node, "Operator not recognized.");
        }
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        if (!(node.getExpression1() instanceof IdentifierExpression)) {
            node.getExpression1().accept(this);
        }
        node.getExpression2().accept(this);

        SymTableEntry entry = null;
        if (node.getExpression1() instanceof IdentifierExpression) {
            entry = ASTUtils.getSafeEnv(node).lookup(((IdentifierExpression) node.getExpression1()).getIdentifier());
        } else if (node.getExpression1() instanceof ArrayExpression) {
            entry = ASTUtils.getSafeEnv(node).lookup(((ArrayExpression) node.getExpression1()).getIdentifier());
        }

        if (node.getExpression2() instanceof ArrayExpression) {
            mn.instructions.add(new InsnNode(Opcodes.IALOAD));
        }

        int index = entry.getIndex();

        Type target = ASTUtils.getSafeType(node.getExpression1());
        Type source = ASTUtils.getSafeType(node.getExpression2());

        if (!(node.getExpression1() instanceof ArrayInitExpression)) {
            widen(target, source);
        }

        if (node.getExpression1() instanceof ArrayExpression) {
            mn.instructions.add(new InsnNode(target.getOpcode(Opcodes.IASTORE)));
        } else {
            mn.instructions.add(new VarInsnNode(target.getOpcode(Opcodes.ISTORE), index));
        }
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        SymTableEntry entry = ASTUtils.getSafeEnv(node).lookup(node.getIdentifier());
        Type identifierType = entry.getType();
        int identifierIndex = entry.getIndex();
        mn.instructions.add(new VarInsnNode(identifierType.getOpcode(Opcodes.ILOAD), identifierIndex));
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        if (node.getExpression1() instanceof ArrayExpression) {
            mn.instructions.add(new InsnNode(Opcodes.IALOAD));
        }
        Type expr1Type = ASTUtils.getSafeType(node.getExpression1());

        node.getExpression2().accept(this);
        if (node.getExpression2() instanceof ArrayExpression) {
            mn.instructions.add(new InsnNode(Opcodes.IALOAD));
        }

        Type expr2Type = ASTUtils.getSafeType(node.getExpression2());

        Type maxType = TypeUtils.maxType(expr1Type, expr2Type);

        // cast top of stack to max
        if (!maxType.equals(expr2Type)) {
            widen(maxType, expr2Type);
        }

        // cast second from top to max
        if (!maxType.equals(expr1Type)) {
            LocalIndexPool lip = ASTUtils.getSafeLocalIndexPool(node);
            int localIndex = -1;
            if (expr2Type.equals(Type.DOUBLE_TYPE) || expr1Type.equals(Type.DOUBLE_TYPE)) {
                localIndex = lip.getLocalIndex(expr2Type);
                mn.instructions.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ISTORE), localIndex));
            } else {
                mn.instructions.add(new InsnNode(Opcodes.SWAP));
            }
            widen(maxType, expr1Type);
            if (expr2Type.equals(Type.DOUBLE_TYPE) || expr1Type.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ILOAD), localIndex));
                lip.freeLocalIndex(localIndex, expr2Type);
            } else {
                mn.instructions.add(new InsnNode(Opcodes.SWAP));
            }
        }

        if (ASTUtils.isBooleanExpression(node)) {

            if (!node.getOperator().isRelational() && !node.getOperator().isLogical()) {
                ASTUtils.error(node, "A not boolean expression used as boolean.");
            }

            ASTUtils.setBooleanExpression(node.getExpression1(), true);
            ASTUtils.setBooleanExpression(node.getExpression2(), true);

            handleBooleanOperator(node, node.getOperator(), maxType);

            if (node.getOperator().equals(Operator.LOGICAL_OR)) {
                backpatch(ASTUtils.getFalseList_Bytecode(node.getExpression1()), new LabelNode());

                ASTUtils.getTrueList_Bytecode(node).addAll(ASTUtils.getTrueList_Bytecode(node.getExpression1()));
                ASTUtils.getTrueList_Bytecode(node).addAll(ASTUtils.getTrueList_Bytecode(node.getExpression2()));

                ASTUtils.getFalseList_Bytecode(node).addAll(ASTUtils.getFalseList_Bytecode(node.getExpression2()));
            }

            if (node.getOperator().equals(Operator.LOGICAL_AND)) {
                backpatch(ASTUtils.getTrueList_Bytecode(node.getExpression1()), new LabelNode());

                ASTUtils.getTrueList_Bytecode(node).addAll(ASTUtils.getTrueList_Bytecode(node.getExpression2()));

                ASTUtils.getFalseList_Bytecode(node).addAll(ASTUtils.getFalseList_Bytecode(node.getExpression1()));
                ASTUtils.getFalseList_Bytecode(node).addAll(ASTUtils.getFalseList_Bytecode(node.getExpression2()));
            }
        } else if (maxType.equals(TypeUtils.STRING_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            handleStringOperator(node, node.getOperator());
        } else {
            handleNumberOperator(node, node.getOperator(), maxType);
        }
    }

    private void backpatch(List<JumpInsnNode> list, LabelNode labelNode) {
        if (list == null) {
            return;
        }
        for (JumpInsnNode instr : list) {
            instr.label = labelNode;
        }
    }

    /**
     * Cast the top of the stack to a particular type
     */
    private void widen(Type target, Type source) {
        if (source.equals(target)) {
            return;
        }

        if (source.equals(Type.BOOLEAN_TYPE)) {
            if (target.equals(Type.INT_TYPE)) {
                // nothing
            } else if (target.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.I2D));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.INT_TYPE)) {
            if (target.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.I2D));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.DOUBLE_TYPE)) {
            if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;"));
            }
        }
    }

    private void handleBooleanOperator(Expression node, Operator op, Type type) throws ASTVisitorException {
        List<JumpInsnNode> trueList = new ArrayList<>();

        if (type.equals(Type.getType("[C"))) {
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            JumpInsnNode jmp = null;
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFNE, null);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mn.instructions.add(jmp);
            trueList.add(jmp);
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.DCMPG));
            JumpInsnNode jmp = null;
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                    mn.instructions.add(jmp);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFNE, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATER:
                    jmp = new JumpInsnNode(Opcodes.IFLT, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATER_EQ:
                    jmp = new JumpInsnNode(Opcodes.IFGE, null);
                    mn.instructions.add(jmp);
                    break;
                case LESS:
                    jmp = new JumpInsnNode(Opcodes.IFLT, null);
                    mn.instructions.add(jmp);
                    break;
                case LESS_EQ:
                    jmp = new JumpInsnNode(Opcodes.IFLE, null);
                    mn.instructions.add(jmp);
                    break;
                case LOGICAL_OR:
                    jmp = new JumpInsnNode(Opcodes.GOTO, null);
                    mn.instructions.add(jmp);
                    break;
                case LOGICAL_AND:
                    jmp = new JumpInsnNode(Opcodes.GOTO, null);
                    mn.instructions.add(jmp);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported");
                    break;
            }
            trueList.add(jmp);
        } else { // INT_TYPE and CHAR_TYPE
            JumpInsnNode jmp = null;
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);
                    mn.instructions.add(jmp);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATER:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATER_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                    mn.instructions.add(jmp);
                    break;
                case LESS:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                    mn.instructions.add(jmp);
                    break;
                case LESS_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLE, null);
                    mn.instructions.add(jmp);
                    break;
                case LOGICAL_OR:
                    jmp = new JumpInsnNode(Opcodes.GOTO, null);
                    mn.instructions.add(jmp);
                    break;
                case LOGICAL_AND:
                    jmp = new JumpInsnNode(Opcodes.GOTO, null);
                    mn.instructions.add(jmp);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported");
                    break;
            }
            trueList.add(jmp);
        }
        ASTUtils.setTrueList_Bytecode(node, trueList);
        List<JumpInsnNode> falseList = new ArrayList<>();
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(jmp);
        falseList.add(jmp);
        ASTUtils.setFalseList_Bytecode(node, falseList);
    }

    /**
     * Assumes top of stack contains two strings
     */
    private void handleStringOperator(ASTNode node, Operator op) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
            mn.instructions.add(new InsnNode(Opcodes.DUP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        } else if (op.isRelational()) {
            LabelNode trueLabelNode = new LabelNode();
            switch (op) {
                case EQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFNE, trueLabelNode));
                    break;
                case NOT_EQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFEQ, trueLabelNode));
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
            LabelNode endLabelNode = new LabelNode();
            mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
            mn.instructions.add(trueLabelNode);
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
            mn.instructions.add(endLabelNode);
        } else {
            ASTUtils.error(node, "Operator not recognized");
        }
    }

    private void handleNumberOperator(ASTNode node, Operator op, Type type) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IADD)));
        } else if (op.equals(Operator.MINUS)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.ISUB)));
        } else if (op.equals(Operator.MULTIPLY)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IMUL)));
        } else if (op.equals(Operator.DIVIDE)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IDIV)));
        } else if (op.equals(Operator.MOD)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IREM)));
        } else if (op.isRelational()) {
            if (type.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.DCMPG));
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mn.instructions.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported");
                        break;
                }
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode();
                mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                LabelNode trueLabelNode = new LabelNode();
                jmp.label = trueLabelNode;
                mn.instructions.add(trueLabelNode);
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(endLabelNode);
            } else if (type.equals(Type.INT_TYPE)) {
                LabelNode trueLabelNode = new LabelNode();
                switch (op) {
                    case EQUAL:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, trueLabelNode));
                        break;
                    case NOT_EQUAL:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPNE, trueLabelNode));
                        break;
                    case GREATER:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGT, trueLabelNode));
                        break;
                    case GREATER_EQ:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, trueLabelNode));
                        break;
                    case LESS:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLT, trueLabelNode));
                        break;
                    case LESS_EQ:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLE, trueLabelNode));
                        break;
                    default:
                        break;
                }
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode();
                mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                mn.instructions.add(trueLabelNode);
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(endLabelNode);
            } else {
                ASTUtils.error(node, "Cannot compare such types.");
            }
        } else {
            ASTUtils.error(node, "Operator " + op + " not recognized.");
        }
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
            if (node.getExpression() instanceof ArrayExpression) {
                mn.instructions.add(new InsnNode(Opcodes.IALOAD));
            }

            Type expressionType = ASTUtils.getSafeType(node.getExpression());
            mn.instructions.add(new InsnNode(expressionType.getOpcode(Opcodes.IRETURN)));
        } else {
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
        }
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        SymTable<SymTableEntry> symbolTable = ASTUtils.getSafeEnv(node);
        SymTableEntry entry = symbolTable.lookup(node.getIdentifier() + "_f");
        String functionType = entry.getType().getDescriptor();

        mn = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, node.getIdentifier(), functionType, null, null);

        for (ParameterDeclaration p : node.getParameters()) {
            p.accept(this);
        }

        mn.maxLocals = ASTUtils.getSafeLocalIndexPool(node).getMaxLocals() + 1;
        mn.maxStack = 32;

        cn.methods.add(mn);
    }

    @Override
    public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
        mn.instructions.add(new LdcInsnNode(node.getLiteral()));
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {

    }

    @Override
    public void visit(ArrayInitExpression node) throws ASTVisitorException {
        node.getIntegerLiteralExpression().accept(this);

        Type type = node.getTypeSpecifier().getElementType();

        int a = 0;

        if (type.equals(Type.INT_TYPE)) {
            a = Opcodes.T_INT;
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            a = Opcodes.T_DOUBLE;
        } else if (type.equals(Type.CHAR_TYPE)) {
            a = Opcodes.T_CHAR;
        } else {
            ASTUtils.error(node, "Type not recognized");
        }

        mn.instructions.add(new IntInsnNode(Opcodes.NEWARRAY, a));
    }

    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ArrayExpression node) throws ASTVisitorException {
        SymTableEntry entry = ASTUtils.getSafeEnv(node).lookup(node.getIdentifier());
        Type identifierType = entry.getType();
        int identifierIndex = entry.getIndex();
        mn.instructions.add(new VarInsnNode(identifierType.getOpcode(Opcodes.ILOAD), identifierIndex));
        node.getExpression().accept(this);
        if (node.getExpression() instanceof ArrayExpression) {
            mn.instructions.add(new InsnNode(Opcodes.IALOAD));
        }
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        List<Expression> expressionsList = node.getExpressions();
        for (Expression e : expressionsList) {
            e.accept(this);
            if (e instanceof ArrayExpression) {
                mn.instructions.add(new InsnNode(Opcodes.IALOAD));
            }
        }

        String functionName = node.getIdentifier();
        SymTable<SymTableEntry> symbolTable = ASTUtils.getSafeEnv(node);
        SymTableEntry entry = symbolTable.lookup(node.getIdentifier() + "_f");
        String functionType = entry.getType().getDescriptor();

        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, cn.name, functionName, functionType));
    }

}
