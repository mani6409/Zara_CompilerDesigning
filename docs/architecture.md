Zara Interpreter — Architecture Reference
This document describes every component in the codebase: what it does, how it fits into the pipeline, and what to watch out for when modifying it.

Pipeline at a Glance
.zara file (plain text)
        │
        │  Files.readString(path)
        ▼
  ┌─────────────────────────────────────────────┐
  │  Main.java                                  │
  │  Reads file, creates Interpreter, calls run │
  └─────────────────────────────────────────────┘
        │  source: String
        ▼
  ┌─────────────────────────────────────────────┐
  │  Tokenizer.java    (lexer package)           │
  │  Source text → List<Token>                  │
  │  Tracks line numbers; handles INDENT/DEDENT │
  └─────────────────────────────────────────────┘
        │  List<Token>
        ▼
  ┌─────────────────────────────────────────────┐
  │  Parser.java       (parser package)          │
  │  Tokens → List<Instruction>                 │
  │  Recursive-descent; builds AST inline       │
  └─────────────────────────────────────────────┘
        │  List<Instruction>
        ▼
  ┌─────────────────────────────────────────────┐
  │  Interpreter.java  (interpreter package)     │
  │  Walks instructions; calls execute(env)     │
  │  Also handles C-style for-loops via regex   │
  └─────────────────────────────────────────────┘
        │  reads / writes
        ▼
  ┌─────────────────────────────────────────────┐
  │  Environment.java                           │
  │  Scope stack: Deque<Map<String, Object>>    │
  └─────────────────────────────────────────────┘
        │  stdout
        ▼
     Terminal output

Component Reference
1. main/Main.java
Role: Entry point. Reads a .zara file from the command-line argument and passes the raw source string to a new Interpreter instance.
Key behaviour:

Uses Files.readString(Paths.get(filePath)) — requires Java 11+.
Throws a generic Exception up to the JVM on IO failure (no friendly error message).
Does not validate the file extension; any text file can be passed in.

How to extend: If you want to add a REPL mode, add an if (args.length == 0) branch here that reads System.in line-by-line in a loop.

