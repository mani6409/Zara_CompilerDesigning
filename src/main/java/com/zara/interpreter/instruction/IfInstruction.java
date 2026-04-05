package com.zara.interpreter.instruction;

import com.zara.parser.ast.*;
import com.zara.interpreter.*;

import java.util.List;
import java.util.ArrayList;

public class IfInstruction implements Instruction {
    private final Expression condition;
    private final List<Instruction> thenBody;
    private final List<Instruction> elseBody;

    // Constructor with else branch
    public IfInstruction(Expression condition,
                         List<Instruction> thenBody,
                         List<Instruction> elseBody) {
        this.condition = condition;
        this.thenBody  = thenBody;
        this.elseBody  = elseBody != null ? elseBody : new ArrayList<>();
    }

    // Backward-compatible constructor (no else branch)
    public IfInstruction(Expression condition, List<Instruction> thenBody) {
        this(condition, thenBody, new ArrayList<>());
    }

    @Override
    public void execute(Environment env) {
        Object result = condition.evaluate(env);
        if (Boolean.TRUE.equals(result)) {
            Interpreter.executeBlock(thenBody, env);
        } else if (!elseBody.isEmpty()) {
            Interpreter.executeBlock(elseBody, env);
        }
    }
}
