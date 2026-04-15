package com.zara.parser.ast;

import com.zara.interpreter.*;

// This class represents a string literal in the AST
// Example: "hello", "world", etc.
public class StringNode implements Expression {

    // Stores the string value
    private final String value;

    // Constructor to initialize the string node
    public StringNode(String value) {
        this.value = value; // assign the string value
    }

    // Evaluate method returns the string itself
    // No computation or environment lookup is needed
    @Override
    public Object evaluate(Environment env) {
        return value; // simply return the stored string
    }
}
