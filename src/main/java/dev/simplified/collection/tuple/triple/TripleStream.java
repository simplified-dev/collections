package dev.simplified.collection.tuple.triple;

import dev.simplified.collection.function.QuadFunction;
import dev.simplified.collection.function.ToDoubleTriFunction;
import dev.simplified.collection.function.ToIntTriFunction;
import dev.simplified.collection.function.ToLongTriFunction;
import dev.simplified.collection.function.TriConsumer;
import dev.simplified.collection.function.TriFunction;
import dev.simplified.collection.function.TriPredicate;
import dev.simplified.collection.tuple.pair.Pair;
import dev.simplified.collection.tuple.pair.PairStream;
import dev.simplified.collection.tuple.single.SingleStream;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A specialized stream interface that operates on {@link Triple} objects.
 * <p>
 * Provides manipulation of a triple's {@code left}, {@code middle}, and {@code right} elements,
 * enabling transformation, filtering, mapping, and reduction specifically tailored to
 * {@link Triple} objects and their elements.
 * <p>
 * {@code TripleStream} is constructed from a {@link Stream} of triples, or a stream of left
 * elements combined with mapping functions, and maintains standard compatibility with
 * {@link Stream} operations.
 * <p>
 * Sits above {@link PairStream} (2 elements) and {@link SingleStream} (1 element)
 * in the tuple stream hierarchy.
 *
 * @param <L> the type of the left element
 * @param <M> the type of the middle element
 * @param <R> the type of the right element
 */
@FunctionalInterface
public interface TripleStream<L, M, R> extends SingleStream<Triple<L, M, R>> {

    // Create

    /**
     * Returns an empty {@code TripleStream}.
     *
     * @param <L> the left element type
     * @param <M> the middle element type
     * @param <R> the right element type
     * @return an empty {@code TripleStream}
     */
    static <L, M, R> @NotNull TripleStream<L, M, R> empty() {
        return of(Stream.empty());
    }

    /**
     * Creates a {@code TripleStream} wrapping the given stream of {@link Triple} objects.
     *
     * @param <L>    the left element type
     * @param <M>    the middle element type
     * @param <R>    the right element type
     * @param stream the stream of triples to wrap
     * @return a {@code TripleStream} backed by {@code stream}
     */
    static <L, M, R> @NotNull TripleStream<L, M, R> of(@NotNull Stream<Triple<L, M, R>> stream) {
        return () -> stream;
    }

    /**
     * Creates a {@code TripleStream} from a stream of left elements by applying {@code middle}
     * and {@code right} to derive the other two elements of each triple.
     *
     * @param <L>    the left element type
     * @param <M>    the middle element type
     * @param <R>    the right element type
     * @param stream a stream of left elements
     * @param middle a function producing the middle element from each left element
     * @param right a function producing the right element from each left element
     * @return a {@code TripleStream} of constructed triples
     */
    static <L, M, R> @NotNull TripleStream<L, M, R> of(@NotNull Stream<L> stream, @NotNull Function<? super L, ? extends M> middle, @NotNull Function<? super L, ? extends R> right) {
        return () -> stream.map(left -> Triple.of(left, middle.apply(left), right.apply(left)));
    }

    // Close

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("all")
    default @NotNull TripleStream<L, M, R> onClose(@NotNull Runnable closeHandler) {
        return of(this.underlying().onClose(closeHandler));
    }

    /** {@inheritDoc} */
    @Override
    default void close() {
        this.underlying().close();
    }

    // Entries

    /**
     * Returns the underlying {@link Stream} of {@link Triple} objects that backs
     * this {@code TripleStream}.
     *
     * @return the underlying triple stream
     */
    @NotNull Stream<Triple<L, M, R>> underlying();

    /**
     * Returns a {@link SingleStream} of all left elements in this stream, discarding middle and right.
     *
     * @return a {@code SingleStream} of left elements
     */
    default @NotNull SingleStream<L> lefts() {
        return SingleStream.of(this.underlying().map(Triple::left));
    }

    /**
     * Returns a {@link SingleStream} of all middle elements in this stream, discarding left and right.
     *
     * @return a {@code SingleStream} of middle elements
     */
    default @NotNull SingleStream<M> middles() {
        return SingleStream.of(this.underlying().map(Triple::middle));
    }

