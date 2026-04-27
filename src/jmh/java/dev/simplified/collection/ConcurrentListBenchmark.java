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
@State(Scope.Benchmark)
public class ConcurrentListBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentList<Integer> concurrentList;
    private ArrayList<Integer> arrayList;
    private CopyOnWriteArrayList<Integer> cowList;
    private List<Integer> syncList;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentList = Concurrent.newList();
        arrayList = new ArrayList<>();
        cowList = new CopyOnWriteArrayList<>();
        syncList = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < size; i++) {
            concurrentList.add(i);
            arrayList.add(i);
            cowList.add(i);
            syncList.add(i);
        }
    }

    // --- Read: indexed get ---

    @Benchmark
    public int concurrentList_get() {
        return concurrentList.get(size / 2);
    }

    @Benchmark
    public int arrayList_get() {
        return arrayList.get(size / 2);
    }

    @Benchmark
    public int cowList_get() {
        return cowList.get(size / 2);
    }

    @Benchmark
    public int syncList_get() {
        return syncList.get(size / 2);
    }

    // --- Read: iteration ---

    @Benchmark
    public int concurrentList_iterate() {
        int sum = 0;
        for (int i : concurrentList) sum += i;
        return sum;
    }

    @Benchmark
    public int arrayList_iterate() {
        int sum = 0;
        for (int i : arrayList) sum += i;
        return sum;
    }

    @Benchmark
    public int cowList_iterate() {
        int sum = 0;
        for (int i : cowList) sum += i;
        return sum;
    }

    // --- Write: add ---

    @Benchmark
    public boolean concurrentList_add() {
        return concurrentList.add(42);
    }

    @Benchmark
    public boolean arrayList_add() {
        return arrayList.add(42);
    }

    @Benchmark
    public boolean cowList_add() {
        return cowList.add(42);
    }

    // --- Read: contains ---

    @Benchmark
    public boolean concurrentList_contains() {
        return concurrentList.contains(size / 2);
    }

    @Benchmark
    public boolean arrayList_contains() {
        return arrayList.contains(size / 2);
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentList_size() {
        return concurrentList.size();
    }

    @Benchmark
    public int arrayList_size() {
        return arrayList.size();
    }
}
