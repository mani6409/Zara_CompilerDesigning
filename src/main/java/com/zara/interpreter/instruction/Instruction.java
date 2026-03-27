package com.zara.interpreter.instruction;

import java.util.*;
import com.zara.lexer.*;
import com.zara.parser.*;
import com.zara.parser.ast.*;
import com.zara.interpreter.*;
import com.zara.interpreter.instruction.*;
import com.zara.runtime.*;
import com.zara.utils.*;

public interface Instruction {
    // Execute this instruction, reading and writing variables via the Environment.
    void execute(Environment env);
}
