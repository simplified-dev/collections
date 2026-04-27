package dev.simplified.collection.atomic;

import dev.simplified.collection.ConcurrentQueue;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe abstract queue backed by a {@link ReadWriteLock} for concurrent access.
 * Extends {@link AtomicCollection} to add FIFO queue operations ({@link Queue#offer},
 * {@link Queue#peek}, {@link Queue#poll}) with atomic guarantees.
 *
 * @param <E> the type of elements held in this queue
 * @param <T> the type of the underlying queue, which must be both an {@link AbstractCollection}
 *            and a {@link Queue}
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicQueue<E, T extends AbstractCollection<E> & Queue<E>> extends AtomicCollection<E, T> implements ConcurrentQueue<E> {

	protected AtomicQueue(@NotNull T ref) {
		super(ref);
	}

	/**
	 * Constructs an {@code AtomicQueue} with an explicit lock - the pattern used by
	 * {@code ConcurrentUnmodifiableQueue} to install a snapshot queue paired with a no-op lock for
	 * wait-free reads.
	 *
	 * @param ref the underlying queue
	 * @param lock the lock guarding {@code ref}
	 */
	protected AtomicQueue(@NotNull T ref, @NotNull ReadWriteLock lock) {
		super(ref, lock);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offer(E element) {
		return this.withWriteLock(() -> this.ref.offer(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E peek() {
		return this.withReadLock((java.util.function.Supplier<E>) this.ref::peek);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E poll() {
		return this.withWriteLock((java.util.function.Supplier<E>) this.ref::poll);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull E remove() {
		E head = this.poll();

		if (head == null)
			throw new NoSuchElementException();

		return head;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull E element() {
		E head = this.peek();

		if (head == null)
			throw new NoSuchElementException();

		return head;
	}

}
