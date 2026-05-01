package dev.simplified.collection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentLinkedSetTest {

	@Nested
	class BasicOps {

		@Test
		void add_dedupes() {
			ConcurrentSet<String> s = Concurrent.newLinkedSet();
			s.add("a");
			s.add("b");
			s.add("a");
			assertEquals(2, s.size());
		}

		@Test
		void insertionOrder_preserved() {
			ConcurrentSet<String> s = Concurrent.newLinkedSet();
			s.add("c");
			s.add("a");
			s.add("b");

			List<String> order = new ArrayList<>(s);
			assertEquals(List.of("c", "a", "b"), order);
		}

		@Test
		void remove_works() {
			ConcurrentSet<String> s = Concurrent.newLinkedSet();
			s.add("a");
			s.add("b");
			s.remove("a");
			assertFalse(s.contains("a"));
			assertEquals(1, s.size());
		}
	}

	@Nested
	class SpliteratorCharacteristics {

		@Test
		void linkedSet_spliterator_advertisesOrdered() {
			ConcurrentSet<String> s = Concurrent.newLinkedSet();
			s.add("c");
			s.add("a");
			s.add("b");
			assertTrue(s.spliterator().hasCharacteristics(Spliterator.ORDERED),
				"ConcurrentLinkedSet must advertise ORDERED so encounter-order-sensitive stream operations preserve insertion order");
		}

		@Test
		void hashSet_spliterator_doesNotAdvertiseOrdered() {
			ConcurrentSet<String> s = Concurrent.newSet();
			s.add("c");
			s.add("a");
			s.add("b");
			assertFalse(s.spliterator().hasCharacteristics(Spliterator.ORDERED),
				"ConcurrentHashSet must not advertise ORDERED - HashSet has no defined encounter order");
		}

		@Test
		void linkedSet_parallelStreamFindFirst_isDeterministic() {
			ConcurrentSet<Integer> s = Concurrent.newLinkedSet();
			s.add(3);
			s.add(1);
			s.add(4);
			s.add(1);
			s.add(5);
			s.add(9);
			s.add(2);
			s.add(6);

			for (int i = 0; i < 100; i++) {
				Integer first = s.parallelStream().findFirst().orElseThrow();
				assertEquals(3, first,
					"ConcurrentLinkedSet.parallelStream().findFirst() must return the head of the insertion order on every iteration");
			}
		}
	}
}
