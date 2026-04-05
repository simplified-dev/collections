package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import dev.simplified.collection.query.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An unmodifiable thread-safe list backed by a {@link java.util.LinkedList} that permits concurrent reads
 * but rejects all modifications. Mutating operations throw {@link UnsupportedOperationException}.
 * Derived operations ({@code reversed}, {@code sorted}, {@code subList}) return new unmodifiable copies.
 *
 * @param <E> the type of elements in this list
 */
@SuppressWarnings("all")
public class ConcurrentUnmodifiableLinkedList<E> extends ConcurrentLinkedList<E> {

	/**
	 * Create a new unmodifiable concurrent list.
	 */
	public ConcurrentUnmodifiableLinkedList() {
		super();
	}

	/**
	 * Create a new unmodifiable concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentUnmodifiableLinkedList(@NotNull E... array) {
		super(array);
	}

	/**
	 * Create a new unmodifiable concurrent list and fill it with the given collection.
	 */
	public ConcurrentUnmodifiableLinkedList(@Nullable Collection<? extends E> collection) {
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
	public final boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
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
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns a reversed copy of this linked list, wrapped as an unmodifiable linked list.
	 */
	@Override
	public final @NotNull ConcurrentUnmodifiableLinkedList<E> reversed() {
		return Concurrent.newUnmodifiableLinkedList(super.reversed());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned iterator does not support {@code remove}, {@code set}, or {@code add} operations.
	 */
	@Override
	public final @NotNull ListIterator<E> listIterator(int index) {
		return new ListIterator<>() {

            private final ListIterator<? extends E> atomicIterator = ConcurrentUnmodifiableLinkedList.super.listIterator(index);

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
	 * Returns a sorted copy of this linked list using the given sort functions, wrapped as an unmodifiable linked list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableLinkedList<E> sorted(@NotNull Function<E, ? extends Comparable>... sortFunctions) {
		return Concurrent.newUnmodifiableLinkedList(super.sorted(sortFunctions));
	}

	/**
	 * Returns a sorted copy of this linked list using the given sort order and functions, wrapped as an unmodifiable linked list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableLinkedList<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions) {
		return Concurrent.newUnmodifiableLinkedList(super.sorted(sortOrder, functions));
	}

	/**
	 * Returns a sorted copy of this linked list using the given sort functions, wrapped as an unmodifiable linked list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableLinkedList<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return Concurrent.newUnmodifiableLinkedList(super.sorted(functions));
	}

	/**
	 * Returns a sorted copy of this linked list using the given sort order and functions, wrapped as an unmodifiable linked list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableLinkedList<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return Concurrent.newUnmodifiableLinkedList(super.sorted(sortOrder, functions));
	}

	/**
	 * Returns a sorted copy of this linked list using the given comparator, wrapped as an unmodifiable linked list.
	 */
	@Override
	public @NotNull ConcurrentUnmodifiableLinkedList<E> sorted(Comparator<? super E> comparator) {
		return Concurrent.newUnmodifiableLinkedList(super.sorted(comparator));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void sort(Comparator<? super E> comparator) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns an unmodifiable view of a portion of this linked list between the specified indices.
	 */
	@Override
	public final @NotNull ConcurrentLinkedList<E> subList(int fromIndex, int toIndex) {
		return new ConcurrentUnmodifiableLinkedList<>(super.subList(fromIndex, toIndex));
	}

}