    /**
     * Returns a {@link SingleStream} of all right elements in this stream, discarding left and middle.
     *
     * @return a {@code SingleStream} of right elements
     */
    default @NotNull SingleStream<R> rights() {
        return SingleStream.of(this.underlying().map(Triple::right));
    }

    /** {@inheritDoc} */
    @Override
    default long count() {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.count();
        }
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull TripleStream<L, M, R> distinct() {
        return of(this.underlying().distinct());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull TripleStream<L, M, R> limit(long maxSize) {
        return of(this.underlying().limit(maxSize));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull TripleStream<L, M, R> peek(@NotNull Consumer<? super Triple<L, M, R>> action) {
        return of(this.underlying().peek(action));
    }

    /**
     * Returns a {@code TripleStream} that additionally performs the given action on each triple
     * as triples are consumed, supplying the left, middle, and right elements separately.
     *
     * @param action a {@link TriConsumer} receiving each triple's elements
     * @return this stream with the peek action attached
     */
    default @NotNull TripleStream<L, M, R> peek(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        return of(this.underlying().peek(entry -> action.accept(entry.left(), entry.middle(), entry.right())));
    }

    /**
     * Returns a {@code TripleStream} that performs the given action on each triple as a
     * side effect and then passes the triple through unchanged.
     * <p>
     * Unlike {@link #peek(Consumer)}, which is an intermediate stream operation, this method
     * uses {@code map} internally and guarantees the action is called even in lazy pipelines.
     *
     * @param action a {@link Consumer} to apply to each triple
     * @return this stream with the side-effect action attached
     */
    @SuppressWarnings("all")
    default @NotNull TripleStream<L, M, R> with(@NotNull Consumer<? super Triple<L, M, R>> action) {
        return of(this.underlying().map(entry -> {
            action.accept(entry);
            return entry;
        }));
    }

    /**
     * Returns a {@code TripleStream} that performs the given action on each triple as a
     * side effect and then passes the triple through unchanged, supplying the left, middle,
     * and right elements separately.
     * <p>
     * Unlike {@link #peek(TriConsumer)}, which is an intermediate stream operation, this method
     * uses {@code map} internally and guarantees the action is called even in lazy pipelines.
     *
     * @param action a {@link TriConsumer} receiving each triple's elements
     * @return this stream with the side-effect action attached
     */
    @SuppressWarnings("all")
    default @NotNull TripleStream<L, M, R> with(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        return of(this.underlying().map(entry -> {
            action.accept(entry.left(), entry.middle(), entry.right());
            return entry;
        }));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull TripleStream<L, M, R> skip(long number) {
        return of(this.underlying().skip(number));
    }

    // Filter

    /** {@inheritDoc} */
    @Override
    default @NotNull TripleStream<L, M, R> filter(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        return of(this.underlying().filter(predicate));
    }

    /**
     * Returns a {@code TripleStream} containing only triples for which the given
     * {@link TriPredicate} returns {@code true} when supplied the left, middle, and right elements.
     *
     * @param predicate a predicate receiving each triple's elements
     * @return a filtered {@code TripleStream}
     */
    default @NotNull TripleStream<L, M, R> filter(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.left(), entry.middle(), entry.right())));
    }

