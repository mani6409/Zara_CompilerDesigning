package com.zara.lexer;

// This enum defines all possible types of tokens
// that the lexer can identify in the source code.
public enum TokenType {

    // === Basic data types ===

    NUMBER,      // Represents numeric values (e.g., 10, 3.14)
    STRING,      // Represents string values (e.g., "hello")
    IDENTIFIER,  // Represents variable names or user-defined names (e.g., x, total)

    // === Arithmetic operators ===

    PLUS,   // '+' operator (addition)
    MINUS,  // '-' operator (subtraction)
    STAR,   // '*' operator (multiplication)
    SLASH,  // '/' operator (division)

    // === Comparison operators ===

    GREATER,     // '>' operator
    LESS,        // '<' operator
    EQUALS,      // '=' operator (assignment or simple equality depending on language design)
    EQEQ,        // '==' operator (equality check)
    NOT_EQ,      // '!=' operator (not equal)
    LESS_EQ,     // '<=' operator
    GREATER_EQ,  // '>=' operator

    // === Keywords (reserved words in your language) ===

    SET,        // Keyword for assigning values (e.g., set x = 10)
    SHOW,       // Keyword for displaying output (like print)
    WHEN,       // Conditional keyword (similar to "if")
    LOOP,       // Loop keyword (like while/for)
    OTHERWISE,  // Alternative condition (like else)

    // === Structural tokens (used for formatting / parsing structure) ===

    INDENT,   // Represents increase in indentation (like Python blocks)
    DEDENT,   // Represents decrease in indentation
    COLON,    // ':' symbol (used in conditions, loops, etc.)
    NEWLINE,  // End of a line

    // === Special token ===

    EOF       // End Of File (marks the end of input)
}
