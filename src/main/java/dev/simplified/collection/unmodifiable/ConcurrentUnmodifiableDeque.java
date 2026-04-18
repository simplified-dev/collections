package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentDeque;
import dev.simplified.collection.atomic.AtomicDeque;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A live, unmodifiable view over any {@link AtomicDeque}. Shares the source deque's
 * underlying storage, so the wrapper reflects current source state (size, contents, iteration
 * order), but every mutating operation rejects with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this deque
 */
public class ConcurrentUnmodifiableDeque<E> extends ConcurrentDeque<E> {

	/**
	 * Creates a live, unmodifiable view over the given source.
	 *
	 * @param source the source whose storage is shared with this unmodifiable view
	 */
	public ConcurrentUnmodifiableDeque(@NotNull AtomicDeque<E> source) {
		super(source);
	}

	/** {@inheritDoc} */
	@Override
	public final boolean add(E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void addFirst(E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void addLast(E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean offer(E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean offerFirst(E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean offerLast(E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @Nullable E poll() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final E pollFirst() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @Nullable E pollLast() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @NotNull E pop() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void push(E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @NotNull E remove() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean remove(Object obj) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean removeAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final E removeFirst() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean removeFirstOccurrence(Object obj) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final E removeLast() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean removeLastOccurrence(Object obj) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @NotNull ConcurrentDeque<E> toUnmodifiableDeque() {
		return this;
	}

}
