import org.junit.Test;
import static org.junit.Assert.*;

public class EnvironmentTest {
    @Test
    public void testVariableStorageAndRetrieval() {
        Environment env = new Environment();
        env.storeVariable("x", 10);
        env.storeVariable("y", 20);

        assertEquals(10, env.retrieveVariable("x"));
        assertEquals(20, env.retrieveVariable("y"));
        assertEquals(null, env.retrieveVariable("z")); // Check retrieval of a non-existent variable
    }
}