package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentQueue;
import dev.simplified.collection.atomic.AtomicQueue;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	class Impl<E> extends ConcurrentQueue.Impl<E> implements ConcurrentUnmodifiableQueue<E> {

		/**
		 * Wraps a snapshot of the given source queue. The source's contents are copied under its
		 * own read lock at construction time.
		 *
		 * @param source the source queue whose elements are snapshotted
		 */
		public Impl(@NotNull AtomicQueue<E> source) {
			super(snapshotStorage(source));
		}

		private static <E> @NotNull ConcurrentLinkedList.Impl<E> snapshotStorage(@NotNull AtomicQueue<E> source) {
			return (ConcurrentLinkedList.Impl<E>) Concurrent.newLinkedList(source);
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
		@SafeVarargs
		@Override
		public final boolean addAll(@NotNull E... collection) {
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
		public final boolean replace(@NotNull E existingElement, @NotNull E replaceWith) {
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
