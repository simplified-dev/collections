package dev.simplified.collection.query;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.tuple.pair.Pair;
import dev.simplified.collection.tuple.single.SingleStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Differential tests for {@link Indexable}.
 *
 * <p>Every case runs one query twice over the same data - once against elements whose class
 * declares {@link Indexed} and once against a structurally identical class that declares nothing -
 * and asserts the two answers are the same elements in the same order. The second list is the
 * control: it can only ever be scanned, so it is what the first list has to agree with.
 */
class IndexableTest {

    /**
     * The shape both fixtures answer to, so one extractor drives both lists.
     */
    interface Row {

        String mode();

        String code();

        int tier();

        List<String> tags();

        String label();

        Detail detail();

    }

    /**
     * The target of a followed reference, declaring what it is worth finding by.
     */
    static final class Detail {

        @Indexed
        private final String zone;

        Detail(String zone) {
            this.zone = zone;
        }

        String zone() {
            ZONE_READS.incrementAndGet();
            return this.zone;
        }

    }

    /**
     * Indexed fixture - a plain index, a unique index, a composite, and one field nothing indexes.
     */
    static final class Fast implements Row {

        @Indexed
        @Indexed(group = "modeAndTier", order = 0)
        private final String mode;

        @Indexed(unique = true)
        private final String code;

        @Indexed(group = "modeAndTier", order = 1)
        private final int tier;

        @Indexed
        private final List<String> tags;

        @Indexed(follow = true)
        private final Detail detail;

        private final String label;

        Fast(String mode, String code, int tier, List<String> tags, String label, Detail detail) {
            this.mode = mode;
            this.code = code;
            this.tier = tier;
            this.tags = tags;
            this.detail = detail;
            this.label = label;
        }

        /** {@inheritDoc} */
        @Override
        public Detail detail() {
            return this.detail;
        }

        /** {@inheritDoc} */
        @Override
        public String mode() {
            READS.incrementAndGet();
            return this.mode;
        }

        /** {@inheritDoc} */
        @Override
        public String code() {
            return this.code;
        }

        /** {@inheritDoc} */
        @Override
        public int tier() {
            return this.tier;
        }

        /** {@inheritDoc} */
        @Override
        public List<String> tags() {
            TAG_READS.incrementAndGet();
            return this.tags;
        }

        /** {@inheritDoc} */
        @Override
        public String label() {
            return this.label;
        }

    }

    /**
     * Control fixture, declaring nothing, so every query against it is the scan being compared to.
     */
    static final class Slow implements Row {

        private final String mode;
        private final String code;
        private final int tier;
        private final List<String> tags;
        private final Detail detail;
        private final String label;

        Slow(String mode, String code, int tier, List<String> tags, String label, Detail detail) {
            this.mode = mode;
            this.code = code;
            this.tier = tier;
            this.tags = tags;
            this.detail = detail;
            this.label = label;
        }

        /** {@inheritDoc} */
        @Override
        public Detail detail() {
            return this.detail;
        }

        /** {@inheritDoc} */
        @Override
        public String mode() {
            return this.mode;
        }

        /** {@inheritDoc} */
        @Override
        public String code() {
            return this.code;
        }

        /** {@inheritDoc} */
        @Override
        public int tier() {
            return this.tier;
        }

        /** {@inheritDoc} */
        @Override
        public List<String> tags() {
            return this.tags;
        }

        /** {@inheritDoc} */
        @Override
        public String label() {
            return this.label;
        }

    }

    /**
     * Counts how often the indexed fixture's {@code mode} accessor runs, so a test can show the
     * index is serving rather than merely agreeing.
     */
    private static final AtomicInteger READS = new AtomicInteger();

    /**
     * The same counter for the list-valued property, so the containment index can be shown to
     * serve rather than merely to agree.
     */
    private static final AtomicInteger TAG_READS = new AtomicInteger();

    /**
     * The same counter for the property reached through a followed field.
     */
    private static final AtomicInteger ZONE_READS = new AtomicInteger();

