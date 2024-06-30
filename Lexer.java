import java.util.*;

enum TokenType {
    INT,  // integers
    FLOAT,  // floats
    PLUS,  // +
    MINUS,  // -
    EOF,  // end of file
    LPAREN,  // (
    RPAREN,  // )
    MUL,  // *
    DIV,  // /
    MOD,  // %
    EXP,  // **
    NEWLINE,  // newline character
    NAME,  // any possible variable name
    ASSIGN,  // =
    INDENT,  // indentation
    DEDENT,  // dedentation
    IF,  // if
    COLON,  // :
    TRUE,  // True
    FALSE,  // False
    NOT;  // not

    @Override
    public String toString() {
        return this.name();
    }
}

class Token {
    private TokenType type;
    private Object value;

    public Token(TokenType type) {
        this.type = type;
    }

    public Token(TokenType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        if (this.value != null) {
            return String.format("Token(%s, %s)", this.type, this.value);
        } else {
            return String.format("Token(%s)", this.type);
        }
    }
}

class Lexer implements Iterable<Token> {
    private static final Map<Character, TokenType> CHARS_AS_TOKENS = new HashMap<>();
    private static final Map<String, TokenType> KEYWORDS_AS_TOKENS = new HashMap<>();
    private static final String LEGAL_NAME_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private static final String LEGAL_NAME_START_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
    private String code;
    private int ptr;
    private boolean beginningOfLine;
    private int currentIndentationLevel;
    private Deque<Token> nextTokens;

    static {
        CHARS_AS_TOKENS.put('+', TokenType.PLUS);
        CHARS_AS_TOKENS.put('-', TokenType.MINUS);
        CHARS_AS_TOKENS.put('(', TokenType.LPAREN);
        CHARS_AS_TOKENS.put(')', TokenType.RPAREN);
        CHARS_AS_TOKENS.put('*', TokenType.MUL);
        CHARS_AS_TOKENS.put('/', TokenType.DIV);
        CHARS_AS_TOKENS.put('%', TokenType.MOD);
        CHARS_AS_TOKENS.put('=', TokenType.ASSIGN);
        CHARS_AS_TOKENS.put(':', TokenType.COLON);

        KEYWORDS_AS_TOKENS.put("if", TokenType.IF);
        KEYWORDS_AS_TOKENS.put("True", TokenType.TRUE);
        KEYWORDS_AS_TOKENS.put("False", TokenType.FALSE);
        KEYWORDS_AS_TOKENS.put("not", TokenType.NOT);
    }

    public Lexer(String code) {
        this.code = code + "\n"; // Ensure the program ends with a newline.
        this.ptr = 0;
        this.beginningOfLine = true;
        this.currentIndentationLevel = 0;
        this.nextTokens = new ArrayDeque<>();
    }


    /**
     * This method is used to consume an integer from the code string.
     * It starts from the current position (ptr) and continues until it encounters a non-digit character.
     * The consumed integer is then returned.
     *
     * @return The integer that was consumed from the code string.
     */
    private int consumeInt(){
        int start = this.ptr;
        while (this.ptr < code.length() && Character.isDigit(code.charAt(this.ptr))) {
            this.ptr++;
        }
        return Integer.parseInt(code.substring(start, this.ptr));
    }
    /**
     * This method is used to consume a float from the code string.
     * It starts from the current position (ptr) and continues until it encounters a non-digit character.
     * The consumed float is then returned.
     *
     * @return The float that was consumed from the code string.
     */

    private float consumeDecimal() {
        int start = this.ptr;
        this.ptr++;
        while (this.ptr < code.length() && Character.isDigit(code.charAt(this.ptr))) {
            this.ptr++;
        }
        String floatStr = ((this.ptr - start) > 1) ? code.substring(start, this.ptr) : ".0";
        return Float.parseFloat(floatStr);
    }

    /**
     * This method is used to consume a name from the code string.
     * It starts from the current position (ptr) and continues until it encounters a non-alphanumeric character.
     * The consumed name is then returned.
     *
     * @return The name that was consumed from the code string.
     */
    private String consumeName() {
        int start = this.ptr;
        this.ptr++;
        while (this.ptr < code.length() && LEGAL_NAME_CHARACTERS.indexOf(code.charAt(this.ptr)) >= 0) {
            this.ptr++;
        }
        return code.substring(start, this.ptr);
    }

    private String consumeIndentation() {
        int start = ptr;
        while (this.ptr < code.length() && code.charAt(this.ptr) == ' ') {
            this.ptr++;
        }
        return code.substring(start, this.ptr);
    }

    /**
     * This method is used to consume the next token from the code string.
     * It starts from the current position (ptr) and continues until it encounters a token.
     * The consumed token is then returned.
     *
     * @return The token that was consumed from the code string.
     */
    private String peek(int length) {
        return code.substring(this.ptr, Math.min(this.ptr + length, code.length()));
    }



