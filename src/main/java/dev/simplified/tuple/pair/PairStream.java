package dev.sbs.api.tuple.pair;

import dev.sbs.api.function.TriFunction;
import dev.sbs.api.tuple.single.SingleStream;
import dev.sbs.api.tuple.triple.Triple;
import dev.sbs.api.tuple.triple.TripleStream;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A specialized stream interface that operates on {@link Map.Entry} objects.
 * <p>
 * Provides manipulation of keys and values, enabling a variety of methods for
 * transformation, filtering, mapping, and reduction specifically tailored to
 * {@link Map.Entry} objects, their keys, and their values.
 * <p>
 * {@code PairStream} is constructed from a {@link Map}, a {@link Stream} of entries,
 * or a stream of keys combined with a mapping function, and maintains standard
 * compatibility with {@link Stream} operations.
 * <p>
 * Sits between {@link SingleStream} (1 element) and {@link TripleStream} (3 elements)
 * in the tuple stream hierarchy.
 *
 * @param <K> the type of keys represented in the stream
 * @param <V> the type of values represented in the stream
 */
@FunctionalInterface
public interface PairStream<K, V> extends Stream<Map.Entry<K, V>> {

    // Create

    /**
     * Returns an empty {@code PairStream}.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty {@code PairStream}
     */
    static <K, V> @NotNull PairStream<K, V> empty() {
        return of(Stream.empty());
    }

    /**
     * Creates a {@code PairStream} from all entries in the given {@link Map}.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map whose entries will be streamed
     * @return a {@code PairStream} backed by the map's entry set
     */
    static <K, V> @NotNull PairStream<K, V> of(@NotNull Map<K, V> map) {
        return of(map.entrySet().stream());
    }

    /**
     * Creates a {@code PairStream} wrapping the given stream of {@link Map.Entry} objects.
     *
     * @param <K>    the key type
     * @param <V>    the value type
     * @param stream the stream of entries to wrap
     * @return a {@code PairStream} backed by {@code stream}
     */
    static <K, V> @NotNull PairStream<K, V> of(@NotNull Stream<Map.Entry<K, V>> stream) {
        return () -> stream;
    }

    /**
     * Creates a {@code PairStream} from a stream of keys by applying {@code mapper} to
     * derive the value for each key.
     *
     * @param <K>    the key type
     * @param <V>    the value type
     * @param stream a stream of keys
     * @param mapper a function that produces a value for each key
     * @return a {@code PairStream} of key-value entries
     */
    static <K, V> @NotNull PairStream<K, V> of(@NotNull Stream<K> stream, @NotNull Function<? super K, ? extends V> mapper) {
        return () -> stream.map(key -> Pair.of(key, mapper.apply(key)));
    }

    // Close

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("all")
    default @NotNull PairStream<K, V> onClose(@NotNull Runnable closeHandler) {
        return of(this.underlying().onClose(closeHandler));
    }

    /** {@inheritDoc} */
    @Override
    default void close() {
        this.underlying().close();
    }

    // Entries

    /**
     * Returns the underlying {@link Stream} of {@link Map.Entry} objects that backs
     * this {@code PairStream}.
     *
     * @return the underlying entry stream
     */
    @NotNull Stream<Map.Entry<K, V>> underlying();

    /**
     * Returns a {@link SingleStream} of all keys in this stream, discarding values.
     *
     * @return a {@code SingleStream} of keys
     */
    default @NotNull SingleStream<K> keys() {
        return SingleStream.of(this.underlying().map(Map.Entry::getKey));
    }

    /**
     * Returns a {@link SingleStream} of all values in this stream, discarding keys.
     *
     * @return a {@code SingleStream} of values
     */
    default @NotNull SingleStream<V> values() {
        return SingleStream.of(this.underlying().map(Map.Entry::getValue));
    }

