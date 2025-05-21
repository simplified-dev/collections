package dev.sbs.api.collection.concurrent.iterator;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class ConcurrentIterator<E> implements Iterator<E> {

	/** Snapshot of the array */
	protected Object[] snapshot;

	/** Index of element to be returned by subsequent call to next. */
	protected int cursor;

	/** Index of last element to be returned. */
	protected int last = -1;

	protected ConcurrentIterator(Object[] snapshot, int index) {
		this.cursor = index;
		this.snapshot = snapshot;
	}

	@Override
	public boolean hasNext() {
		return this.cursor < this.snapshot.length;
	}

	@Override
	public @NotNull E next() {
		if (this.hasNext())
			return (E) this.snapshot[this.last = this.cursor++];
		else
			throw new NoSuchElementException();
	}

	public abstract void remove();

	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		Objects.requireNonNull(action);
		final int size = this.snapshot.length;
		int i = this.cursor;

		if (i < size) {
			for (; i < size; i++)
				action.accept((E) this.snapshot[i]);

			this.cursor = i;
			this.last = i - 1;
		}
	}

}