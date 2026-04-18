package dev.simplified.collection.atomic;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * A thread-safe abstract queue backed by a {@link ConcurrentLinkedList} for concurrent access.
 * Provides atomic FIFO queue operations with element ordering guarantees.
 *
 * @param <E> the type of elements held in this queue
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicQueue<E> extends AbstractQueue<E> implements Queue<E> {

	protected final @NotNull ConcurrentLinkedList<E> storage;

	protected AtomicQueue(@NotNull Collection<? extends E> collection) {
		this.storage = Concurrent.newLinkedList(collection);
	}

	/**
	 * Constructs an {@code AtomicQueue} sharing the given source's underlying storage - the
	 * pattern used by {@code ConcurrentUnmodifiableQueue} to provide a live unmodifiable view.
	 *
	 * @param source the source queue whose storage is shared
	 */
	protected AtomicQueue(@NotNull AtomicQueue<E> source) {
		this.storage = source.storage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(E element) {
		return super.add(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(@NotNull Collection<? extends E> collection) {
		return this.storage.addAll(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		super.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean contains(Object obj) {
		return this.storage.contains(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containsAll(@NotNull Collection<?> collection) {
		return this.storage.containsAll(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E element() {
		return super.element();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isEmpty() {
		return this.storage.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull Iterator<E> iterator() {
		return this.storage.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offer(E element) {
		return this.storage.add(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable E peek() {
		try {
			this.storage.lock.readLock().lock();
			return this.storage.ref.isEmpty() ? null : this.storage.ref.get(0);
		} finally {
			this.storage.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable E poll() {
		try {
			this.storage.lock.writeLock().lock();
			return this.storage.ref.isEmpty() ? null : this.storage.ref.remove(0);
		} finally {
			this.storage.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull E remove() {
		return super.remove();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object obj) {
		return super.remove(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		return this.storage.removeAll(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		return this.storage.retainAll(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size() {
		return this.storage.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Object @NotNull [] toArray() {
		return this.storage.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final <T> T @NotNull [] toArray(T @NotNull [] array) {
		return this.storage.toArray(array);
	}

}
