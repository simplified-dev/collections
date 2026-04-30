package dev.simplified.collection;

import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark suite for {@link ConcurrentHashSet}, comparing throughput against {@link HashSet},
 * {@code Collections.synchronizedSet}, and {@link ConcurrentHashMap#newKeySet()}.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class ConcurrentHashSetBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentHashSet<Integer> concurrentHashSet;
    private HashSet<Integer> hashSet;
    private Set<Integer> syncSet;
    private Set<Integer> jdkConcurrentKeySet;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentHashSet = new ConcurrentHashSet<>();
        hashSet = new HashSet<>();
        syncSet = Collections.synchronizedSet(new HashSet<>());
        jdkConcurrentKeySet = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < size; i++) {
            concurrentHashSet.add(i);
            hashSet.add(i);
            syncSet.add(i);
            jdkConcurrentKeySet.add(i);
        }
    }

    // --- Write: add ---

    @Benchmark
    public boolean concurrentHashSet_add() {
        return concurrentHashSet.add(size + 1);
    }

    @Benchmark
    public boolean hashSet_add() {
        return hashSet.add(size + 1);
    }

    @Benchmark
    public boolean syncSet_add() {
        return syncSet.add(size + 1);
    }

    @Benchmark
    public boolean jdkConcurrentKeySet_add() {
        return jdkConcurrentKeySet.add(size + 1);
    }

    // --- Read: contains ---

    @Benchmark
    public boolean concurrentHashSet_contains() {
        return concurrentHashSet.contains(size / 2);
    }

    @Benchmark
    public boolean hashSet_contains() {
        return hashSet.contains(size / 2);
    }

    @Benchmark
    public boolean syncSet_contains() {
        return syncSet.contains(size / 2);
    }

    @Benchmark
    public boolean jdkConcurrentKeySet_contains() {
        return jdkConcurrentKeySet.contains(size / 2);
    }

    // --- Read: iteration ---

    @Benchmark
    public int concurrentHashSet_iterate() {
        int sum = 0;
        for (int i : concurrentHashSet) sum += i;
        return sum;
    }

    @Benchmark
    public int hashSet_iterate() {
        int sum = 0;
        for (int i : hashSet) sum += i;
        return sum;
    }

    @Benchmark
    public int syncSet_iterate() {
        int sum = 0;
        synchronized (syncSet) {
            for (int i : syncSet) sum += i;
        }
        return sum;
    }

    @Benchmark
    public int jdkConcurrentKeySet_iterate() {
        int sum = 0;
        for (int i : jdkConcurrentKeySet) sum += i;
        return sum;
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentHashSet_size() {
        return concurrentHashSet.size();
    }

    @Benchmark
    public int hashSet_size() {
        return hashSet.size();
    }

    @Benchmark
    public int syncSet_size() {
        return syncSet.size();
    }

    @Benchmark
    public int jdkConcurrentKeySet_size() {
        return jdkConcurrentKeySet.size();
    }

    // --- Write: remove ---

    @Benchmark
    public boolean concurrentHashSet_remove() {
        return concurrentHashSet.remove(size / 2);
    }

    @Benchmark
    public boolean hashSet_remove() {
        return hashSet.remove(size / 2);
    }

    @Benchmark
    public boolean syncSet_remove() {
        return syncSet.remove(size / 2);
    }

    @Benchmark
    public boolean jdkConcurrentKeySet_remove() {
        return jdkConcurrentKeySet.remove(size / 2);
    }
}