2. lexer/TokenType.java
Role: Enum that names every category of token the tokenizer can produce.
TokenMeaningNUMBERNumeric literal (integer or float)STRINGQuoted string value (without the surrounding ")IDENTIFIERAny user-chosen name (x, myVar, score)PLUS MINUS STAR SLASHArithmetic operatorsGREATER LESS EQUALSSingle-char comparison / assignmentEQEQ NOT_EQ LESS_EQ GREATER_EQTwo-char comparison operatorsSET SHOW WHEN LOOP OTHERWISEZara keywordsINDENTEmitted when indentation level increasesDEDENTEmitted when indentation level decreasesCOLON: — ends a block headerNEWLINEEnd of a logical lineEOFEnd of token stream
How to extend: Add a new enum constant here first, then handle it in Tokenizer.java (scanning) and Parser.java (consumption).

3. lexer/Token.java
Role: Immutable value object carrying the three pieces of information about each token: its TokenType, its raw string value, and the line number it appeared on.
Key detail: line is 1-based. The toString() format [TYPE "value" L5] is used in error messages and debugging.
Thread safety: Immutable — safe to share.

4. lexer/Tokenizer.java
Role: Converts a Zara source string into a List<Token> by scanning line-by-line.
Algorithm:

Split source on \n.
For each line, count leading spaces (tabs count as 4). Skip blank lines and # comment lines.
Compare current indentation to a Stack<Integer> (indentStack). Emit INDENT or one-or-more DEDENT tokens to reflect the change.
Scan the remaining content character-by-character:

Digits → NUMBER
"..." → STRING (throws on unterminated string)
Letters/_ → keyword or IDENTIFIER
Two-character operators (!=, <=, >=, ==) checked before single-char operators
Single-char operators and : → their respective token types
# mid-line → stop scanning (inline comment)


Emit NEWLINE at the end of each content line.
After all lines, close remaining open indents with DEDENT tokens.
Emit EOF.

Known issues:

INDENT tokens are emitted with an empty value (""). The parseBlock() method in Parser.java tries to call Integer.parseInt(peek().getValue()) on them — this will always throw NumberFormatException for nested blocks. This is a known bug.
The NOT_EQ, LESS_EQ, GREATER_EQ operators are tokenized correctly but parseExpression() does not handle them.

How to extend: To add a new keyword, add its TokenType constant and then add a case "keyword" -> TokenType.KEYWORD branch inside the word-scanning switch statement.

5. parser/ast/Expression.java
Role: Marker interface for all AST expression nodes.
javapublic interface Expression {
    Object evaluate(Environment env);
}
evaluate returns either a Double (numeric result), a Boolean (comparison result), or a String. This dynamic typing is the reason all values are stored as Object throughout the runtime.

6. AST node classes
All live in parser/ast/.
ClassWhat it representsevaluate behaviourNumberNodeNumeric literalReturns the stored doubleStringNodeString literalReturns the stored StringVariableNodeVariable referenceCalls env.get(name)BinaryOpNodeleft op rightEvaluates both sides, applies operator
BinaryOpNode details:

+ with a String operand → string concatenation (calls String.valueOf on both sides).
All other operators require both sides to be Number; throws RuntimeException otherwise.
/ checks for divide-by-zero explicitly.
>, <, == return Boolean.
Missing: !=, <=, >= — these tokens exist but no case handles them here.


7. parser/Parser.java
Role: Recursive-descent parser. Consumes a List<Token> and produces a List<Instruction>.
Grammar implemented (simplified):
program      → instruction* EOF
instruction  → assign | print | ifStmt | repeatStmt
assign       → SET IDENTIFIER EQUALS expression NEWLINE?
print        → SHOW expression NEWLINE?
ifStmt       → WHEN expression COLON NEWLINE? block
repeatStmt   → LOOP NUMBER COLON NEWLINE? block
block        → (INDENT instruction NEWLINE?)+
expression   → term ((PLUS | MINUS) term)* comparison?
term         → primary ((STAR | SLASH) primary)*
primary      → NUMBER | STRING | IDENTIFIER
Operator precedence (lowest to highest):
comparison:   >, <, ==
additive:     +, -
multiplicative: *, /
primary:      literals, identifiers
Known limitations:

parseBlock() expects INDENT tokens to carry their indentation level as a numeric string. Since Tokenizer emits INDENT with value = "", nested blocks will throw NumberFormatException.
otherwise: is a recognized token (TokenType.OTHERWISE) but parseIf() does not consume it.
loop count must be a plain NUMBER token; negative numbers (which tokenize as MINUS NUMBER) will throw.


8. interpreter/instruction/Instruction.java
Role: Interface for all executable statements.
javapublic interface Instruction {
    void execute(Environment env);
}

9. Instruction classes
All live in interpreter/instruction/.
AssignInstruction — set x = expr

Evaluates expression, then calls env.set(variableName, value).

PrintInstruction — show expr

Evaluates expression.
If the result is a Double that is a whole number, prints the integer form (16 not 16.0).

IfInstruction — when cond: / otherwise:

Evaluates condition. If Boolean.TRUE, delegates to Interpreter.executeBlock(thenBody, env). If false and elseBody is non-empty, executes the else body.
Has two constructors: one with an else body and one without (for backward compatibility).

RepeatInstruction — loop N:

Calls Interpreter.executeBlock(body, env) exactly count times in a plain for loop.


10. interpreter/Interpreter.java
Role: Top-level execution engine. Also handles a C-style for loop form that does not go through the Zara tokenizer/parser.
Two entry points for run:
java// Accepts already-parsed instructions (used by tests and by run(String)):
public void run(List<Instruction> instructions)

// Accepts raw source (used by Main.java and integration tests):
public void run(String sourceCode)
run(String) logic:

If the source starts with the keyword for (detected with word-boundary guard), parse it as a C-style for-loop and execute directly.
Otherwise, try to normalize simple x = expr into set x = expr via regex, then tokenize, parse, and run normally.

C-style for-loop execution:

tryParseForLoop splits the header into init / condition / increment using balanced-parenthesis tracking.
runForLoop creates a new scope, runs the init statement, then loops while evaluateCondition is true.
evaluateCondition evaluates each side via the Zara parser (by injecting set __for_tmp_N = expr) and compares via compareValues.
A hard limit of 1_000_000 iterations prevents infinite loops.

executeBlock(body, env) (static):

Called by IfInstruction and RepeatInstruction.
Calls env.enterScope(), runs all instructions, then env.exitScope() in a finally block — scopes are always cleaned up even on errors.


11. interpreter/Environment.java
Role: Scoped variable store. Internally a Deque<Map<String, Object>> where the head of the deque is the innermost scope.
Key methods:
MethodBehaviourset(name, value)Walks scopes from inner to outer; updates the first scope that already contains name. If no scope has it, creates it in the current (innermost) scope.get(name)Walks scopes from inner to outer; throws RuntimeException if not found.contains(name)Returns true if the name exists in any scope.getOrDefault(name, default)Like get but returns default instead of throwing.enterScope() / exitScope()Push / pop a HashMap on the scope stack.storeVariable / retrieveVariableAliases used by some tests. retrieveVariable returns null (not an exception) for missing names.
Scope depth limit: enterScope() throws if the stack reaches 1,000 frames — prevents stack overflow from deeply recursive structures.
Global scope: The constructor pushes one initial scope. exitScope() will never pop the last scope.

12. runtime/Value.java
Role: A thin wrapper around an Object with convenience methods asString(), asNumber(), and isNumber().
Current status: This class is not used anywhere in the main pipeline. All values flow as plain Object references. It exists as scaffolding for a future typed value system.

13. utils/ErrorHandler.java
Role: Static utility for printing errors to stderr and exiting.
Methods:

reportError(line, message) — prints [Line N] Error: message and calls System.exit(1).
reportRuntimeError(message) — prints Runtime Error: message and calls System.exit(1).

Current status: Not called by the current interpreter or parser — they throw RuntimeException directly. This class is available for future structured error reporting.

Data Flow Example
Source: set result = x + 3 * 2
Tokenizer output:
  [SET "set" L1] [IDENTIFIER "result" L1] [EQUALS "=" L1]
  [IDENTIFIER "x" L1] [PLUS "+" L1]
  [NUMBER "3" L1] [STAR "*" L1] [NUMBER "2" L1]
  [NEWLINE "" L1] [EOF "" L2]

Parser builds:
  AssignInstruction(
    variableName = "result",
    expression   = BinaryOpNode(
      left     = VariableNode("x"),
      operator = "+",
      right    = BinaryOpNode(
        left     = NumberNode(3.0),
        operator = "*",
        right    = NumberNode(2.0)
      )
    )
  )

Interpreter executes:
  1. evaluate BinaryOpNode(+):
     a. evaluate VariableNode("x")  → env.get("x") → 10.0
     b. evaluate BinaryOpNode(*)    → 3.0 * 2.0 → 6.0
     c. 10.0 + 6.0 → 16.0
  2. env.set("result", 16.0)

Adding a New Instruction Type (Checklist)

Add any new keyword(s) to TokenType.java.
Lex the keyword in Tokenizer.java.
Create a new class in interpreter/instruction/ implementing Instruction.
Add a case for the new keyword in Parser.parseInstruction().
Write the parse method (e.g. parseWhile()).
Add unit tests in the appropriate test class.
Update docs/grammar.md with the new syntax rule.
Add an example to docs/examples.md.
