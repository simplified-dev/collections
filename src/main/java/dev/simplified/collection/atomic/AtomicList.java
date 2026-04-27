package dev.simplified.collection.atomic;

import dev.simplified.collection.query.SortOrder;
import dev.simplified.collection.query.Sortable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

/**
 * A thread-safe abstract list backed by a {@link ReadWriteLock} for concurrent access.
 * Extends {@link AtomicCollection} to provide indexed access, sorting, and list iteration with atomic guarantees.
 *
 * @param <E> the type of elements in this list
 * @param <T> the type of the underlying list
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicList<E, T extends List<E>> extends AtomicCollection<E, T> implements Sortable<E>, List<E> {

	protected AtomicList(@NotNull T type) {
		super(type);
	}

	/**
	 * Constructs an {@code AtomicList} with an explicit lock - the pattern used by
	 * {@code ConcurrentUnmodifiableList} (and its variants) to install a snapshot list
	 * paired with a no-op lock for wait-free reads.
	 *
	 * @param ref the underlying list
	 * @param lock the lock guarding {@code ref}
	 */
	protected AtomicList(@NotNull T ref, @NotNull java.util.concurrent.locks.ReadWriteLock lock) {
		super(ref, lock);
	}

	/**
	 * {@inheritDoc}
	 */
	public void add(int index, @NotNull E element) {
		try {
			super.lock.writeLock().lock();
			super.ref.add(index, element);
		} finally {
			this.invalidateSnapshot();
			super.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFirst(@NotNull E element) {
		try {
			super.lock.writeLock().lock();
			super.ref.addFirst(element);
		} finally {
			this.invalidateSnapshot();
			super.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLast(@NotNull E element) {
		try {
			super.lock.writeLock().lock();
			super.ref.addLast(element);
		} finally {
			this.invalidateSnapshot();
			super.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(int index, @NotNull Collection<? extends E> collection) {
		try {
			super.lock.writeLock().lock();
			return super.ref.addAll(index, collection);
		} finally {
			this.invalidateSnapshot();
			super.lock.writeLock().unlock();
		}
	}

	/**
	 * Creates a new empty instance of this atomic list type.
	 *
	 * @return a new empty {@code AtomicList} of the same concrete type
	 */
	protected abstract @NotNull AtomicList<E, T> createEmpty();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E get(int index) {
		try {
			super.lock.readLock().lock();
			return super.ref.get(index);
		} finally {
			super.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E getFirst() {
		try {
			this.lock.readLock().lock();

			if (this.ref.isEmpty())
				throw new NoSuchElementException();

			return this.ref.getFirst();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E getLast() {
		try {
			this.lock.readLock().lock();

			if (this.ref.isEmpty())
				throw new NoSuchElementException();

			return this.ref.getLast();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns an {@link Optional} containing the first element of this list,
	 * or an empty {@code Optional} if the list is empty.
	 *
	 * @return an {@code Optional} describing the first element, or an empty {@code Optional}
	 */
	public final @NotNull Optional<E> findFirst() {
		try {
			this.lock.readLock().lock();
			return Optional.ofNullable(!this.ref.isEmpty() ? this.ref.getFirst() : null);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns an {@link Optional} containing the last element of this list,
	 * or an empty {@code Optional} if the list is empty.
	 *
	 * @return an {@code Optional} describing the last element, or an empty {@code Optional}
	 */
	public final @NotNull Optional<E> findLast() {
		try {
			this.lock.readLock().lock();
			return Optional.ofNullable(!this.ref.isEmpty() ? this.ref.getLast() : null);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns the element at the specified index, or the default value if the index is out of bounds.
	 *
	 * @param index the index of the element to return
	 * @param defaultValue the default value to return if the index is out of bounds
	 * @return the element at the specified index, or {@code defaultValue} if the index is out of range
	 */
	public final E getOrDefault(int index, E defaultValue) {
		try {
			this.lock.readLock().lock();
			return index < this.ref.size() ? this.ref.get(index) : defaultValue;
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int indexOf(Object item) {
		try {
			this.lock.readLock().lock();
			return this.ref.indexOf(item);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull Iterator<E> iterator() {
		return this.listIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int lastIndexOf(Object item) {
		try {
			this.lock.readLock().lock();
			return this.ref.lastIndexOf(item);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull ListIterator<E> listIterator() {
		return this.listIterator(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull ListIterator<E> listIterator(int index) {
		Object[] snapshot = super.snapshotCache;

		if (snapshot == null) {
			try {
				super.lock.readLock().lock();
				snapshot = super.snapshotCache;

				if (snapshot == null) {
					snapshot = this.ref.toArray();
					super.snapshotCache = snapshot;
				}
			} finally {
				super.lock.readLock().unlock();
			}
		}

		return new ConcurrentListIterator(snapshot, index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E remove(int index) {
		try {
			super.lock.writeLock().lock();
			return super.ref.remove(index);
		} finally {
			this.invalidateSnapshot();
			super.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E removeFirst() {
		try {
			super.lock.writeLock().lock();
			return super.ref.removeFirst();
		} finally {
			this.invalidateSnapshot();
			super.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E removeLast() {
		try {
			super.lock.writeLock().lock();
			return super.ref.removeLast();
		} finally {
			this.invalidateSnapshot();
			super.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull AtomicList<E, T> reversed() {
		List<E> snapshot;
		try {
			this.lock.readLock().lock();
			snapshot = new ArrayList<>(this.ref);
		} finally {
			this.lock.readLock().unlock();
		}

		Collections.reverse(snapshot);
		AtomicList<E, T> result = this.createEmpty();
		result.ref.addAll(snapshot);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E set(int index, E element) {
		try {
			super.lock.writeLock().lock();
			return super.ref.set(index, element);
		} finally {
			this.invalidateSnapshot();
			super.lock.writeLock().unlock();
		}
	}

	/**
	 * Returns a new {@code AtomicList} containing all elements from the current list, sorted in descending order
	 * according to the specified comparison functions. The sorting is performed on a snapshot of the current list,
	 * leaving the original list unmodified.
	 *
	 * @param functions One or more functions used to extract comparable keys for sorting. The elements are sorted
	 *                  based on the first function, and subsequent functions are used when the keys are equal.
	 * @return A new {@code AtomicList} containing the sorted elements.
	 */
	@SuppressWarnings("all")
	public @NotNull AtomicList<E, T> sorted(@NotNull Function<E, ? extends Comparable>... functions) {
		return this.sorted(SortOrder.DESCENDING, functions);
	}

	/**
	 * Returns a new {@code AtomicList} containing all elements from the current list, sorted in descending order
	 * according to the specified collection of comparison functions. The sorting is performed on a snapshot of the
	 * current list, leaving the original list unmodified.
	 *
	 * @param functions An iterable collection of functions used to extract comparable keys for sorting.
	 *                  Elements are sorted by the first function, then by subsequent functions if the keys
	 *                  are equal, and so on.
	 * @return A new {@code AtomicList} containing the sorted elements.
	 */
	@SuppressWarnings("all")
	public @NotNull AtomicList<E, T> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return this.sorted(SortOrder.DESCENDING, functions);
	}

	/**
	 * Returns a new {@code AtomicList} containing all elements from the current list, sorted according to the
	 * specified sort order and comparison functions. The sorting is performed on a snapshot of the current list,
	 * leaving the original list unmodified.
	 *
	 * @param sortOrder The sort order ({@code ASCENDING} or {@code DESCENDING}) to apply while sorting the elements.
	 * @param functions A variable number of functions that extract comparable keys for sorting. Elements are sorted
	 *                  by the first function, then by the second function if the keys are equal, and so on.
	 * @return A new {@code AtomicList} containing the sorted elements.
	 */
	@SuppressWarnings("all")
	public @NotNull AtomicList<E, T> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions) {
		return this.sorted(sortOrder, Arrays.asList(functions));
	}

	/**
	 * Returns a new {@code AtomicList} containing all elements from the current list, sorted according to the
	 * specified sort order and comparison functions. The sort operation works on a snapshot of the current list,
	 * leaving the original list unmodified.
	 *
	 * @param sortOrder The sort order ({@code ASCENDING} or {@code DESCENDING}) to apply while sorting the elements.
	 * @param functions An iterable collection of functions that extract the comparable keys for sorting.
	 *                  The elements are sorted by the first function, then by the second function if the keys
	 *                  are equal, and so on.
	 * @return A new {@code AtomicList} containing the sorted elements.
	 */
	@SuppressWarnings("all")
	public @NotNull AtomicList<E, T> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		if (!functions.iterator().hasNext())
			return this;

		List<E> snapshot;
		try {
			this.lock.readLock().lock();
			snapshot = new ArrayList<>(this.ref);
		} finally {
			this.lock.readLock().unlock();
		}

		Iterator<Function<E, ? extends Comparable>> iterator = functions.iterator();
		Comparator<E> comparator = Comparator.comparing(iterator.next());

		while (iterator.hasNext())
			comparator = comparator.thenComparing(iterator.next());

		final Comparator<E> finalComparator = comparator;
		snapshot.sort(sortOrder == SortOrder.ASCENDING ? finalComparator : finalComparator.reversed());

		AtomicList<E, T> result = this.createEmpty();
		result.ref.addAll(snapshot);
		return result;
	}

	/**
	 * Returns a new {@code AtomicList} containing all elements from the current list, sorted according
	 * to the specified {@code Comparator}. The sorting is performed on a snapshot of the current list,
	 * leaving the original list unmodified.
	 *
	 * @param comparator The {@code Comparator} used to determine the order of the elements. A {@code null}
	 *                   comparator indicates that the elements' natural ordering should be used.
	 * @return A new {@code AtomicList} containing the sorted elements.
	 */
	public @NotNull AtomicList<E, T> sorted(Comparator<? super E> comparator) {
		List<E> snapshot;
		try {
			this.lock.readLock().lock();
			snapshot = new ArrayList<>(this.ref);
		} finally {
			this.lock.readLock().unlock();
		}

		snapshot.sort(comparator);
		AtomicList<E, T> result = this.createEmpty();
		result.ref.addAll(snapshot);
		return result;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sorts the underlying list in place under a single write lock, so the sort is atomic
	 * with respect to concurrent readers and writers.
	 */
	@Override
	public void sort(Comparator<? super E> comparator) {
		try {
			super.lock.writeLock().lock();
			super.ref.sort(comparator);
		} finally {
			this.invalidateSnapshot();
			super.lock.writeLock().unlock();
		}
	}

	/**
	 * Returns a view of the portion of this {@code AtomicList} between the specified {@code fromIndex}, inclusive,
	 * and {@code toIndex}, exclusive. The returned sublist is a snapshot of the current list and does
	 * not reflect subsequent modifications to the original list.
	 *
	 * @param fromIndex The starting index of the sublist (inclusive).
	 * @param toIndex The ending index of the sublist (exclusive).
	 * @return A new {@code AtomicList} representing the specified range within the list.
	 * @throws IndexOutOfBoundsException If either {@code fromIndex} or {@code toIndex}
	 *         is out of range ({@code fromIndex < 0}, {@code toIndex > size()} or
	 *         {@code fromIndex > toIndex}).
	 */
	@Override
	public @NotNull List<E> subList(int fromIndex, int toIndex) {
		List<E> snapshot;
		try {
			this.lock.readLock().lock();
			snapshot = new ArrayList<>(this.ref.subList(fromIndex, toIndex));
		} finally {
			this.lock.readLock().unlock();
		}

		AtomicList<E, T> result = this.createEmpty();
		result.ref.addAll(snapshot);
		return result;
	}

	/**
	 * A concurrent list version of {@link AtomicIterator}.
	 */
	private final class ConcurrentListIterator extends ConcurrentCollectionIterator implements ListIterator<E> {

		private ConcurrentListIterator(Object[] snapshot, int index) {
			super(snapshot, index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasPrevious() {
			return this.cursor > 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public E previous() {
			if (this.hasPrevious())
				return (E) this.snapshot[this.last = --this.cursor];
			else
				throw new NoSuchElementException();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int nextIndex() {
			return this.cursor;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int previousIndex() {
			return this.cursor - 1;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Updates the backing list under its write lock. The iterator's snapshot is not
		 * refreshed, so subsequent calls to {@link #previous()} that re-read the updated
		 * position will still return the snapshot-time value - typical forward iteration
		 * is unaffected.
		 */
		@Override
		public void set(E element) {
			if (this.last < 0)
				throw new IllegalStateException();

			try {
				AtomicList.this.set(this.last, element);
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Inserts into the backing list under its write lock. The iterator's snapshot is
		 * not refreshed, so the inserted element is invisible to this iterator's remaining
		 * iteration - {@link #next()} continues returning the snapshot sequence unchanged.
		 * This is the weakly consistent semantic shared with all other snapshot iterators
		 * in this package.
		 */
		@Override
		public void add(E element) {
			try {
				AtomicList.this.add(this.cursor, element);
				this.last = -1;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

	}

}
