package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.linked.ConcurrentLinkedMap;
import dev.simplified.collection.tree.ConcurrentTreeMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUnmodifiableMapTest {

	@Nested
	class Rejection {

		@Test
		void directWrites_throwUOE() {
			ConcurrentMap<String, Integer> src = Concurrent.newMap();
			src.put("a", 1);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.put("b", 2));
			assertThrows(UnsupportedOperationException.class, () -> u.putAll(Map.of("c", 3)));
			assertThrows(UnsupportedOperationException.class, () -> u.putIfAbsent("d", 4));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a"));
			assertThrows(UnsupportedOperationException.class, () -> u.remove("a", 1));
			assertThrows(UnsupportedOperationException.class, u::clear);
			assertThrows(UnsupportedOperationException.class, () -> u.compute("a", (k, v) -> 99));
			assertThrows(UnsupportedOperationException.class, () -> u.computeIfAbsent("z", k -> 99));
			assertThrows(UnsupportedOperationException.class, () -> u.computeIfPresent("a", (k, v) -> 99));
		}

		@Test
		void entrySet_remove_throwsUOE() {
			ConcurrentMap<String, Integer> src = Concurrent.newMap();
			src.put("a", 1);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.entrySet().remove(Map.entry("a", 1)));
			assertThrows(UnsupportedOperationException.class, () -> u.entrySet().clear());
		}

		@Test
		void entrySet_iterator_remove_throwsUOE() {
			ConcurrentMap<String, Integer> src = Concurrent.newMap();
			src.put("a", 1);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			Iterator<Map.Entry<String, Integer>> it = u.entrySet().iterator();
			assertTrue(it.hasNext());
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}

		@Test
		void entry_setValue_throwsUOE() {
			ConcurrentMap<String, Integer> src = Concurrent.newMap();
			src.put("a", 1);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			Map.Entry<String, Integer> e = u.entrySet().iterator().next();
			assertThrows(UnsupportedOperationException.class, () -> e.setValue(99));
		}

		@Test
		void keySet_remove_throwsUOE() {
			ConcurrentMap<String, Integer> src = Concurrent.newMap();
			src.put("a", 1);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.keySet().remove("a"));
			assertThrows(UnsupportedOperationException.class, () -> u.keySet().clear());

			Iterator<String> it = u.keySet().iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}

		@Test
		void values_remove_throwsUOE() {
			ConcurrentMap<String, Integer> src = Concurrent.newMap();
			src.put("a", 1);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			assertThrows(UnsupportedOperationException.class, () -> u.values().remove(1));
			assertThrows(UnsupportedOperationException.class, () -> u.values().clear());

			Iterator<Integer> it = u.values().iterator();
			it.next();
			assertThrows(UnsupportedOperationException.class, it::remove);
		}
	}

	@Nested
	class LiveView {

		@Test
		void sourceMutations_visibleThroughWrapper() {
			ConcurrentMap<String, Integer> src = Concurrent.newMap();
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			assertTrue(u.isEmpty());
			src.put("a", 1);
			assertEquals(1, u.size());
			assertEquals(1, u.get("a"));

			src.put("b", 2);
			assertEquals(2, u.size());
			assertTrue(u.containsKey("b"));

			src.remove("a");
			assertEquals(1, u.size());
			assertFalse(u.containsKey("a"));
		}

		@Test
		void entrySetIteration_reflectsCurrentState() {
			ConcurrentMap<String, Integer> src = Concurrent.newMap();
			src.put("a", 1);
			src.put("b", 2);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			List<String> keys = new ArrayList<>();
			for (Map.Entry<String, Integer> e : u.entrySet()) keys.add(e.getKey());
			assertEquals(2, keys.size());
			assertTrue(keys.contains("a"));
			assertTrue(keys.contains("b"));
		}
	}

	@Nested
	class TypePreservation {

		@Test
		void wrapsLinkedMap_preservesInsertionOrder() {
			ConcurrentLinkedMap<String, Integer> src = Concurrent.newLinkedMap();
			src.put("c", 3);
			src.put("a", 1);
			src.put("b", 2);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			List<String> order = new ArrayList<>();
			for (Map.Entry<String, Integer> e : u.entrySet()) order.add(e.getKey());
			assertEquals(List.of("c", "a", "b"), order);
		}

		@Test
		void wrapsSortedMap_preservesComparatorOrder() {
			ConcurrentTreeMap<String, Integer> src = Concurrent.newSortedMap();
			src.put("c", 3);
			src.put("a", 1);
			src.put("b", 2);
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();

			List<String> order = new ArrayList<>();
			for (Map.Entry<String, Integer> e : u.entrySet()) order.add(e.getKey());
			assertEquals(List.of("a", "b", "c"), order);
		}

		@Test
		void doubleWrap_returnsSameInstance() {
			ConcurrentMap<String, Integer> src = Concurrent.newMap();
			ConcurrentMap<String, Integer> u = src.toUnmodifiable();
			assertSame(u, u.toUnmodifiable());
		}
	}

	@Nested
	@Tag("slow")
	class ThreadSafety {

		@Test
		void concurrentReads_whileSourceWrites_noExceptions() throws Exception {
			ConcurrentMap<Integer, Integer> src = Concurrent.newMap();
			for (int i = 0; i < 1000; i++) src.put(i, i);
			ConcurrentMap<Integer, Integer> u = src.toUnmodifiable();

			int threadCount = 8;
			ExecutorService pool = Executors.newFixedThreadPool(threadCount);
			CountDownLatch latch = new CountDownLatch(threadCount);
			AtomicInteger errors = new AtomicInteger();

			for (int t = 0; t < threadCount; t++) {
				final int id = t;
				pool.submit(() -> {
					try {
						for (int i = 0; i < 1000; i++) {
							if (id % 2 == 0) {
								src.put(1_000_000 + i, i);
							} else {
								u.get(i);
								u.size();
								for (Map.Entry<Integer, Integer> ignored : u.entrySet()) { /* iter */ }
							}
						}
					} catch (Throwable t2) {
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
