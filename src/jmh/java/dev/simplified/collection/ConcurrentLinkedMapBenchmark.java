package dev.simplified.collection;

import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark suite for {@link ConcurrentLinkedMap}, comparing throughput against
 * {@link LinkedHashMap} and {@code Collections.synchronizedMap(new LinkedHashMap<>())}.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class ConcurrentLinkedMapBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentLinkedMap<Integer, Integer> concurrentLinkedMap;
    private LinkedHashMap<Integer, Integer> linkedHashMap;
    private Map<Integer, Integer> syncLinkedHashMap;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentLinkedMap = new ConcurrentLinkedMap<>();
        linkedHashMap = new LinkedHashMap<>();
        syncLinkedHashMap = Collections.synchronizedMap(new LinkedHashMap<>());

        for (int i = 0; i < size; i++) {
            concurrentLinkedMap.put(i, i);
            linkedHashMap.put(i, i);
            syncLinkedHashMap.put(i, i);
        }
    }

    // --- Read: get ---

    @Benchmark
    public Integer concurrentLinkedHashMap_get() {
        return concurrentLinkedMap.get(size / 2);
    }

    @Benchmark
    public Integer linkedHashMap_get() {
        return linkedHashMap.get(size / 2);
    }

    @Benchmark
    public Integer syncLinkedHashMap_get() {
        return syncLinkedHashMap.get(size / 2);
    }

    // --- Write: put ---

    @Benchmark
    public Integer concurrentLinkedHashMap_put() {
        return concurrentLinkedMap.put(size + 1, 42);
    }

    @Benchmark
    public Integer linkedHashMap_put() {
        return linkedHashMap.put(size + 1, 42);
    }

    @Benchmark
    public Integer syncLinkedHashMap_put() {
        return syncLinkedHashMap.put(size + 1, 42);
    }

    // --- Read: containsKey ---

    @Benchmark
    public boolean concurrentLinkedHashMap_containsKey() {
        return concurrentLinkedMap.containsKey(size / 2);
    }

    @Benchmark
    public boolean linkedHashMap_containsKey() {
        return linkedHashMap.containsKey(size / 2);
    }

    @Benchmark
    public boolean syncLinkedHashMap_containsKey() {
        return syncLinkedHashMap.containsKey(size / 2);
    }

    // --- Read: iterate entries ---

    @Benchmark
    public int concurrentLinkedHashMap_iterate() {
        int sum = 0;
        for (Map.Entry<Integer, Integer> e : concurrentLinkedMap.entrySet()) sum += e.getValue();
        return sum;
    }

    @Benchmark
    public int linkedHashMap_iterate() {
        int sum = 0;
        for (Map.Entry<Integer, Integer> e : linkedHashMap.entrySet()) sum += e.getValue();
        return sum;
    }

    @Benchmark
    public int syncLinkedHashMap_iterate() {
        int sum = 0;
        synchronized (syncLinkedHashMap) {
            for (Map.Entry<Integer, Integer> e : syncLinkedHashMap.entrySet()) sum += e.getValue();
        }
        return sum;
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentLinkedHashMap_size() {
        return concurrentLinkedMap.size();
    }

    @Benchmark
    public int linkedHashMap_size() {
        return linkedHashMap.size();
    }

    @Benchmark
    public int syncLinkedHashMap_size() {
        return syncLinkedHashMap.size();
    }

    // --- Write: remove ---

    @Benchmark
    public Integer concurrentLinkedHashMap_remove() {
        return concurrentLinkedMap.remove(size / 2);
    }

    @Benchmark
    public Integer linkedHashMap_remove() {
        return linkedHashMap.remove(size / 2);
    }

    @Benchmark
    public Integer syncLinkedHashMap_remove() {
        return syncLinkedHashMap.remove(size / 2);
    }
}
