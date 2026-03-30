package com.zara.interpreter;
import com.zara.lexer.*;
import com.zara.parser.*;
import com.zara.interpreter.instruction.*;


import java.util.List;

public class Interpreter {
    public void run(String sourceCode) {
        // Step 1: Pass sourceCode to a new Tokenizer and get the token list.
        List<Token> tokens = new Tokenizer(sourceCode).tokenize();

        // Step 2: Pass the token list to a new Parser and get the instruction list.
        List<Instruction> instructions = new Parser(tokens).parse();

        // Step 3: Create a new Environment, then execute each instruction.
        Environment env = new Environment();
        for (Instruction instruction : instructions) {
            instruction.execute(env);
        }
    }
}
