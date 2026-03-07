package dev.sbs.api.tuple.pair;

import dev.sbs.api.function.TriFunction;
import org.jetbrains.annotations.NotNull;

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
 * Providing manipulation of keys and values, enabling a variety of methods for
 * transformation, filtering, mapping, and reduction specifically tailored to
 * {@link Map.Entry} objects, their keys, and their values.
 * <p>
 * {@code PairStream} is constructed from a {@link Map}, {@link Stream} of
 * entries, or a stream of keys combined with a mapping function and maintains
 * standard compatibility with {@link Stream} operations.
 *
 * @param <K> The type of keys represented in the stream.
 * @param <V> The type of values represented in the stream.
 */
@FunctionalInterface
public interface PairStream<K, V> extends Stream<Map.Entry<K, V>> {

    // Create

    static <K, V> @NotNull PairStream<K, V> empty() {
        return of(Stream.empty());
    }

    static <K, V> @NotNull PairStream<K, V> of(@NotNull Map<K, V> map) {
        return of(map.entrySet().stream());
    }

    static <K, V> @NotNull PairStream<K, V> of(@NotNull Stream<Map.Entry<K, V>> stream) {
        return () -> stream;
    }

    static <K, V> @NotNull PairStream<K, V> of(@NotNull Stream<K> stream, @NotNull Function<? super K, ? extends V> mapper) {
        return () -> stream.map(key -> Pair.of(key, mapper.apply(key)));
    }

    // Close

    @Override
    @SuppressWarnings("all")
    default @NotNull PairStream<K, V> onClose(@NotNull Runnable closeHandler) {
        return of(this.underlying().onClose(closeHandler));
    }

    @Override
    default void close() {
        this.underlying().close();
    }

    // Entries

    @NotNull Stream<Map.Entry<K, V>> underlying();

    default @NotNull Stream<K> keys() {
        return this.underlying().map(Map.Entry::getKey);
    }

    default @NotNull Stream<V> values() {
        return this.underlying().map(Map.Entry::getValue);
    }

    @Override
    default long count() {
        return this.underlying().count();
    }

    @Override
    default @NotNull PairStream<K, V> distinct() {
        return of(this.underlying().distinct());
    }

    @Override
    default @NotNull PairStream<K, V> limit(long maxSize) {
        return of(this.underlying().limit(maxSize));
    }

    @Override
    default @NotNull PairStream<K, V> peek(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        return of(this.underlying().peek(action));
    }

    default @NotNull PairStream<K, V> peek(@NotNull BiConsumer<? super K, ? super V> action) {
        return of(this.underlying().peek(entry -> action.accept(entry.getKey(), entry.getValue())));
    }

    @Override
    default @NotNull PairStream<K, V> skip(long number) {
        return of(this.underlying().skip(number));
    }

    // Filtering

    @Override
    default @NotNull PairStream<K, V> filter(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return of(this.underlying().filter(predicate));
    }

