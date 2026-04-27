package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import dev.simplified.collection.linked.ConcurrentLinkedMap;
import dev.simplified.collection.linked.ConcurrentLinkedSet;
import dev.simplified.collection.tree.ConcurrentTreeMap;
import dev.simplified.collection.tree.ConcurrentTreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnmodifiableAssignabilityTest {

	@Nested
	class FamilyBaseAssignability {

		@Test
		void unmodifiableTreeMap_isUnmodifiableMap() {
			assertTrue(Concurrent.newUnmodifiableTreeMap() instanceof ConcurrentUnmodifiableMap);
		}

		@Test
		void unmodifiableLinkedMap_isUnmodifiableMap() {
			assertTrue(Concurrent.newUnmodifiableLinkedMap() instanceof ConcurrentUnmodifiableMap);
		}

		@Test
		void unmodifiableTreeSet_isUnmodifiableSet() {
			assertTrue(Concurrent.newUnmodifiableTreeSet() instanceof ConcurrentUnmodifiableSet);
		}

		@Test
		void unmodifiableLinkedSet_isUnmodifiableSet() {
			assertTrue(Concurrent.newUnmodifiableLinkedSet() instanceof ConcurrentUnmodifiableSet);
		}

		@Test
		void unmodifiableLinkedList_isUnmodifiableList() {
			assertTrue(Concurrent.newUnmodifiableLinkedList() instanceof ConcurrentUnmodifiableList);
		}

		@Test
		void unmodifiableDeque_isUnmodifiableQueue() {
			assertTrue(Concurrent.newUnmodifiableDeque() instanceof ConcurrentUnmodifiableQueue);
		}

		@Test
		void unmodifiableQueue_isUnmodifiableCollection() {
			assertTrue(Concurrent.newUnmodifiableQueue() instanceof ConcurrentUnmodifiableCollection);
		}

		@Test
		void unmodifiableDeque_isUnmodifiableCollection() {
			assertTrue(Concurrent.newUnmodifiableDeque() instanceof ConcurrentUnmodifiableCollection);
		}
	}

	@Nested
	class FamilyVariantAssignability {

		@Test
		void unmodifiableTreeMap_isTreeMap() {
			assertTrue(Concurrent.newUnmodifiableTreeMap() instanceof ConcurrentTreeMap);
		}

		@Test
		void unmodifiableLinkedMap_isLinkedMap() {
			assertTrue(Concurrent.newUnmodifiableLinkedMap() instanceof ConcurrentLinkedMap);
		}

		@Test
		void unmodifiableTreeSet_isTreeSet() {
			assertTrue(Concurrent.newUnmodifiableTreeSet() instanceof ConcurrentTreeSet);
		}

		@Test
		void unmodifiableLinkedSet_isLinkedSet() {
			assertTrue(Concurrent.newUnmodifiableLinkedSet() instanceof ConcurrentLinkedSet);
		}

		@Test
		void unmodifiableLinkedList_isLinkedList() {
			assertTrue(Concurrent.newUnmodifiableLinkedList() instanceof ConcurrentLinkedList);
		}
	}

	@Nested
	class UnmodifiableInterfaceAssignability {

		@Test
		void unmodifiableTreeMap_isUnmodifiableTreeMap() {
			assertTrue(Concurrent.newUnmodifiableTreeMap() instanceof ConcurrentUnmodifiableTreeMap);
		}

		@Test
		void unmodifiableLinkedMap_isUnmodifiableLinkedMap() {
			assertTrue(Concurrent.newUnmodifiableLinkedMap() instanceof ConcurrentUnmodifiableLinkedMap);
		}

		@Test
		void unmodifiableTreeSet_isUnmodifiableTreeSet() {
			assertTrue(Concurrent.newUnmodifiableTreeSet() instanceof ConcurrentUnmodifiableTreeSet);
		}

		@Test
		void unmodifiableLinkedSet_isUnmodifiableLinkedSet() {
			assertTrue(Concurrent.newUnmodifiableLinkedSet() instanceof ConcurrentUnmodifiableLinkedSet);
		}

		@Test
		void unmodifiableLinkedList_isUnmodifiableLinkedList() {
			assertTrue(Concurrent.newUnmodifiableLinkedList() instanceof ConcurrentUnmodifiableLinkedList);
		}

		@Test
		void unmodifiableDeque_isUnmodifiableDeque() {
			assertTrue(Concurrent.newUnmodifiableDeque() instanceof ConcurrentUnmodifiableDeque);
		}
	}
}
