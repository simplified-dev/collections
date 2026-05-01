package dev.simplified.collection.tree;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentTreeMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

	@Nested
	class NavigableKeySetSortedSpliterator {

		@Test
		void naturalOrder_navigableKeySetSpliteratorComparator_isNull() {
			ConcurrentTreeMap<String, Integer> m = Concurrent.newTreeMap();
			m.put("c", 3);
			m.put("a", 1);
			m.put("b", 2);

			// navigableKeySet() returns a LockedNavigableSetView whose spliterator must surface
			// the backing TreeMap's comparator (null for natural ordering).
			NavigableSet<String> keySet = m.navigableKeySet();
			assertEquals(null, keySet.spliterator().getComparator());
		}

		@Test
		void reverseOrder_navigableKeySetSpliteratorComparator_isReverseOrder() {
			Comparator<String> reverse = Comparator.reverseOrder();
			ConcurrentTreeMap<String, Integer> m = Concurrent.newTreeMap(reverse);
			m.put("a", 1);
			m.put("c", 3);
			m.put("b", 2);

			// Identity check: navigableKeySet spliterator must surface the original comparator.
			NavigableSet<String> keySet = m.navigableKeySet();
			assertSame(reverse, keySet.spliterator().getComparator());
		}

		@Test
		void entrySetSpliterator_advertisesOrdered() {
			ConcurrentTreeMap<Integer, String> m = Concurrent.newTreeMap();
			m.put(3, "three");
			m.put(1, "one");
			m.put(2, "two");

			Spliterator<Map.Entry<Integer, String>> spliterator = m.entrySet().spliterator();
			assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED),
				"TreeMap.entrySet().spliterator() must advertise ORDERED so parallelStream() preserves key order");
			assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT),
				"TreeMap.entrySet().spliterator() must advertise DISTINCT");
		}

		@Test
		void keySetSpliterator_advertisesOrdered() {
			ConcurrentTreeMap<Integer, String> m = Concurrent.newTreeMap();
			m.put(3, "three");
			m.put(1, "one");
			m.put(2, "two");

			Spliterator<Integer> spliterator = m.keySet().spliterator();
			assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED),
				"TreeMap.keySet().spliterator() must advertise ORDERED");
			assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT),
				"TreeMap.keySet().spliterator() must advertise DISTINCT");
		}

		@Test
		void valuesSpliterator_advertisesOrdered() {
			ConcurrentTreeMap<Integer, String> m = Concurrent.newTreeMap();
			m.put(3, "three");
			m.put(1, "one");
			m.put(2, "two");

			Spliterator<String> spliterator = m.values().spliterator();
			assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED),
				"TreeMap.values().spliterator() must advertise ORDERED");
		}

		@Test
		void trySplit_preservesComparator_onNavigableKeySet() {
			Comparator<Integer> reverse = Comparator.reverseOrder();
			ConcurrentTreeMap<Integer, String> m = Concurrent.newTreeMap(reverse);
			for (int i = 0; i < 32; i++) m.put(i, "v" + i);

			NavigableSet<Integer> keySet = m.navigableKeySet();
			Spliterator<Integer> first = keySet.spliterator();
			Spliterator<Integer> second = first.trySplit();

			assertNotNull(second,
				"trySplit on a 32-element navigable key-set snapshot must produce a non-null prefix");
			assertSame(reverse, first.getComparator(),
				"Right-half spliterator must report the original comparator");
			assertSame(reverse, second.getComparator(),
				"Left-half spliterator from trySplit() must report the original comparator");
		}
	}
}
