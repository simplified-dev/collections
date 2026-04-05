# Collections

Thread-safe concurrent collection implementations with atomic operations backed by `ReadWriteLock`. Provides concurrent variants of List, Map, Set, Deque, and Queue, plus tuple types (Pair, Triple, Single), sorted collection views, unmodifiable wrappers, and searchable/queryable interfaces.

> [!IMPORTANT]
> This library requires **Java 21** or later. It is published via [JitPack](https://jitpack.io/#simplified-dev/collections) and is intended as a foundational dependency for other Simplified-Dev modules.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
  - [Concurrent Collections](#concurrent-collections)
  - [Tuple Types](#tuple-types)
- [Project Structure](#project-structure)
- [Building](#building)
  - [Running Tests](#running-tests)
  - [Benchmarks](#benchmarks)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Atomic collections** - `AtomicList`, `AtomicMap`, `AtomicSet`, `AtomicDeque`, and `AtomicQueue` with `ReadWriteLock`-based thread safety
- **Concurrent wrappers** - `ConcurrentList`, `ConcurrentMap`, `ConcurrentSet`, `ConcurrentDeque`, and `ConcurrentQueue` extending atomic base types
- **Linked variants** - `ConcurrentLinkedList`, `ConcurrentLinkedMap`, `ConcurrentLinkedSet` preserving insertion order
- **Sorted collections** - `ConcurrentSortedMap` and `ConcurrentSortedSet` for ordered views
- **Unmodifiable wrappers** - Immutable views of concurrent collections
- **Tuple types** - `Pair`, `Triple`, and `Single` with mutable/immutable variants and streaming support (`PairStream`, `TripleStream`, `SingleStream`)
- **Searchable interface** - Generic `Searchable` and `Sortable` query abstractions
- **Factory methods** - `Concurrent` utility class for creating collection instances
- **Stream utilities** - `StreamUtil` for enhanced stream operations
- **Functional interfaces** - `TriConsumer`, `TriFunction`, `TriPredicate` for three-argument operations

## Getting Started

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| [Java](https://adoptium.net/) | **21+** | Required |
| [Gradle](https://gradle.org/) | **9.4+** | Included via wrapper |
| [Git](https://git-scm.com/) | 2.x+ | For cloning the repository |

### Installation

Add the JitPack repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.simplified-dev:collections:master-SNAPSHOT")
}
```

<details>
<summary>Groovy DSL</summary>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.simplified-dev:collections:master-SNAPSHOT'
}
```

</details>

<details>
<summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.simplified-dev</groupId>
    <artifactId>collections</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

</details>

## Usage

### Concurrent Collections

Create thread-safe collections using the `Concurrent` factory:

```java
import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.ConcurrentSet;

// Thread-safe list
ConcurrentList<String> list = Concurrent.newList();
list.add("hello");

// Thread-safe map
ConcurrentMap<String, Integer> map = Concurrent.newMap();
map.put("key", 42);

// Thread-safe set
ConcurrentSet<String> set = Concurrent.newSet();
set.add("unique");
```

> [!TIP]
> All concurrent collections use `ReadWriteLock` internally, allowing multiple
> concurrent readers while ensuring exclusive write access.

### Tuple Types

Stream-friendly tuple abstractions with mutable and immutable variants:

```java
import dev.simplified.collection.tuple.pair.Pair;
import dev.simplified.collection.tuple.pair.PairStream;
import dev.simplified.collection.tuple.triple.Triple;

// Immutable pair
Pair<String, Integer> pair = Pair.of("name", 42);

// Stream of pairs
PairStream.of(map)
    .filter((key, value) -> value > 10)
    .forEach((key, value) -> System.out.println(key + "=" + value));

// Triple
Triple<String, Integer, Boolean> triple = Triple.of("name", 42, true);
```

## Project Structure

```
collections/
├── src/
│   ├── main/java/dev/simplified/collection/
│   │   ├── atomic/             # AtomicCollection, AtomicList, AtomicMap, AtomicSet,
│   │   │                       # AtomicDeque, AtomicQueue, AtomicIterator
│   │   ├── function/           # TriConsumer, TriFunction, TriPredicate
│   │   ├── linked/             # ConcurrentLinkedList, ConcurrentLinkedMap, ConcurrentLinkedSet
│   │   ├── query/              # Searchable, SearchFunction, Sortable, SortOrder
│   │   ├── sort/               # Graph (topological sort)
│   │   ├── sorted/             # ConcurrentSortedMap, ConcurrentSortedSet
│   │   ├── tuple/
│   │   │   ├── pair/           # Pair, ImmutablePair, MutablePair, PairOptional, PairStream
│   │   │   ├── single/         # SingleStream
│   │   │   └── triple/         # Triple, ImmutableTriple, MutableTriple, TripleStream
│   │   ├── unmodifiable/       # ConcurrentUnmodifiable{Collection,List,Map,Set,LinkedList}
│   │   ├── Concurrent.java     # Factory for creating concurrent collections
│   │   ├── Concurrent*.java    # ConcurrentList, ConcurrentMap, ConcurrentSet, etc.
│   │   └── StreamUtil.java     # Stream utility methods
│   ├── test/java/              # JUnit 5 tests (ConcurrentListTest, ConcurrentMapTest, etc.)
│   └── jmh/java/               # JMH benchmarks (list, map contention benchmarks)
├── build.gradle.kts
├── settings.gradle.kts
└── LICENSE.md
```

## Building

Build the project using the Gradle wrapper:

```bash
./gradlew build
```

### Running Tests

```bash
# Run standard unit tests (excludes slow tests)
./gradlew test

# Run slow integration tests (shutdown, thread leak detection)
./gradlew slowTest
```

> [!NOTE]
> The default `test` task excludes tests tagged with `@Tag("slow")`.
> Use `slowTest` to run long-running integration tests separately.

### Benchmarks

Run JMH benchmarks for concurrent collection performance:

```bash
./gradlew jmh
```

Benchmarks cover list operations, map operations, and contention scenarios under
concurrent access.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, code style
guidelines, and how to submit a pull request.

## License

This project is licensed under the **Apache License 2.0** - see
[LICENSE.md](LICENSE.md) for the full text.
