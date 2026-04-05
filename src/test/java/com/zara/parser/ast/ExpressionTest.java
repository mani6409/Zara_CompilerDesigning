// Tests for AST expression nodes: NumberNode, StringNode, VariableNode, BinaryOpNode

package com.zara.parser.ast;

import com.zara.interpreter.Environment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExpressionTest {

    private Environment env;

    @BeforeEach
    void setUp() {
        env = new Environment();
    }

    // ---------------------------------------------------------------
    // NumberNode
    // ---------------------------------------------------------------

    @Test
    void testNumberNode_evaluatesToDouble() {
        NumberNode node = new NumberNode(42.0);
        Object result = node.evaluate(env);
        assertInstanceOf(Double.class, result,
            "NumberNode should evaluate to a Double");
        assertEquals(42.0, (Double) result, 1e-9,
            "NumberNode(42.0) should evaluate to 42.0");
    }

    @Test
    void testNumberNode_zero() {
        assertEquals(0.0, (Double) new NumberNode(0.0).evaluate(env), 1e-9);
    }

    @Test
    void testNumberNode_negative() {
        assertEquals(-7.5, (Double) new NumberNode(-7.5).evaluate(env), 1e-9);
    }

    // ---------------------------------------------------------------
    // StringNode
    // ---------------------------------------------------------------

    @Test
    void testStringNode_evaluatesToString() {
        StringNode node = new StringNode("hello");
        Object result = node.evaluate(env);
        assertInstanceOf(String.class, result,
            "StringNode should evaluate to a String");
        assertEquals("hello", result);
    }

    @Test
    void testStringNode_emptyString() {
        assertEquals("", new StringNode("").evaluate(env));
    }

    // ---------------------------------------------------------------
    // VariableNode
    // ---------------------------------------------------------------

    @Test
    void testVariableNode_returnsStoredValue() {
        env.set("x", 99.0);
        Object result = new VariableNode("x").evaluate(env);
        assertEquals(99.0, result);
    }

    @Test
    void testVariableNode_undefinedVariable_throws() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> new VariableNode("undefined").evaluate(env),
            "Accessing an undefined variable should throw RuntimeException");
        assertNotNull(ex.getMessage());
    }

    // ---------------------------------------------------------------
    // BinaryOpNode — arithmetic
    // ---------------------------------------------------------------

    @Test
    void testBinaryOpNode_addition() {
        // 3 + 4 = 7
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(3), "+", new NumberNode(4));
        assertEquals(7.0, (Double) node.evaluate(env), 1e-9);
    }

    @Test
    void testBinaryOpNode_subtraction() {
        // 10 - 6 = 4
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(10), "-", new NumberNode(6));
        assertEquals(4.0, (Double) node.evaluate(env), 1e-9);
    }

    @Test
    void testBinaryOpNode_multiplication() {
        // 5 * 3 = 15
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(5), "*", new NumberNode(3));
        assertEquals(15.0, (Double) node.evaluate(env), 1e-9);
    }

    @Test
    void testBinaryOpNode_division() {
        // 9 / 3 = 3
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(9), "/", new NumberNode(3));
        assertEquals(3.0, (Double) node.evaluate(env), 1e-9);
    }

    @Test
    void testBinaryOpNode_divisionByZero_throws() {
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(5), "/", new NumberNode(0));
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> node.evaluate(env),
            "Division by zero should throw RuntimeException");
        assertTrue(ex.getMessage().toLowerCase().contains("zero"),
            "Error message should mention 'zero'");
    }

    // ---------------------------------------------------------------
    // BinaryOpNode — comparison operators
    // ---------------------------------------------------------------

    @Test
    void testBinaryOpNode_greaterThan_true() {
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(5), ">", new NumberNode(3));
        assertEquals(Boolean.TRUE, node.evaluate(env));
    }

    @Test
    void testBinaryOpNode_greaterThan_false() {
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(2), ">", new NumberNode(3));
        assertEquals(Boolean.FALSE, node.evaluate(env));
    }

    @Test
    void testBinaryOpNode_lessThan_true() {
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(1), "<", new NumberNode(4));
        assertEquals(Boolean.TRUE, node.evaluate(env));
    }

    @Test
    void testBinaryOpNode_equalEqual_true() {
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(7), "==", new NumberNode(7));
        assertEquals(Boolean.TRUE, node.evaluate(env));
    }

    @Test
    void testBinaryOpNode_equalEqual_false() {
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(7), "==", new NumberNode(8));
        assertEquals(Boolean.FALSE, node.evaluate(env));
    }

    // ---------------------------------------------------------------
    // BinaryOpNode — string concatenation
    // ---------------------------------------------------------------

    @Test
    void testBinaryOpNode_stringConcatenation() {
        BinaryOpNode node = new BinaryOpNode(
            new StringNode("hello "), "+", new StringNode("world"));
        assertEquals("hello world", node.evaluate(env));
    }

    @Test
    void testBinaryOpNode_numberPlusString_concatenates() {
        // When one operand is a String, '+' should concatenate
        BinaryOpNode node = new BinaryOpNode(
            new NumberNode(5), "+", new StringNode(" items"));
        assertEquals("5.0 items", node.evaluate(env));
    }

    // ---------------------------------------------------------------
    // BinaryOpNode — nested / composed AST
    // ---------------------------------------------------------------

    @Test
    void testBinaryOpNode_nestedArithmetic() {
        // (2 + 3) * 4 = 20
        BinaryOpNode inner = new BinaryOpNode(
            new NumberNode(2), "+", new NumberNode(3));
        BinaryOpNode outer = new BinaryOpNode(
            inner, "*", new NumberNode(4));
        assertEquals(20.0, (Double) outer.evaluate(env), 1e-9);
    }

    @Test
    void testBinaryOpNode_withVariableOperand() {
        // x + 10 where x = 5 → 15
        env.set("x", 5.0);
        BinaryOpNode node = new BinaryOpNode(
            new VariableNode("x"), "+", new NumberNode(10));
        assertEquals(15.0, (Double) node.evaluate(env), 1e-9);
    }

    // ---------------------------------------------------------------
    // BinaryOpNode — invalid operand type
    // ---------------------------------------------------------------

    @Test
    void testBinaryOpNode_nonNumericOperandForMinus_throws() {
        BinaryOpNode node = new BinaryOpNode(
            new StringNode("abc"), "-", new NumberNode(1));
        assertThrows(RuntimeException.class,
            () -> node.evaluate(env),
            "'-' on a String operand should throw RuntimeException");
    }
}