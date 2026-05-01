package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * A functional interface that extends {@link Function} to support composable property-accessor
 * chains used in collection query operations such as {@link Searchable} and {@link Sortable}.
 *
 * @param <T> the input type of the function
 * @param <R> the result type of the function
 */
@FunctionalInterface
public interface SearchFunction<T, R> extends Function<T, R> {

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
     * @return a composed {@link SearchFunction} that applies this function then {@code after}
     */
    @Override
    default <V> @NotNull SearchFunction<T, V> andThen(@NotNull Function<? super R, ? extends V> after) {
        return (T t) -> after.apply(apply(t));
    }

    /**
     * Defines how multiple predicates are combined when filtering elements in a search operation.
     */
    enum Match {

        /** All predicates must match for an element to be included. */
        ALL,
        /** At least one predicate must match for an element to be included. */
        ANY

    }

}
