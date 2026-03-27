package com.zara.utils;

public class ErrorHandler {
    public static void reportError(int line, String message) {
        System.err.println("[Line " + line + "] Error: " + message);
        System.exit(1);
    }
    
    public static void reportRuntimeError(String message) {
        System.err.println("Runtime Error: " + message);
        System.exit(1);
    }
}
