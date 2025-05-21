package dev.sbs.api.collection.concurrent.atomic;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AtomicDeque<E> extends AtomicQueue<E> implements Deque<E> {

	protected AtomicDeque(@NotNull Collection<? extends E> collection) {
		super(collection);
	}

	@Override
	public final void addFirst(E element) {
		super.storage.add(0, element);
	}

	@Override
	public final void addLast(E element) {
		super.add(element);
	}

	@Override
	public final boolean offerFirst(E element) {
		int before = super.size();
		this.addFirst(element);
		return super.size() > before;
	}

	@Override
	public final boolean offerLast(E element) {
		int before = super.size();
		this.addLast(element);
		return super.size() > before;
	}

	@Override
	public final E removeFirst() {
		return super.remove();
	}

	@Override
	public final E removeLast() {
		E element = this.pollLast();

		if (element != null)
			return element;
		else
			throw new NoSuchElementException();
	}

	@Override
	public final E pollFirst() {
		return super.poll();
	}

	@Override
	public final E pollLast() {
		if (super.isEmpty())
			return null;
		else {
			E element = super.storage.get(super.size() - 1);
			super.remove(element);
			return element;
		}
	}

	@Override
	public final E getFirst() {
		E element = this.peekFirst();

		if (element != null)
			return element;
		else
			throw new NoSuchElementException();
	}

	@Override
	public final E getLast() {
		E element = this.peekLast();

		if (element != null)
			return element;
		else
			throw new NoSuchElementException();
	}

	@Override
	public final E peekFirst() {
		return super.peek();
	}

	@Override
	public final E peekLast() {
		if (super.isEmpty())
			return null;
		else
			return super.storage.get(super.size() - 1);
	}

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

	@Override
	public final void push(E element) {
		super.offer(element);
	}

	@Override
	public final E pop() {
		return this.removeFirst();
	}

	@Override
	public @NotNull Iterator<E> descendingIterator() {
		throw new UnsupportedOperationException("This is currently not implemented!");
	}

}
