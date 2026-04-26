package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentDeque;
import dev.simplified.collection.atomic.AtomicDeque;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * An immutable snapshot of a {@link ConcurrentDeque}. The wrapper owns a fresh copy of the
 * source's contents at construction time and never reflects subsequent mutations on the source.
 *
 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}.</p>
 *
 * @param <E> the type of elements in this deque
 */
public class ConcurrentUnmodifiableDeque<E> extends ConcurrentDeque<E> {

	/**
	 * Wraps a snapshot of the given source deque. The source's contents are copied under its
	 * own read lock at construction time.
	 *
	 * @param source the source deque whose elements are snapshotted
	 */
	public ConcurrentUnmodifiableDeque(@NotNull AtomicDeque<E> source) {
		super(snapshotStorage(source));
	}

	private static <E> @NotNull ConcurrentLinkedList<E> snapshotStorage(@NotNull AtomicDeque<E> source) {
		return Concurrent.newLinkedList(source);
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
	public final @NotNull ConcurrentDeque<E> toUnmodifiable() {
		return this;
	}

}
