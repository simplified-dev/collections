package dev.simplified.collection.linked;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.atomic.AtomicList;
import dev.simplified.collection.query.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * A thread-safe list backed by a {@link LinkedList} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Supports indexed access, sorting,
 * and snapshot-based iteration with linked-list insertion characteristics.
 *
 * @param <E> the type of elements in this list
 */
@SuppressWarnings("all")
public class ConcurrentLinkedList<E> extends ConcurrentList<E> {

	/**
	 * Create a new concurrent list.
	 */
	public ConcurrentLinkedList() {
		super(new LinkedList<>());
	}

	/**
	 * Create a new concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentLinkedList(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent list and fill it with the given collection.
	 */
	public ConcurrentLinkedList(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new LinkedList<>() : new LinkedList<>(collection));
	}

	/**
	 * Creates a new empty {@code ConcurrentLinkedList} instance, used internally for copy and sort operations.
	 *
	 * @return a new empty {@link ConcurrentLinkedList}
	 */
	@Override
	protected @NotNull AtomicList<E, List<E>> createEmpty() {
		return Concurrent.newLinkedList();
	}

	/**
	 * Returns a new {@code ConcurrentLinkedList} with elements in reverse order.
	 * The original list is not modified.
	 *
	 * @return a new reversed {@link ConcurrentLinkedList}
	 */
	@Override
	public @NotNull ConcurrentLinkedList<E> reversed() {
		return (ConcurrentLinkedList<E>) super.reversed();
	}

	/**
	 * Returns a new {@code ConcurrentLinkedList} containing all elements sorted in descending order
	 * according to the specified comparison functions. The original list is not modified.
	 *
	 * @param sortFunctions one or more functions used to extract comparable keys for sorting
	 * @return a new sorted {@link ConcurrentLinkedList}
	 */
	@Override
	public @NotNull ConcurrentLinkedList<E> sorted(@NotNull Function<E, ? extends Comparable>... sortFunctions) {
		return (ConcurrentLinkedList<E>) super.sorted(sortFunctions);
	}

	/**
	 * Returns a new {@code ConcurrentLinkedList} containing all elements sorted according to the
	 * specified sort order and comparison functions. The original list is not modified.
	 *
	 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING}) to apply
	 * @param functions one or more functions that extract comparable keys for sorting
	 * @return a new sorted {@link ConcurrentLinkedList}
	 */
	@Override
	public @NotNull ConcurrentLinkedList<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions) {
		return (ConcurrentLinkedList<E>) super.sorted(sortOrder, functions);
	}

	/**
	 * Returns a new {@code ConcurrentLinkedList} containing all elements sorted in descending order
	 * according to the specified collection of comparison functions. The original list is not modified.
	 *
	 * @param functions an iterable collection of functions used to extract comparable keys for sorting
	 * @return a new sorted {@link ConcurrentLinkedList}
	 */
	@Override
	public @NotNull ConcurrentLinkedList<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return (ConcurrentLinkedList<E>) super.sorted(functions);
	}

	/**
	 * Returns a new {@code ConcurrentLinkedList} containing all elements sorted according to the
	 * specified sort order and comparison functions. The original list is not modified.
	 *
	 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING}) to apply
	 * @param functions an iterable collection of functions that extract comparable keys for sorting
	 * @return a new sorted {@link ConcurrentLinkedList}
	 */
	@Override
	public @NotNull ConcurrentLinkedList<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return (ConcurrentLinkedList<E>) super.sorted(sortOrder, functions);
	}

	/**
	 * Returns a new {@code ConcurrentLinkedList} containing all elements sorted according to the
	 * specified {@code Comparator}. The original list is not modified.
	 *
	 * @param comparator the {@code Comparator} used to determine the order of elements
	 * @return a new sorted {@link ConcurrentLinkedList}
	 */
	@Override
	public @NotNull ConcurrentLinkedList<E> sorted(Comparator<? super E> comparator) {
		return (ConcurrentLinkedList<E>) super.sorted(comparator);
	}

}
