package dev.sbs.api.collection.stream;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentCollection;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.api.collection.concurrent.ConcurrentSet;
import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedList;
import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedMap;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableMap;
import dev.sbs.api.collection.stream.triple.TriFunction;
import dev.sbs.api.collection.stream.triple.TripleStream;
import dev.sbs.api.mutable.triple.Triple;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamUtil {

    public static final ConcurrentSet<Collector.Characteristics> CHARACTERISTICS = Concurrent.newSet(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH);
    public static final ConcurrentSet<Collector.Characteristics> UN_CHARACTERISTICS = Concurrent.newSet(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH, Collector.Characteristics.UNORDERED);

    private static <T> @NotNull BinaryOperator<T> throwingMerger() {
        return (key, value) -> { throw new IllegalStateException(String.format("Duplicate key %s!", key)); };
    }

    public static <T> @NotNull Predicate<T> distinctByKey(@NotNull Function<? super T, ?> keyExtractor) {
        ConcurrentSet<Object> seen = Concurrent.newSet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static <T> @NotNull Stream<T> ofArrays(@Nullable T[]... arrays) {
        Stream<T> stream = Stream.empty();

        for (T[] array : arrays)
            stream = Stream.concat(stream, (array == null ? Stream.empty() : Arrays.stream(array)));

        return stream;
    }

    /**
     * Zips the specified stream with its indices.
     */
    public static <T> @NotNull TripleStream<T, Long, Long> zipWithIndex(@NotNull Stream<T> stream) {
        return zipWithIndex(stream.spliterator(), stream.isParallel());
    }

    /**
     * Zips the specified spliterator with its indices.
     */
    public static <T> @NotNull TripleStream<T, Long, Long> zipWithIndex(@NotNull Spliterator<T> spliterator, boolean parallel) {
        return TripleStream.of(mapWithIndex(spliterator, parallel, Triple::of));
    }

    public static <T> @NotNull Stream<Triple<T, Long, Long>> indexedStream(@NotNull Stream<T> stream) {
        return indexedStream(stream.spliterator(), stream.isParallel()).onClose(stream::close);
    }

    public static <T> @NotNull Stream<Triple<T, Long, Long>> indexedStream(@NotNull Spliterator<T> spliterator, boolean parallel) {
        return mapWithIndex(spliterator, parallel, Triple::of);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> @NotNull Stream<R> mapWithIndex(@NotNull Stream<T> stream, @NotNull TriFunction<? super T, Long, Long, ? extends R> function) {
        return (Stream<R>) mapWithIndex(stream.spliterator(), stream.isParallel(), function).onClose(stream::close);
    }

    /**
     * Returns a stream consisting of the results of applying the given function to the elements of
     * {@code stream} and their indices in the stream. For example,
     *
     * <pre>{@code
     * mapWithIndex(
     *     Stream.of("a", "b", "c"),
     *     (str, index, size) -> str + ":" + index + "/" + size)
     * }</pre>
     *
     * <p>would return {@code Stream.of("a:0/3", "b:1/3", "c:2/3")}.
     *
     * <p>The resulting stream is <a
     * href="http://gee.cs.oswego.edu/dl/html/StreamParallelGuidance.html">efficiently splittable</a>
     * if and only if {@code stream} was efficiently splittable and its underlying spliterator
     * reported {@link Spliterator#SUBSIZED}. This is generally the case if the underlying stream
     * comes from a data structure supporting efficient indexed random access, typically an array or
     * list.
     *
     * <p>The order of the resulting stream is defined if and only if the order of the original stream
     * was defined.
     */
    public static <T, R> @NotNull Stream<R> mapWithIndex(@NotNull Spliterator<T> spliterator, boolean parallel, @NotNull TriFunction<? super T, Long, Long, ? extends R> function) {
        long size = spliterator.estimateSize();

        if (!spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<>(size, spliterator.characteristics() & (Spliterator.ORDERED | Spliterator.SIZED)) {
                    private Iterator<T> fromIterator = Spliterators.iterator(spliterator);
                    private long index = 0;

                    @Override
                    public boolean tryAdvance(Consumer<? super R> action) {
                        if (this.fromIterator.hasNext()) {
                            action.accept(function.apply(this.fromIterator.next(), this.index++, size));
                            return true;
                        }

                        return false;
                    }
                },
                parallel
            );
        } else {
            class Splitr extends MapWithIndexSpliterator<Spliterator<T>, R, Splitr> implements Consumer<T> {

                private @Nullable T holder;

                Splitr(Spliterator<T> splitr, long index) {
                    super(splitr, index);
                }

                @Override
                public void accept(@NotNull T t) {
                    this.holder = t;
                }

                @Override
                public boolean tryAdvance(Consumer<? super R> action) {
                    if (fromSpliterator.tryAdvance(this)) {
                        try {
                            // The cast is safe because tryAdvance puts a T into `holder`.
                            action.accept(function.apply(this.holder, this.index++, size));
                            return true;
                        } finally {
                            holder = null;
                        }
                    }
                    return false;
                }

                @Override
                Splitr createSplit(Spliterator<T> from, long i) {
                    return new Splitr(from, i);
                }

            }

            return StreamSupport.stream(new Splitr(spliterator, 0), parallel);
        }
    }

    public static <T> @NotNull Stream<T> modifyStream(@NotNull Stream<T> stream, @NotNull TriFunction<T, Long, Long, T> modFunction) {
        return mapWithIndex(stream, modFunction);
    }

    public static @NotNull Stream<String> appendEach(@NotNull Stream<String> stringStream, @NotNull String entryValue) {
        return appendEach(stringStream, entryValue, entryValue);
    }

    public static @NotNull Stream<String> appendEach(@NotNull Stream<String> stringStream, @NotNull String entryValue, @NotNull String lastEntry) {
        return modifyStream(stringStream, (value, index, size) -> value + (index < size - 1 ? entryValue : lastEntry));
    }

    public static @NotNull Stream<String> prependEach(@NotNull Stream<String> stringStream, @NotNull String entryValue) {
        return prependEach(stringStream, entryValue, entryValue);
    }

    public static @NotNull Stream<String> prependEach(@NotNull Stream<String> stringStream, @NotNull String entryValue, @NotNull String lastEntry) {
        return modifyStream(stringStream, (value, index, size) -> (index < size - 1 ? entryValue : lastEntry) + value);
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
        BiConsumer<A, T> accumulator = (map, element) -> map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction);
        BinaryOperator<A> combiner = (m1, m2) -> { m2.forEach((key, value) -> m1.merge(key, value, mergeFunction)); return m1; };

        return new StreamCollector<>(
            mapSupplier,
            accumulator,
            combiner,
            UN_CHARACTERISTICS
        );
    }

    /**
     * Collect a stream into a single item, only if there is 1 item expected.
     *
     * @return Singleton object from stream.
     * @throws IllegalStateException If result size is not equal to 1.
     */
    public static <T> @NotNull Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
            toConcurrentList(),
            list -> {
                if (list.size() != 1)
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
        BiConsumer<A, T> accumulator = (map, element) -> map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction);
        BinaryOperator<A> combiner = (m1, m2) -> { m2.forEach((key, value) -> m1.merge(key, value, mergeFunction)); return m1; };

        return new StreamCollector<>(
            mapSupplier,
            accumulator,
            combiner,
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

    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    private static abstract class MapWithIndexSpliterator<F extends Spliterator<?>, R, S extends MapWithIndexSpliterator<F, R, S>> implements Spliterator<R> {

        protected final F fromSpliterator;
        protected long index;

        abstract S createSplit(F from, long i);

        @Override
        @SuppressWarnings("unchecked")
        public S trySplit() {
            Spliterator<?> splitOrNull = this.fromSpliterator.trySplit();

            if (splitOrNull == null) {
                return null;
            }

            F split = (F) splitOrNull;
            S result = createSplit(split, index);
            this.index += split.getExactSizeIfKnown();
            return result;
        }

        @Override
        public long estimateSize() {
            return this.fromSpliterator.estimateSize();
        }

        @Override
        public int characteristics() {
            return this.fromSpliterator.characteristics() & (Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);
        }

    }

}
