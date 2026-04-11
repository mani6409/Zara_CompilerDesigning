package com.zara.lexer;

import java.util.*;
import com.zara.lexer.*;
import com.zara.parser.*;
import com.zara.parser.ast.*;
import com.zara.interpreter.*;
import com.zara.interpreter.instruction.*;
import com.zara.runtime.*;
import com.zara.utils.*;

public enum TokenType {
    NUMBER, STRING, IDENTIFIER,
    PLUS, MINUS, STAR, SLASH,
    GREATER, LESS, EQUALS, EQEQ, NOT_EQ, LESS_EQ, GREATER_EQ,
    SET, SHOW, WHEN, LOOP, OTHERWISE,
    INDENT, DEDENT, COLON, NEWLINE, EOF
}
