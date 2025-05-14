package dev.sbs.api.collection.stream.triple;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.mutable.triple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
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
public interface TripleStream<L, M, R> extends Stream<Triple<L, M, R>> {

    // Create

    static <L, M, R> @NotNull TripleStream<L, M, R> of(@NotNull Stream<Triple<L, M, R>> stream) {
        return () -> stream;
    }

    static <L, M, R> @NotNull TripleStream<L, M, R> of(@NotNull Stream<L> stream, @NotNull Function<? super L, ? extends M> middle, @NotNull Function<? super L, ? extends R> right) {
        return () -> stream.map(left -> Triple.of(left, middle.apply(left), right.apply(left)));
    }

    // Close

    @Override
    @SuppressWarnings("all")
    default @NotNull TripleStream<L, M, R> onClose(@NotNull Runnable closeHandler) {
        return of(this.entries().onClose(closeHandler));
    }

    @Override
    default void close() {
        this.entries().close();
    }

    // Entries

    @NotNull Stream<Triple<L, M, R>> entries();

    default @NotNull Stream<L> lefts() {
        return this.entries().map(Triple::getLeft);
    }

    default @NotNull Stream<M> middles() {
        return this.entries().map(Triple::getMiddle);
    }

    default @NotNull Stream<R> rights() {
        return this.entries().map(Triple::getRight);
    }

    @Override
    default long count() {
        return this.entries().count();
    }

    @Override
    default @NotNull TripleStream<L, M, R> distinct() {
        return of(this.entries().distinct());
    }

    @Override
    default @NotNull TripleStream<L, M, R> limit(long maxSize) {
        return of(this.entries().limit(maxSize));
    }

    @Override
    default @NotNull TripleStream<L, M, R> peek(@NotNull Consumer<? super Triple<L, M, R>> action) {
        return of(this.entries().peek(action));
    }

    default @NotNull TripleStream<L, M, R> peek(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        return of(this.entries().peek(entry -> action.accept(entry.getLeft(), entry.getMiddle(), entry.getRight())));
    }

    @Override
    default @NotNull TripleStream<L, M, R> skip(long number) {
        return of(this.entries().skip(number));
    }

    // Filter

    @Override
    default @NotNull TripleStream<L, M, R> filter(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        return of(this.entries().filter(predicate));
    }

    default @NotNull TripleStream<L, M, R> filter(@NotNull TriPredicate<? super L, ? super M, ? super R> mapper) {
        return of(this.entries().filter(entry -> mapper.test(entry.getLeft(), entry.getMiddle(), entry.getRight())));
    }

    default @NotNull TripleStream<L, M, R> filterLeft(@NotNull Predicate<? super L> mapper) {
        return of(this.entries().filter(entry -> mapper.test(entry.getLeft())));
    }

    default @NotNull TripleStream<L, M, R> filterMiddle(@NotNull Predicate<? super M> mapper) {
        return of(this.entries().filter(entry -> mapper.test(entry.getMiddle())));
    }

    default @NotNull TripleStream<L, M, R> filterRight(@NotNull Predicate<? super R> mapper) {
        return of(this.entries().filter(entry -> mapper.test(entry.getRight())));
    }

    // Find

    @Override
    default @NotNull Optional<Triple<L, M, R>> findAny() {
        return this.entries().findAny();
    }

    @Override
    default @NotNull Optional<Triple<L, M, R>> findFirst() {
        return this.entries().findFirst();
    }

    // Flatmapping

    @Override
    default <RK> Stream<RK> flatMap(Function<? super Triple<L, M, R>, ? extends Stream<? extends RK>> mapper) {
        return this.entries().flatMap(mapper);
    }

