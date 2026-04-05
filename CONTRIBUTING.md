# Contributing

Thank you for your interest in contributing to this project. This guide covers
everything you need to get started.

## Table of Contents

- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Development Setup](#development-setup)
- [Making Changes](#making-changes)
  - [Branching Strategy](#branching-strategy)
  - [Code Style](#code-style)
  - [Commit Messages](#commit-messages)
  - [Running Tests](#running-tests)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Reporting Issues](#reporting-issues)
- [Project Architecture](#project-architecture)
- [Legal](#legal)

## Getting Started

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| [Java (JDK)](https://adoptium.net/) | **21+** | Must be a JDK, not a JRE |
| [Gradle](https://gradle.org/) | **9.4+** | Included via wrapper (`./gradlew`) |
| [Git](https://git-scm.com/) | **2.x+** | For version control |

### Development Setup

1. **Fork** the repository on GitHub.

2. **Clone** your fork locally:

   ```bash
   git clone https://github.com/<your-username>/<repo>.git
   cd <repo>
   ```

3. **Build** the project to verify your setup:

   ```bash
   ./gradlew build
   ```

   This compiles all sources, runs tests, and produces the output JAR.

4. **Open** the project in your preferred IDE. IntelliJ IDEA with the Lombok
   plugin is recommended.

> [!TIP]
> You do not need to install Gradle separately. The repository includes a
> Gradle wrapper (`gradlew` / `gradlew.bat`) that downloads the correct version
> automatically.

## Making Changes

### Branching Strategy

- All development is based on the `master` branch.
- Create a feature branch from `master` for your changes:

  ```bash
  git checkout -b feature/your-feature master
  ```

- Keep branches focused on a single change or closely related set of changes.

### Code Style

- Follow standard **Java coding conventions**.
- Use **Lombok** annotations where the project already uses them (e.g.,
  `@Getter`, `@RequiredArgsConstructor`). Do not add Lombok to classes that
  do not already use it without discussion.
- Use **JetBrains `@NotNull` / `@Nullable`** annotations for parameter and
  return type nullability.
- Write Javadoc for all public classes and methods.
- Omit braces on single-line `if`/`for`/`while` bodies; use braces when
  the body wraps to multiple lines.

### Commit Messages

- Use the **imperative mood** in the subject line (e.g., "Add feature" not
  "Added feature" or "Adds feature").
- Keep the subject line under **72 characters**.
- Separate the subject from the body with a blank line if a body is needed.
- Reference related issues in the body (e.g., `Closes #42`).

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run a specific test class
./gradlew test --tests "dev.simplified.*.YourTestClass"

# Full build with all checks
./gradlew build
```

> [!NOTE]
> Ensure all existing tests pass before submitting your pull request. Add new
> tests for any new functionality.

## Submitting a Pull Request

1. Push your branch to your fork:

   ```bash
   git push origin feature/your-feature
   ```

2. Open a **Pull Request** against `master` on the upstream repository.

3. In the PR description:
   - Summarize what the change does and why.
   - Reference any related issues.
   - Note any breaking changes.

4. Wait for review. Address any feedback by pushing additional commits to your
   branch.

## Reporting Issues

- Use the GitHub **Issues** tab to report bugs or request features.
- For bugs, include:
  - Steps to reproduce
  - Expected behavior
  - Actual behavior
  - Java version and OS
- For feature requests, describe the use case and proposed API if applicable.

## Project Architecture

This project is a single-module Gradle library built with `java-library`. The
source follows standard Gradle conventions:

```
src/
├── main/java/    # Library source code
├── test/java/    # Unit and integration tests (JUnit 5)
└── jmh/java/     # Benchmarks (if applicable)
```

All public API lives under the `dev.simplified` package namespace. The library
is published via JitPack as a `master-SNAPSHOT` artifact.

## Legal

By submitting a pull request, you agree that your contributions are licensed
under the [Apache License 2.0](LICENSE.md), the same license that covers
this project.
