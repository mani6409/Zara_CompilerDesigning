package com.zara.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    // Use a Map<String, Object> to store variable names and their values.
    private final Map<String, Object> variables = new HashMap<>();

    public void set(String name, Object value) {
        // Store or update the value for the given variable name.
        variables.put(name, value);
    }

    public Object get(String name) {
        // Return the current value of the variable.
        // If the variable has not been defined, throw a RuntimeException.
        if (!variables.containsKey(name))
            throw new RuntimeException("Variable not defined: " + name);
        return variables.get(name);
    }
}
