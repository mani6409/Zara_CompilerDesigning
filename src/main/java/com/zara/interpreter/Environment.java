package com.zara.interpreter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.zara.runtime.Value;

public class Environment {
    // Scope stack: top (head) scope is where new bindings are created.
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();

    public Environment() {
        // Always keep one global scope so get/set have a place to start from.
        scopes.push(new HashMap<>());
    }

    public void set(String name, Object value) {
        Object toStore = (value instanceof Value) ? value : new Value(value);

        // Update the nearest existing binding, otherwise create it in the current
        // scope.
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                scope.put(name, toStore);
                return;
            }
        }
        scopes.peek().put(name, toStore);
    }

    public Object get(String name) {
        // Return the current value of the variable.
        // If the variable has not been defined, throw a RuntimeException.
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                Object stored = scope.get(name);
                return stored instanceof Value v ? v.getValue() : stored;
            }
        }
        throw new RuntimeException("Environment Error: Variable '" + name + "' is not defined in the current scope.");
    }

    public boolean contains(String name) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    public Object getOrDefault(String name, Object defaultValue) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                Object stored = scope.get(name);
                return stored instanceof Value v ? v.getValue() : stored;
            }
        }
        return defaultValue;
    }

    /**
     * Alias for tests/integration points that expect a "storeVariable" API.
     */
    public void storeVariable(String name, Object value) {
        set(name, value);
    }

    /**
     * Alias for tests/integration points that expect a "retrieveVariable" API.
     * Returns {@code null} when the key is missing (distinct from
     * {@link #get(String)}).
     */
    public Object retrieveVariable(String name) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                Object stored = scope.get(name);
                return stored instanceof Value v ? v.getValue() : stored;
            }
        }
        return null;
    }

    /**
     * Enter a new lexical scope for block-like constructs.
     */
    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    /**
     * Exit the current lexical scope.
     * The global scope is never removed.
     */
    public void exitScope() {
        if (scopes.size() <= 1)
            return;
        scopes.pop();
    }

    // Extra aliases (useful for tests / other integration points).
    public void pushScope() {
        enterScope();
    }

    public void popScope() {
        exitScope();
    }
}
