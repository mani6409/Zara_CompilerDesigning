package com.zara.runtime;

public class Value {
    private Object value;

    public Value(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String asString() {
        return value.toString();
    }

    public double asNumber() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new RuntimeException("Value is not a number: " + value);
    }
    
    public boolean isNumber() {
        return value instanceof Number;
    }

    @Override
    public String toString() {
        return asString();
    }
}
