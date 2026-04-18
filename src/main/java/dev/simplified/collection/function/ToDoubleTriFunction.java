package dev.simplified.collection.function;

import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

/**
 * Represents a function that accepts three arguments and produces a {@code double}-valued
 * result. This is the three-arity {@code double}-producing specialization of
 * {@link TriFunction}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsDouble(Object, Object, Object)}.
 *
 * @param <L> the type of the first argument to the function
 * @param <M> the type of the second argument to the function
 * @param <R> the type of the third argument to the function
 *
 * @see ToDoubleFunction
 * @see ToDoubleBiFunction
 */
@FunctionalInterface
public interface ToDoubleTriFunction<L, M, R> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param l the first function argument
	 * @param m the second function argument
	 * @param r the third function argument
	 * @return the function result
	 */
	double applyAsDouble(L l, M m, R r);

}
