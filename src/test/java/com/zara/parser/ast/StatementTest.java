import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatementTest {
    @Test
    void testStatementNode() {
        // Create a Statement Node
        StatementNode statement = new StatementNode();

        // Test initial state of statement
        assertNotNull(statement);
        assertFalse(statement.isExecuted());
    }
    
    @Test
    void testExecuteStatement() {
        StatementNode statement = new StatementNode();
        statement.execute();

        assertTrue(statement.isExecuted());
    }
}