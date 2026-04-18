package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.linked.ConcurrentLinkedSet;
import dev.simplified.collection.tree.ConcurrentTreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableSetTest {

	@Nested
	class Rejection {

		@Test
		void directWrites_throwUOE() {
			ConcurrentSet<String> src = Concurrent.newSet("a");
			ConcurrentSet<String> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.add("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, u::clear);
		}

		@Test
		void iterator_remove_throwsUOE() {
			ConcurrentSet<String> src = Concurrent.newSet("a", "b");
			ConcurrentSet<String> u = src.toUnmodifiable();

			Iterator<String> it = u.iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}

	@Nested
	class LiveView {

		@Test
		void sourceMutations_visibleThroughWrapper() {
			ConcurrentSet<String> src = Concurrent.newSet();
			ConcurrentSet<String> u = src.toUnmodifiable();

			assertTrue(u.isEmpty());
			src.add("a");
			assertEquals(1, u.size());
			assertTrue(u.contains("a"));
		}
	}

	@Nested
	class TypePreservation {

		@Test
		void wrapsLinkedSet_preservesInsertionOrder() {
			ConcurrentLinkedSet<String> src = Concurrent.newLinkedSet();
			src.add("c");
			src.add("a");
			src.add("b");
			ConcurrentSet<String> u = src.toUnmodifiable();

			List<String> order = new ArrayList<>();
			for (String s : u) order.add(s);
			assertEquals(List.of("c", "a", "b"), order);
		}

		@Test
		void wrapsSortedSet_preservesComparatorOrder() {
			ConcurrentTreeSet<String> src = Concurrent.newSortedSet();
			src.add("c");
			src.add("a");
			src.add("b");
			ConcurrentSet<String> u = src.toUnmodifiable();

			List<String> order = new ArrayList<>();
			for (String s : u) order.add(s);
			assertEquals(List.of("a", "b", "c"), order);
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentSet<String> src = Concurrent.newSet();
			ConcurrentSet<String> u = src.toUnmodifiable();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
