package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableLinkedListTest {

	@Nested
	class Construction {

		@Test
		void factory_empty_isLinkedListSnapshot() {
			ConcurrentUnmodifiableLinkedList<String> u = Concurrent.newUnmodifiableLinkedList();
			assertEquals(0, u.size());
			assertTrue(u instanceof ConcurrentLinkedList);
		}

		@Test
		void factory_varargs_keepsContents() {
			ConcurrentUnmodifiableLinkedList<String> u = Concurrent.newUnmodifiableLinkedList("a", "b", "c");
			assertEquals(3, u.size());
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(u));
		}

		@Test
		void factory_collection_keepsContents() {
			ConcurrentUnmodifiableLinkedList<String> u = Concurrent.newUnmodifiableLinkedList(List.of("x", "y"));
			assertEquals(2, u.size());
			assertEquals(List.of("x", "y"), new ArrayList<>(u));
		}

		@Test
		void toUnmodifiable_fromLinkedList_returnsLinkedListUnmodifiable() {
			ConcurrentLinkedList<String> src = Concurrent.newLinkedList("a", "b");
			assertTrue(src.toUnmodifiable() instanceof ConcurrentUnmodifiableLinkedList);
		}
	}

	@Nested
	class Rejection {

		@Test
		void allMutators_throwUOE() {
			ConcurrentLinkedList<String> src = Concurrent.newLinkedList("a", "b");
			ConcurrentUnmodifiableLinkedList<String> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.add("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.add(0, "c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(0, List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.addFirst("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addLast("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, () -> u.remove(0));
			assertThrows(UnsupportedOperationException.class, u::removeFirst);
			assertThrows(UnsupportedOperationException.class, u::removeLast);
			assertThrows(UnsupportedOperationException.class, u::clear);
			assertThrows(UnsupportedOperationException.class, () -> u.set(0, "z"));
			assertThrows(UnsupportedOperationException.class, () -> u.sort(String::compareTo));
			assertThrows(UnsupportedOperationException.class, () -> u.replaceAll(s -> s));
			assertThrows(UnsupportedOperationException.class, () -> u.removeIf(s -> true));
			assertThrows(UnsupportedOperationException.class, () -> u.removeAll(List.of("a")));
			assertThrows(UnsupportedOperationException.class, () -> u.retainAll(List.of("a")));
			assertThrows(UnsupportedOperationException.class, () -> u.addIf(() -> true, "c"));
		}

		@Test
		void iterator_remove_throwsUOE() {
			ConcurrentUnmodifiableLinkedList<String> u = Concurrent.newUnmodifiableLinkedList("a", "b");
			Iterator<String> it = u.iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}

	@Nested
	class Snapshot {

		@Test
		void sourceMutations_notVisibleThroughWrapper() {
			ConcurrentLinkedList<String> src = Concurrent.newLinkedList();
			src.add("a");
			ConcurrentUnmodifiableLinkedList<String> u = src.toUnmodifiable();

			assertEquals(1, u.size());
			src.add("b");
			assertEquals(1, u.size());
			assertEquals("a", u.get(0));
		}
	}

	@Nested
	class Ordering {

		@Test
		void preservesInsertionOrder() {
			ConcurrentLinkedList<String> src = Concurrent.newLinkedList();
			src.add("c");
			src.add("a");
			src.add("b");
			ConcurrentUnmodifiableLinkedList<String> u = src.toUnmodifiable();
			assertEquals(List.of("c", "a", "b"), new ArrayList<>(u));
		}
	}

	@Nested
	class Assignability {

		@Test
		void isAlsoConcurrentListAndUnmodifiableList() {
			ConcurrentUnmodifiableLinkedList<String> u = Concurrent.newUnmodifiableLinkedList("a");
			assertTrue(u instanceof ConcurrentList);
			assertTrue(u instanceof ConcurrentUnmodifiableList);
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentUnmodifiableLinkedList<String> u = Concurrent.newUnmodifiableLinkedList("a");
			assertSame(u, u.toUnmodifiable());
		}
	}
}
