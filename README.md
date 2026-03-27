# Zara Interpreter

A custom programming language interpreter written in Java. ZARA is designed to be simple, clean, and intuitive, featuring basic operations like variable assignments, math evaluations, conditional statements, and loops.

## Features
- **Lexical Analysis (Lexer)**: Tokenizes source code, handling numbers, strings, identifiers, symbols, and indentation blocks.
- **Syntax Analysis (Parser)**: Constructs an Abstract Syntax Tree (AST) representing operations based on token precedence.
- **Execution Engine (Interpreter)**: Evaluates the AST nodes and manages runtime state (variables and environments).
- **Maven Integration**: Pre-configured with Maven for easy dependency management and execution.

## Project Structure
```text
zara-interpreter/
├── src/main/java/com/zara/     # Java Source Code
│   ├── lexer/                  # Tokenizer and Token definitions
│   ├── parser/                 # AST Node definitions and Parser
│   ├── interpreter/            # Evaluation Logic and Environment
│   ├── runtime/                # Value representations
│   ├── utils/                  # Error Handling utilities
│   └── main/                   # Entry point (Main.java)
├── programs/                   # Sample .zara programs
├── docs/                       # ZARA architecture, grammar, and examples
├── pom.xml                     # Maven project configuration
└── README.md                   # This file
```

## How to Build and Run

### Prerequisites
- **Java 17+**
- **Maven 3.6+**

### 1. Build the Interpreter
Navigate to the project root directory and compile the source code:
```bash
mvn clean compile
```

### 2. Run a ZARA Program
You can run any `.zara` file using Maven's `exec:java` plugin:
```bash
mvn exec:java -Dexec.mainClass="com.zara.main.Main" -Dexec.args="programs/program1.zara"
```

## Example Program
Here is an example ZARA program:
```zara
set x = 10
set y = 20
set result = x + y * 2

show "The result is:"
show result

when result > 40:
    show "Result is greater than 40"
```

Output:
```text
The result is:
50.0
Result is greater than 40
```

## Contributing
Feel free to fork this project, create a new branch, and submit a Pull Request!
