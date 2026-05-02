package dev.simplified.collection;

import dev.simplified.collection.query.SortOrder;
import dev.simplified.collection.query.Sortable;
import dev.simplified.collection.sort.SortAlgorithm;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A thread-safe {@link List} extension combining the {@link ConcurrentCollection} surface with
 * indexed access, atomic sorting, and snapshot-based list iteration.
 *
 * @param <E> the type of elements in this list
 */
public interface ConcurrentList<E> extends ConcurrentCollection<E>, Sortable<E>, List<E> {

	/**
	 * Returns an {@link Optional} containing the first element of this list, or an empty
	 * {@code Optional} if the list is empty.
	 *
	 * @return an {@code Optional} describing the first element, or an empty {@code Optional}
	 */
	@NotNull Optional<E> findFirst();

	/**
	 * Returns an {@link Optional} containing the last element of this list, or an empty
	 * {@code Optional} if the list is empty.
	 *
	 * @return an {@code Optional} describing the last element, or an empty {@code Optional}
	 */
	@NotNull Optional<E> findLast();

	/**
	 * Returns the element at the specified index, or the default value if the index is out of
	 * bounds.
	 *
	 * @param index the index of the element to return
	 * @param defaultValue the default value to return if the index is out of bounds
	 * @return the element at the specified index, or {@code defaultValue} if the index is out of
	 *         range
	 */
	E getOrDefault(int index, E defaultValue);

	/**
	 * Returns a new list containing all elements from this list, sorted in descending order
	 * according to the specified comparison functions. The original list is not modified.
	 *
	 * @param functions one or more functions used to extract comparable keys for sorting
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(@NotNull Function<E, ? extends Comparable<?>>... functions);

	/**
	 * Returns a new list containing all elements from this list, sorted in descending order
	 * according to the specified collection of comparison functions. The original list is not
	 * modified.
	 *
	 * @param functions an iterable collection of functions used to extract comparable keys for
	 *                  sorting
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable<?>>> functions);

	/**
	 * Returns a new list containing all elements from this list, sorted according to the
	 * specified sort order and comparison functions. The original list is not modified.
	 *
	 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING})
	 * @param functions one or more functions that extract comparable keys for sorting
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable<?>>... functions);

	/**
	 * Returns a new list containing all elements from this list, sorted according to the
	 * specified sort order and comparison functions. The original list is not modified.
	 *
	 * @param sortOrder the sort order ({@code ASCENDING} or {@code DESCENDING})
	 * @param functions an iterable collection of functions that extract comparable keys for
	 *                  sorting
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable<?>>> functions);

	/**
	 * Returns a new list containing all elements from this list, sorted according to the given
	 * comparator. The original list is not modified.
	 *
	 * @param comparator the comparator used to order the elements; {@code null} requests natural
	 *                   ordering
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(Comparator<? super E> comparator);

	/**
	 * Returns a new list containing all elements from this list, sorted by the given pluggable
	 * {@link SortAlgorithm}. The original list is not modified.
	 * <p>
	 * Use this overload when a workload-specific algorithm beats the default Timsort - typically
	 * {@code RadixSort.byInt(...)} for large integer-keyed collections, {@code Comparison.timsort(cmp)}
	 * to be explicit, or a lambda for a one-off custom strategy.
	 *
	 * @param algorithm the sort strategy to apply
	 * @return a new sorted list
	 */
	@NotNull ConcurrentList<E> sorted(@NotNull SortAlgorithm<E> algorithm);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentList<E> reversed();

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentList<E> subList(int fromIndex, int toIndex);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentList<E> toUnmodifiable();

}
