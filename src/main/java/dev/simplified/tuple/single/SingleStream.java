package dev.simplified.tuple.single;

import dev.simplified.collection.concurrent.Concurrent;
import dev.simplified.collection.concurrent.ConcurrentList;
import dev.simplified.tuple.pair.Pair;
import dev.simplified.tuple.pair.PairStream;
import dev.simplified.tuple.triple.Triple;
import dev.simplified.tuple.triple.TripleStream;
import dev.simplified.util.StreamUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A specialized stream interface that operates on individual elements.
 * <p>
 * Provides conversion to {@link PairStream} and {@link TripleStream}, collection
 * flatmap shortcuts, and full compatibility with standard {@link Stream} operations
 * while preserving the {@code SingleStream} type through intermediate operations.
 * <p>
 * {@code SingleStream} is constructed from an existing {@link Stream} or {@link Collection}
 * and is the base of the tuple stream hierarchy, sitting below {@link PairStream} (2 elements)
 * and {@link TripleStream} (3 elements).
 *
 * @param <E> the type of elements in the stream
 */
@FunctionalInterface
public interface SingleStream<E> extends Stream<E> {

    // Create

    /**
     * Returns an empty {@code SingleStream}.
     *
     * @param <E> the element type
     * @return an empty {@code SingleStream}
     */
    static <E> @NotNull SingleStream<E> empty() {
        return of(Stream.empty());
    }

    /**
     * Creates a {@code SingleStream} wrapping the given {@link Stream}.
     *
     * @param <E>    the element type
     * @param stream the stream to wrap
     * @return a {@code SingleStream} backed by {@code stream}
     */
    static <E> @NotNull SingleStream<E> of(@NotNull Stream<E> stream) {
        return () -> stream;
    }

    /**
     * Creates a {@code SingleStream} from the given {@link Collection}.
     *
     * @param <E>        the element type
     * @param collection the collection to stream
     * @return a {@code SingleStream} backed by the collection's stream
     */
    static <E> @NotNull SingleStream<E> of(@NotNull Collection<E> collection) {
        return of(collection.stream());
    }

    /**
     * Combines two streams into a single stream by concatenating their elements in order.
     *
     * @param <T> the type of elements in the streams
     * @param a the first stream to concatenate
     * @param b the second stream to concatenate
     * @return a new stream containing all elements from the first stream,
     *         followed by all elements from the second stream
     */
    static <T> @NotNull SingleStream<T> concat(@NotNull Stream<? extends T> a, @NotNull Stream<? extends T> b) {
        return of(Stream.concat(a, b));
    }

    // Close

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("all")
    default @NotNull SingleStream<E> onClose(@NotNull Runnable closeHandler) {
        return of(this.underlying().onClose(closeHandler));
    }

    /** {@inheritDoc} */
    @Override
    default void close() {
        this.underlying().close();
    }

    // Entries

    /**
     * Returns the underlying {@link Stream} that backs this {@code SingleStream}.
     *
     * @return the underlying stream
     */
    @NotNull Stream<E> underlying();

