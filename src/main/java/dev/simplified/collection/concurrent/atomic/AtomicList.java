package dev.sbs.api.collection.concurrent.atomic;

import dev.sbs.api.collection.concurrent.iterator.ConcurrentIterator;
import dev.sbs.api.collection.query.SortOrder;
import dev.sbs.api.collection.query.Sortable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public abstract class AtomicList<E, T extends List<E>> extends AtomicCollection<E, T> implements Sortable<E>, List<E> {

	protected AtomicList(@NotNull T type) {
		super(type);
	}

	/**
	 * {@inheritDoc}
	 */
	public void add(int index, @NotNull E element) {
		try {
			super.lock.writeLock().lock();
			super.ref.add(index, element);
		} finally {
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
			super.lock.writeLock().unlock();
		}
	}

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

	public final @NotNull Optional<E> getFirst() {
		try {
			this.lock.readLock().lock();
			return Optional.ofNullable(!this.ref.isEmpty() ? this.ref.get(0) : null);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	public final @NotNull Optional<E> getLast() {
		try {
			this.lock.readLock().lock();
			return Optional.ofNullable(!this.ref.isEmpty() ? this.ref.get(this.ref.size() - 1) : null);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	public final E getOrDefault(int index, E defaultValue) {
		return index < this.size() ? this.get(index) : defaultValue;
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
	 * Returns a new {@code List} containing all elements from the current list in reverse order.
	 * The reversal is performed on a snapshot of the current list, ensuring that the original
	 * list remains unmodified.
	 *
	 * @return A new {@code List} containing the elements of the current list in reverse order.
	 */
	public @NotNull List<E> inverse() {
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
		return new ConcurrentListIterator(this.ref.toArray(), index);
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
			super.lock.writeLock().unlock();
		}
	}

	public final E removeFirst() {
		return this.remove(0);
	}

	public final E removeLast() {
		return this.remove(this.size() - 1);
	}

	@Override
	public E set(int index, E element) {
		try {
			super.lock.writeLock().lock();
			return super.ref.set(index, element);
		} finally {
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
	 */
	@Override
	public void sort(Comparator<? super E> comparator) {
		List.super.sort(comparator);
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
	 * A concurrent list version of {@link ConcurrentIterator}.
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
		 */
		@Override
		public void set(E element) {
			if (this.last < 0)
				throw new IllegalStateException();

			try {
				AtomicList.this.set(this.last, element);
				this.snapshot = AtomicList.this.toArray();
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void add(E element) {
			this.snapshot = AtomicList.this.toArray();

			try {
				AtomicList.this.add(this.cursor++, element);
				this.last = -1;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

	}

}
