package dev.simplified.collection;

import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark suite for {@link ConcurrentLinkedHashSet}, comparing throughput against
 * {@link LinkedHashSet} and {@code Collections.synchronizedSet(new LinkedHashSet<>())}.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class ConcurrentLinkedHashSetBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentLinkedHashSet<Integer> concurrentLinkedHashSet;
    private LinkedHashSet<Integer> linkedHashSet;
    private Set<Integer> syncLinkedHashSet;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentLinkedHashSet = new ConcurrentLinkedHashSet<>();
        linkedHashSet = new LinkedHashSet<>();
        syncLinkedHashSet = Collections.synchronizedSet(new LinkedHashSet<>());

        for (int i = 0; i < size; i++) {
            concurrentLinkedHashSet.add(i);
            linkedHashSet.add(i);
            syncLinkedHashSet.add(i);
        }
    }

    // --- Write: add ---

    @Benchmark
    public boolean concurrentLinkedHashSet_add() {
        return concurrentLinkedHashSet.add(size + 1);
    }

    @Benchmark
    public boolean linkedHashSet_add() {
        return linkedHashSet.add(size + 1);
    }

    @Benchmark
    public boolean syncLinkedHashSet_add() {
        return syncLinkedHashSet.add(size + 1);
    }

    // --- Read: contains ---

    @Benchmark
    public boolean concurrentLinkedHashSet_contains() {
        return concurrentLinkedHashSet.contains(size / 2);
    }

    @Benchmark
    public boolean linkedHashSet_contains() {
        return linkedHashSet.contains(size / 2);
    }

    @Benchmark
    public boolean syncLinkedHashSet_contains() {
        return syncLinkedHashSet.contains(size / 2);
    }

    // --- Read: iteration ---

    @Benchmark
    public int concurrentLinkedHashSet_iterate() {
        int sum = 0;
        for (int i : concurrentLinkedHashSet) sum += i;
        return sum;
    }

    @Benchmark
    public int linkedHashSet_iterate() {
        int sum = 0;
        for (int i : linkedHashSet) sum += i;
        return sum;
    }

    @Benchmark
    public int syncLinkedHashSet_iterate() {
        int sum = 0;
        synchronized (syncLinkedHashSet) {
            for (int i : syncLinkedHashSet) sum += i;
        }
        return sum;
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentLinkedHashSet_size() {
        return concurrentLinkedHashSet.size();
    }

    @Benchmark
    public int linkedHashSet_size() {
        return linkedHashSet.size();
    }

    @Benchmark
    public int syncLinkedHashSet_size() {
        return syncLinkedHashSet.size();
    }

    // --- Write: remove ---

    @Benchmark
    public boolean concurrentLinkedHashSet_remove() {
        return concurrentLinkedHashSet.remove(size / 2);
    }

    @Benchmark
    public boolean linkedHashSet_remove() {
        return linkedHashSet.remove(size / 2);
    }

    @Benchmark
    public boolean syncLinkedHashSet_remove() {
        return syncLinkedHashSet.remove(size / 2);
    }
}
