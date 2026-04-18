package dev.simplified.collection;

import lombok.Cleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentMapTest {

    private ConcurrentMap<String, Integer> map;

    @BeforeEach
    void setup() {
        map = Concurrent.newMap();
    }

    @Nested
    class BasicOperations {

        @Test
        void put_and_get() {
            map.put("a", 1);
            map.put("b", 2);
            assertEquals(2, map.size());
            assertEquals(1, map.get("a"));
            assertEquals(2, map.get("b"));
        }

        @Test
        void put_overwritesExisting() {
            map.put("a", 1);
            map.put("a", 99);
            assertEquals(99, map.get("a"));
            assertEquals(1, map.size());
        }

        @Test
        void remove_byKey() {
            map.put("a", 1);
            map.put("b", 2);
            Integer removed = map.remove("a");
            assertEquals(1, removed);
            assertNull(map.get("a"));
            assertEquals(1, map.size());
        }

        @Test
        void containsKey_and_containsValue() {
            map.put("x", 42);
            assertTrue(map.containsKey("x"));
            assertFalse(map.containsKey("y"));
            assertTrue(map.containsValue(42));
            assertFalse(map.containsValue(99));
        }

        @Test
        void clear_emptiesMap() {
            map.put("a", 1);
            map.put("b", 2);
            map.clear();
            assertTrue(map.isEmpty());
            assertEquals(0, map.size());
        }

        @Test
        void getOrDefault_present() {
            map.put("a", 1);
            assertEquals(1, map.getOrDefault("a", -1));
        }

        @Test
        void getOrDefault_absent() {
            assertEquals(-1, map.getOrDefault("missing", -1));
        }

        @Test
        void putIfAbsent_doesNotOverwrite() {
            map.put("a", 1);
            map.putIfAbsent("a", 99);
            assertEquals(1, map.get("a"));
        }

        @Test
        void putIfAbsent_insertsWhenAbsent() {
            map.putIfAbsent("a", 1);
            assertEquals(1, map.get("a"));
        }
    }

    @Nested
    class ComputeFamily {

        @Test
        void compute_insertsWhenAbsent() {
            Integer result = map.compute("a", (k, v) -> 42);
            assertEquals(42, result);
            assertEquals(42, map.get("a"));
        }

        @Test
        void compute_replacesWhenPresent() {
            map.put("a", 10);
            Integer result = map.compute("a", (k, v) -> v + 1);
            assertEquals(11, result);
            assertEquals(11, map.get("a"));
        }

        @Test
        void compute_removesWhenFunctionReturnsNull() {
            map.put("a", 10);
            Integer result = map.compute("a", (k, v) -> null);
            assertNull(result);
            assertFalse(map.containsKey("a"));
        }

        @Test
        void computeIfAbsent_insertsAndReturns() {
            Integer result = map.computeIfAbsent("a", k -> 99);
            assertEquals(99, result);
            assertEquals(99, map.get("a"));
        }

        @Test
        void computeIfAbsent_doesNotOverwrite() {
            map.put("a", 1);
            Integer result = map.computeIfAbsent("a", k -> {
                throw new AssertionError("mapping function must not run for present key");
            });
            assertEquals(1, result);
        }

        @Test
        void computeIfAbsent_skipsWhenMappingReturnsNull() {
            Integer result = map.computeIfAbsent("a", k -> null);
            assertNull(result);
            assertFalse(map.containsKey("a"));
        }

        @Test
        void computeIfPresent_updatesWhenPresent() {
            map.put("a", 10);
            Integer result = map.computeIfPresent("a", (k, v) -> v * 2);
            assertEquals(20, result);
            assertEquals(20, map.get("a"));
        }

        @Test
        void computeIfPresent_skipsWhenAbsent() {
            Integer result = map.computeIfPresent("missing", (k, v) -> {
                throw new AssertionError("remapping function must not run for absent key");
            });
            assertNull(result);
        }

        @Test
        void computeIfPresent_removesWhenFunctionReturnsNull() {
            map.put("a", 10);
            Integer result = map.computeIfPresent("a", (k, v) -> null);
            assertNull(result);
            assertFalse(map.containsKey("a"));
        }

        @Test
        void computeIfAbsent_runsMappingExactlyOncePerKeyUnderContention() throws Exception {
            int threadCount = 16;
            AtomicInteger invocations = new AtomicInteger();
            @Cleanup ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        map.computeIfAbsent("shared", k -> {
                            invocations.incrementAndGet();
                            // Hold the write lock briefly so any non-atomic impl would let
                            // a sibling thread slip in and double-invoke.
                            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                            return 1;
                        });
                    } catch (InterruptedException ignored) {
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            assertTrue(done.await(5, TimeUnit.SECONDS));
            pool.shutdown();
            assertEquals(1, invocations.get(), "computeIfAbsent must invoke mapping exactly once per key");
            assertEquals(1, map.get("shared"));
        }
    }

    @Nested
    class ConstructFromEntries {

        @Test
        void fromEntries() {
            ConcurrentMap<String, Integer> m = Concurrent.newMap(
                Map.entry("x", 10),
                Map.entry("y", 20)
            );
            assertEquals(2, m.size());
            assertEquals(10, m.get("x"));
            assertEquals(20, m.get("y"));
        }

        @Test
        void fromMap() {
            ConcurrentMap<String, Integer> m = Concurrent.newMap(Map.of("a", 1, "b", 2));
            assertEquals(2, m.size());
            assertTrue(m.containsKey("a"));
            assertTrue(m.containsKey("b"));
        }
    }

    @Nested
    class Views {

        @Test
        void keySet_reflectsEntries() {
            map.put("a", 1);
            map.put("b", 2);
            Set<String> keys = map.keySet();
            assertEquals(2, keys.size());
            assertTrue(keys.contains("a"));
            assertTrue(keys.contains("b"));
        }

        @Test
        void values_reflectsEntries() {
            map.put("a", 1);
            map.put("b", 2);
            Collection<Integer> values = map.values();
            assertEquals(2, values.size());
            assertTrue(values.contains(1));
            assertTrue(values.contains(2));
        }

        @Test
        void entrySet_reflectsEntries() {
            map.put("a", 1);
            Set<Map.Entry<String, Integer>> entries = map.entrySet();
            assertEquals(1, entries.size());
        }

        @Test
        void toUnmodifiableMap_rejectsModification() {
            map.put("a", 1);
            ConcurrentMap<String, Integer> unmod = map.toUnmodifiable();
            assertEquals(1, unmod.get("a"));
            assertThrows(UnsupportedOperationException.class, () -> unmod.put("b", 2));
        }
    }

    @Nested
    class ThreadSafety {

        @Test
        void concurrent_puts_noLostEntries() throws Exception {
            int threadCount = 8;
            int opsPerThread = 1000;
            @Cleanup ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                pool.submit(() -> {
                    for (int i = 0; i < opsPerThread; i++) {
                        // Each thread uses unique keys to avoid merge conflicts
                        map.put(threadId + "-" + i, i);
                    }
                    latch.countDown();
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            pool.shutdown();
            assertEquals(threadCount * opsPerThread, map.size());
        }

        @Test
        void concurrent_putAndGet_noException() throws Exception {
            int threadCount = 8;
            @Cleanup ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            AtomicInteger errors = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int id = t;
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < 500; i++) {
                            if (id % 2 == 0) {
                                map.put("key" + i, i);
                            } else {
                                map.get("key" + i);
                            }
                        }
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                    latch.countDown();
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            pool.shutdown();
            assertEquals(0, errors.get());
        }

        @Test
        void concurrent_iterateAndModify_noException() throws Exception {
            for (int i = 0; i < 100; i++) map.put("k" + i, i);

            int threadCount = 4;
            @Cleanup ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            AtomicInteger errors = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int id = t;
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < 200; i++) {
                            if (id % 2 == 0) {
                                map.put("new" + i, i);
                            } else {
                                for (Map.Entry<String, Integer> ignored : map.entrySet()) { /* snapshot */ }
                            }
                        }
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                    latch.countDown();
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            pool.shutdown();
            assertEquals(0, errors.get());
        }
    }

    @Nested
    class StreamCollectors {

        @Test
        void toMap_fromEntries() {
            ConcurrentMap<String, Integer> result = Map.of("a", 1, "b", 2)
                .entrySet()
                .stream()
                .collect(Concurrent.toMap());
            assertEquals(2, result.size());
            assertEquals(1, result.get("a"));
        }

        @Test
        void toMap_withKeyValueMappers() {
            ConcurrentMap<String, Integer> result = java.util.List.of("alpha", "beta", "gamma")
                .stream()
                .collect(Concurrent.toMap(s -> s.substring(0, 1), String::length));
            assertEquals(3, result.size());
            assertEquals(5, result.get("a"));
        }
    }
}
