package dev.sbs.api.collection.stream;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedMap;
import dev.sbs.api.collection.stream.triple.TriFunction;
import dev.sbs.api.mutable.pair.Pair;
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

@FunctionalInterface
public interface PairStream<K, V> extends Stream<Map.Entry<K, V>> {

    // Create

    static <K, V> @NotNull PairStream<K, V> of(@NotNull Map<K, V> map) {
        return of(map.entrySet().stream());
    }

    static <K, V> @NotNull PairStream<K, V> of(@NotNull Stream<Map.Entry<K, V>> stream) {
        return () -> stream;
    }

    static <K, V> @NotNull PairStream<K, V> of(@NotNull Stream<K> stream, @NotNull Function<? super K, ? extends V> function) {
        return () -> stream.map(key -> Pair.of(key, function.apply(key)));
    }

    // Close

    @Override
    @SuppressWarnings("all")
    default @NotNull PairStream<K, V> onClose(@NotNull Runnable closeHandler) {
        return of(this.entries().onClose(closeHandler));
    }

    @Override
    default void close() {
        this.entries().close();
    }

    // Entries

    @NotNull Stream<Map.Entry<K, V>> entries();

    default @NotNull Stream<K> keys() {
        return this.entries().map(Map.Entry::getKey);
    }

    default @NotNull Stream<V> values() {
        return this.entries().map(Map.Entry::getValue);
    }

    @Override
    default long count() {
        return this.entries().count();
    }

    @Override
    default @NotNull PairStream<K, V> distinct() {
        return of(this.entries().distinct());
    }

    @Override
    default @NotNull PairStream<K, V> limit(long maxSize) {
        return of(this.entries().limit(maxSize));
    }

    @Override
    default @NotNull PairStream<K, V> peek(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        return of(this.entries().peek(action));
    }

    default @NotNull PairStream<K, V> peek(@NotNull BiConsumer<? super K, ? super V> action) {
        return of(this.entries().peek(entry -> action.accept(entry.getKey(), entry.getValue())));
    }

    @Override
    default @NotNull PairStream<K, V> skip(long number) {
        return of(this.entries().skip(number));
    }

    // Filtering

    @Override
    default @NotNull PairStream<K, V> filter(@NotNull Predicate<? super Map.Entry<K, V>> mapper) {
        return of(this.entries().filter(mapper));
    }

    default @NotNull PairStream<K, V> filter(@NotNull BiPredicate<? super K, ? super V> mapper) {
        return of(this.entries().filter(entry -> mapper.test(entry.getKey(), entry.getValue())));
    }

    default @NotNull PairStream<K, V> filterKey(@NotNull Predicate<? super K> mapper) {
        return of(this.entries().filter(entry -> mapper.test(entry.getKey())));
    }

    default @NotNull PairStream<K, V> filterValue(@NotNull Predicate<? super V> mapper) {
        return of(this.entries().filter(entry -> mapper.test(entry.getValue())));
    }

    // Find

    @Override
    default @NotNull Optional<Map.Entry<K, V>> findAny() {
        return this.entries().findAny();
    }

    @Override
    default @NotNull Optional<Map.Entry<K, V>> findFirst() {
        return this.entries().findFirst();
    }

    // Flatmapping

    @Override
    default <R> @NotNull Stream<R> flatMap(@NotNull Function<? super Map.Entry<K, V>, ? extends Stream<? extends R>> mapper) {
        return this.entries().flatMap(mapper);
    }

    default <RK, RV> @NotNull PairStream<RK, RV> flatMap(@NotNull BiFunction<? super K, ? super V, ? extends PairStream<RK, RV>> mapper) {
        return of(this.entries().flatMap(entry -> mapper.apply(entry.getKey(), entry.getValue()).entries()));
    }

