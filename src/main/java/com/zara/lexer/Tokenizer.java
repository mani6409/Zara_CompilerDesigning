package com.zara.lexer;

import java.util.*;
import com.zara.lexer.*;
import com.zara.parser.*;
import com.zara.parser.ast.*;
import com.zara.interpreter.*;
import com.zara.interpreter.instruction.*;
import com.zara.runtime.*;
import com.zara.utils.*;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private final String source;

    public Tokenizer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        String[] lines = source.split("\n", -1);
        int lineNum = 1;

        for (String rawLine : lines) {
            // Count leading spaces for indentation
            int indent = 0;
            for (int k = 0; k < rawLine.length(); k++) {
                char c = rawLine.charAt(k);
                if (c == ' ')       indent++;
                else if (c == '\t') indent += 4;
                else break;
            }

            String trimmed = rawLine.strip();

            // Skip blank lines and comment lines
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                lineNum++;
                continue;
            }

            // Emit INDENT token so parser knows this line's depth
            tokens.add(new Token(TokenType.INDENT, String.valueOf(indent), lineNum));

            // Tokenize the content of this line
            int i = 0;
            while (i < trimmed.length()) {
                char c = trimmed.charAt(i);

                if (c == ' ' || c == '\t') { i++; continue; }
                if (c == '#') break; // inline comment

                // Number
                if (Character.isDigit(c)) {
                    int start = i;
                    while (i < trimmed.length() && (Character.isDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '.'))
                        i++;
                    tokens.add(new Token(TokenType.NUMBER, trimmed.substring(start, i), lineNum));
                    continue;
                }

                // String literal
                if (c == '"') {
                    i++;
                    int start = i;
                    while (i < trimmed.length() && trimmed.charAt(i) != '"') i++;
                    tokens.add(new Token(TokenType.STRING, trimmed.substring(start, i), lineNum));
                    i++;
                    continue;
                }

                // Word
                if (Character.isLetter(c) || c == '_') {
                    int start = i;
                    while (i < trimmed.length() && (Character.isLetterOrDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '_'))
                        i++;
                    String word = trimmed.substring(start, i);
                    TokenType type = switch (word) {
                        case "set"  -> TokenType.SET;
                        case "show" -> TokenType.SHOW;
                        case "when" -> TokenType.WHEN;
                        case "loop" -> TokenType.LOOP;
                        default     -> TokenType.IDENTIFIER;
                    };
                    tokens.add(new Token(type, word, lineNum));
                    continue;
                }

                // Symbols
                if (c == '=' && i + 1 < trimmed.length() && trimmed.charAt(i + 1) == '=') {
                    tokens.add(new Token(TokenType.EQEQ, "==", lineNum));
                    i += 2;
                    continue;
                }

                switch (c) {
                    case '+' -> tokens.add(new Token(TokenType.PLUS,    "+", lineNum));
                    case '-' -> tokens.add(new Token(TokenType.MINUS,   "-", lineNum));
                    case '*' -> tokens.add(new Token(TokenType.STAR,    "*", lineNum));
                    case '/' -> tokens.add(new Token(TokenType.SLASH,   "/", lineNum));
                    case '>' -> tokens.add(new Token(TokenType.GREATER, ">", lineNum));
                    case '<' -> tokens.add(new Token(TokenType.LESS,    "<", lineNum));
                    case '=' -> tokens.add(new Token(TokenType.EQUALS,  "=", lineNum));
                    case ':' -> tokens.add(new Token(TokenType.COLON,   ":", lineNum));
                    default  -> { /* ignore */ }
                }
                i++;
            }

            tokens.add(new Token(TokenType.NEWLINE, "\\n", lineNum));
            lineNum++;
        }

        tokens.add(new Token(TokenType.EOF, "", lineNum));
        return tokens;
    }
}
