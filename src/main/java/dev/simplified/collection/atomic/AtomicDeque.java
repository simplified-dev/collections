package dev.simplified.collection.atomic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A thread-safe abstract double-ended queue extending {@link AtomicQueue} with {@link Deque} operations.
 * Supports element insertion and removal at both ends with atomic guarantees.
 *
 * @param <E> the type of elements held in this deque
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicDeque<E> extends AtomicQueue<E> implements Deque<E> {

	protected AtomicDeque(@NotNull Collection<? extends E> collection) {
		super(collection);
	}

	/**
	 * Constructs an {@code AtomicDeque} sharing the given source's underlying storage - the
	 * pattern used by {@code ConcurrentUnmodifiableDeque} to provide a live unmodifiable view.
	 *
	 * @param source the source deque whose storage is shared
	 */
	protected AtomicDeque(@NotNull AtomicDeque<E> source) {
		super(source);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFirst(E element) {
		this.storage.add(0, element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLast(E element) {
		super.add(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offerFirst(E element) {
		try {
			this.storage.lock.writeLock().lock();
			int before = this.storage.ref.size();
			this.storage.ref.add(0, element);
			return this.storage.ref.size() > before;
		} finally {
			this.storage.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offerLast(E element) {
		try {
			this.storage.lock.writeLock().lock();
			return this.storage.ref.add(element);
		} finally {
			this.storage.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E removeFirst() {
		return super.remove();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E removeLast() {
		E element = this.pollLast();

		if (element != null)
			return element;
		else
			throw new NoSuchElementException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E pollFirst() {
		return super.poll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable E pollLast() {
		try {
			this.storage.lock.writeLock().lock();

			if (this.storage.ref.isEmpty())
				return null;

			return this.storage.ref.remove(this.storage.ref.size() - 1);
		} finally {
			this.storage.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull E getFirst() {
		E element = this.peekFirst();

		if (element != null)
			return element;
		else
			throw new NoSuchElementException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull E getLast() {
		E element = this.peekLast();

		if (element != null)
			return element;
		else
			throw new NoSuchElementException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @Nullable E peekFirst() {
		return super.peek();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @Nullable E peekLast() {
		try {
			this.storage.lock.readLock().lock();

			if (this.storage.ref.isEmpty())
				return null;

			return this.storage.ref.get(this.storage.ref.size() - 1);
		} finally {
			this.storage.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Performs the search and removal atomically under a single write lock, so the removed
	 * occurrence is guaranteed to be the first match present at the moment of the call.
	 */
	@Override
	public boolean removeFirstOccurrence(Object obj) {
		try {
			this.storage.lock.writeLock().lock();
			return this.storage.ref.remove(obj);
		} finally {
			this.storage.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Performs the search and removal atomically under a single write lock, so the removed
	 * occurrence is guaranteed to be the last match present at the moment of the call.
	 */
	@Override
	public boolean removeLastOccurrence(Object obj) {
		try {
			this.storage.lock.writeLock().lock();
			int index = this.storage.ref.lastIndexOf(obj);

			if (index < 0)
				return false;

			this.storage.ref.remove(index);
			return true;
		} finally {
			this.storage.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void push(E element) {
		super.offer(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull E pop() {
		return this.removeFirst();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns a snapshot-backed iterator that traverses the elements of this deque in
	 * reverse order. The snapshot is taken under a read lock at the moment of the call;
	 * concurrent modifications to the backing deque are not reflected by the iterator.
	 * {@link Iterator#remove()} removes the last element returned from the underlying deque.
	 */
	@Override
	public @NotNull Iterator<E> descendingIterator() {
		final Object[] snapshot;

		try {
			this.storage.lock.readLock().lock();
			Object[] forward = this.storage.ref.toArray();
			int length = forward.length;
			snapshot = new Object[length];

			for (int i = 0; i < length; i++)
				snapshot[i] = forward[length - 1 - i];
		} finally {
			this.storage.lock.readLock().unlock();
		}

		return new DescendingIterator(snapshot);
	}

	private final class DescendingIterator implements Iterator<E> {

		private final Object[] snapshot;
		private int cursor;
		private int last;

		private DescendingIterator(Object[] snapshot) {
			this.snapshot = snapshot;
			this.cursor = 0;
			this.last = -1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return this.cursor < this.snapshot.length;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			if (!this.hasNext())
				throw new NoSuchElementException();

			return (E) this.snapshot[this.last = this.cursor++];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			if (this.last < 0)
				throw new IllegalStateException();

			try {
				AtomicDeque.this.storage.remove(this.snapshot[this.last]);
				this.last = -1;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

	}

}
