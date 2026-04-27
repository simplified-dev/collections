package dev.simplified.collection.linked;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.linked.ConcurrentLinkedSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConcurrentLinkedSetTest {

	@Nested
	class BasicOps {

		@Test
		void add_dedupes() {
			ConcurrentLinkedSet<String> s = Concurrent.newLinkedSet();
			s.add("a");
			s.add("b");
			s.add("a");
			assertEquals(2, s.size());
		}

		@Test
		void insertionOrder_preserved() {
			ConcurrentLinkedSet<String> s = Concurrent.newLinkedSet();
			s.add("c");
			s.add("a");
			s.add("b");

			List<String> order = new ArrayList<>(s);
			assertEquals(List.of("c", "a", "b"), order);
		}

		@Test
		void remove_works() {
			ConcurrentLinkedSet<String> s = Concurrent.newLinkedSet();
			s.add("a");
			s.add("b");
			s.remove("a");
			assertFalse(s.contains("a"));
			assertEquals(1, s.size());
		}
	}
}
