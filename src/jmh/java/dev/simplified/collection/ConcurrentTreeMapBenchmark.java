package dev.simplified.collection;

import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark suite for {@link ConcurrentTreeMap}, comparing throughput against {@link TreeMap},
 * {@code Collections.synchronizedSortedMap}, and {@link ConcurrentSkipListMap}.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class ConcurrentTreeMapBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentTreeMap<Integer, Integer> concurrentTreeMap;
    private TreeMap<Integer, Integer> treeMap;
    private SortedMap<Integer, Integer> syncSortedMap;
    private ConcurrentSkipListMap<Integer, Integer> skipListMap;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentTreeMap = new ConcurrentTreeMap<>();
        treeMap = new TreeMap<>();
        syncSortedMap = Collections.synchronizedSortedMap(new TreeMap<>());
        skipListMap = new ConcurrentSkipListMap<>();

        for (int i = 0; i < size; i++) {
            concurrentTreeMap.put(i, i);
            treeMap.put(i, i);
            syncSortedMap.put(i, i);
            skipListMap.put(i, i);
        }
    }

    // --- Read: get ---

    @Benchmark
    public Integer concurrentTreeMap_get() {
        return concurrentTreeMap.get(size / 2);
    }

    @Benchmark
    public Integer treeMap_get() {
        return treeMap.get(size / 2);
    }

    @Benchmark
    public Integer syncSortedMap_get() {
        return syncSortedMap.get(size / 2);
    }

    @Benchmark
    public Integer skipListMap_get() {
        return skipListMap.get(size / 2);
    }

    // --- Write: put ---

    @Benchmark
    public Integer concurrentTreeMap_put() {
        return concurrentTreeMap.put(size + 1, 42);
    }

    @Benchmark
    public Integer treeMap_put() {
        return treeMap.put(size + 1, 42);
    }

    @Benchmark
    public Integer syncSortedMap_put() {
        return syncSortedMap.put(size + 1, 42);
    }

    @Benchmark
    public Integer skipListMap_put() {
        return skipListMap.put(size + 1, 42);
    }

    // --- Read: containsKey ---

    @Benchmark
    public boolean concurrentTreeMap_containsKey() {
        return concurrentTreeMap.containsKey(size / 2);
    }

    @Benchmark
    public boolean treeMap_containsKey() {
        return treeMap.containsKey(size / 2);
    }

    @Benchmark
    public boolean syncSortedMap_containsKey() {
        return syncSortedMap.containsKey(size / 2);
    }

    @Benchmark
    public boolean skipListMap_containsKey() {
        return skipListMap.containsKey(size / 2);
    }

    // --- Read: iterate entries ---

    @Benchmark
    public int concurrentTreeMap_iterate() {
        int sum = 0;
        for (Map.Entry<Integer, Integer> e : concurrentTreeMap.entrySet()) sum += e.getValue();
        return sum;
    }

    @Benchmark
    public int treeMap_iterate() {
        int sum = 0;
        for (Map.Entry<Integer, Integer> e : treeMap.entrySet()) sum += e.getValue();
        return sum;
    }

    @Benchmark
    public int syncSortedMap_iterate() {
        int sum = 0;
        synchronized (syncSortedMap) {
            for (Map.Entry<Integer, Integer> e : syncSortedMap.entrySet()) sum += e.getValue();
        }
        return sum;
    }

    @Benchmark
    public int skipListMap_iterate() {
        int sum = 0;
        for (Map.Entry<Integer, Integer> e : skipListMap.entrySet()) sum += e.getValue();
        return sum;
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentTreeMap_size() {
        return concurrentTreeMap.size();
    }

    @Benchmark
    public int treeMap_size() {
        return treeMap.size();
    }

    @Benchmark
    public int syncSortedMap_size() {
        return syncSortedMap.size();
    }

    @Benchmark
    public int skipListMap_size() {
        return skipListMap.size();
    }

    // --- Write: remove ---

    @Benchmark
    public Integer concurrentTreeMap_remove() {
        return concurrentTreeMap.remove(size / 2);
    }

    @Benchmark
    public Integer treeMap_remove() {
        return treeMap.remove(size / 2);
    }

    @Benchmark
    public Integer syncSortedMap_remove() {
        return syncSortedMap.remove(size / 2);
    }

    @Benchmark
    public Integer skipListMap_remove() {
        return skipListMap.remove(size / 2);
    }

    // --- Sorted: firstKey ---

    @Benchmark
    public Integer concurrentTreeMap_firstKey() {
        return concurrentTreeMap.firstKey();
    }

    @Benchmark
    public Integer treeMap_firstKey() {
        return treeMap.firstKey();
    }

    @Benchmark
    public Integer syncSortedMap_firstKey() {
        return syncSortedMap.firstKey();
    }

    @Benchmark
    public Integer skipListMap_firstKey() {
        return skipListMap.firstKey();
    }

    // --- Sorted: lastKey ---

    @Benchmark
    public Integer concurrentTreeMap_lastKey() {
        return concurrentTreeMap.lastKey();
    }

    @Benchmark
    public Integer treeMap_lastKey() {
        return treeMap.lastKey();
    }

    @Benchmark
    public Integer syncSortedMap_lastKey() {
        return syncSortedMap.lastKey();
    }

    @Benchmark
    public Integer skipListMap_lastKey() {
        return skipListMap.lastKey();
    }

    // --- Sorted: headMap ---

    @Benchmark
    public int concurrentTreeMap_headMap() {
        return concurrentTreeMap.headMap(size / 2).size();
    }

    @Benchmark
    public int treeMap_headMap() {
        return treeMap.headMap(size / 2).size();
    }

    @Benchmark
    public int skipListMap_headMap() {
        return skipListMap.headMap(size / 2).size();
    }

    // --- Sorted: tailMap ---

    @Benchmark
    public int concurrentTreeMap_tailMap() {
        return concurrentTreeMap.tailMap(size / 2).size();
    }

    @Benchmark
    public int treeMap_tailMap() {
        return treeMap.tailMap(size / 2).size();
    }

    @Benchmark
    public int skipListMap_tailMap() {
        return skipListMap.tailMap(size / 2).size();
    }
}
