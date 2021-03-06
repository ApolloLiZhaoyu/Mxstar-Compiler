package FrontEnd;

import IR.*;
import IR.Instruction.*;
import IR.Operand.*;

import java.util.ArrayList;
import java.util.HashMap;

public class IRPrinter implements IRVistor {
    private StringBuilder program;
    private HashMap<BasicBlock, String> bbNames;
    private HashMap<StaticData, String> sdNames;
    private HashMap<StackSlot,String> ssNames;
    private HashMap<VirtualRegister,String> varNames;

    private BasicBlock nextBB;

    private boolean inLeaInst;

    private int bbIndex;
    private int sdIndex;
    private int varIndex;
    private int ssIndex;

    public IRPrinter() {
        program = new StringBuilder();
        bbNames = new HashMap<>();
        sdNames = new HashMap<>();
        ssNames = new HashMap<>();
        varNames = new HashMap<>();
        bbIndex = 0;
        sdIndex = 0;
        varIndex = 0;
        ssIndex = 0;
        inLeaInst = false;
    }

    public void print() {
        System.err.print(program.toString());
    }

    private void addLine(String line) {
        program.append(line + "\n");
    }

    private void add(String str) {
        program.append(str);
    }

    private String getBasicBlockName(BasicBlock bb) {
        if(!bbNames.containsKey(bb)) {
            bbNames.put(bb, "_block_" + (bbIndex++));
        }
        return bbNames.get(bb);
    }

    private String getStaticDataName(StaticData sd) {
        if(!sdNames.containsKey(sd)) {
            sdNames.put(sd, "_global_" + (sdIndex++));
        }
        return sdNames.get(sd);
    }

    private String getVirtualRegisterName(VirtualRegister virtualRegister) {
        if(!varNames.containsKey(virtualRegister)) {
            virtualRegister.setName("v" + (varIndex++));
            varNames.put(virtualRegister, virtualRegister.getName());
        }

        return varNames.get(virtualRegister);
    }
    private String getStackSlotName(StackSlot ss) {
        if(!ssNames.containsKey(ss))
            ssNames.put(ss, "stack[" + (ssIndex++) + "]");
        return ssNames.get(ss);
    }

    @Override
    public void visit(IRProgram node) {
        for(Function function : node.getFunctions().values()) {
            if(function.getType() == Function.FuncType.UserDefined) {
                System.out.println();
                function.accept(this);
            }
        }
        for(StaticVariable var : node.getStaticVariables()) {
            addLine(getStaticDataName(var));
        }
        for(StaticString str : node.getStaticStrings()) {
            addLine(getStaticDataName(str));
        }
    }

    @Override
    public void visit(Function node) {
        add("define " + node.getName() + " ");
        add("(");
        boolean first = true;
        for(VirtualRegister vr : node.getParameters()) {
            if(first)
                first = false;
            else
                add(", ");
            vr.accept(this);
        }
        add(") {\n");
        ArrayList<BasicBlock> reversePostOrder = new ArrayList<>(node.getReversePostOrder());
        for(int i = 0; i < reversePostOrder.size(); i++) {
            BasicBlock bb = reversePostOrder.get(i);
            nextBB = (i + 1 == reversePostOrder.size()) ? null : reversePostOrder.get(i + 1);
            bb.accept(this);
        }
        add("} \n");
    }

    @Override
    public void visit(BasicBlock node) {
        addLine("\t" + getBasicBlockName(node) + ":");
        for(Instruction inst = node.getHead(); inst != null; inst = inst.getNext()) {
            inst.accept(this);
        }
    }

    @Override
    public void visit(Jump node) {
        if(nextBB != node.getTargetBB())
            addLine("\tjmp " + getBasicBlockName(node.getTargetBB()));
    }

    @Override
    public void visit(CJump node) {
        String op = null;
        switch(node.getOp()) {
            case EQ:
                op = "je";
                break;
            case LT:
                op = "jl";
                break;
            case GT:
                op = "jg";
                break;
            case LE:
                op = "jle";
                break;
            case GE:
                op = "jge";
                break;
            case NE:
                op = "jne";
                break;
        }
        add("\t" + op + " ");
        node.getLhs().accept(this);
        add(", ");
        node.getRhs().accept(this);

        add(" " + getBasicBlockName(node.getThenBB()));
        add(", " + getBasicBlockName(node.getElseBB()));
        add("\n");
    }

    @Override
    public void visit(Return node) {
        addLine("\tret");
    }

