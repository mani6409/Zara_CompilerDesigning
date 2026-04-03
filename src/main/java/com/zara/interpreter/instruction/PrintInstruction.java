package com.zara.interpreter.instruction;
import com.zara.parser.ast.*;
import com.zara.interpreter.*;

public class PrintInstruction implements Instruction {
    private final Expression expression;

    public PrintInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        // Evaluate the expression and print the result to standard output.
        Object value = expression.evaluate(env);
        // Print integers without decimal point (16 not 16.0)
        if (value instanceof Double d && d == Math.floor(d) && !Double.isInfinite(d)) {
            System.out.println(d.intValue());
        } else {
            System.out.println(value);
        }
    }
}
