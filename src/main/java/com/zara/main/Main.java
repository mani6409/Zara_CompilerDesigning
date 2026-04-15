package com.zara.main;

import com.zara.interpreter.Interpreter;

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