    /** {@inheritDoc} */
    @Override
    default long count() {
        return this.underlying().count();
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> distinct() {
        return of(this.underlying().distinct());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> limit(long maxSize) {
        return of(this.underlying().limit(maxSize));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> peek(@NotNull Consumer<? super E> action) {
        return of(this.underlying().peek(action));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> skip(long number) {
        return of(this.underlying().skip(number));
    }

    // Filtering

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> filter(@NotNull Predicate<? super E> predicate) {
        return of(this.underlying().filter(predicate));
    }

    // Find

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<E> findAny() {
        return this.underlying().findAny();
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<E> findFirst() {
        return this.underlying().findFirst();
    }

    // Flatmapping

    /** {@inheritDoc} */
    @Override
    default <R> @NotNull SingleStream<R> flatMap(@NotNull Function<? super E, ? extends Stream<? extends R>> mapper) {
        return of(this.underlying().flatMap(mapper));
    }

    /**
     * Returns a {@code SingleStream} by replacing each element with the contents of
     * the {@link Collection} returned by {@code mapper}, avoiding a manual
     * {@code .flatMap(c -> c.stream())} call.
     *
     * @param <R>    the element type of the new stream
     * @param mapper a function returning a collection of new values for each element
     * @return a flat-mapped {@code SingleStream}
     */
    default <R> @NotNull SingleStream<R> flatMapMany(@NotNull Function<? super E, ? extends Collection<? extends R>> mapper) {
        return of(this.underlying().flatMap(e -> mapper.apply(e).stream()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull DoubleStream flatMapToDouble(@NotNull Function<? super E, ? extends DoubleStream> mapper) {
        return this.underlying().flatMapToDouble(mapper);
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull IntStream flatMapToInt(@NotNull Function<? super E, ? extends IntStream> mapper) {
        return this.underlying().flatMapToInt(mapper);
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull LongStream flatMapToLong(@NotNull Function<? super E, ? extends LongStream> mapper) {
        return this.underlying().flatMapToLong(mapper);
    }

    // ForEach

    /** {@inheritDoc} */
    @Override
    default void forEach(@NotNull Consumer<? super E> action) {
        this.underlying().forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    default void forEachOrdered(@NotNull Consumer<? super E> action) {
        this.underlying().forEachOrdered(action);
    }

    // Iterator

    /** {@inheritDoc} */
    @Override
    default @NotNull Iterator<E> iterator() {
        return this.underlying().iterator();
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Spliterator<E> spliterator() {
        return this.underlying().spliterator();
    }

    // Mapping

    /** {@inheritDoc} */
    @Override
    default <R> @NotNull SingleStream<R> map(@NotNull Function<? super E, ? extends R> mapper) {
        return of(this.underlying().map(mapper));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleFunction<? super E> mapper) {
        return this.underlying().mapToDouble(mapper);
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull IntStream mapToInt(@NotNull ToIntFunction<? super E> mapper) {
        return this.underlying().mapToInt(mapper);
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull LongStream mapToLong(@NotNull ToLongFunction<? super E> mapper) {
        return this.underlying().mapToLong(mapper);
    }

    // Matching

    /** {@inheritDoc} */
    @Override
    default boolean allMatch(@NotNull Predicate<? super E> predicate) {
        return this.underlying().allMatch(predicate);
    }

    /** {@inheritDoc} */
    @Override
    default boolean anyMatch(@NotNull Predicate<? super E> predicate) {
        return this.underlying().anyMatch(predicate);
    }

    /** {@inheritDoc} */
    @Override
    default boolean noneMatch(@NotNull Predicate<? super E> predicate) {
        return this.underlying().noneMatch(predicate);
    }

    // Minmax

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<E> max(@NotNull Comparator<? super E> comparator) {
        return this.underlying().max(comparator);
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<E> min(@NotNull Comparator<? super E> comparator) {
        return this.underlying().min(comparator);
    }

    // Order

    /** {@inheritDoc} */
    @Override
    default boolean isParallel() {
        return this.underlying().isParallel();
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> parallel() {
        return of(this.underlying().parallel());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> sequential() {
        return of(this.underlying().sequential());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> unordered() {
        return of(this.underlying().unordered());
    }

    // Reduction

    /** {@inheritDoc} */
    @Override
    default @NotNull E reduce(@NotNull E identity, @NotNull BinaryOperator<E> accumulator) {
        return this.underlying().reduce(identity, accumulator);
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<E> reduce(@NotNull BinaryOperator<E> accumulator) {
        return this.underlying().reduce(accumulator);
    }

    /** {@inheritDoc} */
    @Override
    default <U> @NotNull U reduce(@NotNull U identity, @NotNull BiFunction<U, ? super E, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        return this.underlying().reduce(identity, accumulator, combiner);
    }

    // Sorting

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> sorted() {
        return of(this.underlying().sorted());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull SingleStream<E> sorted(@NotNull Comparator<? super E> comparator) {
        return of(this.underlying().sorted(comparator));
    }

    // Collect

    /** {@inheritDoc} */
    @Override
    default <R, A> R collect(@NotNull Collector<? super E, A, R> collector) {
        return this.underlying().collect(collector);
    }

    /** {@inheritDoc} */
    @Override
    default <R> R collect(@NotNull Supplier<R> supplier, @NotNull BiConsumer<R, ? super E> accumulator, @NotNull BiConsumer<R, R> combiner) {
        return this.underlying().collect(supplier, accumulator, combiner);
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Object @NotNull [] toArray() {
        return this.underlying().toArray();
    }

    /** {@inheritDoc} */
    @Override
    default <A> @NotNull A @NotNull [] toArray(@NotNull IntFunction<A[]> generator) {
        return this.underlying().toArray(generator);
    }

    /**
     * Accumulates the elements of this stream into a {@link ConcurrentList}.
     *
     * @return a {@code ConcurrentList} containing the stream elements
     */
    @Override
    default @NotNull ConcurrentList<E> toList() {
        return this.collect(Concurrent.toList());
    }

    /**
     * Accumulates the elements of this stream into a linked {@link ConcurrentList}.
     *
     * @return a linked {@code ConcurrentList} containing the stream elements
     */
    default @NotNull ConcurrentList<E> toLinkedList() {
        return this.collect(Concurrent.toLinkedList());
    }

    /**
     * Accumulates the elements of this stream into an unmodifiable {@link ConcurrentList}.
     *
     * @return an unmodifiable {@code ConcurrentList} containing the stream elements
     */
    default @NotNull ConcurrentList<E> toUnmodifiableList() {
        return this.collect(Concurrent.toUnmodifiableList());
    }

    // Expand

    /**
     * Expands each element into a {@link Pair}, using the element itself as the key and
     * the result of {@code valueMapper} as the value.
     *
     * @param <K>         the type of the pair key
     * @param <V>         the type of the pair value
     * @param entryMapper a function producing the entry from each element
     * @return a {@link PairStream} of {@code (element, mappedValue)} pairs
     */
    default <K, V> @NotNull PairStream<K, V> expandToPair(@NotNull Function<? super E, ? extends Map.Entry<K, V>> entryMapper) {
        return PairStream.of(this.underlying().map(entryMapper));
    }

    /**
     * Expands each element into a {@link Pair} by independently mapping to a key and a value.
     *
     * @param <K>         the type of the pair key
     * @param <V>         the type of the pair value
     * @param keyMapper a function producing the key from each element
     * @param valueMapper a function producing the value from each element
     * @return a {@link PairStream} of mapped key-value pairs
     */
    default <K, V> @NotNull PairStream<K, V> expandToPair(@NotNull Function<? super E, ? extends K> keyMapper, @NotNull Function<? super E, ? extends V> valueMapper) {
        return PairStream.of(this.underlying().map(e -> Pair.of(keyMapper.apply(e), valueMapper.apply(e))));
    }

    /**
     * Expands each element into a {@link Triple} by independently mapping to a Triple.
     *
     * @param <L>          the type of the triple middle element
     * @param <M>          the type of the triple middle element
     * @param <R>          the type of the triple right element
     * @param tripleMapper a function producing the triple value from each element
     * @return a {@link TripleStream} of {@code (element, middle, right)} triples
     */
    default <L, M, R> @NotNull TripleStream<L, M, R> expandToTriple(@NotNull Function<? super E, ? extends Triple<L, M, R>> tripleMapper) {
        return TripleStream.of(this.underlying().map(tripleMapper));
    }

    /**
     * Expands each element into a {@link Triple}, using the element itself as the left value and
     * the results of {@code middleMapper} and {@code rightMapper} as the middle and right values.
     *
     * @param <M>          the type of the triple middle element
     * @param <R>          the type of the triple right element
     * @param middleMapper a function producing the middle value from each element
     * @param rightMapper a function producing the right value from each element
     * @return a {@link TripleStream} of {@code (element, middle, right)} triples
     */
    default <M, R> @NotNull TripleStream<E, M, R> expandToTriple(@NotNull Function<? super E, ? extends M> middleMapper, @NotNull Function<? super E, ? extends R> rightMapper) {
        return TripleStream.of(this.underlying().map(e -> Triple.of(e, middleMapper.apply(e), rightMapper.apply(e))));
    }

    /**
     * Expands each element into a {@link Triple} by independently mapping to left, middle, and right values.
     *
     * @param <L>          the type of the triple left element
     * @param <M>          the type of the triple middle element
     * @param <R>          the type of the triple right element
     * @param leftMapper a function producing the left value from each element
     * @param middleMapper a function producing the middle value from each element
     * @param rightMapper a function producing the right value from each element
     * @return a {@link TripleStream} of fully mapped triples
     */
    default <L, M, R> @NotNull TripleStream<L, M, R> expandToTriple(@NotNull Function<? super E, ? extends L> leftMapper, @NotNull Function<? super E, ? extends M> middleMapper, @NotNull Function<? super E, ? extends R> rightMapper) {
        return TripleStream.of(this.underlying().map(e -> Triple.of(leftMapper.apply(e), middleMapper.apply(e), rightMapper.apply(e))));
    }

    /**
     * Zips the current stream with its indices, producing a {@link TripleStream} of
     * {@code (element, index, size)}.
     *
     * @return a triple stream where each triple contains the element, its zero-based index, and the estimated size
     */
    default @NotNull TripleStream<E, Long, Long> indexed() {
        return StreamUtil.zipWithIndex(this);
    }

}
