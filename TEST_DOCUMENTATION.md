Zara Interpreter
A custom programming language interpreter written in Java. Zara is a simple, clean, and readable language supporting variables, arithmetic, conditionals, loops, and a C-style for construct — all built on a classic Tokenizer → Parser → Interpreter pipeline.

Table of Contents

Quick Start
Prerequisites
Project Structure
Building the Project
Running a Program
The Zara Language
Running Tests
Architecture Overview
How to Add a New Feature
Known Limitations & Bug Notes
Roadmap
Contributing


Quick Start
bash# 1. Clone the repository
git clone https://github.com/mani6409/Zara_CompilerDesigning.git
cd Zara_CompilerDesigning

# 2. Build
mvn clean install -DskipTests

# 3. Run a sample program
mvn exec:java \
  -Dexec.mainClass="com.zara.main.Main" \
  -Dexec.args="programs/program1.zara"
Expected output for program1.zara:
16

Prerequisites
ToolMinimum VersionCheckJava JDK17java -versionApache Maven3.6mvn -version
No other dependencies are required. All test dependencies (JUnit 5) are declared in pom.xml and downloaded automatically by Maven.

Project Structure
Zara_CompilerDesigning/
├── src/
│   ├── main/java/com/zara/
│   │   ├── main/
│   │   │   └── Main.java                  ← Entry point: reads file, calls Interpreter
│   │   ├── lexer/
│   │   │   ├── Token.java                 ← Immutable data class: (type, value, line)
│   │   │   ├── TokenType.java             ← Enum of all token categories
│   │   │   └── Tokenizer.java             ← Converts source text → List<Token>
│   │   ├── parser/
│   │   │   ├── Parser.java                ← Converts List<Token> → List<Instruction>
│   │   │   └── ast/
│   │   │       ├── Expression.java        ← Interface: evaluate(env) → Object
│   │   │       ├── BinaryOpNode.java      ← +, -, *, /, >, <, ==
│   │   │       ├── NumberNode.java        ← Numeric literal
│   │   │       ├── StringNode.java        ← String literal
│   │   │       └── VariableNode.java      ← Variable reference
│   │   ├── interpreter/
│   │   │   ├── Interpreter.java           ← Main execution engine; also handles for-loops
│   │   │   ├── Environment.java           ← Scoped variable store (stack of HashMaps)
│   │   │   └── instruction/
│   │   │       ├── Instruction.java       ← Interface: execute(env)
│   │   │       ├── AssignInstruction.java ← set x = expr
│   │   │       ├── PrintInstruction.java  ← show expr
│   │   │       ├── IfInstruction.java     ← when … : / otherwise:
│   │   │       └── RepeatInstruction.java ← loop N:
│   │   ├── runtime/
│   │   │   └── Value.java                 ← Wrapper for runtime values (helper class)
│   │   └── utils/
│   │       └── ErrorHandler.java          ← Static error reporting utilities
│   └── test/java/com/zara/
│       ├── lexer/
│       │   ├── LexerTest.java
│       │   └── TokenizerTest.java
│       ├── parser/
│       │   ├── ParserTest.java
│       │   └── ast/
│       │       ├── ExpressionTest.java
│       │       └── StatementTest.java
│       ├── interpreter/
│       │   ├── EnvironmentTest.java
│       │   └── InterpreterIntegrationTest.java
│       └── utils/
│           └── TestHelper.java
├── programs/                              ← Sample .zara programs to run
│   ├── program1.zara                      ← Arithmetic
│   ├── program2.zara                      ← String output
│   ├── program3.zara                      ← Conditional
│   └── program4.zara                      ← Loop
├── docs/
│   ├── architecture.md                    ← Component deep-dive
│   ├── grammar.md                         ← Full language grammar reference
│   └── examples.md                        ← Annotated code examples
├── pom.xml                                ← Maven build + JUnit 5 dependencies
├── README.md                              ← This file
├── RUNNING_TESTS.md                       ← How to run and interpret tests
└── TEST_SUITE_SUMMARY.md                  ← Test file inventory and coverage map

Building the Project
bash# Full build including tests
mvn clean install

# Build only (skip tests)
mvn clean install -DskipTests

# Compile only (no packaging)
mvn compile
Maven places compiled classes in target/classes/ and the JAR at target/zara-interpreter-1.0-SNAPSHOT.jar.

Running a Program
Via Maven (recommended during development)
bashmvn exec:java \
  -Dexec.mainClass="com.zara.main.Main" \
  -Dexec.args="programs/program1.zara"
Via the JAR (after mvn package)
bashjava -cp target/zara-interpreter-1.0-SNAPSHOT.jar \
  com.zara.main.Main programs/program1.zara
