# collections

Thread-safe concurrent collection library using ReadWriteLock-based atomic operations.

## Package Structure
- `dev.simplified.collection` - Concurrent (factory), ConcurrentCollection/List/Set/Map/Queue/Deque, plus the Linked* and Tree* variants, StreamUtil
- `dev.simplified.collection.atomic` - AtomicCollection (base), AtomicList, AtomicSet, AtomicMap, AtomicQueue, AtomicDeque, AtomicNavigableSet, AtomicNavigableMap, AtomicIterator
- `dev.simplified.collection.unmodifiable` - ConcurrentUnmodifiable{Collection,List,Set,Map,Queue,Deque,LinkedList,LinkedSet,LinkedMap,TreeSet,TreeMap}, NoOpReadWriteLock
- `dev.simplified.collection.tuple.pair` - Pair, ImmutablePair, MutablePair, PairOptional, PairStream
- `dev.simplified.collection.tuple.single` - SingleStream
- `dev.simplified.collection.tuple.triple` - Triple, ImmutableTriple, MutableTriple, TripleStream
- `dev.simplified.collection.query` - Searchable, SearchFunction, Sortable, SortOrder
- `dev.simplified.collection.sort` - Graph
- `dev.simplified.collection.function` - TriConsumer, TriFunction, TriPredicate
- `dev.simplified.collection.gson` - ConcurrentTypeAdapterFactory; opt-in Gson SPI shipped via `META-INF/services/com.google.gson.TypeAdapterFactory` (gson is `compileOnly`, only loaded when consumers have it on the classpath)

## Architecture
- `Atomic*` classes directly implement their corresponding `Concurrent*` interface; covariant returns let bulk operations live on the abstract base while still satisfying the interface contract.
- Each `Concurrent*` interface holds an inner `Impl` class that extends the matching `Atomic*` and acts as a thin construction shim.
- `protected withReadLock(Supplier|Runnable)` / `protected withWriteLock(...)` helpers on `AtomicCollection` and `AtomicMap` wrap the read/write lock + snapshot-invalidation pattern; CRUD bodies are one-liner lambdas, not hand-rolled try/finally.
- Lock-guarded sub-views (LockedNavigableMapView, LockedNavigableSetView, LockedEntrySetView, LockedValuesView) are protected non-static inner classes of `AtomicNavigableMap` / `AtomicNavigableSet`, capturing the enclosing instance's lock.
- `NoOpReadWriteLock` is `Serializable` with `readResolve`; lock fields on `AtomicCollection` / `AtomicMap` are non-transient so unmodifiable snapshots round-trip through serialization.

## Key Classes
- `Concurrent` - Factory for creating all concurrent collection types
- `AtomicCollection` - Abstract base with ReadWriteLock for all atomic collections; provides `withReadLock` / `withWriteLock` helpers
- `AtomicNavigableMap` / `AtomicNavigableSet` - Navigable bases hosting first/last/ceiling/floor/headMap/tailMap/subMap/descendingMap with atomic guarantees
- Each `Concurrent*` interface exposes static factories: `empty()`, `of(...)`, `from(...)`, `adopt(...)`. List adds `withCapacity(int)`; Tree variants add `withComparator(...)`; ConcurrentLinkedMap adds `withMaxSize(int)`.
- `Pair`/`Triple` - Immutable + mutable variants with stream support

## Dependencies
- JetBrains annotations, Log4j2, Lombok
- Gson (`compileOnly`, opt-in) - powers `ConcurrentTypeAdapterFactory`; absent from runtime unless the consumer pulls in Gson themselves
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
- 61 source files, 29 test files, 4 JMH benchmarks
- Published via JitPack: `com.github.simplified-dev:collections:master-SNAPSHOT`
