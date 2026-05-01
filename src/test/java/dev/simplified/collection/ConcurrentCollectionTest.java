package dev.simplified.collection;
import dev.simplified.collection.ConcurrentCollection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentCollectionTest {

	@Nested
	class BasicOps {

		@Test
		void add_and_contains() {
			ConcurrentCollection<String> c = Concurrent.newList();
			c.add("a");
			c.add("b");
			assertTrue(c.contains("a"));
			assertTrue(c.contains("b"));
			assertEquals(2, c.size());
		}

		@Test
		void remove_works() {
			ConcurrentCollection<String> c = Concurrent.newList("a", "b");
			assertTrue(c.remove("a"));
			assertFalse(c.contains("a"));
			assertEquals(1, c.size());
		}

		@Test
		void iterator_visitsAllElements() {
			ConcurrentCollection<String> c = Concurrent.newList("a", "b", "c");
			int count = 0;
			for (Iterator<String> it = c.iterator(); it.hasNext(); ) {
				it.next();
				count++;
			}
			assertEquals(3, count);
		}

		@Test
		void clear_emptiesCollection() {
			ConcurrentCollection<String> c = Concurrent.newList("a", "b");
			c.clear();
			assertTrue(c.isEmpty());
		}
	}

	@Nested
	class ProjectMethods {

		@Test
		void addIf_supplier_true_addsElement() {
			ConcurrentCollection<String> c = Concurrent.newList();
			assertTrue(c.addIf(() -> true, "a"));
			assertTrue(c.contains("a"));
		}

		@Test
		void addIf_supplier_false_skips() {
			ConcurrentCollection<String> c = Concurrent.newList();
			assertFalse(c.addIf(() -> false, "a"));
			assertTrue(c.isEmpty());
		}

		@Test
		void replace_swapsExistingElement() {
			ConcurrentCollection<String> c = Concurrent.newList("a", "b");
			assertTrue(c.replace("a", "z"));
			assertFalse(c.contains("a"));
			assertTrue(c.contains("z"));
		}

		@Test
		void replace_absent_returnsFalse() {
			ConcurrentCollection<String> c = Concurrent.newList("a");
			assertFalse(c.replace("missing", "z"));
			assertFalse(c.contains("z"));
		}

		@Test
		void notContains_absent_isTrue() {
			ConcurrentCollection<String> c = Concurrent.newList("a");
			assertTrue(c.notContains("missing"));
			assertFalse(c.notContains("a"));
		}

		@Test
		void notEmpty_reflectsSize() {
			ConcurrentCollection<String> c = Concurrent.newList();
			assertFalse(c.notEmpty());
			c.add("a");
			assertTrue(c.notEmpty());
		}

		@Test
		void contains_byFunction_matchesValue() {
			ConcurrentCollection<String> c = Concurrent.newList("alpha", "bravo", "charlie");
			assertTrue(c.contains(String::length, 5));   // "alpha", "bravo"
			assertTrue(c.contains(String::length, 7));   // "charlie"
			assertFalse(c.contains(String::length, 99));
		}

		@Test
		void indexedStream_pairsElementsWithIndexAndSize() {
			ConcurrentCollection<String> c = Concurrent.newList("a", "b", "c");
			long count = c.indexedStream().count();
			assertEquals(3, count);
		}

		@Test
		void indexedStream_parallelFlag_buildsParallelStream() {
			ConcurrentCollection<String> c = Concurrent.newList("a", "b", "c");
			assertTrue(c.indexedStream(true).isParallel());
			assertFalse(c.indexedStream(false).isParallel());
		}

		@Test
		void stream_isSnapshotBased() {
			ConcurrentCollection<String> c = Concurrent.newList("a", "b", "c");
			List<String> snapshot = c.stream().toList();
			c.clear();
			assertEquals(3, snapshot.size());
		}

		@Test
		void parallelStream_isParallel() {
			ConcurrentCollection<String> c = Concurrent.newList("a", "b", "c");
			assertTrue(c.parallelStream().isParallel());
		}

		@Test
		void addAll_varargs_addsAll() {
			ConcurrentCollection<String> c = Concurrent.newList();
			assertTrue(c.addAll("a", "b", "c"));
			assertEquals(3, c.size());
		}

		@Test
		void toUnmodifiable_returnsUnmodifiableSnapshot() {
			ConcurrentCollection<String> c = Concurrent.newList("a");
			ConcurrentCollection<String> u = c.toUnmodifiable();
			assertEquals(1, u.size());
			assertThrows(UnsupportedOperationException.class, () -> u.add("b"));
		}
	}
}
