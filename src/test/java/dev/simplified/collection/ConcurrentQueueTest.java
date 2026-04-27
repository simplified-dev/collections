package dev.simplified.collection;
import dev.simplified.collection.ConcurrentQueue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentQueueTest {

	@Nested
	class BasicOps {

		@Test
		void offer_poll_fifo() {
			ConcurrentQueue<String> q = Concurrent.newQueue();
			q.offer("a");
			q.offer("b");
			q.offer("c");
			assertEquals("a", q.poll());
			assertEquals("b", q.poll());
			assertEquals("c", q.poll());
			assertNull(q.poll());
		}

		@Test
		void peek_doesNotRemove() {
			ConcurrentQueue<String> q = Concurrent.newQueue();
			q.offer("a");
			assertEquals("a", q.peek());
			assertEquals("a", q.peek());
			assertEquals(1, q.size());
		}

		@Test
		void peek_emptyReturnsNull() {
			ConcurrentQueue<String> q = Concurrent.newQueue();
			assertNull(q.peek());
		}

		@Test
		void size_and_isEmpty() {
			ConcurrentQueue<String> q = Concurrent.newQueue();
			assertTrue(q.isEmpty());
			q.offer("a");
			q.offer("b");
			assertEquals(2, q.size());
			assertFalse(q.isEmpty());
		}
	}

	@Nested
	@Tag("slow")
	class ThreadSafety {

		@Test
		void concurrent_offerAndPoll_noExceptions() throws Exception {
			ConcurrentQueue<Integer> q = Concurrent.newQueue();
			int threadCount = 8;
			ExecutorService pool = Executors.newFixedThreadPool(threadCount);
			CountDownLatch latch = new CountDownLatch(threadCount);
			AtomicInteger errors = new AtomicInteger();

			for (int t = 0; t < threadCount; t++) {
				final int id = t;
				pool.submit(() -> {
					try {
						for (int i = 0; i < 500; i++) {
							if (id % 2 == 0) q.offer(id * 1000 + i);
							else q.poll();
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
