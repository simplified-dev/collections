# collections

Thread-safe concurrent collection library using ReadWriteLock-based atomic operations.

## Package Structure
- `dev.simplified.collection` - `Concurrent` (factory hub), six base interfaces (ConcurrentCollection/List/Set/Map/Queue/Deque), 10 public impl classes (ConcurrentArrayList, ConcurrentLinkedList, ConcurrentHashSet, ConcurrentLinkedSet, ConcurrentTreeSet, ConcurrentHashMap, ConcurrentLinkedMap, ConcurrentTreeMap, ConcurrentArrayQueue, ConcurrentArrayDeque), package-private `ConcurrentUnmodifiable` mega-factory housing all snapshot wrappers + `NoOpReadWriteLock`, `StreamUtil`
- `dev.simplified.collection.atomic` - AtomicCollection (base), AtomicList, AtomicSet, AtomicMap, AtomicQueue, AtomicDeque, AtomicNavigableSet, AtomicNavigableMap, AtomicIterator, SortedSnapshotSpliterator
- `dev.simplified.collection.tuple.pair` - Pair, ImmutablePair, MutablePair, PairOptional, PairStream
- `dev.simplified.collection.tuple.single` - SingleStream, LifecycleSingleStream
- `dev.simplified.collection.tuple.triple` - Triple, ImmutableTriple, MutableTriple, TripleStream
- `dev.simplified.collection.query` - Searchable, SearchFunction, Sortable, SortOrder
- `dev.simplified.collection.sort` - Graph (O(1) node lookup, iterative topological sort)
- `dev.simplified.collection.function` - TriConsumer/Function/Predicate, ToInt/Long/DoubleTriFunction, QuadFunction, IndexedConsumer/Function/Predicate
- `dev.simplified.collection.gson` - ConcurrentTypeAdapterFactory; opt-in Gson SPI shipped via `META-INF/services/com.google.gson.TypeAdapterFactory` (gson is `compileOnly`, only loaded when consumers have it on the classpath)

## Architecture
- Six base interfaces (`ConcurrentCollection/List/Set/Map/Queue/Deque`) carry only contract methods - no nested `Impl`, no static factories.
- 10 public impl classes named after the JDK type they wrap (`ConcurrentArrayList`, `ConcurrentHashMap`, etc.) extend the matching `Atomic*` directly and implement the relevant base interface. Each carries its own constructors and a `public static adopt(<concrete-backing>)` factory; no `empty()`/`of()`/`from()`/`with*` builders - extra knobs (initial capacity, comparator, max size) are constructor params.
- `ConcurrentLinkedList` does NOT extend `ConcurrentArrayList` (mirrors JDK - `LinkedList` doesn't extend `ArrayList`); both extend `AtomicList<E, List<E>>` directly. `ConcurrentLinkedSet extends ConcurrentHashSet` and `ConcurrentLinkedMap extends ConcurrentHashMap` (these DO mirror the JDK; `ConcurrentLinkedMap` accepts an optional max-size for eldest-entry eviction via constructor).
- `ConcurrentArrayQueue` and `ConcurrentArrayDeque` both back `ArrayDeque<E>` internally; the queue-only variant exists so consumers can enforce non-deque semantics at the type level. `ArrayDeque` rejects null elements - documented in their Javadoc.
- All snapshot wrappers live as package-private `static final` nested classes inside `ConcurrentUnmodifiable` (mirrors `Collections.UnmodifiableMap` and friends). Each `extends` the matching mutable impl, has a single package-private constructor `(backing)` calling `super(backing, NoOpReadWriteLock.INSTANCE)`, and overrides every mutating method to throw `UnsupportedOperationException`. `NoOpReadWriteLock` is also a nested class inside `ConcurrentUnmodifiable`.
- `protected withReadLock(Supplier|Runnable)` / `protected withWriteLock(...)` helpers on `AtomicCollection` and `AtomicMap` wrap the read/write lock + snapshot-invalidation pattern; CRUD bodies are one-liner lambdas, not hand-rolled try/finally. `AtomicMap` exposes a `checkMutationAllowed` hook subclasses override to gate writes (e.g. unmodifiable wrappers throw).
- Lock-guarded sub-views (LockedNavigableMapView, LockedNavigableSetView, LockedEntrySetView, LockedValuesView) are protected non-static inner classes of `AtomicNavigableMap` / `AtomicNavigableSet`, capturing the enclosing instance's lock.
- `SortedSnapshotSpliterator` provides `SORTED | ORDERED | DISTINCT` characteristics for tree-backed iteration; `LinkedHashMap`/`LinkedHashSet`-backed types override `spliterator()` to restore the `ORDERED` characteristic the JDK strips.
- `NoOpReadWriteLock` is `Serializable` with `readResolve` (aligns with `ReentrantReadWriteLock` contract); lock fields on `AtomicCollection` / `AtomicMap` are non-transient so unmodifiable snapshots round-trip through serialization.

## Key Classes
- `Concurrent` - Factory hub providing `newX(...)`, `toX(...)`, `adoptX(...)`, and `newUnmodifiableX(...)` helpers, organized alphabetically; return types narrow to the concrete impl class (e.g. `newLinkedMap` returns `ConcurrentLinkedMap<K,V>`). No `Collection`-flavored factories - use the `List` family instead.
- `ConcurrentUnmodifiable` - Package-private mega-factory housing all snapshot wrappers + `NoOpReadWriteLock`; never appears in any public type signature, only in stack traces and serialized form.
- `AtomicCollection` - Abstract base with ReadWriteLock for all atomic collections; provides `withReadLock` / `withWriteLock` helpers.
- `AtomicNavigableMap` / `AtomicNavigableSet` - Navigable bases hosting first/last/ceiling/floor/headMap/tailMap/subMap/descendingMap with atomic guarantees.
- `Pair`/`Triple` - Immutable + mutable variants with stream support; `Pair` caches its natural-order comparator.

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
./gradlew jmh           # benchmarks (toggles: -PjmhInclude=, -PjmhFork=, -PjmhWarmup=, -PjmhIter=)
```

## Stats
- Java 21, group `dev.simplified`, version `1.0.0`
- 56 source files, 30 test files, 12 JMH benchmarks
- Published via JitPack: `com.github.simplified-dev:collections:master-SNAPSHOT`
