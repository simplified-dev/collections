package dev.simplified.collection;

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
			ConcurrentSet<String> u = Concurrent.newUnmodifiableLinkedSet();
			assertEquals(0, u.size());
			assertTrue(u instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet);
		}

		@Test
		void factory_varargs_keepsContents() {
			ConcurrentSet<String> u = Concurrent.newUnmodifiableLinkedSet("a", "b", "c");
			assertEquals(3, u.size());
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(u));
		}

		@Test
		void factory_collection_keepsContents() {
			ConcurrentSet<String> u = Concurrent.newUnmodifiableLinkedSet(List.of("x", "y"));
			assertEquals(2, u.size());
		}

		@Test
		void toUnmodifiable_fromLinkedSet_returnsLinkedSetUnmodifiable() {
			ConcurrentSet<String> src = Concurrent.newLinkedSet("a", "b");
			assertTrue(src.toUnmodifiable() instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet);
		}
	}

	@Nested
	class Rejection {

		@Test
		void allMutators_throwUOE() {
			ConcurrentSet<String> src = Concurrent.newLinkedSet("a");
			ConcurrentSet<String> u = src.toUnmodifiable();

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
			ConcurrentSet<String> u = Concurrent.newUnmodifiableLinkedSet("a", "b");
			Iterator<String> it = u.iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}

	@Nested
	class Snapshot {

		@Test
		void sourceMutations_notVisibleThroughWrapper() {
			ConcurrentSet<String> src = Concurrent.newLinkedSet();
			src.add("a");
			ConcurrentSet<String> u = src.toUnmodifiable();

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
			ConcurrentSet<String> src = Concurrent.newLinkedSet();
			src.add("c");
			src.add("a");
			src.add("b");
			ConcurrentSet<String> u = src.toUnmodifiable();
			assertEquals(List.of("c", "a", "b"), new ArrayList<>(u));
		}
	}

	@Nested
	class Assignability {

		@Test
		void isAlsoConcurrentSet() {
			ConcurrentSet<String> u = Concurrent.newUnmodifiableLinkedSet();
			assertTrue(u instanceof ConcurrentSet);
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentSet<String> u = Concurrent.newUnmodifiableLinkedSet();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
