package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.ConcurrentTreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableTreeSetTest {

	@Nested
	class Construction {

		@Test
		void factory_empty_isTreeSetSnapshot() {
			ConcurrentUnmodifiableTreeSet<String> u = Concurrent.newUnmodifiableTreeSet();
			assertEquals(0, u.size());
			assertTrue(u instanceof ConcurrentTreeSet);
		}

		@Test
		void factory_comparator_storesComparator() {
			ConcurrentUnmodifiableTreeSet<String> u = Concurrent.newUnmodifiableTreeSet(Comparator.<String>reverseOrder());
			assertNotNull(u.comparator());
		}

		@Test
		void factory_varargs_keepsContents() {
			ConcurrentUnmodifiableTreeSet<String> u = Concurrent.newUnmodifiableTreeSet("c", "a", "b");
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(u));
		}

		@Test
		void factory_comparatorAndVarargs_keepsContents() {
			ConcurrentUnmodifiableTreeSet<String> u = Concurrent.newUnmodifiableTreeSet(
				Comparator.<String>reverseOrder(), "a", "c", "b");
			assertEquals(List.of("c", "b", "a"), new ArrayList<>(u));
		}

		@Test
		void factory_collection_keepsContents() {
			ConcurrentUnmodifiableTreeSet<String> u = Concurrent.newUnmodifiableTreeSet(List.of("c", "a", "b"));
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(u));
		}

		@Test
		void factory_comparatorAndCollection_keepsContents() {
			ConcurrentUnmodifiableTreeSet<String> u = Concurrent.newUnmodifiableTreeSet(
				Comparator.<String>reverseOrder(), List.of("a", "b", "c"));
			assertEquals(List.of("c", "b", "a"), new ArrayList<>(u));
		}

		@Test
		void toUnmodifiable_fromTreeSet_returnsTreeSetUnmodifiable() {
			ConcurrentTreeSet<String> src = Concurrent.newTreeSet("a", "b");
			assertTrue(src.toUnmodifiable() instanceof ConcurrentUnmodifiableTreeSet);
		}
	}

	@Nested
	class Rejection {

		@Test
		void allMutators_throwUOE() {
			ConcurrentTreeSet<String> src = Concurrent.newTreeSet("a");
			ConcurrentUnmodifiableTreeSet<String> u = (ConcurrentUnmodifiableTreeSet<String>) src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.add("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, () -> u.removeAll(List.of("a")));
			assertThrows(UnsupportedOperationException.class, () -> u.retainAll(List.of("a")));
			assertThrows(UnsupportedOperationException.class, () -> u.removeIf(s -> true));
			assertThrows(UnsupportedOperationException.class, u::clear);
			assertThrows(UnsupportedOperationException.class, u::pollFirst);
			assertThrows(UnsupportedOperationException.class, u::pollLast);
			assertThrows(UnsupportedOperationException.class, () -> u.addIf(() -> true, "z"));
		}

		@Test
		void iterator_remove_throwsUOE() {
			ConcurrentUnmodifiableTreeSet<String> u = Concurrent.newUnmodifiableTreeSet("a", "b");
			Iterator<String> it = u.iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}

	@Nested
	class Snapshot {

		@Test
		void sourceMutations_notVisibleThroughWrapper() {
			ConcurrentTreeSet<String> src = Concurrent.newTreeSet();
			src.add("a");
			ConcurrentUnmodifiableTreeSet<String> u = (ConcurrentUnmodifiableTreeSet<String>) src.toUnmodifiable();

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
		void preservesNaturalOrder() {
			ConcurrentTreeSet<String> src = Concurrent.newTreeSet();
			src.add("c");
			src.add("a");
			src.add("b");
			ConcurrentUnmodifiableTreeSet<String> u = (ConcurrentUnmodifiableTreeSet<String>) src.toUnmodifiable();
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(u));
		}

		@Test
		void preservesComparatorOrder() {
			ConcurrentTreeSet<String> src = Concurrent.newTreeSet(Comparator.reverseOrder());
			src.add("a");
			src.add("c");
			src.add("b");
			ConcurrentUnmodifiableTreeSet<String> u = (ConcurrentUnmodifiableTreeSet<String>) src.toUnmodifiable();
			assertEquals(List.of("c", "b", "a"), new ArrayList<>(u));
		}
	}

	@Nested
	class Assignability {

		@Test
		void isAlsoConcurrentSetAndUnmodifiableSet() {
			ConcurrentUnmodifiableTreeSet<String> u = Concurrent.newUnmodifiableTreeSet();
			assertTrue(u instanceof ConcurrentSet);
			assertTrue(u instanceof ConcurrentUnmodifiableSet);
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentUnmodifiableTreeSet<String> u = Concurrent.newUnmodifiableTreeSet();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
