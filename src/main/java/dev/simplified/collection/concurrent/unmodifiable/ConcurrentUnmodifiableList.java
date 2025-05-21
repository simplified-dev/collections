package dev.sbs.api.collection.concurrent.unmodifiable;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * An unmodifiable concurrent list that allows for simultaneous fast reading and iteration utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the list by replacing the
 * entire list on each modification. This allows for maintaining the original
 * speed of {@link ArrayList#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
public class ConcurrentUnmodifiableList<E> extends ConcurrentList<E> {

	/**
	 * Create a new unmodifiable concurrent list.
	 */
	public ConcurrentUnmodifiableList() {
		super();
	}

	/**
	 * Create a new unmodifiable concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentUnmodifiableList(@NotNull E... array) {
		super(array);
	}

	/**
	 * Create a new unmodifiable concurrent list and fill it with the given collection.
	 */
	public ConcurrentUnmodifiableList(@Nullable Collection<? extends E> collection) {
		super(collection);
	}

	@Override
	public final void add(int index, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean add(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean addAll(int index, @NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final @NotNull ConcurrentUnmodifiableList<E> inverse() {
		ConcurrentList<E> list = Concurrent.newList(this);
		Collections.reverse(list);
		return Concurrent.newUnmodifiableList(list);
	}

	@Override
	public final @NotNull ListIterator<E> listIterator(int index) {
		return new ListIterator<>() {

            private final ListIterator<? extends E> atomicIterator = ConcurrentUnmodifiableList.super.listIterator(index);

            @Override
            public boolean hasNext() {
                return atomicIterator.hasNext();
            }

            @Override
            public E next() {
                return atomicIterator.next();
            }

            @Override
            public boolean hasPrevious() {
                return atomicIterator.hasPrevious();
            }

            @Override
            public E previous() {
                return atomicIterator.previous();
            }

            @Override
            public int nextIndex() {
                return atomicIterator.nextIndex();
            }

            @Override
            public int previousIndex() {
                return atomicIterator.previousIndex();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(E e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(E e) {
                throw new UnsupportedOperationException();
            }

        };
	}

	@Override
	public final E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean remove(Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean removeAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void sort(Comparator<? super E> comparator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final @NotNull ConcurrentList<E> subList(int fromIndex, int toIndex) {
		return Concurrent.newUnmodifiableList(super.subList(fromIndex, toIndex));
	}

}
