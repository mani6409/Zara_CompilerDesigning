package com.zara.lexer;

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

        Stack<Integer> indentStack = new Stack<>();
        indentStack.push(0);

        for (String rawLine : lines) {

            int indent = 0;
            int index = 0;
            while (index < rawLine.length()) {
                char c = rawLine.charAt(index);
                if (c == ' ') indent++;
                else if (c == '\t') indent += 4;
                else break;
                index++;
            }

            String trimmed = rawLine.strip();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                lineNum++;
                continue;
            }

            int currentIndent = indentStack.peek();

            if (indent > currentIndent) {
                indentStack.push(indent);
                tokens.add(new Token(TokenType.INDENT, "", lineNum));
            } 
            else if (indent < currentIndent) {
                while (indentStack.size() > 1 && indent < indentStack.peek()) {
                    indentStack.pop();
                    tokens.add(new Token(TokenType.DEDENT, "", lineNum));
                }

                if (indentStack.peek() != indent) {
                    throw new LexerException("Invalid indentation", lineNum);
                }
            }

            int i = 0;
            while (i < trimmed.length()) {
                char c = trimmed.charAt(i);

                if (c == ' ' || c == '\t') {
                    i++;
                    continue;
                }

                if (c == '#') break;

                // NUMBER
                if (Character.isDigit(c)) {
                    int start = i;
                    boolean hasDot = false;

                    while (i < trimmed.length()) {
                        char ch = trimmed.charAt(i);
                        if (Character.isDigit(ch)) {
                            i++;
                        } else if (ch == '.') {
                            if (hasDot) {
                                throw new LexerException("Invalid number format", lineNum);
                            }
                            hasDot = true;
                            i++;
                        } else break;
                    }

                    tokens.add(new Token(TokenType.NUMBER, trimmed.substring(start, i), lineNum));
                    continue;
                }

                // STRING
                if (c == '"') {
                    i++;
                    int start = i;

                    while (i < trimmed.length() && trimmed.charAt(i) != '"') {
                        i++;
                    }

                    if (i >= trimmed.length()) {
                        throw new LexerException("Unterminated string", lineNum);
                    }

                    tokens.add(new Token(TokenType.STRING, trimmed.substring(start, i), lineNum));
                    i++;
                    continue;
                }

                // IDENTIFIER / KEYWORDS
                if (Character.isLetter(c) || c == '_') {
                    int start = i;

                    while (i < trimmed.length()
                            && (Character.isLetterOrDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '_')) {
                        i++;
                    }

                    String word = trimmed.substring(start, i);

                    TokenType type = switch (word) {
                        case "set" -> TokenType.SET;
                        case "show" -> TokenType.SHOW;
                        case "when" -> TokenType.WHEN;
                        case "loop" -> TokenType.LOOP;
                        case "true" -> TokenType.TRUE;
                        case "false" -> TokenType.FALSE;
                        default -> TokenType.IDENTIFIER;
                    };

                    tokens.add(new Token(type, word, lineNum));
                    continue;
                }

                // MULTI-CHAR OPERATORS
                if (i + 1 < trimmed.length()) {
                    String two = "" + c + trimmed.charAt(i + 1);

                    switch (two) {
                        case "!=" -> { tokens.add(new Token(TokenType.NOT_EQ, "!=", lineNum)); i += 2; continue; }
                        case "<=" -> { tokens.add(new Token(TokenType.LESS_EQ, "<=", lineNum)); i += 2; continue; }
                        case ">=" -> { tokens.add(new Token(TokenType.GREATER_EQ, ">=", lineNum)); i += 2; continue; }
                        case "==" -> { tokens.add(new Token(TokenType.EQEQ, "==", lineNum)); i += 2; continue; }
                    }
                }

                // SINGLE CHAR TOKENS
                switch (c) {
                    case '+' -> tokens.add(new Token(TokenType.PLUS, "+", lineNum));
                    case '-' -> tokens.add(new Token(TokenType.MINUS, "-", lineNum));
                    case '*' -> tokens.add(new Token(TokenType.STAR, "*", lineNum));
                    case '/' -> tokens.add(new Token(TokenType.SLASH, "/", lineNum));
                    case '>' -> tokens.add(new Token(TokenType.GREATER, ">", lineNum));
                    case '<' -> tokens.add(new Token(TokenType.LESS, "<", lineNum));
                    case '=' -> tokens.add(new Token(TokenType.EQUALS, "=", lineNum));
                    case ':' -> tokens.add(new Token(TokenType.COLON, ":", lineNum));
                    default -> throw new LexerException(
                            "Unexpected character '" + c + "'", lineNum);
                }

                i++;
            }

            tokens.add(new Token(TokenType.NEWLINE, "", lineNum));
            lineNum++;
        }

        while (indentStack.size() > 1) {
            indentStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", lineNum));
        }

        tokens.add(new Token(TokenType.EOF, "", lineNum));
        return tokens;
    }
}