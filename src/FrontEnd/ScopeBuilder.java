package FrontEnd;

import AST.*;
import Scope.*;
import Type.Type;
import Utility.SemanticError;

import java.util.LinkedList;
import java.util.List;

public class ScopeBuilder implements ASTVistor {
    private GlobalScopeBuilder globalScope;
    private Scope curScope;
    //private FunctionEntity curFunctionEntity;

    public ScopeBuilder() {
        globalScope = new GlobalScopeBuilder();
        curScope = globalScope.getScope();
        //curFunctionEntity = null;
    }

    private void enterScope(Scope scope) {
        curScope = scope;
    }

    private void exitScope() {
        curScope = curScope.getParent();
    }

    private Type resolveType(TypeNode typeNode) {
        if(typeNode.isPrimitiveType()) {
            return typeNode.getType();
        } else if(typeNode.isArrayType()) {
            return resolveType(((ArrayTypeNode) typeNode).getBaseType());
        } else if(typeNode.isClassType()) {
            if(globalScope.getClassEntity(typeNode.getType().getTypeName()) == null) {
                return null;
            } else {
                return typeNode.getType();
            }
        } else {
            return null;
        }
    }

    private VariableEntity getVariableEntity (VariableDeclaration variableDeclaration) {
        VariableEntity variableEntity = new VariableEntity();
        variableEntity.setType(variableDeclaration.getType().getType());
        variableEntity.setName(variableDeclaration.getName());
        return variableEntity;
    }

    private void firstVisit(FunctionDeclaration node) {
        if(curScope.getFunction(node.getName()) != null) {
            throw new SemanticError(node.getLocation(), "Duplicate FunctionDeclaration");
        }
        if(globalScope.getClassEntity(node.getName()) != null) {
            throw new SemanticError(node.getLocation(), "The name of function conflicts with a class");
        }
        FunctionEntity functionEntity = new FunctionEntity();
        functionEntity.setName(node.getName());
        functionEntity.setReturnType(node.getReturnType().getType());
        List<VariableEntity> parameters = new LinkedList<VariableEntity>();
        for(VariableDeclaration variableDeclaration : node.getParameters()) {
            parameters.add(getVariableEntity(variableDeclaration));
        }
        functionEntity.setParameters(parameters);
        node.setFunctionEntity(functionEntity);

        curScope.putFunction(functionEntity.getName(), functionEntity);

    }

    private void firstVisit(ClassDeclaration node) {
        if(globalScope.getClassEntity(node.getName()) != null) {
            throw new SemanticError(node.getLocation(), "Duplicate ClassDeclaration");
        }
        if(globalScope.getScope().getFunction(node.getName()) != null) {
            throw new SemanticError(node.getLocation(), "The name of class conflicts with a function");
        }

        ClassEntity classEntity = new ClassEntity();
        classEntity.setName(node.getName());
        classEntity.setScope(new Scope(globalScope.getScope()));
        node.setClassEntity(classEntity);

        enterScope(classEntity.getScope());
        if(node.getConstructor() != null) {
            firstVisit(node.getConstructor());
        }
        for(FunctionDeclaration functionDeclaration : node.getMethods()) {
            firstVisit(functionDeclaration);
        }
        for(VariableDeclaration variableDeclaration : node.getFields()) {
            visit(variableDeclaration);
        }

        exitScope();

        globalScope.putClassEntity(classEntity.getName(), classEntity);

    }

    @Override
    public void visit(Program node) {
        for(ClassDeclaration classDeclaration : node.getClasses()) {
            firstVisit(classDeclaration);
        }
        for(FunctionDeclaration functionDeclaration : node.getFunctions()) {
            firstVisit(functionDeclaration);
        }
        for(VariableDeclaration variableDeclaration : node.getVariables()) {
            visit(variableDeclaration);
        }
        for(ClassDeclaration classDeclaration : node.getClasses()) {
            visit(classDeclaration);
        }
        for(FunctionDeclaration functionDeclaration : node.getFunctions()) {
            visit(functionDeclaration);
        }
    }

    @Override
    public void visit(Declaration node) {

    }

    @Override
    public void visit(FunctionDeclaration node) {
        FunctionEntity functionEntity = curScope.getFunction(node.getName());
        //curFunctionEntity = functionEntity;
        functionEntity.setScope(new Scope(curScope));
        enterScope(functionEntity.getScope());
        for(VariableDeclaration variableDeclaration : node.getParameters()) {
            visit(variableDeclaration);
        }
        for(Statement statement : node.getBody()) {
            statement.accept(this);
        }
        exitScope();
        //curFunctionEntity = null;
    }

    @Override
    public void visit(ClassDeclaration node) {
        ClassEntity classEntity = globalScope.getClassEntity(node.getName());
        enterScope(classEntity.getScope());
        if(node.getConstructor() != null) {
            visit(node.getConstructor());
        }
        for(FunctionDeclaration functionDeclaration : node.getMethods()) {
            visit(functionDeclaration);
        }
        exitScope();
    }

