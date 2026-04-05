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
    GREATER, LESS, EQUALS, EQEQ,
    SET, SHOW, WHEN, LOOP,
    INDENT, DEDENT, COLON, NEWLINE, EOF
}
