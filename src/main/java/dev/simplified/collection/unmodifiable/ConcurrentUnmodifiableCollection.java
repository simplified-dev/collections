package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentCollection;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An immutable snapshot view of a {@link ConcurrentCollection}. All mutating operations on
 * implementations of this interface reject with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this collection
 */
public interface ConcurrentUnmodifiableCollection<E> extends ConcurrentCollection<E> {

	/**
	 * An immutable snapshot of a {@link ConcurrentCollection.Impl}. The wrapper owns a fresh copy
	 * of the source's contents at construction time and never reflects subsequent mutations on the
	 * source. Reads on the snapshot are wait-free, backed by {@link NoOpReadWriteLock}.
	 *
	 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}.</p>
	 *
	 * @param <E> the type of elements in this collection
	 */
	class Impl<E> extends ConcurrentCollection.Impl<E> implements ConcurrentUnmodifiableCollection<E> {

		/**
		 * Wraps the given pre-cloned snapshot reference. Callers should obtain {@code snapshot} by
		 * copying a source collection's contents under that source's read lock.
		 *
		 * @param snapshot a freshly cloned backing collection
		 */
		public Impl(@NotNull AbstractCollection<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		/** {@inheritDoc} */
		@Override
		public final boolean add(@NotNull E element) {
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
		public final boolean addIf(@NotNull Predicate<AbstractCollection<E>> predicate, @NotNull E element) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final void clear() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean remove(Object element) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean removeAll(@NotNull Collection<?> collection) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean removeIf(@NotNull Predicate<? super E> filter) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean retainAll(@NotNull Collection<?> collection) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final @NotNull ConcurrentUnmodifiableCollection<E> toUnmodifiable() {
			return this;
		}

	}

}
