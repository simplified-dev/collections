package dev.simplified.collection;

import dev.simplified.collection.sorted.ConcurrentSortedMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentSortedMapTest {

	@Nested
	class BasicOps {

		@Test
		void put_and_get() {
			ConcurrentSortedMap<String, Integer> m = Concurrent.newSortedMap();
			m.put("b", 2);
			m.put("a", 1);
			assertEquals(1, m.get("a"));
			assertEquals(2, m.get("b"));
		}

		@Test
		void naturalOrder_iteratesSortedInEntrySet() {
			ConcurrentSortedMap<String, Integer> m = Concurrent.newSortedMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);

			List<String> keys = new ArrayList<>();
			for (Map.Entry<String, Integer> e : m.entrySet()) keys.add(e.getKey());
			assertEquals(List.of("a", "b", "c"), keys);
		}

		@Test
		void customComparator_ordersAccordingly() {
			ConcurrentSortedMap<String, Integer> m = new ConcurrentSortedMap<>(Comparator.reverseOrder());
			m.put("a", 1);
			m.put("c", 3);
			m.put("b", 2);

			List<String> keys = new ArrayList<>(m.keySet());
			assertEquals(List.of("c", "b", "a"), keys);
		}

		@Test
		void firstEntry_inIteration() {
			ConcurrentSortedMap<String, Integer> m = Concurrent.newSortedMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);
			assertEquals("a", m.entrySet().iterator().next().getKey());
		}
	}
}
