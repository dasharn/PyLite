package ByteUtils;

import java.util.Iterator;

public class BytecodeGenerator implements Iterator<Bytecode> {
    private Iterator<Bytecode> iterator;

    public BytecodeGenerator(Iterator<Bytecode> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Bytecode next() {
        return iterator.next();
    }
}