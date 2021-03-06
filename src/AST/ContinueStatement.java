package AST;

public class ContinueStatement extends Statement {
    public ContinueStatement() {}

    @Override
    public void accept(ASTVistor vistor) {
        vistor.visit(this);
    }
}
