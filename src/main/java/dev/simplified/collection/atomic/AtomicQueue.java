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
	 * {@inheritDoc}
	 */
	@Override
	public final boolean add(E element) {
		return super.add(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		return this.storage.addAll(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void clear() {
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
	public final boolean offer(E element) {
		return this.storage.add(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @Nullable E peek() {
		return this.isEmpty() ? null : this.storage.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @Nullable E poll() {
		return this.isEmpty() ? null : this.storage.remove(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull E remove() {
		return super.remove();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean remove(Object obj) {
		return super.remove(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean removeAll(@NotNull Collection<?> collection) {
		return this.storage.removeAll(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean retainAll(@NotNull Collection<?> collection) {
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
