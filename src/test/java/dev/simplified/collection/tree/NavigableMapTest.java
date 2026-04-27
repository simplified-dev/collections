package dev.simplified.collection.tree;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigableMapTest {

	private static ConcurrentTreeMap<Integer, String> seeded() {
		ConcurrentTreeMap<Integer, String> m = Concurrent.newTreeMap();
		m.put(10, "a");
		m.put(20, "b");
		m.put(30, "c");
		m.put(40, "d");
		return m;
	}

	@Nested
	class Scalars {

		@Test
		void firstAndLastKey() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			assertEquals(10, m.firstKey());
			assertEquals(40, m.lastKey());
		}

		@Test
		void firstAndLastEntry() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			assertEquals(10, m.firstEntry().getKey());
			assertEquals("a", m.firstEntry().getValue());
			assertEquals(40, m.lastEntry().getKey());
			assertEquals("d", m.lastEntry().getValue());
		}

		@Test
		void floorAndCeiling() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			assertEquals(20, m.floorKey(25));
			assertEquals(30, m.ceilingKey(25));
			assertEquals(20, m.floorKey(20));
			assertEquals(20, m.ceilingKey(20));
		}

		@Test
		void lowerAndHigher() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			assertEquals(20, m.lowerKey(25));
			assertEquals(30, m.higherKey(25));
			assertEquals(10, m.lowerKey(20));
			assertEquals(30, m.higherKey(20));
			assertNull(m.lowerKey(10));
			assertNull(m.higherKey(40));
		}

		@Test
		void floorAndCeilingEntries() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			Map.Entry<Integer, String> floor = m.floorEntry(25);
			Map.Entry<Integer, String> ceiling = m.ceilingEntry(25);
			assertEquals(20, floor.getKey());
			assertEquals(30, ceiling.getKey());
		}

		@Test
		void comparator_natural_isNull() {
			ConcurrentTreeMap<Integer, String> m = Concurrent.newTreeMap();
			assertNull(m.comparator());
		}
	}

	@Nested
	class Polling {

		@Test
		void pollFirstEntry_removesAndReturns() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			Map.Entry<Integer, String> first = m.pollFirstEntry();
			assertEquals(10, first.getKey());
			assertEquals("a", first.getValue());
			assertEquals(3, m.size());
			assertNull(m.get(10));
		}

		@Test
		void pollLastEntry_removesAndReturns() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			Map.Entry<Integer, String> last = m.pollLastEntry();
			assertEquals(40, last.getKey());
			assertEquals("d", last.getValue());
			assertEquals(3, m.size());
			assertNull(m.get(40));
		}

		@Test
		void pollFirstEntry_emptyMap_returnsNull() {
			ConcurrentTreeMap<Integer, String> m = Concurrent.newTreeMap();
			assertNull(m.pollFirstEntry());
		}
	}

	@Nested
	class Caching {

		@Test
		void descendingMap_cachedReference_returnedTwice() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			NavigableMap<Integer, String> first = m.descendingMap();
			NavigableMap<Integer, String> second = m.descendingMap();
			assertSame(first, second);
		}

		@Test
		void descendingMap_invalidatedOnPut_returnsFreshView() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			NavigableMap<Integer, String> firstView = m.descendingMap();
			assertEquals(40, firstView.firstKey());

			m.put(50, "e");
			NavigableMap<Integer, String> secondView = m.descendingMap();

			assertEquals(50, secondView.firstKey());
			assertTrue(secondView.containsKey(50));
		}

		@Test
		void navigableKeySet_cached_andInvalidated() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			NavigableSet<Integer> first = m.navigableKeySet();
			NavigableSet<Integer> second = m.navigableKeySet();
			assertSame(first, second);

			m.put(5, "z");
			NavigableSet<Integer> third = m.navigableKeySet();
			assertEquals(5, third.first());
		}

		@Test
		void descendingKeySet_cached_andInvalidated() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			NavigableSet<Integer> first = m.descendingKeySet();
			assertSame(first, m.descendingKeySet());

			m.remove(40);
			NavigableSet<Integer> after = m.descendingKeySet();
			assertEquals(30, after.first());
		}
	}

	@Nested
	class BoundedViews {

		@Test
		void subMap_inclusiveExclusive() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			NavigableMap<Integer, String> sub = m.subMap(15, true, 35, false);
			assertEquals(2, sub.size());
			assertTrue(sub.containsKey(20));
			assertTrue(sub.containsKey(30));
		}

		@Test
		void headMap_exclusiveDefault() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			assertEquals(2, m.headMap(30).size());
			assertEquals(3, m.headMap(30, true).size());
		}

		@Test
		void tailMap_inclusiveDefault() {
			ConcurrentTreeMap<Integer, String> m = seeded();
			assertEquals(3, m.tailMap(20).size());
			assertEquals(2, m.tailMap(20, false).size());
		}
	}

	@Nested
	class UnmodifiableSnapshot {

		@Test
		void unmodifiable_descendingMap_throwsOnPut() {
			ConcurrentUnmodifiableTreeMap<Integer, String> u = Concurrent.newUnmodifiableTreeMap(
				java.util.Map.of(1, "a", 2, "b", 3, "c")
			);
			NavigableMap<Integer, String> desc = u.descendingMap();
			assertEquals(3, desc.firstKey());
			assertThrows(UnsupportedOperationException.class, () -> desc.put(99, "z"));
		}

		@Test
		void unmodifiable_pollFirstEntry_throws() {
			ConcurrentUnmodifiableTreeMap<Integer, String> u = Concurrent.newUnmodifiableTreeMap(
				java.util.Map.of(1, "a")
			);
			assertThrows(UnsupportedOperationException.class, u::pollFirstEntry);
			assertThrows(UnsupportedOperationException.class, u::pollLastEntry);
		}

		@Test
		void unmodifiable_navigableKeySet_throwsOnAdd() {
			ConcurrentUnmodifiableTreeMap<Integer, String> u = Concurrent.newUnmodifiableTreeMap(
				java.util.Map.of(1, "a", 2, "b")
			);
			NavigableSet<Integer> ks = u.navigableKeySet();
			assertNotNull(ks);
			assertThrows(UnsupportedOperationException.class, () -> ks.remove(1));
		}

		@Test
		void unmodifiable_subMap_throwsOnPut() {
			ConcurrentUnmodifiableTreeMap<Integer, String> u = Concurrent.newUnmodifiableTreeMap(
				java.util.Map.of(1, "a", 2, "b", 3, "c")
			);
			NavigableMap<Integer, String> sub = u.subMap(1, true, 3, true);
			assertEquals(3, sub.size());
			assertThrows(UnsupportedOperationException.class, () -> sub.put(2, "z"));
		}
	}
}
