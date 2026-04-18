package dev.simplified.collection.unmodifiable;

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
			ConcurrentQueue<String> src = new ConcurrentQueue<>();
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
	class LiveView {

		@Test
		void sourceMutations_visibleThroughWrapper() {
			ConcurrentQueue<String> src = new ConcurrentQueue<>();
			ConcurrentQueue<String> u = src.toUnmodifiable();

			assertTrue(u.isEmpty());
			src.offer("a");
			src.offer("b");
			assertEquals(2, u.size());
			assertEquals("a", u.peek());
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentQueue<String> src = new ConcurrentQueue<>();
			ConcurrentQueue<String> u = src.toUnmodifiable();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
