package BackEnd;

import IR.BasicBlock;
import IR.Function;
import IR.IRProgram;
import IR.Instruction.*;
import IR.Operand.IntImmediate;
import IR.Operand.PhysicalRegister;
import IR.Operand.StackSlot;
import Utility.Config;

import java.util.HashSet;
import java.util.LinkedList;

import static IR.RegisterSet.*;

public class StackBuilder {
    private IRProgram program;
    private LinkedList<StackSlot> parameters;
    private LinkedList<StackSlot> temporaries;

    public StackBuilder(IRProgram program) {
        this.program = program;
        this.parameters = new LinkedList<>();
        this.temporaries = new LinkedList<>();
    }

    public void run() {
        for(Function function : program.getFunctions().values()) {
            if(function.getType() == Function.FuncType.UserDefined)
                buildStack(function);
        }
    }

    private void buildStack(Function function) {
        parameters.clear();
        temporaries.clear();
        if(function.getParameters().size() > 6) {
            for(int i = 6; i < function.getParameters().size(); i++) {
                parameters.add((StackSlot) function.getParameters().get(i).getSpillSpace());
            }
        }

        HashSet<StackSlot> slotsSet = new HashSet<>();
        for(BasicBlock bb : function.getBasicBlocks()) {
            for(Instruction inst = bb.getHead(); inst != null; inst = inst.getNext()) {
                LinkedList<StackSlot> slots = inst.getStackSlots();
                for(StackSlot ss : slots) {
                    if(!parameters.contains(ss)) {
                        slotsSet.add(ss);
                    }
                }
            }
        }
        temporaries.addAll(slotsSet);

        for(int i = 0; i < parameters.size(); i++) {
            StackSlot ss = parameters.get(i);
            ss.setBase(rbp);
            ss.setOffset(new IntImmediate(16 + 8 * i));
        }
        for(int i = 0; i < temporaries.size(); i++) {
            StackSlot ss = temporaries.get(i);
            ss.setBase(rbp);
            ss.setOffset(new IntImmediate(-8 - 8 * i));
        }

        HashSet<PhysicalRegister> needToSave = new HashSet<>(function.getUsedPhysicalRegisters());
        needToSave.retainAll(calleeSave);

        int bytes = Config.REG_SIZE * (parameters.size() + temporaries.size());
        bytes = (bytes + 8) / 16 * 16;
        if(needToSave.size() % 2 == 1) {
            bytes += 8;
        }

        BasicBlock headBB = function.getHeadBB();
        headBB.addPrevInst(new BinaryOperation(headBB, rsp, BinaryOperation.BinaryOp.SUB, new IntImmediate(bytes)));
        Instruction headInst = headBB.getHead();
        headInst.prepend(new Push(headBB, rbp));
        headInst.prepend(new Move(headBB, rbp, rsp));

        if(bytes == 0) {
            headInst = headInst.getPrev();
            headInst.getNext().remove();
        }

        for(PhysicalRegister pr : needToSave) {
            headInst.append(new Push(headBB, pr));
        }

        BasicBlock tailBB = function.getTailBB();
        Instruction tailInst = tailBB.getTail();
        for(PhysicalRegister pr : needToSave) {
            tailInst.prepend(new Pop(tailBB, pr));
        }
        tailInst.prepend(new Leave(tailBB));
    }
}
