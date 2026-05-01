package dev.simplified.collection;

import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark suite for {@link ConcurrentLinkedList}, comparing throughput against
 * {@link LinkedList} and {@code Collections.synchronizedList(new LinkedList<>())}.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class ConcurrentLinkedListBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentLinkedList<Integer> concurrentLinkedList;
    private LinkedList<Integer> linkedList;
    private List<Integer> syncLinkedList;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentLinkedList = new ConcurrentLinkedList<>();
        linkedList = new LinkedList<>();
        syncLinkedList = Collections.synchronizedList(new LinkedList<>());

        for (int i = 0; i < size; i++) {
            concurrentLinkedList.add(i);
            linkedList.add(i);
            syncLinkedList.add(i);
        }
    }

    // --- Read: indexed get ---

    @Benchmark
    public int concurrentLinkedList_get() {
        return concurrentLinkedList.get(size / 2);
    }

    @Benchmark
    public int linkedList_get() {
        return linkedList.get(size / 2);
    }

    @Benchmark
    public int syncLinkedList_get() {
        return syncLinkedList.get(size / 2);
    }

    // --- Read: iteration ---

    @Benchmark
    public int concurrentLinkedList_iterate() {
        int sum = 0;
        for (int i : concurrentLinkedList) sum += i;
        return sum;
    }

    @Benchmark
    public int linkedList_iterate() {
        int sum = 0;
        for (int i : linkedList) sum += i;
        return sum;
    }

    @Benchmark
    public int syncLinkedList_iterate() {
        int sum = 0;
        synchronized (syncLinkedList) {
            for (int i : syncLinkedList) sum += i;
        }
        return sum;
    }

    // --- Write: add ---

    @Benchmark
    public boolean concurrentLinkedList_add() {
        return concurrentLinkedList.add(42);
    }

    @Benchmark
    public boolean linkedList_add() {
        return linkedList.add(42);
    }

    @Benchmark
    public boolean syncLinkedList_add() {
        return syncLinkedList.add(42);
    }

    // --- Read: contains ---

    @Benchmark
    public boolean concurrentLinkedList_contains() {
        return concurrentLinkedList.contains(size / 2);
    }

    @Benchmark
    public boolean linkedList_contains() {
        return linkedList.contains(size / 2);
    }

    @Benchmark
    public boolean syncLinkedList_contains() {
        return syncLinkedList.contains(size / 2);
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentLinkedList_size() {
        return concurrentLinkedList.size();
    }

    @Benchmark
    public int linkedList_size() {
        return linkedList.size();
    }

    @Benchmark
    public int syncLinkedList_size() {
        return syncLinkedList.size();
    }
}
