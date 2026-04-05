package com.zara.lexer;

public enum TokenType {
    NUMBER, STRING, IDENTIFIER,
    PLUS, MINUS, STAR, SLASH,
    GREATER, LESS, EQUALS, EQEQ, NOT_EQ, LESS_EQ, GREATER_EQ,
<<<<<<< HEAD
    SET, SHOW, WHEN, LOOP, OTHERWISE,
=======
    SET, SHOW, WHEN, LOOP, TRUE, FALSE,
>>>>>>> a391127 (Add support for boolean literals (true/false) in tokenizer)
    INDENT, DEDENT, COLON, NEWLINE, EOF
}