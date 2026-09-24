package dev.simplified.collection;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentCollection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableCollectionTest {

	@Nested
	class Rejection {

		@Test
		void directWrites_throwUOE() {
			ConcurrentCollection<String> src = Concurrent.newList("a");
			ConcurrentCollection<String> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.add("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, u::clear);
		}

		@Test
		void iterator_remove_throwsUOE() {
			ConcurrentCollection<String> src = Concurrent.newList("a", "b");
			ConcurrentCollection<String> u = src.toUnmodifiable();

			Iterator<String> it = u.iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}

		@Test
		void replace_throwsUOE_onEveryWrapper() {
			// replace is final - it is one atomic remove-then-add rather than the pair of calls a
			// caller could make - so no wrapper can reject it by overriding it, and every one of
			// them has to reject it through the mutation hook instead.
			for (ConcurrentCollection<String> unmodifiable : everyKind())
				assertThrows(UnsupportedOperationException.class, () -> unmodifiable.replace("a", "b"), named(unmodifiable));
		}

		@Test
		void addIf_throwsUOE_onEveryWrapper() {
			for (ConcurrentCollection<String> unmodifiable : everyKind())
				assertThrows(UnsupportedOperationException.class, () -> unmodifiable.addIf(() -> true, "b"), named(unmodifiable));
		}

		@Test
		void addIfOverTheBacking_throwsUOE_onEveryWrapper() {
			// The overload testing the backing collection is not on ConcurrentCollection, so it is
			// only reachable through the concrete type - which is exactly what the factories hand
			// back, and where two of the wrappers had no override at all.
			assertThrows(UnsupportedOperationException.class, () -> Concurrent.<String>newUnmodifiableList("a").addIf(backing -> true, "b"));
			assertThrows(UnsupportedOperationException.class, () -> Concurrent.<String>newUnmodifiableLinkedList("a").addIf(backing -> true, "b"));
			assertThrows(UnsupportedOperationException.class, () -> Concurrent.<String>newUnmodifiableSet("a").addIf(backing -> true, "b"));
			assertThrows(UnsupportedOperationException.class, () -> Concurrent.<String>newUnmodifiableLinkedSet("a").addIf(backing -> true, "b"));
			assertThrows(UnsupportedOperationException.class, () -> Concurrent.<String>newUnmodifiableTreeSet("a").addIf(backing -> true, "b"));
			assertThrows(UnsupportedOperationException.class, () -> Concurrent.<String>newUnmodifiableQueue("a").addIf(backing -> true, "b"));
			assertThrows(UnsupportedOperationException.class, () -> Concurrent.<String>newUnmodifiableDeque("a").addIf(backing -> true, "b"));
		}

		@Test
		void removeIf_throwsUOE_onEveryWrapper() {
			// Unconditionally, the way Collections.unmodifiableCollection does it - the inherited
			// default only reaches a rejection when the predicate happens to match something.
			for (ConcurrentCollection<String> unmodifiable : everyKind())
				assertThrows(UnsupportedOperationException.class, () -> unmodifiable.removeIf(held -> false), named(unmodifiable));
		}

		@Test
		void refusedWrites_leaveTheSnapshotAlone() {
			for (ConcurrentCollection<String> unmodifiable : everyKind()) {
				assertThrows(UnsupportedOperationException.class, () -> unmodifiable.replace("a", "b"));
				assertEquals(List.of("a"), List.copyOf(unmodifiable), named(unmodifiable));
			}
		}
	}

	/**
	 * One unmodifiable snapshot of every kind, each holding the single element {@code "a"}.
	 */
	private static List<ConcurrentCollection<String>> everyKind() {
		return List.of(
			Concurrent.newUnmodifiableList("a"),
			Concurrent.newUnmodifiableLinkedList("a"),
			Concurrent.newUnmodifiableSet("a"),
			Concurrent.newUnmodifiableLinkedSet("a"),
			Concurrent.newUnmodifiableTreeSet("a"),
			Concurrent.newUnmodifiableQueue("a"),
			Concurrent.newUnmodifiableDeque("a")
		);
	}

	private static String named(@NotNull ConcurrentCollection<?> collection) {
		return collection.getClass().getSimpleName();
	}

	@Nested
	class Snapshot {

		@Test
		void sourceMutations_notVisibleThroughWrapper() {
			ConcurrentCollection<String> src = Concurrent.newList();
			src.add("a");
			ConcurrentCollection<String> u = src.toUnmodifiable();

			assertEquals(1, u.size());
			src.add("b");
			assertEquals(1, u.size());
			assertTrue(u.contains("a"));
			assertFalse(u.contains("b"));
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentCollection<String> src = Concurrent.newList();
			ConcurrentCollection<String> u = src.toUnmodifiable();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
