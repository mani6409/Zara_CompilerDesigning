<<<<<<< HEAD
# Zara Interpreter

A custom programming language interpreter written in Java. **ZARA** is designed to be simple, clean, and intuitive, supporting core programming constructs such as variables, arithmetic operations, conditionals, and loops.

---

## 🚀 Features

* **Lexer (Lexical Analysis)**
  Converts source code into tokens such as numbers, strings, identifiers, and symbols, with support for indentation-based blocks.

* **Parser (Syntax Analysis)**
  Builds an Abstract Syntax Tree (AST) using operator precedence and structured grammar rules.

* **Interpreter (Execution Engine)**
  Executes AST nodes and manages runtime state, including variables and environments.

* **Modular Architecture**
  Clean separation of concerns (lexer, parser, interpreter, runtime, utils).

* **Maven Support**
  Easy build and execution using Maven.

---

## 📁 Project Structure

```text
zara-interpreter/
├── src/main/java/com/zara/
│   ├── lexer/          # Tokenization logic
│   ├── parser/         # AST + parsing logic
│   ├── interpreter/    # Execution engine
│   ├── runtime/        # Value representations
│   ├── utils/          # Error handling utilities
│   └── main/           # Entry point (Main.java)
├── programs/           # Sample .zara programs
├── docs/               # Documentation (grammar, architecture)
├── pom.xml             # Maven configuration
└── README.md           # Project documentation
```

---

## ⚙️ Prerequisites

Make sure you have:

* Java 17 or higher
* Maven 3.6 or higher

---

## 🛠️ Build Instructions

```bash
mvn clean install
```

---

## ▶️ Run a Program

Execute a `.zara` file using:

```bash
mvn exec:java \
  -Dexec.mainClass="com.zara.main.Main" \
  -Dexec.args="programs/program1.zara"
```

---

## 🧪 Example

### ZARA Code

```zara
set x = 10
set y = 20
set result = x + y * 2

show "The result is:"
show result

when result > 40:
    show "Result is greater than 40"
```

### Output

```text
The result is:
50.0
Result is greater than 40
```

---

## 💡 Future Improvements

* Add functions and recursion support
* Improve error reporting with line/column info
* Add REPL (interactive shell)
* Extend standard library

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a new branch (`feature/your-feature-name`)
3. Commit your changes
4. Push to your fork
5. Open a Pull Request

---

## 📜 License

This project is open-source. Add a license if not already included.
=======
[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/kPCOqlbB)
>>>>>>> f4e56d70b1a86b55aab3f46491705a1fa4f9bf51
