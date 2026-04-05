package dev.simplified.collection.atomic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
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
	 * {@inheritDoc}
	 */
	@Override
	public final void addFirst(E element) {
		super.storage.add(0, element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addLast(E element) {
		super.add(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean offerFirst(E element) {
		int before = super.size();
		this.addFirst(element);
		return super.size() > before;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean offerLast(E element) {
		int before = super.size();
		this.addLast(element);
		return super.size() > before;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E removeFirst() {
		return super.remove();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E removeLast() {
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
	public final E pollFirst() {
		return super.poll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @Nullable E pollLast() {
		if (super.isEmpty())
			return null;
		else {
			E element = super.storage.get(super.size() - 1);
			super.remove(element);
			return element;
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
		if (super.isEmpty())
			return null;
		else
			return super.storage.get(super.size() - 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean removeFirstOccurrence(Object obj) {
		Iterator<E> iterator = super.iterator();

		while (iterator.hasNext()) {
			E element = iterator.next();

			if (element.equals(obj)) {
				super.remove(obj);
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean removeLastOccurrence(Object obj) {
		Iterator<E> iterator = this.descendingIterator();

		while (iterator.hasNext()) {
			E element = iterator.next();

			if (element.equals(obj)) {
				super.remove(obj);
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void push(E element) {
		super.offer(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull E pop() {
		return this.removeFirst();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull Iterator<E> descendingIterator() {
		throw new UnsupportedOperationException("This is currently not implemented");
	}

}
