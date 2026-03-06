package dev.sbs.api.collection.concurrent;

import dev.sbs.api.collection.concurrent.atomic.AtomicList;
import dev.sbs.api.collection.query.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A concurrent list that allows for simultaneous fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the list by replacing the
 * entire list on each modification. This allows for maintaining the original
 * speed of {@link ArrayList#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
@SuppressWarnings("all")
public class ConcurrentList<E> extends AtomicList<E, ArrayList<E>> {

	/**
	 * Create a new concurrent list.
	 */
	public ConcurrentList() {
		super(new ArrayList<>());
	}

	/**
	 * Create a new concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentList(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent list and fill it with the given collection.
	 */
	public ConcurrentList(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new ArrayList<>() : new ArrayList<>(collection));
	}

	@Override
	protected final @NotNull AtomicList<E, ArrayList<E>> createEmpty() {
		return Concurrent.newList();
	}

	/**
	 * Returns a new {@code ConcurrentList} containing all elements from the current list
	 * in reverse order. The reversal is performed on a snapshot of the current list,
	 * ensuring that the original list remains unmodified.
	 *
	 * @return A new {@code ConcurrentList} with the elements of the current list in reverse order.
	 */
	@Override
	public @NotNull ConcurrentList<E> inverse() {
		return (ConcurrentList<E>) super.inverse();
	}

	/**
	 * Returns a view of the portion of this {@code ConcurrentList} between the specified {@code fromIndex}, inclusive,
	 * and {@code toIndex}, exclusive. The returned sublist is a snapshot of the current list and does
	 * not reflect subsequent modifications to the original list.
	 *
	 * @param fromIndex The starting index of the sublist (inclusive).
	 * @param toIndex The ending index of the sublist (exclusive).
	 * @return A new {@code ConcurrentList} representing the specified range within the list.
	 * @throws IndexOutOfBoundsException If either {@code fromIndex} or {@code toIndex}
	 *         is out of range ({@code fromIndex < 0}, {@code toIndex > size()} or
	 *         {@code fromIndex > toIndex}).
	 */
	@Override
	public @NotNull ConcurrentList<E> subList(int fromIndex, int toIndex) {
		return (ConcurrentList<E>) super.subList(fromIndex, toIndex);
	}

	/**
	 * Returns a view of the portion of this {@code ConcurrentList} between the specified
	 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive. The returned
	 * sublist is a snapshot of the current list and does not reflect subsequent modifications
	 * to the original list.
	 *
	 * @param fromIndex The starting index of the sublist (inclusive).
	 * @param toIndex The ending index of the sublist (exclusive).
	 * @return A new {@code ConcurrentList} representing the specified range within the list.
	 * @throws IndexOutOfBoundsException If either {@code fromIndex} or {@code toIndex}
	 *         is out of range ({@code fromIndex < 0}, {@code toIndex > size()} or
	 *         {@code fromIndex > toIndex}).
	 */
	@Override
	public @NotNull ConcurrentList<E> sorted(@NotNull Function<E, ? extends Comparable>... sortFunctions) {
		return (ConcurrentList<E>) super.sorted(sortFunctions);
	}

	/**
	 * Returns a new {@code ConcurrentList} containing all elements from the current list, sorted according to the
	 * specified sort order and comparison functions. The sorting operation is performed on a snapshot of the current list,
	 * leaving the original list unmodified.
	 *
	 * @param sortOrder The sort order ({@code ASCENDING} or {@code DESCENDING}) to apply while sorting the elements.
	 * @param functions A variable number of functions that extract comparable keys for sorting. Elements are sorted
	 *                  by the first function, then by the second function if the keys are equal, and so on.
	 * @return A new {@code ConcurrentList} containing the elements sorted according to the specified sort
	 *         order and comparison functions.
	 */
	@Override
	public @NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions) {
		return (ConcurrentList<E>) super.sorted(sortOrder, functions);
	}

	/**
	 * Returns a new {@code ConcurrentList} containing all elements from the current list, sorted in descending order
	 * according to the specified collection of comparison functions. The sorting is performed on a snapshot of the
	 * current list, leaving the original list unmodified.
	 *
	 * @param functions An iterable collection of functions used to extract comparable keys for sorting.
	 *                  The elements of the list are primarily sorted by the first function. Subsequent functions
	 *                  are used to break ties if the keys produced by the earlier functions are equal.
	 * @return A new {@code ConcurrentList} containing the sorted elements in descending order
	 *         based on the provided comparison functions.
	 */
	@Override
	public @NotNull ConcurrentList<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return (ConcurrentList<E>) super.sorted(functions);
	}

	/**
	 * Returns a new {@code ConcurrentList} containing all elements from the current list, sorted according to the
	 * specified sort order and comparison functions. The sort operation works on a snapshot of the current list,
	 * leaving the original list unmodified.
	 *
	 * @param sortOrder The sort order ({@code ASCENDING} or {@code DESCENDING}) to apply while sorting the elements.
	 * @param functions An iterable collection of functions that extract the comparable keys for sorting.
	 *                  The elements are sorted by the first function, then by the second function if the keys
	 *                  are equal, and so on.
	 * @return A new {@code ConcurrentList} containing the sorted elements.
	 */
	@Override
	public @NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return (ConcurrentList<E>) super.sorted(sortOrder, functions);
	}

	/**
	 * Returns a new {@code ConcurrentList} containing all elements from the current list,
	 * sorted according to the specified {@code Comparator}. The sorting operation is performed
	 * on a snapshot of the current list, leaving the original list unmodified.
	 *
	 * @param comparator The {@code Comparator} used to determine the order of the elements.
	 *                   If the comparator is {@code null}, the elements' natural ordering
	 *                   is used.
	 * @return A new {@code ConcurrentList} containing the sorted elements.
	 */
	@Override
	public @NotNull ConcurrentList<E> sorted(Comparator<? super E> comparator) {
		return (ConcurrentList<E>) super.sorted(comparator);
	}

	public @NotNull ConcurrentList<E> toUnmodifiableList() {
		return Concurrent.newUnmodifiableList(this);
	}

}
