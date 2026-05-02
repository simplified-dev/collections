package dev.simplified.collection;

import dev.simplified.collection.sort.Comparison;
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
 * Compares insertion sort against Timsort and the other comparison algorithms at sizes where
 * insertion sort wins ({@code n} = 16, 32, 64). The hypothesis: insertion sort dominates for
 * {@code n < ~32} thanks to its trivial constant factor and sequential access pattern, then
 * loses ground rapidly as Timsort's run-detection and merge passes amortize.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class SortAlgorithmTinyBenchmark {

    @Param({"16", "32", "64", "128"})
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
    public List<Integer> jdkSort_tiny() {
        List<Integer> list = freshSource();
        list.sort(Comparator.naturalOrder());
        return list;
    }

    @Benchmark
    public List<Integer> timsort_tiny() {
        List<Integer> list = freshSource();
        Comparison.timsort(Comparator.<Integer>naturalOrder()).sort(list);
        return list;
    }

    @Benchmark
    public List<Integer> insertion_tiny() {
        List<Integer> list = freshSource();
        Comparison.insertion(Comparator.<Integer>naturalOrder()).sort(list);
        return list;
    }

    @Benchmark
    public List<Integer> shell_tiny() {
        List<Integer> list = freshSource();
        Comparison.shell(Comparator.<Integer>naturalOrder()).sort(list);
        return list;
    }
}
