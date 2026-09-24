package dev.simplified.collection.query;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.ConcurrentSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins that an index never outlives the elements it describes.
 *
 * <p>Every mutator gets its own case, because the index is dropped by one shared hook and a mutator
 * that bypassed it would answer from elements the collection no longer holds - a wrong answer
 * rather than a slow one.
 */
class IndexInvalidationTest {

    /**
     * Fixture whose key is indexed and whose id keeps two rows with one key distinguishable.
     */
    record Row(@Indexed String key, int id) {}

    private static final SearchFunction<Row, String> BY_KEY = Row::key;

    private static final Row ALPHA = new Row("alpha", 1);
    private static final Row BETA = new Row("beta", 2);
    private static final Row GAMMA = new Row("gamma", 3);

    private ConcurrentList<Row> rows;

    @BeforeEach
    void setup() {
        this.rows = Concurrent.newList();
        this.rows.add(ALPHA);
        this.rows.add(BETA);
    }

    /**
     * Builds the index, so anything asserted afterwards is asserted against a warm cache rather
     * than a cold one that would be correct by accident.
     */
    private void warm() {
        assertEquals(List.of(ALPHA), this.rows.findAll(BY_KEY, "alpha").toList());
        assertFalse(this.rows.indexes().isEmpty());
    }

    private List<Row> byKey(String key) {
        return this.rows.findAll(BY_KEY, key).toList();
    }

    @Nested
    class EveryMutatorDropsIt {

        @Test
        void add_isSeenByTheNextQuery() {
            warm();
            rows.add(new Row("alpha", 9));
            assertEquals(List.of(ALPHA, new Row("alpha", 9)), byKey("alpha"));
        }

        @Test
        void addAtIndex_isSeenByTheNextQuery() {
            warm();
            rows.add(0, new Row("alpha", 9));
            assertEquals(List.of(new Row("alpha", 9), ALPHA), byKey("alpha"));
        }

        @Test
        void addAll_isSeenByTheNextQuery() {
            warm();
            rows.addAll(List.of(GAMMA, new Row("alpha", 9)));
            assertEquals(List.of(ALPHA, new Row("alpha", 9)), byKey("alpha"));
            assertEquals(List.of(GAMMA), byKey("gamma"));
        }

        @Test
        void addAllVarargs_isSeenByTheNextQuery() {
            warm();
            rows.addAll(GAMMA);
            assertEquals(List.of(GAMMA), byKey("gamma"));
        }

        @Test
        void addFirst_isSeenByTheNextQuery() {
            warm();
            rows.addFirst(new Row("alpha", 9));
            assertEquals(List.of(new Row("alpha", 9), ALPHA), byKey("alpha"));
        }

        @Test
        void addLast_isSeenByTheNextQuery() {
            warm();
            rows.addLast(new Row("alpha", 9));
            assertEquals(List.of(ALPHA, new Row("alpha", 9)), byKey("alpha"));
        }

        @Test
        void addIf_isSeenByTheNextQuery() {
            warm();
            rows.addIf(() -> true, new Row("alpha", 9));
            assertEquals(List.of(ALPHA, new Row("alpha", 9)), byKey("alpha"));
        }

        @Test
        void remove_isSeenByTheNextQuery() {
            warm();
            rows.remove(ALPHA);
            assertEquals(List.of(), byKey("alpha"));
        }

        @Test
        void removeAtIndex_isSeenByTheNextQuery() {
            warm();
            rows.remove(0);
            assertEquals(List.of(), byKey("alpha"));
        }

        @Test
        void removeFirst_isSeenByTheNextQuery() {
            warm();
            rows.removeFirst();
            assertEquals(List.of(), byKey("alpha"));
        }

        @Test
        void removeLast_isSeenByTheNextQuery() {
            warm();
            rows.removeLast();
            assertEquals(List.of(), byKey("beta"));
        }

        @Test
        void removeAll_isSeenByTheNextQuery() {
            warm();
            rows.removeAll(List.of(ALPHA));
            assertEquals(List.of(), byKey("alpha"));
        }

        @Test
        void retainAll_isSeenByTheNextQuery() {
            warm();
            rows.retainAll(List.of(BETA));
            assertEquals(List.of(), byKey("alpha"));
            assertEquals(List.of(BETA), byKey("beta"));
        }

