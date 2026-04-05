package com.zara.parser;

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

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() { return tokens.get(pos); }
    private Token consume() { return tokens.get(pos++); }
    private boolean check(TokenType t) { return peek().getType() == t; }

    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            if (check(TokenType.NEWLINE)) { consume(); continue; }
            if (check(TokenType.INDENT)) consume(); // consume top-level indent (always 0)
            if (check(TokenType.EOF)) break;
            instructions.add(parseInstruction());
            if (check(TokenType.NEWLINE)) consume();
        }
        return instructions;
    }

    private Instruction parseInstruction() {
        Token t = peek();
        return switch (t.getType()) {
            case SET  -> parseAssign();
            case SHOW -> parsePrint();
            case WHEN -> parseIf();
            case LOOP -> parseRepeat();
            default   -> throw new RuntimeException(
                "Unexpected token '" + t.getValue() + "' on line " + t.getLine()
            );
        };
    }

    // set x = expr
    private Instruction parseAssign() {
        consume(); // set
        String name = consume().getValue(); // variable name
        consume(); // =
        return new AssignInstruction(name, parseExpression());
    }

    // show expr
    private Instruction parsePrint() {
        consume(); // show
        return new PrintInstruction(parseExpression());
    }

    // when condition:
    //     indented body
    private Instruction parseIf() {
        consume(); // when
        Expression condition = parseExpression();
        consume(); // :
        if (check(TokenType.NEWLINE)) consume();
        return new IfInstruction(condition, parseBlock());
    }

    // loop N:
    //     indented body
    private Instruction parseRepeat() {
        consume(); // loop
        int count = (int) Double.parseDouble(consume().getValue());
        consume(); // :
        if (check(TokenType.NEWLINE)) consume();
        return new RepeatInstruction(count, parseBlock());
    }

    // Parse an indented block — entered on INDENT token, exited on DEDENT token
    private List<Instruction> parseBlock() {
        List<Instruction> body = new ArrayList<>();
        if (!check(TokenType.INDENT)) return body;  // no block follows
        consume(); // consume INDENT
        while (!check(TokenType.EOF) && !check(TokenType.DEDENT)) {
            if (check(TokenType.NEWLINE)) { consume(); continue; }
            body.add(parseInstruction());
            if (check(TokenType.NEWLINE)) consume();
        }
        if (check(TokenType.DEDENT)) consume(); // consume DEDENT
        return body;
    }

    // Entry point — delegates to comparison (lowest precedence)
    private Expression parseExpression() {
        return parseComparison();
    }

    // Handles > < == != <= >= (lower precedence than + -)
    private Expression parseComparison() {
        Expression left = parseAddSub();
        if (check(TokenType.GREATER) || check(TokenType.LESS)   ||
            check(TokenType.EQEQ)   || check(TokenType.NOT_EQ)  ||
            check(TokenType.LESS_EQ)|| check(TokenType.GREATER_EQ)) {
            String op = consume().getValue();
            left = new BinaryOpNode(left, op, parseAddSub());
        }
        return left;
    }

    // Handles + - (higher precedence than comparisons)
    private Expression parseAddSub() {
        Expression left = parseTerm();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = consume().getValue();
            left = new BinaryOpNode(left, op, parseTerm());
        }
        return left;
    }

    // Handles * / (highest precedence among binary ops)
    private Expression parseTerm() {
        Expression left = parsePrimary();
        while (check(TokenType.STAR) || check(TokenType.SLASH)) {
            String op = consume().getValue();
            left = new BinaryOpNode(left, op, parsePrimary());
        }
        return left;
    }

    // Handles a single value: number, string, or variable
    private Expression parsePrimary() {
        Token t = consume();
        return switch (t.getType()) {
            case NUMBER     -> new NumberNode(Double.parseDouble(t.getValue()));
            case STRING     -> new StringNode(t.getValue());
            case IDENTIFIER -> new VariableNode(t.getValue());
            default -> throw new RuntimeException(
                "Expected a value but got '" + t.getValue() + "' on line " + t.getLine()
            );
        };
    }
}
