package com.zara.lexer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

class TokenizerTest {

    @Test
    void testTokenize() {
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
}