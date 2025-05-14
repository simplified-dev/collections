package dev.sbs.api.collection.stream.triple;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts three arguments and produces a result.
 * This is the three-arity specialization of {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object, Object)}.
 *
 * @param <L> the type of the first argument to the function
 * @param <M> the type of the second argument to the function
 * @param <R> the type of the third argument to the function
 * @param <T> the type of the result of the function
 *
 * @see java.util.function.Function
 */
@FunctionalInterface
public interface TriFunction<L, M, R, T> {

    /**
     * Applies this function to the given arguments.
     *
     * @param l the first function argument
     * @param m the second function argument
     * @param r the second function argument
     * @return the function result
     */
    T apply(L l, M m, R r);

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <V> TriFunction<L, M, R, V> andThen(Function<? super T, ? extends V> after) {
        Objects.requireNonNull(after);
        return (L l, M m, R r) -> after.apply(apply(l, m, r));
    }

}