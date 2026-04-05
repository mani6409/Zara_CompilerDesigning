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
import java.util.Stack;

public class Tokenizer {
    private final String source;

    public Tokenizer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        String[] lines = source.split("\n", -1);
        int lineNum = 1;

        Stack<Integer> indentStack = new Stack<>(); // UPDATED
        indentStack.push(0);

        for (String rawLine : lines) {
            // Count leading spaces for indentation
            int indent = 0;
            int index = 0;
            while (index < rawLine.length()) {
                char c = rawLine.charAt(index);
                if (c == ' ')
                    indent++;
                else if (c == '\t')
                    indent += 4;
                else
                    break;
                index++;
            }

            String trimmed = rawLine.strip();

            // Skip blank lines and comment lines
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                lineNum++;
                continue;
            }

            // UPDATED: INDENT / DEDENT LOGIC
            int currentIndent = indentStack.peek();

            if (indent > currentIndent) {
                indentStack.push(indent);
                tokens.add(new Token(TokenType.INDENT, "", lineNum)); // UPDATED
            } else if (indent < currentIndent) {
                while (indentStack.size() > 0 && indent < indentStack.peek()) {
                    indentStack.pop();
                    tokens.add(new Token(TokenType.DEDENT, "", lineNum)); // UPDATED
                }

                if (indentStack.peek() != indent) {
                    throw new RuntimeException("Invalid indentation at line " + lineNum); // UPDATED
                }
            }

            // // Emit INDENT token so parser knows this line's depth
            // tokens.add(new Token(TokenType.INDENT, String.valueOf(indent), lineNum));

            // Tokenize the content of this line
            int i = 0;
            while (i < trimmed.length()) {
                char c = trimmed.charAt(i);

                if (c == ' ' || c == '\t') {
                    i++;
                    continue;
                }
                if (c == '#')
                    break; // inline comment

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
                    while (i < trimmed.length() && trimmed.charAt(i) != '"')
                        i++;
                    if (i >= trimmed.length()) { // error handling
                        throw new RuntimeException("Unterminated string at line " + lineNum);
                    }
                    tokens.add(new Token(TokenType.STRING, trimmed.substring(start, i), lineNum));
                    i++;
                    continue;
                }

                // Word
                if (Character.isLetter(c) || c == '_') {
                    int start = i;
                    while (i < trimmed.length()
                            && (Character.isLetterOrDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '_'))
                        i++;
                    String word = trimmed.substring(start, i);
                    TokenType type = switch (word) {
                        case "set" -> TokenType.SET;
                        case "show" -> TokenType.SHOW;
                        case "when" -> TokenType.WHEN;
                        case "loop" -> TokenType.LOOP;
                        default -> TokenType.IDENTIFIER;
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
                    case '+' -> tokens.add(new Token(TokenType.PLUS, "+", lineNum));
                    case '-' -> tokens.add(new Token(TokenType.MINUS, "-", lineNum));
                    case '*' -> tokens.add(new Token(TokenType.STAR, "*", lineNum));
                    case '/' -> tokens.add(new Token(TokenType.SLASH, "/", lineNum));
                    case '>' -> tokens.add(new Token(TokenType.GREATER, ">", lineNum));
                    case '<' -> tokens.add(new Token(TokenType.LESS, "<", lineNum));
                    case '=' -> tokens.add(new Token(TokenType.EQUALS, "=", lineNum));
                    case ':' -> tokens.add(new Token(TokenType.COLON, ":", lineNum));
                    default -> {
                        /* ignore */ }
                }
                i++;
            }

            tokens.add(new Token(TokenType.NEWLINE, "", lineNum));
            lineNum++;
        }

        // Close remaining indents
        while (indentStack.size() > 1) {
            indentStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", lineNum));
        }

        tokens.add(new Token(TokenType.EOF, "", lineNum));
        return tokens;
    }
}
