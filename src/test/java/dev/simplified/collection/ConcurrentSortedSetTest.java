package dev.simplified.collection;

import dev.simplified.collection.sorted.ConcurrentSortedSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentSortedSetTest {

	@Nested
	class BasicOps {

		@Test
		void naturalOrder_iteratesSorted() {
			ConcurrentSortedSet<String> s = Concurrent.newSortedSet();
			s.add("c");
			s.add("a");
			s.add("b");

			List<String> order = new ArrayList<>(s);
			assertEquals(List.of("a", "b", "c"), order);
		}

		@Test
		void customComparator_ordersAccordingly() {
			ConcurrentSortedSet<String> s = new ConcurrentSortedSet<>(Comparator.reverseOrder());
			s.add("a");
			s.add("c");
			s.add("b");

			List<String> order = new ArrayList<>(s);
			assertEquals(List.of("c", "b", "a"), order);
		}

		@Test
		void firstElement_inIteration() {
			ConcurrentSortedSet<String> s = Concurrent.newSortedSet();
			s.add("c");
			s.add("a");
			s.add("b");
			assertEquals("a", s.iterator().next());
		}
	}
}
