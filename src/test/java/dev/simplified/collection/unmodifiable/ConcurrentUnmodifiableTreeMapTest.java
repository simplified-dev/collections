package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.ConcurrentTreeMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableTreeMapTest {

	@Nested
	class Construction {

		@Test
		void factory_empty_isTreeMapSnapshot() {
			ConcurrentUnmodifiableTreeMap<String, Integer> u = Concurrent.newUnmodifiableTreeMap();
			assertEquals(0, u.size());
			assertTrue(u instanceof ConcurrentTreeMap);
		}

		@Test
		void factory_comparator_storesComparator() {
			ConcurrentUnmodifiableTreeMap<String, Integer> u = Concurrent.newUnmodifiableTreeMap(Comparator.<String>reverseOrder());
			assertNotNull(u.comparator());
		}

		@Test
		void factory_entries_keepsContents() {
			ConcurrentUnmodifiableTreeMap<String, Integer> u = Concurrent.newUnmodifiableTreeMap(
				Map.entry("b", 2),
				Map.entry("a", 1)
			);
			assertEquals(2, u.size());
			assertEquals("a", u.firstKey());
			assertEquals("b", u.lastKey());
		}

		@Test
		void factory_comparatorAndEntries_keepsContents() {
			ConcurrentUnmodifiableTreeMap<String, Integer> u = Concurrent.newUnmodifiableTreeMap(
				Comparator.reverseOrder(),
				Map.entry("a", 1),
				Map.entry("b", 2)
			);
			assertEquals("b", u.firstKey());
			assertEquals("a", u.lastKey());
		}

		@Test
		void factory_map_keepsContents() {
			ConcurrentUnmodifiableTreeMap<String, Integer> u = Concurrent.newUnmodifiableTreeMap(Map.of("x", 1, "y", 2));
			assertEquals(2, u.size());
		}

		@Test
		void factory_comparatorAndMap_keepsContents() {
			ConcurrentUnmodifiableTreeMap<String, Integer> u = Concurrent.newUnmodifiableTreeMap(
				Comparator.<String>reverseOrder(), Map.of("a", 1, "b", 2));
			assertEquals("b", u.firstKey());
		}

		@Test
		void toUnmodifiable_fromTreeMap_returnsTreeMapUnmodifiable() {
			ConcurrentTreeMap<String, Integer> src = Concurrent.newTreeMap();
			src.put("a", 1);
			assertTrue(src.toUnmodifiable() instanceof ConcurrentUnmodifiableTreeMap);
		}
	}

	@Nested
	class Rejection {

		@Test
		void allMutators_throwUOE() {
			ConcurrentTreeMap<String, Integer> src = Concurrent.newTreeMap();
			src.put("a", 1);
			ConcurrentUnmodifiableTreeMap<String, Integer> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.put("b", 2));
			assertThrows(UnsupportedOperationException.class, () -> u.putAll(Map.of("c", 3)));
			assertThrows(UnsupportedOperationException.class, () -> u.putIfAbsent("d", 4));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a", 1));
			assertThrows(UnsupportedOperationException.class, u::clear);
			assertThrows(UnsupportedOperationException.class, () -> u.compute("a", (k, v) -> 99));
			assertThrows(UnsupportedOperationException.class, () -> u.computeIfAbsent("z", k -> 99));
			assertThrows(UnsupportedOperationException.class, () -> u.computeIfPresent("a", (k, v) -> 99));
			assertThrows(UnsupportedOperationException.class, u::pollFirstEntry);
			assertThrows(UnsupportedOperationException.class, u::pollLastEntry);
			assertThrows(UnsupportedOperationException.class, () -> u.putIf(() -> true, "z", 99));
			assertThrows(UnsupportedOperationException.class, () -> u.removeIf((k, v) -> true));
		}

		@Test
		void entrySet_iterator_remove_throwsUOE() {
			ConcurrentUnmodifiableTreeMap<String, Integer> u = Concurrent.newUnmodifiableTreeMap(
				Map.entry("a", 1)
			);
			Iterator<Map.Entry<String, Integer>> it = u.entrySet().iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}

	@Nested
	class Snapshot {

		@Test
		void sourceMutations_notVisibleThroughWrapper() {
			ConcurrentTreeMap<String, Integer> src = Concurrent.newTreeMap();
			src.put("a", 1);
			ConcurrentUnmodifiableTreeMap<String, Integer> u = src.toUnmodifiable();

			assertEquals(1, u.size());
			src.put("b", 2);
			assertEquals(1, u.size());
			assertFalse(u.containsKey("b"));
		}
	}

	@Nested
	class Ordering {

		@Test
		void preservesNaturalOrder_inEntrySet() {
			ConcurrentTreeMap<String, Integer> src = Concurrent.newTreeMap();
			src.put("c", 3);
			src.put("a", 1);
			src.put("b", 2);
			ConcurrentUnmodifiableTreeMap<String, Integer> u = src.toUnmodifiable();

			List<String> keys = new ArrayList<>();
			for (Map.Entry<String, Integer> e : u.entrySet()) keys.add(e.getKey());
			assertEquals(List.of("a", "b", "c"), keys);
		}

		@Test
		void preservesComparatorOrder() {
			ConcurrentTreeMap<String, Integer> src = Concurrent.newTreeMap(Comparator.reverseOrder());
			src.put("a", 1);
			src.put("c", 3);
			src.put("b", 2);
			ConcurrentUnmodifiableTreeMap<String, Integer> u = src.toUnmodifiable();
			List<String> keys = new ArrayList<>(u.keySet());
			assertEquals(List.of("c", "b", "a"), keys);
		}
	}

	@Nested
	class Assignability {

		@Test
		void isAlsoConcurrentMapAndUnmodifiableMap() {
			ConcurrentUnmodifiableTreeMap<String, Integer> u = Concurrent.newUnmodifiableTreeMap();
			assertTrue(u instanceof ConcurrentMap);
			assertTrue(u instanceof ConcurrentUnmodifiableMap);
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentUnmodifiableTreeMap<String, Integer> u = Concurrent.newUnmodifiableTreeMap();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
