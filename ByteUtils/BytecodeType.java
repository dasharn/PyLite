package ByteUtils;

/**
 * Enum representing different types of bytecodes that can be used in a virtual machine or interpreter.
 */
public enum BytecodeType {
    BINARYOP, // Represents a binary operation (e.g., addition, subtraction)
    UNARYOP, // Represents a unary operation (e.g., negation)
    PUSH, // Represents pushing a value onto the stack
    POP, // Represents popping a value from the stack
    SAVE, // Represents saving a value to a variable
    LOAD, // Represents loading a value from a variable
    COPY, // Represents copying a value
    POP_JUMP_IF_FALSE; // Represents popping the top of the stack and jumping if it is false

    /**
     * Returns a string representation of the bytecode type.
     * 
     * @return A string representation of the bytecode type.
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "." + this.name();
    }

    /**
     * Returns the name of the bytecode type as a string.
     * 
     * This method seems to be intended to convert a string representation back to a BytecodeType,
     * but currently, it just returns the name of the enum constant. This might need correction or further implementation.
     * 
     * @return The name of the bytecode type.
     */
    public String fromString() {
        return (""+ this.name());
    }
}