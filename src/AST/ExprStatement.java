package AST;

public class ExprStatement extends Statement {
    private Expression expr;

    public ExprStatement() {
        expr = null;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public Expression getExpr() {
        return expr;
    }

    @Override
    public void accept(ASTVistor vistor) {
        vistor.visit(this);
    }
}
