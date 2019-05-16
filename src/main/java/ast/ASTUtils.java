package ast;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.JumpInsnNode;
import symbol.LocalIndexPool;

import symbol.SymTable;
import symbol.SymTableEntry;
import threeaddr.GotoInstr;

/**
 * Class with static helper methods for AST handling
 */
public class ASTUtils {

    public static final String SYMTABLE_PROPERTY = "SYMTABLE_PROPERTY";
    public static final String LOCAL_INDEX_POOL_PROPERTY = "LOCAL_INDEX_POOL_PROPERTY";
    public static final String IS_BOOLEAN_EXPR_PROPERTY = "IS_BOOLEAN_EXPR_PROPERTY";
    public static final String TYPE_PROPERTY = "TYPE_PROPERTY";
    public static final String IS_VALID_LVALUE = "IS_VALID_LVALUE";
    
    public static final String NEXT_LIST_PROPERTY = "NEXT_LIST_PROPERTY";
    public static final String BREAK_LIST_PROPERTY = "BREAK_LIST_PROPERTY";
    public static final String CONTINUE_LIST_PROPERTY = "CONTINUE_LIST_PROPERTY";
    public static final String TRUE_LIST_PROPERTY = "TRUE_LIST_PROPERTY";
    public static final String FALSE_LIST_PROPERTY = "FALSE_LIST_PROPERTY";
    
    public static final String NEXT_LIST_PROPERTY_BYTECODE = "NEXT_LIST_PROPERTY_BYTECODE";
    public static final String BREAK_LIST_PROPERTY_BYTECODE = "BREAK_LIST_PROPERTY_BYTECODE";
    public static final String CONTINUE_LIST_PROPERTY_BYTECODE = "CONTINUE_LIST_PROPERTY_BYTECODE";
    public static final String TRUE_LIST_PROPERTY_BYTECODE = "TRUE_LIST_PROPERTY_BYTECODE";
    public static final String FALSE_LIST_PROPERTY_BYTECODE = "FALSE_LIST_PROPERTY_BYTECODE";

    private ASTUtils() {
    }

    public static void setLocalIndexPool(ASTNode node, LocalIndexPool pool) {
        node.setProperty(LOCAL_INDEX_POOL_PROPERTY, pool);
    }

    @SuppressWarnings("unchecked")
    public static LocalIndexPool getSafeLocalIndexPool(ASTNode node) throws ASTVisitorException {
        LocalIndexPool lip = (LocalIndexPool) node.getProperty(LOCAL_INDEX_POOL_PROPERTY);
        if (lip == null) {
            ASTUtils.error(node, "Local index pool not found.");
        }
        return lip;
    }

    @SuppressWarnings("unchecked")
    public static SymTable<SymTableEntry> getEnv(ASTNode node) {
        return (SymTable<SymTableEntry>) node.getProperty(SYMTABLE_PROPERTY);
    }

    public static boolean isLValueExpression(Expression node) {
        Boolean b = (Boolean) node.getProperty(IS_VALID_LVALUE);
        if (b == null) {
            return false;
        }
        return b;
    }

    public static void setLValueExpression(Expression node, boolean value) {
        node.setProperty(IS_VALID_LVALUE, value);
    }

    @SuppressWarnings("unchecked")
    public static SymTable<SymTableEntry> getSafeEnv(ASTNode node)
            throws ASTVisitorException {
        SymTable<SymTableEntry> symTable = (SymTable<SymTableEntry>) node.getProperty(SYMTABLE_PROPERTY);
        if (symTable == null) {
            ASTUtils.error(node, "Symbol table not found.");
        }
        return symTable;
    }

    public static void setEnv(ASTNode node, SymTable<SymTableEntry> env) {
        node.setProperty(SYMTABLE_PROPERTY, env);
    }

    public static boolean isBooleanExpression(Expression node) {
        Boolean b = (Boolean) node.getProperty(IS_BOOLEAN_EXPR_PROPERTY);
        if (b == null) {
            return false;
        }
        return b;
    }

