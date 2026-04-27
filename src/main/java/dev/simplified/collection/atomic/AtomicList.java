package dev.simplified.collection.atomic;

import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.query.SortOrder;
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
public abstract class AtomicList<E, T extends List<E>> extends AtomicCollection<E, T> implements ConcurrentList<E> {

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
		this.withWriteLock(() -> super.ref.add(index, element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFirst(@NotNull E element) {
		this.withWriteLock(() -> super.ref.addFirst(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLast(@NotNull E element) {
		this.withWriteLock(() -> super.ref.addLast(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(int index, @NotNull Collection<? extends E> collection) {
		return this.withWriteLock(() -> super.ref.addAll(index, collection));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E get(int index) {
		return this.withReadLock(() -> super.ref.get(index));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E getFirst() {
		return this.withReadLock(() -> {
			if (this.ref.isEmpty())
				throw new NoSuchElementException();

			return this.ref.getFirst();
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E getLast() {
		return this.withReadLock(() -> {
			if (this.ref.isEmpty())
				throw new NoSuchElementException();

			return this.ref.getLast();
		});
	}

	/**
	 * Returns an {@link Optional} containing the first element of this list,
	 * or an empty {@code Optional} if the list is empty.
	 *
	 * @return an {@code Optional} describing the first element, or an empty {@code Optional}
	 */
	public final @NotNull Optional<E> findFirst() {
		return this.withReadLock(() -> Optional.ofNullable(!this.ref.isEmpty() ? this.ref.getFirst() : null));
	}

	/**
	 * Returns an {@link Optional} containing the last element of this list,
	 * or an empty {@code Optional} if the list is empty.
	 *
	 * @return an {@code Optional} describing the last element, or an empty {@code Optional}
	 */
	public final @NotNull Optional<E> findLast() {
		return this.withReadLock(() -> Optional.ofNullable(!this.ref.isEmpty() ? this.ref.getLast() : null));
	}

	/**
	 * Returns the element at the specified index, or the default value if the index is out of bounds.
	 *
	 * @param index the index of the element to return
	 * @param defaultValue the default value to return if the index is out of bounds
	 * @return the element at the specified index, or {@code defaultValue} if the index is out of range
	 */
	public final E getOrDefault(int index, E defaultValue) {
		return this.withReadLock(() -> index < this.ref.size() ? this.ref.get(index) : defaultValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int indexOf(Object item) {
		return this.withReadLock(() -> this.ref.indexOf(item));
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
		return this.withReadLock(() -> this.ref.lastIndexOf(item));
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
		return this.withWriteLock(() -> super.ref.remove(index));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E removeFirst() {
		return this.withWriteLock((java.util.function.Supplier<E>) super.ref::removeFirst);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E removeLast() {
		return this.withWriteLock((java.util.function.Supplier<E>) super.ref::removeLast);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E set(int index, E element) {
		return this.withWriteLock(() -> super.ref.set(index, element));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sorts the underlying list in place under a single write lock, so the sort is atomic
	 * with respect to concurrent readers and writers.
	 */
	@Override
	public void sort(Comparator<? super E> comparator) {
		this.withWriteLock(() -> super.ref.sort(comparator));
	}

	/**
	 * Returns a fresh mutable {@link List} containing the current contents of this list, captured
	 * atomically under the read lock. Subclasses backed by a different concrete {@link List}
	 * implementation override this to return an instance of that type so iteration order and
	 * structural characteristics are preserved on the snapshot.
	 *
	 * @return a fresh {@link List} containing the current elements
	 */
	protected @NotNull List<E> snapshot() {
		return this.withReadLock(() -> new ArrayList<>(this.ref));
	}

	/**
	 * Returns a new empty instance of this list's runtime type. Used by {@link #sorted},
	 * {@link #reversed}, and {@link #subList} to materialize their result.
	 *
	 * @return a new empty {@code AtomicList} of the same concrete type
	 */
	protected abstract @NotNull AtomicList<E, T> newEmpty();

	/**
	 * Returns a new list containing all elements from this list, sorted in descending order
	 * according to the specified comparison functions. The original list is not modified.
	 *
	 * @param functions one or more functions used to extract comparable keys for sorting
	 * @return a new sorted list
	 */
	@Override
	@SuppressWarnings("unchecked")
	public @NotNull AtomicList<E, T> sorted(@NotNull Function<E, ? extends Comparable<?>>... functions) {
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
	@Override
	public @NotNull AtomicList<E, T> sorted(@NotNull Iterable<Function<E, ? extends Comparable<?>>> functions) {
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
	@Override
	@SuppressWarnings("unchecked")
	public @NotNull AtomicList<E, T> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable<?>>... functions) {
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
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public @NotNull AtomicList<E, T> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable<?>>> functions) {
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

		AtomicList<E, T> result = this.newEmpty();
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
	@Override
	public @NotNull AtomicList<E, T> sorted(Comparator<? super E> comparator) {
		List<E> snapshot = this.snapshot();
		snapshot.sort(comparator);
		AtomicList<E, T> result = this.newEmpty();
		result.addAll(snapshot);
		return result;
	}

	/**
	 * Returns a new list containing all elements of this list in reverse order. The original list
	 * is not modified.
	 *
	 * @return a new reversed list
	 */
	@Override
	public @NotNull AtomicList<E, T> reversed() {
		List<E> snapshot = this.snapshot();
		Collections.reverse(snapshot);
		AtomicList<E, T> result = this.newEmpty();
		result.addAll(snapshot);
		return result;
	}

	/**
	 * Returns a snapshot view of the portion of this list between the specified {@code fromIndex},
	 * inclusive, and {@code toIndex}, exclusive. The returned sublist is a fresh list and does not
	 * reflect subsequent modifications to the original.
	 *
	 * @param fromIndex the starting index of the sublist (inclusive)
	 * @param toIndex the ending index of the sublist (exclusive)
	 * @return a new list representing the specified range
	 * @throws IndexOutOfBoundsException if either index is out of range
	 */
	@Override
	public @NotNull AtomicList<E, T> subList(int fromIndex, int toIndex) {
		List<E> snapshot = this.snapshot();
		AtomicList<E, T> result = this.newEmpty();
		result.addAll(snapshot.subList(fromIndex, toIndex));
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
