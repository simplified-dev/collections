package dev.simplified.collection.sort;

import dev.simplified.collection.ConcurrentList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {

	@Nested
	class LinearTopologicalSort {

		@Test
		void simpleChain() {
			Graph<Integer> graph = Graph.<Integer>builder()
				.withValues(1, 2, 3)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.build();

			ConcurrentList<Integer> sorted = graph.linearTopologicalSort();
			assertEquals(3, sorted.size());
			assertEquals(3, sorted.get(0));
			assertEquals(2, sorted.get(1));
			assertEquals(1, sorted.get(2));
		}

		@Test
		void deepChainDoesNotBlowStack() {
			int n = 20_000;
			Integer[] values = new Integer[n];
			for (int i = 0; i < n; i++) values[i] = i;

			Graph.Builder<Integer> b = Graph.<Integer>builder().withValues(values);
			for (int i = 0; i < n - 1; i++) b.withEdge(i, i + 1);

			ConcurrentList<Integer> sorted = b.build().linearTopologicalSort();
			assertEquals(n, sorted.size());
			assertEquals(n - 1, sorted.get(0));
			assertEquals(0, sorted.get(n - 1));
		}

		@Test
		void cycleDetected() {
			Graph<Integer> graph = Graph.<Integer>builder()
				.withValues(1, 2, 3)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.withEdge(3, 1)
				.build();

			assertThrows(IllegalStateException.class, graph::linearTopologicalSort);
		}

	}

	@Nested
	class LayeredTopologicalSort {

		@Test
		void simpleChainOneNodePerLayer() {
			Graph<Integer> graph = Graph.<Integer>builder()
				.withValues(1, 2, 3)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.build();

			ConcurrentList<ConcurrentList<Integer>> layers = graph.layeredTopologicalSort();
			assertEquals(3, layers.size());
			assertEquals(Set.of(3), Set.copyOf(layers.get(0)));
			assertEquals(Set.of(2), Set.copyOf(layers.get(1)));
			assertEquals(Set.of(1), Set.copyOf(layers.get(2)));
		}

		@Test
		void diamondCollapsesMiddleIntoSingleLayer() {
			// A -> B, A -> C, B -> D, C -> D    (A depends on B,C; B,C depend on D)
			Graph<String> graph = Graph.<String>builder()
				.withValues("A", "B", "C", "D")
				.withEdge("A", "B")
				.withEdge("A", "C")
				.withEdge("B", "D")
				.withEdge("C", "D")
				.build();

			ConcurrentList<ConcurrentList<String>> layers = graph.layeredTopologicalSort();
			assertEquals(3, layers.size());
			assertEquals(Set.of("D"), Set.copyOf(layers.get(0)));
			assertEquals(Set.of("B", "C"), Set.copyOf(layers.get(1)));
			assertEquals(Set.of("A"), Set.copyOf(layers.get(2)));
		}

		@Test
		void independentNodesShareSingleLayer() {
			Graph<Integer> graph = Graph.<Integer>builder()
				.withValues(1, 2, 3, 4)
				.build();

			ConcurrentList<ConcurrentList<Integer>> layers = graph.layeredTopologicalSort();
			assertEquals(1, layers.size());
			assertEquals(Set.of(1, 2, 3, 4), Set.copyOf(layers.get(0)));
		}

		@Test
		void cycleDetected() {
			Graph<Integer> graph = Graph.<Integer>builder()
				.withValues(1, 2, 3)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.withEdge(3, 1)
				.build();

			assertThrows(IllegalStateException.class, graph::layeredTopologicalSort);
		}

		@Test
		void flattenedMatchesLinearSortSize() {
			Graph<String> graph = Graph.<String>builder()
				.withValues("A", "B", "C", "D")
				.withEdge("A", "B")
				.withEdge("A", "C")
				.withEdge("B", "D")
				.withEdge("C", "D")
				.build();

			int flatSize = graph.layeredTopologicalSort().stream().mapToInt(ConcurrentList::size).sum();
			assertEquals(graph.linearTopologicalSort().size(), flatSize);
		}

	}

	@Nested
	class StructuralQueries {

		private Graph<String> sample() {
			// A -> B, A -> C, B -> D
			return Graph.<String>builder()
				.withValues("A", "B", "C", "D")
				.withEdge("A", "B")
				.withEdge("A", "C")
				.withEdge("B", "D")
				.build();
		}

		@Test
		void containsAndHasEdge() {
			Graph<String> g = this.sample();
			assertTrue(g.contains("A"));
			assertFalse(g.contains("Z"));
			assertTrue(g.hasEdge("A", "B"));
			assertFalse(g.hasEdge("B", "A"));
			assertFalse(g.hasEdge("A", "Z"));
		}

		@Test
		void successorsAndPredecessors() {
			Graph<String> g = this.sample();
			assertEquals(Set.of("B", "C"), Set.copyOf(g.successors("A")));
			assertEquals(Set.of("D"), Set.copyOf(g.successors("B")));
			assertTrue(g.successors("D").isEmpty());

			assertEquals(Set.of("A"), Set.copyOf(g.predecessors("B")));
			assertEquals(Set.of("B"), Set.copyOf(g.predecessors("D")));
			assertTrue(g.predecessors("A").isEmpty());
		}

		@Test
		void degrees() {
			Graph<String> g = this.sample();
			assertEquals(2, g.outDegree("A"));
			assertEquals(1, g.outDegree("B"));
			assertEquals(0, g.outDegree("D"));
			assertEquals(0, g.inDegree("A"));
			assertEquals(1, g.inDegree("D"));
			assertEquals(1, g.inDegree("B"));
			assertEquals(1, g.inDegree("C"));
		}

		@Test
		void rootsAndLeaves() {
			Graph<String> g = this.sample();
			assertEquals(Set.of("A"), Set.copyOf(g.roots()));
			assertEquals(Set.of("C", "D"), Set.copyOf(g.leaves()));
		}

		@Test
		void reverseFlipsEdges() {
			Graph<String> g = this.sample();
			Graph<String> r = g.reverse();
			assertEquals(Set.copyOf(g.getNodes()), Set.copyOf(r.getNodes()));
			assertTrue(r.hasEdge("B", "A"));
			assertTrue(r.hasEdge("C", "A"));
			assertTrue(r.hasEdge("D", "B"));
			assertFalse(r.hasEdge("A", "B"));
		}

	}

	@Nested
	class AutoRegistration {

		@Test
		void edgeOnlyNodesAppearInTopologicalSort() {
			Graph<String> graph = Graph.<String>builder()
				.withEdge("A", "B")
				.withEdge("B", "C")
				.build();

			ConcurrentList<String> sorted = graph.linearTopologicalSort();
			assertEquals(Set.of("A", "B", "C"), new HashSet<>(sorted));
			assertTrue(graph.contains("A"));
			assertTrue(graph.contains("B"));
			assertTrue(graph.contains("C"));
		}

		@Test
		void edgeOnlyNodesAppearInLayeredSort() {
			Graph<String> graph = Graph.<String>builder()
				.withEdge("A", "B")
				.build();

			ConcurrentList<ConcurrentList<String>> layers = graph.layeredTopologicalSort();
			assertEquals(2, layers.size());
			assertEquals(Set.of("B"), Set.copyOf(layers.get(0)));
			assertEquals(Set.of("A"), Set.copyOf(layers.get(1)));
		}

	}

	@Nested
	class AsLinearSort {

		@Test
		void ordersArbitrarySubsetByGraphPosition() {
			// chain: 1 -> 2 -> 3, so linear order is [3, 2, 1]
			Graph<Integer> graph = Graph.<Integer>builder()
				.withValues(1, 2, 3, 4, 5)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.withEdge(4, 5)
				.build();

			SortAlgorithm<Integer> algorithm = graph.asLinearSort();

			List<Integer> subset = new ArrayList<>(List.of(1, 3, 5));
			algorithm.sort(subset);

			// linear order across all nodes is [3, 5, 2, 4, 1] (one valid order; subset preserves relative)
			// the subset {1, 3, 5} should reorder so 3 and 5 (leaves) come before 1 (root)
			List<Integer> fullOrder = new ArrayList<>(graph.linearTopologicalSort());
			subset.sort((a, b) -> Integer.compare(fullOrder.indexOf(a), fullOrder.indexOf(b)));
			List<Integer> expected = subset;

			List<Integer> actual = new ArrayList<>(List.of(1, 3, 5));
			algorithm.sort(actual);
			assertEquals(expected, actual);
		}

		@Test
		void reusableAcrossMultipleSorts() {
			Graph<Integer> graph = Graph.<Integer>builder()
				.withValues(1, 2, 3)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.build();

			SortAlgorithm<Integer> algorithm = graph.asLinearSort();

			List<Integer> first = new ArrayList<>(List.of(1, 2, 3));
			algorithm.sort(first);
			assertEquals(List.of(3, 2, 1), first);

			List<Integer> second = new ArrayList<>(List.of(2, 1));
			algorithm.sort(second);
			assertEquals(List.of(2, 1), second);
		}

		@Test
		void elementNotInGraphThrows() {
			Graph<String> graph = Graph.<String>builder().withValues("A", "B").withEdge("A", "B").build();
			SortAlgorithm<String> algorithm = graph.asLinearSort();

			List<String> bad = new ArrayList<>(List.of("A", "ZZZ"));
			assertThrows(NoSuchElementException.class, () -> algorithm.sort(bad));
		}

		@Test
		void cycleInGraphSurfacesAsIllegalState() {
			Graph<Integer> graph = Graph.<Integer>builder()
				.withValues(1, 2, 3)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.withEdge(3, 1)
				.build();
			assertThrows(IllegalStateException.class, graph::asLinearSort);
		}
	}

	@Nested
	class AsLayeredSort {

		@Test
		void ordersByLayerIndexStableWithinLayer() {
			// A -> B, A -> C, B -> D, C -> D
			// layers: 0 = {D}, 1 = {B, C}, 2 = {A}
			Graph<String> graph = Graph.<String>builder()
				.withValues("A", "B", "C", "D")
				.withEdge("A", "B")
				.withEdge("A", "C")
				.withEdge("B", "D")
				.withEdge("C", "D")
				.build();

			SortAlgorithm<String> algorithm = graph.asLayeredSort();

			// Input order: A, C, B, D — within layer 1 {B,C} the input order C-then-B should be preserved
			List<String> input = new ArrayList<>(List.of("A", "C", "B", "D"));
			algorithm.sort(input);

			// Expected: D (layer 0), then C, B (layer 1, input order), then A (layer 2)
			assertEquals(List.of("D", "C", "B", "A"), input);
		}

		@Test
		void reusableAcrossMultipleSorts() {
			Graph<String> graph = Graph.<String>builder()
				.withValues("A", "B", "C")
				.withEdge("A", "B")
				.withEdge("A", "C")
				.build();

			SortAlgorithm<String> algorithm = graph.asLayeredSort();

			List<String> first = new ArrayList<>(List.of("A", "B", "C"));
			algorithm.sort(first);
			// layers: 0 = {B, C}, 1 = {A}
			assertEquals(List.of("B", "C", "A"), first);

			List<String> second = new ArrayList<>(List.of("C", "A", "B"));
			algorithm.sort(second);
			assertEquals(List.of("C", "B", "A"), second);
		}

		@Test
		void elementNotInGraphThrows() {
			Graph<String> graph = Graph.<String>builder().withValues("A", "B").withEdge("A", "B").build();
			SortAlgorithm<String> algorithm = graph.asLayeredSort();

			List<String> bad = new ArrayList<>(List.of("A", "ZZZ"));
			assertThrows(NoSuchElementException.class, () -> algorithm.sort(bad));
		}

		@Test
		void cycleInGraphSurfacesAsIllegalState() {
			Graph<Integer> graph = Graph.<Integer>builder()
				.withValues(1, 2, 3)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.withEdge(3, 1)
				.build();
			assertThrows(IllegalStateException.class, graph::asLayeredSort);
		}
	}

}
