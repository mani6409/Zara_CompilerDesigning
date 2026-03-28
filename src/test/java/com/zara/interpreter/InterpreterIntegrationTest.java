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
        interpreter.execute("x = 5;");
        assertEquals(5, interpreter.getVariable("x"));
    }

    @Test
    public void testArithmeticOperations() {
        interpreter.execute("x = 5;");
        interpreter.execute("y = 10;");
        interpreter.execute("result = x + y;");
        assertEquals(15, interpreter.getVariable("result"));
        interpreter.execute("result = y - x;");
        assertEquals(5, interpreter.getVariable("result"));
        interpreter.execute("result = x * y;");
        assertEquals(50, interpreter.getVariable("result"));
        interpreter.execute("result = y / x;");
        assertEquals(2, interpreter.getVariable("result"));
    }

    @Test
    public void testComparisons() {
        interpreter.execute("x = 5;");
        interpreter.execute("y = 10;");
        interpreter.execute("result = x < y;");
        assertTrue(interpreter.getVariable("result"));
        interpreter.execute("result = x > y;");
        assertFalse(interpreter.getVariable("result"));
        interpreter.execute("result = x == 5;");
        assertTrue(interpreter.getVariable("result"));
    }

    @Test
    public void testLoops() {
        interpreter.execute("sum = 0;");
        interpreter.execute("for (i = 1; i <= 5; i = i + 1) { sum = sum + i; }");
        assertEquals(15, interpreter.getVariable("sum"));
    }

    @Test
    public void testVariableReferences() {
        interpreter.execute("x = 5;");
        interpreter.execute("y = x + 5;");
        assertEquals(10, interpreter.getVariable("y"));
    }
}