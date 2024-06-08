package ByteUtils;

public enum BytecodeType {
    BINARYOP,
    UNARYOP,
    PUSH,
    POP,
    SAVE,
    LOAD,
    COPY,
    POP_JUMP_IF_FALSE;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "." + this.name();
    }

    public String fromString() {
        return (""+ this.name());
    }
}