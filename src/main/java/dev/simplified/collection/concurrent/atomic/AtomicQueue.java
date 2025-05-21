package dev.sbs.api.collection.concurrent.atomic;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.linked.ConcurrentLinkedList;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public abstract class AtomicQueue<E> extends AbstractQueue<E> implements Queue<E> {

	protected final @NotNull ConcurrentLinkedList<E> storage;

	protected AtomicQueue(@NotNull Collection<? extends E> collection) {
		this.storage = Concurrent.newLinkedList(collection);
	}

	@Override
	public final boolean add(E element) {
		return super.add(element);
	}

	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		return this.storage.addAll(collection);
	}

	@Override
	public final void clear() {
		super.clear();
	}

	@Override
	public final boolean contains(Object obj) {
		return this.storage.contains(obj);
	}

	@Override
	public final boolean containsAll(@NotNull Collection<?> collection) {
		return this.storage.containsAll(collection);
	}

	@Override
	public final E element() {
		return super.element();
	}

	@Override
	public final boolean isEmpty() {
		return this.storage.isEmpty();
	}

	@Override
	public final @NotNull Iterator<E> iterator() {
		return this.storage.iterator();
	}

	@Override
	public final boolean offer(E element) {
		return this.storage.add(element);
	}

	@Override
	public final E peek() {
		return this.isEmpty() ? null : this.storage.get(0);
	}

	@Override
	public final E poll() {
		return this.isEmpty() ? null : this.storage.remove(0);
	}

	@Override
	public final E remove() {
		return super.remove();
	}

	@Override
	public final boolean remove(Object obj) {
		return super.remove(obj);
	}

	@Override
	public final boolean removeAll(@NotNull Collection<?> collection) {
		return this.storage.removeAll(collection);
	}

	@Override
	public final boolean retainAll(@NotNull Collection<?> collection) {
		return this.storage.retainAll(collection);
	}

	@Override
	public final int size() {
		return this.storage.size();
	}

	@Override
	public final @NotNull Object[] toArray() {
		return this.storage.toArray();
	}

	@Override
	public final @NotNull <T> T[] toArray(@NotNull T[] array) {
		return this.storage.toArray(array);
	}

}
