package dev.simplified.collection.sort;

import dev.simplified.collection.ConcurrentList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Pluggable sorting strategy applied by {@link ConcurrentList#sorted(SortAlgorithm)}.
 * <p>
 * Pre-built strategies live as static factories on {@link Comparison} (general-purpose
 * comparison sorts) and {@link RadixSort} (key-extracted integer-bucketed sorts). Custom
 * strategies may be supplied as a lambda - the receiver is a mutable {@link List} that the
 * implementation must sort in place.
 *
 * @param <E> the element type the algorithm sorts
 */
@FunctionalInterface
public interface SortAlgorithm<E> {

	/**
	 * Sorts {@code list} in place according to the algorithm's strategy.
	 *
	 * @param list the mutable list to sort
	 */
	void sort(@NotNull List<E> list);

}
