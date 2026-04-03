// LexerTest.java

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LexerTest {
    @Test
    public void testValidTokens() {
        // Example test for valid tokens
        Lexer lexer = new Lexer("int a = 10;");
        assertEquals(Token.Type.INT, lexer.nextToken().getType());
    }

    @Test
    public void testInvalidToken() {
        // Example test for invalid tokens
        Lexer lexer = new Lexer("int @a = 10;");
        assertEquals(Token.Type.ERROR, lexer.nextToken().getType());
    }

    @Test
    public void testWhitespaceHandling() {
        // Example test for whitespace handling
        Lexer lexer = new Lexer("   int b = 20;");
        lexer.nextToken(); // Should skip whitespaces
        assertEquals(Token.Type.INT, lexer.nextToken().getType());
    }

    @Test
    public void testCommentHandling() {
        // Example test for comments
        Lexer lexer = new Lexer("// This is a comment\nint c = 30;");
        lexer.nextToken(); // Skip comment
        assertEquals(Token.Type.INT, lexer.nextToken().getType());
    }

    // Add more tests as necessary
}