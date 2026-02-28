package dev.sbs.api.tuple.triple;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
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

    static <L, M, R> @NotNull TripleStream<L, M, R> empty() {
        return of(Stream.empty());
    }

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
        return of(this.underlying().onClose(closeHandler));
    }

    @Override
    default void close() {
        this.underlying().close();
    }

    // Entries

    @NotNull Stream<Triple<L, M, R>> underlying();

    default @NotNull Stream<L> lefts() {
        return this.underlying().map(Triple::getLeft);
    }

    default @NotNull Stream<M> middles() {
        return this.underlying().map(Triple::getMiddle);
    }

    default @NotNull Stream<R> rights() {
        return this.underlying().map(Triple::getRight);
    }

    @Override
    default long count() {
        return this.underlying().count();
    }

    @Override
    default @NotNull TripleStream<L, M, R> distinct() {
        return of(this.underlying().distinct());
    }

    @Override
    default @NotNull TripleStream<L, M, R> limit(long maxSize) {
        return of(this.underlying().limit(maxSize));
    }

    @Override
    default @NotNull TripleStream<L, M, R> peek(@NotNull Consumer<? super Triple<L, M, R>> action) {
        return of(this.underlying().peek(action));
    }

    default @NotNull TripleStream<L, M, R> peek(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        return of(this.underlying().peek(entry -> action.accept(entry.getLeft(), entry.getMiddle(), entry.getRight())));
    }

    @SuppressWarnings("all")
    default @NotNull TripleStream<L, M, R> with(@NotNull Consumer<? super Triple<L, M, R>> action) {
        return of(this.underlying().map(entry -> {
            action.accept(entry);
            return entry;
        }));
    }

    @SuppressWarnings("all")
    default @NotNull TripleStream<L, M, R> with(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        return of(this.underlying().map(entry -> {
            action.accept(entry.getLeft(), entry.getMiddle(), entry.getRight());
            return entry;
        }));
    }

    @Override
    default @NotNull TripleStream<L, M, R> skip(long number) {
        return of(this.underlying().skip(number));
    }

    // Filter

    @Override
    default @NotNull TripleStream<L, M, R> filter(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        return of(this.underlying().filter(predicate));
    }

    default @NotNull TripleStream<L, M, R> filter(@NotNull TriPredicate<? super L, ? super M, ? super R> mapper) {
        return of(this.underlying().filter(entry -> mapper.test(entry.getLeft(), entry.getMiddle(), entry.getRight())));
    }

    default @NotNull TripleStream<L, M, R> filterLeft(@NotNull Predicate<? super L> mapper) {
        return of(this.underlying().filter(entry -> mapper.test(entry.getLeft())));
    }

    default @NotNull TripleStream<L, M, R> filterMiddle(@NotNull Predicate<? super M> mapper) {
        return of(this.underlying().filter(entry -> mapper.test(entry.getMiddle())));
    }

    default @NotNull TripleStream<L, M, R> filterRight(@NotNull Predicate<? super R> mapper) {
        return of(this.underlying().filter(entry -> mapper.test(entry.getRight())));
    }

    // Find

    @Override
    default @NotNull Optional<Triple<L, M, R>> findAny() {
        return this.underlying().findAny();
    }

    @Override
    default @NotNull Optional<Triple<L, M, R>> findFirst() {
        return this.underlying().findFirst();
    }

    // Flatmapping

    @Override
    default <RK> Stream<RK> flatMap(Function<? super Triple<L, M, R>, ? extends Stream<? extends RK>> mapper) {
        return this.underlying().flatMap(mapper);
    }

    default <RL, RM, RR> @NotNull TripleStream<RL, RM, RR> flatMap(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends TripleStream<RL, RM, RR>> mapper) {
        return of(this.underlying().flatMap(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()).underlying()));
    }

    default <RT> @NotNull Stream<RT> flatMapToObj(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends Stream<RT>> mapper) {
        return this.underlying().flatMap(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull DoubleStream flatMapToDouble(Function<? super Triple<L, M, R>, ? extends DoubleStream> mapper) {
        return this.underlying().flatMapToDouble(mapper);
    }

    default @NotNull DoubleStream flatMapToDouble(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends DoubleStream> mapper) {
        return this.underlying().flatMapToDouble(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull IntStream flatMapToInt(Function<? super Triple<L, M, R>, ? extends IntStream> mapper) {
        return this.underlying().flatMapToInt(mapper);
    }

    default @NotNull IntStream flatMapToInt(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends IntStream> mapper) {
        return this.underlying().flatMapToInt(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull LongStream flatMapToLong(Function<? super Triple<L, M, R>, ? extends LongStream> mapper) {
        return this.underlying().flatMapToLong(mapper);
    }

    default @NotNull LongStream flatMapToLong(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends LongStream> mapper) {
        return this.underlying().flatMapToLong(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    // ForEach

    @Override
    default void forEach(@NotNull Consumer<? super Triple<L, M, R>> action) {
        this.underlying().forEach(action);
    }

    default void forEach(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        this.underlying().forEach(entry -> action.accept(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default void forEachOrdered(@NotNull Consumer<? super Triple<L, M, R>> action) {
        this.underlying().forEachOrdered(action);
    }

    default void forEachOrdered(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        this.underlying().forEachOrdered(entry -> action.accept(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    // Iterator

    @Override
    default @NotNull Iterator<Triple<L, M, R>> iterator() {
        return this.underlying().iterator();
    }

    @Override
    default @NotNull Spliterator<Triple<L, M, R>> spliterator() {
        return this.underlying().spliterator();
    }

    // Mapping

    @Override
    default <RK> @NotNull Stream<RK> map(Function<? super Triple<L, M, R>, ? extends RK> mapper) {
        return this.underlying().map(mapper);
    }

    default <T> @NotNull Stream<T> mapToObj(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends T> mapper) {
        return this.underlying().map(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    default <RL, RM, RR> @NotNull TripleStream<RL, RM, RR> map(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends Triple<RL, RM, RR>> mapper) {
        return of(this.underlying().map(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight())));
    }

    @Override
    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleFunction<? super Triple<L, M, R>> mapper) {
        return this.underlying().mapToDouble(mapper);
    }

    default @NotNull DoubleStream mapToDouble(@NotNull TriFunction<? super L, ? super M, ? super R, Double> mapper) {
        return this.underlying().mapToDouble(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull IntStream mapToInt(@NotNull ToIntFunction<? super Triple<L, M, R>> mapper) {
        return this.underlying().mapToInt(mapper);
    }

    default @NotNull IntStream mapToInt(@NotNull TriFunction<? super L, ? super M, ? super R, Integer> mapper) {
        return this.underlying().mapToInt(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default @NotNull LongStream mapToLong(@NotNull ToLongFunction<? super Triple<L, M, R>> mapper) {
        return this.underlying().mapToLong(mapper);
    }

    default @NotNull LongStream mapToLong(@NotNull TriFunction<? super L, ? super M, ? super R, Long> mapper) {
        return this.underlying().mapToLong(entry -> mapper.apply(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    default <RT> @NotNull TripleStream<RT, M, R> mapLeft(@NotNull Function<? super L, ? extends RT> mapper) {
        return of(this.underlying().map(entry -> Triple.of(mapper.apply(entry.getLeft()), entry.getMiddle(), entry.getRight())));
    }

    default <RT> @NotNull TripleStream<L, RT, R> mapMiddle(@NotNull Function<? super M, ? extends RT> mapper) {
        return of(this.underlying().map(entry -> Triple.of(entry.getLeft(), mapper.apply(entry.getMiddle()), entry.getRight())));
    }

    default <RT> @NotNull TripleStream<L, M, RT> mapRight(@NotNull Function<? super R, ? extends RT> mapper) {
        return of(this.underlying().map(entry -> Triple.of(entry.getLeft(), entry.getMiddle(), mapper.apply(entry.getRight()))));
    }

    // Matching

    @Override
    default boolean allMatch(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        return this.underlying().allMatch(predicate);
    }

    default boolean allMatch(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        return this.underlying().allMatch(entry -> predicate.test(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default boolean anyMatch(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        return this.underlying().anyMatch(predicate);
    }

    default boolean anyMatch(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        return this.underlying().anyMatch(entry -> predicate.test(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    @Override
    default boolean noneMatch(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        return this.underlying().noneMatch(predicate);
    }

    default boolean noneMatch(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        return this.underlying().noneMatch(entry -> predicate.test(entry.getLeft(), entry.getMiddle(), entry.getRight()));
    }

    // Minmax

    @Override
    default @NotNull Optional<Triple<L, M, R>> max(@NotNull Comparator<? super Triple<L, M, R>> comparator) {
        return this.underlying().max(comparator);
    }

    default @NotNull Optional<Triple<L, M, R>> maxByLeft(@NotNull Comparator<? super L> comparator) {
        return this.underlying().max((c1, c2) -> comparator.compare(c1.getLeft(), c2.getLeft()));
    }

    default @NotNull Optional<Triple<L, M, R>> maxByMiddle(@NotNull Comparator<? super M> comparator) {
        return this.underlying().max((c1, c2) -> comparator.compare(c1.getMiddle(), c2.getMiddle()));
    }

    default @NotNull Optional<Triple<L, M, R>> maxByRight(@NotNull Comparator<? super R> comparator) {
        return this.underlying().max((c1, c2) -> comparator.compare(c1.getRight(), c2.getRight()));
    }

    @Override
    default @NotNull Optional<Triple<L, M, R>> min(@NotNull Comparator<? super Triple<L, M, R>> comparator) {
        return this.underlying().min(comparator);
    }

    default @NotNull Optional<Triple<L, M, R>> minByLeft(@NotNull Comparator<? super L> comparator) {
        return this.underlying().min((c1, c2) -> comparator.compare(c1.getLeft(), c2.getLeft()));
    }

    default @NotNull Optional<Triple<L, M, R>> minByMiddle(@NotNull Comparator<? super M> comparator) {
        return this.underlying().min((c1, c2) -> comparator.compare(c1.getMiddle(), c2.getMiddle()));
    }

    default @NotNull Optional<Triple<L, M, R>> minByRight(@NotNull Comparator<? super R> comparator) {
        return this.underlying().min((c1, c2) -> comparator.compare(c1.getRight(), c2.getRight()));
    }

    // Order

    @Override
    default boolean isParallel() {
        return this.underlying().isParallel();
    }

    @Override
    default @NotNull TripleStream<L, M ,R> parallel() {
        return of(this.underlying().parallel());
    }

    @Override
    default @NotNull TripleStream<L, M ,R> sequential() {
        return of(this.underlying().sequential());
    }

    @Override
    default @NotNull TripleStream<L, M ,R> unordered() {
        return of(this.underlying().unordered());
    }

    // Reduction

    @Override
    default @NotNull Triple<L, M, R> reduce(@NotNull Triple<L, M, R> identity, @NotNull BinaryOperator<Triple<L, M, R>> accumulator) {
        return this.underlying().reduce(identity, accumulator);
    }

    @Override
    default @NotNull Optional<Triple<L, M, R>> reduce(@NotNull BinaryOperator<Triple<L, M, R>> accumulator) {
        return this.underlying().reduce(accumulator);
    }

    @Override
    default <U> @NotNull U reduce(@NotNull U identity, @NotNull BiFunction<U, ? super Triple<L, M, R>, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        return this.underlying().reduce(identity, accumulator, combiner);
    }

    // Sorting

    @Override
    default TripleStream<L, M ,R> sorted() {
        return of(this.underlying().sorted());
    }

    @Override
    default @NotNull TripleStream<L, M ,R> sorted(@NotNull Comparator<? super Triple<L, M, R>> comparator) {
        return of(this.underlying().sorted(comparator));
    }

    default @NotNull TripleStream<L, M ,R> sortedByLeft(@NotNull Comparator<? super L> comparator) {
        return of(this.underlying().sorted((c1, c2) -> comparator.compare(c1.getLeft(), c2.getLeft())));
    }

    default @NotNull TripleStream<L, M ,R> sortedByMiddle(@NotNull Comparator<? super M> comparator) {
        return of(this.underlying().sorted((c1, c2) -> comparator.compare(c1.getMiddle(), c2.getMiddle())));
    }

    default @NotNull TripleStream<L, M ,R> sortedByRight(@NotNull Comparator<? super R> comparator) {
        return of(this.underlying().sorted((c1, c2) -> comparator.compare(c1.getRight(), c2.getRight())));
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
    @Override
    default <T, A> T collect(@NotNull Collector<? super Triple<L, M, R>, A, T> collector) {
        return this.underlying().collect(collector);
    }

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this stream.  A mutable
     * reduction is one in which the reduced value is a mutable result container,
     * such as an {@code ArrayList}, and elements are incorporated by updating
     * the state of the result rather than by replacing the result.  This
     * produces a result equivalent to:
     * <pre>{@code
     *     R result = supplier.get();
     *     for (T element : this stream)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     *
     * <p>Like {@link Stream#reduce(Object, BinaryOperator)}, {@code collect} operations
     * can be parallelized without requiring additional synchronization.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @apiNote There are many existing classes in the JDK whose signatures are
     * well-suited for use with method references as arguments to {@code collect()}.
     * For example, the following will accumulate strings into an {@code ArrayList}:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(ArrayList::new, ArrayList::add,
     *                                                ArrayList::addAll);
     * }</pre>
     *
     * <p>The following will take a stream of strings and concatenates them into a
     * single string:
     * <pre>{@code
     *     String concat = stringStream.collect(StringBuilder::new, StringBuilder::append,
     *                                          StringBuilder::append)
     *                                 .toString();
     * }</pre>
     *
     * @param <T> the type of the mutable result container
     * @param supplier a function that creates a new mutable result container.
     *                 For a parallel execution, this function may be called
     *                 multiple times and must return a fresh value each time.
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function that must fold an element into a result
     *                    container.
     * @param combiner an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function that accepts two partial result containers
     *                    and merges them, which must be compatible with the
     *                    accumulator function.  The combiner function must fold
     *                    the elements from the second result container into the
     *                    first result container.
     * @return the result of the reduction
     */
    @Override
    default <T> T collect(@NotNull Supplier<T> supplier, @NotNull BiConsumer<T, ? super Triple<L, M, R>> accumulator, @NotNull BiConsumer<T, T> combiner) {
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

    default @NotNull ConcurrentList<Triple<L, M, R>> toConcurrentList() {
        return this.underlying().collect(Concurrent.toList());
    }

    default @NotNull ConcurrentList<Triple<L, M, R>> toConcurrentUnmodifiableList() {
        return this.underlying().collect(Concurrent.toUnmodifiableList());
    }

}