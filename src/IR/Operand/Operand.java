package IR.Operand;

import IR.IRVistor;

public abstract class Operand {
    public abstract void accept(IRVistor vistor);
}
