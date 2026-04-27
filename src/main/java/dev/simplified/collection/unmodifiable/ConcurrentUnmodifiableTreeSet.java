package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.tree.ConcurrentTreeSet;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An immutable snapshot view of a {@link ConcurrentTreeSet}. All mutating operations on
 * implementations of this interface reject with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this set
 */
public interface ConcurrentUnmodifiableTreeSet<E> extends ConcurrentTreeSet<E>, ConcurrentUnmodifiableSet<E> {

	/**
	 * An immutable snapshot of a {@link ConcurrentTreeSet.Impl} preserving the source's comparator
	 * and sort order. The wrapper owns a fresh {@link TreeSet} copy and never reflects subsequent
	 * mutations on the source. Reads on the snapshot are wait-free, backed by
	 * {@link NoOpReadWriteLock}.
	 *
	 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}.</p>
	 *
	 * @param <E> the type of elements in this set
	 */
	class Impl<E> extends ConcurrentTreeSet.Impl<E> implements ConcurrentUnmodifiableTreeSet<E> {

		/**
		 * Wraps the given pre-cloned snapshot reference. Callers should obtain {@code snapshot} by
		 * copying a source set's contents under that source's read lock.
		 *
		 * @param snapshot a freshly cloned backing set
		 */
		public Impl(@NotNull TreeSet<E> snapshot) {
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
		public final E pollFirst() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final E pollLast() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableSet<E> descendingSet() {
			return Collections.unmodifiableNavigableSet(super.descendingSet());
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull Iterator<E> descendingIterator() {
			Iterator<E> iterator = super.descendingIterator();
			return new Iterator<>() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public E next() {
					return iterator.next();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableSet<E> subSet(E from, boolean fromInclusive, E to, boolean toInclusive) {
			return Collections.unmodifiableNavigableSet(super.subSet(from, fromInclusive, to, toInclusive));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableSet<E> headSet(E to, boolean inclusive) {
			return Collections.unmodifiableNavigableSet(super.headSet(to, inclusive));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableSet<E> tailSet(E from, boolean inclusive) {
			return Collections.unmodifiableNavigableSet(super.tailSet(from, inclusive));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull SortedSet<E> subSet(E from, E to) {
			return Collections.unmodifiableSortedSet(super.subSet(from, to));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull SortedSet<E> headSet(E to) {
			return Collections.unmodifiableSortedSet(super.headSet(to));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull SortedSet<E> tailSet(E from) {
			return Collections.unmodifiableSortedSet(super.tailSet(from));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull ConcurrentUnmodifiableTreeSet<E> toUnmodifiable() {
			return this;
		}

	}

}
