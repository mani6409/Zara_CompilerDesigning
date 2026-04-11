package com.zara.lexer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class LexerTest {
    
    @Test
    public void testValidTokens() {
        // Example test for valid tokens using Zara's syntax
        Tokenizer tokenizer = new Tokenizer("set a = 10");
        List<Token> tokens = tokenizer.tokenize();
        assertEquals(TokenType.SET, tokens.get(0).getType(), "First token should be SET");
    }

    @Test
    public void testWhitespaceHandling() {
        // Example test for inline whitespace handling
        Tokenizer tokenizer = new Tokenizer("set   b  =  20");
        List<Token> tokens = tokenizer.tokenize();
        assertEquals(TokenType.SET, tokens.get(0).getType(), "First token should be SET");
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType(), "Second token should be IDENTIFIER");
        assertEquals(TokenType.EQUALS, tokens.get(2).getType(), "Third token should be EQUALS");
    }

    @Test
    public void testCommentHandling() {
        // Example test for comments (Zara uses # for inline comments)
        Tokenizer tokenizer = new Tokenizer("# This is a comment\nset c = 30");
        List<Token> tokens = tokenizer.tokenize();
        // The comment line is skipped. The first token will be from the next line.
        assertEquals(TokenType.SET, tokens.get(0).getType(), "First token should be SET since the comment was skipped");
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType(), "Second token should be IDENTIFIER");
    }
}