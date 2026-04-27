package dev.simplified.collection.tree;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.tree.ConcurrentTreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentTreeSetTest {

	@Nested
	class BasicOps {

		@Test
		void naturalOrder_iteratesSorted() {
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet();
			s.add("c");
			s.add("a");
			s.add("b");

			List<String> order = new ArrayList<>(s);
			assertEquals(List.of("a", "b", "c"), order);
		}

		@Test
		void customComparator_ordersAccordingly() {
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet(Comparator.reverseOrder());
			s.add("a");
			s.add("c");
			s.add("b");

			List<String> order = new ArrayList<>(s);
			assertEquals(List.of("c", "b", "a"), order);
		}

		@Test
		void firstElement_inIteration() {
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet();
			s.add("c");
			s.add("a");
			s.add("b");
			assertEquals("a", s.iterator().next());
		}
	}
}
