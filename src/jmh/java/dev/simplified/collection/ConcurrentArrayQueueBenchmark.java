package dev.simplified.collection;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark suite for {@link ConcurrentArrayQueue}, comparing throughput against
 * {@link ArrayDeque} and {@link ConcurrentLinkedQueue}.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class ConcurrentArrayQueueBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentArrayQueue<Integer> concurrentArrayQueue;
    private ArrayDeque<Integer> arrayDeque;
    private ConcurrentLinkedQueue<Integer> jdkConcurrentQueue;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentArrayQueue = new ConcurrentArrayQueue<>();
        arrayDeque = new ArrayDeque<>();
        jdkConcurrentQueue = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < size; i++) {
            concurrentArrayQueue.offer(i);
            arrayDeque.offer(i);
            jdkConcurrentQueue.offer(i);
        }
    }

    // --- Write: offer ---

    @Benchmark
    public boolean concurrentArrayQueue_offer() {
        return concurrentArrayQueue.offer(42);
    }

    @Benchmark
    public boolean arrayDeque_offer() {
        return arrayDeque.offer(42);
    }

    @Benchmark
    public boolean jdkConcurrentQueue_offer() {
        return jdkConcurrentQueue.offer(42);
    }

    // --- Read: peek ---

    @Benchmark
    public Integer concurrentArrayQueue_peek() {
        return concurrentArrayQueue.peek();
    }

    @Benchmark
    public Integer arrayDeque_peek() {
        return arrayDeque.peek();
    }

    @Benchmark
    public Integer jdkConcurrentQueue_peek() {
        return jdkConcurrentQueue.peek();
    }

    // --- Read: iteration ---

    @Benchmark
    public int concurrentArrayQueue_iterate() {
        int sum = 0;
        for (int i : concurrentArrayQueue) sum += i;
        return sum;
    }

    @Benchmark
    public int arrayDeque_iterate() {
        int sum = 0;
        for (int i : arrayDeque) sum += i;
        return sum;
    }

    @Benchmark
    public int jdkConcurrentQueue_iterate() {
        int sum = 0;
        for (int i : jdkConcurrentQueue) sum += i;
        return sum;
    }

    // --- Read: contains ---

    @Benchmark
    public boolean concurrentArrayQueue_contains() {
        return concurrentArrayQueue.contains(size / 2);
    }

    @Benchmark
    public boolean arrayDeque_contains() {
        return arrayDeque.contains(size / 2);
    }

    @Benchmark
    public boolean jdkConcurrentQueue_contains() {
        return jdkConcurrentQueue.contains(size / 2);
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentArrayQueue_size() {
        return concurrentArrayQueue.size();
    }

    @Benchmark
    public int arrayDeque_size() {
        return arrayDeque.size();
    }

    @Benchmark
    public int jdkConcurrentQueue_size() {
        return jdkConcurrentQueue.size();
    }
}
