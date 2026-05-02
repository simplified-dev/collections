package dev.simplified.collection.sort;

import dev.simplified.collection.ConcurrentArrayList;
import dev.simplified.collection.ConcurrentList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortAlgorithmTest {

	@Nested
	class ComparisonAlgorithms {

		@Test
		void timsortMatchesJdkBaseline() {
			assertMatchesJdk(Comparison.timsort(Comparator.<Integer>naturalOrder()));
		}

		@Test
		void heapMatchesJdkBaseline() {
			assertMatchesJdk(Comparison.heap(Comparator.<Integer>naturalOrder()));
		}

		@Test
		void insertionMatchesJdkBaseline() {
			assertMatchesJdk(Comparison.insertion(Comparator.<Integer>naturalOrder()));
		}

		@Test
		void shellMatchesJdkBaseline() {
			assertMatchesJdk(Comparison.shell(Comparator.<Integer>naturalOrder()));
		}

		@Test
		void quicksortMatchesJdkBaseline() {
			assertMatchesJdk(Comparison.quicksort(Comparator.<Integer>naturalOrder()));
		}

		@Test
		void mergesortMatchesJdkBaseline() {
			assertMatchesJdk(Comparison.mergesort(Comparator.<Integer>naturalOrder()));
		}

		@Test
		void allAlgorithmsHandleEdgeShapes() {
			List<SortAlgorithm<Integer>> algorithms = List.of(
				Comparison.timsort(Comparator.naturalOrder()),
				Comparison.heap(Comparator.naturalOrder()),
				Comparison.insertion(Comparator.naturalOrder()),
				Comparison.shell(Comparator.naturalOrder()),
				Comparison.quicksort(Comparator.naturalOrder()),
				Comparison.mergesort(Comparator.naturalOrder())
			);
			for (SortAlgorithm<Integer> algorithm : algorithms) {
				assertSortsCorrectly(algorithm, new ArrayList<>(), List.of());
				assertSortsCorrectly(algorithm, new ArrayList<>(List.of(7)), List.of(7));
				assertSortsCorrectly(algorithm, new ArrayList<>(List.of(5, 4, 3, 2, 1)), List.of(1, 2, 3, 4, 5));
				assertSortsCorrectly(algorithm, new ArrayList<>(Collections.nCopies(50, 9)), Collections.nCopies(50, 9));
			}
		}

		@Test
		void quicksortHandlesAlreadySortedWithoutN2Blowup() {
			// median-of-three pivot avoids the naive quicksort O(n^2) on sorted input
			List<Integer> alreadySorted = new ArrayList<>();
			for (int i = 0; i < 5_000; i++) alreadySorted.add(i);
			List<Integer> expected = new ArrayList<>(alreadySorted);

			Comparison.quicksort(Comparator.<Integer>naturalOrder()).sort(alreadySorted);
			assertEquals(expected, alreadySorted);
		}

		private void assertMatchesJdk(SortAlgorithm<Integer> algorithm) {
			List<Integer> data = randomInts(500, 0xDEADBEEF);
			List<Integer> expected = new ArrayList<>(data);
			expected.sort(Comparator.naturalOrder());

			algorithm.sort(data);
			assertEquals(expected, data);
		}

		private <E> void assertSortsCorrectly(SortAlgorithm<E> algorithm, List<E> input, List<E> expected) {
			algorithm.sort(input);
			assertEquals(expected, input);
		}
	}

	@Nested
	class RadixByInt {

		private final ToIntFunction<Integer> identity = Integer::intValue;

		@Test
		void emptyListIsNoOp() {
			List<Integer> list = new ArrayList<>();
			RadixSort.byInt(identity).sort(list);
			assertEquals(List.of(), list);
		}

		@Test
		void singleElementUnchanged() {
			List<Integer> list = new ArrayList<>(List.of(42));
			RadixSort.byInt(identity).sort(list);
			assertEquals(List.of(42), list);
		}

		@Test
		void positiveIntsMatchJdkSort() {
			assertMatchesBaseline(randomInts(1_000, 0xBEEF, 1, Integer.MAX_VALUE), identity);
		}

		@Test
		void negativeIntsMatchJdkSort() {
			assertMatchesBaseline(randomInts(1_000, 0xBEEF, Integer.MIN_VALUE, -1), identity);
		}

		@Test
		void mixedSignedMatchesJdkSort() {
			assertMatchesBaseline(randomInts(1_000, 0xBEEF, Integer.MIN_VALUE, Integer.MAX_VALUE), identity);
		}

		@Test
		void edgeValues() {
			assertMatchesBaseline(new ArrayList<>(List.of(
				Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1, 1,
				Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1
			)), identity);
		}

		@Test
		void allDuplicateKeys() {
			List<Integer> list = new ArrayList<>(Collections.nCopies(100, 7));
			RadixSort.byInt(identity).sort(list);
			assertEquals(Collections.nCopies(100, 7), list);
		}

		@Test
		void stabilityPreservesInsertionOrderForEqualKeys() {
			List<Tagged> list = new ArrayList<>();
			for (int i = 0; i < 50; i++) list.add(new Tagged(/* sortKey */ 1, /* id */ i));
			for (int i = 0; i < 50; i++) list.add(new Tagged(/* sortKey */ 0, /* id */ i + 100));

			RadixSort.byInt((ToIntFunction<Tagged>) t -> t.sortKey).sort(list);

			for (int i = 0; i < 50; i++) {
				assertEquals(0, list.get(i).sortKey);
				assertEquals(100 + i, list.get(i).id, "stability violated at index " + i);
			}
			for (int i = 0; i < 50; i++) {
				assertEquals(1, list.get(50 + i).sortKey);
				assertEquals(i, list.get(50 + i).id, "stability violated at index " + (50 + i));
			}
		}

		@Test
		void largeRandomMatchesJdkSort() {
			assertMatchesBaseline(randomInts(100_000, 0xC0FFEE, Integer.MIN_VALUE, Integer.MAX_VALUE), identity);
		}

		private <E> void assertMatchesBaseline(List<E> list, ToIntFunction<E> keyFn) {
			List<E> expected = new ArrayList<>(list);
			expected.sort(Comparator.comparingInt(keyFn));

			RadixSort.byInt(keyFn).sort(list);
			assertEquals(expected, list);
		}
	}

	@Nested
	class RadixByLong {

		private final ToLongFunction<Long> identity = Long::longValue;

		@Test
		void emptyAndSingleAreNoOps() {
			RadixSort.byLong(identity).sort(new ArrayList<>());
			List<Long> single = new ArrayList<>(List.of(42L));
			RadixSort.byLong(identity).sort(single);
			assertEquals(List.of(42L), single);
		}

		@Test
		void mixedSignedMatchesJdkSort() {
			List<Long> data = randomLongs(1_000, 0xBEEF);
			List<Long> expected = new ArrayList<>(data);
			expected.sort(Comparator.naturalOrder());

			RadixSort.byLong(identity).sort(data);
			assertEquals(expected, data);
		}

		@Test
		void edgeValues() {
			List<Long> list = new ArrayList<>(List.of(
				Long.MIN_VALUE, Long.MAX_VALUE, 0L, -1L, 1L,
				Long.MIN_VALUE + 1L, Long.MAX_VALUE - 1L
			));
			List<Long> expected = new ArrayList<>(list);
			expected.sort(Comparator.naturalOrder());

			RadixSort.byLong(identity).sort(list);
			assertEquals(expected, list);
		}

		@Test
		void largeRandomMatchesJdkSort() {
			List<Long> data = randomLongs(50_000, 0xC0FFEE);
			List<Long> expected = new ArrayList<>(data);
			expected.sort(Comparator.naturalOrder());

			RadixSort.byLong(identity).sort(data);
			assertEquals(expected, data);
		}
	}

	@Nested
	class CountingSortByInt {

		@Test
		void emptyAndSingleAreNoOps() {
			CountingSort.byInt(Integer::intValue, 0, 100).sort(new ArrayList<>());
			List<Integer> single = new ArrayList<>(List.of(50));
			CountingSort.byInt(Integer::intValue, 0, 100).sort(single);
			assertEquals(List.of(50), single);
		}

		@Test
		void boundedRangeMatchesJdkSort() {
			List<Integer> data = randomInts(10_000, 0xBEEF, 0, 99);
			List<Integer> expected = new ArrayList<>(data);
			expected.sort(Comparator.naturalOrder());

			CountingSort.byInt(Integer::intValue, 0, 99).sort(data);
			assertEquals(expected, data);
		}

		@Test
		void negativeRangeBoundsHandled() {
			List<Integer> data = new ArrayList<>(List.of(-5, 5, -10, 10, 0, -10, 5));
			List<Integer> expected = new ArrayList<>(data);
			expected.sort(Comparator.naturalOrder());

			CountingSort.byInt(Integer::intValue, -10, 10).sort(data);
			assertEquals(expected, data);
		}

		@Test
		void stabilityPreservesInsertionOrderForEqualKeys() {
			List<Tagged> list = new ArrayList<>();
			for (int i = 0; i < 25; i++) list.add(new Tagged(2, i));
			for (int i = 0; i < 25; i++) list.add(new Tagged(0, i + 100));
			for (int i = 0; i < 25; i++) list.add(new Tagged(1, i + 200));

			CountingSort.byInt((ToIntFunction<Tagged>) t -> t.sortKey, 0, 2).sort(list);

			for (int i = 0; i < 25; i++) assertEquals(100 + i, list.get(i).id);
			for (int i = 0; i < 25; i++) assertEquals(200 + i, list.get(25 + i).id);
			for (int i = 0; i < 25; i++) assertEquals(i, list.get(50 + i).id);
		}

		@Test
		void invalidArgumentsRejected() {
			assertThrows(IllegalArgumentException.class,
				() -> CountingSort.byInt(Integer::intValue, 5, 4));
		}

		@Test
		void outOfRangeKeyRejectedAtSortTime() {
			List<Integer> data = new ArrayList<>(List.of(5, 200, 10));
			SortAlgorithm<Integer> algorithm = CountingSort.byInt(Integer::intValue, 0, 100);
			assertThrows(IllegalArgumentException.class, () -> algorithm.sort(data));
		}
	}

	@Nested
	class AtomicListIntegration {

		@Test
		void sortedSortAlgorithmReturnsNewSortedListLeavingOriginalUnchanged() {
			ConcurrentArrayList<Integer> original = new ConcurrentArrayList<>(List.of(3, 1, 4, 1, 5, 9, 2, 6));
			ConcurrentList<Integer> sorted = original.sorted(RadixSort.byInt(Integer::intValue));

			assertEquals(List.of(3, 1, 4, 1, 5, 9, 2, 6), Arrays.asList(original.toArray()));
			assertEquals(List.of(1, 1, 2, 3, 4, 5, 6, 9), Arrays.asList(sorted.toArray()));
			assertNotSame(original, sorted);
		}

		@Test
		void sortedComparatorRedirectsThroughSortAlgorithmAndStillProducesSortedOutput() {
			ConcurrentArrayList<Integer> original = new ConcurrentArrayList<>(List.of(3, 1, 4, 1, 5, 9, 2, 6));
			ConcurrentList<Integer> sorted = original.sorted(Comparator.<Integer>naturalOrder());
			assertEquals(List.of(1, 1, 2, 3, 4, 5, 6, 9), Arrays.asList(sorted.toArray()));
		}

		@Test
		void lambdaSortAlgorithmIsAccepted() {
			ConcurrentArrayList<Integer> original = new ConcurrentArrayList<>(List.of(5, 2, 8, 1, 9));
			ConcurrentList<Integer> sorted = original.sorted(list -> list.sort(Comparator.<Integer>reverseOrder()));
			assertEquals(List.of(9, 8, 5, 2, 1), Arrays.asList(sorted.toArray()));
			assertNotSame(original, sorted);
		}

		@Test
		void countingSortIntegration() {
			ConcurrentArrayList<Integer> original = new ConcurrentArrayList<>(List.of(2, 0, 1, 2, 1, 0, 2));
			ConcurrentList<Integer> sorted = original.sorted(CountingSort.byInt(Integer::intValue, 0, 2));
			assertEquals(List.of(0, 0, 1, 1, 2, 2, 2), Arrays.asList(sorted.toArray()));
		}
	}

	private record Tagged(int sortKey, int id) {}

	private static List<Integer> randomInts(int n, long seed) {
		return randomInts(n, seed, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	private static List<Integer> randomInts(int n, long seed, int minInclusive, int maxInclusive) {
		Random rng = new Random(seed);
		List<Integer> result = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			result.add(rng.nextInt(minInclusive, maxInclusive == Integer.MAX_VALUE ? Integer.MAX_VALUE : maxInclusive + 1));
		}
		return result;
	}

	private static List<Long> randomLongs(int n, long seed) {
		Random rng = new Random(seed);
		List<Long> result = new ArrayList<>(n);
		for (int i = 0; i < n; i++) result.add(rng.nextLong());
		return result;
	}
}
