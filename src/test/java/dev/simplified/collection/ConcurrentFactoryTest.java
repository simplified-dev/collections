package dev.simplified.collection;

import dev.simplified.collection.ConcurrentLinkedMap;
import dev.simplified.collection.ConcurrentLinkedSet;
import dev.simplified.collection.ConcurrentTreeMap;
import dev.simplified.collection.ConcurrentTreeSet;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableDeque;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedMap;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedSet;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableQueue;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeMap;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for the new {@code Concurrent.new*} and {@code Concurrent.to*} factory and
 * collector overloads added in the factory completeness pass. One test per overload, asserting
 * the returned interface type and basic content survival.
 */
class ConcurrentFactoryTest {

	private static <K, V> Map.Entry<K, V> entry(K k, V v) {
		return new AbstractMap.SimpleImmutableEntry<>(k, v);
	}

	@Nested
	class NewCreators {

		@Test
		void newTreeMap_entries() {
			ConcurrentTreeMap<String, Integer> m = Concurrent.newTreeMap(entry("b", 2), entry("a", 1));
			assertEquals(2, m.size());
			assertEquals("a", m.firstKey());
			assertEquals("b", m.lastKey());
		}

		@Test
		void newTreeMap_comparator_entries() {
			ConcurrentTreeMap<String, Integer> m = Concurrent.newTreeMap(
				Comparator.<String>reverseOrder(), entry("a", 1), entry("b", 2));
			assertEquals("b", m.firstKey());
			assertEquals("a", m.lastKey());
		}

		@Test
		void newTreeSet_varargs() {
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet("c", "a", "b");
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(s));
		}

		@Test
		void newTreeSet_comparator_varargs() {
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet(Comparator.<String>reverseOrder(), "a", "c", "b");
			assertEquals(List.of("c", "b", "a"), new ArrayList<>(s));
		}

		@Test
		void newUnmodifiableLinkedMap_entries() {
			ConcurrentUnmodifiableLinkedMap<String, Integer> m = Concurrent.newUnmodifiableLinkedMap(
				entry("a", 1), entry("b", 2));
			assertEquals(2, m.size());
			assertThrows(UnsupportedOperationException.class, () -> m.put("c", 3));
		}

