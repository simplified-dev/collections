package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentQueue;
import dev.simplified.collection.atomic.AtomicQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A live, unmodifiable view over any {@link AtomicQueue}. Shares the source queue's
 * underlying storage, so the wrapper reflects current source state (size, contents, iteration
 * order), but every mutating operation rejects with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this queue
 */
public class ConcurrentUnmodifiableQueue<E> extends ConcurrentQueue<E> {

	/**
	 * Creates a live, unmodifiable view over the given source.
	 *
	 * @param source the source whose storage is shared with this unmodifiable view
	 */
	public ConcurrentUnmodifiableQueue(@NotNull AtomicQueue<E> source) {
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
