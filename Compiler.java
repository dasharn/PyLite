import ast.AST.*;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import ByteUtils.Bytecode;
import ByteUtils.BytecodeType;


interface BytecodeGenerator extends Iterator<Bytecode> {}


class Compiler {
    private final TreeNode tree;

    public Compiler(Object tree2) {
        this.tree = (TreeNode) tree2;
    }

    public BytecodeGenerator compile() {
        return new BytecodeGeneratorImpl(this._compile(tree));
    }

    private Iterable<Bytecode> _compile(TreeNode tree) {
        String nodeName = tree.getClass().getSimpleName();
        try {
            String capitalizedNodeName = Character.toUpperCase(nodeName.charAt(0)) + nodeName.substring(1);
            //System.out.println("Running compile" + capitalizedNodeName);
            return (Iterable<Bytecode>) this.getClass().getDeclaredMethod("compile" + capitalizedNodeName, tree.getClass()).invoke(this, tree);
        } catch (Exception e) {
            String capitalizedNodeName = Character.toUpperCase(nodeName.charAt(0)) + nodeName.substring(1);
            System.out.println("compile" + capitalizedNodeName);
            throw new RuntimeException("Can't compile " + nodeName, e);
        }
    }

    private Iterable<Bytecode> compileProgram(Program program) {
        //System.out.println("In compileProgram");
        List<Bytecode> bytecodes = new ArrayList<>();
        for (TreeNode statement : program.getStatements()) {
            //System.out.println("In compileProgram for loop, executing _compile on " + statement.getClass().getSimpleName());
            for (Bytecode bc : _compile(statement)) {
                bytecodes.add(bc);
            }
        }
        return bytecodes;
    }

    private Iterable<Bytecode> compileConditional(Conditional conditional) {
        List<Bytecode> bytecodes = new ArrayList<>();
        for (Bytecode bc : _compile(conditional.getCondition())) {
            bytecodes.add(bc);
        }
        List<Bytecode> bodyBytecode = new ArrayList<>();
        for (Bytecode bc : _compile(conditional.getBody())) {
            bodyBytecode.add(bc);
        }
        bytecodes.add(new Bytecode(BytecodeType.POP_JUMP_IF_FALSE, bodyBytecode.size() + 1));
        bytecodes.addAll(bodyBytecode);
        return bytecodes;
    }

    private Iterable<Bytecode> compileBody(Body body) {
        List<Bytecode> bytecodes = new ArrayList<>();
        for (TreeNode statement : body.getStatements()) {
            for (Bytecode bc : _compile(statement)) {
                bytecodes.add(bc);
            }
        }
        return bytecodes;
    }

    private Iterable<Bytecode> compileAssignemnt(Assignment assignment) {
        List<Bytecode> bytecodes = new ArrayList<>();
        for (Bytecode bc : _compile(assignment.getValue())) {
            bytecodes.add(bc);
        }
        for (int i = 0; i < assignment.getTargets().size() - 1; i++) {
            bytecodes.add(new Bytecode(BytecodeType.COPY));
            bytecodes.add(new Bytecode(BytecodeType.SAVE, assignment.getTargets().get(i).getName()));
        }
        bytecodes.add(new Bytecode(BytecodeType.SAVE, assignment.getTargets().get(assignment.getTargets().size() - 1).getName()));
        return bytecodes;
    }

    private Iterable<Bytecode> compileExprStatement(ExprStatement expression) {
        List<Bytecode> bytecodes = new ArrayList<>();
        for (Bytecode bc : _compile(expression.getExpr())) {
            bytecodes.add(bc);
        }
        bytecodes.add(new Bytecode(BytecodeType.POP));
        return bytecodes;
    }

    private Iterable<Bytecode> compileUnaryOp(UnaryOp tree) {
        List<Bytecode> bytecodes = new ArrayList<>();
        for (Bytecode bc : _compile(tree.getValue())) {
            bytecodes.add(bc);
        }
        bytecodes.add(new Bytecode(BytecodeType.UNARYOP, tree.getOp()));
        return bytecodes;
    }

    private Iterable<Bytecode> compileBinaryOp(BinaryOp tree) {
        List<Bytecode> bytecodes = new ArrayList<>();
        for (Bytecode bc : _compile(tree.getLeft())) {
            bytecodes.add(bc);
        }
        for (Bytecode bc : _compile(tree.getRight())) {
            bytecodes.add(bc);
        }
        bytecodes.add(new Bytecode(BytecodeType.BINARYOP, tree.getOp()));
        return bytecodes;
    }

    private Iterable<Bytecode> compileConstant(Constant constant) {
        List<Bytecode> bytecodes = new ArrayList<>();
        bytecodes.add(new Bytecode(BytecodeType.PUSH, constant.getValue()));
        return bytecodes;
    }

    private Iterable<Bytecode> compileVariable(Variable var) {
        List<Bytecode> bytecodes = new ArrayList<>();
        bytecodes.add(new Bytecode(BytecodeType.LOAD, var.getName()));
        return bytecodes;
    }

    private class BytecodeGeneratorImpl implements BytecodeGenerator {
        private final Iterator<Bytecode> iterator;

        public BytecodeGeneratorImpl(Iterable<Bytecode> bytecodes) {
            this.iterator = bytecodes.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Bytecode next() {
            if (!hasNext()) {
                throw new RuntimeException("No more bytecode");
            }
            return iterator.next();
        }

        @Override
        public void forEachRemaining(Consumer<? super Bytecode> action) {
            iterator.forEachRemaining(action);
        }
    }

    
}
