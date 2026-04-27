package dev.simplified.collection;

import dev.simplified.collection.ConcurrentList;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(8)
@State(Scope.Benchmark)
public class ConcurrentListContentionBenchmark {

    private ConcurrentList<Integer> concurrentList;
    private CopyOnWriteArrayList<Integer> cowList;
    private List<Integer> syncList;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentList = Concurrent.newList();
        cowList = new CopyOnWriteArrayList<>();
        syncList = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 1000; i++) {
            concurrentList.add(i);
            cowList.add(i);
            syncList.add(i);
        }
    }

    // --- Mixed read/write under contention ---

    @Benchmark
    public int concurrentList_read() {
        return concurrentList.get(500);
    }

    @Benchmark
    public int cowList_read() {
        return cowList.get(500);
    }

    @Benchmark
    public int syncList_read() {
        return syncList.get(500);
    }

    @Benchmark
    public boolean concurrentList_write() {
        return concurrentList.add(42);
    }

    @Benchmark
    public boolean cowList_write() {
        return cowList.add(42);
    }

    @Benchmark
    public boolean syncList_write() {
        return syncList.add(42);
    }

    @Benchmark
    public int concurrentList_iterate() {
        int sum = 0;
        for (int i : concurrentList) sum += i;
        return sum;
    }

    @Benchmark
    public int cowList_iterate() {
        int sum = 0;
        for (int i : cowList) sum += i;
        return sum;
    }
}