    /**
     * Returns a {@code TripleStream} containing only triples whose left element matches
     * the given predicate.
     *
     * @param predicate a predicate to test each triple's left element
     * @return a filtered {@code TripleStream}
     */
    default @NotNull TripleStream<L, M, R> filterLeft(@NotNull Predicate<? super L> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.left())));
    }

    /**
     * Returns a {@code TripleStream} containing only triples whose middle element matches
     * the given predicate.
     *
     * @param predicate a predicate to test each triple's middle element
     * @return a filtered {@code TripleStream}
     */
    default @NotNull TripleStream<L, M, R> filterMiddle(@NotNull Predicate<? super M> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.middle())));
    }

    /**
     * Returns a {@code TripleStream} containing only triples whose right element matches
     * the given predicate.
     *
     * @param predicate a predicate to test each triple's right element
     * @return a filtered {@code TripleStream}
     */
    default @NotNull TripleStream<L, M, R> filterRight(@NotNull Predicate<? super R> predicate) {
        return of(this.underlying().filter(entry -> predicate.test(entry.right())));
    }

    // Find

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Triple<L, M, R>> findAny() {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.findAny();
        }
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Triple<L, M, R>> findFirst() {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.findFirst();
        }
    }

    // Flatmapping

    /** {@inheritDoc} */
    @Override
    default <RK> @NotNull SingleStream<RK> flatMap(@NotNull Function<? super Triple<L, M, R>, ? extends Stream<? extends RK>> mapper) {
        return SingleStream.of(this.underlying().flatMap(mapper));
    }

    /**
     * Returns a {@code TripleStream} by replacing each triple with the contents of a
     * {@code TripleStream} produced by {@code mapper}, which receives the left, middle,
     * and right elements separately.
     *
     * @param <RL>   the left type of the resulting stream
     * @param <RM>   the middle type of the resulting stream
     * @param <RR>   the right type of the resulting stream
     * @param mapper a function receiving each triple's elements, returning a {@code TripleStream}
     * @return a flat-mapped {@code TripleStream}
     */
    default <RL, RM, RR> @NotNull TripleStream<RL, RM, RR> flatMap(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends TripleStream<RL, RM, RR>> mapper) {
        return of(this.underlying().flatMap(entry -> mapper.apply(entry.left(), entry.middle(), entry.right()).underlying()));
    }

    /**
     * Returns a {@link SingleStream} by replacing each triple with the contents of a stream
     * produced by {@code mapper}, which receives the left, middle, and right elements separately.
     *
     * @param <RT>   the element type of the resulting stream
     * @param mapper a function receiving each triple's elements, returning a stream of results
     * @return a flat-mapped {@link SingleStream}
     */
    default <RT> @NotNull SingleStream<RT> flatMapToObj(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends Stream<RT>> mapper) {
        return SingleStream.of(this.underlying().flatMap(entry -> mapper.apply(entry.left(), entry.middle(), entry.right())));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull DoubleStream flatMapToDouble(@NotNull Function<? super Triple<L, M, R>, ? extends DoubleStream> mapper) {
        return this.underlying().flatMapToDouble(mapper);
    }

    /**
     * Returns a {@link DoubleStream} by replacing each triple with the contents of a
     * {@code DoubleStream} produced by {@code mapper}, which receives the left, middle,
     * and right elements separately.
     *
     * @param mapper a function receiving each triple's elements, returning a {@code DoubleStream}
     * @return a flat-mapped {@code DoubleStream}
     */
    default @NotNull DoubleStream flatMapToDouble(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends DoubleStream> mapper) {
        return this.underlying().flatMapToDouble(entry -> mapper.apply(entry.left(), entry.middle(), entry.right()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull IntStream flatMapToInt(@NotNull Function<? super Triple<L, M, R>, ? extends IntStream> mapper) {
        return this.underlying().flatMapToInt(mapper);
    }

    /**
     * Returns an {@link IntStream} by replacing each triple with the contents of an
     * {@code IntStream} produced by {@code mapper}, which receives the left, middle,
     * and right elements separately.
     *
     * @param mapper a function receiving each triple's elements, returning an {@code IntStream}
     * @return a flat-mapped {@code IntStream}
     */
    default @NotNull IntStream flatMapToInt(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends IntStream> mapper) {
        return this.underlying().flatMapToInt(entry -> mapper.apply(entry.left(), entry.middle(), entry.right()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull LongStream flatMapToLong(@NotNull Function<? super Triple<L, M, R>, ? extends LongStream> mapper) {
        return this.underlying().flatMapToLong(mapper);
    }

    /**
     * Returns a {@link LongStream} by replacing each triple with the contents of a
     * {@code LongStream} produced by {@code mapper}, which receives the left, middle,
     * and right elements separately.
     *
     * @param mapper a function receiving each triple's elements, returning a {@code LongStream}
     * @return a flat-mapped {@code LongStream}
     */
    default @NotNull LongStream flatMapToLong(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends LongStream> mapper) {
        return this.underlying().flatMapToLong(entry -> mapper.apply(entry.left(), entry.middle(), entry.right()));
    }

    // ForEach

    /** {@inheritDoc} */
    @Override
    default void forEach(@NotNull Consumer<? super Triple<L, M, R>> action) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            s.forEach(action);
        }
    }

    /**
     * Performs {@code action} for each triple in this stream, supplying the left, middle,
     * and right elements separately. The encounter order is not guaranteed for parallel streams.
     *
     * @param action a {@link TriConsumer} receiving each triple's elements
     */
    default void forEach(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            s.forEach(entry -> action.accept(entry.left(), entry.middle(), entry.right()));
        }
    }

    /** {@inheritDoc} */
    @Override
    default void forEachOrdered(@NotNull Consumer<? super Triple<L, M, R>> action) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            s.forEachOrdered(action);
        }
    }

    /**
     * Performs {@code action} for each triple in this stream in encounter order,
     * supplying the left, middle, and right elements separately.
     *
     * @param action a {@link TriConsumer} receiving each triple's elements
     */
    default void forEachOrdered(@NotNull TriConsumer<? super L, ? super M, ? super R> action) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            s.forEachOrdered(entry -> action.accept(entry.left(), entry.middle(), entry.right()));
        }
    }

    // Iterator

    /** {@inheritDoc} */
    @Override
    default @NotNull Iterator<Triple<L, M, R>> iterator() {
        return this.underlying().iterator();
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Spliterator<Triple<L, M, R>> spliterator() {
        return this.underlying().spliterator();
    }

    // Mapping

    /** {@inheritDoc} */
    @Override
    default <RK> @NotNull SingleStream<RK> map(@NotNull Function<? super Triple<L, M, R>, ? extends RK> mapper) {
        return SingleStream.of(this.underlying().map(mapper));
    }

    /**
     * Returns a {@code TripleStream} by applying {@code mapper} to the left, middle, and
     * right elements of each triple, producing a new {@link Triple} for each.
     *
     * @param <RL>   the left type of the resulting stream
     * @param <RM>   the middle type of the resulting stream
     * @param <RR>   the right type of the resulting stream
     * @param mapper a function receiving each triple's elements, returning a new triple
     * @return a mapped {@code TripleStream}
     */
    default <RL, RM, RR> @NotNull TripleStream<RL, RM, RR> map(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends Triple<RL, RM, RR>> mapper) {
        return of(this.underlying().map(entry -> mapper.apply(entry.left(), entry.middle(), entry.right())));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleFunction<? super Triple<L, M, R>> mapper) {
        return this.underlying().mapToDouble(mapper);
    }

    /**
     * Returns a {@link DoubleStream} by applying {@code mapper} to the left, middle, and
     * right elements of each triple, with no boxing of the primitive result.
     *
     * @param mapper a function receiving each triple's elements, returning a {@code double}
     * @return a mapped {@code DoubleStream}
     */
    default @NotNull DoubleStream mapToDouble(@NotNull ToDoubleTriFunction<? super L, ? super M, ? super R> mapper) {
        return this.underlying().mapToDouble(entry -> mapper.applyAsDouble(entry.left(), entry.middle(), entry.right()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull IntStream mapToInt(@NotNull ToIntFunction<? super Triple<L, M, R>> mapper) {
        return this.underlying().mapToInt(mapper);
    }

    /**
     * Returns an {@link IntStream} by applying {@code mapper} to the left, middle, and
     * right elements of each triple, with no boxing of the primitive result.
     *
     * @param mapper a function receiving each triple's elements, returning an {@code int}
     * @return a mapped {@code IntStream}
     */
    default @NotNull IntStream mapToInt(@NotNull ToIntTriFunction<? super L, ? super M, ? super R> mapper) {
        return this.underlying().mapToInt(entry -> mapper.applyAsInt(entry.left(), entry.middle(), entry.right()));
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull LongStream mapToLong(@NotNull ToLongFunction<? super Triple<L, M, R>> mapper) {
        return this.underlying().mapToLong(mapper);
    }

    /**
     * Returns a {@link LongStream} by applying {@code mapper} to the left, middle, and
     * right elements of each triple, with no boxing of the primitive result.
     *
     * @param mapper a function receiving each triple's elements, returning a {@code long}
     * @return a mapped {@code LongStream}
     */
    default @NotNull LongStream mapToLong(@NotNull ToLongTriFunction<? super L, ? super M, ? super R> mapper) {
        return this.underlying().mapToLong(entry -> mapper.applyAsLong(entry.left(), entry.middle(), entry.right()));
    }

    /**
     * Returns a {@code TripleStream} with each left element replaced by the result of
     * applying {@code mapper} to it, leaving middle and right elements unchanged.
     *
     * @param <RT>   the new left element type
     * @param mapper a function to apply to each left element
     * @return a {@code TripleStream} with transformed left elements
     */
    default <RT> @NotNull TripleStream<RT, M, R> mapLeft(@NotNull Function<? super L, ? extends RT> mapper) {
        return of(this.underlying().map(entry -> Triple.of(mapper.apply(entry.left()), entry.middle(), entry.right())));
    }

    /**
     * Returns a {@code TripleStream} with each middle element replaced by the result of
     * applying {@code mapper} to it, leaving left and right elements unchanged.
     *
     * @param <RT>   the new middle element type
     * @param mapper a function to apply to each middle element
     * @return a {@code TripleStream} with transformed middle elements
     */
    default <RT> @NotNull TripleStream<L, RT, R> mapMiddle(@NotNull Function<? super M, ? extends RT> mapper) {
        return of(this.underlying().map(entry -> Triple.of(entry.left(), mapper.apply(entry.middle()), entry.right())));
    }

    /**
     * Returns a {@code TripleStream} with each right element replaced by the result of
     * applying {@code mapper} to it, leaving left and middle elements unchanged.
     *
     * @param <RT>   the new right element type
     * @param mapper a function to apply to each right element
     * @return a {@code TripleStream} with transformed right elements
     */
    default <RT> @NotNull TripleStream<L, M, RT> mapRight(@NotNull Function<? super R, ? extends RT> mapper) {
        return of(this.underlying().map(entry -> Triple.of(entry.left(), entry.middle(), mapper.apply(entry.right()))));
    }

    // Matching

    /** {@inheritDoc} */
    @Override
    default boolean allMatch(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.allMatch(predicate);
        }
    }

    /**
     * Returns whether all triples match the given {@link TriPredicate} when supplied
     * the left, middle, and right elements of each triple.
     *
     * @param predicate a predicate receiving each triple's elements
     * @return {@code true} if all triples match, {@code false} otherwise
     */
    default boolean allMatch(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.allMatch(entry -> predicate.test(entry.left(), entry.middle(), entry.right()));
        }
    }

    /** {@inheritDoc} */
    @Override
    default boolean anyMatch(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.anyMatch(predicate);
        }
    }

    /**
     * Returns whether any triple matches the given {@link TriPredicate} when supplied
     * the left, middle, and right elements of each triple.
     *
     * @param predicate a predicate receiving each triple's elements
     * @return {@code true} if any triple matches, {@code false} otherwise
     */
    default boolean anyMatch(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.anyMatch(entry -> predicate.test(entry.left(), entry.middle(), entry.right()));
        }
    }

    /** {@inheritDoc} */
    @Override
    default boolean noneMatch(@NotNull Predicate<? super Triple<L, M, R>> predicate) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.noneMatch(predicate);
        }
    }

    /**
     * Returns whether no triples match the given {@link TriPredicate} when supplied
     * the left, middle, and right elements of each triple.
     *
     * @param predicate a predicate receiving each triple's elements
     * @return {@code true} if no triples match, {@code false} otherwise
     */
    default boolean noneMatch(@NotNull TriPredicate<? super L, ? super M, ? super R> predicate) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.noneMatch(entry -> predicate.test(entry.left(), entry.middle(), entry.right()));
        }
    }

    // Minmax

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Triple<L, M, R>> max(@NotNull Comparator<? super Triple<L, M, R>> comparator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.max(comparator);
        }
    }

    /**
     * Returns the maximum triple according to the given left-element comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare left elements
     * @return an {@code Optional} of the triple with the maximum left element
     */
    default @NotNull Optional<Triple<L, M, R>> maxByLeft(@NotNull Comparator<? super L> comparator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.max((c1, c2) -> comparator.compare(c1.left(), c2.left()));
        }
    }

    /**
     * Returns the maximum triple according to the given middle-element comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare middle elements
     * @return an {@code Optional} of the triple with the maximum middle element
     */
    default @NotNull Optional<Triple<L, M, R>> maxByMiddle(@NotNull Comparator<? super M> comparator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.max((c1, c2) -> comparator.compare(c1.middle(), c2.middle()));
        }
    }

    /**
     * Returns the maximum triple according to the given right-element comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare right elements
     * @return an {@code Optional} of the triple with the maximum right element
     */
    default @NotNull Optional<Triple<L, M, R>> maxByRight(@NotNull Comparator<? super R> comparator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.max((c1, c2) -> comparator.compare(c1.right(), c2.right()));
        }
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Triple<L, M, R>> min(@NotNull Comparator<? super Triple<L, M, R>> comparator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.min(comparator);
        }
    }

    /**
     * Returns the minimum triple according to the given left-element comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare left elements
     * @return an {@code Optional} of the triple with the minimum left element
     */
    default @NotNull Optional<Triple<L, M, R>> minByLeft(@NotNull Comparator<? super L> comparator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.min((c1, c2) -> comparator.compare(c1.left(), c2.left()));
        }
    }

    /**
     * Returns the minimum triple according to the given middle-element comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare middle elements
     * @return an {@code Optional} of the triple with the minimum middle element
     */
    default @NotNull Optional<Triple<L, M, R>> minByMiddle(@NotNull Comparator<? super M> comparator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.min((c1, c2) -> comparator.compare(c1.middle(), c2.middle()));
        }
    }

    /**
     * Returns the minimum triple according to the given right-element comparator, or an empty
     * {@link Optional} if the stream is empty.
     *
     * @param comparator a comparator to compare right elements
     * @return an {@code Optional} of the triple with the minimum right element
     */
    default @NotNull Optional<Triple<L, M, R>> minByRight(@NotNull Comparator<? super R> comparator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.min((c1, c2) -> comparator.compare(c1.right(), c2.right()));
        }
    }

    // Order

    /** {@inheritDoc} */
    @Override
    default boolean isParallel() {
        return this.underlying().isParallel();
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull TripleStream<L, M ,R> parallel() {
        return of(this.underlying().parallel());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull TripleStream<L, M ,R> sequential() {
        return of(this.underlying().sequential());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull TripleStream<L, M ,R> unordered() {
        return of(this.underlying().unordered());
    }

    // Reduction

    /** {@inheritDoc} */
    @Override
    default @NotNull Triple<L, M, R> reduce(@NotNull Triple<L, M, R> identity, @NotNull BinaryOperator<Triple<L, M, R>> accumulator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.reduce(identity, accumulator);
        }
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Optional<Triple<L, M, R>> reduce(@NotNull BinaryOperator<Triple<L, M, R>> accumulator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.reduce(accumulator);
        }
    }

    /** {@inheritDoc} */
    @Override
    default <U> @NotNull U reduce(@NotNull U identity, @NotNull BiFunction<U, ? super Triple<L, M, R>, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.reduce(identity, accumulator, combiner);
        }
    }

    /**
     * Performs a reduction on the triples of this stream, using the provided identity value,
     * an accumulator that receives the left, middle, and right elements separately, and a
     * combiner for parallel execution.
     *
     * @param <U>         the type of the result
     * @param identity    the identity value for the accumulator
     * @param accumulator a function that folds the left, middle, and right elements into the result
     * @param combiner    a function to combine two partial results in parallel execution
     * @return the reduced result
     */
    default <U> @NotNull U reduce(@NotNull U identity, @NotNull QuadFunction<U, ? super L, ? super M, ? super R, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.reduce(identity, (u, entry) -> accumulator.apply(u, entry.left(), entry.middle(), entry.right()), combiner);
        }
    }

    // Sorting

    /**
     * {@inheritDoc}
     * <p>
     * Sorts triples by their natural left-element ordering. The left type must be
     * {@link Comparable}; otherwise a {@link ClassCastException} is thrown at terminal
     * evaluation, matching the underlying {@link Stream#sorted()} contract.
     */
    @Override
    @SuppressWarnings("unchecked")
    default @NotNull TripleStream<L, M ,R> sorted() {
        return this.sortedByLeft((Comparator<? super L>) Comparator.naturalOrder());
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull TripleStream<L, M ,R> sorted(@NotNull Comparator<? super Triple<L, M, R>> comparator) {
        return of(this.underlying().sorted(comparator));
    }

    /**
     * Returns a {@code TripleStream} with triples sorted according to the given
     * left-element comparator.
     *
     * @param comparator a comparator to compare left elements
     * @return a sorted {@code TripleStream}
     */
    default @NotNull TripleStream<L, M ,R> sortedByLeft(@NotNull Comparator<? super L> comparator) {
        return of(this.underlying().sorted((c1, c2) -> comparator.compare(c1.left(), c2.left())));
    }

    /**
     * Returns a {@code TripleStream} with triples sorted according to the given
     * middle-element comparator.
     *
     * @param comparator a comparator to compare middle elements
     * @return a sorted {@code TripleStream}
     */
    default @NotNull TripleStream<L, M ,R> sortedByMiddle(@NotNull Comparator<? super M> comparator) {
        return of(this.underlying().sorted((c1, c2) -> comparator.compare(c1.middle(), c2.middle())));
    }

    /**
     * Returns a {@code TripleStream} with triples sorted according to the given
     * right-element comparator.
     *
     * @param comparator a comparator to compare right elements
     * @return a sorted {@code TripleStream}
     */
    default @NotNull TripleStream<L, M ,R> sortedByRight(@NotNull Comparator<? super R> comparator) {
        return of(this.underlying().sorted((c1, c2) -> comparator.compare(c1.right(), c2.right())));
    }

    // Collect

    /**
     * {@inheritDoc}
     */
    @Override
    default <T, A> T collect(@NotNull Collector<? super Triple<L, M, R>, A, T> collector) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.collect(collector);
        }
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
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.collect(supplier, accumulator, combiner);
        }
    }

    /** {@inheritDoc} */
    @Override
    default @NotNull Object @NotNull [] toArray() {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.toArray();
        }
    }

    /** {@inheritDoc} */
    @Override
    default <A> @NotNull A @NotNull [] toArray(@NotNull IntFunction<A[]> generator) {
        try (Stream<Triple<L, M, R>> s = this.underlying()) {
            return s.toArray(generator);
        }
    }

    // Collapse

    /**
     * Collapses each triple into a single value by applying {@code mapper} to the left,
     * middle, and right elements, returning a {@link SingleStream} of the results.
     *
     * @param <T>    the result element type
     * @param mapper a function receiving each triple's elements, returning the collapsed result
     * @return a {@link SingleStream} of collapsed values
     */
    default <T> @NotNull SingleStream<T> collapseToSingle(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends T> mapper) {
        return SingleStream.of(this.underlying().map(entry -> mapper.apply(entry.left(), entry.middle(), entry.right())));
    }

    /**
     * Collapses each triple into a {@link Pair} by applying {@code mapper} to the left,
     * middle, and right elements, returning a {@link PairStream} of the results.
     *
     * @param <K>    the key type of the resulting pairs
     * @param <V>    the value type of the resulting pairs
     * @param mapper a function receiving each triple's elements, returning a {@code Pair}
     * @return a {@link PairStream} of collapsed pairs
     */
    default <K, V> @NotNull PairStream<K, V> collapseToPair(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends Pair<K, V>> mapper) {
        return PairStream.of(this.underlying().map(entry -> mapper.apply(entry.left(), entry.middle(), entry.right())));
    }

    /**
     * Drops the left element of each triple, returning a {@link PairStream} of
     * {@code (middle, right)} pairs.
     *
     * @return a {@link PairStream} of middle-right pairs
     */
    default @NotNull PairStream<M, R> dropLeft() {
        return PairStream.of(this.underlying().map(entry -> Pair.of(entry.middle(), entry.right())));
    }

    /**
     * Drops the middle element of each triple, returning a {@link PairStream} of
     * {@code (left, right)} pairs.
     *
     * @return a {@link PairStream} of left-right pairs
     */
    default @NotNull PairStream<L, R> dropMiddle() {
        return PairStream.of(this.underlying().map(entry -> Pair.of(entry.left(), entry.right())));
    }

    /**
     * Drops the right element of each triple, returning a {@link PairStream} of
     * {@code (left, middle)} pairs.
     *
     * @return a {@link PairStream} of left-middle pairs
     */
    default @NotNull PairStream<L, M> dropRight() {
        return PairStream.of(this.underlying().map(entry -> Pair.of(entry.left(), entry.middle())));
    }

    /**
     * Returns a {@link SingleStream} by replacing each triple with the contents of the
     * {@link Collection} returned by {@code mapper}, avoiding a manual
     * {@code .flatMap(c -> c.stream())} call.
     *
     * @param <T>    the element type of the resulting stream
     * @param mapper a function receiving each triple's elements, returning a collection of results
     * @return a flat-mapped {@link SingleStream}
     */
    default <T> @NotNull SingleStream<T> flatMapCollection(@NotNull TriFunction<? super L, ? super M, ? super R, ? extends Collection<? extends T>> mapper) {
        return SingleStream.of(this.underlying().flatMap(entry -> mapper.apply(entry.left(), entry.middle(), entry.right()).stream()));
    }

}
