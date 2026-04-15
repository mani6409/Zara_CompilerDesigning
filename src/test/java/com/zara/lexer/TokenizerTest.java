package com.zara.lexer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class TokenizerTest {

    @Test
    void testTokenizeSimpleAssignment() {
        // "set x = 10" → SET, IDENTIFIER, EQUALS, NUMBER, NEWLINE, EOF
        String input = "set x = 10";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenize();

        assertFalse(tokens.isEmpty());
        assertEquals(TokenType.SET,        tokens.get(0).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals(TokenType.EQUALS,     tokens.get(2).getType());
        assertEquals(TokenType.NUMBER,     tokens.get(3).getType());
    }

    // ✅ Strict version from your branch (important!)
    @Test
    void testTokenizeFullSequence() {
        String input = "set x = 10";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenize();

        assertEquals(6, tokens.size());
        assertEquals(TokenType.SET, tokens.get(0).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals(TokenType.EQUALS, tokens.get(2).getType());
        assertEquals(TokenType.NUMBER, tokens.get(3).getType());
        assertEquals(TokenType.NEWLINE, tokens.get(4).getType());
        assertEquals(TokenType.EOF, tokens.get(5).getType());
    }

    @Test
    void testTokenizeNumber() {
        String input = "42";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenize();

        assertEquals(TokenType.NUMBER, tokens.get(0).getType());
        assertEquals("42", tokens.get(0).getValue());
    }

    @Test
    void testTokenizeIdentifier() {
        String input = "myVar";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenize();

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("myVar", tokens.get(0).getValue());
    }

    @Test
    void testTokenizeStringLiteral() {
        String input = "\"hello\"";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenize();

        assertEquals(TokenType.STRING, tokens.get(0).getType());
        assertEquals("hello", tokens.get(0).getValue());
    }

    @Test
    void testTokenizeEndsWithEOF() {
        String input = "set x = 1";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenize();

        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType());
    }

    @Test
    void testUnterminatedStringThrowsException() {
        String input = "\"hello";
        Tokenizer tokenizer = new Tokenizer(input);

        assertThrows(RuntimeException.class, tokenizer::tokenize);
    }
}