package com.zara.parser.ast;

import java.util.*;
import com.zara.lexer.*;
import com.zara.parser.*;
import com.zara.parser.ast.*;
import com.zara.interpreter.*;
import com.zara.interpreter.instruction.*;
import com.zara.runtime.*;
import com.zara.utils.*;

public class BinaryOpNode implements Expression {
    private final Expression left;
    private final String operator;
    private final Expression right;

    public BinaryOpNode(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) {
        Object leftVal  = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        // String concatenation support
        if (operator.equals("+") && (leftVal instanceof String || rightVal instanceof String)) {
            return String.valueOf(leftVal) + String.valueOf(rightVal);
        }

        // Ensure numeric operands
        if (!(leftVal instanceof Number) || !(rightVal instanceof Number)) {
            throw new RuntimeException(
                "Invalid operation: '" + operator + "' requires numeric operands"
            );
        }

        double l = ((Number) leftVal).doubleValue();
        double r = ((Number) rightVal).doubleValue();

        return switch (operator) {
            case "+"  -> l + r;
            case "-"  -> l - r;
            case "*"  -> l * r;
            case "/"  -> {
                if (r == 0) throw new RuntimeException("Division by zero");
                yield l / r;
            }
            case ">"  -> l > r;
            case "<"  -> l < r;
            case "==" -> l == r;
            default   -> throw new RuntimeException("Unknown operator: " + operator);
        };
    }

    /*
     * Returns a readable string representation of this expression.
     * Useful for debugging and error messages.
     * Example output:
     *      (x + 5)
     */
    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}