    @Override
    public void visit(BinaryOperation node) {
        if(node.getOp() == BinaryOperation.BinaryOp.MUL) {
            add("\timul ");
            node.getSrc().accept(this);
            add("\n");
            return;
        }
        if(node.getOp() == BinaryOperation.BinaryOp.DIV || node.getOp() == BinaryOperation.BinaryOp.MOD) {
            add("\tidiv ");
            node.getSrc().accept(this);
            add("\n");
            return;
        }
        String op = null;
        switch(node.getOp()) {
            case OR:
                op = "or";
                break;
            case ADD:
                op = "add";
                break;
            case AND:
                op = "and";
                break;
            case SAL:
                op = "sal";
                break;
            case SAR:
                op = "sar";
                break;
            case SUB:
                op = "sub";
                break;
            case XOR:
                op = "xor";
                break;
        }
        if(node.getOp() == BinaryOperation.BinaryOp.SAL || node.getOp() == BinaryOperation.BinaryOp.SAR) {
            add("\t" + op + " ");
            node.getDst().accept(this);
            add(", cl\n");
            return;
        }
        add("\t" + op + " ");
        node.getDst().accept(this);
        add(", ");
        node.getSrc().accept(this);
        add("\n");
    }

    @Override
    public void visit(UnaryOperation node) {
        String op = null;
        switch(node.getOp()) {
            case DEC:
                op = "dec";
                break;
            case INC:
                op = "inc";
                break;
            case NEG:
                op = "neg";
                break;
            case NOT:
                op = "not";
                break;
        }
        add("\t" + op + " ");
        node.getDst().accept(this);
        add("\n");
    }

    @Override
    public void visit(Compare node) {
        String op = null;
        switch(node.getOp()) {
            case EQ:
                op = "je";
                break;
            case LT:
                op = "jl";
                break;
            case GT:
                op = "jg";
                break;
            case LE:
                op = "jle";
                break;
            case GE:
                op = "jge";
                break;
            case NE:
                op = "jne";
                break;
        }
        add("\t" + op + " ");
        node.getDst().accept(this);
        add(" ");
        node.getLhs().accept(this);
        add(", ");
        node.getRhs().accept(this);
    }

    @Override
    public void visit(Move node) {
        if(node.getSrc() == node.getDst())
            return;
        add("\tmov ");
        node.getDst().accept(this);
        add(", ");
        node.getSrc().accept(this);
        add("\n");
    }

    @Override
    public void visit(Lea node) {
        inLeaInst = true;
        add("\tlea ");
        node.getDst().accept(this);
        add(", ");
        node.getSrc().accept(this);
        add("\n");
        inLeaInst = false;
    }

    @Override
    public void visit(Call node) {
        add("\tcall ");
        node.getDst().accept(this);
        add(" = ");
        add(node.getFunc().getName());
        for(Operand operand : node.getArgs()) {
            add(", ");
            operand.accept(this);
        }
        add("\n");
    }

    @Override
    public void visit(Push node) {
        add("\tpush ");
        node.getSrc().accept(this);
        add("\n");
    }

    @Override
    public void visit(Pop node) {
        add("\tpop ");
        node.getDst().accept(this);
        add("\n");
    }

    @Override
    public void visit(Leave node) {
        addLine("\tleave");
    }

    @Override
    public void visit(Cdq node) {
        addLine("\tcdq");
    }

    @Override
    public void visit(Memory node) {
        boolean occur = false;
        if(!inLeaInst)
            add("qword ");
        add("[");
        if(node.getBase() != null) {
            node.getBase().accept(this);
            occur = true;
        }
        if(node.getIndex() != null) {
            if(occur)
                add(" + ");
            node.getIndex().accept(this);
            if(node.getScale() != 1)
                add(" * " + (node.getScale()));

            occur = true;
        }
        if(node.getOffset() != null) {
            Constant constant = node.getOffset();
            if(constant instanceof StaticData) {
                if(occur)
                    add(" + ");
                constant.accept(this);
            } else if(constant instanceof IntImmediate) {
                int value = ((IntImmediate) constant).getValue();
                if(occur) {
                    if(value > 0)
                        add(" + " + (value));
                    else if(value < 0)
                        add(" - " + (-value));
                } else {
                    add(String.valueOf(value));
                }
            }
        }
        add("]");
    }

    @Override
    public void visit(StackSlot node) {
        if(node.getBase() != null || node.getIndex() != null || node.getOffset() != null) {
            visit((Memory) node);
        } else {
            add(getStackSlotName(node));
        }
    }

    @Override
    public void visit(VirtualRegister node) {
        if(node.getAllocatedPhysicalRegister() != null) {
            visit(node.getAllocatedPhysicalRegister());
            varNames.put(node, node.getAllocatedPhysicalRegister().getName());
        } else {
            add(getVirtualRegisterName(node));
        }
    }

    @Override
    public void visit(PhysicalRegister node) {
        add(node.getName());
    }

    @Override
    public void visit(IntImmediate node) {
        add(String.valueOf(node.getValue()));
    }

    @Override
    public void visit(StaticVariable node) {
        add(getStaticDataName(node));
    }

    @Override
    public void visit(StaticString node) {
        add(getStaticDataName(node));
    }
}
