package dev.simplified.collection.tree;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigableSetTest {

	private static ConcurrentTreeSet<Integer> seeded() {
		ConcurrentTreeSet<Integer> s = Concurrent.newTreeSet();
		s.add(10);
		s.add(20);
		s.add(30);
		s.add(40);
		return s;
	}

	@Nested
	class Scalars {

		@Test
		void firstAndLast() {
			ConcurrentTreeSet<Integer> s = seeded();
			assertEquals(10, s.first());
			assertEquals(40, s.last());
		}

		@Test
		void floorAndCeiling() {
			ConcurrentTreeSet<Integer> s = seeded();
			assertEquals(20, s.floor(25));
			assertEquals(30, s.ceiling(25));
			assertEquals(20, s.floor(20));
			assertEquals(20, s.ceiling(20));
		}

		@Test
		void lowerAndHigher() {
			ConcurrentTreeSet<Integer> s = seeded();
			assertEquals(20, s.lower(25));
			assertEquals(30, s.higher(25));
			assertEquals(10, s.lower(20));
			assertEquals(30, s.higher(20));
			assertNull(s.lower(10));
			assertNull(s.higher(40));
		}

		@Test
		void comparator_natural_isNull() {
			ConcurrentTreeSet<Integer> s = Concurrent.newTreeSet();
			assertNull(s.comparator());
		}
	}

	@Nested
	class Polling {

		@Test
		void pollFirst_removesAndReturns() {
			ConcurrentTreeSet<Integer> s = seeded();
			assertEquals(10, s.pollFirst());
			assertEquals(3, s.size());
			assertTrue(!s.contains(10));
		}

		@Test
		void pollLast_removesAndReturns() {
			ConcurrentTreeSet<Integer> s = seeded();
			assertEquals(40, s.pollLast());
			assertEquals(3, s.size());
			assertTrue(!s.contains(40));
		}

		@Test
		void pollFirst_emptySet_returnsNull() {
			ConcurrentTreeSet<Integer> s = Concurrent.newTreeSet();
			assertNull(s.pollFirst());
		}
	}

	@Nested
	class Caching {

		@Test
		void descendingSet_cachedReference_returnedTwice() {
			ConcurrentTreeSet<Integer> s = seeded();
			NavigableSet<Integer> first = s.descendingSet();
			NavigableSet<Integer> second = s.descendingSet();
			assertSame(first, second);
		}

		@Test
		void descendingSet_invalidatedOnAdd_returnsFreshView() {
			ConcurrentTreeSet<Integer> s = seeded();
			NavigableSet<Integer> firstView = s.descendingSet();
			assertEquals(40, firstView.first());

			s.add(50);
			NavigableSet<Integer> secondView = s.descendingSet();

			assertEquals(50, secondView.first());
			assertTrue(secondView.contains(50));
		}
	}

	@Nested
	class DescendingIteration {

		@Test
		void descendingIterator_traversesInReverse() {
			ConcurrentTreeSet<Integer> s = seeded();
			List<Integer> seen = new ArrayList<>();
			Iterator<Integer> it = s.descendingIterator();
			while (it.hasNext()) seen.add(it.next());
			assertEquals(List.of(40, 30, 20, 10), seen);
		}

		@Test
		void descendingIterator_isSnapshot_notReflectingPostMutation() {
			ConcurrentTreeSet<Integer> s = seeded();
			Iterator<Integer> it = s.descendingIterator();
			s.add(50);
			List<Integer> seen = new ArrayList<>();
			while (it.hasNext()) seen.add(it.next());
			assertEquals(List.of(40, 30, 20, 10), seen);
		}
	}

	@Nested
	class BoundedViews {

		@Test
		void subSet_inclusiveExclusive() {
			ConcurrentTreeSet<Integer> s = seeded();
			NavigableSet<Integer> sub = s.subSet(15, true, 35, false);
			assertEquals(2, sub.size());
			assertTrue(sub.contains(20));
			assertTrue(sub.contains(30));
		}

		@Test
		void headSet_andTailSet() {
			ConcurrentTreeSet<Integer> s = seeded();
			assertEquals(2, s.headSet(30).size());
			assertEquals(3, s.headSet(30, true).size());
			assertEquals(3, s.tailSet(20).size());
			assertEquals(2, s.tailSet(20, false).size());
		}
	}

	@Nested
	class UnmodifiableSnapshot {

		@Test
		void unmodifiable_descendingSet_throwsOnAdd() {
			ConcurrentUnmodifiableTreeSet<Integer> u = Concurrent.newUnmodifiableTreeSet(List.of(1, 2, 3));
			NavigableSet<Integer> desc = u.descendingSet();
			assertEquals(3, desc.first());
			assertThrows(UnsupportedOperationException.class, () -> desc.add(99));
		}

		@Test
		void unmodifiable_pollFirst_throws() {
			ConcurrentUnmodifiableTreeSet<Integer> u = Concurrent.newUnmodifiableTreeSet(List.of(1));
			assertThrows(UnsupportedOperationException.class, u::pollFirst);
			assertThrows(UnsupportedOperationException.class, u::pollLast);
		}

		@Test
		void unmodifiable_subSet_throwsOnAdd() {
			ConcurrentUnmodifiableTreeSet<Integer> u = Concurrent.newUnmodifiableTreeSet(List.of(1, 2, 3));
			NavigableSet<Integer> sub = u.subSet(1, true, 3, true);
			assertEquals(3, sub.size());
			assertThrows(UnsupportedOperationException.class, () -> sub.add(2));
		}

		@Test
		void unmodifiable_descendingIterator_doesNotPermitRemove() {
			ConcurrentUnmodifiableTreeSet<Integer> u = Concurrent.newUnmodifiableTreeSet(List.of(1, 2, 3));
			Iterator<Integer> it = u.descendingIterator();
			assertEquals(3, it.next());
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}
}
