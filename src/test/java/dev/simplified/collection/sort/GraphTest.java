package dev.simplified.collection.sort;

import dev.simplified.collection.ConcurrentList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraphTest {

	@Nested
	class TopologicalSort {

		@Test
		void simpleChain() {
			Graph<Integer> graph = Graph.builder(0)
				.withValues(1, 2, 3)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.build();

			ConcurrentList<Integer> sorted = graph.topologicalSort();
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

			Graph.Builder<Integer> b = Graph.builder(0).withValues(values);
			for (int i = 0; i < n - 1; i++) b.withEdge(i, i + 1);

			ConcurrentList<Integer> sorted = b.build().topologicalSort();
			assertEquals(n, sorted.size());
			assertEquals(n - 1, sorted.get(0));
			assertEquals(0, sorted.get(n - 1));
		}

		@Test
		void cycleDetected() {
			Graph<Integer> graph = Graph.builder(0)
				.withValues(1, 2, 3)
				.withEdge(1, 2)
				.withEdge(2, 3)
				.withEdge(3, 1)
				.build();

			assertThrows(IllegalStateException.class, graph::topologicalSort);
		}

	}

}