		@Test
		void newUnmodifiableTreeSet_varargs() {
			ConcurrentUnmodifiableTreeSet<String> s = Concurrent.newUnmodifiableTreeSet("c", "a", "b");
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(s));
			assertThrows(UnsupportedOperationException.class, () -> s.add("z"));
		}

		@Test
		void newUnmodifiableTreeSet_comparator_varargs() {
			ConcurrentUnmodifiableTreeSet<String> s = Concurrent.newUnmodifiableTreeSet(
				Comparator.<String>reverseOrder(), "a", "b", "c");
			assertEquals(List.of("c", "b", "a"), new ArrayList<>(s));
		}

		@Test
		void newUnmodifiableTreeMap_entries() {
			ConcurrentUnmodifiableTreeMap<String, Integer> m = Concurrent.newUnmodifiableTreeMap(
				entry("b", 2), entry("a", 1));
			assertEquals(2, m.size());
			assertEquals("a", m.firstKey());
			assertThrows(UnsupportedOperationException.class, () -> m.put("c", 3));
		}

		@Test
		void newUnmodifiableTreeMap_comparator_entries() {
			ConcurrentUnmodifiableTreeMap<String, Integer> m = Concurrent.newUnmodifiableTreeMap(
				Comparator.<String>reverseOrder(), entry("a", 1), entry("b", 2));
			assertEquals("b", m.firstKey());
		}
	}

	@Nested
	class Collectors {

		@Test
		void toQueue() {
			ConcurrentQueue<Integer> q = Stream.of(1, 2, 3).collect(Concurrent.toQueue());
			assertNotNull(q);
			assertEquals(3, q.size());
			assertTrue(q.contains(2));
		}

		@Test
		void toDeque() {
			ConcurrentDeque<Integer> d = Stream.of(1, 2, 3).collect(Concurrent.toDeque());
			assertEquals(3, d.size());
			assertTrue(d.contains(2));
		}

		@Test
		void toLinkedSet() {
			ConcurrentSet<Integer> s = Stream.of(3, 1, 2).collect(Concurrent.toLinkedSet());
			assertEquals(3, s.size());
			assertEquals(List.of(3, 1, 2), new ArrayList<>(s));
		}

		@Test
		void toTreeSet_natural() {
			ConcurrentTreeSet<Integer> s = Stream.of(3, 1, 2).collect(Concurrent.toTreeSet());
			assertEquals(List.of(1, 2, 3), new ArrayList<>(s));
		}

		@Test
		void toTreeSet_comparator() {
			ConcurrentTreeSet<Integer> s = Stream.of(1, 2, 3).collect(Concurrent.toTreeSet(Comparator.reverseOrder()));
			assertEquals(List.of(3, 2, 1), new ArrayList<>(s));
		}

		@Test
		void toTreeMap_comparator_entries() {
			ConcurrentTreeMap<String, Integer> m = Stream.of(entry("b", 2), entry("a", 1))
				.collect(Concurrent.toTreeMap(Comparator.<String>reverseOrder()));
			assertEquals("b", m.firstKey());
			assertEquals("a", m.lastKey());
		}

		@Test
		void toTreeMap_keyValueMappers() {
			ConcurrentTreeMap<String, Integer> m = Stream.of("ab", "a", "abc")
				.collect(Concurrent.toTreeMap(s -> s, String::length));
			assertEquals(3, m.size());
			assertEquals("a", m.firstKey());
		}

		@Test
		void toTreeMap_keyValueMappers_merge() {
			ConcurrentTreeMap<String, Integer> m = Stream.of("ab", "cd", "ef")
				.collect(Concurrent.toTreeMap(s -> "k", String::length, Integer::sum));
			assertEquals(1, m.size());
			assertEquals(6, m.get("k"));
		}

		@Test
		void toTreeMap_comparator_keyValueMappers_merge() {
			ConcurrentTreeMap<String, Integer> m = Stream.of("ab", "a", "abc")
				.collect(Concurrent.toTreeMap(Comparator.<String>reverseOrder(), s -> s, String::length, Integer::sum));
			assertEquals("abc", m.firstKey());
		}

		@Test
		void toUnmodifiableQueue() {
			ConcurrentUnmodifiableQueue<Integer> q = Stream.of(1, 2, 3).collect(Concurrent.toUnmodifiableQueue());
			assertEquals(3, q.size());
			assertThrows(UnsupportedOperationException.class, () -> q.offer(4));
		}

		@Test
		void toUnmodifiableDeque() {
			ConcurrentUnmodifiableDeque<Integer> d = Stream.of(1, 2, 3).collect(Concurrent.toUnmodifiableDeque());
			assertEquals(3, d.size());
			assertThrows(UnsupportedOperationException.class, () -> d.offerFirst(0));
		}

		@Test
		void toUnmodifiableLinkedSet() {
			ConcurrentUnmodifiableLinkedSet<Integer> s = Stream.of(3, 1, 2)
				.collect(Concurrent.toUnmodifiableLinkedSet());
			assertEquals(List.of(3, 1, 2), new ArrayList<>(s));
			assertThrows(UnsupportedOperationException.class, () -> s.add(99));
		}

		@Test
		void toUnmodifiableTreeSet_natural() {
			ConcurrentUnmodifiableTreeSet<Integer> s = Stream.of(3, 1, 2)
				.collect(Concurrent.toUnmodifiableTreeSet());
			assertEquals(List.of(1, 2, 3), new ArrayList<>(s));
			assertThrows(UnsupportedOperationException.class, () -> s.add(99));
		}

		@Test
		void toUnmodifiableTreeSet_comparator() {
			ConcurrentUnmodifiableTreeSet<Integer> s = Stream.of(1, 2, 3)
				.collect(Concurrent.toUnmodifiableTreeSet(Comparator.reverseOrder()));
			assertEquals(List.of(3, 2, 1), new ArrayList<>(s));
		}

		@Test
		void toUnmodifiableLinkedMap_entries() {
			ConcurrentUnmodifiableLinkedMap<String, Integer> m = Stream.of(entry("c", 3), entry("a", 1), entry("b", 2))
				.collect(Concurrent.<String, Integer, Map.Entry<String, Integer>>toUnmodifiableLinkedMap());
			assertEquals(List.of("c", "a", "b"), new ArrayList<>(m.keySet()));
			assertThrows(UnsupportedOperationException.class, () -> m.put("z", 0));
		}

		@Test
		void toUnmodifiableLinkedMap_entries_merge() {
			ConcurrentUnmodifiableLinkedMap<String, Integer> m = Stream.of(entry("a", 1), entry("a", 2))
				.collect(Concurrent.<String, Integer, Map.Entry<String, Integer>>toUnmodifiableLinkedMap(Integer::sum));
			assertEquals(3, m.get("a"));
		}

		@Test
		void toUnmodifiableLinkedMap_keyValueMappers() {
			ConcurrentUnmodifiableLinkedMap<Character, Integer> m = Stream.of("ab", "cd")
				.collect(Concurrent.toUnmodifiableLinkedMap(s -> s.charAt(0), String::length));
			assertEquals(2, m.size());
			assertEquals(2, m.get('a'));
		}

		@Test
		void toUnmodifiableLinkedMap_keyValueMappers_merge() {
			ConcurrentUnmodifiableLinkedMap<String, Integer> m = Stream.of("ab", "cd", "ef")
				.collect(Concurrent.toUnmodifiableLinkedMap(s -> "k", String::length, Integer::sum));
			assertEquals(6, m.get("k"));
		}

		@Test
		void toUnmodifiableTreeMap_entries() {
			ConcurrentUnmodifiableTreeMap<String, Integer> m = Stream.of(entry("c", 3), entry("a", 1), entry("b", 2))
				.collect(Concurrent.<String, Integer, Map.Entry<String, Integer>>toUnmodifiableTreeMap());
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(m.keySet()));
			assertThrows(UnsupportedOperationException.class, () -> m.put("z", 0));
		}

		@Test
		void toUnmodifiableTreeMap_keyValueMappers() {
			ConcurrentUnmodifiableTreeMap<String, Integer> m = Stream.of("c", "a", "b")
				.collect(Concurrent.toUnmodifiableTreeMap(s -> s, String::length));
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(m.keySet()));
		}

		@Test
		void toUnmodifiableTreeMap_keyValueMappers_merge() {
			ConcurrentUnmodifiableTreeMap<String, Integer> m = Stream.of("ab", "cd", "ef")
				.collect(Concurrent.toUnmodifiableTreeMap(s -> "k", String::length, Integer::sum));
			assertEquals(6, m.get("k"));
		}

		@Test
		void toUnmodifiableTreeMap_comparator_entries() {
			ConcurrentUnmodifiableTreeMap<String, Integer> m = Stream.of(entry("a", 1), entry("b", 2))
				.collect(Concurrent.<String, Integer, Map.Entry<String, Integer>>toUnmodifiableTreeMap(Comparator.<String>reverseOrder()));
			assertEquals("b", m.firstKey());
		}
	}

	@Nested
	class FactoryTypeChecks {

		@Test
		void newTreeMap_isConcurrentTreeMap() {
			assertTrue(Concurrent.newTreeMap(entry("a", 1)) instanceof ConcurrentTreeMap);
		}

		@Test
		void newTreeMap_comparator_isConcurrentTreeMap() {
			assertTrue(Concurrent.newTreeMap(Comparator.naturalOrder(), entry("a", 1)) instanceof ConcurrentTreeMap);
		}

		@Test
		void newUnmodifiableLinkedMap_isLinkedMap() {
			assertTrue(Concurrent.newUnmodifiableLinkedMap(entry("a", 1)) instanceof ConcurrentLinkedMap);
		}

		@Test
		void newUnmodifiableTreeMap_entries_isTreeMap() {
			assertTrue(Concurrent.newUnmodifiableTreeMap(entry("a", 1)) instanceof ConcurrentTreeMap);
		}

		@Test
		void newUnmodifiableTreeMap_comparator_entries_isTreeMap() {
			assertTrue(Concurrent.newUnmodifiableTreeMap(Comparator.naturalOrder(), entry("a", 1)) instanceof ConcurrentTreeMap);
		}
	}
}
