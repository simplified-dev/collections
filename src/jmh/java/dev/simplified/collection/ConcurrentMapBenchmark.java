package dev.simplified.collection;

import dev.simplified.collection.ConcurrentMap;

import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class ConcurrentMapBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentMap<Integer, Integer> concurrentMap;
    private HashMap<Integer, Integer> hashMap;
    private ConcurrentHashMap<Integer, Integer> jdkConcurrentMap;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentMap = Concurrent.newMap();
        hashMap = new HashMap<>();
        jdkConcurrentMap = new ConcurrentHashMap<>();

        for (int i = 0; i < size; i++) {
            concurrentMap.put(i, i);
            hashMap.put(i, i);
            jdkConcurrentMap.put(i, i);
        }
    }

    // --- Read: get ---

    @Benchmark
    public Integer concurrentMap_get() {
        return concurrentMap.get(size / 2);
    }

    @Benchmark
    public Integer hashMap_get() {
        return hashMap.get(size / 2);
    }

    @Benchmark
    public Integer jdkConcurrentMap_get() {
        return jdkConcurrentMap.get(size / 2);
    }

    // --- Write: put ---

    @Benchmark
    public Integer concurrentMap_put() {
        return concurrentMap.put(size + 1, 42);
    }

    @Benchmark
    public Integer hashMap_put() {
        return hashMap.put(size + 1, 42);
    }

    @Benchmark
    public Integer jdkConcurrentMap_put() {
        return jdkConcurrentMap.put(size + 1, 42);
    }

    // --- Read: containsKey ---

    @Benchmark
    public boolean concurrentMap_containsKey() {
        return concurrentMap.containsKey(size / 2);
    }

    @Benchmark
    public boolean hashMap_containsKey() {
        return hashMap.containsKey(size / 2);
    }

    @Benchmark
    public boolean jdkConcurrentMap_containsKey() {
        return jdkConcurrentMap.containsKey(size / 2);
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentMap_size() {
        return concurrentMap.size();
    }

    @Benchmark
    public int hashMap_size() {
        return hashMap.size();
    }

    @Benchmark
    public int jdkConcurrentMap_size() {
        return jdkConcurrentMap.size();
    }
}