Run all sample programs
bashfor f in programs/*.zara; do
  echo "--- $f ---"
  mvn -q exec:java -Dexec.mainClass="com.zara.main.Main" -Dexec.args="$f"
done

The Zara Language
Variable Assignment
zaraset x = 10
set name = "Alice"
set result = x * 2 + 5
Output
zarashow x
show "Hello, world!"
show result
Conditionals
zarawhen score > 50:
    show "Pass"

when score > 90:
    show "Excellent"
otherwise:
    show "Try harder"
Indentation inside blocks must use spaces (4 spaces recommended). Tabs are accepted and counted as 4 spaces.
Fixed-count loop
zaraloop 5:
    show "iteration"
C-style for loop (interpreter-only, not Zara syntax)
The Interpreter.run(String) method also accepts a limited C-style for loop, useful for integration testing:
for (i = 0; i < 5; i++) { show i; }
Supported increment forms: i++, ++i, i--, --i, i += n, i -= n, i *= n, i /= n.
Operators
CategoryOperatorsArithmetic+, -, *, /Comparison>, <, ==, !=, <=, >=String concat+ (when either operand is a string)
Comments
Lines starting with # are ignored. Inline # comments are also supported:
zara# This is a comment
set x = 10  # inline comment
Output formatting
Whole-number results are printed without a decimal point (16, not 16.0). Fractional results retain their decimals (3.14).

Running Tests
bash# Run the full test suite
mvn test

# Run a specific test class
mvn test -Dtest=TokenizerTest
mvn test -Dtest=ParserTest
mvn test -Dtest=EnvironmentTest
mvn test -Dtest=InterpreterIntegrationTest

# Run tests and view surefire report
mvn test
open target/surefire-reports/   # macOS
xdg-open target/surefire-reports/  # Linux
Test results appear in target/surefire-reports/ as both .txt (plain) and .xml (for CI tools).
See RUNNING_TESTS.md for a detailed per-class test guide and TEST_SUITE_SUMMARY.md for a coverage map.

Architecture Overview
Source Code (String)
        │
        ▼
  ┌─────────────┐
  │  Tokenizer  │  lexer/Tokenizer.java
  │             │  Splits text into tokens; tracks line numbers
  └──────┬──────┘
         │  List<Token>
         ▼
  ┌─────────────┐
  │   Parser    │  parser/Parser.java
  │             │  Builds List<Instruction> using recursive descent
  └──────┬──────┘
         │  List<Instruction>
         ▼
  ┌─────────────┐
  │ Interpreter │  interpreter/Interpreter.java
  │             │  Walks instructions; manages scope via Environment
  └──────┬──────┘
         │  side-effects (stdout, env mutations)
         ▼
     Output / State
Each Instruction holds an Expression tree (AST). When execute(env) is called, the expression is evaluated against the current Environment, which is a stack of HashMap<String, Object> scopes.
For the full component-by-component breakdown, see docs/architecture.md.

How to Add a New Feature
This section walks through adding a new language construct end-to-end.
Example: adding a not boolean negation operator
Step 1 — Add a token type (lexer/TokenType.java)
java// Add to the enum:
NOT
Step 2 — Lex the keyword (lexer/Tokenizer.java, inside the word-scanning switch)
javacase "not" -> TokenType.NOT;
Step 3 — Add an AST node (new file parser/ast/NotNode.java)
javapublic class NotNode implements Expression {
    private final Expression operand;
    public NotNode(Expression operand) { this.operand = operand; }

    @Override
    public Object evaluate(Environment env) {
        Object v = operand.evaluate(env);
        if (v instanceof Boolean b) return !b;
        throw new RuntimeException("'not' requires a boolean operand");
    }
}
Step 4 — Parse the keyword (parser/Parser.java, inside parsePrimary())
javacase NOT -> new NotNode(parsePrimary());
Step 5 — Write tests (test/.../parser/ParserTest.java and test/.../interpreter/InterpreterIntegrationTest.java)
java@Test void testNotTrue()  { /* parse "not true" and assert result is false */ }
@Test void testNotFalse() { /* parse "not false" and assert result is true  */ }
Step 6 — Update the grammar in docs/grammar.md and add an example to docs/examples.md.

Known Limitations & Bug Notes
AreaLimitationloop countMust be a non-negative integer literal; variables and floats are rejectedwhen / otherwiseotherwise keyword is recognized by the lexer but not yet parsed by Parser.java — the parseIf() method does not consume it. IfInstruction supports an else body in its constructor, so completing the parser is the only missing step.Nested blocksThe current parseBlock() in Parser.java does not support multi-level indentation (blocks inside blocks). INDENT tokens with a value of "0" are parsed via Integer.parseInt, but INDENT tokens only store an empty string "" — this will throw NumberFormatException for nested blocks.Comparison operators!=, <=, >= are tokenized correctly but parseExpression() in Parser.java only checks for >, <, == — the others will be silently ignored.Value.javaThe Value wrapper class in runtime/ is not used anywhere in the pipeline. All values are stored as plain Object (Double or String). The class can be removed or adopted uniformly in a future refactor.Error recoveryErrors cause an immediate System.exit(1) via ErrorHandler; there is no error recovery or friendly line/column reporting from the interpreter layer.

Roadmap
See ToDoList for the full phased roadmap. Priority items:

 Fix otherwise block parsing in Parser.java
 Fix nested block support (INDENT token value is always "")
 Expose !=, <=, >= in parseExpression()
 Boolean literals (true / false)
 User-defined functions
 REPL (interactive shell)
 Improved error messages with line/column info


Contributing

Fork the repository and create a branch: git checkout -b feature/your-feature
Make changes — follow the existing package and naming conventions
Add or update tests for anything you change
Run mvn test and ensure all tests pass
Update the relevant docs file (docs/grammar.md, docs/architecture.md, or docs/examples.md) if you change language behaviour
Open a Pull Request using the template in .github/pull_request_template.md


License
This project is open-source. No license file is present in the repository — add one before public distribution.
