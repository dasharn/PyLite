import java.util.*;
import java.util.function.BiFunction;

import ByteUtils.Bytecode;

public class Interpreter {
    /**
     * A map that associates binary operation strings with their corresponding Java implementations.
     * Each binary operation is represented as a string (e.g., "+", "-", "*", "/", "%", "**") and is mapped to a BiFunction
     * that takes two Integer arguments and returns an Integer result. This map is used to interpret and execute the binary operations.
     */
    private static final Map<String, BiFunction<Integer, Integer, Integer>> BINOPS_TO_OPERATOR = new HashMap<>();
    static {
        BINOPS_TO_OPERATOR.put("**", (a, b) -> (int) Math.pow(a, b));
        BINOPS_TO_OPERATOR.put("%", (a, b) -> a % b);
        BINOPS_TO_OPERATOR.put("/", (a, b) -> a / b);
        BINOPS_TO_OPERATOR.put("*", (a, b) -> a * b);
        BINOPS_TO_OPERATOR.put("+", (a, b) -> a + b);
        BINOPS_TO_OPERATOR.put("-", (a, b) -> a - b);
    }

    /**
     * A simple stack implementation that uses a List<Object> to store elements.
     * This class provides basic stack operations: push, pop, and peek. It is used to manage the runtime stack of the interpreter.
     */
    public static class Stack {
        private List<Object> stack; // The underlying list that stores the stack elements

        /**
         * Constructs an empty Stack.
         */
        public Stack() {
            this.stack = new ArrayList<>();
        }

        /**
         * Pushes an object onto the top of the stack.
         * 
         * @param object The object to be pushed onto the stack.
         */
        public void push(Object object) {
            this.stack.add(object);
        }

        /**
         * Removes and returns the object at the top of the stack.
         * 
         * @return The object at the top of the stack.
         */
        public Object pop() {
            return this.stack.remove(this.stack.size() - 1);
        }

        /**
         * Returns (but does not remove) the object at the top of the stack.
         * 
         * @return The object at the top of the stack.
         */
        public Object peek() {
            return this.stack.get(this.stack.size() - 1);
        }

        /**
         * Returns a string representation of the stack.
         * 
         * @return A string representation of the stack, showing all elements.
         */
        @Override
        public String toString() {
            return "Stack(" + this.stack + ")";
        }
    }

    private Stack stack;
    private Map<Object, Object> scope;
    private List<Bytecode> bytecode;
    private int ptr;
    private Object lastValuePopped;

    /**
     * Constructs an Interpreter with a given list of Bytecodes.
     * Initializes the stack, scope, bytecode sequence, and pointer for bytecode execution.
     * 
     * @param bytecode The list of Bytecodes to be interpreted.
     */
    public Interpreter(List<Bytecode> bytecode) {
        this.stack = new Stack();
        this.scope = new HashMap<>();
        this.bytecode = bytecode;
        this.ptr = 0;
        this.lastValuePopped = null;
    }

    /**
     * Interprets the bytecode sequence provided to the interpreter.
     * This method iterates through the bytecode list, dynamically invoking the corresponding interpret method for each bytecode.
     * It handles exceptions by throwing a RuntimeException with details of the failed bytecode.
     */
    public void interpret() {
        while (this.ptr < this.bytecode.size()) {
            Bytecode bc = this.bytecode.get(this.ptr);
            String bcName = bc.getType().fromString();
            try {
                String methodName = "interpret" + bcName.substring(0, 1).toUpperCase() + bcName.substring(1).toLowerCase();
                java.lang.reflect.Method method = this.getClass().getDeclaredMethod(methodName, Bytecode.class);
                method.invoke(this, bc);
            } catch (Exception e) {
                throw new RuntimeException("Can't interpret " + bcName + ".", e);
            }
        }

        System.out.println(this.scope);
        System.out.println("Result: "+ this.lastValuePopped);
        System.out.println("Program Fully Interpreted.");
    }

