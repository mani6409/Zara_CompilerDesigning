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
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }
}
