package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.linked.ConcurrentLinkedSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableLinkedSetTest {

	@Nested
	class Construction {

		@Test
		void factory_empty_isLinkedSetSnapshot() {
			ConcurrentUnmodifiableLinkedSet<String> u = Concurrent.newUnmodifiableLinkedSet();
			assertEquals(0, u.size());
			assertTrue(u instanceof ConcurrentLinkedSet);
		}

		@Test
		void factory_varargs_keepsContents() {
			ConcurrentUnmodifiableLinkedSet<String> u = Concurrent.newUnmodifiableLinkedSet("a", "b", "c");
			assertEquals(3, u.size());
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(u));
		}

		@Test
		void factory_collection_keepsContents() {
			ConcurrentUnmodifiableLinkedSet<String> u = Concurrent.newUnmodifiableLinkedSet(List.of("x", "y"));
			assertEquals(2, u.size());
		}

		@Test
		void toUnmodifiable_fromLinkedSet_returnsLinkedSetUnmodifiable() {
			ConcurrentLinkedSet<String> src = Concurrent.newLinkedSet("a", "b");
			assertTrue(src.toUnmodifiable() instanceof ConcurrentUnmodifiableLinkedSet);
		}
	}

	@Nested
	class Rejection {

		@Test
		void allMutators_throwUOE() {
			ConcurrentLinkedSet<String> src = Concurrent.newLinkedSet("a");
			ConcurrentUnmodifiableLinkedSet<String> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.add("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, () -> u.removeAll(List.of("a")));
			assertThrows(UnsupportedOperationException.class, () -> u.retainAll(List.of("a")));
			assertThrows(UnsupportedOperationException.class, () -> u.removeIf(s -> true));
			assertThrows(UnsupportedOperationException.class, u::clear);
			assertThrows(UnsupportedOperationException.class, () -> u.addIf(() -> true, "z"));
		}

		@Test
		void iterator_remove_throwsUOE() {
			ConcurrentUnmodifiableLinkedSet<String> u = Concurrent.newUnmodifiableLinkedSet("a", "b");
			Iterator<String> it = u.iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}

	@Nested
	class Snapshot {

		@Test
		void sourceMutations_notVisibleThroughWrapper() {
			ConcurrentLinkedSet<String> src = Concurrent.newLinkedSet();
			src.add("a");
			ConcurrentUnmodifiableLinkedSet<String> u = src.toUnmodifiable();

			assertEquals(1, u.size());
			src.add("b");
			assertEquals(1, u.size());
			assertTrue(u.contains("a"));
			assertFalse(u.contains("b"));
		}
	}

	@Nested
	class Ordering {

		@Test
		void preservesInsertionOrder() {
			ConcurrentLinkedSet<String> src = Concurrent.newLinkedSet();
			src.add("c");
			src.add("a");
			src.add("b");
			ConcurrentUnmodifiableLinkedSet<String> u = src.toUnmodifiable();
			assertEquals(List.of("c", "a", "b"), new ArrayList<>(u));
		}
	}

	@Nested
	class Assignability {

		@Test
		void isAlsoConcurrentSetAndUnmodifiableSet() {
			ConcurrentUnmodifiableLinkedSet<String> u = Concurrent.newUnmodifiableLinkedSet();
			assertTrue(u instanceof ConcurrentSet);
			assertTrue(u instanceof ConcurrentUnmodifiableSet);
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentUnmodifiableLinkedSet<String> u = Concurrent.newUnmodifiableLinkedSet();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
