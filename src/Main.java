import AST.Program;
import FrontEnd.*;
import Parser.MxstarLexer;
import Parser.MxstarParser;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        //String inputFile = "test.cpp";
        //InputStream inputStream = new FileInputStream(inputFile);
        InputStream inputStream = System.in;
        try{
            compile(inputStream);
        } catch (Error error) {
            System.err.println(error.getMessage());
            System.exit(1);
        }
    }

    private static void compile(InputStream sourceCode) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(sourceCode);
        MxstarLexer lexer = new MxstarLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MxstarParser parser = new MxstarParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new PaserErrorListener());
        ParseTree tree = parser.program();
        ASTBuilder astBuilder = new ASTBuilder();
        astBuilder.visit(tree);
        Program program = astBuilder.getProgram();

        ASTPrinter astPrinter = new ASTPrinter();

        //astPrinter.visit(program);
        //astPrinter.print();
        ScopeBuilder scopeBuilder = new ScopeBuilder();
        scopeBuilder.visit(program);
        SemanticChecker semanticChecker = new SemanticChecker(scopeBuilder.getGlobalScope());
        semanticChecker.visit(program);
    }
}
