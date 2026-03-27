🚀 ZARA Interpreter
A Modular Compiler Design Project in Java
🧠 Overview

ZARA Interpreter is a custom-designed scripting language engine built using Java, implementing core principles of compiler design with a clean, modular, and scalable architecture.

It processes .zara programs through:

Lexical Analysis → Syntax Parsing → Abstract Syntax Tree → Execution

The project is structured following system design principles, ensuring high readability, maintainability, and extensibility.

✨ Features
🔍 Lexical Analysis (Tokenizer) – Converts source code into tokens
🌳 Recursive Descent Parser – Builds structured AST
⚙️ Interpreter Engine – Executes instructions dynamically
🧩 Custom ZARA Language
Variables
Arithmetic expressions
Strings
Conditionals (when)
Loops (loop)
🏗️ Clean Architecture
Separation of concerns (Lexer, Parser, Interpreter)
❌ Error Handling Support
📦 Extensible Design (easy to add new features)
🏗️ Project Structure
zara-interpreter/
│
├── src/
│   └── com/zara/
│
│       ├── lexer/                 # Tokenization
│       │   ├── Tokenizer.java
│       │   ├── Token.java
│       │   └── TokenType.java
│
│       ├── parser/                # Parsing logic
│       │   ├── Parser.java
│       │   └── ast/
│       │       ├── Expression.java
│       │       ├── NumberNode.java
│       │       ├── StringNode.java
│       │       ├── VariableNode.java
│       │       └── BinaryOpNode.java
│
│       ├── interpreter/           # Execution engine
│       │   ├── Interpreter.java
│       │   ├── Environment.java
│       │   └── instruction/
│       │       ├── Instruction.java
│       │       ├── AssignInstruction.java
│       │       ├── PrintInstruction.java
│       │       ├── IfInstruction.java
│       │       └── RepeatInstruction.java
│
│       ├── runtime/               # Runtime abstractions
│       │   └── Value.java
│
│       ├── utils/                 # Utility classes
│       │   └── ErrorHandler.java
│
│       └── main/                  # Entry point
│           └── Main.java
│
├── programs/                      # Sample ZARA programs
├── docs/                          # Documentation (optional)
├── tests/                         # Unit tests (future)
└── README.md
⚙️ Installation & Setup
🔧 Clone Repository
git clone https://github.com/your-username/zara-interpreter.git
cd zara-interpreter
▶️ Compile
cd src
javac com/zara/**/*.java
▶️ Run
java com.zara.main.Main ../programs/program1.zara
🧪 Sample Program
📌 Input (program1.zara)
set x = 10
set y = 5
show x + y
📌 Output
15
📚 ZARA Language Syntax
Feature	Example
Assignment	set x = 10
Print	show x
String	show "Hello"
Condition	when x > 5:
Loop	loop 3:
Operators	+ - * /
Comparison	> < ==
⚡ How It Works
1️⃣ Lexer
Reads source code
Converts into tokens
Handles indentation-based structure
2️⃣ Parser
Uses recursive descent parsing
Builds Abstract Syntax Tree (AST)
Maintains operator precedence
3️⃣ Interpreter
Executes AST nodes
Uses Environment (symbol table)
Supports control flow and expressions
🎯 Concepts Demonstrated
Compiler Design Phases
Abstract Syntax Trees (AST)
Interpreter Pattern
Symbol Table Management
Recursive Parsing
Clean Code Architecture
🚀 Future Enhancements
🔧 Type Checking (Semantic Analysis)
⚡ Bytecode / Intermediate Code Generation
🧠 Function Support & Scope Handling
📊 Debug Mode (step execution)
🌐 GUI Visualization
👨‍💻 Author

Mani Kumar
Akanksha Kushwaha
Nirmal Mewada

B.Tech Student
Open Source Contributor (EWOC Top 50)
Passionate about Systems & AI
⭐ Resume Highlight

Developed a modular scripting language interpreter in Java implementing lexical analysis, recursive descent parsing, AST construction, and execution engine with clean architecture and scalable design.

🤝 Contributing

Contributions are welcome!

Fork → Branch → Commit → Push → Pull Request
⭐ Support

If you found this project useful, give it a ⭐ on GitHub!
