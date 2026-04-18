package dev.simplified.collection.function;

import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

/**
 * Represents a function that accepts three arguments and produces a {@code long}-valued
 * result. This is the three-arity {@code long}-producing specialization of {@link TriFunction}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsLong(Object, Object, Object)}.
 *
 * @param <L> the type of the first argument to the function
 * @param <M> the type of the second argument to the function
 * @param <R> the type of the third argument to the function
 *
 * @see ToLongFunction
 * @see ToLongBiFunction
 */
@FunctionalInterface
public interface ToLongTriFunction<L, M, R> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param l the first function argument
	 * @param m the second function argument
	 * @param r the third function argument
	 * @return the function result
	 */
	long applyAsLong(L l, M m, R r);

}
