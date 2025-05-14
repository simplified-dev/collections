package dev.sbs.api.collection.stream.triple;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a predicate (boolean-valued function) of three arguments.  This is
 * the three-arity specialization of {@link Predicate}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #test(Object, Object, Object)}.
 *
 * @param <L> the type of the first argument to the predicate
 * @param <M> the type of the second argument the predicate
 * @param <R> the type of the third argument the predicate
 *
 * @see Predicate
 */
@FunctionalInterface
public interface TriPredicate<L, M, R> {

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param l the first input argument
     * @param m the second input argument
     * @param r the third input argument
     * @return {@code true} if the input arguments match the predicate,
     * otherwise {@code false}
     */
    boolean test(L l, M m, R r);

    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * AND of this predicate and another.  When evaluating the composed
     * predicate, if this predicate is {@code false}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this
     *              predicate
     * @return a composed predicate that represents the short-circuiting logical
     * AND of this predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default TriPredicate<L, M, R> and(TriPredicate<? super L, ? super M, ? super R> other) {
        Objects.requireNonNull(other);
        return (L l, M m, R r) -> test(l, m, r) && other.test(l, m, r);
    }

    /**
     * Returns a predicate that represents the logical negation of this
     * predicate.
     *
     * @return a predicate that represents the logical negation of this
     * predicate
     */
    default TriPredicate<L, M, R> negate() {
        return (L l, M m, R r) -> !test(l, m,r );
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * OR of this predicate and another.  When evaluating the composed
     * predicate, if this predicate is {@code true}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ORed with this
     *              predicate
     * @return a composed predicate that represents the short-circuiting logical
     * OR of this predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default TriPredicate<L, M, R> or(TriPredicate<? super L, ? super M, ? super R> other) {
        Objects.requireNonNull(other);
        return (L l, M m, R r) -> test(l, m, r) || other.test(l, m, r);
    }
}