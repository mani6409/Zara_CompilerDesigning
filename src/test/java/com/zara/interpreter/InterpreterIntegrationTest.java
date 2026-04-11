package com.zara.interpreter;

import com.zara.interpreter.Interpreter;

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
        interpreter.run("x = 5;");
        assertEquals(5, interpreter.getVariable("x"));
    }

    @Test
    public void testArithmeticOperations() {
        interpreter.run("x = 5;");
        interpreter.run("y = 10;");
        interpreter.run("result = x + y;");
        assertEquals(15, interpreter.getVariable("result"));
        interpreter.run("result = y - x;");
        assertEquals(5, interpreter.getVariable("result"));
        interpreter.run("result = x * y;");
        assertEquals(50, interpreter.getVariable("result"));
        interpreter.run("result = y / x;");
        assertEquals(2, interpreter.getVariable("result"));
    }

    @Test
    public void testComparisons() {
        interpreter.run("x = 5;");
        interpreter.run("y = 10;");
        interpreter.run("result = x < y;");
        assertTrue((Boolean) interpreter.getVariable("result"));
        interpreter.run("result = x > y;");
        assertFalse((Boolean) interpreter.getVariable("result"));
        interpreter.run("result = x == 5;");
        assertTrue((Boolean) interpreter.getVariable("result"));
    }

    @Test
    public void testLoops() {
        interpreter.run("sum = 0;");
        interpreter.run("for (i = 1; i <= 5; i = i + 1) { sum = sum + i; }");
        assertEquals(15, interpreter.getVariable("sum"));
    }

    @Test
    public void testVariableReferences() {
        interpreter.run("x = 5;");
        interpreter.run("y = x + 5;");
        assertEquals(10, interpreter.getVariable("y"));
    }
}