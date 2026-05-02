package dev.simplified.collection;

import dev.simplified.collection.sort.Comparison;
import dev.simplified.collection.sort.RadixSort;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Compares the JDK's Timsort against alternative comparison algorithms and LSD-radix on
 * uniformly-random int data across sizes 1k / 100k / 1M. Insertion sort is excluded - it would
 * dominate runtime at large {@code n} and has its own dedicated benchmark file.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class SortAlgorithmBenchmark {

    @Param({"1000", "100000", "1000000"})
    private int size;

    private Integer[] sourceTemplate;

    @Setup(Level.Iteration)
    public void setup() {
        Random rng = new Random(0xC0FFEEL ^ size);
        sourceTemplate = new Integer[size];
        for (int i = 0; i < size; i++) sourceTemplate[i] = rng.nextInt();
    }

    private List<Integer> freshSource() {
        List<Integer> list = new ArrayList<>(size);
        for (Integer v : sourceTemplate) list.add(v);
        return list;
    }

    @Benchmark
    public List<Integer> jdkSort_random() {
        List<Integer> list = freshSource();
        list.sort(Comparator.naturalOrder());
        return list;
    }

    @Benchmark
    public List<Integer> timsort_random() {
        List<Integer> list = freshSource();
        Comparison.timsort(Comparator.<Integer>naturalOrder()).sort(list);
        return list;
    }

    @Benchmark
    public List<Integer> heap_random() {
        List<Integer> list = freshSource();
        Comparison.heap(Comparator.<Integer>naturalOrder()).sort(list);
        return list;
    }

    @Benchmark
    public List<Integer> shell_random() {
        List<Integer> list = freshSource();
        Comparison.shell(Comparator.<Integer>naturalOrder()).sort(list);
        return list;
    }

    @Benchmark
    public List<Integer> quicksort_random() {
        List<Integer> list = freshSource();
        Comparison.quicksort(Comparator.<Integer>naturalOrder()).sort(list);
        return list;
    }

    @Benchmark
    public List<Integer> mergesort_random() {
        List<Integer> list = freshSource();
        Comparison.mergesort(Comparator.<Integer>naturalOrder()).sort(list);
        return list;
    }

    @Benchmark
    public List<Integer> radix_random() {
        List<Integer> list = freshSource();
        RadixSort.byInt(Integer::intValue).sort(list);
        return list;
    }
}
