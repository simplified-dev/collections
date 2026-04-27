package dev.simplified.collection.tree;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe concurrent set variant backed by a {@link TreeSet} that maintains its elements in
 * sorted order according to their natural ordering or a {@link Comparator} provided at
 * construction time.
 *
 * @param <E> the type of elements in this set
 */
public interface ConcurrentTreeSet<E> extends ConcurrentSet<E>, NavigableSet<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableTreeSet<E> toUnmodifiable();

	/**
	 * Wraps {@code backing} as a {@link ConcurrentTreeSet} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to
	 * {@code backing} bypass the read/write lock and may corrupt concurrent reads. Use this for
	 * zero-copy publication of single-threaded build results. The adopted set's comparator is
	 * preserved.
	 *
	 * @param backing the tree set to adopt
	 * @param <E> the element type
	 * @return a concurrent tree set backed by {@code backing}
	 */
	static <E> @NotNull ConcurrentTreeSet<E> adopt(@NotNull TreeSet<E> backing) {
		return new Impl<>(backing);
	}

	/**
	 * A thread-safe set backed by a {@link TreeSet} with concurrent read and write access via
	 * {@link ReadWriteLock}. Maintains element ordering defined by a {@link Comparator} or the
	 * elements' natural ordering.
	 *
	 * @param <E> the type of elements in this set
	 */
	class Impl<E> extends ConcurrentSet.Impl<E> implements ConcurrentTreeSet<E> {

		private transient volatile @Nullable NavigableSet<E> descendingSetView;

		/**
		 * Creates a new concurrent sorted set with natural element ordering.
		 */
		public Impl() {
			super(new TreeSet<>());
		}

		/**
		 * Creates a new concurrent sorted set with the given comparator.
		 *
		 * @param comparator the comparator used to order the elements
		 */
		public Impl(@NotNull Comparator<? super E> comparator) {
			super(new TreeSet<>(comparator));
		}

		/**
		 * Creates a new concurrent sorted set with natural element ordering and fills it with the
		 * given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			this(Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent sorted set with the given comparator and fills it with the given
		 * array.
		 *
		 * @param comparator the comparator used to order the elements
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull Comparator<? super E> comparator, @NotNull E... array) {
			this(comparator, Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent sorted set with natural element ordering and fills it with the
		 * given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty set
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new TreeSet<>() : new TreeSet<>(collection));
		}

		/**
		 * Creates a new concurrent sorted set with the given comparator and fills it with the given
		 * collection.
		 *
		 * @param comparator the comparator used to order the elements
		 * @param collection the source collection to copy from
		 */
		public Impl(@NotNull Comparator<? super E> comparator, @Nullable Collection<? extends E> collection) {
			super(newTreeSet(comparator, collection));
		}

		/**
		 * Constructs a {@code ConcurrentTreeSet.Impl} that adopts {@code backingSet} as its
		 * storage with a fresh lock. Public callers should go through
		 * {@link ConcurrentTreeSet#adopt(TreeSet)}.
		 *
		 * @param backingSet the backing tree set to adopt
		 */
		protected Impl(@NotNull TreeSet<E> backingSet) {
			super(backingSet);
		}

		/**
		 * Constructs a {@code ConcurrentTreeSet.Impl} with a pre-built backing set and an explicit
		 * lock. Used by {@link ConcurrentUnmodifiableTreeSet.Impl} to install a snapshot set paired
		 * with a no-op lock for wait-free reads.
		 *
		 * @param backingSet the pre-built backing set
		 * @param lock the lock guarding {@code backingSet}
		 */
		protected Impl(@NotNull TreeSet<E> backingSet, @NotNull ReadWriteLock lock) {
			super(backingSet, lock);
		}

		/**
		 * Creates a new {@link TreeSet} with the given comparator, populated from the collection.
		 */
		private static <E> @NotNull TreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator, @Nullable Collection<? extends E> collection) {
			TreeSet<E> set = new TreeSet<>(comparator);
			if (collection != null) set.addAll(collection);
			return set;
		}

		/**
		 * Creates a new empty {@code ConcurrentTreeSet.Impl} instance, used internally for copy
		 * operations.
		 *
		 * @return a new empty {@link ConcurrentTreeSet.Impl}
		 */
		@Override
		@SuppressWarnings("unchecked")
		protected @NotNull AtomicCollection<E, AbstractSet<E>> createEmpty() {
			return (ConcurrentTreeSet.Impl<E>) Concurrent.newTreeSet();
		}

		/**
		 * {@inheritDoc}
		 *
		 * <p>Overrides {@link ConcurrentSet.Impl#cloneRef()} to produce a {@link TreeSet} snapshot.
		 * {@link TreeSet#TreeSet(java.util.SortedSet)} preserves the source's comparator when the
		 * source is itself a {@code SortedSet}.</p>
		 */
		@Override
		protected @NotNull AbstractSet<E> cloneRef() {
			try {
				this.lock.readLock().lock();
				return new TreeSet<>((TreeSet<E>) this.ref);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentTreeSet.Impl} preserving the
		 * source's comparator and sort order.
		 *
		 * @return an unmodifiable {@link ConcurrentTreeSet.Impl} containing a snapshot of the elements
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableTreeSet<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableTreeSet.Impl<>((TreeSet<E>) this.cloneRef());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void onSnapshotInvalidated() {
			this.descendingSetView = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Comparator<? super E> comparator() {
			return ((TreeSet<E>) this.ref).comparator();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E first() {
			try {
				this.lock.readLock().lock();
				return ((TreeSet<E>) this.ref).first();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E last() {
			try {
				this.lock.readLock().lock();
				return ((TreeSet<E>) this.ref).last();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E lower(E element) {
			try {
				this.lock.readLock().lock();
				return ((TreeSet<E>) this.ref).lower(element);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E floor(E element) {
			try {
				this.lock.readLock().lock();
				return ((TreeSet<E>) this.ref).floor(element);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E ceiling(E element) {
			try {
				this.lock.readLock().lock();
				return ((TreeSet<E>) this.ref).ceiling(element);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E higher(E element) {
			try {
				this.lock.readLock().lock();
				return ((TreeSet<E>) this.ref).higher(element);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E pollFirst() {
			try {
				this.lock.writeLock().lock();
				return ((TreeSet<E>) this.ref).pollFirst();
			} finally {
				this.invalidateSnapshot();
				this.lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E pollLast() {
			try {
				this.lock.writeLock().lock();
				return ((TreeSet<E>) this.ref).pollLast();
			} finally {
				this.invalidateSnapshot();
				this.lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableSet<E> descendingSet() {
			NavigableSet<E> view = this.descendingSetView;

			if (view != null)
				return view;

			try {
				this.lock.readLock().lock();
				view = this.descendingSetView;

				if (view == null) {
					view = new ConcurrentTreeMap.LockedNavigableSetView<>(((TreeSet<E>) this.ref).descendingSet(), this.lock);
					this.descendingSetView = view;
				}

				return view;
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull Iterator<E> descendingIterator() {
			Object[] snapshot;

			try {
				this.lock.readLock().lock();
				snapshot = ((TreeSet<E>) this.ref).descendingSet().toArray();
			} finally {
				this.lock.readLock().unlock();
			}

			return new DescendingIterator(snapshot);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableSet<E> subSet(E from, boolean fromInclusive, E to, boolean toInclusive) {
			try {
				this.lock.readLock().lock();
				return new ConcurrentTreeMap.LockedNavigableSetView<>(((TreeSet<E>) this.ref).subSet(from, fromInclusive, to, toInclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableSet<E> headSet(E to, boolean inclusive) {
			try {
				this.lock.readLock().lock();
				return new ConcurrentTreeMap.LockedNavigableSetView<>(((TreeSet<E>) this.ref).headSet(to, inclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableSet<E> tailSet(E from, boolean inclusive) {
			try {
				this.lock.readLock().lock();
				return new ConcurrentTreeMap.LockedNavigableSetView<>(((TreeSet<E>) this.ref).tailSet(from, inclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull SortedSet<E> subSet(E from, E to) {
			return this.subSet(from, true, to, false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull SortedSet<E> headSet(E to) {
			return this.headSet(to, false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull SortedSet<E> tailSet(E from) {
			return this.tailSet(from, true);
		}

		/**
		 * Snapshot-backed iterator over a descending traversal of this tree set.
		 */
		private final class DescendingIterator implements Iterator<E> {

			private final Object[] snapshot;
			private int cursor;
			private int last;

			DescendingIterator(@NotNull Object[] snapshot) {
				this.snapshot = snapshot;
				this.cursor = 0;
				this.last = -1;
			}

			@Override
			public boolean hasNext() {
				return this.cursor < this.snapshot.length;
			}

			@Override
			@SuppressWarnings("unchecked")
			public E next() {
				if (!this.hasNext())
					throw new NoSuchElementException();

				return (E) this.snapshot[this.last = this.cursor++];
			}

			@Override
			public void remove() {
				if (this.last < 0)
					throw new IllegalStateException();

				ConcurrentTreeSet.Impl.this.remove(this.snapshot[this.last]);
				this.last = -1;
			}

		}

	}

}