    default <RL, RM, RR> @NotNull TripleStream<RL, RM, RR> flatMap(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends TripleStream<RL, RM, RR>> mapper) {
        return of(this.entries().flatMap(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()).entries()));
    }

    default <RT> @NotNull Stream<RT> flatMapToObj(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends Stream<RT>> mapper) {
        return this.entries().flatMap(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull DoubleStream flatMapToDouble(Function<? super Triple<L, M, R>, ? extends DoubleStream> mapper) {
        return this.entries().flatMapToDouble(mapper);
    }

    default @NotNull DoubleStream flatMapToDouble(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends DoubleStream> mapper) {
        return this.entries().flatMapToDouble(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull IntStream flatMapToInt(Function<? super Triple<L, M, R>, ? extends IntStream> mapper) {
        return this.entries().flatMapToInt(mapper);
    }

    default @NotNull IntStream flatMapToInt(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends IntStream> mapper) {
        return this.entries().flatMapToInt(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull LongStream flatMapToLong(Function<? super Triple<L, M, R>, ? extends LongStream> mapper) {
        return this.entries().flatMapToLong(mapper);
    }

    default @NotNull LongStream flatMapToLong(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends LongStream> mapper) {
        return this.entries().flatMapToLong(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    // ForEach

    @Override
    default void forEach(@NotNull Consumer<? super Triple<L, M, R>> action) {
        this.entries().forEach(action);
    }

    default void forEach(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        this.entries().forEach(entry -> action.accept(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default void forEachOrdered(@NotNull Consumer<? super Triple<L, M, R>> action) {
        this.entries().forEachOrdered(action);
    }

    default void forEachOrdered(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        this.entries().forEachOrdered(entry -> action.accept(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    // Iterator

    @Override
    default @NotNull Iterator<Triple<L, M, R>> iterator() {
        return this.entries().iterator();
    }

    @Override
    default @NotNull Spliterator<Triple<L, M, R>> spliterator() {
        return this.entries().spliterator();
    }

    // Mapping

    @Override
    default <RK> @NotNull Stream<RK> map(Function<? super Triple<L, M, R>, ? extends RK> mapper) {
        return this.entries().map(mapper);
    }

    default <T> @NotNull Stream<T> map(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends T> mapper) {
        return this.entries().map(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleFunction<? super Triple<L, M, R>> mapper) {
        return this.entries().mapToDouble(mapper);
    }

    default @NotNull DoubleStream mapToDouble(@NotNull TriFunction<? super L, ? super M, ? super R, Double> mapper) {
        return this.entries().mapToDouble(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull IntStream mapToInt(@NotNull ToIntFunction<? super Triple<L, M, R>> mapper) {
        return this.entries().mapToInt(mapper);
    }

    default @NotNull IntStream mapToInt(@NotNull TriFunction<? super L, ? super M, ? super R, Integer> mapper) {
        return this.entries().mapToInt(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull LongStream mapToLong(@NotNull ToLongFunction<? super Triple<L, M, R>> mapper) {
        return this.entries().mapToLong(mapper);
    }

    default @NotNull LongStream mapToLong(@NotNull TriFunction<? super L, ? super M, ? super R, Long> mapper) {
        return this.entries().mapToLong(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    default <RT> @NotNull TripleStream<RT, M, R> mapLeft(@NotNull Function<? super L, ? extends RT> mapper) {
        return of(this.entries().map(entry -> Triple.of(mapper.apply(entry.getLeft()), entry.getMiddle(), entry.getRight())));
    }

    default <RT> @NotNull TripleStream<L, RT, R> mapMiddle(@NotNull Function<? super M, ? extends RT> mapper) {
        return of(this.entries().map(entry -> Triple.of(entry.getLeft(), mapper.apply(entry.getMiddle()), entry.getRight())));
    }

    default <RT> @NotNull TripleStream<L, M, RT> mapRight(@NotNull Function<? super R, ? extends RT> mapper) {
        return of(this.entries().map(entry -> Triple.of(entry.getLeft(), entry.getMiddle(), mapper.apply(entry.getRight()))));
    }

    // Matching

    @Override
    default boolean allMatch(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        return this.entries().allMatch(predicate);
    }

    default boolean allMatch(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        return this.entries().allMatch(entry -> predicate.test(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default boolean anyMatch(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        return this.entries().anyMatch(predicate);
    }

    default boolean anyMatch(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        return this.entries().anyMatch(entry -> predicate.test(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default boolean noneMatch(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        return this.entries().noneMatch(predicate);
    }

    default boolean noneMatch(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        return this.entries().noneMatch(entry -> predicate.test(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    // Minmax

    @Override
    default @NotNull Optional<Triple<L, M, R>> max(@NotNull Comparator<? super Triple<L, M, R>> comparator) {
        return this.entries().max(comparator);
    }

    default @NotNull Optional<Triple<L, M, R>> maxByLeft(@NotNull Comparator<? super L> comparator) {
        return this.entries().max((c1, c2) -> comparator.compare(c1.getLeft(), c2.getLeft()));
    }

    default @NotNull Optional<Triple<L, M, R>> maxByMiddle(@NotNull Comparator<? super M> comparator) {
        return this.entries().max((c1, c2) -> comparator.compare(c1.getMiddle(), c2.getMiddle()));
    }

    default @NotNull Optional<Triple<L, M, R>> maxByRight(@NotNull Comparator<? super R> comparator) {
        return this.entries().max((c1, c2) -> comparator.compare(c1.getRight(), c2.getRight()));
    }

    @Override
    default @NotNull Optional<Triple<L, M, R>> min(@NotNull Comparator<? super Triple<L, M, R>> comparator) {
        return this.entries().min(comparator);
    }

    default @NotNull Optional<Triple<L, M, R>> minByLeft(@NotNull Comparator<? super L> comparator) {
        return this.entries().min((c1, c2) -> comparator.compare(c1.getLeft(), c2.getLeft()));
    }

    default @NotNull Optional<Triple<L, M, R>> minByMiddle(@NotNull Comparator<? super M> comparator) {
        return this.entries().min((c1, c2) -> comparator.compare(c1.getMiddle(), c2.getMiddle()));
    }

    default @NotNull Optional<Triple<L, M, R>> minByRight(@NotNull Comparator<? super R> comparator) {
        return this.entries().min((c1, c2) -> comparator.compare(c1.getRight(), c2.getRight()));
    }

    // Order

    @Override
    default boolean isParallel() {
        return this.entries().isParallel();
    }

    @Override
    default @NotNull TripleStream<L, M ,R> parallel() {
        return of(this.entries().parallel());
    }

    @Override
    default @NotNull TripleStream<L, M ,R> sequential() {
        return of(this.entries().sequential());
    }

    @Override
    default @NotNull TripleStream<L, M ,R> unordered() {
        return of(this.entries().unordered());
    }

    // Reduction

    @Override
    default @NotNull Triple<L, M, R> reduce(@NotNull Triple<L, M, R> identity, @NotNull BinaryOperator<Triple<L, M, R>> accumulator) {
        return this.entries().reduce(identity, accumulator);
    }

    @Override
    default @NotNull Optional<Triple<L, M, R>> reduce(@NotNull BinaryOperator<Triple<L, M, R>> accumulator) {
        return this.entries().reduce(accumulator);
    }

    @Override
    default <U> @NotNull U reduce(@NotNull U identity, @NotNull BiFunction<U, ? super Triple<L, M, R>, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        return this.entries().reduce(identity, accumulator, combiner);
    }

    // Sorting

    @Override
    default TripleStream<L, M ,R> sorted() {
        return of(this.entries().sorted());
    }

    @Override
    default @NotNull TripleStream<L, M ,R> sorted(@NotNull Comparator<? super Triple<L, M, R>> comparator) {
        return of(this.entries().sorted(comparator));
    }

    default @NotNull TripleStream<L, M ,R> sortedByLeft(@NotNull Comparator<? super L> comparator) {
        return of(this.entries().sorted((c1, c2) -> comparator.compare(c1.getLeft(), c2.getLeft())));
    }

    default @NotNull TripleStream<L, M ,R> sortedByMiddle(@NotNull Comparator<? super M> comparator) {
        return of(this.entries().sorted((c1, c2) -> comparator.compare(c1.getMiddle(), c2.getMiddle())));
    }

    default @NotNull TripleStream<L, M ,R> sortedByRight(@NotNull Comparator<? super R> comparator) {
        return of(this.entries().sorted((c1, c2) -> comparator.compare(c1.getRight(), c2.getRight())));
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
     * @param <T> the type of the result
     * @param <A> the intermediate accumulation type of the {@code Collector}
     * @param collector the {@code Collector} describing the reduction
     * @return the result of the reduction
     * @see Stream#collect(Supplier, BiConsumer, BiConsumer)
     * @see Collectors
     */
    default <T, A> T collect(@NotNull Collector<? super Triple<L, M, R>, A, T> collector) {
        return this.entries().collect(collector);
    }

    @Override
    default <T> T collect(@NotNull Supplier<T> supplier, @NotNull BiConsumer<T, ? super Triple<L, M, R>> accumulator, @NotNull BiConsumer<T, T> combiner) {
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

    default @NotNull ConcurrentList<Triple<L, M, R>> toConcurrentList() {
        return this.entries().collect(Concurrent.toList());
    }

    default @NotNull ConcurrentList<Triple<L, M, R>> toConcurrentUnmodifiableList() {
        return this.entries().collect(Concurrent.toUnmodifiableList());
    }

}