    private static final SearchFunction<Row, String> BY_MODE = Row::mode;
    private static final SearchFunction<Row, String> BY_CODE = Row::code;
    private static final SearchFunction<Row, List<String>> BY_TAGS = Row::tags;
    private static final SearchFunction<Row, String> BY_LABEL = Row::label;

    /**
     * A property of a property, which nothing declares by name - the path is derived from the
     * followed field and the target's own declaration.
     */
    private static final SearchFunction<Row, String> BY_ZONE = row -> row.detail().zone();

    /**
     * The two halves of the composite index, widened to a common result type.
     *
     * <p>A multi-predicate query carries one type parameter for every value it compares, so two
     * predicates over differently-typed properties can only be expressed at {@link Object}. That is
     * a property of the finder signatures rather than of the index, and it is what a caller
     * reaching a mixed-type composite has to write.
     */
    private static final SearchFunction<Row, Object> BY_MODE_VALUE = row -> row.mode();
    private static final SearchFunction<Row, Object> BY_TIER_VALUE = row -> row.tier();
    private static final SearchFunction<Row, Object> BY_LABEL_VALUE = row -> row.label();

    private static final Object[][] DATA = {
        { "alpha", "A1", 1, List.of("admin", "ops"), "first", new Detail("north") },
        { "beta", "B1", 1, List.of("ops"), "second", new Detail("south") },
        { "alpha", "A2", 2, List.of("admin"), "third", new Detail("north") },
        { null, "N1", 2, List.of(), "fourth", new Detail(null) },
        { "alpha", "A3", 1, null, "fifth", null },
        { "gamma", "G1", 3, List.of("ops", "admin", "ops"), "sixth", new Detail("north") }
    };

    private ConcurrentList<Row> indexed;
    private ConcurrentList<Row> scanned;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        this.indexed = Concurrent.newList();
        this.scanned = Concurrent.newList();