    /**
     * Interprets a PUSH bytecode, pushing its value onto the stack.
     * Increments the bytecode pointer after execution.
     * 
     * @param bc The PUSH bytecode to interpret.
     */
    private void interpretPush(Bytecode bc) {
        this.stack.push(bc.getValue());
        this.ptr += 1;
    }

    /**
     * Interprets a POP bytecode, popping the top value from the stack and storing it.
     * Increments the bytecode pointer after execution.
     * 
     * @param bc The POP bytecode to interpret.
     */
    private void interpretPop(Bytecode bc) {
        this.lastValuePopped = this.stack.pop();
        this.ptr += 1;
    }

    /**
     * Interprets a BINARYOP bytecode, performing the specified binary operation on the two topmost stack values.
     * Pushes the result back onto the stack. Increments the bytecode pointer after execution.
     * 
     * @param bc The BINARYOP bytecode to interpret.
     */
    private void interpretBinaryop(Bytecode bc) {
        Object right = this.stack.pop();
        Object left = this.stack.pop();
        BiFunction<Integer, Integer, Integer> op = BINOPS_TO_OPERATOR.get(bc.getValue());
        if (op != null) {
            int result = op.apply((Integer) left, (Integer) right);
            this.stack.push(result);
        } else {
            throw new RuntimeException("Unknown operator " + bc.getValue() + ".");
        }
        this.ptr += 1;
    }

    /**
     * Interprets a UNARYOP bytecode, performing the specified unary operation on the top stack value.
     * Supports unary operations like negation (-) and logical NOT (not). Updates the stack with the result.
     * Increments the bytecode pointer after execution.
     * 
     * @param bc The UNARYOP bytecode to interpret.
     */
    private void interpretUnaryop(Bytecode bc) {
        Object result = this.stack.pop();
        switch ((String) bc.getValue()) {
            case "+":
                break;
            case "-":
                result = -(int)result;
                break;
            case "not":
                result = !(Boolean)result;
                break;
            default:
                throw new RuntimeException("Unknown operator " + bc.getValue() + ".");
        }
        this.stack.push(result);
        this.ptr += 1;
    }

    /**
     * Interprets a SAVE bytecode, storing the top stack value into the scope map with the given identifier.
     * Removes the value from the stack. Increments the bytecode pointer after execution.
     * 
     * @param bc The SAVE bytecode to interpret.
     */
    private void interpretSave(Bytecode bc) {
        this.scope.put(bc.getValue(), this.stack.pop());
        this.ptr += 1;
    }

    /**
     * Interprets a LOAD bytecode, pushing the value associated with the given identifier from the scope map onto the stack.
     * Increments the bytecode pointer after execution.
     * 
     * @param bc The LOAD bytecode to interpret.
     */
    private void interpretLoad(Bytecode bc) {
        this.stack.push(this.scope.get(bc.getValue()));
        this.ptr += 1;
    }

    /**
     * Interprets a COPY bytecode, duplicating the top value of the stack.
     * Increments the bytecode pointer after execution.
     * 
     * @param bc The COPY bytecode to interpret.
     */
    private void interpretCopy(Bytecode bc) {
        this.stack.push(this.stack.peek());
        this.ptr += 1;
    }

    /**
     * Interprets a POPJUMPIFFALSE bytecode, popping the top stack value and jumping to the bytecode index specified if the value is false.
     * If the value is true or non-null, moves to the next bytecode. Adjusts the bytecode pointer based on the condition.
     * 
     * @param bc The POPJUMPIFFALSE bytecode to interpret.
     */
    private void interpretPopJumpIfFalse(Bytecode bc) {
        Object value = this.stack.pop();
        if (value == null) {
            this.ptr += (Integer) bc.getValue();
        } else {
            this.ptr += 1; // Default behaviour is to move to the next bytecode.
        }
    }


}


