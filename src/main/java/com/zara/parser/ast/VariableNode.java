package com.zara.parser.ast;

import com.zara.interpreter.*;

public class VariableNode implements Expression {
    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public Object evaluate(Environment env) {
        // Ask the Environment for the current value of this variable.
        return env.get(name);
    }
}
