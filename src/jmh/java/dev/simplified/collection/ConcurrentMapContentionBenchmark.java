package dev.simplified.collection;

import dev.simplified.collection.ConcurrentMap;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(8)
@State(Scope.Benchmark)
public class ConcurrentMapContentionBenchmark {

    private ConcurrentMap<Integer, Integer> concurrentMap;
    private ConcurrentHashMap<Integer, Integer> jdkConcurrentMap;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentMap = Concurrent.newMap();
        jdkConcurrentMap = new ConcurrentHashMap<>();

        for (int i = 0; i < 1000; i++) {
            concurrentMap.put(i, i);
            jdkConcurrentMap.put(i, i);
        }
    }

    @Benchmark
    public Integer concurrentMap_read() {
        return concurrentMap.get(500);
    }

    @Benchmark
    public Integer jdkConcurrentMap_read() {
        return jdkConcurrentMap.get(500);
    }

    @Benchmark
    public Integer concurrentMap_write() {
        return concurrentMap.put(1001, 42);
    }

    @Benchmark
    public Integer jdkConcurrentMap_write() {
        return jdkConcurrentMap.put(1001, 42);
    }
}
