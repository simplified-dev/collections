package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.atomic.AtomicDeque;
import dev.simplified.collection.atomic.AtomicList;
import dev.simplified.collection.atomic.AtomicMap;
import dev.simplified.collection.atomic.AtomicQueue;
import dev.simplified.collection.atomic.AtomicSet;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import dev.simplified.collection.linked.ConcurrentLinkedMap;
import dev.simplified.collection.linked.ConcurrentLinkedSet;
import dev.simplified.collection.sorted.ConcurrentSortedMap;
import dev.simplified.collection.sorted.ConcurrentSortedSet;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableCollection;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableDeque;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableList;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableMap;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableQueue;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableSet;
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

	/** Collector characteristics for ordered, concurrent collectors with identity finish. */
	public static final ConcurrentSet<Collector.Characteristics> ORDERED_CHARACTERISTICS = Concurrent.newSet(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH);
	/** Collector characteristics for unordered, concurrent collectors with identity finish. */
	public static final ConcurrentSet<Collector.Characteristics> UNORDERED_CHARACTERISTICS = Concurrent.newSet(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH, Collector.Characteristics.UNORDERED);

	/**
	 * Returns a merge function that always throws {@link IllegalStateException} on duplicate keys.
	 *
	 * @param <T> the type of the values being merged
	 * @return a merge function that rejects duplicates
	 */
	private static <T> @NotNull BinaryOperator<T> throwingMerger() {
		return (key, value) -> { throw new IllegalStateException(String.format("Duplicate key %s", key)); };
	}

	/**
	 * Creates a new empty {@link ConcurrentCollection}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent collection
	 */
	public static <E> @NotNull ConcurrentCollection<E> newCollection() {
		return new ConcurrentCollection<>();
	}

	/**
	 * Creates a new {@link ConcurrentCollection} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent collection containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentCollection<E> newCollection(@NotNull E... array) {
		return new ConcurrentCollection<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentCollection} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent collection containing the source elements
	 */
	public static <E> @NotNull ConcurrentCollection<E> newCollection(@NotNull Collection<? extends E> collection) {
		return new ConcurrentCollection<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentDeque}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent deque
	 */
	public static <E> @NotNull ConcurrentDeque<E> newDeque() {
		return new ConcurrentDeque<>();
	}

	/**
	 * Creates a new {@link ConcurrentDeque} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent deque containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentDeque<E> newDeque(@NotNull E... array) {
		return new ConcurrentDeque<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentDeque} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent deque containing the source elements
	 */
	public static <E> @NotNull ConcurrentDeque<E> newDeque(@NotNull Collection<? extends E> collection) {
		return new ConcurrentDeque<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentList}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent list
	 */
	public static <E> @NotNull ConcurrentList<E> newList() {
		return new ConcurrentList<>();
	}

	/**
	 * Creates a new {@link ConcurrentList} containing the given elements.
	 *
	 * @param initialCapacity the initial capacity of the underlying list
	 * @param <E>             the element type
	 * @return a new concurrent list containing the specified elements
	 */
	public static <E> @NotNull ConcurrentList<E> newList(int initialCapacity) {
		return new ConcurrentList<>(initialCapacity);
	}

	/**
	 * Creates a new {@link ConcurrentList} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentList<E> newList(@NotNull E... array) {
		return new ConcurrentList<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentList} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent list containing the source elements
	 */
	public static <E> @NotNull ConcurrentList<E> newList(@Nullable Collection<? extends E> collection) {
		return new ConcurrentList<>(collection);
	}

	/**
	 * Creates a new {@link ConcurrentMap} containing the given map entries.
	 *
	 * @param entries the entries to include
	 * @param <K>     the key type
	 * @param <V>     the value type
	 * @return a new concurrent map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentMap<K, V> newMap(@NotNull Map.Entry<K, V>... entries) {
		return new ConcurrentMap<>(entries);
	}

	/**
	 * Creates a new {@link ConcurrentMap} containing all entries from the given map.
	 *
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent map containing the source entries
	 */
	public static <K, V> @NotNull ConcurrentMap<K, V> newMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentMap<>(map);
	}

	/**
	 * Creates a new empty {@link ConcurrentQueue}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent queue
	 */
	public static <E> @NotNull ConcurrentQueue<E> newQueue() {
		return new ConcurrentQueue<>();
	}

	/**
	 * Creates a new {@link ConcurrentQueue} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent queue containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentQueue<E> newQueue(@NotNull E... array) {
		return new ConcurrentQueue<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentQueue} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent queue containing the source elements
	 */
	public static <E> @NotNull ConcurrentQueue<E> newQueue(@NotNull Collection<? extends E> collection) {
		return new ConcurrentQueue<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentSet}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent set
	 */
	public static <E> @NotNull ConcurrentSet<E> newSet() {
		return new ConcurrentSet<>();
	}

	/**
	 * Creates a new {@link ConcurrentSet} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentSet<E> newSet(@NotNull E... array) {
		return new ConcurrentSet<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentSet} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent set containing the source elements
	 */
	public static <E> @NotNull ConcurrentSet<E> newSet(@Nullable Collection<? extends E> collection) {
		return new ConcurrentSet<>(collection);
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
	 * @param <E>   the element type
	 * @return a new concurrent linked list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList(@NotNull E... array) {
		return new ConcurrentLinkedList<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedList} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent linked list containing the source elements
	 */
	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList(@NotNull Collection<? extends E> collection) {
		return new ConcurrentLinkedList<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedMap} with no maximum size constraint.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty concurrent linked map
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap() {
		return newLinkedMap(-1);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedMap} with the specified maximum size.
	 * A value of {@code -1} indicates no size limit.
	 *
	 * @param maxSize the maximum number of entries, or {@code -1} for unlimited
	 * @param <K>     the key type
	 * @param <V>     the value type
	 * @return a new empty concurrent linked map with the given size constraint
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap(int maxSize) {
		return new ConcurrentLinkedMap<>(maxSize);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedMap} containing all entries from the given map.
	 *
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent linked map containing the source entries
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentLinkedMap<>(map);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedMap} containing all entries from the given map, with a maximum size.
	 *
	 * @param map the source map to copy from
	 * @param maxSize the maximum number of entries, or {@code -1} for unlimited
	 * @param <K>     the key type
	 * @param <V>     the value type
	 * @return a new concurrent linked map containing the source entries with the given size constraint
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap(@NotNull Map<? extends K, ? extends V> map, int maxSize) {
		return new ConcurrentLinkedMap<>(map, maxSize);
	}

	/**
	 * Creates a new empty {@link ConcurrentSortedMap} with natural key ordering.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty concurrent sorted map
	 */
	public static <K, V> @NotNull ConcurrentSortedMap<K, V> newSortedMap() {
		return new ConcurrentSortedMap<>();
	}

	/**
	 * Creates a new empty {@link ConcurrentSortedMap} with the specified comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @return a new empty concurrent sorted map ordered by the given comparator
	 */
	public static <K, V> @NotNull ConcurrentSortedMap<K, V> newSortedMap(@NotNull Comparator<? super K> comparator) {
		return new ConcurrentSortedMap<>(comparator);
	}

	/**
	 * Creates a new {@link ConcurrentSortedMap} containing all entries from the given map,
	 * with natural key ordering.
	 *
	 * @param map the source map to copy from
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent sorted map containing the source entries
	 */
	public static <K, V> @NotNull ConcurrentSortedMap<K, V> newSortedMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentSortedMap<>(map);
	}

	/**
	 * Creates a new {@link ConcurrentSortedMap} containing all entries from the given map,
	 * ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param map        the source map to copy from
	 * @param <K>        the key type
	 * @param <V>        the value type
	 * @return a new concurrent sorted map containing the source entries ordered by the given comparator
	 */
	public static <K, V> @NotNull ConcurrentSortedMap<K, V> newSortedMap(@NotNull Comparator<? super K> comparator, @NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentSortedMap<>(comparator, map);
	}

	/**
	 * Creates a new empty {@link ConcurrentSortedSet} with natural element ordering.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent sorted set
	 */
	public static <E> @NotNull ConcurrentSortedSet<E> newSortedSet() {
		return new ConcurrentSortedSet<>();
	}

	/**
	 * Creates a new empty {@link ConcurrentSortedSet} with the specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E>        the element type
	 * @return a new empty concurrent sorted set ordered by the given comparator
	 */
	public static <E> @NotNull ConcurrentSortedSet<E> newSortedSet(@NotNull Comparator<? super E> comparator) {
		return new ConcurrentSortedSet<>(comparator);
	}

	/**
	 * Creates a new {@link ConcurrentSortedSet} containing all elements from the given collection,
	 * with natural element ordering.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent sorted set containing the source elements
	 */
	public static <E> @NotNull ConcurrentSortedSet<E> newSortedSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentSortedSet<>(collection);
	}

	/**
	 * Creates a new {@link ConcurrentSortedSet} containing all elements from the given collection,
	 * ordered by the specified comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent sorted set containing the source elements ordered by the given comparator
	 */
	public static <E> @NotNull ConcurrentSortedSet<E> newSortedSet(@NotNull Comparator<? super E> comparator, @NotNull Collection<? extends E> collection) {
		return new ConcurrentSortedSet<>(comparator, collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedSet}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent linked set
	 */
	public static <E> @NotNull ConcurrentLinkedSet<E> newLinkedSet() {
		return new ConcurrentLinkedSet<>();
	}

	/**
	 * Creates a new {@link ConcurrentLinkedSet} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new concurrent linked set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentLinkedSet<E> newLinkedSet(@NotNull E... array) {
		return new ConcurrentLinkedSet<>(array);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedSet} containing all elements from the given collection.
	 *
	 * @param collection the source collection to copy from
	 * @param <E>        the element type
	 * @return a new concurrent linked set containing the source elements
	 */
	public static <E> @NotNull ConcurrentLinkedSet<E> newLinkedSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentLinkedSet<>(collection);
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableCollection} backed by a fresh
	 * {@link ConcurrentCollection}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent collection
	 */
	public static <E> @NotNull ConcurrentUnmodifiableCollection<E> newUnmodifiableCollection() {
		return new ConcurrentUnmodifiableCollection<>(new ConcurrentCollection<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableCollection} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent collection containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableCollection<E> newUnmodifiableCollection(@NotNull E... array) {
		return new ConcurrentUnmodifiableCollection<>(new ConcurrentCollection<>(array));
	}

	/**
	 * Creates an unmodifiable view of the given collection. If the source is already an
	 * {@link AtomicCollection}, the returned wrapper shares its state (live view); otherwise
	 * the source is first copied into a fresh {@link ConcurrentCollection}.
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return an unmodifiable {@link ConcurrentCollection} view
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableCollection<E> newUnmodifiableCollection(@NotNull Collection<? extends E> collection) {
		AtomicCollection<E, ? extends java.util.AbstractCollection<E>> source = collection instanceof AtomicCollection
			? (AtomicCollection<E, ? extends java.util.AbstractCollection<E>>) collection
			: new ConcurrentCollection<>((Collection<E>) collection);
		return new ConcurrentUnmodifiableCollection<>(source);
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableList} backed by a fresh
	 * {@link ConcurrentList}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent list
	 */
	public static <E> @NotNull ConcurrentUnmodifiableList<E> newUnmodifiableList() {
		return new ConcurrentUnmodifiableList<>(new ConcurrentList<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableList} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent list containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableList<E> newUnmodifiableList(@NotNull E... array) {
		return new ConcurrentUnmodifiableList<>(new ConcurrentList<>(array));
	}

	/**
	 * Creates an unmodifiable view of the given collection as a list. If the source is
	 * already an {@link AtomicList}, the wrapper shares its state (live view, preserves
	 * insertion order for {@link ConcurrentLinkedList}); otherwise the source is copied
	 * into a fresh {@link ConcurrentList}.
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return an unmodifiable {@link ConcurrentList} view
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableList<E> newUnmodifiableList(@Nullable Collection<? extends E> collection) {
		AtomicList<E, ? extends java.util.List<E>> source = collection instanceof AtomicList
			? (AtomicList<E, ? extends java.util.List<E>>) collection
			: new ConcurrentList<>((Collection<E>) collection);
		return new ConcurrentUnmodifiableList<>(source);
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableMap} containing the given map entries.
	 *
	 * @param entries the entries to include
	 * @param <K>     the key type
	 * @param <V>     the value type
	 * @return a new unmodifiable concurrent map containing the specified entries
	 */
	@SafeVarargs
	public static <K, V> @NotNull ConcurrentUnmodifiableMap<K, V> newUnmodifiableMap(@NotNull Map.Entry<K, V>... entries) {
		return new ConcurrentUnmodifiableMap<>(new ConcurrentMap<>(entries));
	}

	/**
	 * Creates an unmodifiable view of the given map. If the source is already an
	 * {@link AtomicMap}, the wrapper shares its state (live view, preserves iteration
	 * order for {@link ConcurrentLinkedMap} or {@link ConcurrentSortedMap}); otherwise
	 * the source is copied into a fresh {@link ConcurrentMap}.
	 *
	 * @param map the source map
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return an unmodifiable {@link ConcurrentMap} view
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> @NotNull ConcurrentUnmodifiableMap<K, V> newUnmodifiableMap(@NotNull Map<? extends K, ? extends V> map) {
		AtomicMap<K, V, ? extends java.util.AbstractMap<K, V>> source = map instanceof AtomicMap
			? (AtomicMap<K, V, ? extends java.util.AbstractMap<K, V>>) map
			: new ConcurrentMap<>((Map<K, V>) map);
		return new ConcurrentUnmodifiableMap<>(source);
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableSet} backed by a fresh
	 * {@link ConcurrentSet}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent set
	 */
	public static <E> @NotNull ConcurrentUnmodifiableSet<E> newUnmodifiableSet() {
		return new ConcurrentUnmodifiableSet<>(new ConcurrentSet<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableSet} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent set containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableSet<E> newUnmodifiableSet(@NotNull E... array) {
		return new ConcurrentUnmodifiableSet<>(new ConcurrentSet<>(array));
	}

	/**
	 * Creates an unmodifiable view of the given collection as a set. If the source is
	 * already an {@link AtomicSet}, the wrapper shares its state (live view, preserves
	 * ordering for {@link ConcurrentLinkedSet} / {@link ConcurrentSortedSet}); otherwise
	 * the source is copied into a fresh {@link ConcurrentSet}.
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return an unmodifiable {@link ConcurrentSet} view
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableSet<E> newUnmodifiableSet(@NotNull Collection<? extends E> collection) {
		AtomicSet<E, ? extends java.util.AbstractSet<E>> source = collection instanceof AtomicSet
			? (AtomicSet<E, ? extends java.util.AbstractSet<E>>) collection
			: new ConcurrentSet<>((Collection<E>) collection);
		return new ConcurrentUnmodifiableSet<>(source);
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableQueue}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent queue
	 */
	public static <E> @NotNull ConcurrentUnmodifiableQueue<E> newUnmodifiableQueue() {
		return new ConcurrentUnmodifiableQueue<>(new ConcurrentQueue<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableQueue} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent queue containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableQueue<E> newUnmodifiableQueue(@NotNull E... array) {
		return new ConcurrentUnmodifiableQueue<>(new ConcurrentQueue<>(array));
	}

	/**
	 * Creates an unmodifiable view of the given queue. If the source is already an
	 * {@link AtomicQueue}, the wrapper shares its state (live view); otherwise the source
	 * is copied into a fresh {@link ConcurrentQueue}.
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return an unmodifiable {@link ConcurrentQueue} view
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableQueue<E> newUnmodifiableQueue(@NotNull Collection<? extends E> collection) {
		AtomicQueue<E> source = collection instanceof AtomicQueue
			? (AtomicQueue<E>) collection
			: new ConcurrentQueue<>((Collection<E>) collection);
		return new ConcurrentUnmodifiableQueue<>(source);
	}

	/**
	 * Creates a new empty {@link ConcurrentUnmodifiableDeque}.
	 *
	 * @param <E> the element type
	 * @return a new empty unmodifiable concurrent deque
	 */
	public static <E> @NotNull ConcurrentUnmodifiableDeque<E> newUnmodifiableDeque() {
		return new ConcurrentUnmodifiableDeque<>(new ConcurrentDeque<>());
	}

	/**
	 * Creates a new {@link ConcurrentUnmodifiableDeque} containing the given elements.
	 *
	 * @param array the elements to include
	 * @param <E>   the element type
	 * @return a new unmodifiable concurrent deque containing the specified elements
	 */
	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableDeque<E> newUnmodifiableDeque(@NotNull E... array) {
		return new ConcurrentUnmodifiableDeque<>(new ConcurrentDeque<>(array));
	}

	/**
	 * Creates an unmodifiable view of the given deque. If the source is already an
	 * {@link AtomicDeque}, the wrapper shares its state (live view); otherwise the source
	 * is copied into a fresh {@link ConcurrentDeque}.
	 *
	 * @param collection the source collection
	 * @param <E>        the element type
	 * @return an unmodifiable {@link ConcurrentDeque} view
	 */
	@SuppressWarnings("unchecked")
	public static <E> @NotNull ConcurrentUnmodifiableDeque<E> newUnmodifiableDeque(@NotNull Collection<? extends E> collection) {
		AtomicDeque<E> source = collection instanceof AtomicDeque
			? (AtomicDeque<E>) collection
			: new ConcurrentDeque<>((Collection<E>) collection);
		return new ConcurrentUnmodifiableDeque<>(source);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentCollection}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentCollection}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentCollection<E>> toCollection() {
		return new StreamCollector<>(ConcurrentCollection::new, ConcurrentCollection::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentLinkedList}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentLinkedList}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentLinkedList<E>> toLinkedList() {
		return new StreamCollector<>(ConcurrentLinkedList::new, ConcurrentLinkedList::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new
	 * {@link ConcurrentUnmodifiableList} backed by a {@link ConcurrentLinkedList}, so
	 * iteration of the result preserves insertion order.
	 *
	 * @param <E> the element type
	 * @return a collector producing an unmodifiable concurrent linked list
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentUnmodifiableList<E>> toUnmodifiableLinkedList() {
		return new StreamCollector<E, ConcurrentLinkedList<E>, ConcurrentUnmodifiableList<E>>(
			ConcurrentLinkedList::new,
			ConcurrentLinkedList::addAll,
			(left, right) -> { left.addAll(right); return left; },
			ConcurrentUnmodifiableList::new,
			ORDERED_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentList}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentList}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentList<E>> toList() {
		return new StreamCollector<>(ConcurrentList::new, ConcurrentList::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentUnmodifiableList}.
	 *
	 * @param <E> the element type
	 * @param <A> the result type (extends {@link ConcurrentList})
	 * @return a collector producing an unmodifiable concurrent list
	 */
	@SuppressWarnings("unchecked")
	public static <E, A extends ConcurrentList<E>> @NotNull Collector<E, ?, A> toUnmodifiableList() {
		return new StreamCollector<>(
			ConcurrentList::new,
			ConcurrentList::addAll,
			(left, right) -> { left.addAll(right); return left; },
			list -> (A) list.toUnmodifiableList(),
			ORDERED_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentMap}
	 * by casting each element to {@link Map.Entry}. Throws on duplicate keys.
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
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentMap}.
	 * Throws on duplicate keys.
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
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentMap},
	 * using the specified merge function for duplicate keys.
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
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentMap}
	 * using the given key and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toMap(keyMapper, valueMapper, throwingMerger(), ConcurrentMap::new);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentMap}
	 * using the given key mapper, value mapper, and merge function for duplicate keys.
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
		return toMap(keyMapper, valueMapper, mergeFunction, ConcurrentMap::new);
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
	 * @param <A>           the map type (extends {@link ConcurrentMap})
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
		return Collectors.collectingAndThen(
			toList(),
			list -> {
				if (list.isEmpty())
					throw new NoSuchElementException();

				if (list.size() >= 2)
					throw new IllegalStateException();

				return list.getFirst();
			}
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentLinkedMap}
	 * by casting each element to {@link Map.Entry}. Throws on duplicate keys.
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
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentLinkedMap}.
	 * Throws on duplicate keys.
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
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentLinkedMap},
	 * using the specified merge function for duplicate keys.
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
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentLinkedMap}
	 * using the given key and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentLinkedMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toLinkedMap(keyMapper, valueMapper, throwingMerger(), ConcurrentLinkedMap::new);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentLinkedMap}
	 * using the given key mapper, value mapper, and merge function for duplicate keys.
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
		return toLinkedMap(keyMapper, valueMapper, mergeFunction, ConcurrentLinkedMap::new);
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
	 * @param <A>           the map type (extends {@link ConcurrentLinkedMap})
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
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentUnmodifiableMap}
	 * by casting each element to {@link Map.Entry}. Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap}
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toWeakUnmodifiableMap() {
		return toUnmodifiableMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentUnmodifiableMap}.
	 * Throws on duplicate keys.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap() {
		return toUnmodifiableMap(throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a {@link ConcurrentUnmodifiableMap},
	 * using the specified merge function for duplicate keys.
	 *
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentUnmodifiableMap}
	 * using the given key and value mappers. Throws on duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param <K>         the key type
	 * @param <V>         the value type
	 * @param <T>         the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap}
	 */
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toUnmodifiableMap(keyMapper, valueMapper, throwingMerger());
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentUnmodifiableMap}
	 * using the given key mapper, value mapper, and merge function for duplicate keys.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap}
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
			UNORDERED_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a {@link ConcurrentUnmodifiableMap},
	 * using the given key mapper, value mapper, merge function, and map supplier.
	 * This is the most general {@code toUnmodifiableMap} overload.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param mapSupplier the supplier providing a new empty mutable map used during accumulation
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @param <A>           the intermediate map type (extends {@link ConcurrentMap})
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap}
	 */
	public static <K, V, T, A extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(
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
			Concurrent::newUnmodifiableMap,
			UNORDERED_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates {@link Map.Entry} stream elements into a
	 * {@link ConcurrentUnmodifiableMap} backed by a {@link ConcurrentSortedMap} ordered by the
	 * given comparator. Throws on duplicate keys.
	 *
	 * @param comparator the comparator used to order and compare the keys
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the stream element type (must extend {@link Map.Entry})
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap} ordered by {@code comparator}
	 */
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableSortedMap(@NotNull Comparator<? super K> comparator) {
		return toUnmodifiableSortedMap(
			Map.Entry::getKey,
			Map.Entry::getValue,
			throwingMerger(),
			() -> Concurrent.newSortedMap(comparator)
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a
	 * {@link ConcurrentUnmodifiableMap} backed by the supplied map (typically a
	 * {@link ConcurrentSortedMap}), applying the given key mapper, value mapper, and
	 * merge function.
	 *
	 * @param keyMapper the function to extract map keys from stream elements
	 * @param valueMapper the function to extract map values from stream elements
	 * @param mergeFunction the function to resolve collisions between values associated with the same key
	 * @param mapSupplier the supplier providing a new empty sorted map used during accumulation
	 * @param <K>           the key type
	 * @param <V>           the value type
	 * @param <T>           the stream element type
	 * @param <A>           the intermediate map type (extends {@link ConcurrentMap})
	 * @return a collector producing a {@link ConcurrentUnmodifiableMap}
	 */
	public static <K, V, T, A extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableSortedMap(
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
			Concurrent::newUnmodifiableMap,
			UNORDERED_CHARACTERISTICS
		);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentSet}.
	 *
	 * @param <E> the element type
	 * @return a collector producing a {@link ConcurrentSet}
	 */
	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toSet() {
		return new StreamCollector<>(ConcurrentSet::new, ConcurrentSet::addAll, (left, right) -> { left.addAll(right); return left; }, UNORDERED_CHARACTERISTICS);
	}

	/**
	 * Returns a {@link Collector} that accumulates stream elements into a new {@link ConcurrentUnmodifiableSet}.
	 *
	 * @param <E> the element type
	 * @param <A> the result type (extends {@link ConcurrentSet})
	 * @return a collector producing an unmodifiable concurrent set
	 */
	@SuppressWarnings("unchecked")
	public static <E, A extends ConcurrentSet<E>> @NotNull Collector<E, ?, A> toUnmodifiableSet() {
		return new StreamCollector<>(
			ConcurrentSet::new,
			ConcurrentSet::addAll,
			(left, right) -> { left.addAll(right); return left; },
			list -> (A) list.toUnmodifiableSet(),
			UNORDERED_CHARACTERISTICS
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

		/**
		 * Returns a casting identity function, used as the default finisher when no transformation is needed.
		 *
		 * @param <I> the input type
		 * @param <R> the result type
		 * @return an identity function that casts input to the result type
		 */
		@SuppressWarnings("unchecked")
		private static <I, R> Function<I, R> castingIdentity() {
			return i -> (R) i;
		}

		private final @NotNull Supplier<A> supplier;
		private final @NotNull BiConsumer<A, T> accumulator;
		private final @NotNull BinaryOperator<A> combiner;
		private @NotNull Function<A, R> finisher = castingIdentity();
		private final @NotNull Set<Characteristics> characteristics;

	}

}
