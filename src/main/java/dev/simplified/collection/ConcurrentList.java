package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicList;
import dev.simplified.collection.query.SortOrder;
import dev.simplified.collection.query.Sortable;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

/**
 * A thread-safe {@link List} extension combining the {@link ConcurrentCollection} surface with
 * indexed access, atomic sorting, and snapshot-based list iteration.
 *
 * @param <E> the type of elements in this list
 */
@SuppressWarnings("unchecked")
public interface ConcurrentList<E> extends ConcurrentCollection<E>, Sortable<E>, List<E> {

	/**
	 * Returns an {@link Optional} containing the first element of this list, or an empty
	 * {@code Optional} if the list is empty.
	 *
	 * @return an {@code Optional} describing the first element, or an empty {@code Optional}
	 */
	@NotNull Optional<E> findFirst();

	/**
	 * Returns an {@link Optional} containing the last element of this list, or an empty
	 * {@code Optional} if the list is empty.
	 *
	 * @return an {@code Optional} describing the last element, or an empty {@code Optional}
	 */
	@NotNull Optional<E> findLast();

	/**
	 * Returns the element at the specified index, or the default value if the index is out of
	 * bounds.
	 *
	 * @param index the index of the element to return
	 * @param defaultValue the default value to return if the index is out of bounds
	 * @return the element at the specified index, or {@code defaultValue} if the index is out of
	 *         range
	 */
	E getOrDefault(int index, E defaultValue);

	/**
	 * Returns a new list containing all elements from this list, sorted in descending order
	 * according to the specified comparison functions. The original list is not modified.
	 *
	 * @param functions one or more functions used to extract comparable keys for sorting
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(@NotNull Function<E, ? extends Comparable>... functions);

	/**
	 * Returns a new list containing all elements from this list, sorted in descending order
	 * according to the specified collection of comparison functions. The original list is not
	 * modified.
	 *
	 * @param functions an iterable collection of functions used to extract comparable keys for
	 *                  sorting
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions);

	/**
	 * Returns a new list containing all elements from this list, sorted according to the
	 * specified sort order and comparison functions. The original list is not modified.
	 *
	 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING})
	 * @param functions one or more functions that extract comparable keys for sorting
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions);

	/**
	 * Returns a new list containing all elements from this list, sorted according to the
	 * specified sort order and comparison functions. The original list is not modified.
	 *
	 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING})
	 * @param functions an iterable collection of functions that extract comparable keys for
	 *                  sorting
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions);

	/**
	 * Returns a new list containing all elements from this list, sorted according to the given
	 * comparator. The original list is not modified.
	 *
	 * @param comparator the comparator used to order the elements; {@code null} requests natural
	 *                   ordering
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(Comparator<? super E> comparator);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentList<E> reversed();

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentList<E> subList(int fromIndex, int toIndex);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableList<E> toUnmodifiable();

	/**
	 * A thread-safe list backed by an {@link ArrayList} with concurrent read and write access
	 * via {@link ReadWriteLock}. Supports indexed access, sorting, and snapshot-based iteration.
	 *
	 * @param <E> the type of elements in this list
	 */
	@SuppressWarnings("all")
	class Impl<E> extends AtomicList<E, List<E>> implements ConcurrentList<E> {

		/**
		 * Creates a new concurrent list.
		 */
		public Impl() {
			super(new ArrayList<>());
		}

		/**
		 * Creates a new concurrent list and fills it with the given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			super(new ArrayList<>(Arrays.asList(array)));
		}

		/**
		 * Creates a new concurrent list with an initial capacity.
		 *
		 * @param initialCapacity the initial capacity of the backing list
		 */
		public Impl(int initialCapacity) {
			super(new ArrayList<>(initialCapacity));
		}

		/**
		 * Creates a new concurrent list and fills it with the given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty list
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new ArrayList<>() : new ArrayList<>(collection));
		}

		/**
		 * Creates a new concurrent list with the given backing list.
		 *
		 * @param backingList the backing list implementation
		 */
		protected Impl(@NotNull List<E> backingList) {
			super(backingList);
		}

		/**
		 * Constructs a {@code ConcurrentList.Impl} with a pre-built backing list and an explicit
		 * lock. Used by {@link ConcurrentUnmodifiableList.Impl} (and its variants) to install a
		 * snapshot list paired with a no-op lock for wait-free reads.
		 *
		 * @param backingList the pre-built backing list
		 * @param lock the lock guarding {@code backingList}
		 */
		protected Impl(@NotNull List<E> backingList, @NotNull ReadWriteLock lock) {
			super(backingList, lock);
		}

