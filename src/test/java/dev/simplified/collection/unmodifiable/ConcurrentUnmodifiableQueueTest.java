package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentQueue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableQueueTest {

	@Nested
	class Rejection {

		@Test
		void directWrites_throwUOE() {
			ConcurrentQueue<String> src = Concurrent.newQueue();
			src.offer("a");
			ConcurrentQueue<String> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.add("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.offer("c"));
			assertThrows(UnsupportedOperationException.class, u::poll);
			assertThrows(UnsupportedOperationException.class, u::remove);
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, u::clear);
		}
	}

	@Nested
	class Snapshot {

		@Test
		void sourceMutations_notVisibleThroughWrapper() {
			ConcurrentQueue<String> src = Concurrent.newQueue();
			src.offer("a");
			src.offer("b");
			ConcurrentQueue<String> u = src.toUnmodifiable();

			assertEquals(2, u.size());
			src.offer("c");
			assertEquals(2, u.size());
			assertEquals("a", u.peek());
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentQueue<String> src = Concurrent.newQueue();
			ConcurrentQueue<String> u = src.toUnmodifiable();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
