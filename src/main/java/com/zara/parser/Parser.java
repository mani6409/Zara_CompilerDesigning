package com.zara.parser;

import java.util.ArrayList;
import java.util.List;
import com.zara.lexer.Token;
import com.zara.lexer.TokenType;
import com.zara.parser.ast.BinaryOpNode;
import com.zara.parser.ast.Expression;
import com.zara.parser.ast.NumberNode;
import com.zara.parser.ast.StringNode;
import com.zara.parser.ast.VariableNode;
import com.zara.interpreter.instruction.AssignInstruction;
import com.zara.interpreter.instruction.IfInstruction;
import com.zara.interpreter.instruction.Instruction;
import com.zara.interpreter.instruction.PrintInstruction;
import com.zara.interpreter.instruction.RepeatInstruction;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token consume() {
        return tokens.get(pos++);
    }

    private boolean check(TokenType t) {
        return peek().getType() == t;
    }

    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            if (check(TokenType.NEWLINE)) {
                consume();
                continue;
            }
            if (check(TokenType.INDENT))
                consume();
            if (check(TokenType.EOF))
                break;
            instructions.add(parseInstruction());
            if (check(TokenType.NEWLINE))
                consume();
        }
        return instructions;
    }

    private Instruction parseInstruction() {
        Token t = peek();
        return switch (t.getType()) {
            case SET -> parseAssign();
            case SHOW -> parsePrint();
            case WHEN -> parseIf();
            case LOOP -> parseRepeat();
            default -> throw new RuntimeException(
                    "Unexpected token '" + t.getValue() + "' on line " + t.getLine());
        };
    }

    private Instruction parseAssign() {
        consume();
        String name = consume().getValue();
        consume();
        return new AssignInstruction(name, parseExpression());
    }

    private Instruction parsePrint() {
        consume();
        return new PrintInstruction(parseExpression());
    }

    private Instruction parseIf() {
        consume();
        Expression condition = parseExpression();
        consume();

        if (check(TokenType.NEWLINE))
            consume();

        List<Instruction> thenBody = parseBlock();

        List<Instruction> elseBody = new ArrayList<>();

        if (check(TokenType.OTHERWISE)) {
            consume();
            consume();

            if (check(TokenType.NEWLINE))
                consume();

            elseBody = parseBlock();
        }

        return new IfInstruction(condition, thenBody, elseBody);
    }

    private Instruction parseRepeat() {
        consume();
        double raw = Double.parseDouble(consume().getValue());

        if (raw != Math.floor(raw) || raw < 0)
            throw new RuntimeException(
                    "loop count must be a non-negative integer, got: " + raw);

        int count = (int) raw;

        consume();

        if (check(TokenType.NEWLINE))
            consume();

        return new RepeatInstruction(count, parseBlock());
    }

    /*
     * FIXED BLOCK PARSER
     * Uses INDENT and DEDENT tokens structurally
     */
    private List<Instruction> parseBlock() {

        List<Instruction> body = new ArrayList<>();

        if (!check(TokenType.INDENT)) {
            return body;
        }

        consume(); // consume INDENT

        while (!check(TokenType.EOF) && !check(TokenType.DEDENT)) {

            if (check(TokenType.NEWLINE)) {
                consume();
                continue;
            }

            body.add(parseInstruction());

            if (check(TokenType.NEWLINE)) {
                consume();
            }
        }

        if (check(TokenType.DEDENT)) {
            consume(); // consume DEDENT
        }

        return body;
    }

    private Expression parseExpression() {
        return parseComparison();
    }

    private Expression parseComparison() {
        Expression left = parseAddSub();

        if (check(TokenType.GREATER) || check(TokenType.LESS) ||
                check(TokenType.EQEQ) || check(TokenType.NOT_EQ) ||
                check(TokenType.LESS_EQ) || check(TokenType.GREATER_EQ)) {

            String op = consume().getValue();
            left = new BinaryOpNode(left, op, parseAddSub());
        }

        return left;
    }

    private Expression parseAddSub() {
        Expression left = parseTerm();

        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = consume().getValue();
            left = new BinaryOpNode(left, op, parseTerm());
        }

        return left;
    }

    private Expression parseTerm() {
        Expression left = parsePrimary();

        while (check(TokenType.STAR) || check(TokenType.SLASH)) {
            String op = consume().getValue();
            left = new BinaryOpNode(left, op, parsePrimary());
        }

        return left;
    }

    private Expression parsePrimary() {
        Token t = consume();

        return switch (t.getType()) {
            case NUMBER -> new NumberNode(Double.parseDouble(t.getValue()));
            case STRING -> new StringNode(t.getValue());
            case IDENTIFIER -> new VariableNode(t.getValue());

            default -> throw new RuntimeException(
                    "Expected a value but got '" + t.getValue() +
                            "' on line " + t.getLine());
        };
    }
}