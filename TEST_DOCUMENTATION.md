# TEST_DOCUMENTATION.md
## Zara Interpreter — Complete Testing Guide

This document is the single reference for anyone working on the Zara test suite — whether you are running tests for the first time, fixing a failing test, or adding tests for a new feature. It covers the testing architecture, every existing test explained line-by-line, how the pipeline flows, known gaps, and a step-by-step guide for adding tests when fixing bugs or building features.

---

## Table of Contents

1. [How to Run Tests](#1-how-to-run-tests)
2. [Testing Architecture](#2-testing-architecture)
3. [How the Interpreter Pipeline Works (testing perspective)](#3-how-the-interpreter-pipeline-works-testing-perspective)
4. [Test File Reference — Every Test Explained](#4-test-file-reference--every-test-explained)
   - [TokenizerTest.java](#41-tokenizertest)
   - [LexerTest.java](#42-lexertest)
   - [ExpressionTest.java](#43-expressiontest)
   - [StatementTest.java](#44-statementtest)
   - [ParserTest.java](#45-parsertest)
   - [EnvironmentTest.java](#46-environmenttest)
   - [InterpreterIntegrationTest.java](#47-interpreterintegrationtest)
   - [TestHelper.java](#48-testhelper-utility)
5. [Coverage Map — What Is and Isn't Tested](#5-coverage-map--what-is-and-isnt-tested)
6. [How to Fix a Bug — Testing Workflow](#6-how-to-fix-a-bug--testing-workflow)
7. [How to Add a New Feature — Testing Workflow](#7-how-to-add-a-new-feature--testing-workflow)
8. [Test Writing Reference](#8-test-writing-reference)

---

## 1. How to Run Tests

The project uses **JUnit 5** with **Maven Surefire**. All commands below are run from the project root (`Zara_CompilerDesigning/`).

```bash
# Run every test
mvn test

# Run one specific test class
mvn test -Dtest=TokenizerTest
mvn test -Dtest=LexerTest
mvn test -Dtest=ExpressionTest
mvn test -Dtest=StatementTest
mvn test -Dtest=ParserTest
mvn test -Dtest=EnvironmentTest
mvn test -Dtest=InterpreterIntegrationTest

# Run one specific test method inside a class
mvn test -Dtest=TokenizerTest#testTokenizeNumber
mvn test -Dtest=ParserTest#testParsingLoop

# Clean, recompile, and run all tests (use this if classes seem stale)
mvn clean test
```

**Reading the output:**

```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

- **Failure** — an assertion was wrong (e.g. expected 5, got 6).
- **Error** — an unexpected exception was thrown during the test.
- **Skipped** — test was marked `@Disabled` or a precondition failed.

Detailed reports are written to `target/surefire-reports/`. Each class has a `.txt` (human-readable) and `.xml` (CI-readable) file.

---

## 2. Testing Architecture

### Layer structure

The tests mirror the source package structure exactly:

```
src/test/java/com/zara/
├── lexer/
│   ├── TokenizerTest.java          ← Unit tests for Tokenizer.java
│   └── LexerTest.java              ← Additional edge-case tokenizer tests
├── parser/
│   ├── ParserTest.java             ← Unit/integration tests for Parser.java
│   └── ast/
│       ├── ExpressionTest.java     ← Unit tests for AST expression nodes
│       └── StatementTest.java      ← Unit tests for instruction execution
├── interpreter/
│   ├── EnvironmentTest.java        ← Unit tests for Environment.java
│   └── InterpreterIntegrationTest.java  ← End-to-end: source string → result
└── utils/
    └── TestHelper.java             ← Shared utility (not a test class itself)
```

### Three levels of tests

```
Level 1: Unit Tests (isolated)
  ExpressionTest, StatementTest, EnvironmentTest, TokenizerTest, LexerTest
  → Test one class at a time; no file I/O; no full pipeline
  → Fast, precise, easy to pinpoint failures

Level 2: Parser Tests (component)
  ParserTest
  → Combines Tokenizer + Parser together
  → Verifies the correct Instruction type is produced from source text

Level 3: Integration Tests (end-to-end)
  InterpreterIntegrationTest
  → Runs the full pipeline: source string → Tokenizer → Parser → Interpreter → Environment
  → Verifies final variable values in the environment after execution
```

### The `TestHelper` utility

`TestHelper.java` captures `System.out` output during tests. It is available to any test class that needs to verify what was printed:

```java
TestHelper helper = new TestHelper();
helper.startCapture();
// ... run code that calls System.out.println ...
String output = helper.stopCapture();
assertEquals("hello\n", output);
helper.clearCapture();  // reset for the next assertion
```

---

## 3. How the Interpreter Pipeline Works (testing perspective)

Understanding where each test class sits in the pipeline helps you know which test to write when something breaks.

```
Source text (String)
       │
       ▼
┌──────────────┐   TokenizerTest.java
│  Tokenizer   │   LexerTest.java
│              │   → Does "set x = 10" produce the right tokens?
└──────┬───────┘
       │  List<Token>
       ▼
┌──────────────┐   ParserTest.java
│   Parser     │   → Does the token stream produce the right Instruction type?
└──────┬───────┘
       │  List<Instruction>
       │  (each Instruction holds an Expression tree)
       │
       │  ← ExpressionTest.java tests these tree nodes directly
       │  ← StatementTest.java tests Instruction.execute() directly
       │
       ▼
┌──────────────┐   InterpreterIntegrationTest.java
│ Interpreter  │   → Does running the full source string produce the right result?
└──────┬───────┘
       │  reads/writes
       ▼
┌──────────────┐   EnvironmentTest.java
│ Environment  │   → Does variable storage and scoping work correctly?
└──────────────┘
```

**Failure triage:** If an integration test fails, check the pipeline from left to right. If `set x = 5` stores the wrong value, check `TokenizerTest` first (is the number tokenized correctly?), then `ParserTest` (is `AssignInstruction` produced?), then `ExpressionTest` (does `NumberNode(5)` evaluate to `5.0`?), then `StatementTest` (does `AssignInstruction.execute` write to the env?), then `EnvironmentTest` (does `env.get` return what was set?).

---

## 4. Test File Reference — Every Test Explained

### 4.1 `TokenizerTest`

**File:** `src/test/java/com/zara/lexer/TokenizerTest.java`  
**Tests:** `Tokenizer.tokenize()` in isolation — verifies that raw source strings produce the correct sequence of `Token` objects.

---

#### `testTokenizeSimpleAssignment`

```java
String input = "set x = 10";
List<Token> tokens = new Tokenizer(input).tokenize();

assertEquals(TokenType.SET,        tokens.get(0).getType());
assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType());
assertEquals(TokenType.EQUALS,     tokens.get(2).getType());
assertEquals(TokenType.NUMBER,     tokens.get(3).getType());
```

**What it verifies:** The most fundamental tokenization case. Confirms that keyword detection (`set` → `SET`), identifier scanning (`x` → `IDENTIFIER`), single-char operator (`=` → `EQUALS`), and number scanning (`10` → `NUMBER`) all work in sequence.

**Why it matters:** If this fails, nothing else in the pipeline will work. It is the smoke test for the tokenizer.

---

#### `testTokenizeNumber`

```java
List<Token> tokens = new Tokenizer("42").tokenize();
assertEquals(TokenType.NUMBER, tokens.get(0).getType());
assertEquals("42", tokens.get(0).getValue());
```

**What it verifies:** A bare number tokenizes to `NUMBER` and the `value` field holds the original text `"42"` (not the parsed double). The `value` is preserved as a string so the parser can call `Double.parseDouble` later.

---

#### `testTokenizeIdentifier`

```java
List<Token> tokens = new Tokenizer("myVar").tokenize();
assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
assertEquals("myVar", tokens.get(0).getValue());
```

**What it verifies:** Mixed-case identifiers with letters only are correctly classified. Also confirms that `value` holds the exact spelling.

---

#### `testTokenizeStringLiteral`

```java
List<Token> tokens = new Tokenizer("\"hello\"").tokenize();
assertEquals(TokenType.STRING, tokens.get(0).getType());
assertEquals("hello", tokens.get(0).getValue());
```

**What it verifies:** The tokenizer strips the surrounding double-quotes and stores only the inner content. The token value for `"hello"` is `hello`, not `"hello"`.

---

#### `testTokenizeEndsWithEOF`

```java
List<Token> tokens = new Tokenizer("set x = 1").tokenize();
assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType());
```

**What it verifies:** Every token stream ends with `EOF`. The `Parser` relies on this sentinel to know when to stop; if it is missing, the parser will throw `IndexOutOfBoundsException`.

---

#### `testUnterminatedStringThrowsException`

```java
assertThrows(RuntimeException.class, () -> new Tokenizer("\"hello").tokenize());
```

**What it verifies:** A string that opens with `"` but never closes throws a `RuntimeException` with a message containing the line number. This is the tokenizer's primary error condition.

---

### 4.2 `LexerTest`

**File:** `src/test/java/com/zara/lexer/LexerTest.java`  
**Tests:** Additional tokenizer edge cases — characters that are unusual or potentially problematic.

---

#### `testValidTokens`

```java
List<Token> tokens = new Tokenizer("set a = 10").tokenize();
assertEquals(TokenType.SET, tokens.get(0).getType());
```

**What it verifies:** Basic sanity check — the first token of a simple assignment is `SET`. Complements `TokenizerTest` with a slightly different variable name.

---

#### `testInvalidCharacterIsIgnored`

```java
List<Token> tokens = new Tokenizer("set @a = 10").tokenize();
assertEquals(TokenType.SET, tokens.get(0).getType());
```

**What it verifies:** Unknown characters like `@` fall through the `default -> { /* ignore */ }` branch in `Tokenizer.java` and are silently skipped. The rest of the line continues tokenizing normally.

**Note for future work:** This silent-ignore behaviour means typos in source code produce no error. If you add proper error reporting, this test will need updating.

---

#### `testWhitespaceHandling`

```java
List<Token> tokens = new Tokenizer("   set b = 20").tokenize();
assertEquals(TokenType.INDENT,     tokens.get(0).getType());
assertEquals(TokenType.SET,        tokens.get(1).getType());
assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
assertEquals("b", tokens.get(2).getValue());
```

**What it verifies:** Leading spaces on a line trigger an `INDENT` token *before* the line's content tokens. The indentation detection logic compares the current indent to the `indentStack` and emits structural tokens accordingly.

**Why this matters:** The `Parser.parseBlock()` method looks for `INDENT` tokens to know when a block starts. If indentation tokens were not emitted, nested blocks would not parse.

---

#### `testCommentHandling`

```java
String input = "# This is a comment\nset c = 30";
List<Token> tokens = new Tokenizer(input).tokenize();
assertEquals(TokenType.SET, tokens.get(0).getType());
```

**What it verifies:** Lines beginning with `#` are completely skipped — no tokens are emitted for them. The first token in the stream is the `SET` from the second line.

---

#### `testArithmeticOperators`

```java
List<Token> tokens = new Tokenizer("set x = 5 + 3").tokenize();
assertEquals(TokenType.PLUS, tokens.get(4).getType());
```

**What it verifies:** `+` is at index 4 in `[SET, IDENTIFIER, EQUALS, NUMBER, PLUS, NUMBER, NEWLINE, EOF]`. Confirms the operator is classified correctly and sits at the right position.

---

#### `testComparisonOperators`

```java
List<Token> tokens = new Tokenizer("set x = 5 <= 10").tokenize();
assertEquals(TokenType.LESS_EQ, tokens.get(4).getType());
```

**What it verifies:** The two-character `<=` operator is detected before the single-character fallback would match just `<`. This relies on the "multi-char symbols checked first" logic in `Tokenizer.java`.

**Important:** `LESS_EQ` is correctly tokenized here, but `Parser.parseExpression()` does not currently handle it. This test confirms the *lexer* is correct; a future parser test should confirm end-to-end parsing of `<=`.

---

### 4.3 `ExpressionTest`

**File:** `src/test/java/com/zara/parser/ast/ExpressionTest.java`  
**Tests:** AST expression nodes directly by calling `.evaluate(env)` without going through the tokenizer or parser. This is pure unit testing of the AST layer.

---

#### Number node tests

```java
// testNumberNode_evaluatesToDouble
new NumberNode(42.0).evaluate(env)  // → Double 42.0

// testNumberNode_zero
new NumberNode(0.0).evaluate(env)   // → Double 0.0

// testNumberNode_negative
new NumberNode(-7.5).evaluate(env)  // → Double -7.5
```

**What they verify:** `NumberNode` is a leaf node that holds a `double` and returns it unchanged. Tests cover positive, zero, and negative values. The `assertInstanceOf(Double.class, result)` check confirms the return type is `Double`, not `Integer` or `String`.

---

#### String node tests

```java
// testStringNode_evaluatesToString
new StringNode("hello").evaluate(env)  // → String "hello"

// testStringNode_emptyString
new StringNode("").evaluate(env)       // → String ""
```

**What they verify:** `StringNode` returns its stored string. The empty string case is important because `""` can cause issues in string concatenation downstream.

---

#### Variable node tests

```java
// testVariableNode_returnsStoredValue
env.set("x", 99.0);
new VariableNode("x").evaluate(env)  // → 99.0

// testVariableNode_undefinedVariable_throws
new VariableNode("undefined").evaluate(env)  // → RuntimeException
```

**What they verify:** `VariableNode` delegates to `env.get(name)`. The happy path confirms it retrieves the correct value. The error path confirms that accessing an undefined variable throws — this prevents silent bugs where an unset variable would return `null` and cause confusing downstream failures.

---

#### `BinaryOpNode` — arithmetic

```java
// testBinaryOpNode_addition:      3 + 4 = 7.0
// testBinaryOpNode_subtraction:   10 - 6 = 4.0
// testBinaryOpNode_multiplication: 5 * 3 = 15.0
// testBinaryOpNode_division:       9 / 3 = 3.0
```

**What they verify:** Each of the four arithmetic operators produces the correct `double` result. The `1e-9` delta on `assertEquals` handles floating-point imprecision.

---

#### `testBinaryOpNode_divisionByZero_throws`

```java
new BinaryOpNode(new NumberNode(5), "/", new NumberNode(0)).evaluate(env)
// → RuntimeException with "zero" in the message
```

**What it verifies:** Division by zero is caught explicitly in `BinaryOpNode` and throws with an informative message. The test checks both that an exception is thrown *and* that the message contains `"zero"` — so someone reading the error at runtime immediately knows what happened.

---

#### `BinaryOpNode` — comparison operators

```java
// testBinaryOpNode_greaterThan_true:   5 > 3  → Boolean.TRUE
// testBinaryOpNode_greaterThan_false:  2 > 3  → Boolean.FALSE
// testBinaryOpNode_lessThan_true:      1 < 4  → Boolean.TRUE
// testBinaryOpNode_lessThan_false:     9 < 4  → Boolean.FALSE
// testBinaryOpNode_equalEqual_true:    7 == 7 → Boolean.TRUE
// testBinaryOpNode_equalEqual_false:   7 == 8 → Boolean.FALSE
```

**What they verify:** Comparison operators return `Boolean`, not `Double`. Testing both `true` and `false` for each operator confirms there are no inverted conditions.

---

#### `BinaryOpNode` — string concatenation

```java
// testBinaryOpNode_stringConcatenation:
new BinaryOpNode(new StringNode("hello "), "+", new StringNode("world")).evaluate(env)
// → "hello world"

// testBinaryOpNode_numberPlusString_concatenates:
new BinaryOpNode(new NumberNode(5), "+", new StringNode(" items")).evaluate(env)
// → "5.0 items"
```

**What they verify:** When either operand is a `String`, `+` performs concatenation rather than addition. The second test confirms that `String.valueOf(5.0)` produces `"5.0"` — note the decimal point in the expected value.

---

#### Nested and composed AST tests

```java
// testBinaryOpNode_nestedArithmetic: (2 + 3) * 4 = 20.0
BinaryOpNode inner = new BinaryOpNode(new NumberNode(2), "+", new NumberNode(3));
BinaryOpNode outer = new BinaryOpNode(inner, "*", new NumberNode(4));
// → 20.0

// testBinaryOpNode_withVariableOperand: x + 10 where x = 5 → 15.0
env.set("x", 5.0);
new BinaryOpNode(new VariableNode("x"), "+", new NumberNode(10)).evaluate(env)
// → 15.0
```

**What they verify:** AST nodes compose correctly. The nested test confirms evaluation is recursive (inner node evaluated first). The variable operand test confirms the env reference flows through the tree.

---

#### Error case tests

```java
// testBinaryOpNode_nonNumericOperandForMinus_throws:
new BinaryOpNode(new StringNode("abc"), "-", new NumberNode(1)).evaluate(env)
// → RuntimeException ("Invalid operation")

// testBinaryOpNode_unsupportedOperator_throws:
new BinaryOpNode(new NumberNode(5), "!=", new NumberNode(3)).evaluate(env)
// → RuntimeException ("Unknown operator")
```

**What they verify:** `BinaryOpNode` validates its inputs. Non-numeric operands for arithmetic operators throw. Unknown operators (like `!=`, which is tokenized but not implemented in `BinaryOpNode`) throw. These tests document *current* behaviour — when `!=` is eventually implemented, `testBinaryOpNode_unsupportedOperator_throws` must be updated to assert `false` instead of an exception.

---

### 4.4 `StatementTest`

**File:** `src/test/java/com/zara/parser/ast/StatementTest.java`  
**Tests:** `Instruction.execute(env)` directly — the layer between the AST and the environment. No tokenizer or parser involved.

---

#### `testAssignInstruction_storesValueInEnvironment`

```java
new AssignInstruction("x", new NumberNode(10.0)).execute(env);
assertEquals(10.0, env.get("x"));
```

**What it verifies:** `AssignInstruction` evaluates its expression (`NumberNode(10.0)` → `10.0`) and writes the result to the environment under the given name.

---

#### `testAssignInstruction_overwritesPreviousValue`

```java
env.set("y", 1.0);
new AssignInstruction("y", new NumberNode(99.0)).execute(env);
assertEquals(99.0, env.get("y"));
```

**What it verifies:** Re-assigning an existing variable overwrites its value rather than creating a second binding. This tests the `env.set` lookup-before-create behaviour in `Environment.java`.

---

#### `testAssignInstruction_withExpression`

```java
BinaryOpNode expr = new BinaryOpNode(new NumberNode(3), "+", new NumberNode(4));
new AssignInstruction("z", expr).execute(env);
assertEquals(7.0, env.get("z"));
```

**What it verifies:** `AssignInstruction` evaluates complex expressions (not just literals) before storing. Confirms the evaluation chain: `AssignInstruction.execute` → `expression.evaluate` → `BinaryOpNode.evaluate` → result stored.

---

#### `testPrintInstruction_doesNotThrow` and `testPrintInstruction_numericExpression_doesNotThrow`

```java
new PrintInstruction(new StringNode("hello")).execute(env);         // no exception
new PrintInstruction(new BinaryOpNode(...)).execute(env);           // no exception
```

**What they verify:** `PrintInstruction` executes without throwing for both string and numeric expressions. These tests do not assert the printed value — use `TestHelper.startCapture()` if you need to verify stdout content.

---

### 4.5 `ParserTest`

**File:** `src/test/java/com/zara/parser/ParserTest.java`  
**Tests:** `Parser.parse()` — the full tokenize+parse pipeline producing `List<Instruction>`.

The test file uses a private helper:

```java
private List<Instruction> parse(String source) {
    List<Token> tokens = new Tokenizer(source).tokenize();
    return new Parser(tokens).parse();
}
```

This helper is used throughout. When a parser test fails, you need to check both the tokenizer output *and* the parser logic.

---

#### `testParsingAssignment`

```java
List<Instruction> instructions = parse("set x = 5");
assertEquals(1, instructions.size());
assertInstanceOf(AssignInstruction.class, instructions.get(0));

Environment env = new Environment();
instructions.get(0).execute(env);
assertEquals(5.0, env.get("x"));
```

**What it verifies:** Single assignment produces one `AssignInstruction`, and when executed, stores `5.0` under `"x"`. This is the only parser test that also *executes* the instruction.

---

#### `testParsingPrint`

```java
List<Instruction> instructions = parse("show \"hello\"");
assertEquals(1, instructions.size());
assertInstanceOf(PrintInstruction.class, instructions.get(0));
```

**What it verifies:** `show` produces a `PrintInstruction`. The test only checks the type, not the printed output.

---

#### `testParsingIfWithoutElse`

```java
String src = "set x = 5\nwhen x > 0:\n    show \"positive\"\n";
List<Instruction> instructions = parse(src);
assertEquals(2, instructions.size());
assertInstanceOf(IfInstruction.class, instructions.get(1));
```

**What it verifies:** A `when` block with an indented body parses to an `IfInstruction`. The total instruction count is 2 (`set` + `when`), confirming the block body is *inside* the `IfInstruction`, not promoted to top level.

---

#### `testParsingIfWithOtherwise`

```java
String src = "set x = 1\nwhen x > 0:\n    show \"pos\"\notherwise:\n    show \"non-pos\"\n";
List<Instruction> instructions = parse(src);
assertEquals(2, instructions.size());
assertInstanceOf(IfInstruction.class, instructions.get(1));
```

**What it verifies:** A `when`/`otherwise` pair still produces 2 top-level instructions. The `otherwise` branch is contained inside the single `IfInstruction`.

**Note:** At the time of writing, `Parser.parseIf()` does not actually consume the `otherwise` block. This test may pass because the parser stops before `otherwise:` — meaning the else body will always be empty. When the `otherwise` feature is implemented, this test must be strengthened to verify the else body is non-empty.

---

#### `testParsingLoop`

```java
String src = "loop 3:\n    show \"hi\"\n";
List<Instruction> instructions = parse(src);
assertEquals(1, instructions.size());
assertInstanceOf(RepeatInstruction.class, instructions.get(0));
```

**What it verifies:** `loop N:` with an indented body produces a `RepeatInstruction`. As with the `if` test, the body is inside the instruction.

---

#### `testParsingMultipleInstructions`

```java
String src = "set a = 1\nset b = 2\nshow a\n";
assertEquals(3, parse(src).size());
```

**What it verifies:** A 3-line program produces exactly 3 instructions. This confirms the top-level `parse()` loop advances correctly between instructions and does not consume extra tokens.

---

#### `testArithmeticPrecedence`

```java
assertDoesNotThrow(() -> parse("when a + b > c:\n    show \"ok\"\n"));
```

**What it verifies:** An expression with mixed `+` and `>` operators parses without throwing. This tests that `parseExpression()` can handle both additive and comparison operators in the same expression (comparison is the final step in `parseExpression()`).

---

#### `testAllComparisonOperators`

```java
String[] ops = { ">", "<", "==" };
for (String op : ops) {
    String src = "when x " + op + " 0:\n    show \"ok\"\n";
    assertDoesNotThrow(() -> parse(src));
}
```

**What it verifies:** Each comparison operator that `parseExpression()` handles (`>`, `<`, `==`) parses without error. Note that `!=`, `<=`, `>=` are intentionally absent — they are tokenized but not yet parsed.

---

#### Error case tests

```java
// testParsingInvalidInput_FloatLoopCount
// "loop 3.7:" → RuntimeException containing "non-negative integer"
RuntimeException ex = assertThrows(RuntimeException.class,
    () -> parse("loop 3.7:\n    show \"hi\"\n"));
assertTrue(ex.getMessage().contains("non-negative integer"));

// testParsingInvalidInput_NegativeLoopCount
// "loop -2:" → RuntimeException (MINUS is not a NUMBER token)
assertThrows(RuntimeException.class,
    () -> parse("loop -2:\n    show \"hi\"\n"));

// testParsingInvalidInput_UnexpectedToken
// "unknown stuff here" → RuntimeException
RuntimeException ex = assertThrows(RuntimeException.class,
    () -> parse("unknown stuff here"));
assertNotNull(ex.getMessage());
```

**What they verify:** The parser rejects invalid input rather than silently producing wrong output. The float loop count test specifically checks the error message text — when you improve error messages, update this assertion to match.

---

### 4.6 `EnvironmentTest`

**File:** `src/test/java/com/zara/interpreter/EnvironmentTest.java`  
**Tests:** `Environment.java` in complete isolation.

---

#### `testVariableStorageAndRetrieval`

```java
Environment env = new Environment();
env.set("x", 10);
env.set("y", 20);
assertEquals(10, ((Number) env.get("x")).intValue());
assertEquals(20, ((Number) env.get("y")).intValue());
```

**What it verifies:** Independent variables can be stored and retrieved correctly. The cast to `Number` then `.intValue()` is needed because `env.get` returns `Object` and the stored value is an `Integer` `10` (not a `Double` — the integer `10` is passed directly here, unlike the parser which always stores `Double`).

---

#### `testRetrieveNonExistentVariable`

```java
Environment env = new Environment();
assertThrows(RuntimeException.class, () -> env.get("z"));
```

**What it verifies:** Accessing a variable that was never set throws `RuntimeException`. This is the fundamental safety guarantee of the environment — undefined variables are never silently `null`.

---

### 4.7 `InterpreterIntegrationTest`

**File:** `src/test/java/com/zara/interpreter/InterpreterIntegrationTest.java`  
**Tests:** The full pipeline end-to-end. Source string goes in; variable values in the environment are inspected after execution.

Each test creates a **fresh `Interpreter` instance** in `@BeforeEach`. This ensures tests do not share state.

---

#### `testSimpleAssignment`

```java
interpreter.run("set x = 5;");
assertEquals(5, ((Number) interpreter.getVariable("x")).intValue());
```

**What it verifies:** The simplest possible program stores a value retrievable via `getVariable`. Note the trailing `;` — this is accepted because `normalizeToZara` strips it, and the regex `SIMPLE_ASSIGNMENT` handles trailing semicolons.

---

#### `testArithmeticOperations`

```java
interpreter.run("set x = 5;");
interpreter.run("set y = 10;");
interpreter.run("set result = x + y;");  assertEquals(15, ...);
interpreter.run("set result = y - x;");  assertEquals(5, ...);
interpreter.run("set result = x * y;");  assertEquals(50, ...);
interpreter.run("set result = y / x;");  assertEquals(2, ...);
```

**What it verifies:** All four arithmetic operators work correctly in the full pipeline. Variables persist across multiple `run()` calls on the same interpreter — the environment is shared.

**Key point:** Each call to `run(String)` that starts with `x = expr` (no `set` keyword) is normalized by the `SIMPLE_ASSIGNMENT` regex in `Interpreter.normalizeToZara()`. This is how C-style `x = expr` is supported without native Zara `set` syntax.

---

#### `testComparisons`

```java
interpreter.run("set x = 5;");
interpreter.run("set y = 10;");
interpreter.run("set result = x < y;");   assertTrue((Boolean) interpreter.getVariable("result"));
interpreter.run("set result = x > y;");   assertFalse((Boolean) interpreter.getVariable("result"));
interpreter.run("set result = x == 5;");  assertTrue((Boolean) interpreter.getVariable("result"));
```

**What it verifies:** Comparison operators return `Boolean` values that can be stored in variables and retrieved. The `(Boolean)` cast confirms the stored type.

---

#### `testLoops`

```java
interpreter.run("set sum = 0;");
interpreter.run("for (i = 1; i <= 5; i = i + 1) { sum = sum + i; }");
assertEquals(15, ((Number) interpreter.getVariable("sum")).intValue());
```

**What it verifies:** The C-style `for` loop in `Interpreter.run(String)`. This tests `tryParseForLoop`, `runForLoop`, `evaluateCondition`, `normalizeStatement`, and `normalizeIncrement` all in one test. The sum of 1+2+3+4+5 = 15 confirms the loop ran exactly 5 times with correct accumulation.

**Note:** This test uses the `for` form, not Zara's native `loop N:`. A gap exists here — `loop N:` is not integration-tested via `run(String)`.

---

#### `testVariableReferences`

```java
interpreter.run("set x = 5;");
interpreter.run("set y = x + 5;");
assertEquals(10, ((Number) interpreter.getVariable("y")).intValue());
```

**What it verifies:** Variables can reference other variables in expressions. `VariableNode.evaluate` calls `env.get("x")` at execution time, so `y` gets the current value of `x`.

---

### 4.8 `TestHelper` Utility

**File:** `src/test/java/com/zara/utils/TestHelper.java`

Not a test class — a shared utility for capturing stdout.

```java
public void startCapture()      // Redirects System.out to internal buffer
public String stopCapture()     // Restores System.out; returns captured text
public void clearCapture()      // Resets buffer (for multiple assertions in one test)
```

**How to use it:**

```java
TestHelper helper = new TestHelper();
helper.startCapture();

Interpreter interp = new Interpreter();
interp.run("show \"hello world\"");

String output = helper.stopCapture();
assertEquals("hello world\n", output);
```

Always call `stopCapture()` — even if the test fails — or subsequent tests will write to the wrong stream. Consider using `@AfterEach` to ensure cleanup:

```java
private TestHelper helper = new TestHelper();

@BeforeEach void setUp() { helper.startCapture(); }
@AfterEach  void tearDown() { helper.stopCapture(); }
```

---

## 5. Coverage Map — What Is and Isn't Tested

| Component | Tested | Not yet tested |
|-----------|--------|----------------|
| `Tokenizer` | NUMBER, STRING, IDENTIFIER, SET, SHOW, INDENT, EOF, unterminated string, invalid char, comments, `<=` | WHEN, LOOP, OTHERWISE keywords; DEDENT; mixed tabs/spaces; invalid indentation alignment; `!=`, `>=`, `==` operators |
| `BinaryOpNode` | `+`, `-`, `*`, `/`, `>`, `<`, `==`, string concat, division by zero, nested trees, variable operands | `!=`, `<=`, `>=` (documented as unsupported; test confirms they throw) |
| `NumberNode` | Positive, zero, negative doubles | — |
| `StringNode` | Non-empty, empty string | — |
| `VariableNode` | Existing variable, undefined variable | — |
| `AssignInstruction` | Literal, overwrite, expression | — |
| `PrintInstruction` | No-throw for string, no-throw for arithmetic | Actual printed output (use `TestHelper`) |
| `IfInstruction` | Parsed correctly, with and without `otherwise` | Execution: does the right branch run? (no integration test) |
| `RepeatInstruction` | Parsed correctly | Execution via `run(String)` with native `loop N:` |
| `Parser` | All four instruction types, multiple instructions, arithmetic precedence, `>` `<` `==`, invalid float count, negative count, unknown token | Nested blocks, `!=`/`<=`/`>=` in expressions, empty program |
| `Environment` | Set/get, undefined throws | Scoping (enterScope/exitScope), variable shadowing, `contains`, `getOrDefault`, aliases, depth limit |
| `Interpreter` | Assignment, all arithmetic, comparisons, C-style for loop, variable references | Native `loop N:` via `run(String)`, native `when` via `run(String)`, string concatenation end-to-end, division by zero end-to-end, nested for loops, for loop iteration limit |

---

## 6. How to Fix a Bug — Testing Workflow

Follow this workflow any time you fix a bug.

### Step 1: Reproduce the bug with a failing test

Write the smallest test that demonstrates the bug *before* touching any source code. This confirms the bug is real and gives you a target.

**Example:** Suppose `show 8 / 2` prints `4.0` instead of `4`.

```java
// In InterpreterIntegrationTest.java
@Test
void testWholeNumberDivisionPrintsInteger() {
    TestHelper helper = new TestHelper();
    helper.startCapture();
    interpreter.run("set result = 8 / 2\nshow result\n");
    String output = helper.stopCapture();
    assertEquals("4\n", output);  // fails: currently prints "4.0\n"
}
```

Run `mvn test -Dtest=InterpreterIntegrationTest#testWholeNumberDivisionPrintsInteger` — it should fail.

### Step 2: Locate the bug in the pipeline

Use the pipeline diagram in Section 3. For a printing bug, check `PrintInstruction.java`:

```java
if (value instanceof Double d && d == Math.floor(d) && !Double.isInfinite(d)) {
    System.out.println(d.intValue());  // already handles whole numbers
}
```

If it handles whole numbers, is the division result a `Double`? Add a unit test at the `ExpressionTest` level to isolate:

```java
@Test void testDivisionResult() {
    Object result = new BinaryOpNode(new NumberNode(8), "/", new NumberNode(2)).evaluate(env);
    assertEquals(4.0, (Double) result, 1e-9);  // confirm it's a whole-number Double
}
```

### Step 3: Fix the source code

Make the minimal change needed. Do not add unrelated changes in the same commit.

### Step 4: Run all tests

```bash
mvn test
```

All previously-passing tests must still pass. If any new failures appear, your fix has a side effect — investigate before committing.

### Step 5: Commit with a clear message

```
fix: PrintInstruction now prints whole-number doubles as integers

Adds integration test testWholeNumberDivisionPrintsInteger to cover
the case where 8 / 2 should print "4" not "4.0".
```

---

### Known bugs and their test anchors

#### Bug 1: Nested blocks throw `NumberFormatException`

**Cause:** `Tokenizer` emits `INDENT` tokens with `value = ""`. `Parser.parseBlock()` calls `Integer.parseInt(peek().getValue())` which throws on an empty string.

**Where to test:** `ParserTest.java`

```java
@Test
void testParsingNestedBlock_currentlyThrows() {
    // This test documents the known bug.
    // When the bug is fixed, change assertThrows → assertDoesNotThrow
    // and verify the nested instruction count.
    assertThrows(RuntimeException.class, () -> parse(
        "when x > 0:\n" +
        "    when x > 5:\n" +
        "        show \"deep\"\n"
    ));
}
```

**Fix direction:** Either store the indent level in the `INDENT` token's value, or rewrite `parseBlock()` to use `INDENT`/`DEDENT` tokens as structural markers (matching open/close) rather than numeric comparison.

---

#### Bug 2: `otherwise:` is parsed but not executed

**Cause:** `Parser.parseIf()` does not consume the `OTHERWISE` token or its body.

**Where to test:** `ParserTest.java` and `InterpreterIntegrationTest.java`

```java
// ParserTest — confirm the else body is non-empty when fixed
@Test
void testParsingIfWithOtherwise_elseBodyIsNonEmpty() {
    // Currently this would need reflection or a custom subclass to inspect
    // the IfInstruction's internal elseBody list.
    // Simpler: use the integration test below.
}

// InterpreterIntegrationTest — confirm the else branch actually runs
@Test
void testOtherwiseBranchExecutes() {
    interpreter.run("set x = 0\nwhen x > 5:\n    set result = 1\notherwise:\n    set result = 2\n");
    assertEquals(2, ((Number) interpreter.getVariable("result")).intValue());
}
```

**Fix direction:** In `Parser.parseIf()`, after parsing the `then` block, check `if (check(TokenType.OTHERWISE))` and consume the `OTHERWISE COLON NEWLINE? block`.

---

#### Bug 3: `!=`, `<=`, `>=` are tokenized but ignored by the parser

**Cause:** `Parser.parseExpression()` only checks `TokenType.GREATER`, `TokenType.LESS`, and `TokenType.EQEQ`.

**Where to test:** `ParserTest.java`

```java
@Test
void testParsingNotEquals() {
    // Currently does not throw but silently ignores !=
    // When fixed, this should parse and the result should be Boolean.FALSE
    assertDoesNotThrow(() -> {
        List<Instruction> instructions = parse("set result = 5 != 3");
        Environment env = new Environment();
        instructions.get(0).execute(env);
        assertEquals(Boolean.TRUE, env.get("result"));
    });
}
```

**Fix direction:** In `Parser.parseExpression()`, extend the `if` check to include `NOT_EQ`, `LESS_EQ`, `GREATER_EQ`. Also add corresponding `case` entries in `BinaryOpNode.evaluate`.

---

## 7. How to Add a New Feature — Testing Workflow

This section gives a concrete, step-by-step process for adding a new Zara language feature with full test coverage.

### Complete example: adding `true` and `false` boolean literals

#### Step 1: Write the failing tests first (TDD)

Before touching source, write tests at every layer:

```java
// ExpressionTest.java — AST layer
@Test
void testTrueNode_evaluatesToBooleanTrue() {
    // BooleanNode is the new AST node we'll create
    assertEquals(Boolean.TRUE, new BooleanNode(true).evaluate(env));
}

@Test
void testFalseNode_evaluatesToBooleanFalse() {
    assertEquals(Boolean.FALSE, new BooleanNode(false).evaluate(env));
}

// TokenizerTest.java — lexer layer
@Test
void testTrueKeywordTokenized() {
    List<Token> tokens = new Tokenizer("true").tokenize();
    assertEquals(TokenType.TRUE, tokens.get(0).getType());
}

@Test
void testFalseKeywordTokenized() {
    List<Token> tokens = new Tokenizer("false").tokenize();
    assertEquals(TokenType.FALSE, tokens.get(0).getType());
}

// InterpreterIntegrationTest.java — end-to-end layer
@Test
void testBooleanLiteralAssignment() {
    interpreter.run("set flag = true");
    assertEquals(Boolean.TRUE, interpreter.getVariable("flag"));
}

@Test
void testBooleanLiteralInCondition() {
    interpreter.run("set flag = true");
    interpreter.run("set result = 0\nwhen flag:\n    set result = 1\n");
    assertEquals(1, ((Number) interpreter.getVariable("result")).intValue());
}
```

Run `mvn test` — all these tests fail because `TRUE`, `FALSE`, `BooleanNode` don't exist yet. This is the expected state.

#### Step 2: Add to `TokenType.java`

```java
// Add to the enum:
TRUE, FALSE
```

#### Step 3: Lex in `Tokenizer.java`

```java
// Inside the word-scanning switch:
case "true"  -> TokenType.TRUE;
case "false" -> TokenType.FALSE;
```

Run `mvn test -Dtest=TokenizerTest` — the tokenizer tests pass.

#### Step 4: Create `BooleanNode.java`

```java
package com.zara.parser.ast;
import com.zara.interpreter.Environment;

public class BooleanNode implements Expression {
    private final boolean value;
    public BooleanNode(boolean value) { this.value = value; }

    @Override
    public Object evaluate(Environment env) { return value; }
}
```

Run `mvn test -Dtest=ExpressionTest` — the expression tests pass.

#### Step 5: Parse in `Parser.java`

```java
// Inside parsePrimary(), add cases:
case TRUE  -> new BooleanNode(true);
case FALSE -> new BooleanNode(false);
```

#### Step 6: Run all tests

```bash
mvn test
```

All tests should pass, including the integration tests written in Step 1.

#### Step 7: Update documentation

- Add `true` and `false` to `docs/grammar.md` under literals.
- Add an example to `docs/examples.md`.
- Update `TEST_SUITE_SUMMARY.md` coverage notes.

---

### Checklist for any new feature

| Step | Action |
|------|--------|
| 1 | Write failing tests at every affected layer before coding |
| 2 | Add `TokenType` constant if a new keyword/operator is needed |
| 3 | Add lexing logic in `Tokenizer.java` |
| 4 | Add or update AST node class in `parser/ast/` |
| 5 | Add or update `Instruction` class in `interpreter/instruction/` if it's a new statement |
| 6 | Add parsing logic in `Parser.java` |
| 7 | Run `mvn test` — all tests (old and new) must pass |
| 8 | Update `docs/grammar.md`, `docs/examples.md`, `TEST_SUITE_SUMMARY.md` |

---

## 8. Test Writing Reference

### Template for a unit test

```java
@Test
void testDescriptiveName_describesScenario() {
    // Arrange: set up the object under test and any required state
    Environment env = new Environment();
    env.set("x", 5.0);

    // Act: perform the operation being tested
    Object result = new VariableNode("x").evaluate(env);

    // Assert: verify the outcome
    assertEquals(5.0, result, "VariableNode should return the value stored in env");
}
```

### Template for an exception test

```java
@Test
void testSomething_throwsOnInvalidInput() {
    RuntimeException ex = assertThrows(
        RuntimeException.class,
        () -> new VariableNode("undefined").evaluate(new Environment()),
        "Accessing undefined variable should throw RuntimeException"
    );
    // Optionally verify the message:
    assertTrue(ex.getMessage().contains("not defined"),
        "Error message should describe the missing variable");
}
```

### Template for an integration test with output capture

```java
private TestHelper helper = new TestHelper();

@Test
void testShowPrintsCorrectValue() {
    helper.startCapture();
    try {
        interpreter.run("set x = 42\nshow x\n");
        String output = helper.stopCapture();
        assertEquals("42\n", output);
    } finally {
        helper.stopCapture();  // safe to call again; restores System.out
    }
}
```

### Naming conventions

| Pattern | Example |
|---------|---------|
| `testClassName_descriptionOfBehaviour` | `testBinaryOpNode_divisionByZero_throws` |
| `testFeature_withCondition_expectedOutcome` | `testAssignInstruction_overwritesPreviousValue` |
| Use `_throws` suffix for exception tests | `testVariableNode_undefinedVariable_throws` |
| Use `_true` / `_false` suffix for boolean assertions | `testBinaryOpNode_greaterThan_true` |

### JUnit 5 assertion quick reference

```java
assertEquals(expected, actual)                 // values are equal
assertEquals(expected, actual, delta)          // doubles within delta
assertNotEquals(a, b)                          // values are not equal
assertTrue(condition)                          // condition is true
assertFalse(condition)                         // condition is false
assertNull(value)                              // value is null
assertNotNull(value)                           // value is not null
assertInstanceOf(Type.class, object)           // object is an instance of Type
assertThrows(ExceptionType.class, () -> {...}) // lambda throws that exception
assertDoesNotThrow(() -> {...})                // lambda throws nothing
```

### What makes a good test

A good test has exactly **one reason to fail**. If a test for `RepeatInstruction` fails, you should know immediately that something in the loop execution is wrong — not that the tokenizer or environment is broken. Keep tests focused at one layer.

A good test name tells you what went wrong without reading the body. `testBinaryOpNode_divisionByZero_throws` is clear. `testCase3` is not.

A good test covers both the happy path *and* at least one error path. For every method that can throw, there should be a test that triggers the throw.
