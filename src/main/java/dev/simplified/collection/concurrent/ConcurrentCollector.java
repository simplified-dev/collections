package dev.sbs.api.collection.concurrent;

import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedList;
import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedMap;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@UtilityClass
public final class ConcurrentCollector {

    public static final ConcurrentSet<Collector.Characteristics> CHARACTERISTICS = Concurrent.newSet(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH);
    public static final ConcurrentSet<Collector.Characteristics> UN_CHARACTERISTICS = Concurrent.newSet(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH, Collector.Characteristics.UNORDERED);

    private static <T> @NotNull BinaryOperator<T> throwingMerger() {
        return (key, value) -> { throw new IllegalStateException(String.format("Duplicate key %s!", key)); };
    }

    public static <E> @NotNull Collector<E, ?, ConcurrentCollection<E>> toConcurrentCollection() {
        return new StreamCollector<>(ConcurrentCollection::new, ConcurrentCollection::addAll, (left, right) -> { left.addAll(right); return left; }, CHARACTERISTICS);
    }

    public static <E> @NotNull Collector<E, ?, ConcurrentLinkedList<E>> toConcurrentLinkedList() {
        return new StreamCollector<>(ConcurrentLinkedList::new, ConcurrentLinkedList::addAll, (left, right) -> { left.addAll(right); return left; }, CHARACTERISTICS);
    }

    @SuppressWarnings("unchecked")
    public static <E, A extends ConcurrentLinkedList<E>> @NotNull Collector<E, ?, A> toConcurrentUnmodifiableLinkedList() {
        return new StreamCollector<>(
            ConcurrentLinkedList::new,
            ConcurrentLinkedList::addAll,
            (left, right) -> { left.addAll(right); return left; },
            list -> (A) list.toUnmodifiableLinkedList(),
            CHARACTERISTICS
        );
    }

    public static <E> @NotNull Collector<E, ?, ConcurrentList<E>> toConcurrentList() {
        return new StreamCollector<>(ConcurrentList::new, ConcurrentList::addAll, (left, right) -> { left.addAll(right); return left; }, CHARACTERISTICS);
    }

    @SuppressWarnings("unchecked")
    public static <E, A extends ConcurrentList<E>> @NotNull Collector<E, ?, A> toConcurrentUnmodifiableList() {
        return new StreamCollector<>(
            ConcurrentList::new,
            ConcurrentList::addAll,
            (left, right) -> { left.addAll(right); return left; },
            list -> (A) list.toUnmodifiableList(),
            CHARACTERISTICS
        );
    }

    @SuppressWarnings("unchecked")
    public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toWeakConcurrentMap() {
        return toConcurrentMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
    }

    public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toConcurrentMap() {
        return toConcurrentMap(throwingMerger());
    }

    public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toConcurrentMap(@NotNull BinaryOperator<V> mergeFunction) {
        return toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
    }

    public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toConcurrentMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
        return toConcurrentMap(keyMapper, valueMapper, throwingMerger(), ConcurrentMap::new);
    }

    public static <K, V, T> @NotNull Collector<T, ?, ConcurrentMap<K, V>> toConcurrentMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
        return toConcurrentMap(keyMapper, valueMapper, mergeFunction, ConcurrentMap::new);
    }

    public static <K, V, T, A extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, A> toConcurrentMap(
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
            UN_CHARACTERISTICS
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
            toConcurrentList(),
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
    public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toWeakConcurrentLinkedMap() {
        return toConcurrentLinkedMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
    }

    public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toConcurrentLinkedMap() {
        return toConcurrentLinkedMap(throwingMerger());
    }

    public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toConcurrentLinkedMap(@NotNull BinaryOperator<V> mergeFunction) {
        return toConcurrentLinkedMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
    }

    public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toConcurrentLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
        return toConcurrentLinkedMap(keyMapper, valueMapper, throwingMerger(), ConcurrentLinkedMap::new);
    }

    public static <K, V, T> @NotNull Collector<T, ?, ConcurrentLinkedMap<K, V>> toConcurrentLinkedMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
        return toConcurrentLinkedMap(keyMapper, valueMapper, mergeFunction, ConcurrentLinkedMap::new);
    }

    public static <K, V, T, A extends ConcurrentLinkedMap<K, V>> @NotNull Collector<T, ?, A> toConcurrentLinkedMap(
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
            CHARACTERISTICS
        );
    }

    @SuppressWarnings("unchecked")
    public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toWeakConcurrentUnmodifiableMap() {
        return toConcurrentUnmodifiableMap(entry -> ((Map.Entry<K, V>) entry).getKey(), entry -> ((Map.Entry<K, V>) entry).getValue(), throwingMerger());
    }
    public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toConcurrentUnmodifiableMap() {
        return toConcurrentUnmodifiableMap(throwingMerger());
    }

    public static <K, V, T extends Map.Entry<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toConcurrentUnmodifiableMap(@NotNull BinaryOperator<V> mergeFunction) {
        return toConcurrentUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
    }

    public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toConcurrentUnmodifiableMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper) {
        return toConcurrentUnmodifiableMap(keyMapper, valueMapper, throwingMerger(), ConcurrentMap::new);
    }

    public static <K, V, T> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toConcurrentUnmodifiableMap(@NotNull Function<? super T, ? extends K> keyMapper, @NotNull Function<? super T, ? extends V> valueMapper, @NotNull BinaryOperator<V> mergeFunction) {
        return toConcurrentUnmodifiableMap(keyMapper, valueMapper, mergeFunction, ConcurrentMap::new);
    }

    public static <K, V, T, A extends ConcurrentMap<K, V>> @NotNull Collector<T, ?, ConcurrentUnmodifiableMap<K, V>> toConcurrentUnmodifiableMap(
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
            UN_CHARACTERISTICS
        );
    }

    public static <E> @NotNull Collector<E, ?, ConcurrentSet<E>> toConcurrentSet() {
        return new StreamCollector<>(ConcurrentSet::new, ConcurrentSet::addAll, (left, right) -> { left.addAll(right); return left; }, UN_CHARACTERISTICS);
    }

    @SuppressWarnings("unchecked")
    public static <E, A extends ConcurrentSet<E>> @NotNull Collector<E, ?, A> toConcurrentUnmodifiableSet() {
        return new StreamCollector<>(
            ConcurrentSet::new,
            ConcurrentSet::addAll,
            (left, right) -> { left.addAll(right); return left; },
            list -> (A) list.toUnmodifiableSet(),
            UN_CHARACTERISTICS
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
            CHARACTERISTICS
        );
    }

    public static <E> @NotNull Collector<E, ?, StringBuilder> toStringBuilder(@NotNull String separator) {
        return new StreamCollector<>(
            StringBuilder::new,
            (builder, element) -> builder.append(element.toString()).append(separator),
            (left, right) -> left.append(right.toString()),
            CHARACTERISTICS
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
