package dev.simplified.collection.tree;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.tree.ConcurrentTreeMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentTreeMapTest {

	@Nested
	class BasicOps {

		@Test
		void put_and_get() {
			ConcurrentTreeMap<String, Integer> m = Concurrent.newTreeMap();
			m.put("b", 2);
			m.put("a", 1);
			assertEquals(1, m.get("a"));
			assertEquals(2, m.get("b"));
		}

		@Test
		void naturalOrder_iteratesSortedInEntrySet() {
			ConcurrentTreeMap<String, Integer> m = Concurrent.newTreeMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);

			List<String> keys = new ArrayList<>();
			for (Map.Entry<String, Integer> e : m.entrySet()) keys.add(e.getKey());
			assertEquals(List.of("a", "b", "c"), keys);
		}

		@Test
		void customComparator_ordersAccordingly() {
			ConcurrentTreeMap<String, Integer> m = Concurrent.newTreeMap(Comparator.reverseOrder());
			m.put("a", 1);
			m.put("c", 3);
			m.put("b", 2);

			List<String> keys = new ArrayList<>(m.keySet());
			assertEquals(List.of("c", "b", "a"), keys);
		}

		@Test
		void firstEntry_inIteration() {
			ConcurrentTreeMap<String, Integer> m = Concurrent.newTreeMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);
			assertEquals("a", m.entrySet().iterator().next().getKey());
		}
	}
}
