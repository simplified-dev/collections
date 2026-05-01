package dev.simplified.collection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentLinkedMapTest {

	@Nested
	class BasicOps {

		@Test
		void put_and_get() {
			ConcurrentMap<String, Integer> m = Concurrent.newLinkedMap();
			m.put("a", 1);
			m.put("b", 2);
			assertEquals(1, m.get("a"));
			assertEquals(2, m.get("b"));
			assertEquals(2, m.size());
		}

		@Test
		void insertionOrder_preservedInEntrySet() {
			ConcurrentMap<String, Integer> m = Concurrent.newLinkedMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);

			List<String> keys = new ArrayList<>();
			for (Map.Entry<String, Integer> e : m.entrySet()) keys.add(e.getKey());
			assertEquals(List.of("c", "a", "b"), keys);
		}

		@Test
		void insertionOrder_preservedInKeySet() {
			ConcurrentMap<String, Integer> m = Concurrent.newLinkedMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);

			List<String> keys = new ArrayList<>(m.keySet());
			assertEquals(List.of("c", "a", "b"), keys);
		}

		@Test
		void maxSize_evictsEldest() {
			ConcurrentMap<String, Integer> m = Concurrent.newLinkedMap(2);
			m.put("a", 1);
			m.put("b", 2);
			m.put("c", 3);
			assertEquals(2, m.size());
			assertFalse(m.containsKey("a"));
			assertTrue(m.containsKey("b"));
			assertTrue(m.containsKey("c"));
		}
	}

	@Nested
	class SpliteratorCharacteristics {

		@Test
		void linkedMap_entrySetSpliterator_advertisesOrdered() {
			ConcurrentMap<String, Integer> m = Concurrent.newLinkedMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);
			assertTrue(m.entrySet().spliterator().hasCharacteristics(Spliterator.ORDERED),
				"ConcurrentLinkedMap.entrySet() spliterator must advertise ORDERED");
		}

		@Test
		void linkedMap_keySetSpliterator_advertisesOrdered() {
			ConcurrentMap<String, Integer> m = Concurrent.newLinkedMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);
			assertTrue(m.keySet().spliterator().hasCharacteristics(Spliterator.ORDERED),
				"ConcurrentLinkedMap.keySet() spliterator must advertise ORDERED");
		}

		@Test
		void linkedMap_valuesSpliterator_advertisesOrdered() {
			ConcurrentMap<String, Integer> m = Concurrent.newLinkedMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);
			assertTrue(m.values().spliterator().hasCharacteristics(Spliterator.ORDERED),
				"ConcurrentLinkedMap.values() spliterator must advertise ORDERED");
		}

		@Test
		void hashMap_entrySetSpliterator_doesNotAdvertiseOrdered() {
			ConcurrentMap<String, Integer> m = Concurrent.newMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);
			assertFalse(m.entrySet().spliterator().hasCharacteristics(Spliterator.ORDERED),
				"ConcurrentHashMap.entrySet() spliterator must not advertise ORDERED");
		}

		@Test
		void linkedMap_parallelStreamFindFirstKey_isDeterministic() {
			ConcurrentMap<Integer, String> m = Concurrent.newLinkedMap();
			m.put(3, "three");
			m.put(1, "one");
			m.put(4, "four");
			m.put(5, "five");
			m.put(9, "nine");

			for (int i = 0; i < 100; i++) {
				Integer first = m.keySet().parallelStream().findFirst().orElseThrow();
				assertEquals(3, first,
					"ConcurrentLinkedMap.keySet().parallelStream().findFirst() must return head of insertion order");
			}
		}
	}
}
