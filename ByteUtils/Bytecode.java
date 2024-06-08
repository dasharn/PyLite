package ByteUtils;

import java.util.Objects;




public class Bytecode {
    private final BytecodeType type;
    private final Object value;

    public Bytecode(BytecodeType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Bytecode(BytecodeType type) {
        this(type, null);
    }

    public BytecodeType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value != null) {
            return this.getClass().getSimpleName() + "(" + type + ", " + value + ")";
        } else {
            return this.getClass().getSimpleName() + "(" + type + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bytecode bytecode = (Bytecode) o;
        return type == bytecode.type && Objects.equals(value, bytecode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}