    /** {@inheritDoc} */
    @Override
    default long count() {
        return this.underlying().count();
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull PairStream<K, V> distinct() {
        return of(this.underlying().distinct());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull PairStream<K, V> limit(long maxSize) {
        return of(this.underlying().limit(maxSize));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull PairStream<K, V> peek(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        return of(this.underlying().peek(action));
    }

    /**
     * Returns a {@code PairStream} that additionally performs the given action on each
     * entry as entries are consumed, supplying the key and value separately.
     *
     * @param action a {@link BiConsumer} receiving the key and value of each entry
     * @return this stream with the peek action attached
     */
    default @NotNull PairStream<K, V> peek(@NotNull BiConsumer<? super K, ? super V> action) {
        return of(this.underlying().peek(entry -> action.accept(entry.getKey(), entry.getValue())));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull PairStream<K, V> skip(long number) {
        return of(this.underlying().skip(number));
    }

    // Filtering

    /** {@inheritDoc} */
    @Override
    default @NotNull PairStream<K, V> filter(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return of(this.underlying().filter(predicate));
    }

    /**
     * Returns a {@code PairStream} containing only entries for which the given
     * {@link BiPredicate} returns {@code true} when supplied the key and value.
     *
     * @param predicate a predicate receiving the key and value of each entry
     * @return a filtered {@code PairStream}
     */
    default @NotNull PairStream<K, V> filter(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.getKey(), entry.getValue())));
    }

    /**
     * Returns a {@code PairStream} containing only entries whose key matches the
     * given predicate, leaving values unchanged.
     *
     * @param predicate a predicate to test each entry's key
     * @return a filtered {@code PairStream}
     */
    default @NotNull PairStream<K, V> filterKey(@NotNull Predicate<? super K> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.getKey())));
    }

    /**
     * Returns a {@code PairStream} containing only entries whose value matches the
     * given predicate, leaving keys unchanged.
     *
     * @param predicate a predicate to test each entry's value
     * @return a filtered {@code PairStream}
     */
    default @NotNull PairStream<K, V> filterValue(@NotNull Predicate<? super V> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.getValue())));
    }

    // Find

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Map.Entry<K, V>> findAny() {
        return this.underlying().findAny();
    }

    /**
     * Returns a {@link PairOptional} describing some entry in this stream,
     * or an empty {@code PairOptional} if the stream is empty.
     *
     * @return a {@code PairOptional} of any matching entry
     */
    default @NotNull PairOptional<K, V> findAnyPair() {
        return PairOptional.of(this.underlying().findAny());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Map.Entry<K, V>> findFirst() {
        return this.underlying().findFirst();
    }

    /**
     * Returns a {@link PairOptional} describing the first entry in this stream,
     * or an empty {@code PairOptional} if the stream is empty.
     *
     * @return a {@code PairOptional} of the first entry
     */
    default @NotNull PairOptional<K, V> findFirstPair() {
        return PairOptional.of(this.underlying().findFirst());
    }

    // Flatmapping

    /** {@inheritDoc} */
    @Override
    default <R> @NotNull SingleStream<R> flatMap(@NotNull Function<? super Map.Entry<K, V>, ? extends Stream<? extends R>> mapper) {
        return SingleStream.of(this.underlying().flatMap(mapper));
    }

    /**
     * Returns a {@link SingleStream} by replacing each entry with the contents of a stream
     * produced by {@code mapper}, which receives the key and value separately.
     *
     * @param <R>    the element type of the resulting stream
     * @param mapper a function receiving the key and value, returning a stream of results
     * @return a flat-mapped {@link SingleStream}
     */
    default <R> @NotNull SingleStream<R> flatMapToObj(@NotNull BiFunction<? super K, ? super V, ? extends Stream<? extends R>> mapper) {
        return SingleStream.of(this.underlying().flatMap(entry -> mapper.apply(entry.getKey(), entry.getValue())));
    }

    /**
     * Returns a {@code PairStream} by replacing each entry with the contents of a
     * {@code PairStream} produced by {@code mapper}, which receives the key and value separately.
     *
     * @param <RK>   the key type of the resulting stream
     * @param <RV>   the value type of the resulting stream
     * @param mapper a function receiving the key and value, returning a {@code PairStream}
     * @return a flat-mapped {@code PairStream}
     */
    default <RK, RV> @NotNull PairStream<RK, RV> flatMap(@NotNull BiFunction<? super K, ? super V, ? extends PairStream<RK, RV>> mapper) {
        return of(this.underlying().flatMap(entry -> mapper.apply(entry.getKey(), entry.getValue()).underlying()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull DoubleStream flatMapToDouble(@NotNull Function<? super Map.Entry<K, V>, ? extends DoubleStream> mapper) {
        return this.underlying().flatMapToDouble(mapper);
    }

    /**
     * Returns a {@link DoubleStream} by replacing each entry with the contents of a
     * {@code DoubleStream} produced by {@code mapper}, which receives the key and value separately.
     *
     * @param mapper a function receiving the key and value, returning a {@code DoubleStream}
     * @return a flat-mapped {@code DoubleStream}
     */
    default @NotNull DoubleStream flatMapToDouble(@NotNull BiFunction<? super K, ? super V, ? extends DoubleStream> mapper) {
        return this.underlying().flatMapToDouble(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull IntStream flatMapToInt(@NotNull Function<? super Map.Entry<K, V>, ? extends IntStream> mapper) {
        return this.underlying().flatMapToInt(mapper);
    }

    /**
     * Returns an {@link IntStream} by replacing each entry with the contents of an
     * {@code IntStream} produced by {@code mapper}, which receives the key and value separately.
     *
     * @param mapper a function receiving the key and value, returning an {@code IntStream}
     * @return a flat-mapped {@code IntStream}
     */
    default @NotNull IntStream flatMapToInt(@NotNull BiFunction<? super K, ? super V, ? extends IntStream> mapper) {
        return this.underlying().flatMapToInt(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull LongStream flatMapToLong(@NotNull Function<? super Map.Entry<K, V>, ? extends LongStream> mapper) {
        return this.underlying().flatMapToLong(mapper);
    }

    /**
     * Returns a {@link LongStream} by replacing each entry with the contents of a
     * {@code LongStream} produced by {@code mapper}, which receives the key and value separately.
     *
     * @param mapper a function receiving the key and value, returning a {@code LongStream}
     * @return a flat-mapped {@code LongStream}
     */
    default @NotNull LongStream flatMapToLong(@NotNull BiFunction<? super K, ? super V, ? extends LongStream> mapper) {
        return this.underlying().flatMapToLong(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    // ForEach

    /** {@inheritDoc} */
    @Override
    default void forEach(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        this.underlying().forEach(action);
    }

    /**
     * Performs {@code action} for each entry in this stream, supplying the key and
     * value separately. The encounter order is not guaranteed for parallel streams.
     *
     * @param action a {@link BiConsumer} receiving the key and value of each entry
     */
    default void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        this.underlying().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    /** {@inheritDoc} */
    @Override
    default void forEachOrdered(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        this.underlying().forEachOrdered(action);
    }

    /**
     * Performs {@code action} for each entry in this stream in encounter order,
     * supplying the key and value separately.
     *
     * @param action a {@link BiConsumer} receiving the key and value of each entry
     */
    default void forEachOrdered(@NotNull BiConsumer<? super K, ? super V> action) {
        this.underlying().forEachOrdered(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    // Iterator

    /** {@inheritDoc} */
    @Override
    default @NotNull Iterator<Map.Entry<K, V>> iterator() {
        return this.underlying().iterator();
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Spliterator<Map.Entry<K, V>> spliterator() {
        return this.underlying().spliterator();
    }

    // Mapping

    /** {@inheritDoc} */
    @Override
    default <R> @NotNull SingleStream<R> map(@NotNull Function<? super Map.Entry<K, V>, ? extends R> mapper) {
        return SingleStream.of(this.underlying().map(mapper));
    }

    /**
     * Returns a {@code PairStream} by applying {@code mapper} to the key and value of each
     * entry, producing a new {@link Map.Entry} for each.
     *
     * @param <RK>   the key type of the resulting stream
     * @param <RV>   the value type of the resulting stream
     * @param mapper a function receiving the key and value, returning a new entry
     * @return a mapped {@code PairStream}
     */
    default <RK, RV> @NotNull PairStream<RK, RV> map(@NotNull BiFunction<? super K, ? super V, ? extends Map.Entry<RK, RV>> mapper) {
        return of(this.underlying().map(entry -> mapper.apply(entry.getKey(), entry.getValue())));
    }

    /**
     * Returns a {@code PairStream} with each key replaced by the result of applying
     * {@code mapper} to it, leaving values unchanged.
     *
     * @param <R>    the new key type
     * @param mapper a function to apply to each key
     * @return a {@code PairStream} with transformed keys
     */
    default <R> @NotNull PairStream<R, V> mapKey(@NotNull Function<? super K, ? extends R> mapper) {
        return of(this.underlying().map(entry -> Pair.of(mapper.apply(entry.getKey()), entry.getValue())));
    }

    /**
     * Returns a {@code PairStream} with each value replaced by the result of applying
     * {@code mapper} to it, leaving keys unchanged.
     *
     * @param <R>    the new value type
     * @param mapper a function to apply to each value
     * @return a {@code PairStream} with transformed values
     */
    default <R> @NotNull PairStream<K, R> mapValue(@NotNull Function<? super V, ? extends R> mapper) {
        return of(this.underlying().map(entry -> Pair.of(entry.getKey(), mapper.apply(entry.getValue()))));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleFunction<? super Map.Entry<K, V>> mapper) {
        return this.underlying().mapToDouble(mapper);
    }

    /**
     * Returns a {@link DoubleStream} by applying {@code mapper} to the key and value of
     * each entry.
     *
     * @param mapper a function receiving the key and value, returning a {@code double}
     * @return a mapped {@code DoubleStream}
     */
    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleBiFunction<? super K, ? super V> mapper) {
        return this.underlying().mapToDouble(entry -> mapper.applyAsDouble(entry.getKey(), entry.getValue()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull IntStream mapToInt(@NotNull ToIntFunction<? super Map.Entry<K, V>> mapper) {
        return this.underlying().mapToInt(mapper);
    }

    /**
     * Returns an {@link IntStream} by applying {@code mapper} to the key and value of
     * each entry.
     *
     * @param mapper a function receiving the key and value, returning an {@code int}
     * @return a mapped {@code IntStream}
     */
    default @NotNull IntStream mapToInt(@NotNull ToIntBiFunction<? super K, ? super V> mapper) {
        return this.underlying().mapToInt(entry -> mapper.applyAsInt(entry.getKey(), entry.getValue()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull LongStream mapToLong(@NotNull ToLongFunction<? super Map.Entry<K, V>> mapper) {
        return this.underlying().mapToLong(mapper);
    }

    /**
     * Returns a {@link LongStream} by applying {@code mapper} to the key and value of
     * each entry.
     *
     * @param mapper a function receiving the key and value, returning a {@code long}
     * @return a mapped {@code LongStream}
     */
    default @NotNull LongStream mapToLong(@NotNull ToLongBiFunction<? super K, ? super V> mapper) {
        return this.underlying().mapToLong(entry -> mapper.applyAsLong(entry.getKey(), entry.getValue()));
    }

    // Matching

    /** {@inheritDoc} */
    @Override
    default boolean allMatch(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return this.underlying().allMatch(predicate);
    }

    /**
     * Returns whether all entries match the given {@link BiPredicate} when supplied
     * the key and value of each entry.
     *
     * @param predicate a predicate receiving the key and value of each entry
     * @return {@code true} if all entries match, {@code false} otherwise
     */
    default boolean allMatch(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return this.underlying().allMatch(entry -> predicate.test(entry.getKey(), entry.getValue()));
    }

    /** {@inheritDoc} */
    @Override
    default boolean anyMatch(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return this.underlying().anyMatch(predicate);
    }

    /**
     * Returns whether any entry matches the given {@link BiPredicate} when supplied
     * the key and value of each entry.
     *
     * @param predicate a predicate receiving the key and value of each entry
     * @return {@code true} if any entry matches, {@code false} otherwise
     */
    default boolean anyMatch(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return this.underlying().anyMatch(entry -> predicate.test(entry.getKey(), entry.getValue()));
    }

    /** {@inheritDoc} */
    @Override
    default boolean noneMatch(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return this.underlying().noneMatch(predicate);
    }

    /**
     * Returns whether no entries match the given {@link BiPredicate} when supplied
     * the key and value of each entry.
     *
     * @param predicate a predicate receiving the key and value of each entry
     * @return {@code true} if no entries match, {@code false} otherwise
     */
    default boolean noneMatch(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return this.underlying().noneMatch(entry -> predicate.test(entry.getKey(), entry.getValue()));
    }

    // Minmax

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Map.Entry<K, V>> max(@NotNull Comparator<? super Map.Entry<K, V>> comparator) {
        return this.underlying().max(comparator);
    }

    /**
     * Returns the maximum entry according to the given key comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare keys
     * @return an {@code Optional} of the entry with the maximum key
     */
    default @NotNull Optional<Map.Entry<K, V>> maxByKey(@NotNull Comparator<? super K> comparator) {
        return this.underlying().max(Map.Entry.comparingByKey(comparator));
    }

    /**
     * Returns the maximum entry according to the given value comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare values
     * @return an {@code Optional} of the entry with the maximum value
     */
    default @NotNull Optional<Map.Entry<K, V>> maxByValue(@NotNull Comparator<? super V> comparator) {
        return this.underlying().max(Map.Entry.comparingByValue(comparator));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Map.Entry<K, V>> min(@NotNull Comparator<? super Map.Entry<K, V>> comparator) {
        return this.underlying().min(comparator);
    }

    /**
     * Returns the minimum entry according to the given key comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare keys
     * @return an {@code Optional} of the entry with the minimum key
     */
    default @NotNull Optional<Map.Entry<K, V>> minByKey(@NotNull Comparator<? super K> comparator) {
        return this.underlying().min(Map.Entry.comparingByKey(comparator));
    }

    /**
     * Returns the minimum entry according to the given value comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare values
     * @return an {@code Optional} of the entry with the minimum value
     */
    default @NotNull Optional<Map.Entry<K, V>> minByValue(@NotNull Comparator<? super V> comparator) {
        return this.underlying().min(Map.Entry.comparingByValue(comparator));
    }

    // Order

    /** {@inheritDoc} */
    @Override
    default boolean isParallel() {
        return this.underlying().isParallel();
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull PairStream<K, V> parallel() {
        return of(this.underlying().parallel());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull PairStream<K, V> sequential() {
        return of(this.underlying().sequential());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull PairStream<K, V> unordered() {
        return of(this.underlying().unordered());
    }

    // Reduction

    /** {@inheritDoc} */
    @Override
    default @NotNull Map.Entry<K, V> reduce(@NotNull Map.Entry<K, V> identity, @NotNull BinaryOperator<Map.Entry<K, V>> accumulator) {
        return this.underlying().reduce(identity, accumulator);
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Map.Entry<K, V>> reduce(@NotNull BinaryOperator<Map.Entry<K, V>> accumulator) {
        return this.underlying().reduce(accumulator);
    }

    /** {@inheritDoc} */
    @Override
    default <U> @NotNull U reduce(@NotNull U identity, @NotNull BiFunction<U, ? super Map.Entry<K, V>, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        return this.underlying().reduce(identity, accumulator, combiner);
    }

    /**
     * Performs a reduction on the entries of this stream, using the provided identity value,
     * an accumulator that receives both the key and value separately, and a combiner for
     * parallel execution.
     *
     * @param <U>         the type of the result
     * @param identity    the identity value for the accumulator
     * @param accumulator a function that folds a key-value pair into the result
     * @param combiner    a function to combine two partial results in parallel execution
     * @return the reduced result
     */
    default <U> @NotNull U reduce(@NotNull U identity, @NotNull TriFunction<U, ? super K, ? super V, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        return this.underlying().reduce(identity, (u, entry) -> accumulator.apply(u, entry.getKey(), entry.getValue()), combiner);
    }

    // Sorting

    /** {@inheritDoc} */
    @Override
    default PairStream<K, V> sorted() {
        return of(this.underlying().sorted());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull PairStream<K, V> sorted(@NotNull Comparator<? super Map.Entry<K, V>> comparator) {
        return of(this.underlying().sorted(comparator));
    }

    /**
     * Returns a {@code PairStream} with entries sorted according to the given key comparator.
     *
     * @param comparator a comparator to compare keys
     * @return a sorted {@code PairStream}
     */
    default @NotNull PairStream<K, V> sortedByKey(@NotNull Comparator<? super K> comparator) {
        return of(this.underlying().sorted(Map.Entry.comparingByKey(comparator)));
    }

    /**
     * Returns a {@code PairStream} with entries sorted according to the given value comparator.
     *
     * @param comparator a comparator to compare values
     * @return a sorted {@code PairStream}
     */
    default @NotNull PairStream<K, V> sortedByValue(@NotNull Comparator<? super V> comparator) {
        return of(this.underlying().sorted(Map.Entry.comparingByValue(comparator)));
    }

    // Collect

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this stream using a
     * {@code Collector}.  A {@code Collector}
     * encapsulates the functions used as arguments to
     * {@link Stream#collect(Supplier, BiConsumer, BiConsumer)}, allowing for reuse of
     * collection strategies and composition of collect operations such as
     * multiple-level grouping or partitioning.
     *
     * <p>If the stream is parallel, and the {@code Collector}
     * is {@link Collector.Characteristics#CONCURRENT concurrent}, and
     * either the stream is unordered or the collector is
     * {@link Collector.Characteristics#UNORDERED unordered},
     * then a concurrent reduction will be performed (see {@link Collector} for
     * details on concurrent reduction.)
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * <p>When executed in parallel, multiple intermediate results may be
     * instantiated, populated, and merged so as to maintain isolation of
     * mutable data structures.  Therefore, even when executed in parallel
     * with non-thread-safe data structures (such as {@code ArrayList}), no
     * additional synchronization is needed for a parallel reduction.
     *
     * @apiNote
     * The following will accumulate strings into a List:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(Collectors.toList());
     * }</pre>
     *
     * <p>The following will classify {@code Person} objects by city:
     * <pre>{@code
     *     Map<String, List<Person>> peopleByCity
     *         = personStream.collect(Collectors.groupingBy(Person::getCity));
     * }</pre>
     *
     * <p>The following will classify {@code Person} objects by state and city,
     * cascading two {@code Collector}s together:
     * <pre>{@code
     *     Map<String, Map<String, List<Person>>> peopleByStateAndCity
     *         = personStream.collect(Collectors.groupingBy(Person::getState,
     *                                                      Collectors.groupingBy(Person::getCity)));
     * }</pre>
     *
     * @param <R> the type of the result
     * @param <A> the intermediate accumulation type of the {@code Collector}
     * @param collector the {@code Collector} describing the reduction
     * @return the result of the reduction
     * @see Stream#collect(Supplier, BiConsumer, BiConsumer)
     * @see Collectors
     */
    @Override
    default <R, A> R collect(@NotNull Collector<? super Map.Entry<K, V>, A, R> collector) {
        return this.underlying().collect(collector);
    }

    /** {@inheritDoc} */
    @Override
    default <R> R collect(@NotNull Supplier<R> supplier, @NotNull BiConsumer<R, ? super Map.Entry<K, V>> accumulator, @NotNull BiConsumer<R, R> combiner) {
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

    // Collapse

    /**
     * Collapses each entry into a single value by applying {@code mapper} to the key
     * and value, returning a {@link SingleStream} of the results.
     *
     * @param <R>    the result element type
     * @param mapper a function receiving the key and value, returning the collapsed result
     * @return a {@link SingleStream} of collapsed values
     */
    default <R> @NotNull SingleStream<R> collapseToSingle(@NotNull BiFunction<? super K, ? super V, ? extends R> mapper) {
        return SingleStream.of(this.underlying().map(entry -> mapper.apply(entry.getKey(), entry.getValue())));
    }

    /**
     * Returns a {@link SingleStream} by replacing each entry with the contents of the
     * {@link Collection} returned by {@code mapper}, avoiding a manual
     * {@code .flatMap(c -> c.stream())} call.
     *
     * @param <R>    the element type of the resulting stream
     * @param mapper a function receiving the key and value, returning a collection of results
     * @return a flat-mapped {@link SingleStream}
     */
    default <R> @NotNull SingleStream<R> flatMapCollection(@NotNull BiFunction<? super K, ? super V, ? extends Collection<? extends R>> mapper) {
        return SingleStream.of(this.underlying().flatMap(entry -> mapper.apply(entry.getKey(), entry.getValue()).stream()));
    }

    // Expand

    /**
     * Expands each entry into a {@link Triple} by appending a right value derived from the
     * key and value, keeping the key as left and value as middle.
     *
     * @param <R>         the type of the triple right element
     * @param rightMapper a function receiving the key and value, returning the right value
     * @return a {@link TripleStream} of {@code (key, value, right)} triples
     */
    default <R> @NotNull TripleStream<K, V, R> expandToTriple(@NotNull BiFunction<? super K, ? super V, ? extends R> rightMapper) {
        return TripleStream.of(this.underlying().map(entry -> Triple.of(entry.getKey(), entry.getValue(), rightMapper.apply(entry.getKey(), entry.getValue()))));
    }

    /**
     * Expands each entry into a {@link Triple} by prepending a left value derived from the
     * key and value, keeping the key as middle and value as right.
     *
     * @param <L>        the type of the triple left element
     * @param leftMapper a function receiving the key and value, returning the left value
     * @return a {@link TripleStream} of {@code (left, key, value)} triples
     */
    default <L> @NotNull TripleStream<L, K, V> expandLeft(@NotNull BiFunction<? super K, ? super V, ? extends L> leftMapper) {
        return TripleStream.of(this.underlying().map(entry -> Triple.of(leftMapper.apply(entry.getKey(), entry.getValue()), entry.getKey(), entry.getValue())));
    }

}
