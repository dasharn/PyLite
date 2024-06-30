package ByteUtils;

import java.util.Objects;

/**
 * Represents a bytecode with a specific type and an optional value.
 */
public class Bytecode {
    private final BytecodeType type; // The type of the bytecode
    private final Object value; // The optional value associated with the bytecode

    /**
     * Constructs a new Bytecode with a specified type and value.
     * 
     * @param type The type of the bytecode.
     * @param value The value associated with the bytecode, can be null.
     */
    public Bytecode(BytecodeType type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Constructs a new Bytecode with a specified type and no value.
     * 
     * @param type The type of the bytecode.
     */
    public Bytecode(BytecodeType type) {
        this(type, null);
    }

    /**
     * Returns the type of the bytecode.
     * 
     * @return The type of the bytecode.
     */
    public BytecodeType getType() {
        return type;
    }

    /**
     * Returns the value associated with the bytecode.
     * 
     * @return The value of the bytecode, can be null.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns a string representation of the bytecode.
     * 
     * @return A string representation of the bytecode.
     */
    @Override
    public String toString() {
        if (value != null) {
            return this.getClass().getSimpleName() + "(" + type + ", " + value + ")";
        } else {
            return this.getClass().getSimpleName() + "(" + type + ")";
        }
    }

    /**
     * Checks if this bytecode is equal to another object.
     * 
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bytecode bytecode = (Bytecode) o;
        return type == bytecode.type && Objects.equals(value, bytecode.value);
    }

    /**
     * Returns a hash code value for the bytecode.
     * 
     * @return A hash code value for the bytecode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}