    default <R> @NotNull Stream<R> flatMapToObj(@NotNull BiFunction<? super K, ? super V, ? extends Stream<? extends R>> mapper) {
        return this.entries().flatMap(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    @Override
    default @NotNull DoubleStream flatMapToDouble(@NotNull Function<? super Map.Entry<K, V>, ? extends DoubleStream> mapper) {
        return this.entries().flatMapToDouble(mapper);
    }

    default @NotNull DoubleStream flatMapToDouble(@NotNull BiFunction<? super K, ? super V, ? extends DoubleStream> mapper) {
        return this.entries().flatMapToDouble(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    @Override
    default @NotNull IntStream flatMapToInt(@NotNull Function<? super Map.Entry<K, V>, ? extends IntStream> mapper) {
        return this.entries().flatMapToInt(mapper);
    }

    default @NotNull IntStream flatMapToInt(@NotNull BiFunction<? super K, ? super V, ? extends IntStream> mapper) {
        return this.entries().flatMapToInt(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    @Override
    default @NotNull LongStream flatMapToLong(@NotNull Function<? super Map.Entry<K, V>, ? extends LongStream> mapper) {
        return this.entries().flatMapToLong(mapper);
    }

    default @NotNull LongStream flatMapToLong(@NotNull BiFunction<? super K, ? super V, ? extends LongStream> mapper) {
        return this.entries().flatMapToLong(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    // ForEach

    @Override
    default void forEach(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        this.entries().forEach(action);
    }

    default void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        this.entries().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    default void forEachOrdered(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        this.entries().forEachOrdered(action);
    }

    default void forEachOrdered(@NotNull BiConsumer<? super K, ? super V> action) {
        this.entries().forEachOrdered(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    // Iterator

    @Override
    default @NotNull Iterator<Map.Entry<K, V>> iterator() {
        return this.entries().iterator();
    }

    @Override
    default @NotNull Spliterator<Map.Entry<K, V>> spliterator() {
        return this.entries().spliterator();
    }

    // Mapping

    @Override
    default <R> @NotNull Stream<R> map(@NotNull Function<? super Map.Entry<K, V>, ? extends R> mapper) {
        return this.entries().map(mapper);
    }

    default <R> @NotNull Stream<R> map(@NotNull BiFunction<? super K, ? super V, ? extends R> mapper) {
        return this.entries().map(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    default <R> @NotNull PairStream<R, V> mapKey(@NotNull Function<? super K, ? extends R> mapper) {
        return of(this.entries().map(entry -> Pair.of(mapper.apply(entry.getKey()), entry.getValue())));
    }

    default <R> @NotNull PairStream<K, R> mapValue(@NotNull Function<? super V, ? extends R> mapper) {
        return of(this.entries().map(entry -> Pair.of(entry.getKey(), mapper.apply(entry.getValue()))));
    }

    @Override
    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleFunction<? super Map.Entry<K, V>> mapper) {
        return this.entries().mapToDouble(mapper);
    }

    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleBiFunction<? super K, ? super V> mapper) {
        return this.entries().mapToDouble(entry -> mapper.applyAsDouble(entry.getKey(), entry.getValue()));
    }

    @Override
    default @NotNull IntStream mapToInt(@NotNull ToIntFunction<? super Map.Entry<K, V>> mapper) {
        return this.entries().mapToInt(mapper);
    }

    default @NotNull IntStream mapToInt(@NotNull ToIntBiFunction<? super K, ? super V> mapper) {
        return this.entries().mapToInt(entry -> mapper.applyAsInt(entry.getKey(), entry.getValue()));
    }

    @Override
    default @NotNull LongStream mapToLong(@NotNull ToLongFunction<? super Map.Entry<K, V>> mapper) {
        return this.entries().mapToLong(mapper);
    }

    default @NotNull LongStream mapToLong(@NotNull ToLongBiFunction<? super K, ? super V> mapper) {
        return this.entries().mapToLong(entry -> mapper.applyAsLong(entry.getKey(), entry.getValue()));
    }

    // Matching

    @Override
    default boolean allMatch(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return this.entries().allMatch(predicate);
    }

    default boolean allMatch(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return this.entries().allMatch(entry -> predicate.test(entry.getKey(), entry.getValue()));
    }

    @Override
    default boolean anyMatch(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return this.entries().anyMatch(predicate);
    }

    default boolean anyMatch(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return this.entries().anyMatch(entry -> predicate.test(entry.getKey(), entry.getValue()));
    }

    @Override
    default boolean noneMatch(@NotNull Predicate<? super Map.Entry<K, V>> predicate) {
        return this.entries().noneMatch(predicate);
    }

    default boolean noneMatch(@NotNull BiPredicate<? super K, ? super V> predicate) {
        return this.entries().noneMatch(entry -> predicate.test(entry.getKey(), entry.getValue()));
    }

    // Minmax

    @Override
    default @NotNull Optional<Map.Entry<K, V>> max(@NotNull Comparator<? super Map.Entry<K, V>> comparator) {
        return this.entries().max(comparator);
    }

    default @NotNull Optional<Map.Entry<K, V>> maxByKey(@NotNull Comparator<? super K> comparator) {
        return this.entries().max(Map.Entry.comparingByKey(comparator));
    }

    default @NotNull Optional<Map.Entry<K, V>> maxByValue(@NotNull Comparator<? super V> comparator) {
        return this.entries().max(Map.Entry.comparingByValue(comparator));
    }

    @Override
    default @NotNull Optional<Map.Entry<K, V>> min(@NotNull Comparator<? super Map.Entry<K, V>> comparator) {
        return this.entries().min(comparator);
    }

    default @NotNull Optional<Map.Entry<K, V>> minByKey(@NotNull Comparator<? super K> comparator) {
        return this.entries().min(Map.Entry.comparingByKey(comparator));
    }

    default @NotNull Optional<Map.Entry<K, V>> minByValue(@NotNull Comparator<? super V> comparator) {
        return this.entries().min(Map.Entry.comparingByValue(comparator));
    }

    // Order

    @Override
    default boolean isParallel() {
        return this.entries().isParallel();
    }

    @Override
    default @NotNull PairStream<K, V> parallel() {
        return of(this.entries().parallel());
    }

    @Override
    default @NotNull PairStream<K, V> sequential() {
        return of(this.entries().sequential());
    }

    @Override
    default @NotNull PairStream<K, V> unordered() {
        return of(this.entries().unordered());
    }

    // Reduction

    @Override
    default @NotNull Map.Entry<K, V> reduce(@NotNull Map.Entry<K, V> identity, @NotNull BinaryOperator<Map.Entry<K, V>> accumulator) {
        return this.entries().reduce(identity, accumulator);
    }

    @Override
    default @NotNull Optional<Map.Entry<K, V>> reduce(@NotNull BinaryOperator<Map.Entry<K, V>> accumulator) {
        return this.entries().reduce(accumulator);
    }

    @Override
    default <U> @NotNull U reduce(@NotNull U identity, @NotNull BiFunction<U, ? super Map.Entry<K, V>, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        return this.entries().reduce(identity, accumulator, combiner);
    }

    default <U> @NotNull U reduce(@NotNull U identity, @NotNull TriFunction<U, ? super K, ? super V, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        return this.entries().reduce(identity, (u, entry) -> accumulator.apply(u, entry.getKey(), entry.getValue()), combiner);
    }

    // Sorting

    @Override
    default PairStream<K, V> sorted() {
        return of(this.entries().sorted());
    }

    @Override
    default @NotNull PairStream<K, V> sorted(@NotNull Comparator<? super Map.Entry<K, V>> comparator) {
        return of(this.entries().sorted(comparator));
    }

    default @NotNull PairStream<K, V> sortedByKey(@NotNull Comparator<? super K> comparator) {
        return of(this.entries().sorted(Map.Entry.comparingByKey(comparator)));
    }

    default @NotNull PairStream<K, V> sortedByValue(@NotNull Comparator<? super V> comparator) {
        return of(this.entries().sorted(Map.Entry.comparingByValue(comparator)));
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
        return this.entries().collect(collector);
    }

    @Override
    default <R> R collect(@NotNull Supplier<R> supplier, @NotNull BiConsumer<R, ? super Map.Entry<K, V>> accumulator, @NotNull BiConsumer<R, R> combiner) {
        return this.entries().collect(supplier, accumulator, combiner);
    }

    @Override
    default @NotNull Object @NotNull [] toArray() {
        return this.entries().toArray();
    }

    @Override
    default <A> @NotNull A @NotNull [] toArray(@NotNull IntFunction<A[]> generator) {
        return this.entries().toArray(generator);
    }

    default @NotNull ConcurrentMap<K, V> toConcurrentMap() {
        return this.entries().collect(Concurrent.toMap());
    }

    default @NotNull ConcurrentMap<K, V> toConcurrentMap(@NotNull BinaryOperator<V> mergeFunction) {
        return this.entries().collect(Concurrent.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction));
    }

    default <K2, V2> @NotNull ConcurrentMap<K2, V2> toConcurrentMap(Function<? super Map.Entry<K, V>, ? extends K2> keyMapper, Function<? super Map.Entry<K, V>, V2> valueMapper) {
        return this.entries().collect(Concurrent.toMap(keyMapper, valueMapper));
    }

    default <K2, V2> @NotNull ConcurrentMap<K2, V2> toConcurrentMap(Function<? super Map.Entry<K, V>, ? extends K2> keyMapper, Function<? super Map.Entry<K, V>, ? extends V2> valueMapper, BinaryOperator<V2> mergeFunction) {
        return this.entries().collect(Concurrent.toMap(keyMapper, valueMapper, mergeFunction));
    }

    default <K2, V2> @NotNull ConcurrentMap<K2, V2> toConcurrentMap(Function<? super Map.Entry<K, V>, ? extends K2> keyMapper, Function<? super Map.Entry<K, V>, ? extends V2> valueMapper, BinaryOperator<V2> mergeFunction, Supplier<ConcurrentMap<K2, V2>> mapSupplier) {
        return this.entries().collect(Concurrent.toMap(keyMapper, valueMapper, mergeFunction, mapSupplier));
    }

    default @NotNull ConcurrentLinkedMap<K, V> toConcurrentLinkedMap() {
        return this.entries().collect(Concurrent.toLinkedMap());
    }

    default @NotNull ConcurrentLinkedMap<K, V> toConcurrentLinkedMap(@NotNull BinaryOperator<V> mergeFunction) {
        return this.entries().collect(Concurrent.toLinkedMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction));
    }

    default <K2, V2> @NotNull ConcurrentLinkedMap<K2, V2> toConcurrentLinkedMap(Function<? super Map.Entry<K, V>, ? extends K2> keyMapper, Function<? super Map.Entry<K, V>, ? extends V2> valueMapper) {
        return this.entries().collect(Concurrent.toLinkedMap(keyMapper, valueMapper));
    }

    default <K2, V2> @NotNull ConcurrentLinkedMap<K2, V2> toConcurrentLinkedMap(Function<? super Map.Entry<K, V>, ? extends K2> keyMapper, Function<? super Map.Entry<K, V>, ? extends V2> valueMapper, BinaryOperator<V2> mergeFunction) {
        return this.entries().collect(Concurrent.toLinkedMap(keyMapper, valueMapper, mergeFunction));
    }

    default <K2, V2> @NotNull ConcurrentLinkedMap<K2, V2> toConcurrentLinkedMap(Function<? super Map.Entry<K, V>, ? extends K2> keyMapper, Function<? super Map.Entry<K, V>, ? extends V2> valueMapper, BinaryOperator<V2> mergeFunction, Supplier<ConcurrentLinkedMap<K2, V2>> mapSupplier) {
        return this.entries().collect(Concurrent.toLinkedMap(keyMapper, valueMapper, mergeFunction, mapSupplier));
    }

    default @NotNull ConcurrentMap<K, V> toConcurrentUnmodifiableMap() {
        return this.entries().collect(Concurrent.toUnmodifiableMap());
    }

    default @NotNull ConcurrentMap<K, V> toConcurrentUnmodifiableMap(@NotNull BinaryOperator<V> mergeFunction) {
        return this.entries().collect(Concurrent.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction));
    }

    default <K2, V2> @NotNull ConcurrentMap<K2, V2> toConcurrentUnmodifiableMap(Function<? super Map.Entry<K, V>, ? extends K2> keyMapper, Function<? super Map.Entry<K, V>, ? extends V2> valueMapper) {
        return this.entries().collect(Concurrent.toUnmodifiableMap(keyMapper, valueMapper));
    }

    default <K2, V2> @NotNull ConcurrentMap<K2, V2> toConcurrentUnmodifiableMap(Function<? super Map.Entry<K, V>, ? extends K2> keyMapper, Function<? super Map.Entry<K, V>, ? extends V2> valueMapper, BinaryOperator<V2> mergeFunction) {
        return this.entries().collect(Concurrent.toUnmodifiableMap(keyMapper, valueMapper, mergeFunction));
    }

    default <K2, V2> @NotNull ConcurrentMap<K2, V2> toConcurrentUnmodifiableMap(Function<? super Map.Entry<K, V>, ? extends K2> keyMapper, Function<? super Map.Entry<K, V>, ? extends V2> valueMapper, BinaryOperator<V2> mergeFunction, Supplier<ConcurrentMap<K2, V2>> mapSupplier) {
        return this.entries().collect(Concurrent.toUnmodifiableMap(keyMapper, valueMapper, mergeFunction, mapSupplier));
    }

}