package dev.simplified.collection.function;

/**
 * Represents a predicate (boolean-valued function) that accepts an element together with its
 * zero-based index and the total size of the sequence it came from. The index and size are
 * primitive {@code long}s, avoiding the boxing that a
 * {@link TriPredicate}{@code <T, Long, Long>} would incur on every call.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #test(Object, long, long)}.
 *
 * @param <T> the element type
 */
@FunctionalInterface
public interface IndexedPredicate<T> {

	/**
	 * Evaluates this predicate on the given element, its index, and the total size.
	 *
	 * @param element the stream element
	 * @param index   the zero-based index of {@code element} in the source sequence
	 * @param size    the estimated total size of the source sequence
	 * @return {@code true} if the element matches
	 */
	boolean test(T element, long index, long size);

}
