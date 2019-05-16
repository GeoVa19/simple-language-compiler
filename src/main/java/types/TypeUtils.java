package types;

import ast.Operator;
import java.util.Set;
import org.objectweb.asm.Type;

public class TypeUtils {

    public static final Type STRING_TYPE = Type.getType(String.class);

    private TypeUtils() {
    }

    public static Type maxType(Type type1, Type type2) {
        if (type1.equals(STRING_TYPE)) {
            return type1;
        } else if (type2.equals(STRING_TYPE)) {
            return type2;
        } else if (type1.equals(Type.DOUBLE_TYPE)) {
            return type1;
        } else if (type2.equals(Type.DOUBLE_TYPE)) {
            return type2;
        } else if (type1.equals(Type.INT_TYPE)) {
            return type1;
        } else if (type2.equals(Type.INT_TYPE)) {
            return type2;
        } else if (type1.equals(Type.BOOLEAN_TYPE)) {
            return type1;
        } else if (type2.equals(Type.BOOLEAN_TYPE)) {
            return type2;
        } else {
            return type1;
        }
    }

    public static Type minType(Type type1, Type type2) {
        if (type1.equals(Type.BOOLEAN_TYPE)) {
            return type1;
        } else if (type2.equals(Type.BOOLEAN_TYPE)) {
            return type2;
        } else if (type1.equals(Type.INT_TYPE)) {
            return type1;
        } else if (type2.equals(Type.INT_TYPE)) {
            return type2;
        } else if (type1.equals(Type.DOUBLE_TYPE)) {
            return type1;
        } else if (type2.equals(Type.DOUBLE_TYPE)) {
            return type2;
        } else if (type1.equals(STRING_TYPE)) {
            return type1;
        } else if (type2.equals(STRING_TYPE)) {
            return type2;
        } else {
            return type1;
        }
    }

    public static boolean isLargerOrEqualType(Type type1, Type type2) {
        return type1.getSort() >= type2.getSort();
    }

    public static boolean isAssignable(Type target, Type source) {
        return isLargerOrEqualType(target, source);
    }

    public static Type maxType(Set<Type> types) {
        Type max = null;
        for (Type t : types) {
            if (max == null) {
                max = t;
            }
            max = maxType(max, t);
        }
        return max;
    }

    public static Type minType(Set<Type> types) {
        Type min = null;
        for (Type t : types) {
            if (min == null) {
                min = t;
            }
            min = minType(min, t);
        }
        return min;
    }

    public static boolean isUnaryComparible(Operator op, Type type) {
        switch (op) {
            case MINUS:
                return isNumber(type);
            case LOGICAL_NOT:
                return type.equals(Type.INT_TYPE);
            default:
                return false;
        }
    }

    public static boolean isNumber(Type type) {
        return type.equals(Type.INT_TYPE) || type.equals(Type.DOUBLE_TYPE);
    }

    public static boolean isNumber(Set<Type> types) {
        return types.stream().anyMatch((t) -> (t.equals(Type.INT_TYPE) || t.equals(Type.DOUBLE_TYPE)));
    }

    public static Type applyUnary(Operator op, Type type) throws TypeException {
        if (!op.isUnary()) {
            throw new TypeException("Operator " + op + " is not unary");
        }
        if (!TypeUtils.isUnaryComparible(op, type)) {
            throw new TypeException("Type " + type + " is not unary comparible");
        }
        return type;
    }

    public static Type applyBinary(Operator op, Type t1, Type t2) throws TypeException {
        if (op.isRelational()) {
            if (TypeUtils.areComparable(t1, t2)) {
                return Type.INT_TYPE;
            } else {
                throw new TypeException("Expressions are not comparable");
            }
        } else if (op.isArithmetic()) {
            if ((t1.equals(Type.INT_TYPE) || t1.equals(Type.DOUBLE_TYPE)) && (t2.equals(Type.INT_TYPE) || t2.equals(Type.DOUBLE_TYPE))) {
                return maxType(t1, t2);
            } else {
                throw new TypeException("Expressions cannot be handled as numbers");
            }
        } else if (op.isLogical()) {
            if (t1.equals(Type.INT_TYPE) && t2.equals(Type.INT_TYPE)) {
                return Type.INT_TYPE;
            } else {
                throw new TypeException("At least one operand is not of integer type");
            }
        }
        throw new TypeException("Operator " + op + " not supported");
    }

    public static boolean areComparable(Type type1, Type type2) {
        if (type1.equals(Type.INT_TYPE)) {
            return type2.equals(Type.INT_TYPE) || type2.equals(Type.DOUBLE_TYPE);
        } else if (type1.equals(Type.DOUBLE_TYPE)) {
            return type2.equals(Type.INT_TYPE) || type2.equals(Type.DOUBLE_TYPE);
        } else if (type1.equals(Type.CHAR_TYPE)) {
            return type2.equals(Type.CHAR_TYPE);
        } else if (type1.equals(Type.getType("[C"))) {
            return type2.equals(Type.getType("[C"));
        } else {
            return false;
        }
    }

}
