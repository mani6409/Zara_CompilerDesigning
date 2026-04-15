# Zara Interpreter

A custom programming language interpreter written in Java. **Zara** is a simple, clean, and readable language supporting variables, arithmetic, conditionals, loops, and a C-style `for` construct — all built on a classic Tokenizer → Parser → Interpreter pipeline.

---

## Table of Contents

- [Quick Start](#quick-start)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Building the Project](#building-the-project)
- [Running a Program](#running-a-program)
- [The Zara Language](#the-zara-language)
- [Running Tests](#running-tests)
- [Architecture Overview](#architecture-overview)
- [How to Add a New Feature](#how-to-add-a-new-feature)
- [Known Limitations & Bug Notes](#known-limitations--bug-notes)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Quick Start

```bash
git clone https://github.com/mani6409/Zara_CompilerDesigning.git
cd Zara_CompilerDesigning

mvn clean install -DskipTests

mvn exec:java \
  -Dexec.mainClass="com.zara.main.Main" \
  -Dexec.args="programs/program1.zara"

Expected output:

16
Prerequisites
Tool	Minimum Version	Check
Java JDK	17	java -version
Apache Maven	3.6	mvn -version
Project Structure
Zara_CompilerDesigning/
├── src/
│   ├── main/java/com/zara/
│   │   ├── main/
│   │   ├── lexer/
│   │   ├── parser/
│   │   ├── interpreter/
│   │   ├── runtime/
│   │   └── utils/
├── programs/
├── docs/
├── pom.xml
└── README.md
Building the Project
mvn clean install
mvn clean install -DskipTests
mvn compile
Running a Program
mvn exec:java \
  -Dexec.mainClass="com.zara.main.Main" \
  -Dexec.args="programs/program1.zara"

OR

java -cp target/zara-interpreter-1.0-SNAPSHOT.jar \
  com.zara.main.Main programs/program1.zara
The Zara Language
Assignment
set x = 10
set name = "Alice"
Output
show x
show "Hello"
Conditionals
when score > 50:
    show "Pass"
otherwise:
    show "Fail"
Loop
loop 5:
    show "iteration"
For Loop
for (i = 0; i < 5; i++) { show i; }
Running Tests
mvn test
Architecture Overview
Source → Tokenizer → Parser → Interpreter → Output
Known Limitations
otherwise not fully parsed
Nested blocks limited
Some comparison operators missing
Roadmap
Fix otherwise
Add boolean support
Add functions
Add REPL
Contributing
Fork repo
Create branch
Add changes + tests
Run mvn test
Submit PR
License

This project is open-source. Add a license if not already included.