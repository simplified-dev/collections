package dev.simplified.collection.atomic;

import dev.simplified.collection.ConcurrentDeque;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe abstract double-ended queue extending {@link AtomicQueue} with {@link Deque}
 * operations. Supports element insertion and removal at both ends with atomic guarantees.
 *
 * @param <E> the type of elements held in this deque
 * @param <T> the type of the underlying deque, which must be both an {@link AbstractCollection}
 *            and a {@link Deque}
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicDeque<E, T extends AbstractCollection<E> & Deque<E>> extends AtomicQueue<E, T> implements ConcurrentDeque<E> {

	protected AtomicDeque(@NotNull T ref) {
		super(ref);
	}

	/**
	 * Constructs an {@code AtomicDeque} with an explicit lock - the pattern used by
	 * {@code ConcurrentUnmodifiableDeque} to install a snapshot deque paired with a no-op lock for
	 * wait-free reads.
	 *
	 * @param ref the underlying deque
	 * @param lock the lock guarding {@code ref}
	 */
	protected AtomicDeque(@NotNull T ref, @NotNull ReadWriteLock lock) {
		super(ref, lock);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFirst(E element) {
		this.withWriteLock(() -> this.ref.addFirst(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLast(E element) {
		this.withWriteLock(() -> this.ref.addLast(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offerFirst(E element) {
		return this.withWriteLock(() -> this.ref.offerFirst(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offerLast(E element) {
		return this.withWriteLock(() -> this.ref.offerLast(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E removeFirst() {
		E head = this.pollFirst();

		if (head == null)
			throw new NoSuchElementException();

		return head;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E removeLast() {
		E tail = this.pollLast();

		if (tail == null)
			throw new NoSuchElementException();

		return tail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E pollFirst() {
		return this.withWriteLock((java.util.function.Supplier<E>) this.ref::pollFirst);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E pollLast() {
		return this.withWriteLock((java.util.function.Supplier<E>) this.ref::pollLast);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull E getFirst() {
		E head = this.peekFirst();

		if (head == null)
			throw new NoSuchElementException();

		return head;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull E getLast() {
		E tail = this.peekLast();

		if (tail == null)
			throw new NoSuchElementException();

		return tail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E peekFirst() {
		return this.withReadLock((java.util.function.Supplier<E>) this.ref::peekFirst);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E peekLast() {
		return this.withReadLock((java.util.function.Supplier<E>) this.ref::peekLast);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Performs the search and removal atomically under a single write lock, so the removed
	 * occurrence is guaranteed to be the first match present at the moment of the call.
	 */
	@Override
	public boolean removeFirstOccurrence(Object obj) {
		return this.withWriteLock(() -> this.ref.removeFirstOccurrence(obj));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Performs the search and removal atomically under a single write lock, so the removed
	 * occurrence is guaranteed to be the last match present at the moment of the call.
	 */
	@Override
	public boolean removeLastOccurrence(Object obj) {
		return this.withWriteLock(() -> this.ref.removeLastOccurrence(obj));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void push(E element) {
		this.addFirst(element);
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
	 * Returns a snapshot-backed iterator that traverses the elements of this deque in reverse
	 * order. The snapshot is taken under a read lock at the moment of the call; concurrent
	 * modifications to the backing deque are not reflected by the iterator.
	 * {@link Iterator#remove()} removes the last element returned from the underlying deque.
	 */
	@Override
	public @NotNull Iterator<E> descendingIterator() {
		Object[] snapshot = this.withReadLock(() -> {
			Object[] arr = new Object[this.ref.size()];
			int i = 0;
			for (Iterator<E> it = this.ref.descendingIterator(); it.hasNext(); )
				arr[i++] = it.next();
			return arr;
		});

		return new DescendingIterator(snapshot);
	}

	private final class DescendingIterator implements Iterator<E> {

		private final Object[] snapshot;
		private int cursor;
		private int last;

		private DescendingIterator(@NotNull Object[] snapshot) {
			this.snapshot = snapshot;
			this.cursor = 0;
			this.last = -1;
		}

		@Override
		public boolean hasNext() {
			return this.cursor < this.snapshot.length;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			if (!this.hasNext())
				throw new NoSuchElementException();

			return (E) this.snapshot[this.last = this.cursor++];
		}

		@Override
		public void remove() {
			if (this.last < 0)
				throw new IllegalStateException();

			try {
				AtomicDeque.this.remove(this.snapshot[this.last]);
				this.last = -1;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

	}

}
