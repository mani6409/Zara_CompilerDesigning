package com.zara.parser.ast;

import com.zara.interpreter.Environment;

// This class represents a binary operation in an expression
// Example: x + 5, a * b, 10 > 3
public class BinaryOpNode implements Expression {

    // Left side of the expression (e.g., x in "x + 5")
    private final Expression left;

    // Operator as a string (e.g., "+", "-", "*", "/", ">", "<", "==")
    private final String operator;

    // Right side of the expression (e.g., 5 in "x + 5")
    private final Expression right;

    // Constructor to initialize the binary operation node
    public BinaryOpNode(Expression left, String operator, Expression right) {
        this.left = left;       // assign left expression
        this.operator = operator; // assign operator
        this.right = right;     // assign right expression
    }

    // This method evaluates the expression using the given environment
    // Environment contains variable values and runtime context
    @Override
    public Object evaluate(Environment env) {

        // Evaluate left and right expressions recursively
        Object leftVal  = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        // ===== Special Case: String Concatenation =====
        // If operator is '+' and either operand is a string,
        // convert both to string and concatenate
        if (operator.equals("+") && (leftVal instanceof String || rightVal instanceof String)) {
            return String.valueOf(leftVal) + String.valueOf(rightVal);
        }

        // ===== Type Check =====
        // Ensure both operands are numbers for arithmetic/comparison
        if (!(leftVal instanceof Number) || !(rightVal instanceof Number)) {
            throw new RuntimeException(
                "Invalid operation: '" + operator + "' requires numeric operands"
            );
        }

        // Convert both operands to double for uniform calculation
        double l = ((Number) leftVal).doubleValue();
        double r = ((Number) rightVal).doubleValue();

        // ===== Perform Operation =====
        // Use switch expression to apply operator
        return switch (operator) {

            // Arithmetic operations
            case "+"  -> l + r;
            case "-"  -> l - r;
            case "*"  -> l * r;

            // Division with zero check
            case "/"  -> {
                if (r == 0) throw new RuntimeException("Division by zero");
                yield l / r;
            }

            // Comparison operations (return boolean)
            case ">"  -> l > r;
            case "<"  -> l < r;
            case "==" -> l == r;

            // If operator is not recognized → error
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
        // Format: (left operator right)
        return "(" + left + " " + operator + " " + right + ")";
    }
}
