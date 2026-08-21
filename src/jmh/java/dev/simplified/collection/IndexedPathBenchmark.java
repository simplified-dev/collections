package dev.simplified.collection;

import dev.simplified.collection.query.Indexed;
import dev.simplified.collection.query.SearchFunction;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Measures what a query for a property of a property costs against one for a property of the
 * element itself.
 *
 * <p>Neither names the path anywhere - it is derived from the row declaring the field and the
 * field's own type declaring what it is worth finding by - and both answer from an index, so the
 * two differ only in what it takes to work out which index the extractor names.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class IndexedPathBenchmark {

    /**
     * The object a row holds, declaring what it is worth finding by.
     */
    public static final class Zone {

        @Indexed
        private final String name;

        Zone(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * Row declaring an index over its own key and holding an indexed object besides.
     */
    public static final class Row {

        @Indexed(unique = true)
        private final String key;

        @Indexed
        private final Zone zone;

        Row(String key, Zone zone) {
            this.key = key;
            this.zone = zone;
        }

        public String getKey() {
            return this.key;
        }

        public Zone getZone() {
            return this.zone;
        }

    }

    private static final SearchFunction<Row, String> BY_KEY = Row::getKey;
    private static final SearchFunction<Row, String> BY_ZONE_NAME = SearchFunction.combine(Row::getZone, Zone::getName);

    @Param({"1000"})
    private int size;

    private ConcurrentList<Row> rows;
    private String key;
    private String zone;

    @Setup(Level.Iteration)
    public void setup() {
        this.rows = Concurrent.newList();

        for (int at = 0; at < this.size; at++)
            this.rows.add(new Row("key-" + at, new Zone("zone-" + at)));

        this.key = "key-" + (this.size - 1);
        this.zone = "zone-" + (this.size - 1);

        // Warm both indexes, so the measured lookups are the steady state rather than the build.
        this.rows.findFirstOrNull(BY_KEY, this.key);
        this.rows.findFirstOrNull(BY_ZONE_NAME, this.zone);
    }

    @Benchmark
    public Row byOwnProperty() {
        return this.rows.findFirstOrNull(BY_KEY, this.key);
    }

    @Benchmark
    public Row byPropertyOfAProperty() {
        return this.rows.findFirstOrNull(BY_ZONE_NAME, this.zone);
    }

}
