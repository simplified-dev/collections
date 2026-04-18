package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentDeque;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableDequeTest {

	@Nested
	class Rejection {

		@Test
		void directWrites_throwUOE() {
			ConcurrentDeque<String> src = new ConcurrentDeque<>();
			src.offer("a");
			ConcurrentDeque<String> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.add("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addAll(List.of("c")));
			assertThrows(UnsupportedOperationException.class, () -> u.addFirst("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.addLast("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.offerFirst("c"));
			assertThrows(UnsupportedOperationException.class, () -> u.offerLast("c"));
			assertThrows(UnsupportedOperationException.class, u::poll);
			assertThrows(UnsupportedOperationException.class, u::pollFirst);
			assertThrows(UnsupportedOperationException.class, u::pollLast);
			assertThrows(UnsupportedOperationException.class, u::remove);
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, u::removeFirst);
			assertThrows(UnsupportedOperationException.class, u::removeLast);
			assertThrows(UnsupportedOperationException.class, () -> u.push("c"));
			assertThrows(UnsupportedOperationException.class, u::pop);
			assertThrows(UnsupportedOperationException.class, u::clear);
		}
	}

	@Nested
	class LiveView {

		@Test
		void sourceMutations_visibleThroughWrapper() {
			ConcurrentDeque<String> src = new ConcurrentDeque<>();
			ConcurrentDeque<String> u = src.toUnmodifiable();

			assertTrue(u.isEmpty());
			src.offerFirst("a");
			src.offerLast("b");
			assertEquals(2, u.size());
			assertEquals("a", u.peekFirst());
			assertEquals("b", u.peekLast());
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentDeque<String> src = new ConcurrentDeque<>();
			ConcurrentDeque<String> u = src.toUnmodifiable();
			assertSame(u, u.toUnmodifiable());
		}
	}
}
