
package ast;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract syntax tree node.
 *
 * This is the basic tree node class. Every other tree node is an instance of
 * this node.
 *
 * <p>
 * This node has a method in order to be able to accept an abstract syntax tree
 * visitor. Moreover it contains a map in order to store properties at the
 * nodes.
 * </p>
 */
public abstract class ASTNode {

    private final Map<String, Object> properties;
    private int line;
    private int column;

    /**
     * Default Constructor
     */
    public ASTNode() {
        properties = new HashMap<>();
    }

    /**
     * Get a node property by name
     *
     * @param propertyName The property name
     * @return The value of the property
     */
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Set a property at the node by name
     *
     * @param propertyName The property name
     * @param data The value of the property
     */
    public void setProperty(String propertyName, Object data) {
        properties.put(propertyName, data);
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * Accept an abstract syntax tree visitor.
     *
     * @param visitor The AST visitor.
     * @throws ASTVisitorException In case a visitor error occurs.
     */
    public abstract void accept(ASTVisitor visitor) throws ASTVisitorException;

}
