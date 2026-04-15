# Zara Language — Annotated Examples

Each example below can be saved as a `.zara` file and run with:

```bash
mvn exec:java -Dexec.mainClass="com.zara.main.Main" -Dexec.args="programs/yourfile.zara"
```

---

## 1. Arithmetic and variable assignment

```zara
set x = 10
set y = 3
set result = x + y * 2
show result
```

**Output:**
```
16
```

**What's happening:**
- `set x = 10` creates a variable `x` with value `10.0`.
- `x + y * 2` is evaluated with standard operator precedence: multiplication first (`3 * 2 = 6`), then addition (`10 + 6 = 16`).
- `show result` prints whole-number doubles without a decimal point.

---

## 2. String output

```zara
set name = "Sitare"
show name
show "Hello from ZARA"
```

**Output:**
```
Sitare
Hello from ZARA
```

---

## 3. String concatenation

```zara
set greeting = "Hello, "
set audience = "world"
set message = greeting + audience
show message
```

**Output:**
```
Hello, world
```

When either operand of `+` is a `String`, the other side is converted with `String.valueOf()` and the two are concatenated.

---

## 4. Conditional (`when`)

```zara
set score = 85
when score > 50:
    show "Pass"
```

**Output:**
```
Pass
```

The body is only executed when the condition evaluates to `true`. Indentation must be consistent — 4 spaces is recommended.

---

## 5. Conditional with comparison operators

Supported comparison operators in `when` conditions: `>`, `<`, `==`.

```zara
set a = 10
set b = 10

when a == b:
    show "equal"

when a < 20:
    show "a is less than 20"

when b > 5:
    show "b is greater than 5"
```

**Output:**
```
equal
a is less than 20
b is greater than 5
```

---

## 6. Storing a comparison result

Comparisons return a `Boolean` that can be stored and re-used:

```zara
set x = 42
set isPositive = x > 0
when isPositive:
    show "x is positive"
```

**Output:**
```
x is positive
```

---

## 7. Fixed-count loop (`loop`)

```zara
set i = 1
loop 4:
    show i
    set i = i + 1
```

**Output:**
```
1
2
3
4
```

The `loop N:` construct executes its body exactly `N` times. `N` must be a non-negative integer literal.

---

## 8. Loop with accumulator

```zara
set total = 0
loop 5:
    set total = total + 10
show total
```

**Output:**
```
50
```

---

## 9. Combining conditionals and loops

```zara
set value = 0
loop 6:
    set value = value + 1
    when value == 3:
        show "halfway"
show "done"
show value
```

**Output:**
```
halfway
done
6
```

---

## 10. Division and floating-point results

```zara
set a = 7
set b = 2
set result = a / b
show result
```

**Output:**
```
3.5
```

Whole-number results (`8 / 2 = 4.0`) are printed as `4`. Non-whole results retain their decimal.

---

## 11. Running from Java (programmatic use)

You can use the `Interpreter` directly without a file:

```java
Interpreter interpreter = new Interpreter();
interpreter.run("set x = 100");
interpreter.run("set y = x / 4");
Object result = interpreter.getVariable("y");  // 25.0
```

The C-style `for` loop form is also supported programmatically:

```java
interpreter.run("set sum = 0;");
interpreter.run("for (i = 1; i <= 10; i++) { sum = sum + i; }");
Object total = interpreter.getVariable("sum");  // 55.0
```

---

## Sample programs in `programs/`

| File | What it demonstrates |
|------|----------------------|
| `program1.zara` | Arithmetic with operator precedence |
| `program2.zara` | String variables and literal output |
| `program3.zara` | `when` conditional |
| `program4.zara` | `loop` with a running counter |
