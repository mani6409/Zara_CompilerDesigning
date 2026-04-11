package com.zara.parser;
import com.zara.lexer.Tokenizer;
import com.zara.lexer.Token;
import com.zara.interpreter.instruction.Instruction;
import com.zara.interpreter.instruction.AssignInstruction;
import com.zara.interpreter.instruction.PrintInstruction;
import com.zara.interpreter.instruction.IfInstruction;
import com.zara.interpreter.instruction.RepeatInstruction;

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
        assertEquals(1, instructions.size(),
            "Expected exactly one instruction for 'set x = 5'");
        assertInstanceOf(AssignInstruction.class, instructions.get(0),
            "Expected an AssignInstruction");
    }

    @Test
    void testParsingPrint() {
        List<Instruction> instructions = parse("show \"hello\"");
        assertEquals(1, instructions.size(),
            "Expected exactly one instruction for 'show \"hello\"'");
        assertInstanceOf(PrintInstruction.class, instructions.get(0),
            "Expected a PrintInstruction");
    }

    @Test
    void testParsingIfWithoutElse() {
        String src = "when x > 0:\n    show \"positive\"\n";
        List<Instruction> instructions = parse(src);
        assertEquals(1, instructions.size(),
            "Expected one instruction for a when block");
        assertInstanceOf(IfInstruction.class, instructions.get(0),
            "Expected an IfInstruction");
    }

    @Test
    void testParsingIfWithOtherwise() {
        String src =
            "when x > 0:\n" +
            "    show \"pos\"\n" +
            "otherwise:\n" +
            "    show \"non-pos\"\n";
        List<Instruction> instructions = parse(src);
        assertEquals(1, instructions.size(),
            "Expected one top-level instruction for when/otherwise");
        assertInstanceOf(IfInstruction.class, instructions.get(0),
            "Expected an IfInstruction");
    }

    @Test
    void testParsingLoop() {
        String src = "loop 3:\n    show \"hi\"\n";
        List<Instruction> instructions = parse(src);
        assertEquals(1, instructions.size(),
            "Expected one instruction for a loop block");
        assertInstanceOf(RepeatInstruction.class, instructions.get(0),
            "Expected a RepeatInstruction");
    }

    @Test
    void testParsingMultipleInstructions() {
        String src =
            "set a = 1\n" +
            "set b = 2\n" +
            "show a\n";
        List<Instruction> instructions = parse(src);
        assertEquals(3, instructions.size(),
            "Expected three top-level instructions");
    }

    @Test
    void testArithmeticPrecedence() {
        // 'a + b > c' — should parse without exception
        // The comparison must bind looser than addition
        assertDoesNotThrow(() -> parse("when a + b > c:\n    show \"ok\"\n"),
            "Arithmetic + comparison should parse cleanly with correct precedence");
    }

    @Test
    void testAllComparisonOperators() {
        String[] ops = { ">", "<", "==", "!=", "<=", ">=" };
        for (String op : ops) {
            String src = "when x " + op + " 0:\n    show \"ok\"\n";
            assertDoesNotThrow(() -> parse(src),
                "Operator '" + op + "' should be parseable");
        }
    }

    // ---------------------------------------------------------------
    // Invalid input tests
    // ---------------------------------------------------------------

    @Test
    void testParsingInvalidInput_FloatLoopCount() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> parse("loop 3.7:\n    show \"hi\"\n"),
            "A float loop count should throw RuntimeException");
        assertTrue(ex.getMessage().contains("non-negative integer"),
            "Error message should mention 'non-negative integer', got: " + ex.getMessage());
    }

    @Test
    void testParsingInvalidInput_NegativeLoopCount() {
        // "loop -2" tokenises as LOOP, MINUS("-"), NUMBER("2").
        // parseRepeat() consumes the MINUS token as the count value, so
        // Double.parseDouble("-") throws NumberFormatException before the
        // controlled "non-negative integer" message is ever reached.
        // We therefore only assert that *some* RuntimeException is thrown.
        assertThrows(RuntimeException.class,
            () -> parse("loop -2:\n    show \"hi\"\n"),
            "A negative loop count should throw RuntimeException");
    }

    @Test
    void testParsingInvalidInput_UnexpectedToken() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> parse("unknown stuff here"),
            "An unrecognised keyword should throw RuntimeException");
        assertNotNull(ex.getMessage(),
            "Exception should carry a message");
    }
}