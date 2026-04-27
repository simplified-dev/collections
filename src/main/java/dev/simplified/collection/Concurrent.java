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
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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
	 * Creates a new {@link ConcurrentCollection} by copying all elements from {@code collection}.
	 * <p>
	 * The returned collection owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned collection and vice versa.
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
	 * Creates a new {@link ConcurrentDeque} by copying all elements from {@code collection}.
	 * <p>
	 * The returned deque owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned deque and vice versa.
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
	 * Creates a new {@link ConcurrentList} by copying all elements from {@code collection}.
	 * <p>
	 * The returned list owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned list and vice versa. A {@code null}
	 * collection produces an empty list.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty list
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
	 * Creates a new {@link ConcurrentQueue} by copying all elements from {@code collection}.
	 * <p>
	 * The returned queue owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned queue and vice versa.
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
	 * Creates a new {@link ConcurrentSet} by copying all elements from {@code collection}.
	 * <p>
	 * The returned set owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned set and vice versa. A {@code null} collection
	 * produces an empty set.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty set
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
	 * Creates a new {@link ConcurrentLinkedList} by copying all elements from {@code collection}.
	 * <p>
	 * The returned linked list owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned list and vice versa.
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
	 * Creates a new {@link ConcurrentLinkedMap} by copying all entries from {@code map}.
	 * <p>
	 * The returned linked map owns its own backing storage; subsequent modifications to
	 * {@code map} do not affect the returned map and vice versa.
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
	 * Creates a new {@link ConcurrentLinkedMap} with the given maximum size by copying all
	 * entries from {@code map}.
	 * <p>
	 * The returned linked map owns its own backing storage; subsequent modifications to
	 * {@code map} do not affect the returned map and vice versa.
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
		return new ConcurrentTreeMap.Impl<>(map);
	}

	/**
	 * Creates a new {@link ConcurrentTreeMap} ordered by {@code comparator} by copying all
	 * entries from {@code map}.
	 * <p>
	 * The returned tree map owns its own backing storage; subsequent modifications to {@code map}
	 * do not affect the returned map and vice versa.
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
	 * Creates a new {@link ConcurrentTreeSet} with natural element ordering by copying all
	 * elements from {@code collection}.
	 * <p>
	 * The returned tree set owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned set and vice versa.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent tree set containing the source elements
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> newTreeSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentTreeSet.Impl<>(collection);
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
	 * Creates a new {@link ConcurrentLinkedSet} by copying all elements from {@code collection}.
	 * <p>
	 * The returned linked set owns its own backing storage; subsequent modifications to
	 * {@code collection} do not affect the returned set and vice versa.
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
	 * Wraps {@code backing} as a {@link ConcurrentCollection} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentCollection#adopt(AbstractCollection)}; see that method for
	 * the ownership contract.
	 *
	 * @param backing the collection to adopt
	 * @param <E> the element type
	 * @return a concurrent collection backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentCollection<E> adoptCollection(@NotNull AbstractCollection<E> backing) {
		return ConcurrentCollection.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentList} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentList#adopt(List)}; see that method for the ownership
	 * contract.
	 *
	 * @param backing the list to adopt
	 * @param <E> the element type
	 * @return a concurrent list backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentList<E> adoptList(@NotNull List<E> backing) {
		return ConcurrentList.adopt(backing);
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
	 * Delegates to {@link ConcurrentSet#adopt(AbstractSet)}; see that method for the ownership
	 * contract.
	 *
	 * @param backing the set to adopt
	 * @param <E> the element type
	 * @return a concurrent set backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentSet<E> adoptSet(@NotNull AbstractSet<E> backing) {
		return ConcurrentSet.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedSet} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentLinkedSet#adopt(LinkedHashSet)}; see that method for the
	 * ownership contract.
	 *
	 * @param backing the linked hash set to adopt
	 * @param <E> the element type
	 * @return a concurrent linked set backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentLinkedSet<E> adoptLinkedSet(@NotNull LinkedHashSet<E> backing) {
		return ConcurrentLinkedSet.adopt(backing);
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
	 * Delegates to {@link ConcurrentMap#adopt(AbstractMap)}; see that method for the ownership
	 * contract.
	 *
	 * @param backing the map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent map backed by {@code backing}
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> adoptMap(@NotNull AbstractMap<K, V> backing) {
		return ConcurrentMap.adopt(backing);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedMap} without copying.
	 * <p>
	 * Delegates to {@link ConcurrentLinkedMap#adopt(LinkedHashMap)}; see that method for the
	 * ownership contract.
	 *
	 * @param backing the linked hash map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent linked map backed by {@code backing}
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> adoptLinkedMap(@NotNull LinkedHashMap<K, V> backing) {
		return ConcurrentLinkedMap.adopt(backing);
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
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentUnmodifiableDeque} at
	 * finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent deque
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableDeque<E>> toUnmodifiableDeque() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedList<E>>toCollection(LinkedList::new),
			Concurrent::newUnmodifiableDeque
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
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentUnmodifiableQueue} at
	 * finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent queue
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableQueue<E>> toUnmodifiableQueue() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedList<E>>toCollection(LinkedList::new),
			Concurrent::newUnmodifiableQueue
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
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentUnmodifiableLinkedList}
	 * at finish, preserving insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent linked list
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableList<E>> toUnmodifiableLinkedList() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedList<E>>toCollection(LinkedList::new),
			list -> Concurrent.adoptLinkedList(list).toUnmodifiable()
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
	 * (lock-free during accumulation), then adopts it as a {@link ConcurrentList} and exposes it
	 * via {@link ConcurrentList#toUnmodifiable()} at finish.
	 *
	 * @param <E> the element type
	 * @param <A> the result type (extends {@link ConcurrentList})
	 * @return a collector producing an unmodifiable concurrent list
	 */
	@SuppressWarnings("unchecked")
	public static <E, A extends ConcurrentList<E>> @NotNull Collector<E, ?, A> toUnmodifiableList() {
		return Collectors.collectingAndThen(
			Collectors.<E, ArrayList<E>>toCollection(ArrayList::new),
			list -> (A) Concurrent.adoptList(list).toUnmodifiable()
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
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type (must extend {@link Map.Entry})
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
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
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
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
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
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation) by casting each element to
	 * {@link Map.Entry}, then adopts it as a {@link ConcurrentLinkedMap} at finish, preserving
	 * insertion order. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedMap}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toWeakLinkedMap() {
		return toLinkedMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation), then adopts it as a
	 * {@link ConcurrentLinkedMap} at finish, preserving insertion order. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentLinkedMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap() {
		return toLinkedMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation), using the specified merge function
	 * for duplicate keys, then adopts it as a {@link ConcurrentLinkedMap} at finish.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentLinkedMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toLinkedMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation) using the given key and value mappers,
	 * then adopts it as a {@link ConcurrentLinkedMap} at finish. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toLinkedMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new, Concurrent::adoptLinkedMap);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashMap} (lock-free during accumulation) using the given key mapper, value
	 * mapper, and merge function for duplicate keys, then adopts it as a {@link ConcurrentLinkedMap}
	 * at finish.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
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
	 * @param <A> the result type (extends {@link ConcurrentLinkedMap})
	 * @return a collector producing a concurrent linked map of the adopted type
	 */
	public static <K, V, T, M extends Map<K, V>, A extends ConcurrentLinkedMap<K, V>> @NotNull Collector<T, ?, A> toLinkedMap(
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
		return Collectors.collectingAndThen(
			Collectors.<T, K, V, HashMap<K, V>>toMap(keyMapper, valueMapper, mergeFunction, HashMap::new),
			Concurrent::newUnmodifiableMap
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a raw {@link Map} supplied
	 * by {@code rawMapSupplier} (lock-free during accumulation), then wraps the populated map as a
	 * {@link ConcurrentUnmodifiableMap} via {@code finisher} at finish. This is the most general
	 * {@code toUnmodifiableMap} overload.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param rawMapSupplier the supplier providing the empty backing map used during accumulation
	 * @param finisher the finisher that wraps the populated raw map as a {@link ConcurrentUnmodifiableMap}
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @param <M> the raw backing map type
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap}
	 */
	public static <K, V, T, M extends Map<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<M> rawMapSupplier,
		@NotNull Function<M, ConcurrentUnmodifiableMap<K, V>> finisher
	) {
		return Collectors.collectingAndThen(
			Collectors.toMap(keyMapper, valueMapper, mergeFunction, rawMapSupplier),
			finisher
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
			() -> new TreeMap<>(comparator)
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a raw {@link TreeMap}
	 * supplied by {@code rawMapSupplier} (lock-free during accumulation), then wraps the populated
	 * tree map as a {@link ConcurrentUnmodifiableTreeMap} at finish.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param rawMapSupplier the supplier providing the empty backing tree map used during accumulation
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableTreeMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableTreeMap<K, V>> toUnmodifiableTreeMap(
		@NotNull Function<? super T, ? extends K> keyMapper,
		@NotNull Function<? super T, ? extends V> valueMapper,
		@NotNull BinaryOperator<V> mergeFunction,
		@NotNull Supplier<TreeMap<K, V>> rawMapSupplier
	) {
		return Collectors.collectingAndThen(
			Collectors.toMap(keyMapper, valueMapper, mergeFunction, rawMapSupplier),
			ConcurrentUnmodifiableTreeMap.Impl::new
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
	 * (lock-free during accumulation), then adopts it as a {@link ConcurrentSet} and exposes it
	 * via {@link ConcurrentSet#toUnmodifiable()} at finish.
	 *
	 * @param <E> the element type
	 * @param <A> the result type (extends {@link ConcurrentSet})
	 * @return a collector producing an unmodifiable concurrent set
	 */
	@SuppressWarnings("unchecked")
	public static <E, A extends ConcurrentSet<E>> @NotNull Collector<E, ?, A> toUnmodifiableSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, HashSet<E>>toCollection(HashSet::new),
			set -> (A) Concurrent.adoptSet(set).toUnmodifiable()
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashSet} (lock-free during accumulation), then adopts it as a
	 * {@link ConcurrentLinkedSet} at finish, preserving insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentLinkedSet}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentLinkedSet<E>> toLinkedSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedHashSet<E>>toCollection(LinkedHashSet::new),
			Concurrent::adoptLinkedSet
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh
	 * {@link LinkedHashSet} (lock-free during accumulation), then wraps it as a
	 * {@link ConcurrentUnmodifiableLinkedSet} at finish, preserving insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent linked set
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableLinkedSet<E>> toUnmodifiableLinkedSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, LinkedHashSet<E>>toCollection(LinkedHashSet::new),
			ConcurrentUnmodifiableLinkedSet.Impl::new
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
	 * (lock-free during accumulation), then wraps it as a {@link ConcurrentUnmodifiableTreeSet} at
	 * finish.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent tree set
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableTreeSet<E>> toUnmodifiableTreeSet() {
		return Collectors.collectingAndThen(
			Collectors.<E, TreeSet<E>>toCollection(TreeSet::new),
			ConcurrentUnmodifiableTreeSet.Impl::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a fresh {@link TreeSet}
	 * ordered by {@code comparator} (lock-free during accumulation), then wraps it as a
	 * {@link ConcurrentUnmodifiableTreeSet} at finish.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent tree set ordered by {@code comparator}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableTreeSet<E>> toUnmodifiableTreeSet(@NotNull Comparator<? super E> comparator) {
		return Collectors.collectingAndThen(
			Collectors.<E, TreeSet<E>>toCollection(() -> new TreeSet<>(comparator)),
			ConcurrentUnmodifiableTreeSet.Impl::new
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a fresh
	 * {@link TreeMap} ordered by {@code comparator} (lock-free during accumulation), then adopts
	 * it as a {@link ConcurrentTreeMap} at finish. Throws on duplicate keys.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @param <T>        the stream element type (must extend {@link Map.Entry})
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
	 * @param keyMapper   the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
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
	 * @param keyMapper     the function to extract map keys from stream elements
	 * @param valueMapper   the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
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
	 * @param comparator    the comparator used to order the keys
	 * @param keyMapper     the function to extract map keys from stream elements
	 * @param valueMapper   the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
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
	 * @param keyMapper      the function to extract map keys from stream elements
	 * @param valueMapper    the function to extract map values from stream elements
	 * @param mergeFunction  the function to resolve collisions between values associated with the same key
	 * @param <K>            the key type
	 * @param <V>            the value type
	 * @param <T>            the stream element type
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
		return Collectors.collectingAndThen(
			Collectors.<T, K, V, LinkedHashMap<K, V>>toMap(keyMapper, valueMapper, mergeFunction, LinkedHashMap::new),
			ConcurrentUnmodifiableLinkedMap.Impl::new
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
		return Collectors.collectingAndThen(
			Collectors.<T, K, V, TreeMap<K, V>>toMap(keyMapper, valueMapper, mergeFunction, TreeMap::new),
			ConcurrentUnmodifiableTreeMap.Impl::new
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
