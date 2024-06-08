package ast;
import java.util.List;

public class AST {

    public abstract static class TreeNode {}

    public abstract static class Expression extends TreeNode {}

    public abstract static class Statement extends TreeNode {}

    public static class Program extends TreeNode {
        public List<Statement> statements;
        public Program(List<Statement> statements) {
            this.statements = statements;
        }

        public List<Statement> getStatements() {
            return this.statements;
        }
    }
    /*
     * Represents an assignment statement.
     */
    public static class Assignment extends Statement {
        public List<Variable> targets;
        public Expression value;
        public Assignment(List<Variable> targets, Expression value) {
            this.targets = targets;
            this.value = value;
        }

        public List<Variable> getTargets() {
            return this.targets;
        }
        
        public Expression getValue() {
            return this.value;
        }
    }

    /*
     * Represents an expression statement.
     */ 

    public static class ExprStatement extends Statement{
        public Expression expression;
        public ExprStatement(Expression expression){
            this.expression = expression;
        }
        public TreeNode getExpr() {
            return this.expression;
        }
    }
    /*
     * Represents a conditional statement.
     */
    public static class Conditional extends Statement{
        public Expression condition;
        public Body body;

        public Conditional(Expression condition, Body body){
            this.condition = condition;
            this.body = body;
        }

        public TreeNode getCondition() {
            return this.condition;
        }

        public TreeNode getBody() {
            
            return this.body;
        }
    }

    /*
     * Represents a body of a compound statement.
     */
    public static class Body extends TreeNode{
        public List<Statement> statements;
        public Body(List<Statement> statements){
            this.statements = statements;
        }
        public List<Statement> getStatements() {
            // TODO Auto-generated method stub
            return this.statements;
        }
        
    }

    /*
     * Represents a unary operator.
     */
    public static class UnaryOp extends Expression {
        public String op;
        public Expression value;
        public UnaryOp(String op, Expression expr) {
            this.op = op;
            this.value = expr;
        }

        public String getOp() {
            return this.op;
        }

        public Expression getValue() {
            return this.value;
        }

    }
    
    /*
     * Represents a binary operator.
     */
    public static class BinaryOp extends Expression {
        public String op;
        public Expression left;
        public Expression right;

        public BinaryOp(String op, Expression left, Expression right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }
        
        public String getOp() {
            return this.op;
        }

        public Expression getLeft() {
            return this.left;
        }

        public Expression getRight() {
            return this.right;
        }
    }

    /*
     * Represents a variable.
     */
    public static class Variable extends Expression {
        public Object name;
        public Variable(Object object) {
            this.name = object;
        }

        public Object getName() {
            return this.name;
        }
    }

    
    public static class Constant extends Expression {
        public Object value;
        public Constant(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return this.value;
        }
    }
    
}
