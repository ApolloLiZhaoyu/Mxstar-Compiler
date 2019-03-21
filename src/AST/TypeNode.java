package AST;

import Type.Type;

public class TypeNode extends Node{
    protected Type type;

    public TypeNode() {
        type = new Type();
    }

    public TypeNode(Type type, Location location) {
        this.type = type;
        this.location = location;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(ASTVistor vistor) {
        vistor.visit(this);
    }
}
