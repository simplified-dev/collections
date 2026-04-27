package dev.simplified.collection.atomic;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A snapshot-backed iterator that operates over an immutable array captured at iterator
 * creation, providing thread-safe iteration for concurrent collections. The default
 * {@link #remove()} throws {@link UnsupportedOperationException}; subclasses backed by a
 * mutable collection override it to mutate the source under the appropriate lock.
 *
 * @param <E> the type of elements returned by this iterator
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
@SuppressWarnings("unchecked")
class AtomicIterator<E> implements Iterator<E> {

	/** Immutable snapshot of the array captured at iterator creation time. */
	protected final Object[] snapshot;

	/** Index of element to be returned by subsequent call to next. */
	protected int cursor;

	/** Index of last element to be returned. */
	protected int last = -1;

	/**
	 * Creates a new iterator over the given snapshot starting at the specified index.
	 *
	 * @param snapshot the array snapshot to iterate over
	 * @param index the starting index
	 */
	protected AtomicIterator(Object[] snapshot, int index) {
		this.cursor = index;
		this.snapshot = snapshot;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return this.cursor < this.snapshot.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull E next() {
		if (this.hasNext())
			return (E) this.snapshot[this.last = this.cursor++];
		else
			throw new NoSuchElementException();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Throws {@link UnsupportedOperationException} by default. Subclasses backed by a mutable
	 * source override to propagate removal to the source under the appropriate lock.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		Objects.requireNonNull(action);
		final int size = this.snapshot.length;
		int i = this.cursor;

		if (i < size) {
			for (; i < size; i++)
				action.accept((E) this.snapshot[i]);

			this.cursor = i;
			this.last = i - 1;
		}
	}

}
