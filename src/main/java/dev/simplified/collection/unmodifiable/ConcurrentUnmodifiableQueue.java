package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentQueue;
import dev.simplified.collection.atomic.AtomicQueue;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * An immutable snapshot of a {@link ConcurrentQueue}. The wrapper owns a fresh copy of the
 * source's contents at construction time and never reflects subsequent mutations on the source.
 *
 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}.</p>
 *
 * @param <E> the type of elements in this queue
 */
public class ConcurrentUnmodifiableQueue<E> extends ConcurrentQueue<E> {

	/**
	 * Wraps a snapshot of the given source queue. The source's contents are copied under its
	 * own read lock at construction time.
	 *
	 * @param source the source queue whose elements are snapshotted
	 */
	public ConcurrentUnmodifiableQueue(@NotNull AtomicQueue<E> source) {
		super(snapshotStorage(source));
	}

	private static <E> @NotNull ConcurrentLinkedList<E> snapshotStorage(@NotNull AtomicQueue<E> source) {
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
	public final @Nullable E poll() {
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
	public final boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @NotNull ConcurrentQueue<E> toUnmodifiable() {
		return this;
	}

}
