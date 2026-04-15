package com.zara.interpreter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnvironmentTest {
    @Test
    public void testVariableStorageAndRetrieval() {
        Environment env = new Environment();
        env.set("x", 10);
        env.set("y", 20);

        assertEquals(10, env.get("x"));
        assertEquals(20, env.get("y"));
    }

    @Test
    public void testRetrieveNonExistentVariable() {
        Environment env = new Environment();
        assertThrows(RuntimeException.class, () -> {
            env.get("z");
        });
    }
}