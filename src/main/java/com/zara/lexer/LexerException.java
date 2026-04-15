package com.zara.lexer;

// ADDED: Custom exception for lexer errors
public class LexerException extends RuntimeException {

    public LexerException(String message, int line) {
        super("Line " + line + ": " + message);
    }
}