    public static void setBooleanExpression(Expression node, boolean value) {
        node.setProperty(IS_BOOLEAN_EXPR_PROPERTY, value);
    }

    public static Type getType(ASTNode node) {
        return (Type) node.getProperty(TYPE_PROPERTY);
    }

    public static Type getSafeType(ASTNode node) throws ASTVisitorException {
        Type type = (Type) node.getProperty(TYPE_PROPERTY);
        if (type == null) {
            ASTUtils.error(node, "Type not found.");
        }
        return type;
    }

    public static void setType(ASTNode node, Type type) {
        node.setProperty(TYPE_PROPERTY, type);
    }

    public static void error(ASTNode node, String message) throws ASTVisitorException {
        throw new ASTVisitorException(node.getLine() + ":" + node.getColumn()
                + ": " + message);
    }

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getTrueList(Expression node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(TRUE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(TRUE_LIST_PROPERTY, l);
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getTrueList_Bytecode(Expression node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(TRUE_LIST_PROPERTY_BYTECODE);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(TRUE_LIST_PROPERTY_BYTECODE, l);
        }
        return l;
    }

    public static void setTrueList(Expression node, List<GotoInstr> list) {
        node.setProperty(TRUE_LIST_PROPERTY, list);
    }

    public static void setTrueList_Bytecode(Expression node, List<JumpInsnNode> list) {
        node.setProperty(TRUE_LIST_PROPERTY_BYTECODE, list);
    }

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getFalseList(Expression node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(FALSE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(FALSE_LIST_PROPERTY, l);
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getFalseList_Bytecode(Expression node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(FALSE_LIST_PROPERTY_BYTECODE);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(FALSE_LIST_PROPERTY_BYTECODE, l);
        }
        return l;
    }

    public static void setFalseList(Expression node, List<GotoInstr> list) {
        node.setProperty(FALSE_LIST_PROPERTY, list);
    }

    public static void setFalseList_Bytecode(Expression node, List<JumpInsnNode> list) {
        node.setProperty(FALSE_LIST_PROPERTY_BYTECODE, list);
    }

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getNextList(Statement node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(NEXT_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(NEXT_LIST_PROPERTY, l);
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getNextList_Bytecode(Statement node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(NEXT_LIST_PROPERTY_BYTECODE);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(NEXT_LIST_PROPERTY_BYTECODE, l);
        }
        return l;
    }

    public static void setNextList(Statement node, List<GotoInstr> list) {
        node.setProperty(NEXT_LIST_PROPERTY, list);
    }

    public static void setNextList_Bytecode(Statement node, List<JumpInsnNode> list) {
        node.setProperty(NEXT_LIST_PROPERTY_BYTECODE, list);
    }

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getBreakList(Statement node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(BREAK_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(BREAK_LIST_PROPERTY, l);
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getBreakList_Bytecode(Statement node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(BREAK_LIST_PROPERTY_BYTECODE);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(BREAK_LIST_PROPERTY_BYTECODE, l);
        }
        return l;
    }

    public static void setBreakList(Statement node, List<GotoInstr> list) {
        node.setProperty(BREAK_LIST_PROPERTY, list);
    }

    public static void setBreakList_Bytecode(Statement node, List<JumpInsnNode> list) {
        node.setProperty(BREAK_LIST_PROPERTY_BYTECODE, list);
    }

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getContinueList(Statement node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(CONTINUE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(CONTINUE_LIST_PROPERTY, l);
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getContinueList_Bytecode(Statement node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(CONTINUE_LIST_PROPERTY_BYTECODE);
        if (l == null) {
            l = new ArrayList<>();
            node.setProperty(CONTINUE_LIST_PROPERTY_BYTECODE, l);
        }
        return l;
    }

    public static void setContinueList(Statement node, List<GotoInstr> list) {
        node.setProperty(CONTINUE_LIST_PROPERTY, list);
    }

    public static void setContinueList_Bytecode(Statement node, List<JumpInsnNode> list) {
        node.setProperty(CONTINUE_LIST_PROPERTY_BYTECODE, list);
    }

}
