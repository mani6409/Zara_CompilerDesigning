package com.zara.parser.ast;

import com.zara.interpreter.*;

// This class represents a variable in the AST
// Example: x, total, count, etc.
public class VariableNode implements Expression {

    // Stores the variable name
    private final String name;

    // Constructor to initialize the variable node
    public VariableNode(String name) {
        this.name = name; // assign the variable name
    }

    // Evaluate method retrieves the value of the variable
    // from the Environment (runtime storage of variables)
    @Override
    public Object evaluate(Environment env) {

        // Ask the Environment for the current value of this variable
        // Example: if name = "x", it will return the value of x
        return env.get(name);
    }
}
