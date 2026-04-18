# collections

Thread-safe concurrent collection library using ReadWriteLock-based atomic operations.

## Package Structure
- `dev.simplified.collection` - Concurrent, ConcurrentList/Map/Set/Deque/Queue, StreamUtil
- `dev.simplified.collection.atomic` - AtomicCollection (base), AtomicList, AtomicMap, AtomicSet, AtomicDeque, AtomicQueue, AtomicIterator
- `dev.simplified.collection.linked` - ConcurrentLinkedList, ConcurrentLinkedMap, ConcurrentLinkedSet
- `dev.simplified.collection.tree` - ConcurrentSortedMap, ConcurrentSortedSet
- `dev.simplified.collection.unmodifiable` - ConcurrentUnmodifiable{Collection,List,Map,Set,LinkedList}
- `dev.simplified.collection.tuple.pair` - Pair, ImmutablePair, MutablePair, PairOptional, PairStream
- `dev.simplified.collection.tuple.single` - SingleStream
- `dev.simplified.collection.tuple.triple` - Triple, ImmutableTriple, MutableTriple, TripleStream
- `dev.simplified.collection.query` - Searchable, SearchFunction, Sortable, SortOrder
- `dev.simplified.collection.sort` - Graph
- `dev.simplified.collection.function` - TriConsumer, TriFunction, TriPredicate

## Key Classes
- `Concurrent` - Factory for creating all concurrent collection types
- `AtomicCollection` - Abstract base with ReadWriteLock for all atomic collections
- `Pair`/`Triple` - Immutable + mutable variants with stream support

## Dependencies
- JetBrains annotations, Log4j2, Lombok
- JUnit 5, Hamcrest (test), JMH (benchmarks)
- No Simplified-Dev dependencies (foundational library)

## Build
```bash
./gradlew build
./gradlew test          # excludes @Tag("slow")
./gradlew slowTest      # runs only @Tag("slow")
./gradlew jmh           # benchmarks
```

## Stats
- Java 21, group `dev.simplified`, version `1.0.0`
- 43 source files, 3 test files, 4 JMH benchmarks
- Published via JitPack: `com.github.simplified-dev:collections:master-SNAPSHOT`
