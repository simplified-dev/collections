package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.linked.ConcurrentLinkedSet;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An immutable snapshot view of a {@link ConcurrentLinkedSet}. All mutating operations on
 * implementations of this interface reject with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this set
 */
public interface ConcurrentUnmodifiableLinkedSet<E> extends ConcurrentLinkedSet<E>, ConcurrentUnmodifiableSet<E> {

	/**
	 * An immutable snapshot of a {@link ConcurrentLinkedSet.Impl} preserving the source's
	 * insertion order. The wrapper owns a fresh {@link LinkedHashSet} copy and never reflects
	 * subsequent mutations on the source. Reads on the snapshot are wait-free, backed by
	 * {@link NoOpReadWriteLock}.
	 *
	 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}.</p>
	 *
	 * @param <E> the type of elements in this set
	 */
	class Impl<E> extends ConcurrentLinkedSet.Impl<E> implements ConcurrentUnmodifiableLinkedSet<E> {

		/**
		 * Wraps the given pre-cloned snapshot reference. Callers should obtain {@code snapshot} by
		 * copying a source set's contents under that source's read lock.
		 *
		 * @param snapshot a freshly cloned backing set
		 */
		public Impl(@NotNull LinkedHashSet<E> snapshot) {
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
		public final boolean addIf(@NotNull Predicate<AbstractSet<E>> predicate, @NotNull E element) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final void clear() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean remove(Object item) {
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
		public @NotNull ConcurrentUnmodifiableLinkedSet<E> toUnmodifiable() {
			return this;
		}

	}

}
