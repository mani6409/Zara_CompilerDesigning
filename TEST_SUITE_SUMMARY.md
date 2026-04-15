# Test Suite Summary

This document maps every test file to the source it covers and notes any coverage gaps.

---

## Test file inventory

| Test file | Package | Source covered |
|-----------|---------|----------------|
| `TokenizerTest.java` | `com.zara.lexer` | `Tokenizer.java` |
| `LexerTest.java` | `com.zara.lexer` | `Tokenizer.java` (additional cases) |
| `EnvironmentTest.java` | `com.zara.interpreter` | `Environment.java` |
| `InterpreterIntegrationTest.java` | `com.zara.interpreter` | `Interpreter.java` + full pipeline |
| `ParserTest.java` | `com.zara.parser` | `Parser.java` + AST nodes |
| `ExpressionTest.java` | `com.zara.parser.ast` | `BinaryOpNode`, `NumberNode`, `StringNode`, `VariableNode` |
| `StatementTest.java` | `com.zara.parser.ast` | `AssignInstruction`, `PrintInstruction`, `IfInstruction`, `RepeatInstruction` |
| `TestHelper.java` | `com.zara.utils` | Shared test utilities (not a test itself) |

---

## Coverage notes by module

### Lexer (`Tokenizer.java`)

Covered well:
- NUMBER, STRING, IDENTIFIER token types
- `set`, `show` keyword recognition
- EOF token at stream end
- Unterminated string error

Not yet tested:
- `when`, `loop`, `otherwise` keywords
- INDENT / DEDENT emission for real indented blocks
- Invalid indentation (misaligned dedent)
- Inline `#` comments
- Tabs vs spaces mixed indentation
- Multi-character operators (`!=`, `<=`, `>=`, `==`)

### Parser (`Parser.java`)

Covered:
- `AssignInstruction`, `PrintInstruction`, `IfInstruction`, `RepeatInstruction` produced correctly
- Three-statement programs parse to 3 instructions
- `otherwise:` block structure
- Negative loop count throws
- Float loop count throws
- Unknown token throws

Not yet tested:
- Deeply nested blocks (currently throws `NumberFormatException` — known bug)
- `!=`, `<=`, `>=` operators in expressions
- Empty program (just EOF)
- Program with only blank lines / comments

### Environment (`Environment.java`)

Covered:
- Basic `set` / `get`
- Undefined variable throws

Not yet tested:
- `enterScope` / `exitScope` (lexical scoping)
- Variable shadowing across scopes
- `contains()`, `getOrDefault()`
- `storeVariable` / `retrieveVariable` aliases
- Scope depth limit (1,000 frames)

### Interpreter (`Interpreter.java`)

Covered:
- Simple assignment
- All four arithmetic operators
- Comparison operators (`<`, `>`, `==`)
- C-style `for` loop accumulation
- Variable references in expressions

Not yet tested:
- `loop N:` native Zara loop via string source
- `when condition:` via string source
- String concatenation
- Division by zero
- Undefined variable during expression evaluation
- C-style for loop with `i--`, `i += n`, `i *= n` increments
- Nested for loops
- `for` loop with empty body
- For loop exceeding the 1,000,000-iteration limit

### AST nodes

`BinaryOpNode`, `NumberNode`, `StringNode`, `VariableNode` are exercised indirectly by parser and integration tests. Direct unit tests exist in `ExpressionTest.java` and `StatementTest.java`.

---

## How to run and read results

```bash
mvn test
```

Individual class results are in `target/surefire-reports/`. Each `.txt` file shows:

```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

A test **error** (as opposed to a failure) means an unexpected exception was thrown — check the stack trace in the report for the exact cause.

---

## CI/CD integration

The project is hosted on GitHub. Tests run automatically on every push via GitHub Actions (configured via `.github/`). The classroom assignment deadline badge in the original README links to the GitHub Classroom workflow.

To add a new workflow step locally:

1. Create `.github/workflows/ci.yml`
2. Use the standard `actions/setup-java` action with `java-version: '17'` and `distribution: 'temurin'`
3. Run `mvn test` as the test step

Example workflow:

```yaml
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: mvn test
```

---

## Priority gaps to fill

These missing tests have the highest impact:

1. **Scope / shadowing in `EnvironmentTest`** — the scoped variable store is used by every block construct but has no scope-level tests.
2. **Native Zara `loop` in `InterpreterIntegrationTest`** — the integration tests currently only test the C-style for loop, not `loop N:`.
3. **`when` via `InterpreterIntegrationTest`** — conditionals are tested at parse level but not end-to-end with actual output verification.
4. **Division by zero** — a targeted `assertThrows` test in `InterpreterIntegrationTest`.
5. **Nested blocks** — currently causes `NumberFormatException`; adding a failing test first would help track when the bug is fixed.
