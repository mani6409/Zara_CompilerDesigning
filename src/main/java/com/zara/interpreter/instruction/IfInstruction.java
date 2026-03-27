package com.zara.interpreter.instruction;

import java.util.*;
import com.zara.lexer.*;
import com.zara.parser.*;
import com.zara.parser.ast.*;
import com.zara.interpreter.*;
import com.zara.interpreter.instruction.*;
import com.zara.runtime.*;
import com.zara.utils.*;

import java.util.List;

public class IfInstruction implements Instruction {
    private final Expression condition;
    private final List<Instruction> body;

    public IfInstruction(Expression condition, List<Instruction> body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        // Evaluate the condition.
        Object result = condition.evaluate(env);
        // If the result is true, execute each instruction in the body.
        if (Boolean.TRUE.equals(result)) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }
}
