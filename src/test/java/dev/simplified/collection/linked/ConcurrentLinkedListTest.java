package dev.simplified.collection.linked;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentLinkedListTest {

	@Nested
	class BasicOps {

		@Test
		void add_and_get() {
			ConcurrentLinkedList<String> l = Concurrent.newLinkedList();
			l.add("a");
			l.add("b");
			assertEquals("a", l.get(0));
			assertEquals("b", l.get(1));
			assertEquals(2, l.size());
		}

		@Test
		void insertionOrder_preserved() {
			ConcurrentLinkedList<String> l = Concurrent.newLinkedList();
			l.add("c");
			l.add("a");
			l.add("b");

			List<String> order = new ArrayList<>(l);
			assertEquals(List.of("c", "a", "b"), order);
		}

		@Test
		void addFirst_addLast() {
			ConcurrentLinkedList<String> l = Concurrent.newLinkedList();
			l.add("b");
			l.addFirst("a");
			l.addLast("c");
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(l));
		}

		@Test
		void remove_byIndex() {
			ConcurrentLinkedList<String> l = Concurrent.newLinkedList();
			l.add("a");
			l.add("b");
			l.add("c");
			l.remove(1);
			assertEquals(List.of("a", "c"), new ArrayList<>(l));
		}
	}
}
