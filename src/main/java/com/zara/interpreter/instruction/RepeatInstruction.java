package com.zara.interpreter.instruction;

import com.zara.interpreter.*;


import java.util.List;

public class RepeatInstruction implements Instruction {
    private final int count;
    private final List<Instruction> body;

    public RepeatInstruction(int count, List<Instruction> body) {
        this.count = count;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        // Execute all body instructions, repeated count times.
        for (int i = 0; i < count; i++) {
            Interpreter.executeBlock(body, env);
        }
    }
}
