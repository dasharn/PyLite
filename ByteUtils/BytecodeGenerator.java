package ByteUtils;

import java.util.Iterator;

/**
 * A generator for producing Bytecode objects from an underlying iterator.
 */
public class BytecodeGenerator implements Iterator<Bytecode> {
    private Iterator<Bytecode> iterator; // The underlying iterator providing Bytecode objects

    /**
     * Constructs a new BytecodeGenerator with a specified iterator.
     * 
     * @param iterator The iterator that this generator will use to produce Bytecode objects.
     */
    public BytecodeGenerator(Iterator<Bytecode> iterator) {
        this.iterator = iterator;
    }

    /**
     * Checks if there are more Bytecode objects to generate.
     * 
     * @return true if there are more Bytecode objects to generate, false otherwise.
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next Bytecode object from the underlying iterator.
     * 
     * @return The next Bytecode object.
     */
    @Override
    public Bytecode next() {
        return iterator.next();
    }
}