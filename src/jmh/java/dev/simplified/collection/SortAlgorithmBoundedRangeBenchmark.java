package dev.simplified.collection;

import dev.simplified.collection.sort.Comparison;
import dev.simplified.collection.sort.CountingSort;
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
 * Compares Timsort, radix, and counting sort on bounded-range int data ({@code key} in
 * {@code [0, 99]}). Counting sort should dominate here because its work is {@code O(n + k)}
 * where {@code k = 100} - effectively {@code O(n)} with a tiny counter array.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class SortAlgorithmBoundedRangeBenchmark {

    private static final int KEY_MIN = 0;
    private static final int KEY_MAX = 99;

    @Param({"10000", "100000", "1000000"})
    private int size;

    private Integer[] sourceTemplate;

    @Setup(Level.Iteration)
    public void setup() {
        Random rng = new Random(0xC0FFEEL ^ size);
        sourceTemplate = new Integer[size];
        for (int i = 0; i < size; i++) sourceTemplate[i] = rng.nextInt(KEY_MIN, KEY_MAX + 1);
    }

    private List<Integer> freshSource() {
        List<Integer> list = new ArrayList<>(size);
        for (Integer v : sourceTemplate) list.add(v);
        return list;
    }

    @Benchmark
    public List<Integer> jdkSort_bounded() {
        List<Integer> list = freshSource();
        list.sort(Comparator.naturalOrder());
        return list;
    }

    @Benchmark
    public List<Integer> timsort_bounded() {
        List<Integer> list = freshSource();
        Comparison.timsort(Comparator.<Integer>naturalOrder()).sort(list);
        return list;
    }

    @Benchmark
    public List<Integer> radix_bounded() {
        List<Integer> list = freshSource();
        RadixSort.byInt(Integer::intValue).sort(list);
        return list;
    }

    @Benchmark
    public List<Integer> counting_bounded() {
        List<Integer> list = freshSource();
        CountingSort.byInt(Integer::intValue, KEY_MIN, KEY_MAX).sort(list);
        return list;
    }
}
