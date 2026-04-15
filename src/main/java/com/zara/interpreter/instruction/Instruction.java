package com.zara.interpreter.instruction;

import com.zara.interpreter.*;
public interface Instruction {
    // Execute this instruction, reading and writing variables via the Environment.
    void execute(Environment env);
}
