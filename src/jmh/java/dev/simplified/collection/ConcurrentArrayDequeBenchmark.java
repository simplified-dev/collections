package dev.simplified.collection;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark suite for {@link ConcurrentArrayDeque}, comparing throughput against
 * {@link ArrayDeque} and {@link ConcurrentLinkedDeque}.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class ConcurrentArrayDequeBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentArrayDeque<Integer> concurrentArrayDeque;
    private ArrayDeque<Integer> arrayDeque;
    private ConcurrentLinkedDeque<Integer> jdkConcurrentDeque;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentArrayDeque = new ConcurrentArrayDeque<>();
        arrayDeque = new ArrayDeque<>();
        jdkConcurrentDeque = new ConcurrentLinkedDeque<>();

        for (int i = 0; i < size; i++) {
            concurrentArrayDeque.offer(i);
            arrayDeque.offer(i);
            jdkConcurrentDeque.offer(i);
        }
    }

    // --- Write: offer ---

    @Benchmark
    public boolean concurrentArrayDeque_offer() {
        return concurrentArrayDeque.offer(42);
    }

    @Benchmark
    public boolean arrayDeque_offer() {
        return arrayDeque.offer(42);
    }

    @Benchmark
    public boolean jdkConcurrentDeque_offer() {
        return jdkConcurrentDeque.offer(42);
    }

    // --- Read: peek ---

    @Benchmark
    public Integer concurrentArrayDeque_peek() {
        return concurrentArrayDeque.peek();
    }

    @Benchmark
    public Integer arrayDeque_peek() {
        return arrayDeque.peek();
    }

    @Benchmark
    public Integer jdkConcurrentDeque_peek() {
        return jdkConcurrentDeque.peek();
    }

    // --- Read: iteration ---

    @Benchmark
    public int concurrentArrayDeque_iterate() {
        int sum = 0;
        for (int i : concurrentArrayDeque) sum += i;
        return sum;
    }

    @Benchmark
    public int arrayDeque_iterate() {
        int sum = 0;
        for (int i : arrayDeque) sum += i;
        return sum;
    }

    @Benchmark
    public int jdkConcurrentDeque_iterate() {
        int sum = 0;
        for (int i : jdkConcurrentDeque) sum += i;
        return sum;
    }

    // --- Read: contains ---

    @Benchmark
    public boolean concurrentArrayDeque_contains() {
        return concurrentArrayDeque.contains(size / 2);
    }

    @Benchmark
    public boolean arrayDeque_contains() {
        return arrayDeque.contains(size / 2);
    }

    @Benchmark
    public boolean jdkConcurrentDeque_contains() {
        return jdkConcurrentDeque.contains(size / 2);
    }

    // --- Read: size ---

    @Benchmark
    public int concurrentArrayDeque_size() {
        return concurrentArrayDeque.size();
    }

    @Benchmark
    public int arrayDeque_size() {
        return arrayDeque.size();
    }

    @Benchmark
    public int jdkConcurrentDeque_size() {
        return jdkConcurrentDeque.size();
    }

    // --- Deque: addFirst ---

    @Benchmark
    public void concurrentArrayDeque_addFirst() {
        concurrentArrayDeque.addFirst(42);
    }

    @Benchmark
    public void arrayDeque_addFirst() {
        arrayDeque.addFirst(42);
    }

    @Benchmark
    public void jdkConcurrentDeque_addFirst() {
        jdkConcurrentDeque.addFirst(42);
    }

    // --- Deque: addLast ---

    @Benchmark
    public void concurrentArrayDeque_addLast() {
        concurrentArrayDeque.addLast(42);
    }

    @Benchmark
    public void arrayDeque_addLast() {
        arrayDeque.addLast(42);
    }

    @Benchmark
    public void jdkConcurrentDeque_addLast() {
        jdkConcurrentDeque.addLast(42);
    }

    // --- Deque: pollFirst ---

    @Benchmark
    public Integer concurrentArrayDeque_pollFirst() {
        return concurrentArrayDeque.pollFirst();
    }

    @Benchmark
    public Integer arrayDeque_pollFirst() {
        return arrayDeque.pollFirst();
    }

    @Benchmark
    public Integer jdkConcurrentDeque_pollFirst() {
        return jdkConcurrentDeque.pollFirst();
    }

    // --- Deque: pollLast ---

    @Benchmark
    public Integer concurrentArrayDeque_pollLast() {
        return concurrentArrayDeque.pollLast();
    }

    @Benchmark
    public Integer arrayDeque_pollLast() {
        return arrayDeque.pollLast();
    }

    @Benchmark
    public Integer jdkConcurrentDeque_pollLast() {
        return jdkConcurrentDeque.pollLast();
    }
}
