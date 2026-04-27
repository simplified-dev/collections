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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
	 * Returns a fresh mutable {@link List} containing the current contents of this list, captured
	 * atomically under the read lock. Subclasses may override to choose the snapshot's concrete
	 * {@link List} type; the snapshot is the working buffer used by {@link #sorted}, {@link #reversed},
	 * and {@link #subList}.
	 *
	 * @return a fresh {@link List} containing the current elements
	 */
	@NotNull List<E> snapshot();

	/**
	 * Returns a new empty instance of this list's runtime type. Used by {@link #sorted},
	 * {@link #reversed}, and {@link #subList} to materialize their result so the result preserves
	 * the source's backing-list characteristics.
	 *
	 * @return a new empty {@link ConcurrentList} of the same concrete type
	 */
	@NotNull ConcurrentList<E> newEmpty();

	/**
	 * Returns a new list containing all elements from this list, sorted in descending order
	 * according to the specified comparison functions. The original list is not modified.
	 *
	 * @param functions one or more functions used to extract comparable keys for sorting
	 * @return a new sorted list
	 */
	default @NotNull ConcurrentList<E> sorted(@NotNull Function<E, ? extends Comparable<?>>... functions) {
		return this.sorted(SortOrder.DESCENDING, Arrays.asList(functions));
	}

	/**
	 * Returns a new list containing all elements from this list, sorted in descending order
	 * according to the specified collection of comparison functions. The original list is not
	 * modified.
	 *
	 * @param functions an iterable collection of functions used to extract comparable keys for
	 *                  sorting
	 * @return a new sorted list
	 */
	default @NotNull ConcurrentList<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable<?>>> functions) {
		return this.sorted(SortOrder.DESCENDING, functions);
	}

	/**
	 * Returns a new list containing all elements from this list, sorted according to the
	 * specified sort order and comparison functions. The original list is not modified.
	 *
	 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING})
	 * @param functions one or more functions that extract comparable keys for sorting
	 * @return a new sorted list
	 */
	default @NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable<?>>... functions) {
		return this.sorted(sortOrder, Arrays.asList(functions));
	}

	/**
	 * Returns a new list containing all elements from this list, sorted according to the
	 * specified sort order and comparison functions. The original list is not modified.
	 *
	 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING})
	 * @param functions an iterable collection of functions that extract comparable keys for
	 *                  sorting
	 * @return a new sorted list
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default @NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable<?>>> functions) {
		Iterator<Function<E, ? extends Comparable<?>>> iterator = functions.iterator();

		if (!iterator.hasNext())
			return this;

		Comparator<E> comparator = Comparator.comparing((Function) iterator.next());

		while (iterator.hasNext()) {
			Function<E, ? extends Comparable> next = iterator.next();
			comparator = comparator.thenComparing(next);
		}

		List<E> snapshot = this.snapshot();
		snapshot.sort(sortOrder == SortOrder.ASCENDING ? comparator : comparator.reversed());

		ConcurrentList<E> result = this.newEmpty();
		result.addAll(snapshot);
		return result;
	}

	/**
	 * Returns a new list containing all elements from this list, sorted according to the given
	 * comparator. The original list is not modified.
	 *
	 * @param comparator the comparator used to order the elements; {@code null} requests natural
	 *                   ordering
	 * @return a new sorted list
	 */
	default @NotNull ConcurrentList<E> sorted(Comparator<? super E> comparator) {
		List<E> snapshot = this.snapshot();
		snapshot.sort(comparator);
		ConcurrentList<E> result = this.newEmpty();
		result.addAll(snapshot);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default @NotNull ConcurrentList<E> reversed() {
		List<E> snapshot = this.snapshot();
		Collections.reverse(snapshot);
		ConcurrentList<E> result = this.newEmpty();
		result.addAll(snapshot);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default @NotNull ConcurrentList<E> subList(int fromIndex, int toIndex) {
		List<E> snapshot = this.snapshot();
		ConcurrentList<E> result = this.newEmpty();
		result.addAll(snapshot.subList(fromIndex, toIndex));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableList<E> toUnmodifiable();

	/**
	 * Wraps {@code backing} as a {@link ConcurrentList} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to
	 * {@code backing} bypass the read/write lock and may corrupt concurrent reads. Use this for
	 * zero-copy publication of single-threaded build results.
	 *
	 * @param backing the list to adopt
	 * @param <E> the element type
	 * @return a concurrent list backed by {@code backing}
	 */
	static <E> @NotNull ConcurrentList<E> adopt(@NotNull List<E> backing) {
		return new Impl<>(backing);
	}

	/**
	 * A thread-safe list backed by an {@link ArrayList} with concurrent read and write access
	 * via {@link ReadWriteLock}. Supports indexed access, sorting, and snapshot-based iteration.
	 *
	 * @param <E> the type of elements in this list
	 */
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
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		protected @NotNull AtomicList<E, List<E>> createEmpty() {
			return (ConcurrentList.Impl<E>) Concurrent.newList();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull List<E> snapshot() {
			try {
				this.lock.readLock().lock();
				return new ArrayList<>(this.ref);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull ConcurrentList<E> newEmpty() {
			return Concurrent.newList();
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
			return new ConcurrentUnmodifiableList.Impl<>(this.snapshot());
		}

	}

}
