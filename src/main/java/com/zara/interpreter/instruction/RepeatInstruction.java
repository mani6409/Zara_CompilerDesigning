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
        env.enterScope();
        try {
            // Execute all body instructions, repeated count times.
            for (int i = 0; i < count; i++) {
                for (Instruction instruction : body) {
                    instruction.execute(env);
                }
            }
        } finally {
            env.exitScope();
        }
    }
}
