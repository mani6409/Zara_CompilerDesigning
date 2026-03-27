# ZARA Interpreter Documentation

## Overview
Zara is a simple programming language interpreter designed around basic functionalities like variables assignation, conditional statements, and loops.

## Components
*   **Lexer**: Converts plain text code into meaningful Tokens for the interpreter (`TokenType.java`, `Token.java`, `Tokenizer.java`).
*   **Parser**: Takes a list of Tokens and generates an Abstract Syntax Tree (AST) representing operations (`Parser.java` and internal nodes).
*   **Interpreter**: Iterates over AST nodes to execute logic, track variable bindings within its current `Environment.java`, and process runtime values using `Value.java`.