    /**
     * Processes and returns the next token from the source code.
     * This method handles indentation, dedentation, and the generation of various token types based on the current character in the source code.
     * It supports tokens for indentation, dedentation, end of file, new lines, operators, keywords, names, integers, floats, and unrecognized characters.
     * 
     * @return The next token from the source code.
     * @throws RuntimeException If the indentation is not a multiple of 4 or if an unrecognized character is encountered.
     */
    public Token nextToken() {
        // Handle indentation at the beginning of a line
        if (this.beginningOfLine) {
            String indentation = consumeIndentation();
            // Skip empty lines
            if (peek(1).equals("\n")) {
                this.ptr++;
                return nextToken();
            }

            // Check for correct indentation
            if (indentation.length() % 4 != 0) {
                throw new RuntimeException("Indentation must be a multiple of 4.");
            }

            // Adjust indentation levels
            int indentLevel = indentation.length() / 4;
            while (indentLevel > this.currentIndentationLevel) {
                nextTokens.add(new Token(TokenType.INDENT));
                this.currentIndentationLevel++;
            }
            while (indentLevel < this.currentIndentationLevel) {
                nextTokens.add(new Token(TokenType.DEDENT));
                this.currentIndentationLevel--;
            }
            this.beginningOfLine = false;
        }

        // Return pending tokens from adjustments
        if (!nextTokens.isEmpty()) {
            return nextTokens.poll();
        }

        // Skip spaces
        while (this.ptr < code.length() && code.charAt(this.ptr) == ' ') {
            this.ptr++;
        }

        // Handle end of file
        if (this.ptr == code.length()) {
            return new Token(TokenType.EOF);
        }

        // Handle new lines and reset beginning of line
        char charAtPtr = code.charAt(this.ptr);
        if (charAtPtr == '\n') {
            this.ptr++;
            if (!this.beginningOfLine) {
                this.beginningOfLine = true;
                return new Token(TokenType.NEWLINE);
            } else {
                return nextToken();
            }
        }

        this.beginningOfLine = false;
        // Handle specific characters or sequences
        if (peek(2).equals("**")) {
            this.ptr += 2;
            return new Token(TokenType.EXP);
        } else if (CHARS_AS_TOKENS.containsKey(charAtPtr)) {
            this.ptr++;
            return new Token(CHARS_AS_TOKENS.get(charAtPtr));
        } else if (LEGAL_NAME_START_CHARACTERS.indexOf(charAtPtr) >= 0) {
            // Handle names and keywords
            String name = consumeName();
            TokenType keywordTokenType = KEYWORDS_AS_TOKENS.get(name);
            if (keywordTokenType != null) {
                return new Token(keywordTokenType);
            } else {
                return new Token(TokenType.NAME, name);
            }
        } else if (Character.isDigit(charAtPtr)) {
            // Handle integers and floats
            int integer = consumeInt();
            if (this.ptr < code.length() && code.charAt(this.ptr) == '.') {
                float decimal = consumeDecimal();
                return new Token(TokenType.FLOAT, integer + decimal);
            }
            return new Token(TokenType.INT, integer);
        } else if (charAtPtr == '.' && this.ptr + 1 < code.length() && Character.isDigit(code.charAt(this.ptr + 1))) {
            // Handle floats starting with a dot
            float decimal = consumeDecimal();
            return new Token(TokenType.FLOAT, decimal);
        } else {
            // Handle unrecognized characters
            throw new RuntimeException("Unable to tokenize the character '" + charAtPtr + "'. This character is not recognized as part of the expected language syntax.");
        }
    }

    /**
     * Tokenizes the entire source code into a list of tokens.
     * This method iterates through the source code, generating tokens until the end of file (EOF) token is reached.
     * It adds each generated token to a list, including the EOF token at the end, and returns the list.
     * 
     * @return A list of tokens representing the tokenized source code.
     */

    public List<Token> tokenize(){
        
        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = nextToken()).getType() != TokenType.EOF) {
            tokens.add(token);
        }
        tokens.add(token); // Add the EOF token
        return tokens;
    
    }


    /**
     * Provides an iterator over the tokens generated by the lexer.
     * This method implements the Iterator interface, allowing the tokens of the lexer to be iterated in a for-each loop.
     * The iterator generates tokens on-the-fly until the end of file (EOF) token is reached, ensuring that all tokens are processed.
     * It handles the EOF token specially to ensure it is returned exactly once at the end of the iteration.
     * 
     * @return An iterator over the tokens generated by the lexer.
     */
    @Override
    public Iterator<Token> iterator() {
        return new Iterator<>() {
            private Token nextToken = Lexer.this.nextToken();
            private Boolean end = false;

            @Override
            public boolean hasNext() {
                //System.out.println(ptr + "" + code.length());
                //System.out.println(nextToken.getType());

                if (nextToken.getType() == TokenType.EOF && ptr == code.length() && !end){
                    end = true;
                    return true;
                }
                return nextToken.getType() != TokenType.EOF;

            }

            @Override
            public Token next() {
                Token currentToken = this.nextToken;
                this.nextToken = Lexer.this.nextToken();
                return currentToken;
            }
        };
    }

    

}