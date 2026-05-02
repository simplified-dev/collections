package dev.simplified.collection.sort;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * Static factories for general-purpose comparison-based {@link SortAlgorithm} strategies.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Comparison {

	private static final int QUICKSORT_INSERTION_THRESHOLD = 16;

	/**
	 * Sorts via the JDK's adaptive Timsort - a hybrid mergesort/insertion-sort that exploits
	 * pre-existing order in the input.
	 * <p>
	 * Reach for this when the data shape is unknown or partially sorted, or when sort stability
	 * matters and elements have meaningful equals semantics. Wins on real-world data; loses to
	 * specialized algorithms only on extreme shapes (huge integer arrays, bounded enum-like
	 * ranges).
	 *
	 * <p><b>Time:</b> {@code O(n)} on already-sorted or reverse-sorted input, {@code O(n log n)}
	 * average and worst case.
	 * <p><b>Space:</b> {@code O(n)} auxiliary buffer for the merge passes.
	 *
	 * @param comparator the comparator defining element order
	 * @param <E> the element type
	 * @return a {@link SortAlgorithm} that delegates to {@link List#sort(Comparator)}
	 */
	public static <E> @NotNull SortAlgorithm<E> timsort(@NotNull Comparator<? super E> comparator) {
		return list -> list.sort(comparator);
	}

	/**
	 * Sorts via heapsort - builds a max-heap then repeatedly extracts the maximum into the back
	 * of the array.
	 * <p>
	 * Reach for this when memory is constrained and a hard {@code O(n log n)} worst-case
	 * guarantee is required. Doesn't allocate auxiliary buffers like Timsort/mergesort, doesn't
	 * degrade on adversarial input like quicksort. Slower constant factor than Timsort on
	 * typical data, and unstable - equal elements may be reordered.
	 *
	 * <p><b>Time:</b> {@code O(n log n)} always - data shape doesn't matter.
	 * <p><b>Space:</b> {@code O(1)} auxiliary - sorts in place on the array snapshot
	 * (a fixed {@code O(n)} array is allocated once for the snapshot itself).
	 *
	 * @param comparator the comparator defining element order
	 * @param <E> the element type
	 * @return a {@link SortAlgorithm} performing in-place heapsort on the snapshot
	 */
	public static <E> @NotNull SortAlgorithm<E> heap(@NotNull Comparator<? super E> comparator) {
		return arrayBased(comparator, Comparison::heapSort);
	}

	/**
	 * Sorts via insertion sort - walks left-to-right, sliding each new element into its sorted
	 * position among the elements already processed.
	 * <p>
	 * Reach for this only on tiny inputs ({@code n < ~32}) or near-sorted input where each
	 * element has at most a handful of predecessors out of place. Beats every other algorithm
	 * including Timsort on small {@code n} thanks to its trivial overhead and sequential access
	 * pattern. Catastrophic ({@code O(n^2)}) on large random data - never use it for {@code n}
	 * in the thousands.
	 *
	 * <p><b>Time:</b> {@code O(n)} on already-sorted input, {@code O(n^2)} average and worst
	 * case.
	 * <p><b>Space:</b> {@code O(1)} auxiliary - in-place on the snapshot array.
	 *
	 * @param comparator the comparator defining element order
	 * @param <E> the element type
	 * @return a {@link SortAlgorithm} performing in-place insertion sort on the snapshot
	 */
	public static <E> @NotNull SortAlgorithm<E> insertion(@NotNull Comparator<? super E> comparator) {
		return arrayBased(comparator, Comparison::insertionSort);
	}

	/**
	 * Sorts via Shell sort with Knuth's gap sequence ({@code 1, 4, 13, 40, 121, ...}) - a
	 * generalization of insertion sort that pre-conditions the array with progressively smaller
	 * gap-spaced insertion sweeps.
	 * <p>
	 * Reach for this on mid-sized arrays ({@code n} ~ thousands to ~50k) where memory is
	 * constrained and you can't afford Timsort's auxiliary buffer, but {@code O(n^2)} insertion
	 * sort would be too slow. Performs well on partially-sorted input. Unstable.
	 *
	 * <p><b>Time:</b> Empirically {@code O(n^1.3)} with Knuth's gap sequence; theoretical
	 * upper bound {@code O(n^(3/2))} known, tighter bounds are open.
	 * <p><b>Space:</b> {@code O(1)} auxiliary - in-place on the snapshot array.
	 *
	 * @param comparator the comparator defining element order
	 * @param <E> the element type
	 * @return a {@link SortAlgorithm} performing in-place Shell sort on the snapshot
	 */
	public static <E> @NotNull SortAlgorithm<E> shell(@NotNull Comparator<? super E> comparator) {
		return arrayBased(comparator, Comparison::shellSort);
	}

	/**
	 * Sorts via quicksort with median-of-three pivot selection and an insertion-sort fallback
	 * for sub-arrays smaller than 16 elements.
	 * <p>
	 * Reach for this on uniformly random data where Timsort's run-detection overhead provides
	 * no benefit and you don't need stability. Median-of-three pivot avoids the {@code O(n^2)}
	 * worst case on already-sorted input that naive quicksort suffers. Loses to Timsort on
	 * partially-sorted real-world data; unstable - equal elements may be reordered.
	 *
	 * <p><b>Time:</b> {@code O(n log n)} average, {@code O(n^2)} worst case (rare with
	 * median-of-three pivot, e.g. on adversarial input).
	 * <p><b>Space:</b> {@code O(log n)} stack frames from tail-recursion-elimination on the
	 * larger partition; in-place otherwise.
	 *
	 * @param comparator the comparator defining element order
	 * @param <E> the element type
	 * @return a {@link SortAlgorithm} performing in-place introspective quicksort on the snapshot
	 */
	public static <E> @NotNull SortAlgorithm<E> quicksort(@NotNull Comparator<? super E> comparator) {
		return arrayBased(comparator, Comparison::quickSort);
	}

	/**
	 * Sorts via top-down mergesort with a single auxiliary buffer reused across all merges.
	 * <p>
	 * Reach for this when you need a strict {@code O(n log n)} worst-case guarantee and
	 * stability, and you can afford the {@code O(n)} auxiliary buffer. Predictable performance
	 * regardless of input distribution; loses to Timsort on partially-sorted data because
	 * Timsort exploits existing runs.
	 *
	 * <p><b>Time:</b> {@code O(n log n)} always - data shape doesn't matter.
	 * <p><b>Space:</b> {@code O(n)} auxiliary buffer plus {@code O(log n)} recursion stack.
	 *
	 * @param comparator the comparator defining element order
	 * @param <E> the element type
	 * @return a {@link SortAlgorithm} performing top-down mergesort on the snapshot
	 */
	public static <E> @NotNull SortAlgorithm<E> mergesort(@NotNull Comparator<? super E> comparator) {
		return arrayBased(comparator, Comparison::mergeSort);
	}

	/**
	 * Wraps an array-based sort kernel as a {@link SortAlgorithm} that copies the list to an
	 * {@code Object[]}, applies the kernel, then writes back via {@link ListIterator} so the
	 * write-back is {@code O(n)} on both {@code ArrayList} and {@code LinkedList} backings.
	 */
	private static <E> @NotNull SortAlgorithm<E> arrayBased(
		@NotNull Comparator<? super E> comparator, @NotNull ArraySorter<E> sorter
	) {
		return list -> {
			int n = list.size();
			if (n < 2) return;

			@SuppressWarnings("unchecked")
			E[] array = (E[]) list.toArray();
			sorter.sort(array, comparator);
			SortInternals.writeBack(list, array);
		};
	}

	@FunctionalInterface
	private interface ArraySorter<E> {
		void sort(@NotNull E[] array, @NotNull Comparator<? super E> comparator);
	}

	// --- Algorithm kernels ---

	private static <E> void heapSort(@NotNull E[] array, @NotNull Comparator<? super E> comparator) {
		int n = array.length;
		for (int i = n / 2 - 1; i >= 0; i--) siftDown(array, i, n, comparator);
		for (int end = n - 1; end > 0; end--) {
			E tmp = array[0];
			array[0] = array[end];
			array[end] = tmp;
			siftDown(array, 0, end, comparator);
		}
	}

	private static <E> void siftDown(@NotNull E[] array, int root, int end, @NotNull Comparator<? super E> comparator) {
		while (true) {
			int child = root * 2 + 1;
			if (child >= end) return;
			if (child + 1 < end && comparator.compare(array[child], array[child + 1]) < 0) child++;
			if (comparator.compare(array[root], array[child]) >= 0) return;
			E tmp = array[root];
			array[root] = array[child];
			array[child] = tmp;
			root = child;
		}
	}

	private static <E> void insertionSort(@NotNull E[] array, @NotNull Comparator<? super E> comparator) {
		insertionSortRange(array, 0, array.length - 1, comparator);
	}

	private static <E> void insertionSortRange(@NotNull E[] array, int lo, int hi, @NotNull Comparator<? super E> comparator) {
		for (int i = lo + 1; i <= hi; i++) {
			E key = array[i];
			int j = i - 1;
			while (j >= lo && comparator.compare(array[j], key) > 0) {
				array[j + 1] = array[j];
				j--;
			}
			array[j + 1] = key;
		}
	}

	private static <E> void shellSort(@NotNull E[] array, @NotNull Comparator<? super E> comparator) {
		int n = array.length;
		int gap = 1;
		while (gap < n / 3) gap = gap * 3 + 1;
		while (gap > 0) {
			for (int i = gap; i < n; i++) {
				E key = array[i];
				int j = i;
				while (j >= gap && comparator.compare(array[j - gap], key) > 0) {
					array[j] = array[j - gap];
					j -= gap;
				}
				array[j] = key;
			}
			gap /= 3;
		}
	}

	private static <E> void quickSort(@NotNull E[] array, @NotNull Comparator<? super E> comparator) {
		quickSortRange(array, 0, array.length - 1, comparator);
	}

	private static <E> void quickSortRange(@NotNull E[] array, int lo, int hi, @NotNull Comparator<? super E> comparator) {
		while (lo < hi) {
			if (hi - lo < QUICKSORT_INSERTION_THRESHOLD) {
				insertionSortRange(array, lo, hi, comparator);
				return;
			}
			int mid = lo + (hi - lo) / 2;
			if (comparator.compare(array[mid], array[lo]) < 0) swap(array, lo, mid);
			if (comparator.compare(array[hi], array[lo]) < 0) swap(array, lo, hi);
			if (comparator.compare(array[hi], array[mid]) < 0) swap(array, mid, hi);
			E pivot = array[mid];
			int i = lo, j = hi;
			while (i <= j) {
				while (comparator.compare(array[i], pivot) < 0) i++;
				while (comparator.compare(array[j], pivot) > 0) j--;
				if (i <= j) {
					swap(array, i, j);
					i++;
					j--;
				}
			}
			// Recurse on smaller side, iterate on larger to bound stack depth to O(log n)
			if (j - lo < hi - i) {
				quickSortRange(array, lo, j, comparator);
				lo = i;
			} else {
				quickSortRange(array, i, hi, comparator);
				hi = j;
			}
		}
	}

	private static <E> void swap(@NotNull E[] array, int a, int b) {
		E tmp = array[a];
		array[a] = array[b];
		array[b] = tmp;
	}

	private static <E> void mergeSort(@NotNull E[] array, @NotNull Comparator<? super E> comparator) {
		@SuppressWarnings("unchecked")
		E[] aux = (E[]) new Object[array.length];
		mergeSortRange(array, aux, 0, array.length - 1, comparator);
	}

	private static <E> void mergeSortRange(@NotNull E[] array, @NotNull E[] aux, int lo, int hi, @NotNull Comparator<? super E> comparator) {
		if (lo >= hi) return;
		int mid = lo + (hi - lo) / 2;
		mergeSortRange(array, aux, lo, mid, comparator);
		mergeSortRange(array, aux, mid + 1, hi, comparator);
		System.arraycopy(array, lo, aux, lo, hi - lo + 1);
		int i = lo, j = mid + 1;
		for (int k = lo; k <= hi; k++) {
			if (i > mid) array[k] = aux[j++];
			else if (j > hi) array[k] = aux[i++];
			else if (comparator.compare(aux[j], aux[i]) < 0) array[k] = aux[j++];
			else array[k] = aux[i++];
		}
	}

}