        for (Object[] row : DATA) {
            this.indexed.add(new Fast((String) row[0], (String) row[1], (int) row[2], (List<String>) row[3], (String) row[4], (Detail) row[5]));
            this.scanned.add(new Slow((String) row[0], (String) row[1], (int) row[2], (List<String>) row[3], (String) row[4], (Detail) row[5]));
        }
    }

    /**
     * Runs one query against both lists and asserts they answer the same elements in the same
     * order, identifying each element by its label.
     */
    private void differential(Function<ConcurrentList<Row>, List<Row>> query) {
        List<String> fromIndex = labels(query.apply(this.indexed));
        List<String> fromScan = labels(query.apply(this.scanned));
        assertEquals(fromScan, fromIndex);
    }

    /**
     * Runs one single-result query against both lists and asserts they agree.
     */
    private void differentialFirst(Function<ConcurrentList<Row>, Optional<Row>> query) {
        assertEquals(
            query.apply(this.scanned).map(Row::label),
            query.apply(this.indexed).map(Row::label)
        );
    }

    private static List<String> labels(List<Row> rows) {
        List<String> labels = new ArrayList<>(rows.size());
        rows.forEach(row -> labels.add(row.label()));
        return labels;
    }

    @Nested
    class TheIndexIsReallyUsed {

        @Test
        void indexes_declaringClass_holdsAnIndex() {
            assertFalse(indexed.indexes().isEmpty());
        }

        @Test
        void indexes_classDeclaringNothing_holdsNone() {
            assertTrue(scanned.indexes().isEmpty());
        }

        @Test
        void findAll_repeatedQueries_readTheAccessorOnlyWhileBuilding() {
            // The index reads every element once when it is built and never again; a scan reads
            // every element on every query. Nothing else can explain a flat count.
            indexed.findAll(BY_MODE, "alpha").toList();
            READS.set(0);

            for (int repeat = 0; repeat < 25; repeat++)
                indexed.findAll(BY_MODE, "alpha").toList();

            assertEquals(0, READS.get());
        }

        @Test
        void findAll_afterAWrite_rebuildsOnceAndThenStaysFlat() {
            indexed.findAll(BY_MODE, "alpha").toList();
            indexed.add(new Fast("alpha", "A4", 4, List.of(), "seventh", new Detail("north")));
            READS.set(0);

            indexed.findAll(BY_MODE, "alpha").toList();
            int afterRebuild = READS.get();
            assertEquals(indexed.size(), afterRebuild);

            indexed.findAll(BY_MODE, "alpha").toList();
            assertEquals(afterRebuild, READS.get());
        }

        @Test
        void findAll_repeatedCompositeQueries_readTheAccessorsOnlyWhileBuilding() {
            indexed.findAll(SearchFunction.Match.ALL, composite("alpha", 1)).toList();
            READS.set(0);

            for (int repeat = 0; repeat < 25; repeat++)
                indexed.findAll(SearchFunction.Match.ALL, composite("alpha", 1)).toList();

            assertEquals(0, READS.get());
        }

        @Test
        void findAll_compositeNamedInEitherOrder_buildsOneIndex() {
            indexed.findAll(SearchFunction.Match.ALL, composite("alpha", 1)).toList();
            READS.set(0);

            indexed.findAll(SearchFunction.Match.ALL, reversedComposite(1, "alpha")).toList();
            assertEquals(0, READS.get());
        }

        @Test
        void containsAll_repeatedQueries_readTheAccessorOnlyWhileBuilding() {
            indexed.containsAll(BY_TAGS, "admin").toList();
            TAG_READS.set(0);

            for (int repeat = 0; repeat < 25; repeat++)
                indexed.containsAll(BY_TAGS, "admin").toList();

            assertEquals(0, TAG_READS.get());
        }

        @Test
        void contains_repeatedQueries_readTheAccessorOnlyWhileBuilding() {
            indexed.contains(BY_MODE, "alpha");
            READS.set(0);

            for (int repeat = 0; repeat < 25; repeat++)
                assertTrue(indexed.contains(BY_MODE, "alpha"));

            assertEquals(0, READS.get());
        }

        @Test
        void findAll_repeatedMultiHopQueries_readTheAccessorOnlyWhileBuilding() {
            // The path is derived from Fast.detail being followed and Detail.zone declaring itself,
            // so nothing wrote "detail.zone" anywhere and the query still answers from an index.
            indexed.findAll(BY_ZONE, "north").toList();
            ZONE_READS.set(0);

            for (int repeat = 0; repeat < 25; repeat++)
                indexed.findAll(BY_ZONE, "north").toList();

            assertEquals(0, ZONE_READS.get());
        }

        @Test
        void findAll_multiHopByBodyAndByCombine_shareOneIndex() {
            SearchFunction<Row, String> combined = SearchFunction.combine(Row::detail, Detail::zone);

            indexed.findAll(BY_ZONE, "north").toList();
            ZONE_READS.set(0);

            assertEquals(
                labels(indexed.findAll(BY_ZONE, "north").toList()),
                labels(indexed.findAll(combined, "north").toList())
            );
            assertEquals(0, ZONE_READS.get());
        }

        @Test
        void findAll_unresolvableExtractor_keepsScanningEveryTime() {
            // The control for every count above. This extractor reads the same property through a
            // body the decoder refuses, so it can only be scanned - and the count rises by one per
            // element per query, which is exactly what a flat count above rules out.
            SearchFunction<Row, String> refused = row -> row.mode() + "!";
            READS.set(0);

            indexed.findAll(refused, "alpha!").toList();
            assertEquals(indexed.size(), READS.get());

            indexed.findAll(refused, "alpha!").toList();
            assertEquals(indexed.size() * 2, READS.get());
        }

    }

    @Nested
    class AgreesWithTheScan {

        @Test
        void findAll_nonUniqueProperty_matchesInSourceOrder() {
            differential(rows -> rows.findAll(BY_MODE, "alpha").toList());
        }

        @Test
        void findAll_uniqueProperty_matches() {
            differential(rows -> rows.findAll(BY_CODE, "A2").toList());
        }

        @Test
        void findAll_absentValue_matches() {
            differential(rows -> rows.findAll(BY_MODE, "delta").toList());
        }

        @Test
        void findAll_nullValue_matches() {
            differential(rows -> rows.findAll(BY_MODE, null).toList());
        }

        @Test
        void findAll_unindexedProperty_matches() {
            differential(rows -> rows.findAll(BY_LABEL, "third").toList());
        }

        @Test
        void findAll_emptyPredicates_matches() {
            differential(rows -> rows.findAll(
                SearchFunction.Match.ALL,
                List.<Pair<SearchFunction<Row, String>, String>>of()
            ).toList());
        }

        @Test
        void findAll_allWithTwoPredicatesFormingAComposite_matches() {
            differential(rows -> rows.findAll(SearchFunction.Match.ALL, composite("alpha", 1)).toList());
        }

        @Test
        void findAll_allWithTheCompositeReversed_matches() {
            differential(rows -> rows.findAll(SearchFunction.Match.ALL, reversedComposite(1, "alpha")).toList());
        }

        @Test
        void findAll_compositeMatchingNothing_matches() {
            differential(rows -> rows.findAll(SearchFunction.Match.ALL, composite("beta", 3)).toList());
        }

        @Test
        void findAll_allWithOneIndexedAndOneUnindexedPredicate_matches() {
            differential(rows -> rows.findAll(
                SearchFunction.Match.ALL,
                List.of(Pair.of(BY_MODE_VALUE, (Object) "alpha"), Pair.of(BY_LABEL_VALUE, (Object) "third"))
            ).toList());
        }

        @Test
        void findAll_anyWithTwoPredicates_matches() {
            differential(rows -> rows.findAll(
                SearchFunction.Match.ANY,
                List.of(Pair.of(BY_MODE, "beta"), Pair.of(BY_MODE, "gamma"))
            ).toList());
        }

        @Test
        void findAll_allWithTwoPredicatesOverOneProperty_matches() {
            // Two predicates over one property are a contradiction rather than a composite key, so
            // the index steps aside and the scan answers - with nothing, as it should.
            differential(rows -> rows.findAll(
                SearchFunction.Match.ALL,
                List.of(Pair.of(BY_MODE, "alpha"), Pair.of(BY_MODE, "beta"))
            ).toList());
        }

        @Test
        void findAll_lambdaBodyExtractor_matches() {
            differential(rows -> rows.findAll((SearchFunction<Row, String>) row -> row.mode(), "alpha").toList());
        }

        @Test
        void findAll_unresolvableExtractor_matches() {
            differential(rows -> rows.findAll((SearchFunction<Row, String>) row -> row.mode() + "!", "alpha!").toList());
        }

        @Test
        void findFirst_nonUniqueProperty_matches() {
            differentialFirst(rows -> rows.findFirst(BY_MODE, "alpha"));
        }

        @Test
        void findLast_nonUniqueProperty_matches() {
            differentialFirst(rows -> rows.findLast(BY_MODE, "alpha"));
        }

        @Test
        void findFirst_absentValue_matches() {
            differentialFirst(rows -> rows.findFirst(BY_MODE, "delta"));
        }

        @Test
        void containsAll_listProperty_matches() {
            differential(rows -> rows.containsAll(BY_TAGS, "admin").toList());
        }

        @Test
        void containsAll_absentMember_matches() {
            differential(rows -> rows.containsAll(BY_TAGS, "missing").toList());
        }

        @Test
        void containsAll_duplicateMember_yieldsTheElementOnce() {
            differential(rows -> rows.containsAll(BY_TAGS, "ops").toList());
        }

        @Test
        void containsFirst_listProperty_matches() {
            differentialFirst(rows -> rows.containsFirst(BY_TAGS, "admin"));
        }

        @Test
        void matchAll_predicate_matches() {
            differential(rows -> rows.matchAll(row -> "alpha".equals(row.mode())).toList());
        }

        @Test
        void findAll_multiHopPath_matches() {
            differential(rows -> rows.findAll(BY_ZONE, "north").toList());
        }

        @Test
        void findAll_multiHopPathThroughANullIntermediate_matches() {
            // One element holds no detail at all, so reading through it raises the
            // NullPointerException both the scan and the index build read as a non-match.
            differential(rows -> rows.findAll(BY_ZONE, "south").toList());
        }

        @Test
        void findAll_multiHopPathToANullValue_matches() {
            differential(rows -> rows.findAll(BY_ZONE, null).toList());
        }

        @Test
        void findAll_multiHopByCombine_matches() {
            differential(rows -> rows.findAll(SearchFunction.combine(Row::detail, Detail::zone), "north").toList());
        }

        @Test
        void findFirst_multiHopPath_matches() {
            differentialFirst(rows -> rows.findFirst(BY_ZONE, "north"));
        }

        @Test
        void findLast_multiHopPath_matches() {
            differentialFirst(rows -> rows.findLast(BY_ZONE, "north"));
        }

        @Test
        void findAll_multiHopPathToAnAbsentValue_matches() {
            differential(rows -> rows.findAll(BY_ZONE, "east").toList());
        }

        @Test
        void contains_indexedProperty_matches() {
            assertEquals(scanned.contains(BY_MODE, "alpha"), indexed.contains(BY_MODE, "alpha"));
            assertEquals(scanned.contains(BY_MODE, "delta"), indexed.contains(BY_MODE, "delta"));
            assertEquals(scanned.contains(BY_MODE, null), indexed.contains(BY_MODE, null));
        }

    }

    @Nested
    class EdgeCases {

        @Test
        void findAll_emptyCollection_matches() {
            indexed.clear();
            scanned.clear();
            differential(rows -> rows.findAll(BY_MODE, "alpha").toList());
        }

        @Test
        void findAll_singleElement_matches() {
            indexed.clear();
            scanned.clear();
            indexed.add(new Fast("alpha", "A1", 1, List.of("admin"), "only", new Detail("north")));
            scanned.add(new Slow("alpha", "A1", 1, List.of("admin"), "only", new Detail("north")));
            differential(rows -> rows.findAll(BY_MODE, "alpha").toList());
        }

        @Test
        void findAll_uniqueIndexOverDuplicateValues_throws() {
            indexed.add(new Fast("delta", "A1", 9, List.of(), "duplicate", new Detail("north")));

            IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> indexed.findAll(BY_CODE, "A1").toList()
            );

            assertTrue(thrown.getMessage().contains("unique"));
        }

        @Test
        void findAll_handRolledSortable_scansWithoutAnIndex() {
            // The interface stays implementable by a lambda, which is what keeps every existing
            // Searchable and Sortable usable unchanged.
            List<Row> source = List.of(new Fast("alpha", "A1", 1, List.of(), "only", new Detail("north")));
            Sortable<Row> custom = () -> SingleStream.of(source);

            assertTrue(custom.indexes().isEmpty());
            assertEquals("only", custom.findFirst(BY_MODE, "alpha").orElseThrow().label());
        }

    }

    /**
     * The two composite predicates in the index's declared order.
     */
    private static List<Pair<SearchFunction<Row, Object>, Object>> composite(String mode, int tier) {
        return List.of(Pair.of(BY_MODE_VALUE, (Object) mode), Pair.of(BY_TIER_VALUE, (Object) tier));
    }

    /**
     * The same two predicates named in the other order, which has to resolve to the same index.
     */
    private static List<Pair<SearchFunction<Row, Object>, Object>> reversedComposite(int tier, String mode) {
        return List.of(Pair.of(BY_TIER_VALUE, (Object) tier), Pair.of(BY_MODE_VALUE, (Object) mode));
    }

}
