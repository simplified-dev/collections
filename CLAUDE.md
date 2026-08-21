# collections

Thread-safe concurrent collection library using ReadWriteLock-based atomic operations.

## Package Structure
- `dev.simplified.collection` - `Concurrent` (factory hub), six base interfaces (ConcurrentCollection/List/Set/Map/Queue/Deque), 10 public impl classes (ConcurrentArrayList, ConcurrentLinkedList, ConcurrentHashSet, ConcurrentLinkedSet, ConcurrentTreeSet, ConcurrentHashMap, ConcurrentLinkedMap, ConcurrentTreeMap, ConcurrentArrayQueue, ConcurrentArrayDeque), package-private `ConcurrentUnmodifiable` mega-factory housing all snapshot wrappers + `NoOpReadWriteLock`, `StreamUtil`
- `dev.simplified.collection.atomic` - AtomicCollection (base), AtomicList, AtomicSet, AtomicMap, AtomicQueue, AtomicDeque, AtomicNavigableSet, AtomicNavigableMap, AtomicIterator, SortedSnapshotSpliterator
- `dev.simplified.collection.tuple.pair` - Pair, ImmutablePair, MutablePair, PairOptional, PairStream
- `dev.simplified.collection.tuple.single` - SingleStream, LifecycleSingleStream
- `dev.simplified.collection.tuple.triple` - Triple, ImmutableTriple, MutableTriple, TripleStream
- `dev.simplified.collection.query` - Searchable, Indexable, Sortable, SearchFunction, SortOrder, `@Indexed`, PropertyReference, IndexCache, package-private IndexSchema + LambdaBodyReader + SoleBucket
- `dev.simplified.collection.sort` - Graph (O(1) node lookup, iterative topological sort)
- `dev.simplified.collection.function` - TriConsumer/Function/Predicate, ToInt/Long/DoubleTriFunction, QuadFunction, IndexedConsumer/Function/Predicate
- `dev.simplified.collection.gson` - ConcurrentTypeAdapterFactory; opt-in Gson SPI shipped via `META-INF/services/com.google.gson.TypeAdapterFactory` (gson is `compileOnly`, only loaded when consumers have it on the classpath)

