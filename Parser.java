import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ast.AST.*;

public class Parser {
    /**
     * Grammar for the program:
     * <pre>
     *     program := statement* EOF
     *
     *     statement := expr_statement | assignment | conditional
     *
     *     expr_statement := expr NEWLINE
     *     assignment := ( NAME ASSIGN )+ expr NEWLINE
     *     conditional := IF expr COLON NEWLINE body
     *
     *     body := INDENT statement+ DEDENT
     *
     *     expr := negation
     *     negation := NOT negation | computation
     *     computation := term ( (PLUS | MINUS) term )*
     *     term := unary ( (MUL | DIV | MOD) unary )*
     *     unary := PLUS unary | MINUS unary | exponentiation
     *     exponentiation := atom EXP unary | atom
     *     atom := LPAREN expr RPAREN | value
     *     value := NAME | INT | FLOAT | TRUE | FALSE
     * </pre>
     */

    public List<Token> tokens;
    public int nextTokenIndex = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;

    }

    /*abstract class TreeNode {}

    abstract class Expression extends TreeNode {}

    abstract class Statement extends TreeNode {}

    public class Program extends TreeNode {
        public List<Statement> statements;
        public Program(List<Statement> statements) {
            this.statements = statements;
        }
    }
    
    public class Assignment extends Statement {
        public List<Variable> targetsList;
        public Expression value;
        public Assignment(List<Variable> targetsList, Expression value) {
            this.targetsList = targetsList;
            this.value = value;
        }
    }

    public class ExprStatement extends Statement{
        public Expression expression;
        public ExprStatement(Expression expression){
            this.expression = expression;
        }
    }
    
    public class Conditional extends Statement{
        public Expression condition;
        public Body body;

        public Conditional(Expression condition, Body body){
            this.condition = condition;
            this.body = body;
        }
    }
   
    public class Body extends TreeNode{
        public List<Statement> statements;
        public Body(List<Statement> statements){
            this.statements = statements;
        }
        
    }
    
    public class UnaryOp extends Expression {
        public String op;
        public Expression value;
        public UnaryOp(String op, Expression expr) {
            this.op = op;
            this.value = expr;
        }
    }
    
    public class BinaryOp extends Expression {
        public String op;
        public Expression left;
        public Expression right;

        public BinaryOp(String op, Expression left, Expression right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }
    }

    
    public class Variable extends Expression {
        public Object name;
        public Variable(Object object) {
            this.name = object;
        }
    }

    public class Constant extends Expression {
        public Object value;
        public Constant(Object value) {
            this.value = value;
        }
    }*/
    public void printAST(Object obj) {
        printAST(obj, 0, "");
    }

    public void printAST(Object obj, int depth, String prefix) {
        String indent = "    ".repeat(depth);
        String objName = obj.getClass().getSimpleName();
        if (obj instanceof TreeNode) {
            Field[] fields = ((TreeNode) obj).getClass().getDeclaredFields();
            Map<String, Object> items = new HashMap<>();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    items.put(field.getName(), field.get(obj));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (items.isEmpty()) {
                System.out.print(indent + prefix + objName + "()");
            } else if (items.size() == 1 && !(items.values().iterator().next() instanceof TreeNode || items.values().iterator().next() instanceof List)) {
                System.out.print(indent + prefix + objName + "(" + items.values().iterator().next() + ")");
            } else {
                System.out.println(indent + prefix + objName + "(");
                for (Map.Entry<String, Object> entry : items.entrySet()) {
                    printAST(entry.getValue(), depth + 1, entry.getKey() + "=");
                    System.out.println(",");
                }
                System.out.print(indent + ")");
            }
        } else if (obj instanceof List && !((List<?>) obj).isEmpty() && ((List<?>) obj).get(0) instanceof TreeNode) {
            System.out.println(indent + prefix + "[");
            for (Object value : (List<?>) obj) {
                printAST(value, depth + 1, "");
                System.out.println(",");
            }
            System.out.print(indent + "]");
        } else {
            System.out.print(indent + prefix + obj);
        }

        if (depth == 0) {
            System.out.println();
        }
    }

    
    /**
     * Consumes the next token from the token list and advances the token index.
     * If the type of the next token does not match the expected token type, a RuntimeException is thrown.
     *
     * @param expectedTokenType The expected type of the next token.
     * @return The next token from the token list.
     * @throws RuntimeException if the next token's type does not match the expected token type.
     */
    public Token consume(TokenType expectedTokenType) {
        //System.out.println("In consume");
        Token nextToken = tokens.get(this.nextTokenIndex);
        //System.out.println("Next token is: " + nextToken);
        this.nextTokenIndex++;
        //System.out.println("Index moved fored");
        //System.out.println("checking type" + nextToken.getType());
        if (nextToken.getType() != expectedTokenType) {

            throw new RuntimeException(String.format("Expected %s, ate %s.", expectedTokenType, nextToken));
        }
        //System.out.println("next token is: " + nextToken);
        //System.out.println("end of consume");
        return nextToken;

    }

    public TokenType peek() {
        return peek(0);
    }

    public TokenType peek(int steps) {
        int peekAt = nextTokenIndex + steps;
        return peekAt < tokens.size() ? tokens.get(peekAt).getType() : null;
    }

    public Object parseValue() {
        TokenType nextTokenType = peek();
        if (nextTokenType == TokenType.NAME) {
            return new Variable(consume(TokenType.NAME).getValue());
        } else if (nextTokenType == TokenType.INT || nextTokenType == TokenType.FLOAT) {
            return new Constant(consume(nextTokenType).getValue());
        } else if (nextTokenType == TokenType.TRUE || nextTokenType == TokenType.FALSE) {
            consume(nextTokenType);
            return new Constant(nextTokenType == TokenType.TRUE);
        } else {
            throw new RuntimeException(String.format("Can't parse %s as a value.", nextTokenType));
        }
    }

    

    public Expression parseAtom() {
        // Parses a parenthesised expression or a number
        if (peek() == TokenType.LPAREN) {
            consume(TokenType.LPAREN);
            Expression result = parseExpr();
            consume(TokenType.RPAREN);
            return result;
        } else {
            return (Expression) parseValue();
        }
    }
    
    public Expression parseExponentiation() {
        // Parses an exponentiation operator.
        Expression result = parseAtom();
        if (peek() == TokenType.EXP) {
            consume(TokenType.EXP);
            result = new BinaryOp("**", result, parseUnary());
        }
        return result;
    }
    
    public Expression parseUnary() {
        TokenType next_token_type = peek();
        if (next_token_type == TokenType.PLUS || next_token_type == TokenType.MINUS) {
            String op = next_token_type == TokenType.PLUS ? "+" : "-";
            consume(next_token_type);
            Expression value = parseUnary();
            return new UnaryOp(op, value);
        } else {
            return parseExponentiation();
        }
    }

    public Expression parseTerm() {
        Expression result = parseUnary();

        while (true) {
            TokenType next_token_type = peek();
            if (next_token_type == TokenType.MUL || next_token_type == TokenType.DIV || next_token_type == TokenType.MOD) {
                String op = next_token_type == TokenType.MUL ? "*" : next_token_type == TokenType.DIV ? "/" : "%";
                consume(next_token_type);
                Expression right = parseUnary();
                result = new BinaryOp(op, result, right);
            } else {
                break;
            }
        }

        return result;
    }

    public Expression parseComputation() {
        Expression result = parseTerm();

        while (true) {
            TokenType next_token_type = peek();
            if (next_token_type == TokenType.PLUS || next_token_type == TokenType.MINUS) {
                String op = next_token_type == TokenType.PLUS ? "+" : "-";
                consume(next_token_type);
                Expression right = parseTerm();
                result = new BinaryOp(op, result, right);
            } else {
                break;
            }
        }

        return result;
    }

    
    public Expression parseNegation() {
        // Parses a Boolean negation.
        if (peek() == TokenType.NOT) {
            //System.out.println("ParsNegation");
            consume(TokenType.NOT);
            return new UnaryOp("not", parseNegation());
        } else {
            //System.out.println("ParsComputation");
            return parseComputation();
        }
    }
    
    public Expression parseExpr() {
        // Parses a full expression.
        //System.out.println("ParsNegation");
        return parseNegation();
    }
    
    public ExprStatement parseExprStatement() {
        // Parses a standalone expression.
        ExprStatement expr = new ExprStatement(parseExpr());
        consume(TokenType.NEWLINE);
        //System.out.println(expr.expression);
        return expr;
    }

    public Assignment parseAssignment() {
        // Parses an assignment.
        Boolean first = true;
        List<Variable> targets = new ArrayList<>();
        while (first || peek(1) == TokenType.ASSIGN) {
            first = false;
            Token nameToken = consume(TokenType.NAME);
            consume(TokenType.ASSIGN);
            targets.add(new Variable(nameToken.getValue()));
        }

        Expression value = parseExpr();
        consume(TokenType.NEWLINE);
        return new Assignment(targets, value);
    }

    public Body parseBody() {
        // Parses the body of a compound statement.
        consume(TokenType.INDENT);
        List<Statement> bodyStatements = new ArrayList<>();
        while (peek() != TokenType.DEDENT) {
            bodyStatements.add(parseStatement());
        }
        consume(TokenType.DEDENT);
        return new Body(bodyStatements);
    }

    public Conditional parseConditional() {
        consume(TokenType.IF);
        Expression condition = parseExpr();
        consume(TokenType.COLON);
        consume(TokenType.NEWLINE);
        Body body = parseBody();
        return new Conditional(condition, body);
    }
    
    
    public Statement parseStatement() {
        //System.out.println("in ParseStatement");
        
        if (peek(1) == TokenType.ASSIGN) {
            //System.out.println("The first element in the peek is: " + peek(1));
            return parseAssignment();
        } else if (peek() == TokenType.IF) {
            //System.out.println("Parsing Conditional");
            return parseConditional();
        } else {
            return parseExprStatement();
        }
    }

    public Program parse() {
        List<Statement> program = new ArrayList<>();
        while (peek() != TokenType.EOF) {
            //System.out.println("In parse loop body");
            //System.out.println(peek());
            program.add(parseStatement());
        }
        consume(TokenType.EOF);
        return new Program(program);
    }
    
    
    



    
    
    

    
    
    
    
    





}

    






