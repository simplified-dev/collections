package dev.sbs.api.collection.concurrent;

import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedList;
import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedMap;
import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedSet;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableCollection;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableLinkedList;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableMap;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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
 * This utility class provides factory methods for creating thread-safe
 * concurrent collection, queue, set, list, and map implementations with
 * linked and unmodifiable counterparts.
 * <p>
 * It also provides concurrect collectors for {@link Stream#collect}.
 */
@UtilityClass
public final class Concurrent {

	public static final ConcurrentSet<Collector.Characteristics> ORDERED_CHARACTERISTICS = Concurrent.newSet(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH);
	public static final ConcurrentSet<Collector.Characteristics> UNORDERED_CHARACTERISTICS = Concurrent.newSet(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH, Collector.Characteristics.UNORDERED);

	private static <T> @NotNull BinaryOperator<T> throwingMerger() {
		return (key, value) -> { throw new IllegalStateException(String.format("Duplicate key %s!", key)); };
	}

	public static <E> @NotNull ConcurrentCollection<E> newCollection() {
		return new ConcurrentCollection<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentCollection<E> newCollection(@NotNull E... array) {
		return new ConcurrentCollection<>(array);
	}

	public static <E> @NotNull ConcurrentCollection<E> newCollection(@NotNull Collection<? extends E> collection) {
		return new ConcurrentCollection<>(collection);
	}

	public static <E> @NotNull ConcurrentDeque<E> newDeque() {
		return new ConcurrentDeque<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentDeque<E> newDeque(@NotNull E... array) {
		return new ConcurrentDeque<>(array);
	}

	public static <E> @NotNull ConcurrentDeque<E> newDeque(@NotNull Collection<? extends E> collection) {
		return new ConcurrentDeque<>(collection);
	}

	public static <E> @NotNull ConcurrentList<E> newList() {
		return new ConcurrentList<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentList<E> newList(@NotNull E... array) {
		return new ConcurrentList<>(array);
	}

	public static <E> @NotNull ConcurrentList<E> newList(@NotNull Collection<? extends E> collection) {
		return new ConcurrentList<>(collection);
	}

	@SafeVarargs
	public static <K, V> @NotNull ConcurrentMap<K, V> newMap(@NotNull Map.Entry<K, V>... entries) {
		return new ConcurrentMap<>(entries);
	}

	public static <K, V> @NotNull ConcurrentMap<K, V> newMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentMap<>(map);
	}

	public static <E> @NotNull ConcurrentQueue<E> newQueue() {
		return new ConcurrentQueue<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentQueue<E> newQueue(@NotNull E... array) {
		return new ConcurrentQueue<>(array);
	}

	public static <E> @NotNull ConcurrentQueue<E> newQueue(@NotNull Collection<? extends E> collection) {
		return new ConcurrentQueue<>(collection);
	}

	public static <E> @NotNull ConcurrentSet<E> newSet() {
		return new ConcurrentSet<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentSet<E> newSet(@NotNull E... array) {
		return new ConcurrentSet<>(array);
	}

	public static <E> @NotNull ConcurrentSet<E> newSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentSet<>(collection);
	}

	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList() {
		return new ConcurrentLinkedList<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList(@NotNull E... array) {
		return new ConcurrentLinkedList<>(array);
	}

	public static <E> @NotNull ConcurrentLinkedList<E> newLinkedList(@NotNull Collection<? extends E> collection) {
		return new ConcurrentLinkedList<>(collection);
	}

	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap() {
		return newLinkedMap(-1);
	}

	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap(int maxSize) {
		return new ConcurrentLinkedMap<>(maxSize);
	}

	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentLinkedMap<>(map);
	}

	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> newLinkedMap(@NotNull Map<? extends K, ? extends V> map, int maxSize) {
		return new ConcurrentLinkedMap<>(map, maxSize);
	}

	public static <E> @NotNull ConcurrentLinkedSet<E> newLinkedSet() {
		return new ConcurrentLinkedSet<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentLinkedSet<E> newLinkedSet(@NotNull E... array) {
		return new ConcurrentLinkedSet<>(array);
	}

	public static <E> @NotNull ConcurrentLinkedSet<E> newLinkedSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentLinkedSet<>(collection);
	}

	public static <E> @NotNull ConcurrentUnmodifiableCollection<E> newUnmodifiableCollection() {
		return new ConcurrentUnmodifiableCollection<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableCollection<E> newUnmodifiableCollection(@NotNull E... array) {
		return new ConcurrentUnmodifiableCollection<>(array);
	}

	public static <E> @NotNull ConcurrentUnmodifiableCollection<E> newUnmodifiableCollection(@NotNull Collection<? extends E> collection) {
		return new ConcurrentUnmodifiableCollection<>(collection);
	}

	public static <E> @NotNull ConcurrentUnmodifiableLinkedList<E> newUnmodifiableLinkedList() {
		return new ConcurrentUnmodifiableLinkedList<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableLinkedList<E> newUnmodifiableLinkedList(@NotNull E... array) {
		return new ConcurrentUnmodifiableLinkedList<>(array);
	}

	public static <E> @NotNull ConcurrentUnmodifiableLinkedList<E> newUnmodifiableLinkedList(@NotNull Collection<? extends E> collection) {
		return new ConcurrentUnmodifiableLinkedList<>(collection);
	}

	public static <E> @NotNull ConcurrentUnmodifiableList<E> newUnmodifiableList() {
		return new ConcurrentUnmodifiableList<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableList<E> newUnmodifiableList(@NotNull E... array) {
		return new ConcurrentUnmodifiableList<>(array);
	}

	public static <E> @NotNull ConcurrentUnmodifiableList<E> newUnmodifiableList(@NotNull Collection<? extends E> collection) {
		return new ConcurrentUnmodifiableList<>(collection);
	}

	@SafeVarargs
	public static <K, V> @NotNull ConcurrentUnmodifiableMap<K, V> newUnmodifiableMap(@NotNull Map.Entry<K, V>... entries) {
		return new ConcurrentUnmodifiableMap<>(entries);
	}

	public static <K, V> @NotNull ConcurrentUnmodifiableMap<K, V> newUnmodifiableMap(@NotNull Map<? extends K, ? extends V> map) {
		return new ConcurrentUnmodifiableMap<>(map);
	}

	public static <E> @NotNull ConcurrentUnmodifiableSet<E> newUnmodifiableSet() {
		return new ConcurrentUnmodifiableSet<>();
	}

	@SafeVarargs
	public static <E> @NotNull ConcurrentUnmodifiableSet<E> newUnmodifiableSet(@NotNull E... array) {
		return new ConcurrentUnmodifiableSet<>(array);
	}

	public static <E> @NotNull ConcurrentUnmodifiableSet<E> newUnmodifiableSet(@NotNull Collection<? extends E> collection) {
		return new ConcurrentUnmodifiableSet<>(collection);
	}

	public static <E> @NotNull Collector<E, ?, ConcurrentCollection<E>> toCollection() {
		return new StreamCollector<>(ConcurrentCollection::new, ConcurrentCollection::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	public static <E> @NotNull Collector<E, ?, ConcurrentLinkedList<E>> toLinkedList() {
		return new StreamCollector<>(ConcurrentLinkedList::new, ConcurrentLinkedList::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

	@SuppressWarnings("unchecked")
	public static <E, A extends ConcurrentLinkedList<E>> @NotNull Collector<E, ?, A> toUnmodifiableLinkedList() {
		return new StreamCollector<>(
			ConcurrentLinkedList::new,
			ConcurrentLinkedList::addAll,
			(left, right) -> { left.addAll(right); return left; },
			list -> (A) list.toUnmodifiableLinkedList(),
			ORDERED_CHARACTERISTICS
		);
	}

	public static <E> @NotNull Collector<E, ?, ConcurrentList<E>> toList() {
		return new StreamCollector<>(ConcurrentList::new, ConcurrentList::addAll, (left, right) -> { left.addAll(right); return left; }, ORDERED_CHARACTERISTICS);
	}

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

	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toWeakMap() {
		return toMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap() {
		return toMap(throwingMerger());
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toMap(keyMapper, valueMapper, throwingMerger(), ConcurrentMap::new);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toMap(keyMapper, valueMapper, mergeFunction, ConcurrentMap::new);
	}

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

				return list.get(0);
			}
		);
	}

	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toWeakLinkedMap() {
		return toLinkedMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap() {
		return toLinkedMap(throwingMerger());
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toLinkedMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toLinkedMap(keyMapper, valueMapper, throwingMerger(), ConcurrentLinkedMap::new);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toLinkedMap(keyMapper, valueMapper, mergeFunction, ConcurrentLinkedMap::new);
	}

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

	@SuppressWarnings("unchecked")
	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toWeakUnmodifiableMap() {
		return toUnmodifiableMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
	}
	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap() {
		return toUnmodifiableMap(throwingMerger());
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(@NotNull BinaryOperator<V> mergeFunction) {
		return toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
		return toUnmodifiableMap(keyMapper, valueMapper, throwingMerger(), ConcurrentMap::new);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
		return toUnmodifiableMap(keyMapper, valueMapper, mergeFunction, ConcurrentMap::new);
	}

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

	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toSet() {
		return new StreamCollector<>(ConcurrentSet::new, ConcurrentSet::addAll, (left, right) -> { left.addAll(right); return left; }, UNORDERED_CHARACTERISTICS);
	}

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

	public static <E> @NotNull Collector<E, ?, StringBuilder> toStringBuilder() {
		return toStringBuilder(true);
	}

	@SuppressWarnings("all")
	public static <E> @NotNull Collector<E, ?, StringBuilder> toStringBuilder(boolean newLine) {
		return new StreamCollector<E, StringBuilder, StringBuilder>(
			StringBuilder::new,
			newLine ? (builder, element) -> builder.append(element).append(System.lineSeparator()) : StringBuilder::append,
			(left, right) -> {
				if (newLine)
					left.append(right).append(System.lineSeparator());
				else
					left.append(right);

				return left;
			},
			ORDERED_CHARACTERISTICS
		);
	}

	public static <E> @NotNull Collector<E, ?, StringBuilder> toStringBuilder(@NotNull String separator) {
		return new StreamCollector<>(
			StringBuilder::new,
			(builder, element) -> builder.append(element.toString()).append(separator),
			(left, right) -> left.append(right.toString()),
			ORDERED_CHARACTERISTICS
		);
	}

	@Getter
	@Accessors(fluent = true)
	@RequiredArgsConstructor
	@AllArgsConstructor
	private static class StreamCollector<T, A, R> implements Collector<T, A, R> {

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
