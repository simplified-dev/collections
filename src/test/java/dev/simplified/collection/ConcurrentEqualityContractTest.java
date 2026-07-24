package dev.simplified.collection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentEqualityContractTest {

	@Nested
	class SymmetryVsJdkPeer {

		@Test
		void arrayList_symmetricWithPlainArrayList() {
			ConcurrentList<String> concurrent = Concurrent.newList("a", "b");
			List<String> peer = new ArrayList<>(List.of("a", "b"));

			assertTrue(peer.equals(concurrent), "plain ArrayList must equal the concurrent list");
			assertTrue(concurrent.equals(peer), "concurrent list must equal the plain ArrayList");
		}

		@Test
		void arrayList_symmetricWhenContentDiffers() {
			ConcurrentList<String> concurrent = Concurrent.newList("a", "b");
			List<String> peer = new ArrayList<>(List.of("a", "b", "c"));

			assertFalse(peer.equals(concurrent));
			assertFalse(concurrent.equals(peer));
		}

		@Test
		void linkedList_symmetricWithPlainLinkedList() {
			ConcurrentList<String> concurrent = Concurrent.newLinkedList("a", "b");
			List<String> peer = new LinkedList<>(List.of("a", "b"));

			assertTrue(peer.equals(concurrent));
			assertTrue(concurrent.equals(peer));
		}

		@Test
		void linkedList_symmetricWhenContentDiffers() {
			ConcurrentList<String> concurrent = Concurrent.newLinkedList("a", "b");
			List<String> peer = new LinkedList<>(List.of("b", "a"));

			assertFalse(peer.equals(concurrent));
			assertFalse(concurrent.equals(peer));
		}

		@Test
		void hashSet_symmetricWithPlainHashSet() {
			ConcurrentSet<String> concurrent = Concurrent.newSet("a", "b");
			Set<String> peer = new HashSet<>(List.of("a", "b"));

			assertTrue(peer.equals(concurrent));
			assertTrue(concurrent.equals(peer));
		}

		@Test
		void hashSet_symmetricWhenContentDiffers() {
			ConcurrentSet<String> concurrent = Concurrent.newSet("a", "b");
			Set<String> peer = new HashSet<>(List.of("a", "c"));

			assertFalse(peer.equals(concurrent));
			assertFalse(concurrent.equals(peer));
		}

		@Test
		void linkedSet_symmetricWithPlainLinkedHashSet() {
			ConcurrentSet<String> concurrent = Concurrent.newLinkedSet("a", "b");
			Set<String> peer = new LinkedHashSet<>(List.of("a", "b"));

			assertTrue(peer.equals(concurrent));
			assertTrue(concurrent.equals(peer));
		}

		@Test
		void linkedSet_symmetricWhenContentDiffers() {
			ConcurrentSet<String> concurrent = Concurrent.newLinkedSet("a", "b");
			Set<String> peer = new LinkedHashSet<>(List.of("a", "b", "c"));

			assertFalse(peer.equals(concurrent));
			assertFalse(concurrent.equals(peer));
		}

		@Test
		void treeSet_symmetricWithPlainTreeSet() {
			ConcurrentSet<String> concurrent = Concurrent.newTreeSet("a", "b");
			Set<String> peer = new TreeSet<>(List.of("a", "b"));

			assertTrue(peer.equals(concurrent));
			assertTrue(concurrent.equals(peer));
		}

		@Test
		void treeSet_symmetricWhenContentDiffers() {
			ConcurrentSet<String> concurrent = Concurrent.newTreeSet("a", "b");
			Set<String> peer = new TreeSet<>(List.of("a", "z"));

			assertFalse(peer.equals(concurrent));
			assertFalse(concurrent.equals(peer));
		}
	}

	@Nested
	class HashCodeAgreement {

		@Test
		void arrayList_hashesAsPlainArrayList() {
			ConcurrentList<String> concurrent = Concurrent.newList("a", "b");
			List<String> peer = new ArrayList<>(List.of("a", "b"));

			assertEquals(peer.hashCode(), concurrent.hashCode());
		}

		@Test
		void linkedList_hashesAsPlainLinkedList() {
			ConcurrentList<String> concurrent = Concurrent.newLinkedList("a", "b");
			List<String> peer = new LinkedList<>(List.of("a", "b"));

			assertEquals(peer.hashCode(), concurrent.hashCode());
		}

		@Test
		void hashSet_hashesAsPlainHashSet() {
			ConcurrentSet<String> concurrent = Concurrent.newSet("a", "b");
			Set<String> peer = new HashSet<>(List.of("a", "b"));

			assertEquals(peer.hashCode(), concurrent.hashCode());
		}

		@Test
		void linkedSet_hashesAsPlainLinkedHashSet() {
			ConcurrentSet<String> concurrent = Concurrent.newLinkedSet("a", "b");
			Set<String> peer = new LinkedHashSet<>(List.of("a", "b"));

			assertEquals(peer.hashCode(), concurrent.hashCode());
		}

		@Test
		void treeSet_hashesAsPlainTreeSet() {
			ConcurrentSet<String> concurrent = Concurrent.newTreeSet("a", "b");
			Set<String> peer = new TreeSet<>(List.of("a", "b"));

			assertEquals(peer.hashCode(), concurrent.hashCode());
		}
	}

	@Nested
	class CrossContainer {

		@Test
		void list_doesNotEqualSet_bothDirections() {
			ConcurrentList<String> list = Concurrent.newList("a", "b");
			ConcurrentSet<String> set = Concurrent.newSet("a", "b");

			assertFalse(list.equals(set), "a list must never equal a set");
			assertFalse(set.equals(list), "a set must never equal a list");
		}

		@Test
		void list_doesNotEqualPlainSet_bothDirections() {
			ConcurrentList<String> list = Concurrent.newList("a", "b");
			Set<String> peer = new HashSet<>(List.of("a", "b"));

			assertFalse(list.equals(peer));
			assertFalse(peer.equals(list));
		}
	}

	@Nested
	class SelfType {

		@Test
		void twoArrayLists_equalAndHashEqual() {
			ConcurrentList<String> left = Concurrent.newList("a", "b");
			ConcurrentList<String> right = Concurrent.newList("a", "b");

			assertTrue(left.equals(right));
			assertTrue(right.equals(left));
			assertEquals(left.hashCode(), right.hashCode());
		}

		@Test
		void twoHashSets_equalAndHashEqual() {
			ConcurrentSet<String> left = Concurrent.newSet("a", "b");
			ConcurrentSet<String> right = Concurrent.newSet("b", "a");

			assertTrue(left.equals(right));
			assertTrue(right.equals(left));
			assertEquals(left.hashCode(), right.hashCode());
		}

		@Test
		void arrayList_doesNotEqualDifferentContent() {
			ConcurrentList<String> left = Concurrent.newList("a", "b");
			ConcurrentList<String> right = Concurrent.newList("b", "a");

			assertFalse(left.equals(right));
			assertFalse(right.equals(left));
		}
	}

	@Nested
	class MixedHashContainer {

		@Test
		void hashSetOfLists_findsConcurrentEquivalent() {
			Set<List<String>> holder = new HashSet<>();
			holder.add(new ArrayList<>(List.of("a", "b")));

			ConcurrentList<String> concurrent = Concurrent.newList("a", "b");
			assertTrue(holder.contains(concurrent), "hash lookup must find the plain peer via the concurrent key");

			holder.add(concurrent);
			assertEquals(1, holder.size(), "the concurrent list must collapse onto the equal plain list");
		}

		@Test
		void hashSetOfSets_findsConcurrentEquivalent() {
			Set<Set<String>> holder = new HashSet<>();
			holder.add(new HashSet<>(List.of("a", "b")));

			ConcurrentSet<String> concurrent = Concurrent.newSet("a", "b");
			assertTrue(holder.contains(concurrent));

			holder.add(concurrent);
			assertEquals(1, holder.size());
		}
	}

	@Nested
	class QueueAndDequeStayIdentity {

		// Queue and Deque specify no equality contract - AbstractCollection leaves equals as
		// Object identity and ArrayDeque deliberately does not override it. Delegating to the
		// backing ArrayDeque therefore yields identity here, which is the correct per-interface
		// behaviour rather than a gap.

		@Test
		void twoQueues_withIdenticalContent_areNotEqual() {
			ConcurrentQueue<String> left = Concurrent.newQueue("a", "b");
			ConcurrentQueue<String> right = Concurrent.newQueue("a", "b");

			assertFalse(left.equals(right), "Queue specifies no equality contract, so identity is correct");
			assertFalse(right.equals(left));
		}

		@Test
		void twoDeques_withIdenticalContent_areNotEqual() {
			ConcurrentDeque<String> left = Concurrent.newDeque("a", "b");
			ConcurrentDeque<String> right = Concurrent.newDeque("a", "b");

			assertFalse(left.equals(right), "Deque specifies no equality contract, so identity is correct");
			assertFalse(right.equals(left));
		}

		@Test
		void queue_equalsItself() {
			ConcurrentQueue<String> queue = Concurrent.newQueue("a", "b");
			assertTrue(queue.equals(queue));
		}
	}

	@Nested
	class EmptyAndNull {

		@Test
		void equalsNull_isFalseForEveryKind() {
			assertFalse(Concurrent.newList("a").equals(null));
			assertFalse(Concurrent.newLinkedList("a").equals(null));
			assertFalse(Concurrent.newSet("a").equals(null));
			assertFalse(Concurrent.newLinkedSet("a").equals(null));
			assertFalse(Concurrent.newTreeSet("a").equals(null));
			assertFalse(Concurrent.newQueue("a").equals(null));
			assertFalse(Concurrent.newDeque("a").equals(null));
		}

		@Test
		void twoEmptyLists_areEqual() {
			ConcurrentList<String> left = Concurrent.newList();
			ConcurrentList<String> right = Concurrent.newList();

			assertTrue(left.equals(right));
			assertEquals(left.hashCode(), right.hashCode());
			assertTrue(new ArrayList<String>().equals(left));
		}

		@Test
		void twoEmptySets_areEqual() {
			ConcurrentSet<String> left = Concurrent.newSet();
			ConcurrentSet<String> right = Concurrent.newSet();

			assertTrue(left.equals(right));
			assertEquals(left.hashCode(), right.hashCode());
			assertTrue(new HashSet<String>().equals(left));
		}

		@Test
		void twoEmptyTreeSets_areEqual() {
			ConcurrentSet<String> left = Concurrent.newTreeSet();
			ConcurrentSet<String> right = Concurrent.newTreeSet();

			assertTrue(left.equals(right));
			assertEquals(left.hashCode(), right.hashCode());
		}
	}

	@Nested
	class UnmodifiableView {

		@Test
		void arrayList_unmodifiableEqualsSource() {
			ConcurrentList<String> source = Concurrent.newList("a", "b");
			ConcurrentList<String> view = source.toUnmodifiable();

			assertTrue(source.equals(view));
			assertTrue(view.equals(source));
			assertEquals(source.hashCode(), view.hashCode());
		}

		@Test
		void linkedList_unmodifiableEqualsSource() {
			ConcurrentList<String> source = Concurrent.newLinkedList("a", "b");
			ConcurrentList<String> view = source.toUnmodifiable();

			assertTrue(source.equals(view));
			assertTrue(view.equals(source));
			assertEquals(source.hashCode(), view.hashCode());
		}

		@Test
		void hashSet_unmodifiableEqualsSource() {
			ConcurrentSet<String> source = Concurrent.newSet("a", "b");
			ConcurrentSet<String> view = source.toUnmodifiable();

			assertTrue(source.equals(view));
			assertTrue(view.equals(source));
			assertEquals(source.hashCode(), view.hashCode());
		}

		@Test
		void linkedSet_unmodifiableEqualsSource() {
			ConcurrentSet<String> source = Concurrent.newLinkedSet("a", "b");
			ConcurrentSet<String> view = source.toUnmodifiable();

			assertTrue(source.equals(view));
			assertTrue(view.equals(source));
			assertEquals(source.hashCode(), view.hashCode());
		}

		@Test
		void treeSet_unmodifiableEqualsSource() {
			ConcurrentSet<String> source = Concurrent.newTreeSet("a", "b");
			ConcurrentSet<String> view = source.toUnmodifiable();

			assertTrue(source.equals(view));
			assertTrue(view.equals(source));
			assertEquals(source.hashCode(), view.hashCode());
		}
	}

}
