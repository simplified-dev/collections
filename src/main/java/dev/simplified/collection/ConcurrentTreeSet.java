package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.atomic.AtomicNavigableSet;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
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
	 * Creates a new empty {@link ConcurrentTreeSet} backed by a {@link TreeSet} with natural
	 * element ordering.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent tree set
	 */
	static <E> @NotNull ConcurrentTreeSet<E> empty() {
		return new Impl<E>();
	}

	/**
	 * Creates a new empty {@link ConcurrentTreeSet} ordered by the given comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param <E> the element type
	 * @return a new empty concurrent tree set
	 */
	static <E> @NotNull ConcurrentTreeSet<E> withComparator(@NotNull Comparator<? super E> comparator) {
		return new Impl<E>(comparator);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet} containing the given elements with natural ordering.
	 *
	 * @param elements the elements to include
	 * @param <E> the element type
	 * @return a new concurrent tree set containing the specified elements
	 */
	@SafeVarargs
	static <E> @NotNull ConcurrentTreeSet<E> of(@NotNull E... elements) {
		return new Impl<>(elements);
	}

	/**
	 * Creates a new {@link ConcurrentTreeSet} containing all elements of the given collection
	 * with natural ordering.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty set
	 * @param <E> the element type
	 * @return a new concurrent tree set containing the source's elements
	 */
	static <E> @NotNull ConcurrentTreeSet<E> from(@Nullable Collection<? extends E> collection) {
		return new Impl<E>(collection);
	}

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
	class Impl<E> extends AtomicNavigableSet<E, TreeSet<E>> {

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
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("DataFlowIssue")
		protected @NotNull AtomicCollection<E, TreeSet<E>> newEmpty() {
			return new ConcurrentTreeSet.Impl<>(this.ref.comparator());
		}

		/**
		 * Returns a {@link TreeSet}-typed snapshot of this set's backing reference, captured under
		 * the read lock. {@link TreeSet#TreeSet(java.util.SortedSet)} preserves the source's
		 * comparator when the source is itself a {@code SortedSet}.
		 *
		 * @return a fresh {@link TreeSet} containing the current elements
		 */
		protected @NotNull TreeSet<E> cloneRef() {
			return this.withReadLock(() -> new TreeSet<>(this.ref));
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentTreeSet.Impl} preserving the
		 * source's comparator and sort order.
		 *
		 * @return an unmodifiable {@link ConcurrentTreeSet.Impl} containing a snapshot of the elements
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableTreeSet<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableTreeSet.Impl<>(this.cloneRef());
		}

	}

}
