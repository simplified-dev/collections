package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableListTest {

	@Nested
	class Rejection {

		@Test
		void directWrites_throwUOE() {
			ConcurrentList<String> src = Concurrent.newList("a", "b");
			ConcurrentList<String> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.add("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.add(0, "c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.addFirst("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addLast("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, () -> u.remove(0));
			assertThrows(UnsupportedOperationException.class, u::removeFirst);
			assertThrows(UnsupportedOperationException.class, u::removeLast);
			assertThrows(UnsupportedOperationException.class, u::clear);
			assertThrows(UnsupportedOperationException.class, () -> u.set(0, "z"));
			assertThrows(UnsupportedOperationException.class, () -> u.sort(String::compareTo));
		}

		@Test
		void iterator_remove_throwsUOE() {
			ConcurrentList<String> src = Concurrent.newList("a", "b");
			ConcurrentList<String> u = src.toUnmodifiable();

			Iterator<String> it = u.iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}

		@Test
		void listIterator_writes_throwUOE() {
			ConcurrentList<String> src = Concurrent.newList("a", "b");
			ConcurrentList<String> u = src.toUnmodifiable();

			ListIterator<String> it = u.listIterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
			assertThrows(UnsupportedOperationException.class, () -> it.set("z"));
			assertThrows(UnsupportedOperationException.class, () -> it.add("z"));
		}
	}

	@Nested
	class LiveView {

		@Test
		void sourceMutations_visibleThroughWrapper() {
			ConcurrentList<String> src = Concurrent.newList();
			ConcurrentList<String> u = src.toUnmodifiable();

			assertTrue(u.isEmpty());
			src.add("a");
			assertEquals(1, u.size());
			assertEquals("a", u.get(0));
		}
	}

	@Nested
	class TypePreservation {

		@Test
		void wrapsLinkedList_preservesInsertionOrder() {
			ConcurrentLinkedList<String> src = Concurrent.newLinkedList();
			src.add("c");
			src.add("a");
			src.add("b");
			ConcurrentList<String> u = src.toUnmodifiable();

			List<String> order = new ArrayList<>();
			for (String s : u) order.add(s);
			assertEquals(List.of("c", "a", "b"), order);
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentList<String> src = Concurrent.newList();
			ConcurrentList<String> u = src.toUnmodifiable();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