    @Override
    public void visit(VariableDeclaration node) {
        if(node.getInit() != null) {
            node.getInit().accept(this);
        }
        VariableEntity variableEntity = new VariableEntity();
        variableEntity.setType(resolveType(node.getType()));
        variableEntity.setName(node.getName());

        if(curScope == globalScope.getScope()) {
            variableEntity.setGlobal(true);
        }

        if(curScope.getVariable(node.getName()) != null) {
            throw new SemanticError(node.getLocation(), "Duplicate VariableDeclaration");
        } else {
            curScope.putVariable(variableEntity.getName(), variableEntity);
        }
    }

    @Override
    public void visit(TypeNode node) {

    }

    @Override
    public void visit(ArrayTypeNode node) {

    }

    @Override
    public void visit(Statement node) {

    }

    @Override
    public void visit(IfStatement node) {
        node.getCondition().accept(this);
        node.getThenStatement().accept(this);
        if(node.getElseStatement() != null) {
            node.getElseStatement().accept(this);
        }
    }

    @Override
    public void visit(WhileStatement node) {
        node.getCondition().accept(this);
        node.getBody().accept(this);
    }

    @Override
    public void visit(ForStatement node) {
        if(node.getInit() != null) {
            node.getInit().accept(this);
        }
        if(node.getCondition() != null) {
            node.getCondition().accept(this);
        }
        if(node.getUpdate() != null) {
            node.getUpdate().accept(this);
        }
        node.getBody().accept(this);
    }

    @Override
    public void visit(BreakStatement node) {

    }

    @Override
    public void visit(ContinueStatement node) {

    }

    @Override
    public void visit(ReturnStatement node) {
        if(node.getRet() != null) {
            node.getRet().accept(this);
        }
    }

    @Override
    public void visit(ExprStatement node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(VarDeclStatement node) {
        node.getDeclaration().accept(this);
    }

    @Override
    public void visit(BlockStatement node) {
        Scope blockScope = new Scope(curScope);
        enterScope(blockScope);
        for(Statement statement : node.getStatements()) {
            statement.accept(this);
        }
        exitScope();
    }

    @Override
    public void visit(Expression node) {

    }

    @Override
    public void visit(ThisExpression node) {

    }

    @Override
    public void visit(NullLiteral node) {
        node.setType(new Type("null"));
    }

    @Override
    public void visit(BoolLiteral node) {
        node.setType(new Type("bool"));
    }

    @Override
    public void visit(IntLiteral node) {
        node.setType(new Type("int"));
    }

    @Override
    public void visit(StringLiteral node) {
        node.setType(new Type("string"));
    }

    @Override
    public void visit(Identifier node) {
        if(curScope.getRecursiveVariable(node.getName()) == null) {
            throw new SemanticError(node.getLocation(), "Cannot find identifier");
        } else {
            node.setVariableEntity(curScope.getRecursiveVariable(node.getName()));
        }
        node.setType(node.getVariableEntity().getType());
    }

    @Override
    public void visit(MemberExpression node) {
        node.getExpr().accept(this);
        if(node.getMember() != null) {
            visit(node.getMember());
            node.setType(node.getMember().getType());
        } else {
            visit(node.getFuncCall());
            node.setType(node.getFuncCall().getType());
        }
    }

    @Override
    public void visit(ArrayExpression node) {
        node.getArr().accept(this);
        node.getIdx().accept(this);
        node.setType(node.getArr().getType());
    }

    @Override
    public void visit(FuncCallExpression node) {
        if(curScope.getRecursiveFunction(node.getName().getName()) == null) {
            throw new SemanticError(node.getLocation(), "Cannot find function");
        } else {
            node.setFunctionEntity(curScope.getRecursiveFunction(node.getName().getName()));
        }
        for(Expression expression : node.getArguments()) {
            expression.accept(this);
        }
        node.setType(node.getFunctionEntity().getReturnType());

    }

    @Override
    public void visit(NewExpression node) {
        if(node.getDimensions() != null) {
            for(Expression expression : node.getDimensions()) {
                expression.accept(this);
            }
        }
        node.setType(resolveType(node.getTypeNode()));
    }

    @Override
    public void visit(SuffixExpression node) {
        node.getExpr().accept(this);
        node.setType(node.getExpr().getType());
    }

    @Override
    public void visit(PrefixExpression node) {
        node.getExpr().accept(this);
        node.setType(node.getExpr().getType());
    }

    @Override
    public void visit(BinaryExpression node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
        switch (node.getOp()) {
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "==":
            case "!=":
                node.setType(new Type("bool"));
                break;
            default:
                node.setType(node.getLhs().getType());
                break;
        }
    }

    @Override
    public void visit(AssignExpression node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
        node.setType(new Type("void"));
    }

}
