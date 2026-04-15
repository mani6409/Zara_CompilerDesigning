// Tests for AST statement-level nodes via the instruction layer

package com.zara.parser.ast;

import com.zara.interpreter.Environment;
import com.zara.interpreter.instruction.AssignInstruction;
import com.zara.interpreter.instruction.PrintInstruction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatementTest {

    private Environment env;

    @BeforeEach
    void setUp() {
        env = new Environment();
    }

    @Test
    void testAssignInstruction_storesValueInEnvironment() {
        // set x = 10
        AssignInstruction assign = new AssignInstruction("x", new NumberNode(10.0));
        assign.execute(env);

        assertEquals(10.0, env.get("x"),
            "After executing AssignInstruction, env.get('x') should return 10.0");
    }

    @Test
    void testAssignInstruction_overwritesPreviousValue() {
        env.set("y", 1.0);
        AssignInstruction assign = new AssignInstruction("y", new NumberNode(99.0));
        assign.execute(env);

        assertEquals(99.0, env.get("y"),
            "AssignInstruction should overwrite the previous value of 'y'");
    }

    @Test
    void testAssignInstruction_withExpression() {
        // set z = 3 + 4  → should store 7.0
        BinaryOpNode expr = new BinaryOpNode(new NumberNode(3), "+", new NumberNode(4));
        AssignInstruction assign = new AssignInstruction("z", expr);
        assign.execute(env);

        assertEquals(7.0, env.get("z"),
            "AssignInstruction with BinaryOpNode '3 + 4' should store 7.0");
    }

    @Test
    void testPrintInstruction_doesNotThrow() {
        // show "hello" — just verify it executes without exception
        PrintInstruction print = new PrintInstruction(new StringNode("hello"));
        assertDoesNotThrow(() -> print.execute(env),
            "PrintInstruction should execute without throwing");
    }

    @Test
    void testPrintInstruction_numericExpression_doesNotThrow() {
        // show 2 + 3 — evaluates the expression before printing
        PrintInstruction print = new PrintInstruction(
            new BinaryOpNode(new NumberNode(2), "+", new NumberNode(3)));
        assertDoesNotThrow(() -> print.execute(env),
            "PrintInstruction on arithmetic expression should execute without throwing");
    }
}