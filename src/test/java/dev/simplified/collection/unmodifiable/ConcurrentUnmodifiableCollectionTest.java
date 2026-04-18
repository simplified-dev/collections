package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentCollection;
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
			ConcurrentCollection<String> src = Concurrent.newCollection("a");
			ConcurrentCollection<String> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.add("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, u::clear);
		}

		@Test
		void iterator_remove_throwsUOE() {
			ConcurrentCollection<String> src = Concurrent.newCollection("a", "b");
			ConcurrentCollection<String> u = src.toUnmodifiable();

			Iterator<String> it = u.iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}

	@Nested
	class LiveView {

		@Test
		void sourceMutations_visibleThroughWrapper() {
			ConcurrentCollection<String> src = Concurrent.newCollection();
			ConcurrentCollection<String> u = src.toUnmodifiable();

			assertTrue(u.isEmpty());
			src.add("a");
			assertEquals(1, u.size());
			assertTrue(u.contains("a"));
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentCollection<String> src = Concurrent.newCollection();
			ConcurrentCollection<String> u = src.toUnmodifiable();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