        @Test
        void removeIf_isSeenByTheNextQuery() {
            warm();
            rows.removeIf(row -> "alpha".equals(row.key()));
            assertEquals(List.of(), byKey("alpha"));
        }

        @Test
        void clear_isSeenByTheNextQuery() {
            warm();
            rows.clear();
            assertEquals(List.of(), byKey("alpha"));
        }

        @Test
        void replace_isSeenByTheNextQuery() {
            warm();
            rows.replace(ALPHA, GAMMA);
            assertEquals(List.of(), byKey("alpha"));
            assertEquals(List.of(GAMMA), byKey("gamma"));
        }

        @Test
        void set_isSeenByTheNextQuery() {
            warm();
            rows.set(0, GAMMA);
            assertEquals(List.of(), byKey("alpha"));
            assertEquals(List.of(GAMMA), byKey("gamma"));
        }

        @Test
        void sort_isSeenByTheNextQuery() {
            rows.add(new Row("alpha", 9));
            assertEquals(List.of(ALPHA, new Row("alpha", 9)), byKey("alpha"));

            rows.sort(Comparator.comparingInt(Row::id).reversed());

            // Order changed, so the index has to answer in the new source order.
            assertEquals(List.of(new Row("alpha", 9), ALPHA), byKey("alpha"));
        }

        @Test
        void iteratorRemove_isSeenByTheNextQuery() {
            warm();
            Iterator<Row> iterator = rows.iterator();
            iterator.next();
            iterator.remove();

            assertEquals(List.of(), byKey("alpha"));
        }

    }

    @Nested
    class OtherCollections {

        @Test
        void set_isIndexedAndInvalidated() {
            ConcurrentSet<Row> set = Concurrent.newSet();
            set.add(ALPHA);
            set.add(BETA);

            assertEquals(List.of(ALPHA), set.findAll(BY_KEY, "alpha").toList());
            set.remove(ALPHA);
            assertEquals(List.of(), set.findAll(BY_KEY, "alpha").toList());
        }

        @Test
        void unmodifiableSnapshot_keepsItsIndexForGood() {
            rows.add(GAMMA);
            ConcurrentList<Row> frozen = rows.toUnmodifiable();

            assertEquals(List.of(GAMMA), frozen.findAll(BY_KEY, "gamma").toList());
            IndexCache<Row> held = frozen.indexes();

            // Nothing can mutate it, so the index built on the first query is the one it keeps.
            rows.clear();
            assertEquals(List.of(GAMMA), frozen.findAll(BY_KEY, "gamma").toList());
            assertSame(held, frozen.indexes());
        }

    }

    @Nested
    class UnderContention {

        @Test
        void findFirst_neverAnswersWithAnElementThatDoesNotMatch() throws Exception {
            ConcurrentList<Row> shared = Concurrent.newList();

            for (int id = 0; id < 200; id++)
                shared.add(new Row(id % 2 == 0 ? "even" : "odd", id));

            AtomicReference<Throwable> failure = new AtomicReference<>();
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService pool = Executors.newFixedThreadPool(8);

            try {
                for (int worker = 0; worker < 4; worker++) {
                    int seed = worker;

                    pool.submit(() -> {
                        try {
                            start.await();

                            for (int round = 0; round < 250; round++) {
                                shared.add(new Row("even", 1000 + seed * 250 + round));
                                shared.remove(new Row("even", 1000 + seed * 250 + round));
                            }
                        } catch (Throwable thrown) {
                            failure.compareAndSet(null, thrown);
                        }
                    });
                }

                for (int worker = 0; worker < 4; worker++) {
                    pool.submit(() -> {
                        try {
                            start.await();

                            for (int round = 0; round < 250; round++) {
                                Optional<Row> found = shared.findFirst(BY_KEY, "odd");

                                // Whatever generation answered, it has to answer with a row that
                                // really carries the key that was asked for.
                                if (found.isEmpty() || !"odd".equals(found.orElseThrow().key()))
                                    throw new AssertionError("A lookup answered with " + found);
                            }
                        } catch (Throwable thrown) {
                            failure.compareAndSet(null, thrown);
                        }
                    });
                }

                start.countDown();
                pool.shutdown();
                assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
            } finally {
                pool.shutdownNow();
            }

            assertNull(failure.get(), () -> String.valueOf(failure.get()));
        }

    }

}
