package IR;

import IR.Instruction.*;
import IR.Operand.*;

public interface IRVistor {
    void visit(VirtualRegister node);
    void visit(PhysicalRegister node);
    void visit(IntImmediate node);
    void visit(StringLiteral node);

    void visit(Jump node);
    void visit(CJump node);
    void visit(Return node);
    void visit(Allocate node);
    void visit(Move node);
    void visit(Phi node);
    void visit(Load node);
    void visit(Store node);

    void visit(Call node);
    void visit(BinaryOperation node);
    void visit(UnaryOperation node);
}
