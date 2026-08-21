package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A functional interface that extends {@link Function} to support composable property-accessor
 * chains used in collection query operations such as {@link Searchable} and {@link Sortable}.
 *
 * <p>Extending {@link Serializable} is what makes the compiler emit a {@code writeReplace} on every
 * lambda and method reference whose target type is this interface, which is what lets
 * {@link PropertyReference#of(SearchFunction)} recover the property an extractor reads.
 *
 * @param <T> the input type of the function
 * @param <R> the result type of the function
 */
@FunctionalInterface
public interface SearchFunction<T, R> extends Function<T, R>, Serializable {

    /**
     * Combines two {@link SearchFunction} instances into a single function that applies
     * {@code from} first, then passes the result to {@code to}. Useful for traversing
     * nested method references (e.g., {@code combine(Entity::getChild, Child::getName)}).
     *
     * @param from the first function in the chain
     * @param to the next function in the chain
     * @param <T1> the input type of the first function
     * @param <T2> the intermediate type (output of {@code from}, input of {@code to})
     * @param <T3> the final return type
     * @return a composed {@link SearchFunction} mapping from {@code T1} to {@code T3}
     */
    static <T1, T2, T3> @NotNull SearchFunction<T1, T3> combine(@NotNull SearchFunction<T1, T2> from, @NotNull SearchFunction<T2, T3> to) {
        return from.andThen(to);
    }

    /**
     * Returns a composed {@link SearchFunction} that first applies this function to its input,
     * and then applies the {@code after} function to the result.
     *
     * @param after the function to apply after this function
     * @param <V> the output type of the {@code after} function
     * @return a {@link Composed} keeping both halves reachable
     */
    @Override
    default <V> @NotNull Composed<T, R, V> andThen(@NotNull Function<? super R, ? extends V> after) {
        return new Composed<>(this, after);
    }

    /**
     * Returns a composed {@link SearchFunction} whose second half stays decodable, which the
     * {@link Function} overload cannot offer because a method reference bound to it carries no
     * {@code writeReplace}.
     *
     * @param after the extractor to apply after this one
     * @param <V> the output type of the {@code after} extractor
     * @return a {@link Composed} whose joined property path resolves
     */
    default <V> @NotNull Composed<T, R, V> andThen(@NotNull SearchFunction<? super R, ? extends V> after) {
        return new Composed<>(this, after);
    }

    /**
     * A two-stage composition that keeps both halves reachable, so a decoder joins their property
     * paths without reading bytecode.
     *
     * <p>{@link #apply} propagates a {@link NullPointerException} raised by {@code to} when
     * {@code from} yields {@code null}, which is the behaviour the query terminals catch and treat
     * as a non-match.
     *
     * <p>Every other extractor is one class per call site, so what it reads is worked out once and
     * remembered against that class. A composition is one class holding every chain anyone writes,
     * so it is the only shape that has to remember its own - which is why it carries a field and is
     * not a record.
     *
     * @param <T> the input type of {@code from}
     * @param <M> the intermediate type between the two halves
     * @param <R> the result type of {@code to}
     */
    final class Composed<T, M, R> implements SearchFunction<T, R> {

        private final @NotNull SearchFunction<T, M> from;
        private final @NotNull Function<? super M, ? extends R> to;

        /**
         * The joined property path, worked out on the first query that names this composition. Two
         * threads racing here decode the same chain, so either answer stands.
         */
        private transient PropertyReference decoded;

        /**
         * Constructs a new {@code Composed} applying one extractor and then a function over what it
         * yields.
         *
         * @param from the extractor applied first
         * @param to the function applied to the first extractor's result
         */
        public Composed(@NotNull SearchFunction<T, M> from, @NotNull Function<? super M, ? extends R> to) {
            this.from = from;
            this.to = to;
        }

        /**
         * The extractor applied first.
         *
         * @return the first half
         */
        public @NotNull SearchFunction<T, M> from() {
            return this.from;
        }

        /**
         * The function applied to what the first extractor yields.
         *
         * @return the second half
         */
        public @NotNull Function<? super M, ? extends R> to() {
            return this.to;
        }

        /** {@inheritDoc} */
        @Override
        public R apply(T value) {
            return this.to.apply(this.from.apply(value));
        }

        /**
         * The joined property path, when one has been worked out.
         *
         * @return the path both halves read together, or {@code null} until it is decoded
         */
        PropertyReference decoded() {
            return this.decoded;
        }

        /**
         * Remembers the joined property path, so the next query reads it rather than joining it
         * again.
         *
         * @param decoded the path both halves read together
         */
        void decoded(@NotNull PropertyReference decoded) {
            this.decoded = decoded;
        }

    }

    /**
     * Defines how multiple predicates are combined when filtering elements in a search operation.
     */
    enum Match {

        /**
         * All predicates must match for an element to be included.
         */
        ALL,
        /**
         * At least one predicate must match for an element to be included.
         */
        ANY

    }

}
