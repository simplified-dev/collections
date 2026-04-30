package dev.simplified.collection;

import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark suite for {@link ConcurrentTreeSet}, comparing throughput against {@link TreeSet},
 * {@code Collections.synchronizedSortedSet}, and {@link ConcurrentSkipListSet}.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class ConcurrentTreeSetBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentTreeSet<Integer> concurrentTreeSet;
    private TreeSet<Integer> treeSet;
    private SortedSet<Integer> syncSortedSet;
    private ConcurrentSkipListSet<Integer> skipListSet;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentTreeSet = new ConcurrentTreeSet<>();
        treeSet = new TreeSet<>();
        syncSortedSet = Collections.synchronizedSortedSet(new TreeSet<>());
        skipListSet = new ConcurrentSkipListSet<>();

        for (int i = 0; i < size; i++) {
            concurrentTreeSet.add(i);
            treeSet.add(i);
            syncSortedSet.add(i);
            skipListSet.add(i);
        }
    }

    // --- Write: add ---

    @Benchmark
    public boolean concurrentTreeSet_add() {
        return concurrentTreeSet.add(size + 1);
    }

    @Benchmark
    public boolean treeSet_add() {
        return treeSet.add(size + 1);
    }

    @Benchmark
    public boolean syncSortedSet_add() {
        return syncSortedSet.add(size + 1);
    }

    @Benchmark
    public boolean skipListSet_add() {
        return skipListSet.add(size + 1);
    }

    // --- Read: contains ---

    @Benchmark
    public boolean concurrentTreeSet_contains() {
        return concurrentTreeSet.contains(size / 2);
    }

    @Benchmark
    public boolean treeSet_contains() {
        return treeSet.contains(size / 2);
    }

    @Benchmark
    public boolean syncSortedSet_contains() {
        return syncSortedSet.contains(size / 2);
    }

    @Benchmark
    public boolean skipListSet_contains() {
        return skipListSet.contains(size / 2);
    }

    // --- Read: iteration ---

    @Benchmark
    public int concurrentTreeSet_iterate() {
        int sum = 0;
        for (int i : concurrentTreeSet) sum += i;
        return sum;
    }

    @Benchmark
    public int treeSet_iterate() {
        int sum = 0;
        for (int i : treeSet) sum += i;
        return sum;
    }

    @Benchmark
    public int syncSortedSet_iterate() {
        int sum = 0;
        synchronized (syncSortedSet) {
            for (int i : syncSortedSet) sum += i;
        }
        return sum;
    }

    @Benchmark
    public int skipListSet_iterate() {
        int sum = 0;
        for (int i : skipListSet) sum += i;
        return sum;
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentTreeSet_size() {
        return concurrentTreeSet.size();
    }

    @Benchmark
    public int treeSet_size() {
        return treeSet.size();
    }

    @Benchmark
    public int syncSortedSet_size() {
        return syncSortedSet.size();
    }

    @Benchmark
    public int skipListSet_size() {
        return skipListSet.size();
    }

    // --- Write: remove ---

    @Benchmark
    public boolean concurrentTreeSet_remove() {
        return concurrentTreeSet.remove(size / 2);
    }

    @Benchmark
    public boolean treeSet_remove() {
        return treeSet.remove(size / 2);
    }

    @Benchmark
    public boolean syncSortedSet_remove() {
        return syncSortedSet.remove(size / 2);
    }

    @Benchmark
    public boolean skipListSet_remove() {
        return skipListSet.remove(size / 2);
    }

    // --- Sorted: first ---

    @Benchmark
    public int concurrentTreeSet_first() {
        return concurrentTreeSet.first();
    }

    @Benchmark
    public int treeSet_first() {
        return treeSet.first();
    }

    @Benchmark
    public int syncSortedSet_first() {
        return syncSortedSet.first();
    }

    @Benchmark
    public int skipListSet_first() {
        return skipListSet.first();
    }

    // --- Sorted: last ---

    @Benchmark
    public int concurrentTreeSet_last() {
        return concurrentTreeSet.last();
    }

    @Benchmark
    public int treeSet_last() {
        return treeSet.last();
    }

    @Benchmark
    public int syncSortedSet_last() {
        return syncSortedSet.last();
    }

    @Benchmark
    public int skipListSet_last() {
        return skipListSet.last();
    }

    // --- Sorted: headSet ---

    @Benchmark
    public int concurrentTreeSet_headSet() {
        return concurrentTreeSet.headSet(size / 2).size();
    }

    @Benchmark
    public int treeSet_headSet() {
        return treeSet.headSet(size / 2).size();
    }

    @Benchmark
    public int skipListSet_headSet() {
        return skipListSet.headSet(size / 2).size();
    }

    // --- Sorted: tailSet ---

    @Benchmark
    public int concurrentTreeSet_tailSet() {
        return concurrentTreeSet.tailSet(size / 2).size();
    }

    @Benchmark
    public int treeSet_tailSet() {
        return treeSet.tailSet(size / 2).size();
    }

    @Benchmark
    public int skipListSet_tailSet() {
        return skipListSet.tailSet(size / 2).size();
    }
}
