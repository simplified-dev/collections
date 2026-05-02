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
  - [Sorting Algorithms](#sorting-algorithms)
- [Project Structure](#project-structure)
- [Building](#building)
  - [Running Tests](#running-tests)
  - [Benchmarks](#benchmarks)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Atomic bases** - `AtomicCollection`, `AtomicList`, `AtomicSet`, `AtomicMap`, `AtomicQueue`, `AtomicDeque`, plus the navigable bases `AtomicNavigableSet` and `AtomicNavigableMap`. Each is the canonical extension point for custom concurrent types and uses a `ReadWriteLock` for atomic guarantees.
- **Concurrent interfaces** - `ConcurrentCollection`, `ConcurrentList`, `ConcurrentMap`, `ConcurrentSet`, `ConcurrentDeque`, and `ConcurrentQueue` carry only contract methods - no nested `Impl`, no static factories.
- **Public impl classes** - 10 concrete classes named after the JDK type they wrap: `ConcurrentArrayList`, `ConcurrentLinkedList`, `ConcurrentHashSet`, `ConcurrentLinkedSet`, `ConcurrentTreeSet`, `ConcurrentHashMap`, `ConcurrentLinkedMap`, `ConcurrentTreeMap`, `ConcurrentArrayQueue`, `ConcurrentArrayDeque`. Construct directly with `new` (extra knobs like initial capacity, comparator, or max size are constructor params) or via `Xxx.adopt(backing)` for zero-copy publication.
- **Linked variants** - `ConcurrentLinkedList` (extends `AtomicList` directly, mirroring the JDK), `ConcurrentLinkedSet extends ConcurrentHashSet`, `ConcurrentLinkedMap extends ConcurrentHashMap` (insertion-ordered; `ConcurrentLinkedMap` supports an optional eldest-entry eviction cap via constructor).
- **Tree variants** - `ConcurrentTreeMap` and `ConcurrentTreeSet` for sorted/navigable views; backed by a `SortedSnapshotSpliterator` exposing `SORTED | ORDERED | DISTINCT` characteristics.
- **Unmodifiable snapshots** - Immutable wrappers live as package-private nested classes inside `ConcurrentUnmodifiable` (mirrors `Collections.UnmodifiableMap`). Pair an immutable snapshot with `NoOpReadWriteLock` for wait-free reads; obtain via `toUnmodifiable()` on any mutable instance or `Concurrent.newUnmodifiableX(...)`. `NoOpReadWriteLock` is `Serializable` so snapshots round-trip through serialization without losing their lock semantics.
- **`Concurrent` factory hub** - Static `newX(...)`, `toX(...)`, `adoptX(...)`, and `newUnmodifiableX(...)` helpers organized alphabetically; return types narrow to the concrete impl class (e.g. `newLinkedMap` returns `ConcurrentLinkedMap<K,V>`).
- **`ReadWriteLock`-based concurrency** - `withReadLock` / `withWriteLock` lambda helpers on `AtomicCollection` and `AtomicMap` wrap the lock + snapshot-invalidation pattern uniformly. `AtomicMap` exposes a `checkMutationAllowed` hook subclasses override to gate writes.
- **Tuple types** - `Pair`, `Triple`, and `Single` with mutable/immutable variants and streaming support (`PairStream`, `TripleStream`, `SingleStream`, `LifecycleSingleStream`).
- **Searchable / Sortable interfaces** - Generic query abstractions backing the search and sort surfaces.
- **Pluggable sorting algorithms** - `SortAlgorithm<E>` `@FunctionalInterface` plugged into `AtomicList.sorted(...)`. Pre-built strategies on `Comparison` (timsort/heap/insertion/shell/quicksort/mergesort), `RadixSort` (`byInt`/`byLong`), and `CountingSort` (`byInt` for bounded ranges). `Graph.asLinearSort()` / `asLayeredSort()` project graph topology as `SortAlgorithm` strategies. See [Sorting Algorithms](#sorting-algorithms) below.
- **Graph dependency ordering** - `Graph` exposes linear and layered topological sorts (iterative DFS post-order for sequential workloads, Kahn's BFS for parallel scheduling) plus structural queries (`successors`, `predecessors`, `roots`, `leaves`, `reverse`).
- **Stream utilities** - `StreamUtil` for enhanced stream operations including `distinctByKey` (parallel + sequential variants).
- **Functional interfaces** - `TriConsumer`, `TriFunction`, `TriPredicate`, `ToInt/Long/DoubleTriFunction`, `QuadFunction`, plus `IndexedConsumer/Function/Predicate`.
- **Optional Gson SPI** - `ConcurrentTypeAdapterFactory` registered via `META-INF/services` so consumers with Gson on the classpath get JSON support automatically; Gson is `compileOnly`, never pulled into runtime otherwise.

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

Create thread-safe collections by instantiating the public concrete classes directly or via the `Concurrent` factory hub:

```java
import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentArrayList;
import dev.simplified.collection.ConcurrentHashMap;
import dev.simplified.collection.ConcurrentHashSet;
import dev.simplified.collection.ConcurrentLinkedMap;
import dev.simplified.collection.ConcurrentTreeMap;

// Construct via the public impl classes
ConcurrentArrayList<String>      list  = new ConcurrentArrayList<>();
ConcurrentArrayList<String>      seed  = new ConcurrentArrayList<>("a", "b", "c");
ConcurrentHashMap<String, Long>  map   = new ConcurrentHashMap<>(existingMap);
ConcurrentHashSet<String>        set   = new ConcurrentHashSet<>();
ConcurrentTreeMap<String, Long>  tree  = new ConcurrentTreeMap<>(byPriority);
ConcurrentLinkedMap<String, Integer> lru = new ConcurrentLinkedMap<>(/* maxSize */ 1_000);

// Or go through the Concurrent factory hub (return types narrow to the concrete impl)
ConcurrentArrayList<String>      via   = Concurrent.newList("a", "b");
ConcurrentLinkedMap<String, Long> lhm  = Concurrent.newLinkedMap();

// Zero-copy publication of a single-threaded build result
ConcurrentArrayList<String> adopted = ConcurrentArrayList.adopt(prebuilt);

list.add("hello");
map.put("key", 42L);
set.add("unique");
```

> [!TIP]
> All concurrent collections use `ReadWriteLock` internally, allowing multiple
> concurrent readers while ensuring exclusive write access. Call
> `toUnmodifiable()` on any mutable instance to take a wait-free immutable
> snapshot backed by `NoOpReadWriteLock` - snapshots are `Serializable` and
> round-trip without losing their lock semantics.

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

### Sorting Algorithms

`AtomicList` (and its `ConcurrentArrayList` / `ConcurrentLinkedList` impls) hardcodes JDK Timsort via `sorted(Comparator)`. The pluggable `sorted(SortAlgorithm)` overload lets you opt into a workload-specific algorithm when Timsort isn't the right fit. The `SortAlgorithm<E>` type is a `@FunctionalInterface` so it accepts lambdas and pre-built factories alike:

```java
import dev.simplified.collection.ConcurrentArrayList;
import dev.simplified.collection.sort.Comparison;
import dev.simplified.collection.sort.CountingSort;
import dev.simplified.collection.sort.RadixSort;

ConcurrentArrayList<Integer> ids = new ConcurrentArrayList<>(/* large list of ints */);

// Default Timsort — sorted(Comparator) routes through Comparison.timsort internally
ConcurrentList<Integer> a = ids.sorted(Integer::compare);

// LSD base-256 radix on int keys — ~10× faster than Timsort once n >= 100k
ConcurrentList<Integer> b = ids.sorted(RadixSort.byInt(Integer::intValue));

// Counting sort — ~15-20× faster than Timsort when the key range is small
ConcurrentList<Integer> c = ids.sorted(CountingSort.byInt(Integer::intValue, 0, 99));

// Custom one-off via lambda
ConcurrentList<Integer> d = ids.sorted(list -> list.sort(Comparator.reverseOrder()));
```

**Algorithm families** (full Time/Space complexity in each method's javadoc):

| Family | Static factory | Best for |
|---|---|---|
| **Comparison** | `timsort(cmp)` | Default; partially-sorted real-world data |
|  | `heap(cmp)` | Strict O(n log n) worst case, low memory |
|  | `insertion(cmp)` | Tiny inputs (n ≤ ~32) |
|  | `shell(cmp)` | Mid-sized arrays, low memory |
|  | `quicksort(cmp)` | Random data, no stability needed |
|  | `mergesort(cmp)` | Stability + worst-case guarantee |
| **RadixSort** | `byInt(keyFn)` | Large int-keyed data (n ≥ ~10k) |
|  | `byLong(keyFn)` | Long-keyed data at small-medium n (~1k); ties Timsort at scale |
| **CountingSort** | `byInt(keyFn, min, max)` | Bounded-range int data (small `max - min`) |

`Graph` projects its topological order as a `SortAlgorithm`, letting you reorder arbitrary subsets by graph dependencies without re-running the sort:

```java
import dev.simplified.collection.sort.Graph;

Graph<EntityType> dependencies = Graph.<EntityType>builder()
    .withValues(allEntities)
    .withEdgeFunction(entity -> entity.referencedTypes())
    .build();

// Reusable algorithm — index map computed once at construction
SortAlgorithm<EntityType> dependencyOrder = dependencies.asLinearSort();

// Apply to any subset
List<EntityType> subset = userSelection();
dependencyOrder.sort(subset);

// Or feed straight into AtomicList.sorted
ConcurrentList<EntityType> sorted = concurrentEntities.sorted(dependencyOrder);
```

`asLayeredSort()` is the Kahn's-algorithm variant that buckets by topological layer while preserving each layer's input order via Timsort stability — useful when the input list already has a meaningful priority and you only need layer-respecting bucketing.

> [!TIP]
> Empirically measured deltas (random int data): radix beats Timsort by ~10× at n ≥ 100k; counting sort beats Timsort by ~15-20× on bounded ranges (k=100); insertion sort beats Timsort by ~30% at n ≤ 32; `Comparison.timsort` is byte-zero overhead vs raw `list.sort(cmp)`. The `Comparison`/`RadixSort`/`CountingSort` algorithm bodies all use `ListIterator`-based writeback so they're O(n) on `LinkedList`-backed lists, not O(n²).

## Project Structure

```
collections/
├── src/
│   ├── main/java/dev/simplified/collection/
│   │   ├── atomic/             # AtomicCollection, AtomicList, AtomicSet, AtomicMap,
│   │   │                       # AtomicQueue, AtomicDeque, AtomicNavigableSet,
│   │   │                       # AtomicNavigableMap, AtomicIterator,
│   │   │                       # SortedSnapshotSpliterator
│   │   ├── function/           # TriConsumer/Function/Predicate, ToInt/Long/DoubleTriFunction,
│   │   │                       # QuadFunction, IndexedConsumer/Function/Predicate
│   │   ├── gson/               # ConcurrentTypeAdapterFactory (opt-in Gson SPI)
│   │   ├── query/              # Searchable, SearchFunction, Sortable, SortOrder
│   │   ├── sort/               # SortAlgorithm (FunctionalInterface), Comparison,
│   │   │                       # RadixSort, CountingSort, Graph (linear/layered topo
│   │   │                       # sort + asLinearSort/asLayeredSort projections)
│   │   ├── tuple/
│   │   │   ├── pair/           # Pair, ImmutablePair, MutablePair, PairOptional, PairStream
│   │   │   ├── single/         # SingleStream, LifecycleSingleStream
│   │   │   └── triple/         # Triple, ImmutableTriple, MutableTriple, TripleStream
│   │   ├── Concurrent.java     # Factory hub; return types narrow to concrete impls
│   │   ├── ConcurrentUnmodifiable.java  # Package-private snapshot wrappers + NoOpReadWriteLock
│   │   ├── ConcurrentCollection.java, ConcurrentList.java, ConcurrentSet.java, ConcurrentMap.java
│   │   ├── ConcurrentQueue.java, ConcurrentDeque.java                # base interfaces
│   │   ├── ConcurrentArrayList.java, ConcurrentLinkedList.java       # list impls
│   │   ├── ConcurrentHashSet.java, ConcurrentLinkedSet.java, ConcurrentTreeSet.java
│   │   ├── ConcurrentHashMap.java, ConcurrentLinkedMap.java, ConcurrentTreeMap.java
│   │   ├── ConcurrentArrayQueue.java, ConcurrentArrayDeque.java
│   │   └── StreamUtil.java     # Stream utility methods
│   ├── test/java/              # JUnit 5 tests (ConcurrentListTest, ConcurrentMapTest, etc.)
│   └── jmh/java/               # 16 JMH benchmarks: per-impl + contention + 4 sort suites
│                               # (SortAlgorithm, SortAlgorithmLong, SortAlgorithmTiny,
│                               # SortAlgorithmBoundedRange)
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

Benchmarks cover every concrete impl (`ConcurrentArrayList`, `ConcurrentLinkedList`,
`ConcurrentHashSet`, `ConcurrentLinkedSet`, `ConcurrentTreeSet`, `ConcurrentHashMap`,
`ConcurrentLinkedMap`, `ConcurrentTreeMap`, `ConcurrentArrayQueue`, `ConcurrentArrayDeque`),
list/map contention scenarios under concurrent access, plus a four-class suite for the
[Sorting Algorithms](#sorting-algorithms): random ints, random longs, tiny inputs (insertion's
sweet spot), and bounded-range inputs (counting sort's sweet spot).

For focused runs, pass Gradle properties to filter or tune the JMH harness:

```bash
./gradlew jmh -PjmhInclude=ConcurrentTreeMapBenchmark -PjmhFork=1 -PjmhWarmup=2 -PjmhIter=3
```

| Property | Effect |
|----------|--------|
| `-PjmhInclude=<regex>` | Run only benchmarks whose class name matches the regex |
| `-PjmhFork=<n>` | Override the JVM fork count |
| `-PjmhWarmup=<n>` | Override warm-up iterations |
| `-PjmhIter=<n>` | Override measurement iterations |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, code style
guidelines, and how to submit a pull request.

## License

This project is licensed under the **Apache License 2.0** - see
[LICENSE.md](LICENSE.md) for the full text.
