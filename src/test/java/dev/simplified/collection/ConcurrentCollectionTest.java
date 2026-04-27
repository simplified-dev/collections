package dev.simplified.collection;
import dev.simplified.collection.ConcurrentCollection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentCollectionTest {

	@Nested
	class BasicOps {

		@Test
		void add_and_contains() {
			ConcurrentCollection<String> c = Concurrent.newCollection();
			c.add("a");
			c.add("b");
			assertTrue(c.contains("a"));
			assertTrue(c.contains("b"));
			assertEquals(2, c.size());
		}

		@Test
		void remove_works() {
			ConcurrentCollection<String> c = Concurrent.newCollection("a", "b");
			assertTrue(c.remove("a"));
			assertFalse(c.contains("a"));
			assertEquals(1, c.size());
		}

		@Test
		void iterator_visitsAllElements() {
			ConcurrentCollection<String> c = Concurrent.newCollection("a", "b", "c");
			int count = 0;
			for (Iterator<String> it = c.iterator(); it.hasNext(); ) {
				it.next();
				count++;
			}
			assertEquals(3, count);
		}

		@Test
		void clear_emptiesCollection() {
			ConcurrentCollection<String> c = Concurrent.newCollection("a", "b");
			c.clear();
			assertTrue(c.isEmpty());
		}
	}
}
