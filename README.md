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

- **Atomic bases** - `AtomicCollection`, `AtomicList`, `AtomicSet`, `AtomicMap`, `AtomicQueue`, `AtomicDeque`, plus the navigable bases `AtomicNavigableSet` and `AtomicNavigableMap`. Each directly implements its corresponding `Concurrent*` interface and is the canonical extension point for custom concurrent types.
- **Concurrent interfaces** - `ConcurrentList`, `ConcurrentMap`, `ConcurrentSet`, `ConcurrentDeque`, and `ConcurrentQueue` with thin nested `Impl` construction shims over the matching `Atomic*`.
- **Linked variants** - `ConcurrentLinkedList`, `ConcurrentLinkedMap`, `ConcurrentLinkedSet` preserving insertion order; `ConcurrentLinkedMap` supports an optional eldest-entry eviction cap via `withMaxSize(int)`.
- **Tree variants** - `ConcurrentTreeMap` and `ConcurrentTreeSet` for sorted/navigable views over a `TreeMap`/`TreeSet`.
- **Unmodifiable wrappers** - Immutable snapshots paired with a no-op lock for wait-free reads, available for every mutable variant via `toUnmodifiable()`. `NoOpReadWriteLock` is `Serializable` so snapshots round-trip through serialization without losing their lock semantics.
- **Static factories on every interface** - `empty()`, `of(...)`, `from(...)`, `adopt(...)` (zero-copy publication). List adds `withCapacity(int)`; Tree variants add `withComparator(...)`; `ConcurrentLinkedMap` adds `withMaxSize(int)`.
- **`ReadWriteLock`-based concurrency** - `withReadLock` / `withWriteLock` lambda helpers on `AtomicCollection` and `AtomicMap` wrap the lock + snapshot-invalidation pattern uniformly.
- **Tuple types** - `Pair`, `Triple`, and `Single` with mutable/immutable variants and streaming support (`PairStream`, `TripleStream`, `SingleStream`).
- **Searchable / Sortable interfaces** - Generic query abstractions backing the search and sort surfaces.
- **`Concurrent` factory** - Legacy `Concurrent.newList()` / `newMap()` / etc. style entry point; still supported alongside the per-interface statics.
- **Stream utilities** - `StreamUtil` for enhanced stream operations.
- **Functional interfaces** - `TriConsumer`, `TriFunction`, `TriPredicate` for three-argument operations.

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

Create thread-safe collections using either the per-interface static factories or the `Concurrent` utility:

```java
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.ConcurrentTreeMap;

// Static factories on each interface
ConcurrentList<String>           list  = ConcurrentList.empty();
ConcurrentList<String>           seed  = ConcurrentList.of("a", "b", "c");
ConcurrentMap<String, Integer>   map   = ConcurrentMap.from(existingMap);
ConcurrentSet<String>            set   = ConcurrentSet.empty();
ConcurrentTreeMap<String, Long>  tree  = ConcurrentTreeMap.withComparator(byPriority);

// Zero-copy publication of a single-threaded build result
ConcurrentList<String> adopted = ConcurrentList.adopt(prebuilt);

list.add("hello");
map.put("key", 42);
set.add("unique");
```

> [!TIP]
> All concurrent collections use `ReadWriteLock` internally, allowing multiple
> concurrent readers while ensuring exclusive write access. Call
> `toUnmodifiable()` on any mutable instance to take a wait-free immutable
> snapshot backed by `NoOpReadWriteLock`.

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
│   │   ├── atomic/             # AtomicCollection, AtomicList, AtomicSet, AtomicMap,
│   │   │                       # AtomicQueue, AtomicDeque, AtomicNavigableSet,
│   │   │                       # AtomicNavigableMap, AtomicIterator
│   │   ├── function/           # TriConsumer, TriFunction, TriPredicate
│   │   ├── query/              # Searchable, SearchFunction, Sortable, SortOrder
│   │   ├── sort/               # Graph (topological sort)
│   │   ├── tuple/
│   │   │   ├── pair/           # Pair, ImmutablePair, MutablePair, PairOptional, PairStream
│   │   │   ├── single/         # SingleStream
│   │   │   └── triple/         # Triple, ImmutableTriple, MutableTriple, TripleStream
│   │   ├── unmodifiable/       # ConcurrentUnmodifiable{Collection,List,Set,Map,Queue,Deque,
│   │   │                       # LinkedList,LinkedSet,LinkedMap,TreeSet,TreeMap},
│   │   │                       # NoOpReadWriteLock
│   │   ├── Concurrent.java     # Factory for creating concurrent collections
│   │   ├── ConcurrentCollection.java
│   │   ├── ConcurrentList.java, ConcurrentSet.java, ConcurrentMap.java
│   │   ├── ConcurrentQueue.java, ConcurrentDeque.java
│   │   ├── ConcurrentLinkedList.java, ConcurrentLinkedSet.java, ConcurrentLinkedMap.java
│   │   ├── ConcurrentTreeSet.java, ConcurrentTreeMap.java
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
