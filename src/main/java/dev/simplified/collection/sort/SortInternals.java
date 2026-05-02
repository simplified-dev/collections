package dev.simplified.collection.sort;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ListIterator;

/**
 * Package-private utilities shared by {@link Comparison}, {@link RadixSort}, and
 * {@link CountingSort} algorithm kernels.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SortInternals {

	/**
	 * Writes the sorted contents of {@code source} back into {@code list} via {@link ListIterator}
	 * so the operation is {@code O(n)} regardless of the backing list type ({@code ArrayList}'s
	 * indexed {@code set} is O(1) but {@code LinkedList}'s is O(i) - using the iterator avoids
	 * the O(n^2) trap on linked-list backings).
	 *
	 * @param list the destination list (must already have {@code source.length} elements)
	 * @param source the sorted array to copy
	 * @param <E> the element type
	 */
	static <E> void writeBack(@NotNull List<E> list, E @NotNull [] source) {
		ListIterator<E> it = list.listIterator();
		for (E e : source) {
			it.next();
			it.set(e);
		}
	}

}
