package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentArrayQueue;
import dev.simplified.collection.ConcurrentQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * An immutable snapshot view of a {@link ConcurrentQueue}. All mutating operations on
 * implementations of this interface reject with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this queue
 */
public interface ConcurrentUnmodifiableQueue<E> extends ConcurrentQueue<E>, ConcurrentUnmodifiableCollection<E> {

	/**
	 * An immutable snapshot of a {@link ConcurrentQueue.Impl}. The wrapper owns a fresh copy of
	 * the source's contents at construction time and never reflects subsequent mutations on the
	 * source.
	 *
	 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}.</p>
	 *
	 * @param <E> the type of elements in this queue
	 */
	class Impl<E> extends ConcurrentArrayQueue<E> implements ConcurrentUnmodifiableQueue<E> {

		/**
		 * Wraps the given pre-cloned snapshot reference. Callers should obtain {@code snapshot} by
		 * copying a source queue's contents under that source's read lock.
		 *
		 * @param snapshot a freshly cloned backing queue
		 */
		public Impl(@NotNull ArrayDeque<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
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
		public final boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
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
		public final @NotNull ConcurrentUnmodifiableQueue<E> toUnmodifiable() {
			return this;
		}

	}

}
