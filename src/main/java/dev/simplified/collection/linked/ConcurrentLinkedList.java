package dev.simplified.collection.linked;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.atomic.AtomicList;
import dev.simplified.collection.query.SortOrder;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

/**
 * A thread-safe concurrent list variant backed by a {@link LinkedList} that preserves the
 * source's insertion-order traversal characteristics.
 *
 * @param <E> the type of elements in this list
 */
public interface ConcurrentLinkedList<E> extends ConcurrentList<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableLinkedList<E> toUnmodifiable();

	/**
	 * A thread-safe list backed by a {@link LinkedList} with concurrent read and write access via
	 * {@link ReadWriteLock}. Supports indexed access, sorting, and snapshot-based iteration with
	 * linked-list insertion characteristics.
	 *
	 * @param <E> the type of elements in this list
	 */
	@SuppressWarnings("all")
	class Impl<E> extends ConcurrentList.Impl<E> implements ConcurrentLinkedList<E> {

		/**
		 * Creates a new concurrent linked list.
		 */
		public Impl() {
			super(new LinkedList<>());
		}

		/**
		 * Creates a new concurrent linked list and fills it with the given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			this(Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent linked list and fills it with the given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty list
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new LinkedList<>() : new LinkedList<>(collection));
		}

		/**
		 * Constructs a {@code ConcurrentLinkedList.Impl} with a pre-built backing list and an
		 * explicit lock. Used by {@link ConcurrentUnmodifiableLinkedList.Impl} to install a
		 * snapshot list paired with a no-op lock for wait-free reads.
		 *
		 * @param backingList the pre-built backing list
		 * @param lock the lock guarding {@code backingList}
		 */
		protected Impl(@NotNull LinkedList<E> backingList, @NotNull ReadWriteLock lock) {
			super(backingList, lock);
		}

		/**
		 * Creates a new empty {@code ConcurrentLinkedList.Impl} instance, used internally for copy
		 * and sort operations.
		 *
		 * @return a new empty {@link ConcurrentLinkedList.Impl}
		 */
		@Override
		protected @NotNull AtomicList<E, List<E>> createEmpty() {
			return (ConcurrentLinkedList.Impl<E>) Concurrent.newLinkedList();
		}

		/**
		 * {@inheritDoc}
		 *
		 * <p>Overrides {@link ConcurrentList.Impl#cloneRef()} to produce a {@link LinkedList}
		 * snapshot preserving the source's insertion-order traversal characteristics.</p>
		 */
		@Override
		protected @NotNull List<E> cloneRef() {
			try {
				this.lock.readLock().lock();
				return new LinkedList<>(this.ref);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * Returns a new {@code ConcurrentLinkedList.Impl} with elements in reverse order. The
		 * original list is not modified.
		 *
		 * @return a new reversed {@link ConcurrentLinkedList.Impl}
		 */
		@Override
		public @NotNull ConcurrentLinkedList.Impl<E> reversed() {
			return (ConcurrentLinkedList.Impl<E>) super.reversed();
		}

		/**
		 * Returns a new {@code ConcurrentLinkedList.Impl} containing all elements sorted in
		 * descending order according to the specified comparison functions.
		 *
		 * @param sortFunctions one or more functions used to extract comparable keys for sorting
		 * @return a new sorted {@link ConcurrentLinkedList.Impl}
		 */
		@Override
		public @NotNull ConcurrentLinkedList.Impl<E> sorted(@NotNull Function<E, ? extends Comparable>... sortFunctions) {
			return (ConcurrentLinkedList.Impl<E>) super.sorted(sortFunctions);
		}

		/**
		 * Returns a new {@code ConcurrentLinkedList.Impl} containing all elements sorted according
		 * to the specified sort order and comparison functions.
		 *
		 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING}) to apply
		 * @param functions one or more functions that extract comparable keys for sorting
		 * @return a new sorted {@link ConcurrentLinkedList.Impl}
		 */
		@Override
		public @NotNull ConcurrentLinkedList.Impl<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions) {
			return (ConcurrentLinkedList.Impl<E>) super.sorted(sortOrder, functions);
		}

		/**
		 * Returns a new {@code ConcurrentLinkedList.Impl} containing all elements sorted in
		 * descending order according to the specified collection of comparison functions.
		 *
		 * @param functions an iterable collection of functions used to extract comparable keys for
		 *                  sorting
		 * @return a new sorted {@link ConcurrentLinkedList.Impl}
		 */
		@Override
		public @NotNull ConcurrentLinkedList.Impl<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions) {
			return (ConcurrentLinkedList.Impl<E>) super.sorted(functions);
		}

		/**
		 * Returns a new {@code ConcurrentLinkedList.Impl} containing all elements sorted according
		 * to the specified sort order and comparison functions.
		 *
		 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING}) to apply
		 * @param functions an iterable collection of functions that extract comparable keys for
		 *                  sorting
		 * @return a new sorted {@link ConcurrentLinkedList.Impl}
		 */
		@Override
		public @NotNull ConcurrentLinkedList.Impl<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions) {
			return (ConcurrentLinkedList.Impl<E>) super.sorted(sortOrder, functions);
		}

		/**
		 * Returns a new {@code ConcurrentLinkedList.Impl} containing all elements sorted according
		 * to the specified {@link Comparator}.
		 *
		 * @param comparator the comparator used to determine the order of elements
		 * @return a new sorted {@link ConcurrentLinkedList.Impl}
		 */
		@Override
		public @NotNull ConcurrentLinkedList.Impl<E> sorted(Comparator<? super E> comparator) {
			return (ConcurrentLinkedList.Impl<E>) super.sorted(comparator);
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentLinkedList.Impl} preserving
		 * insertion order.
		 *
		 * @return an unmodifiable {@link ConcurrentLinkedList.Impl} containing a snapshot of the
		 *         elements
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableLinkedList<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableLinkedList.Impl<>((LinkedList<E>) this.cloneRef());
		}

	}

}
