package com.zara.interpreter;

import com.zara.lexer.*;
import com.zara.parser.*;
import com.zara.interpreter.instruction.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpreter executes instructions against a shared {@link Environment}.
 * <p>
 * {@link #execute(String)} accepts Zara source and a limited C-style {@code for} form;
 * the latter is normalized and/or executed via a structured loop (not only a fixed regex).
 */
public class Interpreter {

    private static final int MAX_FOR_ITERATIONS = 1_000_000;

    private final Environment env = new Environment();

    private int tempCounter = 0;

    private static final Pattern SIMPLE_ASSIGNMENT = Pattern.compile(
        "^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.+?)\\s*;?\\s*$",
        Pattern.DOTALL
    );

    private static final Pattern INCREMENT_PLUS_EQ = Pattern.compile(
        "^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*\\+=\\s*(.+?)\\s*$"
    );
    private static final Pattern INCREMENT_MINUS_EQ = Pattern.compile(
        "^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*-=\\s*(.+?)\\s*$"
    );
    private static final Pattern INCREMENT_STAR_EQ = Pattern.compile(
        "^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*\\*=\\s*(.+?)\\s*$"
    );
    private static final Pattern INCREMENT_SLASH_EQ = Pattern.compile(
        "^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*/=\\s*(.+?)\\s*$"
    );

    /** Longest-first so {@code <=} does not match as {@code <} first. */
    private static final String[] COMPARISON_OPS = {"<=", ">=", "==", "!=", "<", ">"};

    /**
     * Execute source: either a full Zara program or a supported C-style {@code for} loop.
     */
    public void execute(String sourceCode) {
        String trimmed = sourceCode == null ? "" : sourceCode.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        if (startsWithForKeyword(trimmed)) {
            ForParts parts = tryParseForLoop(trimmed);
            if (parts != null) {
                runForLoop(parts);
                return;
            }
            throw new IllegalArgumentException(
                "Unsupported for-loop syntax. Expected: for (init; condition; increment) { body }"
            );
        }

        String normalized = normalizeToZara(trimmed);
        List<Token> tokens = new Tokenizer(normalized).tokenize();
        List<Instruction> instructions = new Parser(tokens).parse();
        execute(instructions);
    }

    /**
     * Execute an instruction list produced elsewhere (e.g. tests or tooling).
     */
    public void execute(List<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            try {
                instruction.execute(env);
            } catch (Exception e) {
                throw new RuntimeException(
                    "Runtime Error at "
                        + instruction.getClass().getSimpleName()
                        + ": "
                        + e.getMessage(),
                    e
                );
            }
        }
    }

    /**
     * Executes a list of instructions within a new scope block.
     * Centralizes scope management for all block-based constructs.
     */
    public static void executeBlock(List<Instruction> body, Environment env) {
        env.enterScope();
        try {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        } finally {
            env.exitScope();
        }
    }

    /**
     * Backwards-compatible entry: tokenize, parse, execute (no C-style {@code for} normalization).
     */
    public void run(String sourceCode) {
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            return;
        }
        String trimmed = sourceCode.trim();
        if (startsWithForKeyword(trimmed)) {
            execute(trimmed);
            return;
        }
        List<Token> tokens = new Tokenizer(trimmed).tokenize();
        List<Instruction> instructions = new Parser(tokens).parse();
        execute(instructions);
    }

    public Object getVariable(String name) {
        return env.get(name);
    }

    // --- C-style for-loop: structure + execution ---

    private static final class ForParts {
        final String init;
        final String condition;
        final String increment;
        final String body;

        ForParts(String init, String condition, String increment, String body) {
            this.init = init;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }
    }

    /**
     * Parses {@code for ( init ; cond ; incr ) { body }} with balanced parentheses and braces.
     */
    static ForParts tryParseForLoop(String trimmed) {
        if (!startsWithForKeyword(trimmed)) {
            return null;
        }
        int i = 3;
        while (i < trimmed.length() && Character.isWhitespace(trimmed.charAt(i))) {
            i++;
        }
        if (i >= trimmed.length() || trimmed.charAt(i) != '(') {
            return null;
        }
        int parenOpen = i;
        int depth = 0;
        int j = parenOpen;
        for (; j < trimmed.length(); j++) {
            char c = trimmed.charAt(j);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    break;
                }
            }
        }
        if (depth != 0) {
            return null;
        }
        String insideParens = trimmed.substring(parenOpen + 1, j);
        int k = j + 1;
        while (k < trimmed.length() && Character.isWhitespace(trimmed.charAt(k))) {
            k++;
        }
        if (k >= trimmed.length() || trimmed.charAt(k) != '{') {
            return null;
        }
        int braceOpen = k;
        depth = 0;
        int m = braceOpen;
        for (; m < trimmed.length(); m++) {
            char c = trimmed.charAt(m);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    break;
                }
            }
        }
        if (depth != 0) {
            return null;
        }
        String body = trimmed.substring(braceOpen + 1, m).trim();

        List<String> headerParts = splitAtDepth0Semicolons(insideParens);
        if (headerParts.size() != 3) {
            return null;
        }

        return new ForParts(
            headerParts.get(0).trim(),
            headerParts.get(1).trim(),
            headerParts.get(2).trim(),
            body
        );
    }

    private static List<String> splitAtDepth0Semicolons(String s) {
        List<String> out = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ';' && depth == 0) {
                out.add(s.substring(start, i));
                start = i + 1;
            }
        }
        out.add(s.substring(start));
        return out;
    }

    private void runForLoop(ForParts fp) {
        if (fp.condition.isEmpty()) {
            throw new IllegalArgumentException("for-loop condition cannot be empty");
        }
        if (fp.increment.isEmpty()) {
            throw new IllegalArgumentException("for-loop increment cannot be empty");
        }

        if (!fp.init.isEmpty()) {
            executeProgramFragment(normalizeStatement(fp.init));
        }

        int iterations = 0;
        while (evaluateCondition(fp.condition)) {
            if (++iterations > MAX_FOR_ITERATIONS) {
                throw new RuntimeException(
                    "for-loop exceeded maximum iterations (" + MAX_FOR_ITERATIONS + ")"
                );
            }
            executeLoopBody(fp.body);
            executeProgramFragment(normalizeIncrement(fp.increment));
        }
    }

    private void executeLoopBody(String body) {
        if (body == null || body.isBlank()) {
            return;
        }
        for (String stmt : splitBodyStatements(body)) {
            String t = stmt.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (startsWithForKeyword(t)) {
                ForParts inner = tryParseForLoop(t);
                if (inner == null) {
                    throw new IllegalArgumentException("Invalid nested for-loop: " + t);
                }
                runForLoop(inner);
            } else {
                executeProgramFragment(normalizeStatement(t));
            }
        }
    }

    /**
     * Splits on {@code ;} only when not inside {@code ()} or {@code {}}.
     */
    private static List<String> splitBodyStatements(String body) {
        List<String> out = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '(' || c == '{') {
                depth++;
            } else if (c == ')' || c == '}') {
                depth--;
            } else if (c == ';' && depth == 0) {
                out.add(body.substring(start, i));
                start = i + 1;
            }
        }
        out.add(body.substring(start));
        List<String> trimmed = new ArrayList<>();
        for (String s : out) {
            String x = s.trim();
            if (!x.isEmpty()) {
                trimmed.add(x);
            }
        }
        return trimmed;
    }

    private void executeProgramFragment(String zaraSource) {
        String n = zaraSource.trim();
        if (n.isEmpty()) {
            return;
        }
        List<Token> tokens = new Tokenizer(n).tokenize();
        List<Instruction> instructions = new Parser(tokens).parse();
        execute(instructions);
    }

    private String normalizeStatement(String stmt) {
        String t = stmt.trim();
        if (t.isEmpty()) {
            return "";
        }
        if (t.startsWith("set ") || t.startsWith("show ")) {
            return t;
        }
        Matcher assign = SIMPLE_ASSIGNMENT.matcher(t);
        if (assign.matches()) {
            return "set " + assign.group(1) + " = " + assign.group(2).trim();
        }
        throw new IllegalArgumentException("Unsupported statement: " + stmt);
    }

    /**
     * Maps C-style increments to Zara {@code set} assignments understood by the parser.
     */
    private String normalizeIncrement(String inc) {
        String s = inc.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("for-loop increment cannot be empty");
        }

        // Postfix: i++, i--
        if (s.length() >= 3 && s.endsWith("++")) {
            String name = s.substring(0, s.length() - 2).trim();
            if (name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                return "set " + name + " = " + name + " + 1";
            }
        }
        if (s.length() >= 3 && s.endsWith("--")) {
            String name = s.substring(0, s.length() - 2).trim();
            if (name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                return "set " + name + " = " + name + " - 1";
            }
        }
        // Prefix: ++i, --i
        if (s.length() >= 3 && s.startsWith("++")) {
            String name = s.substring(2).trim();
            if (name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                return "set " + name + " = " + name + " + 1";
            }
        }
        if (s.length() >= 3 && s.startsWith("--")) {
            String name = s.substring(2).trim();
            if (name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                return "set " + name + " = " + name + " - 1";
            }
        }

        Matcher m1 = INCREMENT_PLUS_EQ.matcher(s);
        if (m1.matches()) {
            String v = m1.group(1);
            String rhs = m1.group(2).trim();
            return "set " + v + " = " + v + " + (" + rhs + ")";
        }
        Matcher m2 = INCREMENT_MINUS_EQ.matcher(s);
        if (m2.matches()) {
            String v = m2.group(1);
            String rhs = m2.group(2).trim();
            return "set " + v + " = " + v + " - (" + rhs + ")";
        }
        Matcher m3 = INCREMENT_STAR_EQ.matcher(s);
        if (m3.matches()) {
            String v = m3.group(1);
            String rhs = m3.group(2).trim();
            return "set " + v + " = " + v + " * (" + rhs + ")";
        }
        Matcher m4 = INCREMENT_SLASH_EQ.matcher(s);
        if (m4.matches()) {
            String v = m4.group(1);
            String rhs = m4.group(2).trim();
            return "set " + v + " = " + v + " / (" + rhs + ")";
        }

        Matcher assign = SIMPLE_ASSIGNMENT.matcher(s);
        if (assign.matches()) {
            return "set " + assign.group(1) + " = " + assign.group(2).trim();
        }

        throw new IllegalArgumentException("Unsupported increment: " + inc);
    }

    /**
     * Evaluates a boolean condition by splitting a top-level comparison and evaluating each side
     * via the existing parser ({@code set tmp = expr}).
     * <p>
     * Supports {@code <=}, {@code >=}, {@code ==}, {@code !=}, {@code <}, {@code >} without
     * requiring the lexer to emit multi-char operators for standalone expression lines.
     */
    private boolean evaluateCondition(String condition) {
        String cond = condition.trim();
        if (cond.isEmpty()) {
            throw new IllegalArgumentException("for-loop condition cannot be empty");
        }

        String[] parts = splitComparison(cond);
        if (parts == null) {
            Object v = evaluateExpressionViaParser(cond);
            if (v instanceof Boolean b) {
                return b;
            }
            throw new IllegalArgumentException("Condition must be boolean or a comparison: " + condition);
        }

        String left = parts[0];
        String op = parts[1];
        String right = parts[2];

        Object lv = evaluateExpressionViaParser(left);
        Object rv = evaluateExpressionViaParser(right);
        return compareValues(lv, op, rv);
    }

    private static String[] splitComparison(String cond) {
        for (String op : COMPARISON_OPS) {
            int idx = indexOfOperator(cond, op);
            if (idx >= 0) {
                return new String[] {
                    cond.substring(0, idx).trim(),
                    op,
                    cond.substring(idx + op.length()).trim()
                };
            }
        }
        return null;
    }

    /** Finds operator at top level (not inside parentheses). */
    private static int indexOfOperator(String s, String op) {
        int depth = 0;
        for (int i = 0; i <= s.length() - op.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && s.regionMatches(i, op, 0, op.length())) {
                return i;
            }
        }
        return -1;
    }

    private Object evaluateExpressionViaParser(String expr) {
        String e = expr.trim();
        if (e.isEmpty()) {
            throw new IllegalArgumentException("Empty expression");
        }
        String tmp = "__for_tmp_" + (tempCounter++);
        executeProgramFragment("set " + tmp + " = " + e);
        return env.get(tmp);
    }

    private static boolean compareValues(Object left, String op, Object right) {
        if (Objects.equals(op, "==")) {
            return Objects.deepEquals(left, right);
        }
        if (Objects.equals(op, "!=")) {
            return !Objects.deepEquals(left, right);
        }

        double l = toDouble(left);
        double r = toDouble(right);
        return switch (op) {
            case "<" -> l < r;
            case ">" -> l > r;
            case "<=" -> l <= r;
            case ">=" -> l >= r;
            default -> throw new IllegalArgumentException("Unknown operator: " + op);
        };
    }

    private static double toDouble(Object v) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        if (v instanceof Boolean b) {
            return b ? 1.0 : 0.0;
        }
        throw new IllegalArgumentException("Numeric comparison expected, got: " + v);
    }

    private String normalizeToZara(String trimmed) {
        Matcher assign = SIMPLE_ASSIGNMENT.matcher(trimmed);
        if (assign.matches()) {
            String var = assign.group(1);
            String expr = assign.group(2).trim();
            return "set " + var + " = " + expr;
        }
        return trimmed;
    }

    /** Avoid treating identifiers like {@code forever} as a {@code for}-loop. */
    private static boolean startsWithForKeyword(String s) {
        if (s.length() < 3) {
            return false;
        }
        if (!s.regionMatches(true, 0, "for", 0, 3)) {
            return false;
        }
        if (s.length() == 3) {
            return true;
        }
        char c = s.charAt(3);
        return !Character.isLetterOrDigit(c) && c != '_';
    }
}