## Architecture
- Six base interfaces (`ConcurrentCollection/List/Set/Map/Queue/Deque`) carry only contract methods - no nested `Impl`, no static factories.
- 10 public impl classes named after the JDK type they wrap (`ConcurrentArrayList`, `ConcurrentHashMap`, etc.) extend the matching `Atomic*` directly and implement the relevant base interface. Each carries its own constructors and a `public static adopt(<concrete-backing>)` factory; no `empty()`/`of()`/`from()`/`with*` builders - extra knobs (initial capacity, comparator, max size) are constructor params.
- `ConcurrentLinkedList` does NOT extend `ConcurrentArrayList` (mirrors JDK - `LinkedList` doesn't extend `ArrayList`); both extend `AtomicList<E, List<E>>` directly. `ConcurrentLinkedSet extends ConcurrentHashSet` and `ConcurrentLinkedMap extends ConcurrentHashMap` (these DO mirror the JDK; `ConcurrentLinkedMap` accepts an optional max-size for eldest-entry eviction via constructor).
- `ConcurrentArrayQueue` and `ConcurrentArrayDeque` both back `ArrayDeque<E>` internally; the queue-only variant exists so consumers can enforce non-deque semantics at the type level. `ArrayDeque` rejects null elements - documented in their Javadoc.
- All snapshot wrappers live as package-private `static final` nested classes inside `ConcurrentUnmodifiable` (mirrors `Collections.UnmodifiableMap` and friends). Each `extends` the matching mutable impl, has a single package-private constructor `(backing)` calling `super(backing, NoOpReadWriteLock.INSTANCE)`, and overrides exactly one method - `checkModificationAllowed()` - to throw `UnsupportedOperationException`. `NoOpReadWriteLock` is also a nested class inside `ConcurrentUnmodifiable`.
- The backing collection is **private** to `AtomicCollection` / `AtomicMap` and is reached only as the argument the lock helpers hand to an action. A subclass therefore cannot name what it would have to lock, which is what turns "hold the lock" from a convention into the only spelling available. CRUD bodies are one-liner lambdas, not hand-rolled try/finally.
- Two names per lock, four shapes, and the name says what the action answers:

  | | answers something | answers nothing |
  |---|---|---|
  | doesn't need the backing | `withXLock(Supplier<R>)` | `execXLock(Runnable)` |
  | needs the backing | `withXLock(Function<T,R>)` | `execXLock(Consumer<T>)` |

  The zero-argument shapes are what the lock-guarded sub-views take, since they read through their own delegate rather than through what the owner holds.
- The split falls on "answers something" **because it has to fall somewhere**. Two one-argument shapes cannot share one name: a one-argument lambda whose parameter type is inferred is not pertinent to applicability, so javac never reads the body, cannot tell `Function` from `Consumer`, and reports every call site ambiguous. Arity settles everything else, so `Supplier`/`Function` share `withXLock` and `Runnable`/`Consumer` share `execXLock`. A method reference to an **overloaded** method (`delegate::toArray`) is inexact and hits the same wall - write those as an explicit lambda.
- **Every write in the library funnels through those two `withWriteLock` overloads**, which ask `checkModificationAllowed()` before taking the lock. That single choke point is the whole of how an unmodifiable snapshot refuses. A rejection written per method is a rejection that can be forgotten, which is exactly how `replace` and two `addIf` overloads once mutated a snapshot that had promised not to change.
- The mutators are deliberately **not** `final`. Sealing them was tried and reverted: it protects nothing the hook does not already protect - a wrapper extends its impl directly and overrides only the hook, so no downstream subclass can weaken one - and it would close an extension point on classes whose whole stated purpose is to be extended. An override that wants to stay safe writes through the lock helpers, which is now the only way to reach the backing collection at all.
- A mutator must therefore be written as `withWriteLock(...)` over `ref`, never as a hand-rolled lock or a call to a sibling mutator. `AtomicCollection.removeIf` and `AtomicList.replaceAll` override the JDK defaults for this reason: the inherited ones drive the snapshot iterator, which takes one write lock per element and only rejects when the predicate happens to match something.
- Lock-guarded sub-views (LockedNavigableMapView, LockedNavigableSetView, LockedEntrySetView, LockedValuesView) are protected non-static inner classes of `AtomicNavigableMap` / `AtomicNavigableSet`, capturing the enclosing instance's lock.
- `SortedSnapshotSpliterator` provides `SORTED | ORDERED | DISTINCT` characteristics for tree-backed iteration; `LinkedHashMap`/`LinkedHashSet`-backed types override `spliterator()` to restore the `ORDERED` characteristic the JDK strips.
- `NoOpReadWriteLock` is `Serializable` with `readResolve` (aligns with `ReentrantReadWriteLock` contract); lock fields on `AtomicCollection` / `AtomicMap` are non-transient so unmodifiable snapshots round-trip through serialization.

## Indexing
- `Searchable` -> `Indexable` -> `Sortable` is the query chain; `ConcurrentCollection extends Indexable`, so every list/set/queue/deque inherits indexing. `ConcurrentMap` stays `Searchable` - an entry's owner is `Map.Entry`, so `@Indexed` on a value class is a hop away and nothing indexes it yet.
- Two layers reach the index. `Indexable` overrides the two `Iterable` terminals - `findAll(Match, Iterable)` and `containsAll(Match, Iterable)` - which every multi-predicate finder funnels through, so nothing above them is rewritten and nothing can be missed. On top of that, the **single-property** finders (`Searchable.findAll`/`containsAll`, `Sortable.findFirst`/`findLast`/`containsFirst` and their `*OrNull` forms, all in their `(Match, SearchFunction, value)` shape) probe the index themselves and, on a refusal, build the `Pair` the scan needs and fall through unchanged. That second layer is pure allocation avoidance: the marshalling (a `Pair`, a varargs array, `Arrays.asList`, a stream pipeline, an `Optional`) exists only to reach the scan, so it is built where the scan is. `compare`/`contains(Match, TriPredicate, ...)` are never indexed: a caller-supplied comparison is not a question an index over values can answer.
- A single predicate asks the same question in either `Match` mode, which is what lets those fast paths ignore the mode until they fall through. `Indexable.indexable` says the same thing for the general path.
- Every extractor parameter on a **finder** (`Searchable`, `Sortable`, `ConcurrentCollection.contains`) is a `SearchFunction`, because `SearchFunction extends Serializable` is what makes javac emit the `writeReplace` that `PropertyReference` cracks. The parameter type at the call site is what decides this: changing one back to `Function` silently disables indexing for every caller of it, which `PropertyReferenceTest.of_andThenWithAPlainFunction_isRefused` pins.
- `ConcurrentList.sorted` deliberately keeps plain `Function`. Sorting never reaches an index - `AtomicList` names `PropertyReference`/`IndexCache` nowhere - so requiring `SearchFunction` there would break callers holding a `Function` for no gain. Extractor type follows what the method does, not a module-wide rule.
- `PropertyReference.of(SearchFunction)` decodes an unbound method reference from the constant pool, a lambda body through ASM (`LambdaBodyReader`), and a `SearchFunction.Composed` structurally. Anything else - a bound reference, a capturing lambda, arithmetic, a branch, ASM absent - answers `Kind.UNRESOLVED` and the caller scans. Decodes are memoised in a `ClassValue` keyed on the extractor's class, which is stable per call site.
- A composition is the exception to that memo and carries its own. `SearchFunction.Composed` is one class holding every chain anyone writes, so its class is no memo key at all - it is a `final class` rather than a record precisely so it can hold the joined path in a field, filled on the first query that names it. The race there is benign: two threads join the same two (already memoised) halves and either immutable answer stands. `IndexedPathBenchmark` pins the result at 0.018 B/op against 120 B before it.
- ASM is `compileOnly`. `LambdaBodyReader` is reached only behind the `Class.forName` probe in `PropertyReference.Asm`; calling it directly from a new site would `NoClassDefFoundError` for consumers without ASM.
- ASM is on the `test` runtime and **not** on the `jmh` one, so a benchmark written against a lambda-bodied extractor silently measures the scan rather than the index. Benchmark with an unbound method reference, which is decoded from the constant pool and needs no ASM, or add `jmh(libs.asm)` before trusting the number.
- `@Indexed` is opt-in per field, carries `unique`, and is `@Repeatable` so one field can be both a standalone index and a member of a `group`. `IndexSchema` reads it once per class into a `ClassValue`.
- `@Indexed` on a field holding another object **also** indexes what that object's own type declares, reached through it - so `person.department.name` is a hash probe with no path string written anywhere. There is no second opt-in flag: both ends already declared, the target saying what it is worth finding by and the holder saying it cares about that field. A reached index is never `unique` (two people share a department even though department names are unique), only single-component declarations are exported (a composite belongs to the class owning all its values), a collection-typed field is indexed by containment but not reached through (that would be a join), and a path stops at `IndexSchema.MAX_HOPS` = 3 accessors because references form a graph.
- `IndexSchema.readLocal` skips `static` fields - one value for every element would answer every query with the whole collection.
- `IndexSchema` keys declarations on `List<PropertyReference>` - one entry per value a query compares - **not** a flat `List<String>`. That distinction is load-bearing: `(department.name)` is one component of two hops and `(mode, tier)` is two components of one hop each, and a flat list conflates them.
- **The single-predicate path allocates nothing at all**, end to end: `findFirstOrNull(fn, value)` against an index is 0.0 B/op, hit or miss, and `findFirst` is 16.0 B/op - the `Optional` and nothing else. The chain is `IndexCache.lookup(reference, extractor, value)` → `IndexSchema.coveringPath` → declaration-keyed map → pre-sealed bucket. It is nearly every real query, so anything added to it - a marshalling list, a `Set.copyOf`, a `PropertyReference.against`, a key object, or a capturing lambda handed to `computeIfAbsent` - shows up directly. Verify with `./gradlew jmh -PjmhInclude=IndexedLookupBenchmark -PjmhProfilers=gc` and read `gc.alloc.rate.norm`, which is deterministic where throughput on a loaded machine is not.
- `IndexCache.lookup` reads its declaration-keyed map before offering `computeIfAbsent` a build. A mapping function that closes over anything is minted at the call site whether or not the map calls it, and this runs on every query where the build runs once.
- **Buckets live in one flat, open-addressed table** inside `IndexCache.Index` - a key at every even slot, whatever is filed under it at the odd slot after it, probed linearly from a mixed hash. A build walks every element, so the per-entry node a chained map mints is the largest cost of building at all. A null key is held under a private `NULL_KEY` sentinel so an empty slot stays distinguishable; `Bucket extends ArrayList` is the private marker that tells a many-element bucket from a bare element, because an element can itself be any list.
- A bucket of one seals into `SoleBucket`, a 16-byte one-element `List` - the platform's narrowest immutable list is 24, carrying a second element slot it never fills. That is the shape of a unique index and of any index over a property few elements share, so it is most of what holding an index costs. `SoleBucketTest` pins the `List` contract it has to keep.
- `IndexCache.over` answers the shared `none()` when the element class declares nothing, so a collection nobody indexed does not mint a cache per write for queries that can only be refused.
- Sealing at build is deliberate and is **not** to be traded away: a build happens once per write generation and a lookup happens on every query, so holding elements bare and wrapping them on the way out moves allocation onto the hot path this exists for. Whatever else changes, prove with `-prof gc` that the hit path did not regress.
- Every optimisation here reduces allocation **and** work. Beware of ones that trade: donating unchanged buckets from the previous generation's index would cut a rebuild's allocation roughly in half, but it buys that with a random probe into a table wider than L2 per key, to avoid an allocation that dies before the next young collection ever looks at it. Allocation is a proxy metric; short-lived garbage is nearly free to collect.
- Resolving a schema reads other classes' schemas, so the reflective read (`IndexSchema.LOCAL`) is a separate `ClassValue` from the resolved one (`SCHEMAS`). A cycle must never re-enter `computeValue` for a class already being computed.
- The index is **invalidated on write and rebuilt lazily**, mirroring `snapshotCache`. The drop lives in `AtomicCollection.invalidateSnapshot()` rather than in the `onSnapshotInvalidated()` hook, because `AtomicNavigableSet` and `AtomicNavigableMap` override that hook without calling `super`.
- An indexed answer must equal the scanned answer element-for-element in source order; `IndexableTest` is the differential harness that pins it (indexed class vs identical undeclared class), and its accessor-call counters are what prove the index is actually serving rather than merely agreeing.

## Key Classes
- `Concurrent` - Factory hub providing `newX(...)`, `toX(...)`, `adoptX(...)`, and `newUnmodifiableX(...)` helpers, organized alphabetically; return types narrow to the concrete impl class (e.g. `newLinkedMap` returns `ConcurrentLinkedMap<K,V>`). No `Collection`-flavored factories - use the `List` family instead.
- `ConcurrentUnmodifiable` - Package-private mega-factory housing all snapshot wrappers + `NoOpReadWriteLock`; never appears in any public type signature, only in stack traces and serialized form.
- `AtomicCollection` - Abstract base with ReadWriteLock for all atomic collections; provides `withReadLock` / `withWriteLock` helpers.
- `AtomicNavigableMap` / `AtomicNavigableSet` - Navigable bases hosting first/last/ceiling/floor/headMap/tailMap/subMap/descendingMap with atomic guarantees.
- `Pair`/`Triple` - Immutable + mutable variants with stream support; `Pair` caches its natural-order comparator.

## Dependencies
- JetBrains annotations, Log4j2, Simplified Annotations
- ASM (`compileOnly`, opt-in) - powers `LambdaBodyReader`; absent, a lambda-bodied extractor decodes to `UNRESOLVED` and its query scans
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
- 68 source files, 41 test files, 18 JMH benchmarks
- Published via JitPack: `com.github.simplified-dev:collections:master-SNAPSHOT`