		/**
		 * Creates a new empty {@code ConcurrentList.Impl} instance, used internally for copy and
		 * sort operations.
		 *
		 * @return a new empty {@link ConcurrentList.Impl}
		 */
		@Override
		protected @NotNull AtomicList<E, List<E>> createEmpty() {
			return (Impl<E>) Concurrent.newList();
		}

		/**
		 * Returns a type-preserving snapshot of this list's backing reference, captured under the
		 * read lock. Subclasses backed by a different concrete {@link List} implementation override
		 * this to return an instance of that type so iteration order and structural characteristics
		 * are preserved on the snapshot.
		 *
		 * @return a fresh {@link List} containing the current elements
		 */
		protected @NotNull List<E> cloneRef() {
			try {
				this.lock.readLock().lock();
				return new ArrayList<>(this.ref);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * Returns a new {@code ConcurrentList.Impl} with elements in reverse order.
		 * The original list is not modified.
		 *
		 * @return a new reversed {@link ConcurrentList.Impl}
		 */
		@Override
		public @NotNull Impl<E> reversed() {
			return (Impl<E>) super.reversed();
		}

		/**
		 * Returns a snapshot view of the portion of this {@code ConcurrentList.Impl} between the
		 * specified {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
		 *
		 * @param fromIndex the starting index of the sublist (inclusive)
		 * @param toIndex the ending index of the sublist (exclusive)
		 * @return a new {@code ConcurrentList.Impl} representing the specified range within the list
		 * @throws IndexOutOfBoundsException if either {@code fromIndex} or {@code toIndex} is out of
		 *         range ({@code fromIndex < 0}, {@code toIndex > size()} or
		 *         {@code fromIndex > toIndex})
		 */
		@Override
		public @NotNull Impl<E> subList(int fromIndex, int toIndex) {
			return (Impl<E>) super.subList(fromIndex, toIndex);
		}

		/**
		 * Returns a new {@code ConcurrentList.Impl} containing all elements sorted in descending
		 * order according to the given comparison functions.
		 *
		 * @param sortFunctions one or more functions used to extract comparable keys for sorting
		 * @return a new sorted {@link ConcurrentList.Impl}
		 */
		@Override
		public @NotNull Impl<E> sorted(@NotNull Function<E, ? extends Comparable>... sortFunctions) {
			return (Impl<E>) super.sorted(sortFunctions);
		}

		/**
		 * Returns a new {@code ConcurrentList.Impl} containing all elements sorted according to the
		 * specified sort order and comparison functions.
		 *
		 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING}) to apply
		 * @param functions one or more functions that extract comparable keys for sorting
		 * @return a new sorted {@link ConcurrentList.Impl}
		 */
		@Override
		public @NotNull Impl<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions) {
			return (Impl<E>) super.sorted(sortOrder, functions);
		}

		/**
		 * Returns a new {@code ConcurrentList.Impl} containing all elements sorted in descending
		 * order according to the given collection of comparison functions.
		 *
		 * @param functions an iterable collection of functions used to extract comparable keys for
		 *                  sorting
		 * @return a new sorted {@link ConcurrentList.Impl}
		 */
		@Override
		public @NotNull Impl<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions) {
			return (Impl<E>) super.sorted(functions);
		}

		/**
		 * Returns a new {@code ConcurrentList.Impl} containing all elements sorted according to the
		 * specified sort order and comparison functions.
		 *
		 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING}) to apply
		 * @param functions an iterable collection of functions that extract comparable keys for
		 *                  sorting
		 * @return a new sorted {@link ConcurrentList.Impl}
		 */
		@Override
		public @NotNull Impl<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions) {
			return (Impl<E>) super.sorted(sortOrder, functions);
		}

		/**
		 * Returns a new {@code ConcurrentList.Impl} containing all elements sorted according to the
		 * specified {@link Comparator}.
		 *
		 * @param comparator the comparator used to determine the order of elements; {@code null}
		 *                   requests natural ordering
		 * @return a new sorted {@link ConcurrentList.Impl}
		 */
		@Override
		public @NotNull Impl<E> sorted(Comparator<? super E> comparator) {
			return (Impl<E>) super.sorted(comparator);
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentList.Impl}.
		 *
		 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations
		 * on this list are not reflected in the snapshot. Reads on the snapshot are wait-free.
		 * The runtime type is {@link ConcurrentUnmodifiableList.Impl}; the declared return type is
		 * the mutable parent so subclasses can covariantly override to their own
		 * {@code ConcurrentUnmodifiable*} variant.</p>
		 *
		 * @return an immutable snapshot - runtime type is {@link ConcurrentUnmodifiableList.Impl}
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableList<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableList.Impl<>(this.cloneRef());
		}

	}

}
