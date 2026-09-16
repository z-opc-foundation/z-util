# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-module Java utilities library (version 1.0.2-SNAPSHOT) organized as a Maven project. It contains various
utility modules for common programming tasks including expression evaluation, workflow management, AI integrations,
crawling, and more.

## Build Commands

### Full Build

```bash
mvn clean install
```

### Build Without Tests

```bash
mvn clean install -DskipTests=true
```

### Build Single Module

```bash
cd <module-name>
mvn clean install
```

### Run Tests

```bash
# All tests
mvn test

# Single test class
mvn test -Dtest=TestClassName

# Single test method
mvn test -Dtest=TestClassName#testMethod
```

## Project Structure

### Module Categories

| Category | Modules                                                                        |
|----------|--------------------------------------------------------------------------------|
| Core     | `util-core` - Base utilities and common classes                                |
| Data     | `util-jdbc`, `util-expression` - Data processing                               |
| Web      | `util-http`, `util-proxy` - Web utilities                      |
| Compute  | `util-math`, `util-ai`, `util-llm` - Computation and AI                        |
| Media    | `util-media`, `util-office`, `util-visualization` - Media processing           |
| System   | `util-monitor`, `util-devops`, `util-distribute`, `util-source` - System tools |
| Workflow | `util-workflow`, `util-ch` - Workflow and rules                                |
| Misc     | `util-extra`, `util-zex` - Additional utilities                                |

### Module Dependencies

- Most modules depend on `util-core`
- `util-expression` - Custom expression evaluation engine
- `util-workflow` - Workflow engine with nodes and connectors
- `util-ai` and `util-llm` - AI/LLM integrations

## Key Architecture Patterns

### Expression Evaluation

The `util-expression` module implements a custom expression parser and evaluator. Key classes:

- Expression parser with tokenization
- AST (Abstract Syntax Tree) evaluation
- Variable binding and function support

### Workflow Engine

The `util-workflow` module provides a node-based workflow system:

- Nodes represent processing steps
- Connectors define flow between nodes
- Context carries data through the workflow

### Utility Conventions

- Static utility classes use `*Util` or `*Utils` naming
- Classes in `util-core` provide base functionality
- The project does not depend on Lombok; all boilerplate is hand-written (getters/setters/loggers are declared
  explicitly)

## Testing

Tests are located in `src/test/java` using JUnit 4 and JUnit 5.

```bash
# Run all tests in a module
cd util-core
mvn test

# Run specific test
mvn test -Dtest=ClassNameTest
```

## Version Management

The project uses Maven flatten plugin for version management. The version is defined in the parent POM as
`1.0.2-SNAPSHOT`.

To update versions across all modules:

```bash
mvn versions:set -DnewVersion=1.0.3-SNAPSHOT
```

## Common Issues

1. **Build failures**: Ensure Maven 3.6+ and JDK 8+ are installed
2. **Test failures**: Some tests may require specific environment setup
3. **Dependency conflicts**: Check the dependency management section in parent POM
