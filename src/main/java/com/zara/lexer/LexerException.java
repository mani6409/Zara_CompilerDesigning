package com.zara.lexer;

public class LexerException extends RuntimeException {

    public LexerException(String message, int line) {
        super("Line " + line + ": " + message);
    }
}
