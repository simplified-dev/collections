package dev.simplified.collection;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility hub of static factory methods for the concurrent collection, queue, set, list, and map
 * implementations along with their linked and unmodifiable counterparts.
 * <p>
 * Also exposes concurrent {@link Collector} instances for use with {@link Stream#collect}.
 */
@UtilityClass
public final class Concurrent {

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
	 * Creates a new empty {@link ConcurrentArrayList}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent collection
	 */
	public static <E> @NotNull ConcurrentCollection<E> newCollection() {
		return new ConcurrentArrayList<>();
	}

	/**
	 * Creates a new {@link ConcurrentArrayList} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new concurrent collection containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentCollection<E> newCollection(@NotNull E... array) {
		return new ConcurrentArrayList<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentCollection} by copying all elements from {@code collection}.
	 * <p>
	 * The returned collection owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned collection and vice versa.
	 *
	 * @param collection the source collection to copy from
	 * @param <E> the element type
	 * @return a new concurrent collection containing the source elements
	 */
	public static <E> @NotNull ConcurrentCollection<E> newCollection(@NotNull Collection<? extends E> collection) {
		return new ConcurrentArrayList<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentArrayDeque}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent deque
	 */
	public static <E> @NotNull ConcurrentDeque<E> newDeque() {
		return new ConcurrentArrayDeque<>();
	}

	/**
	 * Creates a new {@link ConcurrentArrayDeque} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new concurrent deque containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentDeque<E> newDeque(@NotNull E... array) {
		return new ConcurrentArrayDeque<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentDeque} by copying all elements from {@code collection}.
	 * <p>
	 * The returned deque owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned deque and vice versa.
	 *
	 * @param collection the source collection to copy from
	 * @param <E> the element type
	 * @return a new concurrent deque containing the source elements
	 */
	public static <E> @NotNull ConcurrentDeque<E> newDeque(@NotNull Collection<? extends E> collection) {
		return new ConcurrentArrayDeque<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentArrayList}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent list
	 */
	public static <E> @NotNull ConcurrentList<E> newList() {
		return new ConcurrentArrayList<>();
	}

	/**
	 * Creates a new {@link ConcurrentArrayList} containing the given elements.
	 *
	 * @param initialCapacity the initial capacity of the underlying list
	 * @param <E> the element type
	 * @return a new concurrent list containing the specified elements
	 */
	public static <E> @NotNull ConcurrentList<E> newList(int initialCapacity) {
		return new ConcurrentArrayList<>(initialCapacity);
	}

	/**
	 * Creates a new {@link ConcurrentArrayList} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new concurrent list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentList<E> newList(@NotNull E... array) {
		return new ConcurrentArrayList<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentList} by copying all elements from {@code collection}.
	 * <p>
	 * The returned list owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned list and vice versa. A {@code null}
	 * collection produces an empty list.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty list
	 * @param <E> the element type
	 * @return a new concurrent list containing the source elements
	 */
	public static <E> @NotNull ConcurrentList<E> newList(@Nullable Collection<? extends E> collection) {
		return new ConcurrentArrayList<>(collection);
	}

	/**
	 * Creates a new {@link ConcurrentHashMap} containing the given map entries.
	 *
	 * @param entries the entries to include
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentMap<K, V> newMap(@NotNull Map.Entry<K, V>... entries) {
		return new ConcurrentHashMap<>(entries);
	}

	/**
	 * Creates a new {@link ConcurrentMap} by copying all entries from {@code map}.
	 * <p>
	 * The returned map owns its own backing storage; subsequent modifications to {@code map} do
	 * not affect the returned map and vice versa.
	 *
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent map containing the source entries
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentHashMap<>(map);
	}

	/**
	 * Creates a new empty {@link ConcurrentArrayQueue}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent queue
	 */
	public static <E> @NotNull ConcurrentQueue<E> newQueue() {
		return new ConcurrentArrayQueue<>();
	}

	/**
	 * Creates a new {@link ConcurrentArrayQueue} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new concurrent queue containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentQueue<E> newQueue(@NotNull E... array) {
		return new ConcurrentArrayQueue<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentQueue} by copying all elements from {@code collection}.
	 * <p>
	 * The returned queue owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned queue and vice versa.
	 *
	 * @param collection the source collection to copy from
	 * @param <E> the element type
	 * @return a new concurrent queue containing the source elements
	 */
	public static <E> @NotNull ConcurrentQueue<E> newQueue(@NotNull Collection<? extends E> collection) {
		return new ConcurrentArrayQueue<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentHashSet}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent set
	 */
	public static <E> @NotNull ConcurrentSet<E> newSet() {
		return new ConcurrentHashSet<>();
	}

	/**
	 * Creates a new {@link ConcurrentHashSet} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new concurrent set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentSet<E> newSet(@NotNull E... array) {
		return new ConcurrentHashSet<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentSet} by copying all elements from {@code collection}.
	 * <p>
	 * The returned set owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned set and vice versa. A {@code null} collection
	 * produces an empty set.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty set
	 * @param <E> the element type
	 * @return a new concurrent set containing the source elements
	 */
	public static <E> @NotNull ConcurrentSet<E> newSet(@Nullable Collection<? extends E> collection) {
		return new ConcurrentHashSet<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedList}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent linked list
	 */
	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList() {
		return new ConcurrentLinkedList<>();
	}

	/**
	 * Creates a new {@link ConcurrentLinkedList} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new concurrent linked list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList(@NotNull E... array) {
		return new ConcurrentLinkedList<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedList} by copying all elements from {@code collection}.
	 * <p>
	 * The returned linked list owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned list and vice versa.
	 *
	 * @param collection the source collection to copy from
	 * @param <E> the element type
	 * @return a new concurrent linked list containing the source elements
	 */
	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList(@NotNull Collection<? extends E> collection) {
		return new ConcurrentLinkedList<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedHashMap} with no maximum size constraint.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty concurrent linked map
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newLinkedMap() {
		return newLinkedMap(-1);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedHashMap} with the specified maximum size.
	 * A value of {@code -1} indicates no size limit.
	 *
	 * @param maxSize the maximum number of entries, or {@code -1} for unlimited
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty concurrent linked map with the given size constraint
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newLinkedMap(int maxSize) {
		return new ConcurrentLinkedHashMap<>(maxSize);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedHashMap} by copying all entries from {@code map}.
	 * <p>
	 * The returned linked map owns its own backing storage; subsequent modifications to
	 * {@code map} do not affect the returned map and vice versa.
	 *
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent linked map containing the source entries
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newLinkedMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentLinkedHashMap<>(map);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedHashMap} with the given maximum size by copying all
	 * entries from {@code map}.
	 * <p>
	 * The returned linked map owns its own backing storage; subsequent modifications to
	 * {@code map} do not affect the returned map and vice versa.
	 *
	 * @param map the source map to copy from
	 * @param maxSize the maximum number of entries, or {@code -1} for unlimited
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent linked map containing the source entries with the given size constraint
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newLinkedMap(@NotNull Map<? extends K, ? extends V> map, int maxSize) {
		return new ConcurrentLinkedHashMap<>(map, maxSize);
	}

	/**
	 * Creates a new empty {@link ConcurrentTreeMap} with natural key ordering.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty concurrent tree map
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap() {
		return new ConcurrentTreeMap<>();
	}

	/**
	 * Creates a new empty {@link ConcurrentTreeMap} with the specified comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty concurrent tree map ordered by the given comparator
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Comparator<? super K> comparator) {
		return new ConcurrentTreeMap<>(comparator);
	}

	/**
	 * Creates a new {@link ConcurrentTreeMap} containing the given entries, with natural
	 * key ordering.
	 *
	 * @param pairs the entries to include
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent tree map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Map.Entry<K, V>... pairs) {
		return new ConcurrentTreeMap<>(pairs);
	}

	/**
	 * Creates a new {@link ConcurrentTreeMap} containing the given entries, ordered by the
	 * specified comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param pairs the entries to include
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent tree map containing the specified entries ordered by the given comparator
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Comparator<? super K> comparator, @NotNull Map.Entry<K, V>... pairs) {
		return new ConcurrentTreeMap<>(comparator, pairs);
	}

	/**
	 * Creates a new {@link ConcurrentTreeMap} with natural key ordering by copying all entries
	 * from {@code map}.
	 * <p>
	 * The returned tree map owns its own backing storage; subsequent modifications to {@code map}
	 * do not affect the returned map and vice versa.
	 *
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent tree map containing the source entries
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentTreeMap<>(map);
	}

	/**
	 * Creates a new {@link ConcurrentTreeMap} ordered by {@code comparator} by copying all
	 * entries from {@code map}.
	 * <p>
	 * The returned tree map owns its own backing storage; subsequent modifications to {@code map}
	 * do not affect the returned map and vice versa.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent tree map containing the source entries ordered by the given comparator
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> newTreeMap(@NotNull Comparator<? super K> comparator, @NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentTreeMap<>(comparator, map);
	}

	/**
	 * Creates a new empty {@link ConcurrentTreeSet} with natural element ordering.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent tree set
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet() {
		return new ConcurrentTreeSet<>();
	}

	/**
	 * Creates a new empty {@link ConcurrentTreeSet} with the specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E> the element type
	 * @return a new empty concurrent tree set ordered by the given comparator
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator) {
		return new ConcurrentTreeSet<>(comparator);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet} containing the given elements, with natural
	 * element ordering.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new concurrent tree set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull E... array) {
		return new ConcurrentTreeSet<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet} containing the given elements, ordered by the
	 * specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new concurrent tree set containing the specified elements ordered by the given comparator
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator, @NotNull E... array) {
		return new ConcurrentTreeSet<>(comparator, array);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet} with natural element ordering by copying all
	 * elements from {@code collection}.
	 * <p>
	 * The returned tree set owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned set and vice versa.
	 *
	 * @param collection the source collection to copy from
	 * @param <E> the element type
	 * @return a new concurrent tree set containing the source elements
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentTreeSet<>(collection);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet} ordered by {@code comparator} by copying all
	 * elements from {@code collection}.
	 * <p>
	 * The returned tree set owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned set and vice versa.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param collection the source collection to copy from
	 * @param <E> the element type
	 * @return a new concurrent tree set containing the source elements ordered by the given comparator
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator, @NotNull Collection<? extends E> collection) {
		return new ConcurrentTreeSet<>(comparator, collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedHashSet}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent linked set
	 */
	public static <E> @NotNull ConcurrentSet<E> newLinkedSet() {
		return new ConcurrentLinkedHashSet<>();
	}

	/**
	 * Creates a new {@link ConcurrentLinkedHashSet} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new concurrent linked set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentSet<E> newLinkedSet(@NotNull E... array) {
		return new ConcurrentLinkedHashSet<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedHashSet} by copying all elements from {@code collection}.
	 * <p>
	 * The returned linked set owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned set and vice versa.
	 *
	 * @param collection the source collection to copy from
	 * @param <E> the element type
	 * @return a new concurrent linked set containing the source elements
	 */
	public static <E> @NotNull ConcurrentSet<E> newLinkedSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentLinkedHashSet<>(collection);
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList} backed by a fresh
	 * {@link ConcurrentArrayList}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent collection
	 */
	public static <E> @NotNull ConcurrentCollection<E> newUnmodifiableCollection() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList<>(new ArrayList<>());
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new unmodifiable concurrent collection containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentCollection<E> newUnmodifiableCollection(@NotNull E... array) {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList<>(new ArrayList<>(Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected. If the source is itself a {@link ConcurrentArrayList},
	 * its {@code toUnmodifiable()} is delegated to so its read lock guards the copy.</p>
	 *
	 * @param collection the source collection
	 * @param <E> the element type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentCollection<E> newUnmodifiableCollection(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentCollection)
			return ((ConcurrentCollection<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList<>(new ArrayList<>(collection));
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList} backed by a fresh
	 * {@link ConcurrentArrayList}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent list
	 */
	public static <E> @NotNull ConcurrentList<E> newUnmodifiableList() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList<>(new ArrayList<>());
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new unmodifiable concurrent list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentList<E> newUnmodifiableList(@NotNull E... array) {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList<>(new ArrayList<>(Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a list.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected. If the source is a {@link ConcurrentArrayList} but
	 * not a {@link ConcurrentLinkedList}, its {@code toUnmodifiable()} is delegated to so
	 * the source's read lock guards the copy. {@link ConcurrentLinkedList} sources are
	 * routed through the {@link ArrayList ArrayList}-backed fallback to honor
	 * this factory's {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList} return contract.</p>
	 *
	 * @param collection the source collection
	 * @param <E> the element type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentList<E> newUnmodifiableList(@Nullable Collection<? extends E> collection) {
		if (collection == null) return newUnmodifiableList();
		// ConcurrentLinkedList.toUnmodifiable() yields a LinkedList-backed wrapper; route
		// those sources to the ArrayList-backed fallback so this factory keeps returning an
		// UnmodifiableConcurrentArrayList as documented.
		if (collection instanceof ConcurrentList && !(collection instanceof ConcurrentLinkedList))
			return ((ConcurrentList<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList<>(new ArrayList<>(collection));
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap} containing the given map entries.
	 *
	 * @param entries the entries to include
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new unmodifiable concurrent map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableMap(@NotNull Map.Entry<K, V>... entries) {
		HashMap<K, V> snapshot = new HashMap<>();
		for (Map.Entry<K, V> entry : entries) {
			if (entry != null) snapshot.put(entry.getKey(), entry.getValue());
		}
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap<>(snapshot);
	}

	/**
	 * Creates an immutable snapshot of the given map.
	 *
	 * <p>The snapshot copies the input's entries at construction time; subsequent mutations
	 * on the source are not reflected. If the source is a {@link ConcurrentHashMap} but
	 * neither a {@link ConcurrentTreeMap} nor a {@link ConcurrentLinkedHashMap}, its
	 * {@code toUnmodifiable()} is delegated to so the source's read lock guards the copy.
	 * {@link ConcurrentTreeMap} and {@link ConcurrentLinkedHashMap} sources are routed through
	 * the {@link HashMap HashMap}-backed fallback to honor this factory's
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap} return contract.</p>
	 *
	 * @param map the source map
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableMap(@NotNull Map<? extends K, ? extends V> map) {
		// ConcurrentTreeMap and ConcurrentLinkedHashMap override toUnmodifiable() to
		// yield TreeMap/LinkedHashMap-backed siblings; route those sources to the HashMap-backed
		// fallback so this factory keeps returning a ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap as documented.
		if (map instanceof ConcurrentMap && !(map instanceof ConcurrentTreeMap) && !(map instanceof ConcurrentLinkedHashMap))
			return ((ConcurrentMap<K, V>) map).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap<>(new HashMap<>(map));
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet} backed by a fresh
	 * {@link ConcurrentHashSet}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent set
	 */
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableSet() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet<>(new HashSet<>());
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new unmodifiable concurrent set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableSet(@NotNull E... array) {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet<>(new HashSet<>(Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a set.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected. If the source is a {@link ConcurrentHashSet} but
	 * neither a {@link ConcurrentTreeSet} nor a {@link ConcurrentLinkedHashSet}, its
	 * {@code toUnmodifiable()} is delegated to so the source's read lock guards the copy.
	 * {@link ConcurrentTreeSet} and {@link ConcurrentLinkedHashSet} sources are routed through
	 * the {@link HashSet HashSet}-backed fallback to honor this factory's
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet} return contract.</p>
	 *
	 * @param collection the source collection
	 * @param <E> the element type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableSet(@NotNull Collection<? extends E> collection) {
		// ConcurrentTreeSet and ConcurrentLinkedHashSet override toUnmodifiable() to
		// yield TreeSet/LinkedHashSet-backed siblings; route those sources to the HashSet-backed
		// fallback so this factory keeps returning a ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet as documented.
		if (collection instanceof ConcurrentSet && !(collection instanceof ConcurrentTreeSet) && !(collection instanceof ConcurrentLinkedHashSet))
			return ((ConcurrentSet<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet<>(new HashSet<>(collection));
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayQueue}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent queue
	 */
	public static <E> @NotNull ConcurrentQueue<E> newUnmodifiableQueue() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayQueue<>(new ArrayDeque<>());
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayQueue} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new unmodifiable concurrent queue containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentQueue<E> newUnmodifiableQueue(@NotNull E... array) {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayQueue<>(new ArrayDeque<>(Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given queue.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected.</p>
	 *
	 * @param collection the source collection
	 * @param <E> the element type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayQueue}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentQueue<E> newUnmodifiableQueue(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentQueue)
			return ((ConcurrentQueue<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayQueue<>(new ArrayDeque<>(collection));
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayDeque}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent deque
	 */
	public static <E> @NotNull ConcurrentDeque<E> newUnmodifiableDeque() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayDeque<>(new ArrayDeque<>());
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayDeque} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new unmodifiable concurrent deque containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentDeque<E> newUnmodifiableDeque(@NotNull E... array) {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayDeque<>(new ArrayDeque<>(Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given deque.
	 *
	 * <p>The snapshot copies the input's contents at construction time; subsequent mutations
	 * on the source are not reflected.</p>
	 *
	 * @param collection the source collection
	 * @param <E> the element type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayDeque}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentDeque<E> newUnmodifiableDeque(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentDeque)
			return ((ConcurrentDeque<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayDeque<>(new ArrayDeque<>(collection));
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent linked list
	 */
	public static <E> @NotNull ConcurrentList<E> newUnmodifiableLinkedList() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList<>(new LinkedList<>());
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new unmodifiable concurrent linked list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentList<E> newUnmodifiableLinkedList(@NotNull E... array) {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList<>(new LinkedList<>(Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a linked list, preserving
	 * insertion order.
	 *
	 * @param collection the source collection
	 * @param <E> the element type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentList<E> newUnmodifiableLinkedList(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentLinkedList)
			return ((ConcurrentLinkedList<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList<>(new LinkedList<>(collection));
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent linked set
	 */
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableLinkedSet() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet<>(new LinkedHashSet<>());
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new unmodifiable concurrent linked set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableLinkedSet(@NotNull E... array) {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet<>(new LinkedHashSet<>(Arrays.asList(array)));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a linked set, preserving
	 * insertion order.
	 *
	 * @param collection the source collection
	 * @param <E> the element type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableLinkedSet(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentLinkedHashSet)
			return ((ConcurrentLinkedHashSet<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet<>(new LinkedHashSet<>(collection));
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap}.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty unmodifiable concurrent linked map
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableLinkedMap() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap<>(new LinkedHashMap<>());
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap} containing the given entries,
	 * preserving insertion order.
	 *
	 * @param pairs the entries to include
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new unmodifiable concurrent linked map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableLinkedMap(@NotNull Map.Entry<K, V>... pairs) {
		LinkedHashMap<K, V> snapshot = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : pairs) {
			if (entry != null) snapshot.put(entry.getKey(), entry.getValue());
		}
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap<>(snapshot);
	}

	/**
	 * Creates an immutable snapshot of the given map as a linked map, preserving insertion
	 * order.
	 *
	 * @param map the source map
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableLinkedMap(@NotNull Map<? extends K, ? extends V> map) {
		if (map instanceof ConcurrentLinkedHashMap)
			return ((ConcurrentLinkedHashMap<K, V>) map).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap<>(new LinkedHashMap<>(map));
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet} with natural element ordering.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent tree set
	 */
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableTreeSet() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet<>(new TreeSet<>());
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet} ordered by the given comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent tree set
	 */
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableTreeSet(@NotNull Comparator<? super E> comparator) {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet<>(new TreeSet<>(comparator));
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet} containing the given elements,
	 * with natural element ordering.
	 *
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new unmodifiable concurrent tree set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableTreeSet(@NotNull E... array) {
		TreeSet<E> snapshot = new TreeSet<>();
		Collections.addAll(snapshot, array);
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet<>(snapshot);
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet} containing the given elements,
	 * ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param array the elements to include
	 * @param <E> the element type
	 * @return a new unmodifiable concurrent tree set containing the specified elements ordered by the given comparator
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableTreeSet(@NotNull Comparator<? super E> comparator, @NotNull E... array) {
		TreeSet<E> snapshot = new TreeSet<>(comparator);
		Collections.addAll(snapshot, array);
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet<>(snapshot);
	}

	/**
	 * Creates an immutable snapshot of the given collection as a tree set with natural
	 * element ordering.
	 *
	 * @param collection the source collection
	 * @param <E> the element type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableTreeSet(@NotNull Collection<? extends E> collection) {
		if (collection instanceof ConcurrentTreeSet)
			return ((ConcurrentTreeSet<E>) collection).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet<>(new TreeSet<>(collection));
	}

	/**
	 * Creates an immutable snapshot of the given collection as a tree set ordered by the
	 * given comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param collection the source collection
	 * @param <E> the element type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet}
	 */
	public static <E> @NotNull ConcurrentSet<E> newUnmodifiableTreeSet(@NotNull Comparator<? super E> comparator, @NotNull Collection<? extends E> collection) {
		TreeSet<E> snapshot = new TreeSet<>(comparator);
		snapshot.addAll(collection);
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet<>(snapshot);
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} with natural key ordering.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty unmodifiable concurrent tree map
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableTreeMap() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap<>(new TreeMap<>());
	}

	/**
	 * Creates a new empty {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} ordered by the given comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty unmodifiable concurrent tree map
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableTreeMap(@NotNull Comparator<? super K> comparator) {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap<>(new TreeMap<>(comparator));
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} containing the given entries, with
	 * natural key ordering.
	 *
	 * @param pairs the entries to include
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new unmodifiable concurrent tree map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableTreeMap(Map.Entry<K, V> @NotNull ... pairs) {
		TreeMap<K, V> snapshot = new TreeMap<>();

		for (Map.Entry<K, V> entry : pairs) {
			if (entry != null)
				snapshot.put(entry.getKey(), entry.getValue());
		}

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap<>(snapshot);
	}

	/**
	 * Creates a new {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} containing the given entries,
	 * ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param pairs the entries to include
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new unmodifiable concurrent tree map containing the specified entries ordered by the given comparator
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableTreeMap(@NotNull Comparator<? super K> comparator, Map.Entry<K, V> @NotNull ... pairs) {
		TreeMap<K, V> snapshot = new TreeMap<>(comparator);

		for (Map.Entry<K, V> entry : pairs) {
			if (entry != null)
				snapshot.put(entry.getKey(), entry.getValue());
		}

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap<>(snapshot);
	}

	/**
	 * Creates an immutable snapshot of the given map as a tree map with natural key ordering.
	 *
	 * @param map the source map
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableTreeMap(@NotNull Map<? extends K, ? extends V> map) {
		if (map instanceof ConcurrentTreeMap)
			return ((ConcurrentTreeMap<K, V>) map).toUnmodifiable();

		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap<>(new TreeMap<>(map));
	}

	/**
	 * Creates an immutable snapshot of the given map as a tree map ordered by the given
	 * comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param map the source map
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a snapshot {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap}
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newUnmodifiableTreeMap(@NotNull Comparator<? super K> comparator, @NotNull Map<? extends K, ? extends V> map) {
		TreeMap<K, V> snapshot = new TreeMap<>(comparator);
		snapshot.putAll(map);
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap<>(snapshot);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentCollection} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentArrayList#adopt(List)}; see that method for
	 * the ownership contract.
	 *
	 * @param backing the collection to adopt
	 * @param <E> the element type
	 * @return a concurrent collection backed by {@code backing}
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentCollection<E> adoptCollection(@NotNull AbstractCollection<E> backing) {
		if (backing instanceof List<?>)
			return ConcurrentArrayList.adopt((List<E>) backing);

		return ConcurrentArrayList.adopt(new ArrayList<>(backing));
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentList} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentArrayList#adopt(List)}; see that method for the ownership
	 * contract.
	 *
	 * @param backing the list to adopt
	 * @param <E> the element type
	 * @return a concurrent list backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentList<E> adoptList(@NotNull List<E> backing) {
		return ConcurrentArrayList.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedList} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentLinkedList#adopt(LinkedList)}; see that method for the
	 * ownership contract.
	 *
	 * @param backing the linked list to adopt
	 * @param <E> the element type
	 * @return a concurrent linked list backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentLinkedList<E> adoptLinkedList(@NotNull LinkedList<E> backing) {
		return ConcurrentLinkedList.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentSet} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentHashSet#adopt(AbstractSet)}; see that method for the ownership
	 * contract.
	 *
	 * @param backing the set to adopt
	 * @param <E> the element type
	 * @return a concurrent set backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentSet<E> adoptSet(@NotNull AbstractSet<E> backing) {
		return ConcurrentHashSet.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedHashSet} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentLinkedHashSet#adopt(LinkedHashSet)}; see that method for the
	 * ownership contract.
	 *
	 * @param backing the linked hash set to adopt
	 * @param <E> the element type
	 * @return a concurrent linked set backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentSet<E> adoptLinkedSet(@NotNull LinkedHashSet<E> backing) {
		return ConcurrentLinkedHashSet.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentTreeSet} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentTreeSet#adopt(TreeSet)}; see that method for the ownership
	 * contract.
	 *
	 * @param backing the tree set to adopt
	 * @param <E> the element type
	 * @return a concurrent tree set backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> adoptTreeSet(@NotNull TreeSet<E> backing) {
		return ConcurrentTreeSet.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentMap} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentHashMap#adopt(AbstractMap)}; see that method for the ownership
	 * contract.
	 *
	 * @param backing the map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent map backed by {@code backing}
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> adoptMap(@NotNull AbstractMap<K, V> backing) {
		return ConcurrentHashMap.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedHashMap} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentLinkedHashMap#adopt(LinkedHashMap)}; see that method for the
	 * ownership contract.
	 *
	 * @param backing the linked hash map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent linked map backed by {@code backing}
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> adoptLinkedMap(@NotNull LinkedHashMap<K, V> backing) {
		return ConcurrentLinkedHashMap.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentTreeMap} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentTreeMap#adopt(TreeMap)}; see that method for the ownership
	 * contract.
	 *
	 * @param backing the tree map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent tree map backed by {@code backing}
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> adoptTreeMap(@NotNull TreeMap<K, V> backing) {
		return ConcurrentTreeMap.adopt(backing);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link ArrayList}
	 * (lock-free during accumulation), then adopts it as a {@link ConcurrentCollection} at finish.
	 * Equivalent to {@code Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new),
	 * Concurrent::adoptCollection)}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentCollection}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentCollection<E>> toCollection() {
		return Collectors.collectingAndThen(
			Collectors.<E, ArrayList<E>>toCollection(ArrayList::new),
			Concurrent::adoptCollection
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link LinkedList}
	 * (lock-free during accumulation), then promotes it to a {@link ConcurrentDeque} at finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentDeque}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentDeque<E>> toDeque() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedList<E>>toCollection(LinkedList::new),
			Concurrent::newDeque
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link LinkedList}
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentDeque} at
	 * finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent deque
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentDeque<E>> toUnmodifiableDeque() {
		return Collectors.collectingAndThen(
			Collectors.<E, ArrayDeque<E>>toCollection(ArrayDeque::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentArrayDeque::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link LinkedList}
	 * (lock-free during accumulation), then promotes it to a {@link ConcurrentQueue} at finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentQueue}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentQueue<E>> toQueue() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedList<E>>toCollection(LinkedList::new),
			Concurrent::newQueue
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link LinkedList}
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentQueue} at
	 * finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent queue
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentQueue<E>> toUnmodifiableQueue() {
		return Collectors.collectingAndThen(
			Collectors.<E, ArrayDeque<E>>toCollection(ArrayDeque::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentArrayQueue::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link LinkedList}
	 * (lock-free during accumulation), then adopts it as a {@link ConcurrentLinkedList} at finish.
	 * Equivalent to {@code Collectors.collectingAndThen(Collectors.toCollection(LinkedList::new),
	 * Concurrent::adoptLinkedList)}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentLinkedList}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentLinkedList<E>> toLinkedList() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedList<E>>toCollection(LinkedList::new),
			Concurrent::adoptLinkedList
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link LinkedList}
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentList}
	 * at finish, preserving insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent linked list
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentList<E>> toUnmodifiableLinkedList() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedList<E>>toCollection(LinkedList::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link ArrayList}
	 * (lock-free during accumulation), then adopts it as a {@link ConcurrentList} at finish.
	 * Equivalent to {@code Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new),
	 * Concurrent::adoptList)}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentList}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentList<E>> toList() {
		return Collectors.collectingAndThen(
			Collectors.<E, ArrayList<E>>toCollection(ArrayList::new),
			Concurrent::adoptList
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link ArrayList}
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentList} at
	 * finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent list
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentList<E>> toUnmodifiableList() {
		return Collectors.collectingAndThen(
			Collectors.<E, ArrayList<E>>toCollection(ArrayList::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link HashMap}
	 * (lock-free during accumulation) by casting each element to {@link Map.Entry}, then adopts
	 * it as a {@link ConcurrentMap} at finish. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentMap}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toWeakMap() {
		return toMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a fresh
	 * {@link HashMap} (lock-free during accumulation), then adopts it as a {@link ConcurrentMap}
	 * at finish. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap() {
		return toMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a fresh
	 * {@link HashMap} (lock-free during accumulation), using the specified merge function for
	 * duplicate keys, then adopts it as a {@link ConcurrentMap} at finish.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link HashMap}
	 * (lock-free during accumulation) using the given key and value mappers, then adopts it as a
	 * {@link ConcurrentMap} at finish. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toMap(keyMapper, valueMapper, throwingMerger(), HashMap::new, Concurrent::adoptMap);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link HashMap}
	 * (lock-free during accumulation) using the given key mapper, value mapper, and merge function
	 * for duplicate keys, then adopts it as a {@link ConcurrentMap} at finish.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toMap(keyMapper, valueMapper, mergeFunction, HashMap::new, Concurrent::adoptMap);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a raw {@link Map} supplied
	 * by {@code rawMapSupplier} (lock-free during accumulation), then adopts the populated map via
	 * {@code adoptFn} at finish. This is the most general {@code toMap} overload; the simpler
	 * overloads pin {@code rawMapSupplier} to {@link HashMap#HashMap()} and {@code adoptFn} to
	 * {@link Concurrent#adoptMap(AbstractMap)}.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param rawMapSupplier the supplier providing the empty backing map used during accumulation
	 * @param adoptFn the finisher that wraps the populated raw map as a concurrent variant
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @param <M> the raw backing map type
	 * @param <A> the result type (extends {@link ConcurrentMap})
	 * @return a collector producing a concurrent map of the adopted type
	 */
	public static <K, V, T, M extends Map<K, V>, A extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, A> toMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<M> rawMapSupplier,
		@NotNull Function<M, A> adoptFn
	) {
		return Collectors.collectingAndThen(
			Collectors.toMap(keyMapper, valueMapper, mergeFunction, rawMapSupplier),
			adoptFn
		);
	}

	/**
	 * Returns a {@link Collector} that reduces the stream to its sole element. Throws if the stream
	 * is empty or contains more than one element.
	 *
	 * @param <T> the stream element type
	 * @return a collector producing the single stream element
	 * @throws NoSuchElementException if the stream is empty
	 * @throws IllegalStateException if the stream contains more than one element
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
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation) by casting each element to
	 * {@link Map.Entry}, then adopts it as a {@link ConcurrentLinkedHashMap} at finish, preserving
	 * insertion order. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedHashMap}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toWeakLinkedMap() {
		return toLinkedMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation), then adopts it as a
	 * {@link ConcurrentLinkedHashMap} at finish, preserving insertion order. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentLinkedHashMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toLinkedMap() {
		return toLinkedMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation), using the specified merge function
	 * for duplicate keys, then adopts it as a {@link ConcurrentLinkedHashMap} at finish.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentLinkedHashMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toLinkedMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toLinkedMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation) using the given key and value mappers,
	 * then adopts it as a {@link ConcurrentLinkedHashMap} at finish. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedHashMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toLinkedMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new, Concurrent::adoptLinkedMap);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation) using the given key mapper, value
	 * mapper, and merge function for duplicate keys, then adopts it as a {@link ConcurrentLinkedHashMap}
	 * at finish.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedHashMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toLinkedMap(keyMapper, valueMapper, mergeFunction, LinkedHashMap::new, Concurrent::adoptLinkedMap);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a raw insertion-ordered
	 * {@link Map} supplied by {@code rawMapSupplier} (lock-free during accumulation), then adopts
	 * the populated map via {@code adoptFn} at finish. This is the most general {@code toLinkedMap}
	 * overload; the simpler overloads pin {@code rawMapSupplier} to {@link LinkedHashMap#LinkedHashMap()}
	 * and {@code adoptFn} to {@link Concurrent#adoptLinkedMap(LinkedHashMap)}.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param rawMapSupplier the supplier providing the empty backing map used during accumulation
	 * @param adoptFn the finisher that wraps the populated raw map as a concurrent linked variant
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @param <M> the raw backing map type
	 * @param <A> the result type (extends {@link ConcurrentLinkedHashMap})
	 * @return a collector producing a concurrent linked map of the adopted type
	 */
	public static <K, V, T, M extends Map<K, V>, A extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, A> toLinkedMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<M> rawMapSupplier,
		@NotNull Function<M, A> adoptFn
	) {
		return Collectors.collectingAndThen(
			Collectors.toMap(keyMapper, valueMapper, mergeFunction, rawMapSupplier),
			adoptFn
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}
	 * by casting each element to {@link Map.Entry}. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toWeakUnmodifiableMap() {
		return toUnmodifiableMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}.
	 * Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableMap() {
		return toUnmodifiableMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap},
	 * using the specified merge function for duplicate keys.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}
	 * using the given key and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toUnmodifiableMap(keyMapper, valueMapper, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}
	 * using the given key mapper, value mapper, and merge function for duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return Collectors.collectingAndThen(
			Collectors.<T, K, V, HashMap<K, V>>toMap(keyMapper, valueMapper, mergeFunction, HashMap::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a raw {@link Map} supplied
	 * by {@code rawMapSupplier} (lock-free during accumulation), then wraps the populated map as a
	 * {@link ConcurrentMap} via {@code finisher} at finish. This is the most general
	 * {@code toUnmodifiableMap} overload.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param rawMapSupplier the supplier providing the empty backing map used during accumulation
	 * @param finisher the finisher that wraps the populated raw map as a {@link ConcurrentMap}
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @param <M> the raw backing map type
	 * @return a collector producing a {@link ConcurrentMap}
	 */
	public static <K, V, T, M extends Map<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<M> rawMapSupplier,
		@NotNull Function<M, ConcurrentMap<K, V>> finisher
	) {
		return Collectors.collectingAndThen(
			Collectors.toMap(keyMapper, valueMapper, mergeFunction, rawMapSupplier),
			finisher
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} ordered by the given comparator. Throws on
	 * duplicate keys.
	 *
	 * @param comparator the comparator used to order and compare the keys
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} ordered by {@code comparator}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableTreeMap(@NotNull Comparator<? super K> comparator) {
		return toUnmodifiableTreeMap(
			Map.Entry::getKey,
			Map.Entry::getValue,
			throwingMerger(),
			() -> new TreeMap<>(comparator)
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a raw {@link TreeMap}
	 * supplied by {@code rawMapSupplier} (lock-free during accumulation), then wraps the populated
	 * tree map as a {@link ConcurrentMap} at finish.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param rawMapSupplier the supplier providing the empty backing tree map used during accumulation
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableTreeMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<TreeMap<K, V>> rawMapSupplier
	) {
		return Collectors.collectingAndThen(
			Collectors.toMap(keyMapper, valueMapper, mergeFunction, rawMapSupplier),
			ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link HashSet}
	 * (lock-free during accumulation), then adopts it as a {@link ConcurrentSet} at finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentSet}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, HashSet<E>>toCollection(HashSet::new),
			Concurrent::adoptSet
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link HashSet}
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentSet} at
	 * finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent set
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toUnmodifiableSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, HashSet<E>>toCollection(HashSet::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashSet} (lock-free during accumulation), then adopts it as a
	 * {@link ConcurrentLinkedHashSet} at finish, preserving insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentLinkedHashSet}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toLinkedSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedHashSet<E>>toCollection(LinkedHashSet::new),
			Concurrent::adoptLinkedSet
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashSet} (lock-free during accumulation), then wraps it as a
	 * {@link ConcurrentSet} at finish, preserving insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent linked set
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toUnmodifiableLinkedSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedHashSet<E>>toCollection(LinkedHashSet::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link TreeSet}
	 * (lock-free during accumulation), then adopts it as a {@link ConcurrentTreeSet} at finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentTreeSet}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentTreeSet<E>> toTreeSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, TreeSet<E>>toCollection(TreeSet::new),
			Concurrent::adoptTreeSet
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link TreeSet}
	 * ordered by {@code comparator} (lock-free during accumulation), then adopts it as a
	 * {@link ConcurrentTreeSet} at finish.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentTreeSet} ordered by {@code comparator}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentTreeSet<E>> toTreeSet(@NotNull Comparator<? super E> comparator) {
		return Collectors.collectingAndThen(
			Collectors.<E, TreeSet<E>>toCollection(() -> new TreeSet<>(comparator)),
			Concurrent::adoptTreeSet
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link TreeSet}
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentSet} at
	 * finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent tree set
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toUnmodifiableTreeSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, TreeSet<E>>toCollection(TreeSet::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link TreeSet}
	 * ordered by {@code comparator} (lock-free during accumulation), then wraps it as a
	 * {@link ConcurrentSet} at finish.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent tree set ordered by {@code comparator}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toUnmodifiableTreeSet(@NotNull Comparator<? super E> comparator) {
		return Collectors.collectingAndThen(
			Collectors.<E, TreeSet<E>>toCollection(() -> new TreeSet<>(comparator)),
			ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a fresh
	 * {@link TreeMap} ordered by {@code comparator} (lock-free during accumulation), then adopts
	 * it as a {@link ConcurrentTreeMap} at finish. Throws on duplicate keys.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentTreeMap} ordered by {@code comparator}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMap(@NotNull Comparator<? super K> comparator) {
		return toTreeMap(comparator, Map.Entry::getKey, Map.Entry::getValue, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link TreeMap}
	 * with natural key ordering (lock-free during accumulation), then adopts it as a
	 * {@link ConcurrentTreeMap} at finish. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentTreeMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toTreeMapInternal(TreeMap::new, keyMapper, valueMapper, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link TreeMap}
	 * with natural key ordering (lock-free during accumulation), using the given merge function
	 * for duplicate keys, then adopts it as a {@link ConcurrentTreeMap} at finish.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentTreeMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toTreeMapInternal(TreeMap::new, keyMapper, valueMapper, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link TreeMap}
	 * ordered by {@code comparator} (lock-free during accumulation), using the given merge function
	 * for duplicate keys, then adopts it as a {@link ConcurrentTreeMap} at finish.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentTreeMap} ordered by {@code comparator}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMap(@NotNull Comparator<? super K> comparator, @NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toTreeMapInternal(() -> new TreeMap<>(comparator), keyMapper, valueMapper, mergeFunction);
	}

	/**
	 * Builds a {@link Collector} that accumulates stream elements into a raw {@link TreeMap}
	 * supplied by {@code rawMapSupplier} (lock-free during accumulation), then adopts the
	 * populated tree map as a {@link ConcurrentTreeMap} at finish.
	 *
	 * @param rawMapSupplier the supplier providing the empty backing tree map
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentTreeMap}
	 */
	private static <K, V, T> @NotNull Collector<T, ?, ConcurrentTreeMap<K, V>> toTreeMapInternal(
		@NotNull Supplier<TreeMap<K, V>> rawMapSupplier,
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction
	) {
		return Collectors.collectingAndThen(
			Collectors.toMap(keyMapper, valueMapper, mergeFunction, rawMapSupplier),
			Concurrent::adoptTreeMap
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap}, preserving insertion order. Throws on
	 * duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableLinkedMap() {
		return toUnmodifiableLinkedMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap}, preserving insertion order, using the
	 * specified merge function for duplicate keys.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableLinkedMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toUnmodifiableLinkedMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap} using the given key and value mappers,
	 * preserving insertion order. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toUnmodifiableLinkedMap(keyMapper, valueMapper, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap} using the given key mapper, value mapper, and
	 * merge function for duplicate keys, preserving insertion order.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return Collectors.collectingAndThen(
			Collectors.<T, K, V, LinkedHashMap<K, V>>toMap(keyMapper, valueMapper, mergeFunction, LinkedHashMap::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} with natural key ordering. Throws on
	 * duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableTreeMap() {
		return toUnmodifiableTreeMap(Map.Entry::getKey, Map.Entry::getValue, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} with natural key ordering, using the given key
	 * and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableTreeMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toUnmodifiableTreeMap(keyMapper, valueMapper, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} with natural key ordering, using the given key
	 * mapper, value mapper, and merge function for duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toUnmodifiableTreeMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return Collectors.collectingAndThen(
			Collectors.<T, K, V, TreeMap<K, V>>toMap(keyMapper, valueMapper, mergeFunction, TreeMap::new),
			ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap::new
		);
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
