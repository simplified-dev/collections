package dev.simplified.collection;

import dev.simplified.collection.query.Indexed;
import dev.simplified.collection.query.SearchFunction;
import org.openjdk.jmh.annotations.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Measures what an index buys a {@code findFirst} over a reference table.
 *
 * <p>Two structurally identical rows carry the same data; only one declares {@link Indexed}. The
 * claim to confirm is that the declared variant is flat across sizes while the other is linear, and
 * that it is not slower at the small end where a scan is already cheap.
 *
 * <p>{@code indexedRebuild} is the cost the invalidate-and-rebuild choice pays: it writes before
 * every lookup, so the index is rebuilt every time and never amortised. It is the pathological
 * case, not the expected one.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class IndexedLookupBenchmark {

    /**
     * Row declaring an index over its key.
     */
    public static final class Declared {

        @Indexed(unique = true)
        private final String key;

        private final int payload;

        Declared(String key, int payload) {
            this.key = key;
            this.payload = payload;
        }

        public String getKey() {
            return this.key;
        }

        public int getPayload() {
            return this.payload;
        }

    }

    /**
     * The same row declaring nothing, so every query against it scans.
     */
    public static final class Plain {

        private final String key;
        private final int payload;

        Plain(String key, int payload) {
            this.key = key;
            this.payload = payload;
        }

        public String getKey() {
            return this.key;
        }

        public int getPayload() {
            return this.payload;
        }

    }

    private static final SearchFunction<Declared, String> DECLARED_KEY = Declared::getKey;
    private static final SearchFunction<Plain, String> PLAIN_KEY = Plain::getKey;

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentList<Declared> declared;
    private ConcurrentList<Plain> plain;
    private String hit;
    private String miss;

    @Setup(Level.Iteration)
    public void setup() {
        this.declared = Concurrent.newList();
        this.plain = Concurrent.newList();

        for (int at = 0; at < this.size; at++) {
            this.declared.add(new Declared("key-" + at, at));
            this.plain.add(new Plain("key-" + at, at));
        }

        // The last row, so a scan pays the full table before it answers.
        this.hit = "key-" + (this.size - 1);
        this.miss = "key-absent";

        // Warm the index, so the measured lookups are the steady state rather than the build.
        this.declared.findFirst(DECLARED_KEY, this.hit);
    }

    // --- Hit ---

    @Benchmark
    public Optional<Declared> indexedHit() {
        return this.declared.findFirst(DECLARED_KEY, this.hit);
    }

    @Benchmark
    public Optional<Plain> scannedHit() {
        return this.plain.findFirst(PLAIN_KEY, this.hit);
    }

    // --- Miss ---

    @Benchmark
    public Optional<Declared> indexedMiss() {
        return this.declared.findFirst(DECLARED_KEY, this.miss);
    }

    @Benchmark
    public Optional<Plain> scannedMiss() {
        return this.plain.findFirst(PLAIN_KEY, this.miss);
    }

    // --- Rebuild after every write ---

    @Benchmark
    public Optional<Declared> indexedRebuild() {
        Declared added = new Declared("key-transient", -1);
        this.declared.add(added);

        try {
            return this.declared.findFirst(DECLARED_KEY, this.hit);
        } finally {
            this.declared.remove(added);
        }
    }

    @Benchmark
    public Optional<Plain> scannedRebuild() {
        Plain added = new Plain("key-transient", -1);
        this.plain.add(added);

        try {
            return this.plain.findFirst(PLAIN_KEY, this.hit);
        } finally {
            this.plain.remove(added);
        }
    }

}
