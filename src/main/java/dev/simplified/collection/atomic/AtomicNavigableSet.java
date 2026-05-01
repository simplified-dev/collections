package dev.simplified.collection.atomic;

import dev.simplified.collection.ConcurrentSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe abstract navigable set backed by a {@link ReadWriteLock} for concurrent access.
 * Extends {@link AtomicSet} to add the navigable-traversal surface ({@link NavigableSet#first},
 * {@link NavigableSet#ceiling}, {@link NavigableSet#headSet}, etc.) with atomic guarantees.
 *
 * @param <E> the type of elements in this set
 * @param <T> the type of the underlying set, which must be both an {@link AbstractSet} and a
 *            {@link NavigableSet}
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicNavigableSet<E, T extends AbstractSet<E> & NavigableSet<E>> extends AtomicSet<E, T> implements ConcurrentSet<E>, NavigableSet<E> {

	private transient volatile @Nullable NavigableSet<E> descendingSetView;

	protected AtomicNavigableSet(@NotNull T ref) {
		super(ref);
	}

	/**
	 * Constructs an {@code AtomicNavigableSet} with an explicit lock - the pattern used by
	 * {@code ConcurrentUnmodifiableTreeSet} to install a snapshot set paired with a no-op lock for
	 * wait-free reads.
	 *
	 * @param ref the underlying set
	 * @param lock the lock guarding {@code ref}
	 */
	protected AtomicNavigableSet(@NotNull T ref, @NotNull ReadWriteLock lock) {
		super(ref, lock);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onSnapshotInvalidated() {
		if (this.descendingSetView != null) this.descendingSetView = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int spliteratorCharacteristics() {
		return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE
			| Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns a snapshot-backed spliterator that exposes this set's {@link Comparator} so
	 * downstream operations honoring {@link Spliterator#SORTED} pick up the configured order.
	 */
	@Override
	public @NotNull Spliterator<E> spliterator() {
		Spliterator<E> base = Spliterators.spliterator(this.cachedOrFreshSnapshotArray(), this.spliteratorCharacteristics());
		return new SortedSnapshotSpliterator<>(base, this.ref.comparator());
	}

	/**
	 * Spliterator wrapper that delegates traversal to a snapshot-backed array spliterator while
	 * exposing the source set's {@link Comparator} via {@link #getComparator()}, satisfying the
	 * {@link Spliterator#SORTED} contract.
	 */
	private static final class SortedSnapshotSpliterator<E> implements Spliterator<E> {

		private final Spliterator<E> delegate;
		private final Comparator<? super E> comparator;

		SortedSnapshotSpliterator(@NotNull Spliterator<E> delegate, Comparator<? super E> comparator) {
			this.delegate = delegate;
			this.comparator = comparator;
		}

		@Override
		public boolean tryAdvance(@NotNull java.util.function.Consumer<? super E> action) {
			return this.delegate.tryAdvance(action);
		}

		@Override
		public void forEachRemaining(@NotNull java.util.function.Consumer<? super E> action) {
			this.delegate.forEachRemaining(action);
		}

		@Override
		public Spliterator<E> trySplit() {
			Spliterator<E> split = this.delegate.trySplit();
			return split == null ? null : new SortedSnapshotSpliterator<>(split, this.comparator);
		}

		@Override
		public long estimateSize() {
			return this.delegate.estimateSize();
		}

		@Override
		public long getExactSizeIfKnown() {
			return this.delegate.getExactSizeIfKnown();
		}

		@Override
		public int characteristics() {
			return this.delegate.characteristics();
		}

		@Override
		public Comparator<? super E> getComparator() {
			return this.comparator;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Comparator<? super E> comparator() {
		return this.ref.comparator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E first() {
		return this.withReadLock(this.ref::first);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E last() {
		return this.withReadLock(this.ref::last);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E lower(E element) {
		return this.withReadLock(() -> this.ref.lower(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E floor(E element) {
		return this.withReadLock(() -> this.ref.floor(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E ceiling(E element) {
		return this.withReadLock(() -> this.ref.ceiling(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E higher(E element) {
		return this.withReadLock(() -> this.ref.higher(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E pollFirst() {
		return this.withWriteLock(this.ref::pollFirst);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E pollLast() {
		return this.withWriteLock(this.ref::pollLast);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull NavigableSet<E> descendingSet() {
		NavigableSet<E> view = this.descendingSetView;

		if (view != null)
			return view;

		return this.withReadLock(() -> {
			NavigableSet<E> cached = this.descendingSetView;

			if (cached == null) {
				cached = new LockedNavigableSetView(this.ref.descendingSet());
				this.descendingSetView = cached;
			}

			return cached;
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull Iterator<E> descendingIterator() {
		Object[] snapshot = this.withReadLock(() -> this.ref.descendingSet().toArray());
		return new AtomicIterator<>(snapshot, 0) {
			@Override
			public void remove() {
				if (this.last < 0)
					throw new IllegalStateException();

				AtomicNavigableSet.this.remove(this.snapshot[this.last]);
				this.last = -1;
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull NavigableSet<E> subSet(E from, boolean fromInclusive, E to, boolean toInclusive) {
		return this.withReadLock(() -> new LockedNavigableSetView(this.ref.subSet(from, fromInclusive, to, toInclusive)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull NavigableSet<E> headSet(E to, boolean inclusive) {
		return this.withReadLock(() -> new LockedNavigableSetView(this.ref.headSet(to, inclusive)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull NavigableSet<E> tailSet(E from, boolean inclusive) {
		return this.withReadLock(() -> new LockedNavigableSetView(this.ref.tailSet(from, inclusive)));
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
	 * Lock-guarded {@link NavigableSet} wrapper around a sub-view obtained from this set's
	 * backing {@link NavigableSet}. All reads acquire the enclosing set's read lock; mutations
	 * acquire its write lock and propagate to the backing set.
	 */
	protected final class LockedNavigableSetView extends AbstractSet<E> implements NavigableSet<E> {

		private final @NotNull NavigableSet<E> delegate;

		protected LockedNavigableSetView(@NotNull NavigableSet<E> delegate) {
			this.delegate = delegate;
		}

		@Override public int size() { return AtomicNavigableSet.this.withReadLock(this.delegate::size); }
		@Override public boolean isEmpty() { return AtomicNavigableSet.this.withReadLock(this.delegate::isEmpty); }
		@Override public boolean contains(Object o) { return AtomicNavigableSet.this.withReadLock(() -> this.delegate.contains(o)); }
		@Override public boolean add(E e) { return AtomicNavigableSet.this.withWriteLock(() -> this.delegate.add(e)); }
		@Override public boolean remove(Object o) { return AtomicNavigableSet.this.withWriteLock(() -> this.delegate.remove(o)); }
		@Override public void clear() { AtomicNavigableSet.this.withWriteLock(this.delegate::clear); }
		@Override public Comparator<? super E> comparator() { return this.delegate.comparator(); }
		@Override public E first() { return AtomicNavigableSet.this.withReadLock(this.delegate::first); }
		@Override public E last() { return AtomicNavigableSet.this.withReadLock(this.delegate::last); }
		@Override public E lower(E e) { return AtomicNavigableSet.this.withReadLock(() -> this.delegate.lower(e)); }
		@Override public E floor(E e) { return AtomicNavigableSet.this.withReadLock(() -> this.delegate.floor(e)); }
		@Override public E ceiling(E e) { return AtomicNavigableSet.this.withReadLock(() -> this.delegate.ceiling(e)); }
		@Override public E higher(E e) { return AtomicNavigableSet.this.withReadLock(() -> this.delegate.higher(e)); }
		@Override public E pollFirst() { return AtomicNavigableSet.this.withWriteLock(this.delegate::pollFirst); }
		@Override public E pollLast() { return AtomicNavigableSet.this.withWriteLock(this.delegate::pollLast); }

		@Override
		public @NotNull Iterator<E> iterator() {
			Object[] snapshot = AtomicNavigableSet.this.withReadLock(() -> this.delegate.toArray());
			return new AtomicIterator<>(snapshot, 0);
		}

		@Override
		public @NotNull Spliterator<E> spliterator() {
			Object[] snapshot = AtomicNavigableSet.this.withReadLock(() -> this.delegate.toArray());
			return Spliterators.spliterator(snapshot,
				Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE
					| Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED);
		}

		@Override
		public @NotNull NavigableSet<E> descendingSet() {
			return AtomicNavigableSet.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.descendingSet()));
		}

		@Override
		public @NotNull Iterator<E> descendingIterator() {
			Object[] snapshot = AtomicNavigableSet.this.withReadLock(() -> this.delegate.descendingSet().toArray());
			return new AtomicIterator<>(snapshot, 0);
		}

		@Override
		public @NotNull NavigableSet<E> subSet(E from, boolean fromInclusive, E to, boolean toInclusive) {
			return AtomicNavigableSet.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.subSet(from, fromInclusive, to, toInclusive)));
		}

		@Override
		public @NotNull NavigableSet<E> headSet(E to, boolean inclusive) {
			return AtomicNavigableSet.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.headSet(to, inclusive)));
		}

		@Override
		public @NotNull NavigableSet<E> tailSet(E from, boolean inclusive) {
			return AtomicNavigableSet.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.tailSet(from, inclusive)));
		}

		@Override
		public @NotNull SortedSet<E> subSet(E from, E to) {
			return this.subSet(from, true, to, false);
		}

		@Override
		public @NotNull SortedSet<E> headSet(E to) {
			return this.headSet(to, false);
		}

		@Override
		public @NotNull SortedSet<E> tailSet(E from) {
			return this.tailSet(from, true);
		}

	}

}
