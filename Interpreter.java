import java.util.*;
import java.util.function.BiFunction;

import ByteUtils.Bytecode;

public class Interpreter {
    private static final Map<String, BiFunction<Integer, Integer, Integer>> BINOPS_TO_OPERATOR = new HashMap<>();
    static {
        BINOPS_TO_OPERATOR.put("**", (a, b) -> (int) Math.pow(a, b));
        BINOPS_TO_OPERATOR.put("%", (a, b) -> a % b);
        BINOPS_TO_OPERATOR.put("/", (a, b) -> a / b);
        BINOPS_TO_OPERATOR.put("*", (a, b) -> a * b);
        BINOPS_TO_OPERATOR.put("+", (a, b) -> a + b);
        BINOPS_TO_OPERATOR.put("-", (a, b) -> a - b);
    }

    public static class Stack {
        private List<Object> stack;

        public Stack() {
            this.stack = new ArrayList<>();
        }

        public void push(Object object) {
            this.stack.add(object);
        }

        public Object pop() {
            return this.stack.remove(this.stack.size() - 1);
        }

        public Object peek() {
            return this.stack.get(this.stack.size() - 1);
        }

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

    public Interpreter(List<Bytecode> bytecode) {
        this.stack = new Stack();
        this.scope = new HashMap<>();
        this.bytecode = bytecode;
        this.ptr = 0;
        this.lastValuePopped = null;
    }

    public void interpret() {
        while (this.ptr < this.bytecode.size()) {
            Bytecode bc = this.bytecode.get(this.ptr);
            String bcName = bc.getType().fromString();
            try {
                //System.out.println(""+bcName);
                String methodName = "interpret" + bcName.substring(0, 1).toUpperCase() + bcName.substring(1).toLowerCase();
                //System.out.println(methodName);
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

    private void interpretPush(Bytecode bc) {
        this.stack.push(bc.getValue());
        this.ptr += 1;
    }

    private void interpretPop(Bytecode bc) {
        this.lastValuePopped = this.stack.pop();
        this.ptr += 1;
    }

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

    private void interpretSave(Bytecode bc) {
        this.scope.put(bc.getValue(), this.stack.pop());
        this.ptr += 1;
    }

    private void interpretLoad(Bytecode bc) {
        this.stack.push(this.scope.get(bc.getValue()));
        this.ptr += 1;
    }

    private void interpretCopy(Bytecode bc) {
        this.stack.push(this.stack.peek());
        this.ptr += 1;
    }

    private void interpretPopJumpIfFalse(Bytecode bc) {
        Object value = this.stack.pop();
        if (value == null) {
            this.ptr += (Integer) bc.getValue();
        } else {
            this.ptr += 1; // Default behaviour is to move to the next bytecode.
        }
    }


}


