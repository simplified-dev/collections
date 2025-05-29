package dev.sbs.api.collection.concurrent;

import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedList;
import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedMap;
import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedSet;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableCollection;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableLinkedList;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableMap;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableSet;
import dev.sbs.api.stream.StreamUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Provides factory methods for creating concurrent collection and map implementations with optional initialization parameters.
 * <p>
 * This utility class contains various static methods to create thread-safe collections, queues, deques, lists, maps, sets,
 * and their linked and unmodifiable counterparts.
 * <p>
 * It also provides collectors to create concurrent collections and maps from {@link Stream Streams}.
 */
public final class Concurrent {

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
		return StreamUtil.toConcurrentCollection();
	}

	public static <E> @NotNull Collector<E, ?, ConcurrentLinkedList<E>> toLinkedList() {
		return StreamUtil.toConcurrentLinkedList();
	}

	public static <E> @NotNull Collector<E, ?, ConcurrentList<E>> toList() {
		return StreamUtil.toConcurrentList();
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap() {
		return StreamUtil.toConcurrentMap();
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(BinaryOperator<V> mergeFunction) {
		return StreamUtil.toConcurrentMap(mergeFunction);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return StreamUtil.toConcurrentMap(keyMapper, valueMapper);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper, BinaryOperator<V> mergeFunction) {
		return StreamUtil.toConcurrentMap(keyMapper, valueMapper, mergeFunction);
	}

	public static <K, V, T, R extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, R> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper, BinaryOperator<V> mergeFunction, Supplier<R> mapSupplier) {
		return StreamUtil.toConcurrentMap(keyMapper, valueMapper, mergeFunction, mapSupplier);
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap() {
		return StreamUtil.toConcurrentLinkedMap();
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(BinaryOperator<V> mergeFunction) {
		return StreamUtil.toConcurrentLinkedMap(mergeFunction);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return StreamUtil.toConcurrentLinkedMap(keyMapper, valueMapper);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toLinkedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper, BinaryOperator<V> mergeFunction) {
		return StreamUtil.toConcurrentLinkedMap(keyMapper, valueMapper, mergeFunction);
	}

	public static <K, V, T, R extends ConcurrentLinkedMap<K, V>> @NotNull Collector<T, ?, R> toLinkedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper, BinaryOperator<V> mergeFunction, Supplier<R> mapSupplier) {
		return StreamUtil.toConcurrentLinkedMap(keyMapper, valueMapper, mergeFunction, mapSupplier);
	}

	public static <E, A extends ConcurrentList<E>> @NotNull Collector<E, ?, A> toUnmodifiableList() {
		return StreamUtil.toConcurrentUnmodifiableList();
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap() {
		return StreamUtil.toConcurrentUnmodifiableMap();
	}

	public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(BinaryOperator<V> mergeFunction) {
		return StreamUtil.toConcurrentUnmodifiableMap(mergeFunction);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return StreamUtil.toConcurrentUnmodifiableMap(keyMapper, valueMapper);
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper, BinaryOperator<V> mergeFunction) {
		return StreamUtil.toConcurrentUnmodifiableMap(keyMapper, valueMapper, mergeFunction);
	}

	public static <K, V, T, R extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toUnmodifiableMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper, BinaryOperator<V> mergeFunction, Supplier<R> mapSupplier) {
		return StreamUtil.toConcurrentUnmodifiableMap(keyMapper, valueMapper, mergeFunction, mapSupplier);
	}

	public static <E, A extends ConcurrentSet<E>> @NotNull Collector<E, ?, A> toUnmodifiableSet() {
		return StreamUtil.toConcurrentUnmodifiableSet();
	}

	public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toSet() {
		return StreamUtil.toConcurrentSet();
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toWeakMap() {
		return StreamUtil.toWeakConcurrentMap();
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toWeakLinkedMap() {
		return StreamUtil.toWeakConcurrentLinkedMap();
	}

	public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toWeakUnmodifiableMap() {
		return StreamUtil.toWeakConcurrentUnmodifiableMap();
	}

}
