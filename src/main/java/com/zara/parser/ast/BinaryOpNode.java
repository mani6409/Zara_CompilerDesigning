package com.zara.parser.ast;

import java.util.Objects;
import com.zara.runtime.Environment;

/*
 * BinaryOpNode represents a binary operation in the Abstract Syntax Tree (AST).
 * 
 * A binary operation means an expression that has two operands and one operator
 * between them.
 *
 * Examples:
 *      x + 5
 *      a * b
 *      num1 >= num2
 *
 * In the AST this node stores:
 *      left expression
 *      operator
 *      right expression
 *
 * During interpretation, both expressions are evaluated and then the operator
 * is applied to produce the final result.
 */
public class BinaryOpNode implements Expression {

    // Left side of the binary expression
    private final Expression left;

    // Operator between the two expressions (+, -, *, /, >, <, etc.)
    private final String operator;

    // Right side of the binary expression
    private final Expression right;

    /*
     * Constructor used by the parser while building the AST.
     * It simply stores the left expression, operator, and right expression.
     */
    public BinaryOpNode(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /*
     * evaluate() is called by the interpreter when this expression
     * needs to be executed.
     *
     * The process is:
     * 1. Evaluate the left expression
     * 2. Evaluate the right expression
     * 3. Apply the operator on the results
     * 4. Return the computed value
     */
    @Override
    public Object evaluate(Environment env) {

        // First evaluate both sides of the expression
        Object leftVal  = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        // Apply the correct operation based on the operator
        return switch (operator) {

            // If either side is a String, treat '+' as string concatenation
            // Example: "Hello " + name
            case "+" when (leftVal instanceof String || rightVal instanceof String)
                    -> String.valueOf(leftVal) + rightVal;

            // ---------- Arithmetic operations ----------
            case "+" -> toDouble(leftVal, "+") + toDouble(rightVal, "+");
            case "-" -> toDouble(leftVal, "-") - toDouble(rightVal, "-");
            case "*" -> toDouble(leftVal, "*") * toDouble(rightVal, "*");

            // Division with safety check
            case "/" -> {
                double divisor = toDouble(rightVal, "/");

                // Prevent division by zero
                if (divisor == 0)
                    throw new ArithmeticException("Division by zero in: " + this);

                yield toDouble(leftVal, "/") / divisor;
            }

            // Modulo with safety check
            case "%" -> {
                double mod = toDouble(rightVal, "%");

                // Prevent modulo by zero
                if (mod == 0)
                    throw new ArithmeticException("Modulo by zero in: " + this);

                yield toDouble(leftVal, "%") % mod;
            }

            // ---------- Comparison operations ----------
            case ">"  -> toDouble(leftVal, ">")  >  toDouble(rightVal, ">");
            case "<"  -> toDouble(leftVal, "<")  <  toDouble(rightVal, "<");
            case ">=" -> toDouble(leftVal, ">=") >= toDouble(rightVal, ">=");
            case "<=" -> toDouble(leftVal, "<=") <= toDouble(rightVal, "<=");

            // Equality checks (works for numbers, strings, etc.)
            case "==" -> Objects.equals(leftVal, rightVal);
            case "!=" -> !Objects.equals(leftVal, rightVal);

            // If the parser somehow sends an unknown operator
            default -> throw new RuntimeException(
                "Unknown operator: '" + operator + "' in expression: " + this
            );
        };
    }

    /*
     * Helper method used to convert a value to double.
     * 
     * Arithmetic and comparison operations require numeric values.
     * If a non-number is passed, a runtime error is thrown.
     */
    private double toDouble(Object value, String operator) {

        // If the value is a Number, safely convert it to double
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        // Otherwise the operation is invalid
        throw new RuntimeException(
            "Operator '" + operator + "' requires numeric operands but got: " + value
        );
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
