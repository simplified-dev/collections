package dev.simplified.collection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableLinkedMapTest {

	@Nested
	class Construction {

		@Test
		void factory_empty_isLinkedMapSnapshot() {
			ConcurrentMap<String, Integer> u = Concurrent.newUnmodifiableLinkedMap();
			assertEquals(0, u.size());
			assertTrue(u instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedMap);
		}

		@Test
		void factory_entries_keepsContents() {
			ConcurrentMap<String, Integer> u = Concurrent.newUnmodifiableLinkedMap(
				Map.entry("a", 1),
				Map.entry("b", 2)
			);
			assertEquals(2, u.size());
			assertEquals(1, u.get("a"));
			assertEquals(2, u.get("b"));
		}

		@Test
		void factory_map_keepsContents() {
			ConcurrentMap<String, Integer> u = Concurrent.newUnmodifiableLinkedMap(Map.of("x", 10));
			assertEquals(10, u.get("x"));
		}

		@Test
		void toUnmodifiable_fromLinkedMap_returnsLinkedMapUnmodifiable() {
			ConcurrentMap<String, Integer> src = Concurrent.newLinkedMap();
			src.put("a", 1);
			assertTrue(src.toUnmodifiable() instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedMap);
		}
	}

	@Nested
	class Rejection {

		@Test
		void allMutators_throwUOE() {
			ConcurrentMap<String, Integer> src = Concurrent.newLinkedMap();
			src.put("a", 1);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.put("b", 2));
			assertThrows(UnsupportedOperationException.class, () -> u.putAll(Map.of("c", 3)));
			assertThrows(UnsupportedOperationException.class, () -> u.putIfAbsent("d", 4));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a", 1));
			assertThrows(UnsupportedOperationException.class, u::clear);
			assertThrows(UnsupportedOperationException.class, () -> u.compute("a", (k, v) -> 99));
			assertThrows(UnsupportedOperationException.class, () -> u.computeIfAbsent("z", k -> 99));
			assertThrows(UnsupportedOperationException.class, () -> u.computeIfPresent("a", (k, v) -> 99));
			assertThrows(UnsupportedOperationException.class, () -> u.putIf(() -> true, "z", 99));
			assertThrows(UnsupportedOperationException.class, () -> u.removeIf((k, v) -> true));
		}

		@Test
		void entrySet_iterator_remove_throwsUOE() {
			ConcurrentMap<String, Integer> u = Concurrent.newUnmodifiableLinkedMap(Map.entry("a", 1));
			Iterator<Map.Entry<String, Integer>> it = u.entrySet().iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}

	@Nested
	class Snapshot {

		@Test
		void sourceMutations_notVisibleThroughWrapper() {
			ConcurrentMap<String, Integer> src = Concurrent.newLinkedMap();
			src.put("a", 1);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			assertEquals(1, u.size());
			src.put("b", 2);
			assertEquals(1, u.size());
			assertFalse(u.containsKey("b"));
		}
	}

	@Nested
	class Ordering {

		@Test
		void preservesInsertionOrder_inEntrySet() {
			ConcurrentMap<String, Integer> src = Concurrent.newLinkedMap();
			src.put("c", 3);
			src.put("a", 1);
			src.put("b", 2);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			List<String> keys = new ArrayList<>();
			for (Map.Entry<String, Integer> e : u.entrySet()) keys.add(e.getKey());
			assertEquals(List.of("c", "a", "b"), keys);
		}
	}

	@Nested
	class Assignability {

		@Test
		void isAlsoConcurrentMap() {
			ConcurrentMap<String, Integer> u = Concurrent.newUnmodifiableLinkedMap();
			assertTrue(u instanceof ConcurrentMap);
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentMap<String, Integer> u = Concurrent.newUnmodifiableLinkedMap();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
