package com.zara.parser;

import com.zara.lexer.Tokenizer;
import com.zara.lexer.Token;

import com.zara.interpreter.instruction.Instruction;
import com.zara.interpreter.instruction.AssignInstruction;
import com.zara.interpreter.instruction.PrintInstruction;
import com.zara.interpreter.instruction.IfInstruction;
import com.zara.interpreter.instruction.RepeatInstruction;

import com.zara.runtime.Environment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ParserTest {

    /** Helper: tokenize + parse a Zara source snippet. */
    private List<Instruction> parse(String source) {
        List<Token> tokens = new Tokenizer(source).tokenize();
        return new Parser(tokens).parse();
    }

    // ---------------------------------------------------------------
    // Valid input tests
    // ---------------------------------------------------------------

    @Test
    void testParsingAssignment() {
        List<Instruction> instructions = parse("set x = 5");

        assertEquals(1, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));

        // Execute instruction and verify environment
        Environment env = new Environment();
        instructions.get(0).execute(env);

        assertEquals(5.0, env.get("x"));
    }

    @Test
    void testParsingPrint() {
        List<Instruction> instructions = parse("show \"hello\"");

        assertEquals(1, instructions.size());
        assertInstanceOf(PrintInstruction.class, instructions.get(0));
    }

    @Test
    void testParsingIfWithoutElse() {
        String src =
                "set x = 5\n" +
                "when x > 0:\n" +
                "    show \"positive\"\n";

        List<Instruction> instructions = parse(src);

        assertEquals(2, instructions.size());
        assertInstanceOf(IfInstruction.class, instructions.get(1));
    }

    @Test
    void testParsingIfWithOtherwise() {
        String src =
                "set x = -1\n" +
                "when x > 0:\n" +
                "    show \"pos\"\n" +
                "otherwise:\n" +
                "    show \"non-pos\"\n";

        List<Instruction> instructions = parse(src);

        assertEquals(2, instructions.size());
        assertInstanceOf(IfInstruction.class, instructions.get(1));
    }

    @Test
    void testParsingLoop() {
        String src =
                "loop 3:\n" +
                "    show \"hi\"\n";

        List<Instruction> instructions = parse(src);

        assertEquals(1, instructions.size());
        assertInstanceOf(RepeatInstruction.class, instructions.get(0));
    }

    @Test
    void testParsingMultipleInstructions() {
        String src =
                "set a = 1\n" +
                "set b = 2\n" +
                "show a\n";

        List<Instruction> instructions = parse(src);

        assertEquals(3, instructions.size());
    }

    @Test
    void testArithmeticPrecedence() {
        assertDoesNotThrow(() ->
                parse("when a + b > c:\n    show \"ok\"\n")
        );
    }

    @Test
    void testAllComparisonOperators() {

        String[] ops = { ">", "<", "==", "!=", "<=", ">=" };

        for (String op : ops) {

            String src =
                    "when x " + op + " 0:\n" +
                    "    show \"ok\"\n";

            assertDoesNotThrow(() -> parse(src));
        }
    }

    // ---------------------------------------------------------------
    // Invalid input tests
    // ---------------------------------------------------------------

    @Test
    void testParsingInvalidInput_FloatLoopCount() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> parse("loop 3.7:\n    show \"hi\"\n")
        );

        assertTrue(ex.getMessage().contains("non-negative integer"));
    }

    @Test
    void testParsingInvalidInput_NegativeLoopCount() {

        assertThrows(
                RuntimeException.class,
                () -> parse("loop -2:\n    show \"hi\"\n")
        );
    }

    @Test
    void testParsingInvalidInput_UnexpectedToken() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> parse("unknown stuff here")
        );

        assertNotNull(ex.getMessage());
    }
}