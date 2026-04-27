package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * An immutable snapshot view of a {@link ConcurrentLinkedList}. All mutating operations on
 * implementations of this interface reject with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this list
 */
public interface ConcurrentUnmodifiableLinkedList<E> extends ConcurrentLinkedList<E>, ConcurrentUnmodifiableList<E> {

	/**
	 * An immutable snapshot of a {@link ConcurrentLinkedList.Impl} preserving the source's
	 * insertion order. The wrapper owns a fresh {@link LinkedList} copy and never reflects
	 * subsequent mutations on the source. Reads on the snapshot are wait-free, backed by
	 * {@link NoOpReadWriteLock}.
	 *
	 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}.</p>
	 *
	 * @param <E> the type of elements in this list
	 */
	class Impl<E> extends ConcurrentLinkedList.Impl<E> implements ConcurrentUnmodifiableLinkedList<E> {

		/**
		 * Wraps the given pre-cloned snapshot reference. Callers should obtain {@code snapshot} by
		 * copying a source list's contents under that source's read lock.
		 *
		 * @param snapshot a freshly cloned backing list
		 */
		public Impl(@NotNull LinkedList<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		/** {@inheritDoc} */
		@Override
		public final boolean add(@NotNull E element) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final void add(int index, @NotNull E element) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean addAll(@NotNull Collection<? extends E> collection) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean addAll(int index, @NotNull Collection<? extends E> collection) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final void addFirst(@NotNull E element) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final void addLast(@NotNull E element) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean addIf(@NotNull Predicate<List<E>> predicate, @NotNull E element) {
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
		public final E remove(int index) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final E removeFirst() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final E removeLast() {
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
		public final void replaceAll(@NotNull UnaryOperator<E> operator) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final E set(int index, E element) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final void sort(Comparator<? super E> comparator) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull ConcurrentUnmodifiableLinkedList<E> toUnmodifiable() {
			return this;
		}

	}

}
