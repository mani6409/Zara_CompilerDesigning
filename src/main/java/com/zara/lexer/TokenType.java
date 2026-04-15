package com.zara.lexer;

// This enum defines all possible types of tokens
// that the lexer can identify in the source code.
public enum TokenType {

    // === Basic data types ===

    NUMBER,      // Represents numeric values (e.g., 10, 3.14)
    STRING,      // Represents string values (e.g., "hello")
    IDENTIFIER,  // Represents variable names or user-defined names (e.g., x, total)

    // === Arithmetic operators ===

    PLUS,   // '+'
    MINUS,  // '-'
    STAR,   // '*'
    SLASH,  // '/'

    // === Comparison operators ===

    GREATER,     // '>'
    LESS,        // '<'
    EQUALS,      // '='
    EQEQ,        // '=='
    NOT_EQ,      // '!='
    LESS_EQ,     // '<='
    GREATER_EQ,  // '>='

    // === Keywords ===

    SET,
    SHOW,
    WHEN,
    LOOP,
    OTHERWISE,

    // === Boolean literals ===

    TRUE,
    FALSE,

    // === Structural tokens ===

    INDENT,
    DEDENT,
    COLON,
    NEWLINE,

    // === Special token ===

    EOF
}