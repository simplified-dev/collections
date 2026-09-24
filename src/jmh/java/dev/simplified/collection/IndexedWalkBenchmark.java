package dev.simplified.collection;

import dev.simplified.collection.query.Indexed;
import dev.simplified.collection.query.SearchFunction;
import org.openjdk.jmh.annotations.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Measures what a collection pays to rebuild its index when its elements' class inherits every
 * declaration it carries rather than declaring one of its own.
 *
 * <p>A class declaring nothing of its own - a runtime proxy, or a subclass that only overrides
 * behaviour - reads the declarations of the class above it, so a collection it leads looks over
 * its elements for a wider class standing in for it before reading a schema. A class declaring on
 * its own level never does. The two rows carry the same data and differ only in that, so the gap
 * between {@code ownRebuild} and {@code inheritedRebuild} is the price of the look.
 *
 * <p>Both write before every lookup, so the index is rebuilt every time and never amortised, the
 * same pathological case {@link IndexedLookupBenchmark} measures.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class IndexedWalkBenchmark {

    /**
     * Row declaring an index over its key on its own level.
     */
    public static class Keyed {

        @Indexed(unique = true)
        private final String key;

        private final int payload;

        Keyed(String key, int payload) {
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
     * The same row declaring nothing of its own, so every declaration it carries is inherited.
     */
    public static final class Inherited extends Keyed {

        Inherited(String key, int payload) {
            super(key, payload);
        }

    }

    private static final SearchFunction<Keyed, String> KEY = Keyed::getKey;

    @Param({"100", "1000", "10000"})
    private int size;

    private ConcurrentList<Keyed> own;
    private ConcurrentList<Keyed> inherited;
    private String hit;

    @Setup(Level.Iteration)
    public void setup() {
        this.own = Concurrent.newList();
        this.inherited = Concurrent.newList();

        for (int at = 0; at < this.size; at++) {
            this.own.add(new Keyed("key-" + at, at));
            this.inherited.add(new Inherited("key-" + at, at));
        }

        // The last row, the same probe the rebuild in IndexedLookupBenchmark answers.
        this.hit = "key-" + (this.size - 1);
    }

    // --- Rebuild after every write ---

    @Benchmark
    public Optional<Keyed> ownRebuild() {
        Keyed added = new Keyed("key-transient", -1);
        this.own.add(added);

        try {
            return this.own.findFirst(KEY, this.hit);
        } finally {
            this.own.remove(added);
        }
    }

    @Benchmark
    public Optional<Keyed> inheritedRebuild() {
        Keyed added = new Inherited("key-transient", -1);
        this.inherited.add(added);

        try {
            return this.inherited.findFirst(KEY, this.hit);
        } finally {
            this.inherited.remove(added);
        }
    }

}
