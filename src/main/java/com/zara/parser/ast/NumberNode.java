package com.zara.parser.ast;

import com.zara.interpreter.Environment;

// This class represents a numeric literal in the AST
// Example: 10, 3.14, etc.
public class NumberNode implements Expression {

    // Stores the numeric value
    private final double value;

    // Constructor to initialize the number node
    public NumberNode(double value) {
        this.value = value; // assign the numeric value
    }

    // Evaluate method returns the number itself
    // No computation or environment lookup is needed
    @Override
    public Object evaluate(Environment env) {
        return value; // simply return the stored number
    }
}
