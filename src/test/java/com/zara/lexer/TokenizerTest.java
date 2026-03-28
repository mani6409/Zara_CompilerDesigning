import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TokenizerTest {

    @Test
    void testTokenize() {
        String input = "int x = 10;";
        Tokenizer tokenizer = new Tokenizer(input);
        Token[] tokens = tokenizer.tokenize();

        assertEquals(3, tokens.length);
        assertEquals(TokenType.INT, tokens[0].getType());
        assertEquals(TokenType.IDENTIFIER, tokens[1].getType());
        assertEquals(TokenType.NUMBER, tokens[2].getType());
    }
}