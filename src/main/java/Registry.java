
import ast.ASTNode;

/**
 * Global registry (Singleton pattern)
 */
public class Registry {

    ASTNode root;

    private Registry() {
        root = null;
    }

    private static class SingletonHolder {

        public static final Registry INSTANCE = new Registry();

    }

    public static Registry getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public ASTNode getRoot() {
        return root;
    }

    public void setRoot(ASTNode root) {
        this.root = root;
    }

}
