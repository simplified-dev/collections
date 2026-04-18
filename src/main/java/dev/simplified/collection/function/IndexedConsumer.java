package dev.simplified.collection.function;

/**
 * Represents an operation that accepts an element together with its zero-based index and the
 * total size of the sequence it came from, and returns no result. The index and size are
 * primitive {@code long}s, avoiding the boxing that a
 * {@link TriConsumer}{@code <T, Long, Long>} would incur on every call.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, long, long)}.
 *
 * @param <T> the element type
 */
@FunctionalInterface
public interface IndexedConsumer<T> {

	/**
	 * Performs this operation on the given element, its index, and the total size.
	 *
	 * @param element the stream element
	 * @param index   the zero-based index of {@code element} in the source sequence
	 * @param size    the estimated total size of the source sequence
	 */
	void accept(T element, long index, long size);

}
