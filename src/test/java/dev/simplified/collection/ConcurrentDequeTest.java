package dev.simplified.collection;
import dev.simplified.collection.ConcurrentDeque;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentDequeTest {

	@Nested
	class BasicOps {

		@Test
		void offerFirst_offerLast_iteratesInOrder() {
			ConcurrentDeque<String> d = Concurrent.newDeque();
			d.offerLast("b");
			d.offerFirst("a");
			d.offerLast("c");
			assertEquals(List.of("a", "b", "c"), new ArrayList<>(d));
		}

		@Test
		void pollFirst_pollLast() {
			ConcurrentDeque<String> d = Concurrent.newDeque();
			d.offerLast("a");
			d.offerLast("b");
			d.offerLast("c");
			assertEquals("a", d.pollFirst());
			assertEquals("c", d.pollLast());
			assertEquals("b", d.pollFirst());
			assertNull(d.pollFirst());
		}

		@Test
		void peekFirst_peekLast() {
			ConcurrentDeque<String> d = Concurrent.newDeque();
			assertNull(d.peekFirst());
			assertNull(d.peekLast());
			d.offerLast("a");
			d.offerLast("b");
			assertEquals("a", d.peekFirst());
			assertEquals("b", d.peekLast());
		}

		@Test
		void descendingIterator_reverseOrder() {
			ConcurrentDeque<String> d = Concurrent.newDeque();
			d.offerLast("a");
			d.offerLast("b");
			d.offerLast("c");

			List<String> reversed = new ArrayList<>();
			for (Iterator<String> it = d.descendingIterator(); it.hasNext(); )
				reversed.add(it.next());
			assertEquals(List.of("c", "b", "a"), reversed);
		}

		@Test
		void removeFirstOccurrence_removesFirst() {
			ConcurrentDeque<String> d = Concurrent.newDeque();
			d.offerLast("a");
			d.offerLast("b");
			d.offerLast("a");
			assertTrue(d.removeFirstOccurrence("a"));
			assertEquals(List.of("b", "a"), new ArrayList<>(d));
		}

		@Test
		void removeLastOccurrence_removesLast() {
			ConcurrentDeque<String> d = Concurrent.newDeque();
			d.offerLast("a");
			d.offerLast("b");
			d.offerLast("a");
			assertTrue(d.removeLastOccurrence("a"));
			assertEquals(List.of("a", "b"), new ArrayList<>(d));
		}

		@Test
		void pushPop_lifo() {
			ConcurrentDeque<String> d = Concurrent.newDeque();
			d.push("a");
			d.push("b");
			d.push("c");
			assertEquals("c", d.pop());
			assertEquals("b", d.pop());
			assertEquals("a", d.pop());
		}
	}

	@Nested
	@Tag("slow")
	class ThreadSafety {

		@Test
		void concurrent_mixedEndMutation_noExceptions() throws Exception {
			ConcurrentDeque<Integer> d = Concurrent.newDeque();
			int threadCount = 8;
			ExecutorService pool = Executors.newFixedThreadPool(threadCount);
			CountDownLatch latch = new CountDownLatch(threadCount);
			AtomicInteger errors = new AtomicInteger();

			for (int t = 0; t < threadCount; t++) {
				final int id = t;
				pool.submit(() -> {
					try {
						for (int i = 0; i < 500; i++) {
							switch (id % 4) {
								case 0 -> d.offerFirst(id * 1000 + i);
								case 1 -> d.offerLast(id * 1000 + i);
								case 2 -> d.pollFirst();
								case 3 -> d.pollLast();
							}
						}
					} catch (Throwable ex) {
						errors.incrementAndGet();
					} finally {
						latch.countDown();
					}
				});
			}

			assertTrue(latch.await(10, TimeUnit.SECONDS));
			pool.shutdown();
			assertEquals(0, errors.get());
		}
	}
}
