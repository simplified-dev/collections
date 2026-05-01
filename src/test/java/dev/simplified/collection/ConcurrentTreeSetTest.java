package dev.simplified.collection.tree;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentTreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentTreeSetTest {

	@Nested
	class BasicOps {

		@Test
		void naturalOrder_iteratesSorted() {
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet();
			s.add("c");
			s.add("a");
			s.add("b");

			List<String> order = new ArrayList<>(s);
			assertEquals(List.of("a", "b", "c"), order);
		}

		@Test
		void customComparator_ordersAccordingly() {
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet(Comparator.reverseOrder());
			s.add("a");
			s.add("c");
			s.add("b");

			List<String> order = new ArrayList<>(s);
			assertEquals(List.of("c", "b", "a"), order);
		}

		@Test
		void firstElement_inIteration() {
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet();
			s.add("c");
			s.add("a");
			s.add("b");
			assertEquals("a", s.iterator().next());
		}
	}

	@Nested
	class SortedSpliterator {

		@Test
		void naturalOrder_spliteratorComparator_isNull() {
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet();
			s.add("c");
			s.add("a");
			s.add("b");

			// JDK contract: a SORTED spliterator with natural ordering returns null from getComparator().
			// TreeSet uses natural ordering when no comparator is supplied; the snapshot spliterator
			// must propagate that.
			assertEquals(null, s.spliterator().getComparator());
		}

		@Test
		void reverseOrder_spliteratorComparator_isReverseOrder() {
			Comparator<String> reverse = Comparator.reverseOrder();
			ConcurrentTreeSet<String> s = Concurrent.newTreeSet(reverse);
			s.add("a");
			s.add("c");
			s.add("b");

			// Identity check: the spliterator must surface the same comparator instance the set
			// was built with so downstream stream operations honoring SORTED don't pick a wrong order.
			assertSame(reverse, s.spliterator().getComparator());
		}

		@Test
		void spliterator_advertisesSortedAndOrdered() {
			ConcurrentTreeSet<Integer> s = Concurrent.newTreeSet();
			s.add(3);
			s.add(1);
			s.add(2);

			Spliterator<Integer> spliterator = s.spliterator();
			assertTrue(spliterator.hasCharacteristics(Spliterator.SORTED),
				"TreeSet spliterator must advertise SORTED so parallelStream().sorted() is a no-op");
			assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED),
				"TreeSet spliterator must advertise ORDERED");
			assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT),
				"TreeSet spliterator must advertise DISTINCT");
		}

		@Test
		void trySplit_preservesComparator() {
			Comparator<Integer> reverse = Comparator.reverseOrder();
			ConcurrentTreeSet<Integer> s = Concurrent.newTreeSet(reverse);
			for (int i = 0; i < 32; i++) s.add(i);

			Spliterator<Integer> first = s.spliterator();
			Spliterator<Integer> second = first.trySplit();

			assertNotNull(second, "trySplit on a 32-element snapshot must produce a non-null prefix");
			assertSame(reverse, first.getComparator(),
				"Right-half spliterator must report the original comparator");
			assertSame(reverse, second.getComparator(),
				"Left-half spliterator from trySplit() must report the original comparator");
			assertTrue(second.hasCharacteristics(Spliterator.SORTED),
				"Split-off spliterator must still advertise SORTED");
		}
	}
}
