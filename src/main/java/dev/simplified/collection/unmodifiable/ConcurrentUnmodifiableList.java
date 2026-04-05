package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.query.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An unmodifiable thread-safe list backed by an {@link ArrayList} that permits concurrent reads
 * but rejects all modifications. Mutating operations throw {@link UnsupportedOperationException}.
 * Derived operations ({@code reversed}, {@code sorted}, {@code subList}) return new unmodifiable copies.
 *
 * @param <E> the type of elements in this list
 */
@SuppressWarnings("all")
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void add(int index, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean add(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean addAll(int index, @NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns a reversed copy of this list, wrapped as an unmodifiable list.
	 */
	@Override
	public final @NotNull ConcurrentUnmodifiableList<E> reversed() {
		return Concurrent.newUnmodifiableList(super.reversed());
	}

	/**
	 * Returns a sorted copy of this list using the given sort functions, wrapped as an unmodifiable list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableList<E> sorted(@NotNull Function<E, ? extends Comparable>... sortFunctions) {
		return Concurrent.newUnmodifiableList(super.sorted(sortFunctions));
	}

	/**
	 * Returns a sorted copy of this list using the given sort order and functions, wrapped as an unmodifiable list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableList<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions) {
		return Concurrent.newUnmodifiableList(super.sorted(sortOrder, functions));
	}

	/**
	 * Returns a sorted copy of this list using the given sort functions, wrapped as an unmodifiable list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableList<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return Concurrent.newUnmodifiableList(super.sorted(functions));
	}

	/**
	 * Returns a sorted copy of this list using the given sort order and functions, wrapped as an unmodifiable list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableList<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return Concurrent.newUnmodifiableList(super.sorted(sortOrder, functions));
	}

	/**
	 * Returns a sorted copy of this list using the given comparator, wrapped as an unmodifiable list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableList<E> sorted(Comparator<? super E> comparator) {
		return Concurrent.newUnmodifiableList(super.sorted(comparator));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned iterator does not support {@code remove}, {@code set}, or {@code add} operations.
	 */
	@Override
	public final @NotNull ListIterator<E> listIterator(int index) {
		return new ListIterator<>() {

            private final ListIterator<? extends E> atomicIterator = ConcurrentUnmodifiableList.super.listIterator(index);

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return atomicIterator.hasNext();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public E next() {
                return atomicIterator.next();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasPrevious() {
                return atomicIterator.hasPrevious();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public E previous() {
                return atomicIterator.previous();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int nextIndex() {
                return atomicIterator.nextIndex();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int previousIndex() {
                return atomicIterator.previousIndex();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void set(E e) {
                throw new UnsupportedOperationException();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void add(E e) {
                throw new UnsupportedOperationException();
            }

        };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean remove(Object element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean removeAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void sort(Comparator<? super E> comparator) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns an unmodifiable view of a portion of this list between the specified indices.
	 */
	@Override
	public final @NotNull ConcurrentList<E> subList(int fromIndex, int toIndex) {
		return Concurrent.newUnmodifiableList(super.subList(fromIndex, toIndex));
	}

}
