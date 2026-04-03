package com.zara.interpreter;

import org.junit.Test;
import static org.junit.Assert.*;

public class EnvironmentTest {
    @Test
    public void testVariableStorageAndRetrieval() {
        Environment env = new Environment();
        env.set("x", 10);
        env.set("y", 20);

        assertEquals(10, env.get("x"));
        assertEquals(20, env.get("y"));
    }

    @Test(expected = RuntimeException.class)
    public void testRetrieveNonExistentVariable() {
        Environment env = new Environment();
        env.get("z");
    }
}