# 🤖 Automata & Computability Project

> A university term project for CSE432: Automata and Computability. Includes a comprehensive theoretical report on Turing Machines and Pushdown Automata, alongside three GUI programs implementing CFG to PDA conversion, DFA construction, and PDA simulation.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Project Structure](#-project-structure)
- [Report](#-report)
- [GUI Programs](#-gui-programs)
- [Getting Started](#-getting-started)
- [Usage](#️-usage)

---

## 🧠 Overview

This project consists of a **single unified deliverable** — a report and program suite — covering both theoretical and practical aspects of automata theory:

- **Part 1 (Theory)** — A formal written report on Turing Machines and Pushdown Automata: their implementations, advantages, limitations, and the types of problems they solve.
- **Part 2 (Implementation)** — Three GUI programs implementing automata constructions, documented within the same report with screenshots and explanations.

---

## 📁 Project Structure
```
Automata-Computability-Project/
├── 📄 report/
│   └── Automata_Report.pdf
├── 💻 src/
│   ├── cfg-to-pda/
│   │   ├── Main.java
│   │   └── README.md
│   ├── dfa-divisible-by-3/
│   │   ├── Main.java
│   │   └── README.md
│   └── pda-anbn/
│       ├── Main.java
│       └── README.md
└── README.md
```
---

## 📄 Report

The project report is a **single document** that covers both parts of the project:

### Part 1 — Theoretical Background

| Section | Description |
|---|---|
| Practical Implementations | Real-world uses of TMs and PDAs |
| Advantages & Disadvantages | Comparative strengths and weaknesses |
| Problems Addressed | Classes of languages and problems each model handles |
| Limitations & Drawbacks | Computational boundaries and known restrictions |
| Additional Insights | Connections to complexity theory and modern computing |

### Part 2 — Program Documentation

Each of the three programs is documented inside the report with:
- Description of the algorithm and automaton design
- GUI walkthrough and explanation
- Screenshots demonstrating program output and functionality

---

## 💻 GUI Programs

### 1. CFG → PDA Converter

Converts a user-supplied Context-Free Grammar into an equivalent Pushdown Automaton. The GUI allows entering production rules and displays the resulting PDA states and transitions.

### 2. DFA over {0,1} — Number of 1s divisible by 3 and ends with 0

Constructs and simulates a DFA that accepts binary strings where the count of `1`s is divisible by 3 **and** the string ends with `0`. The GUI visualizes states and transitions.

### 3. PDA for L = { aⁿbⁿ | n ≥ 0 }

Implements a Pushdown Automaton that accepts strings of the form `aⁿbⁿ` using a stack. The GUI allows users to test strings and view the stack trace step-by-step.

---

## 🚀 Getting Started

### Prerequisites

- Java JDK 11+ *or* a C++17-compatible compiler *or* .NET 6+
- A GUI framework: JavaFX / Swing / Qt / WinForms (depending on language used)

### Clone the Repository

```bash
git clone https://github.com/your-username/Automata-Computability-Project.git
cd Automata-Computability-Project
```

### Running a Program

```bash
# Java example
cd src/cfg-to-pda
javac Main.java
java Main
```

> ⚠️ Each sub-folder contains its own `README.md` with specific build and run instructions.

---

## 🖥️ Usage

Each program features a graphical interface. Refer to `report/Automata_Report.pdf` for detailed screenshots and step-by-step usage instructions for all three programs.

---

*CSE432: Automata and Computability — Spring 2026 · Ain Shams University, Faculty of Engineering*