    default @NotNull PairStream<K, V> filter(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.getKey(), entry.getValue())));
    }

    default @NotNull PairStream<K, V> filterKey(@NotNull Predicate<? super K> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.getKey())));
    }

    default @NotNull PairStream<K, V> filterValue(@NotNull Predicate<? super V> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.getValue())));
    }

    // Find

    @Override
    default @NotNull Optional<Map.Entry<K, V>> findAny() {
        return this.underlying().findAny();
    }

    default @NotNull PairOptional<K, V> findAnyPair() {
        return PairOptional.of(this.underlying().findAny());
    }

    @Override
    default @NotNull Optional<Map.Entry<K, V>> findFirst() {
        return this.underlying().findFirst();
    }

    default @NotNull PairOptional<K, V> findFirstPair() {
        return PairOptional.of(this.underlying().findFirst());
    }

    // Flatmapping

    @Override
    default <R> @NotNull Stream<R> flatMap(@NotNull Function<? super Map.Entry<K, V>, ? extends Stream<? extends R>> mapper) {
        return this.underlying().flatMap(mapper);
    }

    default <R> @NotNull Stream<R> flatMapToObj(@NotNull BiFunction<? super K, ? super V, ? extends Stream<? extends R>> mapper) {
        return this.underlying().flatMap(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    default <RK, RV> @NotNull PairStream<RK, RV> flatMap(@NotNull BiFunction<? super K, ? super V, ? extends PairStream<RK, RV>> mapper) {
        return of(this.underlying().flatMap(entry -> mapper.apply(entry.getKey(), entry.getValue()).underlying()));
    }

    @Override
    default @NotNull DoubleStream flatMapToDouble(@NotNull Function<? super Map.Entry<K, V>, ? extends DoubleStream> mapper) {
        return this.underlying().flatMapToDouble(mapper);
    }

    default @NotNull DoubleStream flatMapToDouble(@NotNull BiFunction<? super K, ? super V, ? extends DoubleStream> mapper) {
        return this.underlying().flatMapToDouble(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    @Override
    default @NotNull IntStream flatMapToInt(@NotNull Function<? super Map.Entry<K, V>, ? extends IntStream> mapper) {
        return this.underlying().flatMapToInt(mapper);
    }

    default @NotNull IntStream flatMapToInt(@NotNull BiFunction<? super K, ? super V, ? extends IntStream> mapper) {
        return this.underlying().flatMapToInt(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    @Override
    default @NotNull LongStream flatMapToLong(@NotNull Function<? super Map.Entry<K, V>, ? extends LongStream> mapper) {
        return this.underlying().flatMapToLong(mapper);
    }

    default @NotNull LongStream flatMapToLong(@NotNull BiFunction<? super K, ? super V, ? extends LongStream> mapper) {
        return this.underlying().flatMapToLong(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    // ForEach

    @Override
    default void forEach(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        this.underlying().forEach(action);
    }

    default void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        this.underlying().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    default void forEachOrdered(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        this.underlying().forEachOrdered(action);
    }

    default void forEachOrdered(@NotNull BiConsumer<? super K, ? super V> action) {
        this.underlying().forEachOrdered(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    // Iterator

    @Override
    default @NotNull Iterator<Map.Entry<K, V>> iterator() {
        return this.underlying().iterator();
    }

    @Override
    default @NotNull Spliterator<Map.Entry<K, V>> spliterator() {
        return this.underlying().spliterator();
    }

    // Mapping

    @Override
    default <R> @NotNull Stream<R> map(@NotNull Function<? super Map.Entry<K, V>, ? extends R> mapper) {
        return this.underlying().map(mapper);
    }

    default <R> @NotNull Stream<R> mapToObj(@NotNull BiFunction<? super K, ? super V, ? extends R> mapper) {
        return this.underlying().map(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    default @NotNull Stream<K> mapToKey() {
        return this.underlying().map(Map.Entry::getKey);
    }

    default @NotNull Stream<V> mapToValue() {
        return this.underlying().map(Map.Entry::getValue);
    }

    default <RK, RV> @NotNull PairStream<RK, RV> map(@NotNull BiFunction<? super K, ? super V, ? extends Map.Entry<RK, RV>> mapper) {
        return of(this.underlying().map(entry -> mapper.apply(entry.getKey(), entry.getValue())));
    }

    default <R> @NotNull PairStream<R, V> mapKey(@NotNull Function<? super K, ? extends R> mapper) {
        return of(this.underlying().map(entry -> Pair.of(mapper.apply(entry.getKey()), entry.getValue())));
    }

    default <R> @NotNull PairStream<K, R> mapValue(@NotNull Function<? super V, ? extends R> mapper) {
        return of(this.underlying().map(entry -> Pair.of(entry.getKey(), mapper.apply(entry.getValue()))));
    }

    @Override
    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleFunction<? super Map.Entry<K, V>> mapper) {
        return this.underlying().mapToDouble(mapper);
    }

    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleBiFunction<? super K, ? super V> mapper) {
        return this.underlying().mapToDouble(entry -> mapper.applyAsDouble(entry.getKey(), entry.getValue()));
    }

    @Override
    default @NotNull IntStream mapToInt(@NotNull ToIntFunction<? super Map.Entry<K, V>> mapper) {
        return this.underlying().mapToInt(mapper);
    }

    default @NotNull IntStream mapToInt(@NotNull ToIntBiFunction<? super K, ? super V> mapper) {
        return this.underlying().mapToInt(entry -> mapper.applyAsInt(entry.getKey(), entry.getValue()));
    }

    @Override
    default @NotNull LongStream mapToLong(@NotNull ToLongFunction<? super Map.Entry<K, V>> mapper) {
        return this.underlying().mapToLong(mapper);
    }

    default @NotNull LongStream mapToLong(@NotNull ToLongBiFunction<? super K, ? super V> mapper) {
        return this.underlying().mapToLong(entry -> mapper.applyAsLong(entry.getKey(), entry.getValue()));
    }

    // Matching

    @Override
    default boolean allMatch(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return this.underlying().allMatch(predicate);
    }

    default boolean allMatch(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return this.underlying().allMatch(entry -> predicate.test(entry.getKey(), entry.getValue()));
    }

    @Override
    default boolean anyMatch(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return this.underlying().anyMatch(predicate);
    }

    default boolean anyMatch(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return this.underlying().anyMatch(entry -> predicate.test(entry.getKey(), entry.getValue()));
    }

    @Override
    default boolean noneMatch(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return this.underlying().noneMatch(predicate);
    }

    default boolean noneMatch(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return this.underlying().noneMatch(entry -> predicate.test(entry.getKey(), entry.getValue()));
    }

    // Minmax

    @Override
    default @NotNull Optional<Map.Entry<K, V>> max(@NotNull Comparator<? super Map.Entry<K, V>> comparator) {
        return this.underlying().max(comparator);
    }

    default @NotNull Optional<Map.Entry<K, V>> maxByKey(@NotNull Comparator<? super K> comparator) {
        return this.underlying().max(Map.Entry.comparingByKey(comparator));
    }

    default @NotNull Optional<Map.Entry<K, V>> maxByValue(@NotNull Comparator<? super V> comparator) {
        return this.underlying().max(Map.Entry.comparingByValue(comparator));
    }

    @Override
    default @NotNull Optional<Map.Entry<K, V>> min(@NotNull Comparator<? super Map.Entry<K, V>> comparator) {
        return this.underlying().min(comparator);
    }

    default @NotNull Optional<Map.Entry<K, V>> minByKey(@NotNull Comparator<? super K> comparator) {
        return this.underlying().min(Map.Entry.comparingByKey(comparator));
    }

    default @NotNull Optional<Map.Entry<K, V>> minByValue(@NotNull Comparator<? super V> comparator) {
        return this.underlying().min(Map.Entry.comparingByValue(comparator));
    }

    // Order

    @Override
    default boolean isParallel() {
        return this.underlying().isParallel();
    }

    @Override
    default @NotNull PairStream<K, V> parallel() {
        return of(this.underlying().parallel());
    }

    @Override
    default @NotNull PairStream<K, V> sequential() {
        return of(this.underlying().sequential());
    }

    @Override
    default @NotNull PairStream<K, V> unordered() {
        return of(this.underlying().unordered());
    }

    // Reduction

    @Override
    default @NotNull Map.Entry<K, V> reduce(@NotNull Map.Entry<K, V> identity, @NotNull BinaryOperator<Map.Entry<K, V>> accumulator) {
        return this.underlying().reduce(identity, accumulator);
    }

    @Override
    default @NotNull Optional<Map.Entry<K, V>> reduce(@NotNull BinaryOperator<Map.Entry<K, V>> accumulator) {
        return this.underlying().reduce(accumulator);
    }

    @Override
    default <U> @NotNull U reduce(@NotNull U identity, @NotNull BiFunction<U, ? super Map.Entry<K, V>, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        return this.underlying().reduce(identity, accumulator, combiner);
    }

    default <U> @NotNull U reduce(@NotNull U identity, @NotNull TriFunction<U, ? super K, ? super V, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        return this.underlying().reduce(identity, (u, entry) -> accumulator.apply(u, entry.getKey(), entry.getValue()), combiner);
    }

    // Sorting

    @Override
    default PairStream<K, V> sorted() {
        return of(this.underlying().sorted());
    }

    @Override
    default @NotNull PairStream<K, V> sorted(@NotNull Comparator<? super Map.Entry<K, V>> comparator) {
        return of(this.underlying().sorted(comparator));
    }

    default @NotNull PairStream<K, V> sortedByKey(@NotNull Comparator<? super K> comparator) {
        return of(this.underlying().sorted(Map.Entry.comparingByKey(comparator)));
    }

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

    @Override
    default <R> R collect(@NotNull Supplier<R> supplier, @NotNull BiConsumer<R, ? super Map.Entry<K, V>> accumulator, @NotNull BiConsumer<R, R> combiner) {
        return this.underlying().collect(supplier, accumulator, combiner);
    }

    @Override
    default @NotNull Object @NotNull [] toArray() {
        return this.underlying().toArray();
    }

    @Override
    default <A> @NotNull A @NotNull [] toArray(@NotNull IntFunction<A[]> generator) {
        return this.underlying().toArray(generator);
    }

}