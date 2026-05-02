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
 * Compares Timsort against {@link RadixSort#byLong} on uniformly-random {@code long} keys.
 * The eight-pass radix overhead means break-even is higher than the int variant - radix should
 * still win at 100k+ but the margin is tighter.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class SortAlgorithmLongBenchmark {

    @Param({"1000", "100000", "1000000"})
    private int size;

    private Long[] sourceTemplate;

    @Setup(Level.Iteration)
    public void setup() {
        Random rng = new Random(0xC0FFEEL ^ size);
        sourceTemplate = new Long[size];
        for (int i = 0; i < size; i++) sourceTemplate[i] = rng.nextLong();
    }

    private List<Long> freshSource() {
        List<Long> list = new ArrayList<>(size);
        for (Long v : sourceTemplate) list.add(v);
        return list;
    }

    @Benchmark
    public List<Long> jdkSort_random() {
        List<Long> list = freshSource();
        list.sort(Comparator.naturalOrder());
        return list;
    }

    @Benchmark
    public List<Long> timsort_random() {
        List<Long> list = freshSource();
        Comparison.timsort(Comparator.<Long>naturalOrder()).sort(list);
        return list;
    }

    @Benchmark
    public List<Long> radix_random() {
        List<Long> list = freshSource();
        RadixSort.byLong(Long::longValue).sort(list);
        return list;
    }
}
