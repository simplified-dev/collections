package dev.simplified.collection.function;

/**
 * Represents a function that accepts an element together with its zero-based index and the
 * total size of the sequence it came from, and produces a result. The index and size are
 * primitive {@code long}s, avoiding the boxing that a
 * {@link TriFunction}{@code <T, Long, Long, R>} would incur on every call.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, long, long)}.
 *
 * @param <T> the element type
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface IndexedFunction<T, R> {

	/**
	 * Applies this function to the given element, its index, and the total size.
	 *
	 * @param element the stream element
	 * @param index   the zero-based index of {@code element} in the source sequence
	 * @param size    the estimated total size of the source sequence
	 * @return the function result
	 */
	R apply(T element, long index, long size);

}
