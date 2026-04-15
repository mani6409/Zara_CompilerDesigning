// TestHelper.java
package com.zara.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestHelper {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    public void startCapture() {
        System.setOut(new PrintStream(outputStream));
    }

    public String stopCapture() {
        System.setOut(originalOut);
        return outputStream.toString();
    }

    public void clearCapture() {
        outputStream.reset();
    }
}