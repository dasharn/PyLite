import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import ByteUtils.Bytecode;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <code>");
            return;
        }

        String code = args[0];
        Lexer tokenizer = new Lexer(code);
        List<Token> listOfTokens = new ArrayList<>();
        for (Token token : tokenizer) {
            System.out.println(token);
            // add to listOfTokens
            listOfTokens.add(token);
        }
        List<Token> listOfTokensTwo = tokenizer.tokenize();


        System.out.println("List of tokens: " + listOfTokens);
        System.out.println("----------------------------");
        System.out.println("Abstract Syntax Tree:");
        
        Parser parser = new Parser(listOfTokens);
        Object tree = parser.parse();
        parser.printAST(tree);
        
        Compiler compiler = new Compiler(tree);

        BytecodeGenerator bytecodeGenerator = compiler.compile();

        System.out.println("----------------------------");
        System.out.println("Compile to Bytecode: ");
        

        List<Bytecode> bytecode = new ArrayList<>();
        while (bytecodeGenerator.hasNext()) {
            Bytecode next = bytecodeGenerator.next();
            System.out.println(next);
            bytecode.add(next);
        }

        System.out.println("----------------------------");
        System.out.println("Interpreting Bytecode: ");
        new Interpreter(bytecode).interpret();

        
        

        

        
    }
}