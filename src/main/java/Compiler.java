
import ast.ASTNode;
import ast.ASTVisitor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Compiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Compiler.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.info("Usage : java Compiler [ --encoding <name> ] <inputfile(s)>");
        } else {
            int firstFilePos = 0;
            String encodingName = "UTF-8";
            if (args[0].equals("--encoding")) {
                firstFilePos = 2;
                encodingName = args[1];
                try {
                    java.nio.charset.Charset.forName(encodingName); // Side-effect: is encodingName valid? 
                } catch (Exception e) {
                    LOGGER.error("Invalid encoding '" + encodingName + "'");
                    return;
                }
            }
            for (int i = firstFilePos; i < args.length; i++) {
                Lexer scanner;
                try {
                    java.io.FileInputStream stream = new java.io.FileInputStream(args[i]);
                    LOGGER.info("Scanning file " + args[i]);
                    java.io.Reader reader = new java.io.InputStreamReader(stream, encodingName);
                    scanner = new Lexer(reader);

                    // parse
                    parser p = new parser(scanner);
                    ASTNode compUnit = (ASTNode) p.parse().value;
                    LOGGER.debug("Constructed AST");

                    // keep global instance of program
                    Registry.getInstance().setRoot(compUnit);

                    // build symbol table
                    LOGGER.debug("Building symbol table");
                    compUnit.accept(new SymTableBuilderASTVisitor());
                    LOGGER.debug("Building local variables index");
                    compUnit.accept(new LocalIndexBuilderASTVisitor());

                    // construct types
                    LOGGER.debug("Semantic check");
                    compUnit.accept(new CollectSymbolsASTVisitor());
                    compUnit.accept(new CollectTypesASTVisitor());

                    // print program
                    LOGGER.info("Input:");
                    ASTVisitor printVisitor = new PrintASTVisitor();
                    compUnit.accept(printVisitor);
                    if (false) {
                        // print 3-address code
                        LOGGER.info("3-address code:");
                        IntermediateCodeASTVisitor threeAddrVisitor = new IntermediateCodeASTVisitor();
                        compUnit.accept(threeAddrVisitor);
                        String intermediateCode = threeAddrVisitor.getProgram().emit();
                        System.out.println(intermediateCode);
                        FileUtils.writeStringToFile(new File("intermediateCode_" + i + ".txt"), intermediateCode);
                    } else {
                        // convert to java bytecode
                        LOGGER.info("Bytecode:");
                        BytecodeGeneratorASTVisitor bytecodeVisitor = new BytecodeGeneratorASTVisitor();
                        compUnit.accept(bytecodeVisitor);
                        ClassNode cn = bytecodeVisitor.getClassNode();
                        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
                        TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                        cn.accept(cv);
                        // get code
                        byte code[] = cw.toByteArray();

                        // update to file
                        LOGGER.info("Writing class to file Program_" + i + ".class");
                        try ( FileOutputStream fos = new FileOutputStream("Program_" + i + ".class")) {
                            fos.write(code);
                        }
                        LOGGER.info("Compilation done");

                        // instantiate class
                        LOGGER.info("Loading class Program_" + i + ".class");
                        ReloadingClassLoader rcl = new ReloadingClassLoader(ClassLoader.getSystemClassLoader());
                        rcl.register("Demo", code);
                        Class<?> programClass = rcl.loadClass("Demo");

                        // run main method
                        Method method = programClass.getMethod("main");
                        method.invoke(null);

                        LOGGER.info("Finished execution");
                    }
                } catch (java.io.FileNotFoundException e) {
                    LOGGER.error("File not found : \"" + args[i] + "\"");
                } catch (java.io.IOException e) {
                    LOGGER.error("IO error scanning file \"" + args[i] + "\"");
                    LOGGER.error(e.toString());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
