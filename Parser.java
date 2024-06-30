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


    /**
     * Peeks at the current token in the token stream without consuming it.
     * This method utilizes the peek(int steps) method with a step value of 0 to look at the current token.
     * It is useful for decision-making in parsing algorithms where the next token type needs to be known
     * without advancing the token stream.
     * 
     * @return The TokenType of the current token in the stream.
     */
    public TokenType peek() {
        return peek(0);
    }


    /**
     * Peeks ahead in the token stream by a specified number of steps.
     * This method allows looking ahead in the token stream to determine the type of upcoming tokens without consuming them.
     * It calculates the index of the token to peek at by adding the specified number of steps to the current token index.
     * If the calculated index is within the bounds of the token list, it returns the type of the token at that index.
     * Otherwise, if the index is out of bounds (indicating the end of the token stream has been reached), it returns null.
     * 
     * @param steps The number of steps to peek ahead in the token stream.
     * @return The TokenType of the token at the specified number of steps ahead, or null if out of bounds.
     */
    public TokenType peek(int steps) {
        int peekAt = nextTokenIndex + steps;
        return peekAt < tokens.size() ? tokens.get(peekAt).getType() : null;
    }



    /**
     * Parses a value from the source code.
     * This method is responsible for parsing values, which can be variables, integers, floats, or boolean constants.
     * It checks the type of the next token to determine the type of value to parse. For variables, it creates a Variable object
     * with the token's value. For integers and floats, it creates a Constant object with the token's value. For boolean constants
     * (true or false), it directly creates a Constant object with the boolean value. If the token type does not represent a valid
     * value, it throws a RuntimeException.
     * 
     * @return An Object representing the parsed value, which can be a Variable or a Constant.
     */
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


    /**
     * Parses an atomic expression from the source code.
     * This method handles the parsing of atomic expressions, which can either be a parenthesized expression or a simple value (like a number).
     * If the current token is a left parenthesis (LPAREN), it consumes this token, recursively calls parseExpr to parse the expression inside the parentheses,
     * consumes the right parenthesis (RPAREN), and returns the parsed expression. If the current token is not a left parenthesis, it assumes the token represents
     * a value and calls parseValue to parse and return it.
     * 
     * @return An Expression object representing the parsed atomic expression, which could be a parenthesized expression or a simple value.
     */
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


    /**
     * Parses an exponentiation expression from the source code.
     * This method is responsible for parsing expressions that involve the exponentiation operator (**).
     * It starts by parsing an atomic expression, which can be a number, a variable, or an expression enclosed in parentheses.
     * If the next token is an exponentiation operator, it consumes the token and recursively parses the right-hand side
     * of the exponentiation as a unary expression. This allows for right-associative parsing of chained exponentiation operations.
     * The method constructs and returns a BinaryOp object representing the exponentiation operation.
     * 
     * @return An Expression object representing the parsed exponentiation expression.
     */
    public Expression parseExponentiation() {
        // Parses an exponentiation operator.
        Expression result = parseAtom();
        if (peek() == TokenType.EXP) {
            consume(TokenType.EXP);
            result = new BinaryOp("**", result, parseUnary());
        }
        return result;
    }
    

    /**
     * Parses a unary expression from the source code.
     * This method handles unary expressions, which include unary plus and minus operations.
     * It checks the next token to determine if it is a PLUS or MINUS operator. If so, it consumes the token,
     * recursively calls itself to parse the unary expression that follows the operator, and creates a UnaryOp object
     * representing the unary operation applied to the expression. If the next token is not a PLUS or MINUS operator,
     * it delegates to parseExponentiation to handle other types of expressions.
     * 
     * @return An Expression object representing either a unary operation applied to an expression or the result of parseExponentiation.
     */
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


    /**
     * Parses a term from the source code.
     * This method is responsible for parsing terms in expressions, specifically handling multiplication, division, and modulo operations.
     * It starts by parsing a unary expression, which can be a simple number, a variable, or an expression enclosed in parentheses.
     * After parsing the initial unary expression, it enters a loop to check for the presence of multiplication (*), division (/), or modulo (%) operators.
     * If any of these operators are found, it consumes the operator token, parses another unary expression as the right operand, and constructs a BinaryOp object
     * representing the operation. This process repeats for each multiplication, division, or modulo operator found, allowing for the parsing of expressions with multiple
     * such operations in sequence.
     * 
     * @return An Expression object representing the parsed term, which may be a single unary expression or a binary operation involving multiple unary expressions.
     */
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


    /**
     * Parses a computation expression from the source code.
     * This method handles the parsing of expressions that involve addition and subtraction operations.
     * It starts by parsing a term using the parseTerm method. Then, it enters a loop, checking for the presence of
     * PLUS or MINUS tokens. If either is found, it consumes the token, parses another term, and constructs a BinaryOp
     * object representing the operation between the two terms. This process repeats until no more PLUS or MINUS tokens
     * are found, ensuring all addition and subtraction operations within the expression are parsed.
     * 
     * @return An Expression object representing the parsed computation expression, potentially consisting of multiple
     *         BinaryOp objects nested to represent the order of operations.
     */
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

    /**
     * Parses a negation expression from the source code.
     * This method handles the parsing of expressions that involve the Boolean negation operator.
     * If the current token is a NOT operator, it consumes the token and recursively parses the negated expression,
     * creating a UnaryOp object representing the negation. If the NOT operator is not present, it delegates to parseComputation
     * to handle other types of expressions.
     * 
     * @return An Expression object representing either a negated expression or the result of parseComputation.
     */
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
    

    /**
     * Parses a full expression from the source code.
     * This method serves as an entry point for parsing expressions. It currently delegates to the parseNegation method,
     * which handles parsing expressions with potential negation operators. This setup allows for easy extension to support
     * additional expression types in the future.
     * 
     * @return An Expression object representing the parsed expression.
     */
    public Expression parseExpr() {
        // Parses a full expression.
        //System.out.println("ParsNegation");
        return parseNegation();
    }


    /**
     * Parses an expression statement from the source code.
     * This method is used for parsing statements that consist solely of an expression followed by a newline.
     * It begins by parsing the expression using the parseExpr method. The parsed expression is then used to create an ExprStatement object.
     * After creating the ExprStatement object, it consumes a NEWLINE token to ensure that the statement is properly terminated.
     * 
     * @return An ExprStatement object representing the parsed expression statement.
     */
    public ExprStatement parseExprStatement() {
        // Parses a standalone expression.
        ExprStatement expr = new ExprStatement(parseExpr());
        consume(TokenType.NEWLINE);
        //System.out.println(expr.expression);
        return expr;
    }


    /**
     * Parses an assignment statement from the source code.
     * This method handles parsing of assignment statements, which may include multiple targets for a single value.
     * It starts by parsing the left-hand side (LHS) variable(s) and the assignment operator. It supports chained assignments.
     * After parsing the LHS, it parses the right-hand side (RHS) expression that represents the value to be assigned.
     * Finally, it consumes a newline token to signify the end of the assignment statement.
     * 
     * @return An Assignment object representing the parsed assignment statement, including the target variable(s) and the value expression.
     */
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


    /**
     * Parses the body of a compound statement.
     * This method is responsible for parsing the body of statements that are enclosed within an indentation block.
     * It starts by consuming the initial INDENT token, indicating the start of a new block.
     * Then, it enters a loop, parsing statements until a DEDENT token is encountered, signifying the end of the block.
     * Each parsed statement is added to a list of statements. After consuming the DEDENT token, it constructs and returns a Body object containing all parsed statements.
     * 
     * @return A Body object containing all statements parsed within the indentation block.
     */
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

     /**
     * Parses a conditional statement from the source code.
     * This method starts by consuming the 'if' token, indicating the start of a conditional statement.
     * It then parses the condition expression, consumes the colon token followed by a newline token, and finally parses the body of the conditional.
     * The method constructs and returns a Conditional object representing the parsed conditional statement.
     * 
     * @return A Conditional object representing the parsed conditional statement.
     */
    public Conditional parseConditional() {
        consume(TokenType.IF);
        Expression condition = parseExpr();
        consume(TokenType.COLON);
        consume(TokenType.NEWLINE);
        Body body = parseBody();
        return new Conditional(condition, body);
    }
    
    
    /**
     * Parses a single statement from the source code.
     * This method checks the current token to determine the type of statement to parse.
     * It supports parsing assignment statements, conditional statements (if), and expression statements.
     * The appropriate parsing method is called based on the type of statement detected.
     * 
     * @return The parsed statement as an instance of a subclass of Statement.
     */
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


    /**
     * Parses the entire source code into a program.
     * This method iterates through the source code, parsing each statement until an end of file (EOF) token is encountered.
     * It collects all parsed statements into a list, which is then used to construct a Program instance.
     * The method ensures that the EOF token is explicitly consumed before returning the Program instance.
     * 
     * @return A Program instance containing all parsed statements.
     */
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

    






