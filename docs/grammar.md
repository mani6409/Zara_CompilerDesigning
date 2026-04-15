# Zara Language Grammar Reference

This document is the authoritative reference for Zara syntax. It describes what is **currently implemented** in `Parser.java`, not aspirational future features.

---

## Notation

```
UPPERCASE     — terminal token type (from TokenType.java)
lowercase     — non-terminal rule
?             — optional (zero or one)
*             — zero or more
+             — one or more
|             — alternative
( … )         — grouping
```

---

## Full Grammar

```
program       → statement* EOF

statement     → assign
              | print
              | conditional
              | loop

assign        → SET IDENTIFIER EQUALS expression NEWLINE?

print         → SHOW expression NEWLINE?

conditional   → WHEN expression COLON NEWLINE? block
              ( OTHERWISE COLON NEWLINE? block )?

loop          → LOOP NUMBER COLON NEWLINE? block

block         → INDENT statement NEWLINE? ( INDENT statement NEWLINE? )*

expression    → term ( (PLUS | MINUS) term )*
              ( (GREATER | LESS | EQEQ) term )?

term          → primary ( (STAR | SLASH) primary )*

primary       → NUMBER
              | STRING
              | IDENTIFIER
```

> **Note on `otherwise`:** The `OTHERWISE` token is emitted by the tokenizer but the parser does not currently consume it. The `IfInstruction` class already has an else-body parameter, so completing the `parseIf()` method is the only step needed to enable this feature.

---

## Token Definitions

### Keywords

| Keyword | Token | Meaning |
|---------|-------|---------|
| `set` | `SET` | Variable assignment |
| `show` | `SHOW` | Print to stdout |
| `when` | `WHEN` | Conditional |
| `otherwise` | `OTHERWISE` | Else branch (lexed, not yet parsed) |
| `loop` | `LOOP` | Fixed-iteration loop |

### Literals

```
NUMBER      → [0-9]+ ( '.' [0-9]+ )?
STRING      → '"' [^"]* '"'
IDENTIFIER  → [A-Za-z_] [A-Za-z0-9_]*
```

### Operators

```
PLUS        → '+'
MINUS       → '-'
STAR        → '*'
SLASH       → '/'
GREATER     → '>'
LESS        → '<'
EQUALS      → '='
EQEQ        → '=='
NOT_EQ      → '!='   (tokenized; not handled in Parser.parseExpression)
LESS_EQ     → '<='   (tokenized; not handled in Parser.parseExpression)
GREATER_EQ  → '>='   (tokenized; not handled in Parser.parseExpression)
COLON       → ':'
```

### Structural tokens

```
INDENT      → (emitted when indentation level increases)
DEDENT      → (emitted when indentation level decreases)
NEWLINE     → (emitted at end of every non-blank, non-comment line)
EOF         → (emitted once at end of token stream)
```

---

## Indentation Rules

Indentation determines block scope, similar to Python.

- Leading spaces are counted exactly (1 space = 1 unit).
- A leading tab counts as **4** spaces.
- Mixing tabs and spaces on the same line is allowed but not recommended.
- Increasing indentation emits one `INDENT` token.
- Decreasing indentation emits one or more `DEDENT` tokens (one per level closed).
- If the dedent level does not match any previous indent level, a `RuntimeException` is thrown: `Invalid indentation at line N`.

**Recommended style:** Use 4 spaces per indentation level.

---

## Operator Precedence (lowest to highest)

| Level | Operators | Associativity |
|-------|-----------|---------------|
| 1 (lowest) | `>`, `<`, `==` | None (only one comparison per expression) |
| 2 | `+`, `-` | Left |
| 3 | `*`, `/` | Left |
| 4 (highest) | literals, identifiers | — |

Parentheses are **not currently supported** in Zara expressions.

---

## Type Rules

Zara is dynamically typed. Values are either numbers (`double`) or strings (`String`).

| Operation | Behaviour |
|-----------|-----------|
| `number + number` | Numeric addition |
| `string + anything` | String concatenation (`String.valueOf` on both operands) |
| `anything + string` | String concatenation |
| `number - number` | Numeric subtraction |
| `number * number` | Numeric multiplication |
| `number / 0` | Throws `RuntimeException: Division by zero` |
| `number > number` | Returns `Boolean` |
| `number < number` | Returns `Boolean` |
| `number == number` | Returns `Boolean` |
| `string op string` (non-+) | Throws `RuntimeException: Invalid operation` |

Boolean values returned from comparisons can be stored in variables and used as `when` conditions:

```zara
set flag = x > 0
when flag:
    show "positive"
```

---

## Comments

```zara
# This entire line is a comment

set x = 10  # This is an inline comment
```

Blank lines are silently skipped. Comment-only lines are also skipped.

---

## Error Conditions

| Source | Error message |
|--------|---------------|
| Unterminated string | `Unterminated string at line N` |
| Invalid indentation | `Invalid indentation at line N` |
| Unknown token at statement start | `Unexpected token 'X' on line N` |
| Expected value but got token | `Expected a value but got 'X' on line N` |
| Undefined variable | `Environment Error: Variable 'x' is not defined in the current scope.` |
| Division by zero | `Division by zero` |
| Invalid operator with non-numbers | `Invalid operation: 'op' requires numeric operands` |

---

## Complete Example Program

```zara
# Variable assignment and arithmetic
set base = 10
set height = 5
set area = base * height / 2
show area

# String output
set label = "Area = "
show label
show area

# Conditional
when area > 20:
    show "Large triangle"

# Fixed-count loop
set count = 1
loop 3:
    show count
    set count = count + 1
```

Expected output:
```
25
Area = 
25
Large triangle
1
2
3
```
