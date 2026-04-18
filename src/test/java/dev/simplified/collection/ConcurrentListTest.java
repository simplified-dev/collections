package dev.simplified.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentListTest {

    private ConcurrentList<String> list;

    @BeforeEach
    void setup() {
        list = Concurrent.newList();
    }

    @Nested
    class BasicOperations {

        @Test
        void add_and_get() {
            list.add("a");
            list.add("b");
            assertEquals(2, list.size());
            assertEquals("a", list.get(0));
            assertEquals("b", list.get(1));
        }

        @Test
        void add_atIndex() {
            list.addAll(List.of("a", "c"));
            list.add(1, "b");
            assertEquals(List.of("a", "b", "c"), list);
        }

        @Test
        void addFirst_and_addLast() {
            list.add("b");
            list.addFirst("a");
            list.addLast("c");
            assertEquals("a", list.getFirst());
            assertEquals("c", list.getLast());
            assertEquals(3, list.size());
        }

        @Test
        void set_replacesElement() {
            list.addAll(List.of("a", "b", "c"));
            list.set(1, "x");
            assertEquals("x", list.get(1));
        }

        @Test
        void remove_byIndex() {
            list.addAll(List.of("a", "b", "c"));
            String removed = list.remove(1);
            assertEquals("b", removed);
            assertEquals(List.of("a", "c"), list);
        }

        @Test
        void remove_byObject() {
            list.addAll(List.of("a", "b", "c"));
            assertTrue(list.remove("b"));
            assertFalse(list.contains("b"));
        }

        @Test
        void removeFirst_and_removeLast() {
            list.addAll(List.of("a", "b", "c"));
            assertEquals("a", list.removeFirst());
            assertEquals("c", list.removeLast());
            assertEquals(1, list.size());
            assertEquals("b", list.getFirst());
        }

        @Test
        void contains_ok() {
            list.add("x");
            assertTrue(list.contains("x"));
            assertFalse(list.contains("y"));
        }

        @Test
        void clear_emptiesList() {
            list.addAll(List.of("a", "b"));
            list.clear();
            assertTrue(list.isEmpty());
            assertEquals(0, list.size());
        }

        @Test
        void indexOf_and_lastIndexOf() {
            list.addAll(List.of("a", "b", "a"));
            assertEquals(0, list.indexOf("a"));
            assertEquals(2, list.lastIndexOf("a"));
            assertEquals(-1, list.indexOf("z"));
        }
    }

    @Nested
    class FindOperations {

        @Test
        void findFirst_present() {
            list.add("x");
            Optional<String> result = list.findFirst();
            assertTrue(result.isPresent());
            assertEquals("x", result.get());
        }

        @Test
        void findFirst_empty() {
            assertTrue(list.findFirst().isEmpty());
        }

        @Test
        void findLast_present() {
            list.addAll(List.of("a", "b"));
            Optional<String> result = list.findLast();
            assertTrue(result.isPresent());
            assertEquals("b", result.get());
        }

        @Test
        void getFirst_throwsOnEmpty() {
            assertThrows(NoSuchElementException.class, () -> list.getFirst());
        }

        @Test
        void getLast_throwsOnEmpty() {
            assertThrows(NoSuchElementException.class, () -> list.getLast());
        }

        @Test
        void getOrDefault_inBounds() {
            list.add("a");
            assertEquals("a", list.getOrDefault(0, "fallback"));
        }

        @Test
        void getOrDefault_outOfBounds() {
            assertEquals("fallback", list.getOrDefault(5, "fallback"));
        }
    }

    @Nested
    class DerivedLists {

        @Test
        void reversed_returnsNewList() {
            list.addAll(List.of("a", "b", "c"));
            ConcurrentList<String> reversed = list.reversed();
            assertEquals(List.of("c", "b", "a"), reversed);
            assertEquals(List.of("a", "b", "c"), list); // original unchanged
        }

        @Test
        void subList_returnsSnapshot() {
            list.addAll(List.of("a", "b", "c", "d"));
            ConcurrentList<String> sub = list.subList(1, 3);
            assertEquals(List.of("b", "c"), sub);
        }

        @Test
        void sorted_withComparator() {
            list.addAll(List.of("c", "a", "b"));
            ConcurrentList<String> sorted = list.sorted(String::compareTo);
            assertEquals(List.of("a", "b", "c"), sorted);
            assertEquals(List.of("c", "a", "b"), list); // original unchanged
        }

        @Test
        void toUnmodifiableList_rejectsModification() {
            list.addAll(List.of("a", "b"));
            ConcurrentList<String> unmod = list.toUnmodifiable();
            assertEquals(2, unmod.size());
            assertThrows(UnsupportedOperationException.class, () -> unmod.add("c"));
        }
    }

    @Nested
    class ThreadSafety {

        @Test
        void concurrent_adds_noLostElements() throws Exception {
            int threadCount = 8;
            int opsPerThread = 1000;
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                pool.submit(() -> {
                    for (int i = 0; i < opsPerThread; i++) {
                        list.add("v");
                    }
                    latch.countDown();
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            pool.shutdown();
            assertEquals(threadCount * opsPerThread, list.size());
        }

        @Test
        void concurrent_addAndIterate_noConcurrentModificationException() throws Exception {
            list.addAll(List.of("a", "b", "c"));
            int threadCount = 4;
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            AtomicInteger errors = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int id = t;
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < 500; i++) {
                            if (id % 2 == 0) {
                                list.add("x" + i);
                            } else {
                                for (String ignored : list) { /* snapshot iteration */ }
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
        void concurrent_readAndWrite_noConcurrentModificationException() throws Exception {
            int threadCount = 8;
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            AtomicInteger errors = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // Pre-populate
            for (int i = 0; i < 100; i++) list.add("item" + i);

            for (int t = 0; t < threadCount; t++) {
                final int id = t;
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < 200; i++) {
                            switch (id % 4) {
                                case 0 -> list.add("new" + i);
                                case 1 -> list.size();
                                case 2 -> {
                                    if (!list.isEmpty()) list.get(0);
                                }
                                case 3 -> {
                                    for (String ignored : list) { /* snapshot */ }
                                }
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
        void toList_collector() {
            ConcurrentList<Integer> result = IntStream.rangeClosed(1, 5)
                .boxed()
                .collect(Concurrent.toList());
            assertEquals(5, result.size());
            assertTrue(result.contains(3));
        }

        @Test
        void newUnmodifiableList_rejectsModification() {
            ConcurrentList<Integer> source = IntStream.rangeClosed(1, 3)
                .boxed()
                .collect(Concurrent.toList());
            ConcurrentList<Integer> unmod = Concurrent.newUnmodifiableList(source);
            assertEquals(3, unmod.size());
            assertThrows(UnsupportedOperationException.class, () -> unmod.add(4));
        }
    }
}
