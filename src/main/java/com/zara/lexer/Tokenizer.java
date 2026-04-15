package com.zara.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// This class is responsible for converting raw source code (string)
// into a list of tokens (Token objects).
public class Tokenizer {

    // The input source code as a single string
    private final String source;

    // Constructor to initialize the tokenizer with source code
    public Tokenizer(String source) {
        this.source = source;
    }

    // Main method that performs tokenization
    public List<Token> tokenize() {

        // List to store all generated tokens
        List<Token> tokens = new ArrayList<>();

        // Split the source code into lines (keeping empty lines as well)
        String[] lines = source.split("\n", -1);

        // Line number tracker (used for error reporting and debugging)
        int lineNum = 1;

        // Stack to keep track of indentation levels (like Python)
        Stack<Integer> indentStack = new Stack<>();
        indentStack.push(0); // Base indentation level

        // Process each line one by one
        for (String rawLine : lines) {

            // ===== Step 1: Count indentation =====

            int indent = 0;  // number of spaces (or tabs converted to spaces)
            int index = 0;

            // Count leading spaces/tabs to determine indentation level
            while (index < rawLine.length()) {
                char c = rawLine.charAt(index);

                if (c == ' ')
                    indent++;        // each space = 1 indent
                else if (c == '\t')
                    indent += 4;     // each tab = 4 spaces
                else
                    break;           // stop when actual content starts

                index++;
            }

            // Remove leading and trailing spaces
            String trimmed = rawLine.strip();

            // ===== Step 2: Skip empty lines or full-line comments =====

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                lineNum++;  // move to next line
                continue;   // skip processing this line
            }

            // ===== Step 3: Handle INDENT / DEDENT (block structure) =====

            int currentIndent = indentStack.peek(); // last indentation level

            // Case 1: Increased indentation → new block starts
            if (indent > currentIndent) {
                indentStack.push(indent);
                tokens.add(new Token(TokenType.INDENT, "", lineNum));
            }

            // Case 2: Decreased indentation → block ends
            else if (indent < currentIndent) {

                // Keep popping until we match correct indentation
                while (indentStack.size() > 0 && indent < indentStack.peek()) {
                    indentStack.pop();
                    tokens.add(new Token(TokenType.DEDENT, "", lineNum));
                }

                // If indentation doesn't match any previous level → error
                if (indentStack.peek() != indent) {
                    throw new RuntimeException("Invalid indentation at line " + lineNum);
                }
            }

            // ===== Step 4: Tokenize actual content of the line =====

            int i = 0;

            while (i < trimmed.length()) {

                char c = trimmed.charAt(i);

                // Skip spaces/tabs inside the line
                if (c == ' ' || c == '\t') {
                    i++;
                    continue;
                }

                // Stop processing if inline comment starts
                if (c == '#')
                    break;

                // ===== Case 1: Number (integer or decimal) =====
                if (Character.isDigit(c)) {
                    int start = i;

                    // Continue while digit or decimal point
                    while (i < trimmed.length() &&
                           (Character.isDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '.'))
                        i++;

                    // Create NUMBER token
                    tokens.add(new Token(TokenType.NUMBER,
                            trimmed.substring(start, i), lineNum));
                    continue;
                }

                // ===== Case 2: String literal =====
                if (c == '"') {
                    i++; // skip opening quote
                    int start = i;

                    // Find closing quote
                    while (i < trimmed.length() && trimmed.charAt(i) != '"')
                        i++;

                    // If closing quote not found → error
                    if (i >= trimmed.length()) {
                        throw new RuntimeException("Unterminated string at line " + lineNum);
                    }

                    // Extract string content (without quotes)
                    tokens.add(new Token(TokenType.STRING,
                            trimmed.substring(start, i), lineNum));

                    i++; // skip closing quote
                    continue;
                }

                // ===== Case 3: Word (identifier or keyword) =====
                if (Character.isLetter(c) || c == '_') {
                    int start = i;

                    // Continue while valid identifier characters
                    while (i < trimmed.length() &&
                           (Character.isLetterOrDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '_'))
                        i++;

                    String word = trimmed.substring(start, i);

                    // Check if word is a keyword or identifier
                    TokenType type = switch (word) {
                        case "set"       -> TokenType.SET;
                        case "show"      -> TokenType.SHOW;
                        case "when"      -> TokenType.WHEN;
                        case "loop"      -> TokenType.LOOP;
                        case "otherwise" -> TokenType.OTHERWISE;
                        default          -> TokenType.IDENTIFIER;
                    };

                    tokens.add(new Token(type, word, lineNum));
                    continue;
                }

                // ===== Case 4: Multi-character operators =====
                if (i + 1 < trimmed.length()) {
                    String two = "" + c + trimmed.charAt(i + 1);

                    switch (two) {
                        case "!=" -> {
                            tokens.add(new Token(TokenType.NOT_EQ, "!=", lineNum));
                            i += 2;
                            continue;
                        }
                        case "<=" -> {
                            tokens.add(new Token(TokenType.LESS_EQ, "<=", lineNum));
                            i += 2;
                            continue;
                        }
                        case ">=" -> {
                            tokens.add(new Token(TokenType.GREATER_EQ, ">=", lineNum));
                            i += 2;
                            continue;
                        }
                        case "==" -> {
                            tokens.add(new Token(TokenType.EQEQ, "==", lineNum));
                            i += 2;
                            continue;
                        }
                    }
                }

                // ===== Case 5: Single-character symbols =====
                switch (c) {
                    case '+' -> tokens.add(new Token(TokenType.PLUS, "+", lineNum));
                    case '-' -> tokens.add(new Token(TokenType.MINUS, "-", lineNum));
                    case '*' -> tokens.add(new Token(TokenType.STAR, "*", lineNum));
                    case '/' -> tokens.add(new Token(TokenType.SLASH, "/", lineNum));
                    case '>' -> tokens.add(new Token(TokenType.GREATER, ">", lineNum));
                    case '<' -> tokens.add(new Token(TokenType.LESS, "<", lineNum));
                    case '=' -> tokens.add(new Token(TokenType.EQUALS, "=", lineNum));
                    case ':' -> tokens.add(new Token(TokenType.COLON, ":", lineNum));

                    // Ignore unknown characters (can improve later)
                    default -> { /* ignore */ }
                }

                i++; // move to next character
            }

            // Add NEWLINE token after each processed line
            tokens.add(new Token(TokenType.NEWLINE, "", lineNum));

            lineNum++; // move to next line
        }

        // ===== Step 5: Close remaining open indentation blocks =====
        while (indentStack.size() > 1) {
            indentStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", lineNum));
        }

        // ===== Step 6: Add EOF token (end of file) =====
        tokens.add(new Token(TokenType.EOF, "", lineNum));

        return tokens; // return final list of tokens
    }
}
