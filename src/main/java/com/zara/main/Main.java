package com.zara.main;

import java.util.*;
import com.zara.lexer.*;
import com.zara.parser.*;
import com.zara.parser.ast.*;
import com.zara.interpreter.*;
import com.zara.interpreter.instruction.*;
import com.zara.runtime.*;
import com.zara.utils.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java Main <file.zara>");
            System.out.println("Example: java Main program1.zara");
            return;
        }

        String filePath = args[0];
        String source = Files.readString(Paths.get(filePath));
        new Interpreter().run(source);
    }
}
