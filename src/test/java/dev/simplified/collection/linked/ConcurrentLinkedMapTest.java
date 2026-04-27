package dev.simplified.collection.linked;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.linked.ConcurrentLinkedMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentLinkedMapTest {

	@Nested
	class BasicOps {

		@Test
		void put_and_get() {
			ConcurrentLinkedMap<String, Integer> m = Concurrent.newLinkedMap();
			m.put("a", 1);
			m.put("b", 2);
			assertEquals(1, m.get("a"));
			assertEquals(2, m.get("b"));
			assertEquals(2, m.size());
		}

		@Test
		void insertionOrder_preservedInEntrySet() {
			ConcurrentLinkedMap<String, Integer> m = Concurrent.newLinkedMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);

			List<String> keys = new ArrayList<>();
			for (Map.Entry<String, Integer> e : m.entrySet()) keys.add(e.getKey());
			assertEquals(List.of("c", "a", "b"), keys);
		}

		@Test
		void insertionOrder_preservedInKeySet() {
			ConcurrentLinkedMap<String, Integer> m = Concurrent.newLinkedMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);

			List<String> keys = new ArrayList<>(m.keySet());
			assertEquals(List.of("c", "a", "b"), keys);
		}

		@Test
		void maxSize_evictsEldest() {
			ConcurrentLinkedMap<String, Integer> m = Concurrent.newLinkedMap(2);
			m.put("a", 1);
			m.put("b", 2);
			m.put("c", 3);
			assertEquals(2, m.size());
			assertFalse(m.containsKey("a"));
			assertTrue(m.containsKey("b"));
			assertTrue(m.containsKey("c"));
		}
	}
}
