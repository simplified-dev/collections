package dev.simplified.collection;

import lombok.Cleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentSetTest {

    private ConcurrentSet<String> set;

    @BeforeEach
    void setup() {
        set = Concurrent.newSet();
    }

    @Nested
    class BasicOperations {

        @Test
        void add_and_contains() {
            assertTrue(set.add("a"));
            assertTrue(set.contains("a"));
            assertFalse(set.contains("b"));
        }

        @Test
        void add_rejectsDuplicates() {
            set.add("a");
            assertFalse(set.add("a"));
            assertEquals(1, set.size());
        }

        @Test
        void remove_ok() {
            set.add("a");
            assertTrue(set.remove("a"));
            assertFalse(set.contains("a"));
            assertEquals(0, set.size());
        }

        @Test
        void remove_absent() {
            assertFalse(set.remove("nonexistent"));
        }

        @Test
        void addAll_addsMultiple() {
            set.addAll(List.of("a", "b", "c"));
            assertEquals(3, set.size());
            assertTrue(set.contains("b"));
        }

        @Test
        void clear_emptiesSet() {
            set.addAll(List.of("a", "b"));
            set.clear();
            assertTrue(set.isEmpty());
        }

        @Test
        void constructFromArray() {
            ConcurrentSet<String> s = Concurrent.newSet("x", "y", "z");
            assertEquals(3, s.size());
            assertTrue(s.contains("y"));
        }

        @Test
        void constructFromCollection() {
            ConcurrentSet<String> s = Concurrent.newSet(List.of("a", "b"));
            assertEquals(2, s.size());
        }
    }

    @Nested
    class Unmodifiable {

        @Test
        void toUnmodifiableSet_rejectsModification() {
            set.addAll(List.of("a", "b"));
            ConcurrentSet<String> unmod = set.toUnmodifiable();
            assertEquals(2, unmod.size());
            assertThrows(UnsupportedOperationException.class, () -> unmod.add("c"));
        }

        @Test
        void toUnmodifiableSet_rejectsClear() {
            set.add("a");
            ConcurrentSet<String> unmod = set.toUnmodifiable();
            assertThrows(UnsupportedOperationException.class, unmod::clear);
        }
    }

    @Nested
    class ThreadSafety {

        @Test
        void concurrent_adds_uniqueKeysPreserved() throws Exception {
            int threadCount = 8;
            int opsPerThread = 500;
            @Cleanup ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                pool.submit(() -> {
                    for (int i = 0; i < opsPerThread; i++) {
                        set.add(threadId + "-" + i);
                    }
                    latch.countDown();
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            pool.shutdown();
            assertEquals(threadCount * opsPerThread, set.size());
        }

        @Test
        void concurrent_addAndIterate_noException() throws Exception {
            set.addAll(List.of("a", "b", "c"));
            int threadCount = 4;
            @Cleanup ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            AtomicInteger errors = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int id = t;
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < 300; i++) {
                            if (id % 2 == 0) {
                                set.add("item-" + id + "-" + i);
                            } else {
                                for (String ignored : set) { /* snapshot iteration */ }
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
        void toSet_collector() {
            ConcurrentSet<Integer> result = IntStream.rangeClosed(1, 5)
                .boxed()
                .collect(Concurrent.toSet());
            assertEquals(5, result.size());
            assertTrue(result.contains(3));
        }

        @Test
        void toSet_deduplicates() {
            ConcurrentSet<Integer> result = List.of(1, 2, 2, 3, 3, 3)
                .stream()
                .collect(Concurrent.toSet());
            assertEquals(3, result.size());
        }
    }
}
