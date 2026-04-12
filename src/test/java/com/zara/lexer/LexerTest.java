package com.zara.lexer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class LexerTest {

    @Test
    public void testValidTokens() {
        // "set a = 10" → first token should be SET keyword
        Tokenizer tokenizer = new Tokenizer("set a = 10");
        List<Token> tokens = tokenizer.tokenize();

        assertFalse(tokens.isEmpty());
        assertEquals(TokenType.SET, tokens.get(0).getType());
    }

    @Test
    public void testInvalidCharacterIsIgnored() {
        // The tokenizer silently ignores unknown characters like '@'
        // So "set @a = 10" should still produce a SET token first
        Tokenizer tokenizer = new Tokenizer("set @a = 10");
        List<Token> tokens = tokenizer.tokenize();

        assertEquals(TokenType.SET, tokens.get(0).getType());
    }

    @Test
    public void testWhitespaceHandling() {
        // Leading/trailing spaces should be stripped; first real token is SET
        Tokenizer tokenizer = new Tokenizer("   set b = 20");
        List<Token> tokens = tokenizer.tokenize();

        assertEquals(TokenType.SET, tokens.get(0).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals("b", tokens.get(1).getValue());
    }

    @Test
    public void testCommentHandling() {
        // Lines starting with '#' are skipped entirely
        String input = "# This is a comment\nset c = 30";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenize();

        // First real token should be SET (comment line skipped)
        assertEquals(TokenType.SET, tokens.get(0).getType());
    }

    @Test
    public void testArithmeticOperators() {
        Tokenizer tokenizer = new Tokenizer("set x = 5 + 3");
        List<Token> tokens = tokenizer.tokenize();

        // SET, IDENTIFIER(x), EQUALS, NUMBER(5), PLUS, NUMBER(3)
        assertEquals(TokenType.PLUS, tokens.get(4).getType());
    }

    @Test
    public void testComparisonOperators() {
        Tokenizer tokenizer = new Tokenizer("set x = 5 <= 10");
        List<Token> tokens = tokenizer.tokenize();

        // SET, IDENTIFIER(x), EQUALS, NUMBER(5), LESS_EQ, NUMBER(10)
        assertEquals(TokenType.LESS_EQ, tokens.get(4).getType());
    }
}