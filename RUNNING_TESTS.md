# Running Tests

The Zara project uses **JUnit 5** via Maven Surefire. All test commands are Maven commands — there is no npm or pytest involved.

---

## Prerequisites

- Java 17+
- Maven 3.6+

Both are the same requirements as building the project. No extra installation is needed.

---

## Run all tests

```bash
mvn test
```

This compiles both `src/main` and `src/test`, then runs every `*Test.java` class found in `src/test/`.

Sample output:

```
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Run a specific test class

```bash
mvn test -Dtest=TokenizerTest
mvn test -Dtest=ParserTest
mvn test -Dtest=EnvironmentTest
mvn test -Dtest=InterpreterIntegrationTest
```

---

## Run a specific test method

```bash
mvn test -Dtest=TokenizerTest#testTokenizeNumber
mvn test -Dtest=ParserTest#testParsingAssignment
```

---

## View test reports

After running `mvn test`, reports are written to `target/surefire-reports/`:

```
target/surefire-reports/
├── com.zara.interpreter.EnvironmentTest.txt
├── com.zara.interpreter.InterpreterIntegrationTest.txt
├── com.zara.lexer.LexerTest.txt
├── com.zara.lexer.TokenizerTest.txt
├── com.zara.parser.ParserTest.txt
├── com.zara.parser.ast.ExpressionTest.txt
└── com.zara.parser.ast.StatementTest.txt
```

The `.txt` files contain plain-text pass/fail summaries. The `.xml` files (same directory) are used by CI tools.

---

## Test class inventory

### `lexer/TokenizerTest.java`

Tests the `Tokenizer` class in isolation.

| Test | What it checks |
|------|----------------|
| `testTokenizeSimpleAssignment` | `set x = 10` produces SET, IDENTIFIER, EQUALS, NUMBER in order |
| `testTokenizeNumber` | A bare number produces a NUMBER token with the correct value |
| `testTokenizeIdentifier` | A bare identifier produces IDENTIFIER with the correct value |
| `testTokenizeStringLiteral` | `"hello"` produces STRING without the surrounding quotes |
| `testTokenizeEndsWithEOF` | The last token is always EOF |
| `testUnterminatedStringThrowsException` | `"hello` (missing closing quote) throws RuntimeException |

### `interpreter/EnvironmentTest.java`

Tests the `Environment` variable store in isolation.

| Test | What it checks |
|------|----------------|
| `testVariableStorageAndRetrieval` | `set` and `get` for two variables |
| `testRetrieveNonExistentVariable` | `get` on an undefined name throws RuntimeException |

### `interpreter/InterpreterIntegrationTest.java`

End-to-end tests: source string in, variable value out.

| Test | What it checks |
|------|----------------|
| `testSimpleAssignment` | `set x = 5` stores 5 |
| `testArithmeticOperations` | `+`, `-`, `*`, `/` on two variables |
| `testComparisons` | `<`, `>`, `==` return correct booleans |
| `testLoops` | C-style `for` loop sums 1..5 to 15 |
| `testVariableReferences` | `set y = x + 5` when `x = 5` gives `y = 10` |

### `parser/ParserTest.java`

Tests that the parser produces the correct `Instruction` types and that invalid input throws.

| Test | What it checks |
|------|----------------|
| `testParsingAssignment` | `set x = 5` → `AssignInstruction`; env has `x = 5.0` |
| `testParsingPrint` | `show "hello"` → `PrintInstruction` |
| `testParsingIfWithoutElse` | `when x > 0:` block → `IfInstruction` |
| `testParsingIfWithOtherwise` | `when … / otherwise:` → `IfInstruction` |
| `testParsingLoop` | `loop 3:` → `RepeatInstruction` |
| `testParsingMultipleInstructions` | Three-line program → 3 instructions |
| `testArithmeticPrecedence` | Complex expression parses without error |
| `testAllComparisonOperators` | `>`, `<`, `==` all parse without error |
| `testParsingInvalidInput_FloatLoopCount` | `loop 3.7:` throws with "non-negative integer" |
| `testParsingInvalidInput_NegativeLoopCount` | `loop -2:` throws |
| `testParsingInvalidInput_UnexpectedToken` | Garbage input throws |

---

## Writing a new test

1. Create or open the relevant test class in `src/test/java/com/zara/`.
2. Annotate the method with `@Test` (JUnit 5 — `org.junit.jupiter.api.Test`).
3. Use `assertEquals`, `assertThrows`, `assertInstanceOf`, etc. from `org.junit.jupiter.api.Assertions`.
4. Run `mvn test` to confirm it passes (or fails as expected).

Example skeleton for a new interpreter test:

```java
@Test
void testMyNewFeature() {
    Interpreter interpreter = new Interpreter();
    interpreter.run("set x = 5\nset y = x + 3\n");
    assertEquals(8, ((Number) interpreter.getVariable("y")).intValue());
}
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `BUILD FAILURE: No tests were executed` | Check that the test class name ends with `Test` and the method has `@Test` |
| `ClassNotFoundException` | Run `mvn clean test` to force a full recompile |
| A test throws `NumberFormatException` in `parseBlock` | This is the known nested-block bug — see Known Limitations in README.md |
| `mvn` command not found | Ensure Maven is installed and `JAVA_HOME` is set to a JDK 17+ directory |
