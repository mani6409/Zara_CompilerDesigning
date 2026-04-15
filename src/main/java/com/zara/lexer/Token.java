package com.zara.lexer;

// This class represents a single token in the lexer.
// A token is a small unit of code like keyword, identifier, number, etc.
public class Token {

    // Type of the token (e.g., KEYWORD, IDENTIFIER, NUMBER, etc.)
    private final TokenType type;

    // Actual value/text of the token (e.g., "int", "x", "10")
    private final String value;

    // Line number where this token appears in the source code
    private final int line;

    // Constructor to initialize a Token object with type, value, and line number
    public Token(TokenType type, String value, int line) {
        this.type = type;   // Assign token type
        this.value = value; // Assign token value
        this.line = line;   // Assign line number
    }

    // Getter method to get the token type
    public TokenType getType() { return type; }

    // Getter method to get the token value
    public String getValue()   { return value; }

    // Getter method to get the line number
    public int getLine()       { return line; }

    // This method is automatically called when we print the token
    // It returns a readable string representation of the token
    @Override
    public String toString() {
        // Format: [TYPE "value" LlineNumber]
        // Example: [IDENTIFIER "x" L5]
        return "[" + type + " \"" + value + "\" L" + line + "]";
    }
}
