package com.zara.interpreter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterIntegrationTest {
    private Interpreter interpreter;

    @BeforeEach
    public void setUp() {
        interpreter = new Interpreter();
    }

    @Test
    public void testSimpleAssignment() {
        interpreter.run("set x = 5;");
        assertEquals(5, ((Number) interpreter.getVariable("x")).intValue());
    }

    @Test
    public void testArithmeticOperations() {
        interpreter.run("set x = 5;");
        interpreter.run("set y = 10;");
        interpreter.run("set result = x + y;");
        assertEquals(15, ((Number) interpreter.getVariable("result")).intValue());

        interpreter.run("set result = y - x;");
        assertEquals(5, ((Number) interpreter.getVariable("result")).intValue());

        interpreter.run("set result = x * y;");
        assertEquals(50, ((Number) interpreter.getVariable("result")).intValue());

        interpreter.run("set result = y / x;");
        assertEquals(2, ((Number) interpreter.getVariable("result")).intValue());
    }

    @Test
    public void testComparisons() {
        interpreter.run("set x = 5;");
        interpreter.run("set y = 10;");
        interpreter.run("set result = x < y;");
        assertTrue((Boolean) interpreter.getVariable("result"));

        interpreter.run("set result = x > y;");
        assertFalse((Boolean) interpreter.getVariable("result"));

        interpreter.run("set result = x == 5;");
        assertTrue((Boolean) interpreter.getVariable("result"));
    }

    @Test
    public void testLoops() {
        interpreter.run("set sum = 0;");
        interpreter.run("for (i = 1; i <= 5; i = i + 1) { sum = sum + i; }");
        assertEquals(15, ((Number) interpreter.getVariable("sum")).intValue());
    }

    @Test
    public void testVariableReferences() {
        interpreter.run("set x = 5;");
        interpreter.run("set y = x + 5;");
        assertEquals(10, ((Number) interpreter.getVariable("y")).intValue());
    }
}