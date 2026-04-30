package dev.simplified.collection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnmodifiableAssignabilityTest {

	@Nested
	class FamilyBaseAssignability {

		@Test
		void unmodifiableTreeMap_isMap() {
			assertTrue(Concurrent.newUnmodifiableTreeMap() instanceof ConcurrentMap);
		}

		@Test
		void unmodifiableLinkedMap_isMap() {
			assertTrue(Concurrent.newUnmodifiableLinkedMap() instanceof ConcurrentMap);
		}

		@Test
		void unmodifiableTreeSet_isSet() {
			assertTrue(Concurrent.newUnmodifiableTreeSet() instanceof ConcurrentSet);
		}

		@Test
		void unmodifiableLinkedSet_isSet() {
			assertTrue(Concurrent.newUnmodifiableLinkedSet() instanceof ConcurrentSet);
		}

		@Test
		void unmodifiableLinkedList_isList() {
			assertTrue(Concurrent.newUnmodifiableLinkedList() instanceof ConcurrentList);
		}

		@Test
		void unmodifiableDeque_isQueue() {
			assertTrue(Concurrent.newUnmodifiableDeque() instanceof ConcurrentQueue);
		}

		@Test
		void unmodifiableQueue_isCollection() {
			assertTrue(Concurrent.newUnmodifiableQueue() instanceof ConcurrentCollection);
		}

		@Test
		void unmodifiableDeque_isCollection() {
			assertTrue(Concurrent.newUnmodifiableDeque() instanceof ConcurrentCollection);
		}
	}

	@Nested
	class FamilyVariantAssignability {

		@Test
		void unmodifiableTreeMap_isTreeMap() {
			assertTrue(Concurrent.newUnmodifiableTreeMap() instanceof ConcurrentTreeMap);
		}

		@Test
		void unmodifiableTreeSet_isTreeSet() {
			assertTrue(Concurrent.newUnmodifiableTreeSet() instanceof ConcurrentTreeSet);
		}

		@Test
		void unmodifiableLinkedList_isLinkedList() {
			assertTrue(Concurrent.newUnmodifiableLinkedList() instanceof ConcurrentLinkedList);
		}
	}

	@Nested
	class WrapperClassAssignability {

		@Test
		void unmodifiableTreeMap_isUnmodifiableConcurrentTreeMap() {
			assertTrue(Concurrent.newUnmodifiableTreeMap() instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap);
		}

		@Test
		void unmodifiableLinkedMap_isUnmodifiableConcurrentLinkedHashMap() {
			assertTrue(Concurrent.newUnmodifiableLinkedMap() instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap);
		}

		@Test
		void unmodifiableTreeSet_isUnmodifiableConcurrentTreeSet() {
			assertTrue(Concurrent.newUnmodifiableTreeSet() instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet);
		}

		@Test
		void unmodifiableLinkedSet_isUnmodifiableConcurrentLinkedHashSet() {
			assertTrue(Concurrent.newUnmodifiableLinkedSet() instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashSet);
		}

		@Test
		void unmodifiableLinkedList_isUnmodifiableConcurrentLinkedList() {
			assertTrue(Concurrent.newUnmodifiableLinkedList() instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList);
		}

		@Test
		void unmodifiableDeque_isUnmodifiableConcurrentArrayDeque() {
			assertTrue(Concurrent.newUnmodifiableDeque() instanceof ConcurrentUnmodifiable.UnmodifiableConcurrentArrayDeque);
		}
	}
}
