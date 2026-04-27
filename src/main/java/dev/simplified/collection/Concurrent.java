package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicDeque;
import dev.simplified.collection.atomic.AtomicQueue;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import dev.simplified.collection.linked.ConcurrentLinkedMap;
import dev.simplified.collection.linked.ConcurrentLinkedSet;
import dev.simplified.collection.tree.ConcurrentTreeMap;
import dev.simplified.collection.tree.ConcurrentTreeSet;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableCollection;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableDeque;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedList;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedMap;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedSet;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableList;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableMap;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableQueue;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableSet;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeMap;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A utility class providing static factory methods for creating thread-safe
 * concurrent collection, queue, set, list, and map implementations with
 * linked and unmodifiable counterparts.
 * <p>
 * It also provides concurrent {@link Collector} instances for use with {@link Stream#collect}.
 */
@UtilityClass
public final class Concurrent {

	/**
	 * Collector characteristics for ordered collectors with identity finish.
	 * <p>
	 * {@link Collector.Characteristics#CONCURRENT CONCURRENT} is intentionally omitted: the
	 * underlying accumulators serialize mutations through a {@code ReadWriteLock} write lock,
	 * so advertising concurrent accumulation would force parallel-stream workers to queue on
	 * a single lock instead of combining per-thread partials via the collector's combiner.
	 */
	public static final Set<Collector.Characteristics> ORDERED_CHARACTERISTICS = Set.of(Collector.Characteristics.IDENTITY_FINISH);
	/**
	 * Collector characteristics for unordered collectors with identity finish.
	 * <p>
	 * {@link Collector.Characteristics#CONCURRENT CONCURRENT} is intentionally omitted for the
	 * same reason as {@link #ORDERED_CHARACTERISTICS}.
	 */
	public static final Set<Collector.Characteristics> UNORDERED_CHARACTERISTICS = Set.of(Collector.Characteristics.IDENTITY_FINISH, Collector.Characteristics.UNORDERED);
	/**
	 * Collector characteristics for ordered collectors whose finisher transforms the
	 * accumulator (e.g. wraps it in an unmodifiable view) and must not be elided.
	 */
	private static final Set<Collector.Characteristics> ORDERED_FINISHING_CHARACTERISTICS = Set.of();
	/**
	 * Collector characteristics for unordered collectors whose finisher transforms the
	 * accumulator (e.g. wraps it in an unmodifiable view) and must not be elided.
	 */
	private static final Set<Collector.Characteristics> UNORDERED_FINISHING_CHARACTERISTICS = Set.of(Collector.Characteristics.UNORDERED);

	private static final @NotNull BinaryOperator<Object> THROWING_MERGER = (key, value) -> { throw new IllegalStateException(String.format("Duplicate key %s", key)); };

	/**
	 * Returns a merge function that always throws {@link IllegalStateException} on duplicate keys.
	 *
	 * @param <T> the type of the values being merged
	 * @return a merge function that rejects duplicates
	 */
	@SuppressWarnings("unchecked")
	private static <T> @NotNull BinaryOperator<T> throwingMerger() {
		return (BinaryOperator<T>) THROWING_MERGER;
	}

	/**
	 * Creates a new empty {@link ConcurrentCollection.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent collection
	 */
	public static <E> @NotNull ConcurrentCollection<E> newCollection() {
		return new ConcurrentCollection.Impl<>();
	}

	/**
	 * Creates a new {@link ConcurrentCollection.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent collection containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentCollection<E> newCollection(@NotNull E... array) {
		return new ConcurrentCollection.Impl<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentCollection.Impl} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent collection containing the source elements
	 */
	public static <E> @NotNull ConcurrentCollection<E> newCollection(@NotNull Collection<? extends E> collection) {
		return new ConcurrentCollection.Impl<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentDeque.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent deque
	 */
	public static <E> @NotNull ConcurrentDeque<E> newDeque() {
		return new ConcurrentDeque.Impl<>();
	}

	/**
	 * Creates a new {@link ConcurrentDeque.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent deque containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentDeque<E> newDeque(@NotNull E... array) {
		return new ConcurrentDeque.Impl<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentDeque.Impl} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent deque containing the source elements
	 */
	public static <E> @NotNull ConcurrentDeque<E> newDeque(@NotNull Collection<? extends E> collection) {
		return new ConcurrentDeque.Impl<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentList.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent list
	 */
	public static <E> @NotNull ConcurrentList<E> newList() {
		return new ConcurrentList.Impl<>();
	}

	/**
	 * Creates a new {@link ConcurrentList.Impl} containing the given elements.
	 *
	 * @param initialCapacity the initial capacity of the underlying list
	 * @param <E>             the element type
	 * @return a new concurrent list containing the specified elements
	 */
	public static <E> @NotNull ConcurrentList<E> newList(int initialCapacity) {
		return new ConcurrentList.Impl<>(initialCapacity);
	}

	/**
	 * Creates a new {@link ConcurrentList.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentList<E> newList(@NotNull E... array) {
		return new ConcurrentList.Impl<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentList.Impl} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent list containing the source elements
	 */
	public static <E> @NotNull ConcurrentList<E> newList(@Nullable Collection<? extends E> collection) {
		return new ConcurrentList.Impl<>(collection);
	}

	/**
	 * Creates a new {@link ConcurrentMap.Impl} containing the given map entries.
	 *
	 * @param entries the entries to include
	 * @param <K>     the key type
	 * @param <V>     the value type
	 * @return a new concurrent map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentMap<K, V> newMap(@NotNull Map.Entry<K, V>... entries) {
		return new ConcurrentMap.Impl<>(entries);
	}

	/**
	 * Creates a new {@link ConcurrentMap.Impl} containing all entries from the given map.
	 *
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent map containing the source entries
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentMap.Impl<>(map);
	}

	/**
	 * Creates a new empty {@link ConcurrentQueue.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent queue
	 */
	public static <E> @NotNull ConcurrentQueue<E> newQueue() {
		return new ConcurrentQueue.Impl<>();
	}

	/**
	 * Creates a new {@link ConcurrentQueue.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent queue containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentQueue<E> newQueue(@NotNull E... array) {
		return new ConcurrentQueue.Impl<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentQueue.Impl} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent queue containing the source elements
	 */
	public static <E> @NotNull ConcurrentQueue<E> newQueue(@NotNull Collection<? extends E> collection) {
		return new ConcurrentQueue.Impl<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentSet.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent set
	 */
	public static <E> @NotNull ConcurrentSet<E> newSet() {
		return new ConcurrentSet.Impl<>();
	}

	/**
	 * Creates a new {@link ConcurrentSet.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentSet<E> newSet(@NotNull E... array) {
		return new ConcurrentSet.Impl<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentSet.Impl} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent set containing the source elements
	 */
	public static <E> @NotNull ConcurrentSet<E> newSet(@Nullable Collection<? extends E> collection) {
		return new ConcurrentSet.Impl<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedList.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent linked list
	 */
	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList() {
		return new ConcurrentLinkedList.Impl<>();
	}

	/**
	 * Creates a new {@link ConcurrentLinkedList.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent linked list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList(@NotNull E... array) {
		return new ConcurrentLinkedList.Impl<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedList.Impl} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent linked list containing the source elements
	 */
	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList(@NotNull Collection<? extends E> collection) {
		return new ConcurrentLinkedList.Impl<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedMap.Impl} with no maximum size constraint.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty concurrent linked map
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap() {
		return newLinkedMap(-1);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedMap.Impl} with the specified maximum size.
	 * A value of {@code -1} indicates no size limit.
	 *
	 * @param maxSize the maximum number of entries, or {@code -1} for unlimited
	 * @param <K>     the key type
	 * @param <V>     the value type
	 * @return a new empty concurrent linked map with the given size constraint
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap(int maxSize) {
		return new ConcurrentLinkedMap.Impl<>(maxSize);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedMap.Impl} containing all entries from the given map.
	 *
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent linked map containing the source entries
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentLinkedMap.Impl<>(map);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedMap.Impl} containing all entries from the given map, with a maximum size.
	 *
	 * @param map the source map to copy from
	 * @param maxSize the maximum number of entries, or {@code -1} for unlimited
	 * @param <K>     the key type
	 * @param <V>     the value type
	 * @return a new concurrent linked map containing the source entries with the given size constraint
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap(@NotNull Map<? extends K, ? extends V> map, int maxSize) {
		return new ConcurrentLinkedMap.Impl<>(map, maxSize);
	}

	/**
	 * Creates a new empty {@link ConcurrentTreeMap.Impl} with natural key ordering.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty concurrent tree map
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap() {
		return new ConcurrentTreeMap.Impl<>();
	}

	/**
	 * Creates a new empty {@link ConcurrentTreeMap.Impl} with the specified comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @return a new empty concurrent tree map ordered by the given comparator
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Comparator<? super K> comparator) {
		return new ConcurrentTreeMap.Impl<>(comparator);
	}

	/**
	 * Creates a new {@link ConcurrentTreeMap.Impl} containing the given entries, with natural
	 * key ordering.
	 *
	 * @param pairs the entries to include
	 * @param <K>   the key type
	 * @param <V>   the value type
	 * @return a new concurrent tree map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Map.Entry<K, V>... pairs) {
		return new ConcurrentTreeMap.Impl<>(pairs);
	}

	/**
	 * Creates a new {@link ConcurrentTreeMap.Impl} containing the given entries, ordered by the
	 * specified comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param pairs      the entries to include
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @return a new concurrent tree map containing the specified entries ordered by the given comparator
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Comparator<? super K> comparator, @NotNull Map.Entry<K, V>... pairs) {
		return new ConcurrentTreeMap.Impl<>(comparator, pairs);
	}

	/**
	 * Creates a new {@link ConcurrentTreeMap.Impl} containing all entries from the given map,
	 * with natural key ordering.
	 *
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent tree map containing the source entries
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentTreeMap.Impl<>(map);
	}

	/**
	 * Creates a new {@link ConcurrentTreeMap.Impl} containing all entries from the given map,
	 * ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param map        the source map to copy from
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @return a new concurrent tree map containing the source entries ordered by the given comparator
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Comparator<? super K> comparator, @NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentTreeMap.Impl<>(comparator, map);
	}

	/**
	 * Creates a new empty {@link ConcurrentTreeSet.Impl} with natural element ordering.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent tree set
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet() {
		return new ConcurrentTreeSet.Impl<>();
	}

	/**
	 * Creates a new empty {@link ConcurrentTreeSet.Impl} with the specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E>        the element type
	 * @return a new empty concurrent tree set ordered by the given comparator
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator) {
		return new ConcurrentTreeSet.Impl<>(comparator);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet.Impl} containing the given elements, with natural
	 * element ordering.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent tree set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull E... array) {
		return new ConcurrentTreeSet.Impl<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet.Impl} containing the given elements, ordered by the
	 * specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param array      the elements to include
	 * @param <E>        the element type
	 * @return a new concurrent tree set containing the specified elements ordered by the given comparator
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator, @NotNull E... array) {
		return new ConcurrentTreeSet.Impl<>(comparator, array);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet.Impl} containing all elements from the given collection,
	 * with natural element ordering.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent tree set containing the source elements
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentTreeSet.Impl<>(collection);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet.Impl} containing all elements from the given collection,
	 * ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent tree set containing the source elements ordered by the given comparator
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator, @NotNull Collection<? extends E> collection) {
		return new ConcurrentTreeSet.Impl<>(comparator, collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedSet.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent linked set
	 */
	public static <E> @NotNull ConcurrentLinkedSet<E> newLinkedSet() {
		return new ConcurrentLinkedSet.Impl<>();
	}

	/**
	 * Creates a new {@link ConcurrentLinkedSet.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent linked set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentLinkedSet<E> newLinkedSet(@NotNull E... array) {
		return new ConcurrentLinkedSet.Impl<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedSet.Impl} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent linked set containing the source elements
	 */
	public static <E> @NotNull ConcurrentLinkedSet<E> newLinkedSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentLinkedSet.Impl<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableCollection.Impl} backed by a fresh
	 * {@link ConcurrentCollection.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent collection
	 */
	public static <E> @NotNull ConcurrentUnmodifiableCollection<E> newUnmodifiableCollection() {
		return new ConcurrentUnmodifiableCollection.Impl<>(new java.util.ArrayList<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableCollection.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent collection containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableCollection<E> newUnmodifiableCollection(@NotNull E... array) {
		return new ConcurrentUnmodifiableCollection.Impl<>(new java.util.ArrayList<>(java.util.Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected. If the source is itself a {@link ConcurrentCollection.Impl},
	 * its {@code toUnmodifiable()} is delegated to so its read lock guards the copy.</p>
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return a snapshot {@link ConcurrentUnmodifiableCollection.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableCollection<E> newUnmodifiableCollection(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentCollection)
			return (ConcurrentUnmodifiableCollection<E>) ((ConcurrentCollection<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiableCollection.Impl<>(new java.util.ArrayList<>(collection));
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableList.Impl} backed by a fresh
	 * {@link ConcurrentList.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent list
	 */
	public static <E> @NotNull ConcurrentUnmodifiableList<E> newUnmodifiableList() {
		return new ConcurrentUnmodifiableList.Impl<>(new java.util.ArrayList<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableList.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableList<E> newUnmodifiableList(@NotNull E... array) {
		return new ConcurrentUnmodifiableList.Impl<>(new java.util.ArrayList<>(java.util.Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a list.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected. If the source is itself a {@link ConcurrentList.Impl},
	 * its {@code toUnmodifiable()} is delegated to so the source's read lock guards the copy
	 * and any type-specific iteration order ({@link ConcurrentLinkedList.Impl}) is preserved via
	 * the source's {@code cloneRef} override.</p>
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return a snapshot {@link ConcurrentUnmodifiableList.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableList<E> newUnmodifiableList(@Nullable Collection<? extends E> collection) {
		if (collection == null) return newUnmodifiableList();
		// Narrow the shortcut: ConcurrentLinkedList.Impl overrides toUnmodifiable() to return
		// ConcurrentUnmodifiableLinkedList.Impl (a sibling, not a subtype, of ConcurrentUnmodifiableList.Impl),
		// so let LinkedList variants fall through to the ArrayList-backed snapshot copy.
		if (collection instanceof ConcurrentList && !(collection instanceof ConcurrentLinkedList))
			return (ConcurrentUnmodifiableList<E>) ((ConcurrentList<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiableList.Impl<>(new java.util.ArrayList<>(collection));
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableMap.Impl} containing the given map entries.
	 *
	 * @param entries the entries to include
	 * @param <K>     the key type
	 * @param <V>     the value type
	 * @return a new unmodifiable concurrent map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentUnmodifiableMap<K, V> newUnmodifiableMap(@NotNull Map.Entry<K, V>... entries) {
		java.util.HashMap<K, V> snapshot = new java.util.HashMap<>();
		for (Map.Entry<K, V> entry : entries) {
			if (entry != null) snapshot.put(entry.getKey(), entry.getValue());
		}
		return new ConcurrentUnmodifiableMap.Impl<>(snapshot);
	}

	/**
	 * Creates an immutable snapshot of the given map.
	 *
	 * <p>The snapshot copies the input's entries at construction time; subsequent mutations
	 * on the source are not reflected. If the source is itself a {@link ConcurrentMap.Impl},
	 * its {@code toUnmodifiable()} is delegated to so the source's read lock guards the copy
	 * and any type-specific iteration order ({@link ConcurrentLinkedMap.Impl}, {@link ConcurrentTreeMap.Impl})
	 * is preserved via the source's {@code cloneRef} override.</p>
	 *
	 * @param map the source map
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a snapshot {@link ConcurrentUnmodifiableMap.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> @NotNull ConcurrentUnmodifiableMap<K, V> newUnmodifiableMap(@NotNull Map<? extends K, ? extends V> map) {
		// Narrow the shortcut: ConcurrentTreeMap.Impl and ConcurrentLinkedMap.Impl override
		// toUnmodifiable() to return their own UnmodifiableTreeMap/UnmodifiableLinkedMap
		// (siblings, not subtypes, of ConcurrentUnmodifiableMap.Impl), so let those variants
		// fall through to the HashMap-backed snapshot copy.
		if (map instanceof ConcurrentMap && !(map instanceof ConcurrentTreeMap) && !(map instanceof ConcurrentLinkedMap))
			return (ConcurrentUnmodifiableMap<K, V>) ((ConcurrentMap<K, V>) map).toUnmodifiable();

		return new ConcurrentUnmodifiableMap.Impl<>(new java.util.HashMap<>(map));
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableSet.Impl} backed by a fresh
	 * {@link ConcurrentSet.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent set
	 */
	public static <E> @NotNull ConcurrentUnmodifiableSet<E> newUnmodifiableSet() {
		return new ConcurrentUnmodifiableSet.Impl<>(new java.util.HashSet<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableSet.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableSet<E> newUnmodifiableSet(@NotNull E... array) {
		return new ConcurrentUnmodifiableSet.Impl<>(new java.util.HashSet<>(java.util.Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a set.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected. If the source is itself a {@link ConcurrentSet.Impl}, its
	 * {@code toUnmodifiable()} is delegated to so the source's read lock guards the copy and
	 * any type-specific iteration order ({@link ConcurrentLinkedSet.Impl}, {@link ConcurrentTreeSet.Impl})
	 * is preserved via the source's {@code cloneRef} override.</p>
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return a snapshot {@link ConcurrentUnmodifiableSet.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableSet<E> newUnmodifiableSet(@NotNull Collection<? extends E> collection) {
		// Narrow the shortcut: ConcurrentTreeSet.Impl and ConcurrentLinkedSet.Impl override
		// toUnmodifiable() to return their own UnmodifiableTreeSet/UnmodifiableLinkedSet
		// (siblings, not subtypes, of ConcurrentUnmodifiableSet.Impl), so let those variants
		// fall through to the HashSet-backed snapshot copy.
		if (collection instanceof ConcurrentSet && !(collection instanceof ConcurrentTreeSet) && !(collection instanceof ConcurrentLinkedSet))
			return (ConcurrentUnmodifiableSet<E>) ((ConcurrentSet<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiableSet.Impl<>(new java.util.HashSet<>(collection));
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableQueue.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent queue
	 */
	public static <E> @NotNull ConcurrentUnmodifiableQueue<E> newUnmodifiableQueue() {
		return new ConcurrentUnmodifiableQueue.Impl<>(new ConcurrentQueue.Impl<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableQueue.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent queue containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableQueue<E> newUnmodifiableQueue(@NotNull E... array) {
		return new ConcurrentUnmodifiableQueue.Impl<>(new ConcurrentQueue.Impl<>(array));
	}

	/**
	 * Creates an immutable snapshot of the given queue.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected.</p>
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return a snapshot {@link ConcurrentUnmodifiableQueue.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableQueue<E> newUnmodifiableQueue(@NotNull Collection<? extends E> collection) {
		AtomicQueue<E> source = collection instanceof AtomicQueue
			? (AtomicQueue<E>) collection
			: new ConcurrentQueue.Impl<>((Collection<E>) collection);
		return new ConcurrentUnmodifiableQueue.Impl<>(source);
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableDeque.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent deque
	 */
	public static <E> @NotNull ConcurrentUnmodifiableDeque<E> newUnmodifiableDeque() {
		return new ConcurrentUnmodifiableDeque.Impl<>(new ConcurrentDeque.Impl<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableDeque.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent deque containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableDeque<E> newUnmodifiableDeque(@NotNull E... array) {
		return new ConcurrentUnmodifiableDeque.Impl<>(new ConcurrentDeque.Impl<>(array));
	}

	/**
	 * Creates an immutable snapshot of the given deque.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected.</p>
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return a snapshot {@link ConcurrentUnmodifiableDeque.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableDeque<E> newUnmodifiableDeque(@NotNull Collection<? extends E> collection) {
		AtomicDeque<E> source = collection instanceof AtomicDeque
			? (AtomicDeque<E>) collection
			: new ConcurrentDeque.Impl<>((Collection<E>) collection);
		return new ConcurrentUnmodifiableDeque.Impl<>(source);
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableLinkedList.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent linked list
	 */
	public static <E> @NotNull ConcurrentUnmodifiableLinkedList<E> newUnmodifiableLinkedList() {
		return new ConcurrentUnmodifiableLinkedList.Impl<>(new java.util.LinkedList<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableLinkedList.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent linked list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableLinkedList<E> newUnmodifiableLinkedList(@NotNull E... array) {
		return new ConcurrentUnmodifiableLinkedList.Impl<>(new java.util.LinkedList<>(java.util.Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a linked list, preserving
	 * insertion order.
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return a snapshot {@link ConcurrentUnmodifiableLinkedList.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableLinkedList<E> newUnmodifiableLinkedList(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentLinkedList)
			return (ConcurrentUnmodifiableLinkedList<E>) ((ConcurrentLinkedList<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiableLinkedList.Impl<>(new java.util.LinkedList<>(collection));
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableLinkedSet.Impl}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent linked set
	 */
	public static <E> @NotNull ConcurrentUnmodifiableLinkedSet<E> newUnmodifiableLinkedSet() {
		return new ConcurrentUnmodifiableLinkedSet.Impl<>(new java.util.LinkedHashSet<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableLinkedSet.Impl} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent linked set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableLinkedSet<E> newUnmodifiableLinkedSet(@NotNull E... array) {
		return new ConcurrentUnmodifiableLinkedSet.Impl<>(new java.util.LinkedHashSet<>(java.util.Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a linked set, preserving
	 * insertion order.
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return a snapshot {@link ConcurrentUnmodifiableLinkedSet.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableLinkedSet<E> newUnmodifiableLinkedSet(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentLinkedSet)
			return (ConcurrentUnmodifiableLinkedSet<E>) ((ConcurrentLinkedSet<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiableLinkedSet.Impl<>(new java.util.LinkedHashSet<>(collection));
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableLinkedMap.Impl}.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty unmodifiable concurrent linked map
	 */
	public static <K, V> @NotNull ConcurrentUnmodifiableLinkedMap<K, V> newUnmodifiableLinkedMap() {
		return new ConcurrentUnmodifiableLinkedMap.Impl<>(new java.util.LinkedHashMap<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableLinkedMap.Impl} containing the given entries,
	 * preserving insertion order.
	 *
	 * @param pairs the entries to include
	 * @param <K>   the key type
	 * @param <V>   the value type
	 * @return a new unmodifiable concurrent linked map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentUnmodifiableLinkedMap<K, V> newUnmodifiableLinkedMap(@NotNull Map.Entry<K, V>... pairs) {
		java.util.LinkedHashMap<K, V> snapshot = new java.util.LinkedHashMap<>();
		for (Map.Entry<K, V> entry : pairs) {
			if (entry != null) snapshot.put(entry.getKey(), entry.getValue());
		}
		return new ConcurrentUnmodifiableLinkedMap.Impl<>(snapshot);
	}

	/**
	 * Creates an immutable snapshot of the given map as a linked map, preserving insertion
	 * order.
	 *
	 * @param map the source map
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a snapshot {@link ConcurrentUnmodifiableLinkedMap.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> @NotNull ConcurrentUnmodifiableLinkedMap<K, V> newUnmodifiableLinkedMap(@NotNull Map<? extends K, ? extends V> map) {
		if (map instanceof ConcurrentLinkedMap)
			return (ConcurrentUnmodifiableLinkedMap<K, V>) ((ConcurrentLinkedMap<K, V>) map).toUnmodifiable();

		return new ConcurrentUnmodifiableLinkedMap.Impl<>(new java.util.LinkedHashMap<>(map));
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableTreeSet.Impl} with natural element ordering.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent tree set
	 */
	public static <E> @NotNull ConcurrentUnmodifiableTreeSet<E> newUnmodifiableTreeSet() {
		return new ConcurrentUnmodifiableTreeSet.Impl<>(new java.util.TreeSet<>());
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableTreeSet.Impl} ordered by the given comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E>        the element type
	 * @return a new empty unmodifiable concurrent tree set
	 */
	public static <E> @NotNull ConcurrentUnmodifiableTreeSet<E> newUnmodifiableTreeSet(@NotNull Comparator<? super E> comparator) {
		return new ConcurrentUnmodifiableTreeSet.Impl<>(new java.util.TreeSet<>(comparator));
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableTreeSet.Impl} containing the given elements,
	 * with natural element ordering.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent tree set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableTreeSet<E> newUnmodifiableTreeSet(@NotNull E... array) {
		java.util.TreeSet<E> snapshot = new java.util.TreeSet<>();
		java.util.Collections.addAll(snapshot, array);
		return new ConcurrentUnmodifiableTreeSet.Impl<>(snapshot);
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableTreeSet.Impl} containing the given elements,
	 * ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param array      the elements to include
	 * @param <E>        the element type
	 * @return a new unmodifiable concurrent tree set containing the specified elements ordered by the given comparator
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableTreeSet<E> newUnmodifiableTreeSet(@NotNull Comparator<? super E> comparator, @NotNull E... array) {
		java.util.TreeSet<E> snapshot = new java.util.TreeSet<>(comparator);
		java.util.Collections.addAll(snapshot, array);
		return new ConcurrentUnmodifiableTreeSet.Impl<>(snapshot);
	}

	/**
	 * Creates an immutable snapshot of the given collection as a tree set with natural
	 * element ordering.
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return a snapshot {@link ConcurrentUnmodifiableTreeSet.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableTreeSet<E> newUnmodifiableTreeSet(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentTreeSet)
			return (ConcurrentUnmodifiableTreeSet<E>) ((ConcurrentTreeSet<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiableTreeSet.Impl<>(new java.util.TreeSet<>(collection));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a tree set ordered by the
	 * given comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return a snapshot {@link ConcurrentUnmodifiableTreeSet.Impl}
	 */
	public static <E> @NotNull ConcurrentUnmodifiableTreeSet<E> newUnmodifiableTreeSet(@NotNull Comparator<? super E> comparator, @NotNull Collection<? extends E> collection) {
		java.util.TreeSet<E> snapshot = new java.util.TreeSet<>(comparator);
		snapshot.addAll(collection);
		return new ConcurrentUnmodifiableTreeSet.Impl<>(snapshot);
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableTreeMap.Impl} with natural key ordering.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty unmodifiable concurrent tree map
	 */
	public static <K, V> @NotNull ConcurrentUnmodifiableTreeMap<K, V> newUnmodifiableTreeMap() {
		return new ConcurrentUnmodifiableTreeMap.Impl<>(new java.util.TreeMap<>());
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableTreeMap.Impl} ordered by the given comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @return a new empty unmodifiable concurrent tree map
	 */
	public static <K, V> @NotNull ConcurrentUnmodifiableTreeMap<K, V> newUnmodifiableTreeMap(@NotNull Comparator<? super K> comparator) {
		return new ConcurrentUnmodifiableTreeMap.Impl<>(new java.util.TreeMap<>(comparator));
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableTreeMap.Impl} containing the given entries, with
	 * natural key ordering.
	 *
	 * @param pairs the entries to include
	 * @param <K>   the key type
	 * @param <V>   the value type
	 * @return a new unmodifiable concurrent tree map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentUnmodifiableTreeMap<K, V> newUnmodifiableTreeMap(@NotNull Map.Entry<K, V>... pairs) {
		java.util.TreeMap<K, V> snapshot = new java.util.TreeMap<>();
		for (Map.Entry<K, V> entry : pairs) {
			if (entry != null) snapshot.put(entry.getKey(), entry.getValue());
		}
		return new ConcurrentUnmodifiableTreeMap.Impl<>(snapshot);
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableTreeMap.Impl} containing the given entries,
	 * ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param pairs      the entries to include
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @return a new unmodifiable concurrent tree map containing the specified entries ordered by the given comparator
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentUnmodifiableTreeMap<K, V> newUnmodifiableTreeMap(@NotNull Comparator<? super K> comparator, @NotNull Map.Entry<K, V>... pairs) {
		java.util.TreeMap<K, V> snapshot = new java.util.TreeMap<>(comparator);
		for (Map.Entry<K, V> entry : pairs) {
			if (entry != null) snapshot.put(entry.getKey(), entry.getValue());
		}
		return new ConcurrentUnmodifiableTreeMap.Impl<>(snapshot);
	}

	/**
	 * Creates an immutable snapshot of the given map as a tree map with natural key ordering.
	 *
	 * @param map the source map
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a snapshot {@link ConcurrentUnmodifiableTreeMap.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> @NotNull ConcurrentUnmodifiableTreeMap<K, V> newUnmodifiableTreeMap(@NotNull Map<? extends K, ? extends V> map) {
		if (map instanceof ConcurrentTreeMap)
			return (ConcurrentUnmodifiableTreeMap<K, V>) ((ConcurrentTreeMap<K, V>) map).toUnmodifiable();

		return new ConcurrentUnmodifiableTreeMap.Impl<>(new java.util.TreeMap<>(map));
	}

	/**
	 * Creates an immutable snapshot of the given map as a tree map ordered by the given
	 * comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param map        the source map
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @return a snapshot {@link ConcurrentUnmodifiableTreeMap.Impl}
	 */
	public static <K, V> @NotNull ConcurrentUnmodifiableTreeMap<K, V> newUnmodifiableTreeMap(@NotNull Comparator<? super K> comparator, @NotNull Map<? extends K, ? extends V> map) {
		java.util.TreeMap<K, V> snapshot = new java.util.TreeMap<>(comparator);
		snapshot.putAll(map);
		return new ConcurrentUnmodifiableTreeMap.Impl<>(snapshot);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentCollection.Impl}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentCollection.Impl}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentCollection<E>> toCollection() {
		return new StreamCollector<>(ConcurrentCollection.Impl::new, ConcurrentCollection.Impl::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentDeque.Impl}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentDeque.Impl}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentDeque<E>> toDeque() {
		return new StreamCollector<>(ConcurrentDeque.Impl::new, ConcurrentDeque.Impl::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentUnmodifiableDeque.Impl}.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent deque
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableDeque<E>> toUnmodifiableDeque() {
		return new StreamCollector<E, ConcurrentDeque.Impl<E>, ConcurrentUnmodifiableDeque<E>>(
			ConcurrentDeque.Impl::new,
			ConcurrentDeque.Impl::addAll,
			(left, right) -> { left.addAll(right); return left; },
			ConcurrentUnmodifiableDeque.Impl::new,
			ORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentQueue.Impl}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentQueue.Impl}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentQueue<E>> toQueue() {
		return new StreamCollector<>(ConcurrentQueue.Impl::new, ConcurrentQueue.Impl::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentUnmodifiableQueue.Impl}.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent queue
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableQueue<E>> toUnmodifiableQueue() {
		return new StreamCollector<E, ConcurrentQueue.Impl<E>, ConcurrentUnmodifiableQueue<E>>(
			ConcurrentQueue.Impl::new,
			ConcurrentQueue.Impl::addAll,
			(left, right) -> { left.addAll(right); return left; },
			ConcurrentUnmodifiableQueue.Impl::new,
			ORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentLinkedList.Impl}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentLinkedList.Impl}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentLinkedList<E>> toLinkedList() {
		return new StreamCollector<>(ConcurrentLinkedList.Impl::new, ConcurrentLinkedList.Impl::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new
	 * {@link ConcurrentUnmodifiableList.Impl} backed by a {@link ConcurrentLinkedList.Impl}, so
	 * iteration of the result preserves insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent linked list
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableList<E>> toUnmodifiableLinkedList() {
		return new StreamCollector<E, ConcurrentLinkedList.Impl<E>, ConcurrentUnmodifiableList<E>>(
			ConcurrentLinkedList.Impl::new,
			ConcurrentLinkedList.Impl::addAll,
			(left, right) -> { left.addAll(right); return left; },
			ConcurrentUnmodifiableList.Impl::new,
			ORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentList.Impl}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentList.Impl}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentList<E>> toList() {
		return new StreamCollector<>(ConcurrentList.Impl::new, ConcurrentList.Impl::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentUnmodifiableList.Impl}.
	 *
	 * @param <E> the element type
	 * @param <A> the result type (extends {@link ConcurrentList.Impl})
	 * @return a collector producing an unmodifiable concurrent list
	 */
	@SuppressWarnings("unchecked")
	public static <E, A extends ConcurrentList<E>> @NotNull Collector<E, ?, A> toUnmodifiableList() {
		return new StreamCollector<>(
			ConcurrentList.Impl::new,
			ConcurrentList.Impl::addAll,
			(left, right) -> { left.addAll(right); return left; },
			list -> (A) list.toUnmodifiable(),
			ORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentMap.Impl}
	 * by casting each element to {@link Map.Entry}. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentMap.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toWeakMap() {
		return toMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentMap.Impl}.
	 * Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentMap.Impl}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap() {
		return toMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentMap.Impl},
	 * using the specified merge function for duplicate keys.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentMap.Impl}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentMap.Impl}
	 * using the given key and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toMap(keyMapper, valueMapper, throwingMerger(), ConcurrentMap.Impl::new);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentMap.Impl}
	 * using the given key mapper, value mapper, and merge function for duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toMap(keyMapper, valueMapper, mergeFunction, ConcurrentMap.Impl::new);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a map supplied by {@code mapSupplier},
	 * using the given key mapper, value mapper, and merge function. This is the most general {@code toMap} overload.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param mapSupplier the supplier providing a new empty map instance
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @param <A>           the map type (extends {@link ConcurrentMap.Impl})
	 * @return a collector producing a map of the supplied type
	 */
	public static <K, V, T, A extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, A> toMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<A> mapSupplier
	) {
		return new StreamCollector<>(
			mapSupplier,
			(map, element) -> map.merge(
				keyMapper.apply(element),
				valueMapper.apply(element),
				mergeFunction
			),
			(m1, m2) -> {
				m2.forEach((key, value) -> m1.merge(key, value, mergeFunction));
				return m1;
			},
			UNORDERED_CHARACTERISTICS
		);
	}

	/**
	 * Collect a stream into a single item, only if there is 1 item expected.
	 *
	 * @return Only object from the stream.
	 * @throws NoSuchElementException If the stream size is equal to 0.
	 * @throws IllegalStateException If the stream size is greater than 1.
	 */
	public static <T> @NotNull Collector<T, ?, T> toSingleton() {
		return Collector.of(
			SingletonBox::new,
			(box, element) -> {
				if (box.has) {
					box.overflow = true;
				} else {
					box.value = element;
					box.has = true;
				}
			},
			(left, right) -> {
				if (left.overflow || right.overflow || (left.has && right.has)) {
					left.overflow = true;
					return left;
				}

				return right.has ? right : left;
			},
			box -> {
				if (!box.has)
					throw new NoSuchElementException();

				if (box.overflow)
					throw new IllegalStateException();

				@SuppressWarnings("unchecked")
				T result = (T) box.value;
				return result;
			}
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentLinkedMap.Impl}
	 * by casting each element to {@link Map.Entry}. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedMap.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toWeakLinkedMap() {
		return toLinkedMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentLinkedMap.Impl}.
	 * Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentLinkedMap.Impl}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap() {
		return toLinkedMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentLinkedMap.Impl},
	 * using the specified merge function for duplicate keys.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentLinkedMap.Impl}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toLinkedMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentLinkedMap.Impl}
	 * using the given key and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toLinkedMap(keyMapper, valueMapper, throwingMerger(), ConcurrentLinkedMap.Impl::new);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentLinkedMap.Impl}
	 * using the given key mapper, value mapper, and merge function for duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toLinkedMap(keyMapper, valueMapper, mergeFunction, ConcurrentLinkedMap.Impl::new);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a linked map supplied by {@code mapSupplier},
	 * using the given key mapper, value mapper, and merge function. This is the most general {@code toLinkedMap} overload.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param mapSupplier the supplier providing a new empty linked map instance
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @param <A>           the map type (extends {@link ConcurrentLinkedMap.Impl})
	 * @return a collector producing a linked map of the supplied type
	 */
	public static <K, V, T, A extends ConcurrentLinkedMap<K, V>> @NotNull Collector<T, ?, A> toLinkedMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<A> mapSupplier
	) {
		BiConsumer<A, T> accumulator = (map, element) -> map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction);
		BinaryOperator<A> combiner = (m1, m2) -> { m2.forEach((key, value) -> m1.merge(key, value, mergeFunction)); return m1; };

		return new StreamCollector<>(
			mapSupplier,
			accumulator,
			combiner,
			ORDERED_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentUnmodifiableMap.Impl}
	 * by casting each element to {@link Map.Entry}. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap.Impl}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toWeakUnmodifiableMap() {
		return toUnmodifiableMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentUnmodifiableMap.Impl}.
	 * Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap.Impl}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap() {
		return toUnmodifiableMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentUnmodifiableMap.Impl},
	 * using the specified merge function for duplicate keys.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap.Impl}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentUnmodifiableMap.Impl}
	 * using the given key and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toUnmodifiableMap(keyMapper, valueMapper, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentUnmodifiableMap.Impl}
	 * using the given key mapper, value mapper, and merge function for duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return new StreamCollector<T, HashMap<K, V>, ConcurrentUnmodifiableMap<K, V>>(
			HashMap::new,
			(map, element) -> map.merge(
				keyMapper.apply(element),
				valueMapper.apply(element),
				mergeFunction
			),
			(m1, m2) -> {
				m2.forEach((key, value) -> m1.merge(key, value, mergeFunction));
				return m1;
			},
			Concurrent::newUnmodifiableMap,
			UNORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentUnmodifiableMap.Impl},
	 * using the given key mapper, value mapper, merge function, and final map supplier.
	 * This is the most general {@code toUnmodifiableMap} overload.
	 * <p>
	 * Accumulation uses a plain {@link HashMap} to avoid repeated write-lock traffic on
	 * lock-backed target maps. The supplied map is populated once during finishing and wrapped
	 * as the unmodifiable result, so its key-equality and iteration order apply to the final
	 * view rather than to the accumulation phase.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param mapSupplier the supplier providing the empty map that will back the unmodifiable result
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @param <A>           the final map type (extends {@link ConcurrentMap.Impl})
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap.Impl}
	 */
	public static <K, V, T, A extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<A> mapSupplier
	) {
		return new StreamCollector<T, HashMap<K, V>, ConcurrentUnmodifiableMap<K, V>>(
			HashMap::new,
			(map, element) -> map.merge(
				keyMapper.apply(element),
				valueMapper.apply(element),
				mergeFunction
			),
			(m1, m2) -> {
				m2.forEach((key, value) -> m1.merge(key, value, mergeFunction));
				return m1;
			},
			accumulated -> {
				A target = mapSupplier.get();
				target.putAll(accumulated);
				return Concurrent.newUnmodifiableMap(target);
			},
			UNORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@link ConcurrentUnmodifiableTreeMap.Impl} ordered by the given comparator. Throws on
	 * duplicate keys.
	 *
	 * @param comparator the comparator used to order and compare the keys
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentUnmodifiableTreeMap.Impl} ordered by {@code comparator}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableTreeMap<K, V>> toUnmodifiableTreeMap(@NotNull Comparator<? super K> comparator) {
		return toUnmodifiableTreeMap(
			Map.Entry::getKey,
			Map.Entry::getValue,
			throwingMerger(),
			() -> Concurrent.newTreeMap(comparator)
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@link ConcurrentUnmodifiableTreeMap.Impl} backed by the supplied tree map source
	 * (typically a {@link ConcurrentTreeMap.Impl}), applying the given key mapper, value
	 * mapper, and merge function.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param mapSupplier the supplier providing a new empty tree map used during accumulation
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @param <A>           the intermediate map type (extends {@link ConcurrentMap.Impl})
	 * @return a collector producing a {@link ConcurrentUnmodifiableTreeMap.Impl}
	 */
	public static <K, V, T, A extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableTreeMap<K, V>> toUnmodifiableTreeMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<A> mapSupplier
	) {
		return new StreamCollector<>(
			mapSupplier,
			(map, element) -> map.merge(
				keyMapper.apply(element),
				valueMapper.apply(element),
				mergeFunction
			),
			(m1, m2) -> {
				m2.forEach((key, value) -> m1.merge(key, value, mergeFunction));
				return m1;
			},
			Concurrent::newUnmodifiableTreeMap,
			UNORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentSet.Impl}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentSet.Impl}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toSet() {
		return new StreamCollector<>(ConcurrentSet.Impl::new, ConcurrentSet.Impl::addAll, (left, right) -> { left.addAll(right); return left; }, UNORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentUnmodifiableSet.Impl}.
	 *
	 * @param <E> the element type
	 * @param <A> the result type (extends {@link ConcurrentSet.Impl})
	 * @return a collector producing an unmodifiable concurrent set
	 */
	@SuppressWarnings("unchecked")
	public static <E, A extends ConcurrentSet<E>> @NotNull Collector<E, ?, A> toUnmodifiableSet() {
		return new StreamCollector<>(
			ConcurrentSet.Impl::new,
			ConcurrentSet.Impl::addAll,
			(left, right) -> { left.addAll(right); return left; },
			list -> (A) list.toUnmodifiable(),
			UNORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentLinkedSet.Impl},
	 * preserving insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentLinkedSet.Impl}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentLinkedSet<E>> toLinkedSet() {
		return new StreamCollector<>(ConcurrentLinkedSet.Impl::new, ConcurrentLinkedSet.Impl::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new
	 * {@link ConcurrentUnmodifiableLinkedSet.Impl}, preserving insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent linked set
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableLinkedSet<E>> toUnmodifiableLinkedSet() {
		return new StreamCollector<E, java.util.LinkedHashSet<E>, ConcurrentUnmodifiableLinkedSet<E>>(
			java.util.LinkedHashSet::new,
			java.util.LinkedHashSet::add,
			(left, right) -> { left.addAll(right); return left; },
			ConcurrentUnmodifiableLinkedSet.Impl::new,
			ORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentTreeSet.Impl}
	 * with natural element ordering.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentTreeSet.Impl}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentTreeSet<E>> toTreeSet() {
		return new StreamCollector<>(ConcurrentTreeSet.Impl::new, ConcurrentTreeSet.Impl::addAll, (left, right) -> { left.addAll(right); return left; }, UNORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentTreeSet.Impl}
	 * ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E>        the element type
	 * @return a collector producing a {@link ConcurrentTreeSet.Impl} ordered by {@code comparator}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentTreeSet<E>> toTreeSet(@NotNull Comparator<? super E> comparator) {
		return new StreamCollector<>(() -> new ConcurrentTreeSet.Impl<>(comparator), ConcurrentTreeSet.Impl::addAll, (left, right) -> { left.addAll(right); return left; }, UNORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new
	 * {@link ConcurrentUnmodifiableTreeSet.Impl} with natural element ordering.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent tree set
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableTreeSet<E>> toUnmodifiableTreeSet() {
		return new StreamCollector<E, java.util.TreeSet<E>, ConcurrentUnmodifiableTreeSet<E>>(
			java.util.TreeSet::new,
			java.util.TreeSet::add,
			(left, right) -> { left.addAll(right); return left; },
			ConcurrentUnmodifiableTreeSet.Impl::new,
			UNORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new
	 * {@link ConcurrentUnmodifiableTreeSet.Impl} ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E>        the element type
	 * @return a collector producing an unmodifiable concurrent tree set ordered by {@code comparator}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableTreeSet<E>> toUnmodifiableTreeSet(@NotNull Comparator<? super E> comparator) {
		return new StreamCollector<E, java.util.TreeSet<E>, ConcurrentUnmodifiableTreeSet<E>>(
			() -> new java.util.TreeSet<>(comparator),
			java.util.TreeSet::add,
			(left, right) -> { left.addAll(right); return left; },
			ConcurrentUnmodifiableTreeSet.Impl::new,
			UNORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@link ConcurrentTreeMap.Impl} ordered by the specified comparator. Throws on duplicate keys.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @param <T>        the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentTreeMap.Impl} ordered by {@code comparator}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMap(@NotNull Comparator<? super K> comparator) {
		return toTreeMap(comparator, Map.Entry::getKey, Map.Entry::getValue, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentTreeMap.Impl}
	 * with natural key ordering, using the given key and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper   the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentTreeMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toTreeMapInternal(ConcurrentTreeMap.Impl::new, keyMapper, valueMapper, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentTreeMap.Impl}
	 * with natural key ordering, using the given key mapper, value mapper, and merge function for
	 * duplicate keys.
	 *
	 * @param keyMapper     the function to extract map keys from stream elements
	 * @param valueMapper   the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentTreeMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toTreeMapInternal(ConcurrentTreeMap.Impl::new, keyMapper, valueMapper, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentTreeMap.Impl}
	 * ordered by the specified comparator, using the given key mapper, value mapper, and merge function
	 * for duplicate keys.
	 *
	 * @param comparator    the comparator used to order the keys
	 * @param keyMapper     the function to extract map keys from stream elements
	 * @param valueMapper   the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentTreeMap.Impl} ordered by {@code comparator}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMap(@NotNull Comparator<? super K> comparator, @NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toTreeMapInternal(() -> new ConcurrentTreeMap.Impl<>(comparator), keyMapper, valueMapper, mergeFunction);
	}

	/**
	 * Builds a {@link Collector} that merges stream elements into the supplier-provided
	 * {@link ConcurrentTreeMap.Impl} using the given key/value mappers and merge function.
	 *
	 * @param mapSupplier   the supplier providing the empty backing tree map
	 * @param keyMapper     the function to extract map keys from stream elements
	 * @param valueMapper   the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentTreeMap.Impl}
	 */
	private static <K, V, T> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMapInternal(
		@NotNull Supplier<ConcurrentTreeMap<K, V>> mapSupplier,
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction
	) {
		return new StreamCollector<>(
			mapSupplier,
			(map, element) -> map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction),
			(m1, m2) -> { m2.forEach((key, value) -> m1.merge(key, value, mergeFunction)); return m1; },
			ORDERED_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@link ConcurrentUnmodifiableLinkedMap.Impl}, preserving insertion order. Throws on
	 * duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentUnmodifiableLinkedMap.Impl}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableLinkedMap<K, V>> toUnmodifiableLinkedMap() {
		return toUnmodifiableLinkedMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@link ConcurrentUnmodifiableLinkedMap.Impl}, preserving insertion order, using the
	 * specified merge function for duplicate keys.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentUnmodifiableLinkedMap.Impl}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableLinkedMap<K, V>> toUnmodifiableLinkedMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toUnmodifiableLinkedMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@link ConcurrentUnmodifiableLinkedMap.Impl} using the given key and value mappers,
	 * preserving insertion order. Throws on duplicate keys.
	 *
	 * @param keyMapper   the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableLinkedMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableLinkedMap<K, V>> toUnmodifiableLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toUnmodifiableLinkedMap(keyMapper, valueMapper, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@link ConcurrentUnmodifiableLinkedMap.Impl} using the given key mapper, value mapper, and
	 * merge function for duplicate keys, preserving insertion order.
	 *
	 * @param keyMapper     the function to extract map keys from stream elements
	 * @param valueMapper   the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableLinkedMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableLinkedMap<K, V>> toUnmodifiableLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return new StreamCollector<T, java.util.LinkedHashMap<K, V>, ConcurrentUnmodifiableLinkedMap<K, V>>(
			java.util.LinkedHashMap::new,
			(map, element) -> map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction),
			(m1, m2) -> { m2.forEach((key, value) -> m1.merge(key, value, mergeFunction)); return m1; },
			ConcurrentUnmodifiableLinkedMap.Impl::new,
			ORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@link ConcurrentUnmodifiableTreeMap.Impl} with natural key ordering. Throws on
	 * duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentUnmodifiableTreeMap.Impl}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableTreeMap<K, V>> toUnmodifiableTreeMap() {
		return toUnmodifiableTreeMap(Map.Entry::getKey, Map.Entry::getValue, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@link ConcurrentUnmodifiableTreeMap.Impl} with natural key ordering, using the given key
	 * and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper   the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableTreeMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableTreeMap<K, V>> toUnmodifiableTreeMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toUnmodifiableTreeMap(keyMapper, valueMapper, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@link ConcurrentUnmodifiableTreeMap.Impl} with natural key ordering, using the given key
	 * mapper, value mapper, and merge function for duplicate keys.
	 *
	 * @param keyMapper     the function to extract map keys from stream elements
	 * @param valueMapper   the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableTreeMap.Impl}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableTreeMap<K, V>> toUnmodifiableTreeMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return new StreamCollector<T, java.util.TreeMap<K, V>, ConcurrentUnmodifiableTreeMap<K, V>>(
			java.util.TreeMap::new,
			(map, element) -> map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction),
			(m1, m2) -> { m2.forEach((key, value) -> m1.merge(key, value, mergeFunction)); return m1; },
			ConcurrentUnmodifiableTreeMap.Impl::new,
			UNORDERED_FINISHING_CHARACTERISTICS
		);
	}

	/**
	 * A private {@link Collector} implementation used by the {@link Concurrent} factory methods.
	 * Supports an optional finisher function for transforming the accumulated result (e.g., wrapping
	 * in an unmodifiable view).
	 *
	 * @param <T> the type of input elements to the collector
	 * @param <A> the mutable accumulation type
	 * @param <R> the result type of the collector
	 */
	@Getter
	@Accessors(fluent = true)
	@RequiredArgsConstructor
	@AllArgsConstructor
	private static class StreamCollector<T, A, R> implements Collector<T, A, R> {

		private static final @NotNull Function<Object, Object> CASTING_IDENTITY = i -> i;

		/**
		 * Returns a casting identity function, used as the default finisher when no transformation is needed.
		 *
		 * @param <I> the input type
		 * @param <R> the result type
		 * @return an identity function that casts input to the result type
		 */
		@SuppressWarnings("unchecked")
		private static <I, R> Function<I, R> castingIdentity() {
			return (Function<I, R>) CASTING_IDENTITY;
		}

		private final @NotNull Supplier<A> supplier;
		private final @NotNull BiConsumer<A, T> accumulator;
		private final @NotNull BinaryOperator<A> combiner;
		private @NotNull Function<A, R> finisher = castingIdentity();
		private final @NotNull Set<Characteristics> characteristics;

	}

	/**
	 * Mutable accumulator for {@link #toSingleton()} tracking the single element, whether one
	 * has been seen, and whether a second arrival triggered overflow.
	 */
	private static final class SingletonBox {
		@Nullable Object value;
		boolean has;
		boolean overflow;
	}

}
