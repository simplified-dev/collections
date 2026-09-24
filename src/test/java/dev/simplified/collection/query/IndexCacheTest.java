package dev.simplified.collection.query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IndexCache}, driven directly against a snapshot array so the store is
 * pinned before anything routes queries through it.
 */
class IndexCacheTest {

    /**
     * Fixture carrying one plain index, one unique index, one composite, and one field nothing
     * indexes.
     */
    static class Row {

        @Indexed
        private final String mode;

        @Indexed(unique = true)
        private final String code;

        @Indexed(group = "modeAndTier", order = 0)
        private final String groupedMode;

        @Indexed(group = "modeAndTier", order = 1)
        private final int tier;

        private final String label;

        Row(String mode, String code, int tier, String label) {
            this.mode = mode;
            this.code = code;
            this.groupedMode = mode;
            this.tier = tier;
            this.label = label;
        }

        String getMode() {
            return this.mode;
        }

        String getCode() {
            return this.code;
        }

        String getGroupedMode() {
            return this.groupedMode;
        }

        int getTier() {
            return this.tier;
        }

        String getLabel() {
            return this.label;
        }

    }

    /**
     * Fixture that can no longer be read, the way a detached proxy of an entity raises from every
     * accessor once the session that could load it is gone. It counts its reads, so a test can show
     * a refusal is held rather than decided again.
     */
    static class Detached extends Row {

        private int reads;

        Detached() {
            super(null, null, 0, null);
        }

        @Override
        String getMode() {
            throw this.detached();
        }

        @Override
        String getCode() {
            throw this.detached();
        }

        @Override
        String getGroupedMode() {
            throw this.detached();
        }

        @Override
        int getTier() {
            throw this.detached();
        }

        @Override
        String getLabel() {
            throw this.detached();
        }

        private IllegalStateException detached() {
            this.reads++;
            return new IllegalStateException("detached");
        }

    }

    /**
     * Fixture whose accessor dereferences its own field, so reading the property raises a
     * {@link NullPointerException} for some elements and not others.
     */
    static class Fragile {

        @Indexed
        private final String mode;

        Fragile(String mode) {
            this.mode = mode;
        }

        String getMode() {
            return this.mode.trim();
        }

    }

    /**
     * Fixture declaring nothing.
     */
    record Bare(String id) {}

    /**
     * Fixture declaring through its accessor rather than its field, the way a persistence mapping
     * written against properties does.
     */
    static class Accessed {

        private final String code;

        Accessed(String code) {
            this.code = code;
        }

        @Indexed(unique = true)
        String getCode() {
            return this.code;
        }

    }

    /**
     * Key every instance of which hashes alike, so two of them can only be told apart by probing on
     * past the slot they both land in.
     */
    record Clashing(String name) {

        @Override
        public int hashCode() {
            return 7;
        }

    }

    /**
     * Fixture keyed by a value that hashes alike for every element.
     */
    static class Clashed {

        @Indexed
        private final Clashing key;

        Clashed(String key) {
            this.key = key == null ? null : new Clashing(key);
        }

        Clashing getKey() {
            return this.key;
        }

    }

    /**
     * Fixture carrying one list-valued index, which a containment query reads member by member.
     */
    static class Tagged {

        @Indexed
        private final List<String> tags;

        Tagged(List<String> tags) {
            this.tags = tags;
        }

        List<String> getTags() {
            return this.tags;
        }

    }

    /**
     * Fixture whose list cannot be read at all.
     */
    static class BrokenTags extends Tagged {

        BrokenTags() {
            super(null);
        }

        @Override
        List<String> getTags() {
            throw new IllegalStateException("detached");
        }

    }

    /**
     * Key whose hash cannot be taken, the way a reference to an entity raises from
     * {@code hashCode} once the session that could load it is gone. Its {@code equals} still
     * answers, which is all a scan ever asks of it.
     */
    record Unhashable(String name) {

        @Override
        public int hashCode() {
            throw new IllegalStateException("detached");
        }

    }

    /**
     * Fixture keyed by a value of any class.
     */
    static class Referencing {

        @Indexed
        private final Object target;

        Referencing(Object target) {
            this.target = target;
        }

        Object getTarget() {
            return this.target;
        }

    }

    private static final SearchFunction<Row, String> BY_MODE = Row::getMode;
    private static final SearchFunction<Row, String> BY_CODE = Row::getCode;
    private static final SearchFunction<Row, String> BY_GROUPED_MODE = Row::getGroupedMode;
    private static final SearchFunction<Row, Integer> BY_TIER = Row::getTier;
    private static final SearchFunction<Row, String> BY_LABEL = Row::getLabel;
    private static final SearchFunction<Tagged, List<String>> BY_TAGS = Tagged::getTags;
    private static final SearchFunction<Referencing, Object> BY_TARGET = Referencing::getTarget;

    private static final Row ALPHA_ONE = new Row("alpha", "A1", 1, "first");
    private static final Row ALPHA_TWO = new Row("alpha", "A2", 2, "second");
    private static final Row BETA_ONE = new Row("beta", "B1", 1, "third");
    private static final Row NULL_MODE = new Row(null, "N1", 3, "fourth");

    private static IndexCache<Row> cacheOf(Row... rows) {
        return IndexCache.over(rows);
    }

    /**
     * Probes one single-property index.
     */
    private static List<Row> lookup(IndexCache<Row> cache, SearchFunction<Row, ?> extractor, Object value) {
        return cache.lookup(List.of(PropertyReference.of(extractor)), List.of(extractor), Collections.singletonList(value));
    }

    /**
     * Probes the containment index over the tags.
     */
    private static List<Tagged> containing(IndexCache<Tagged> cache, String tag) {
        return cache.lookupContaining(PropertyReference.of(BY_TAGS), BY_TAGS, tag);
    }

    /**
     * A list that claims a member and raises on reaching it, the way a lazily loaded collection does
     * once the session that could load it is gone.
     */
    private static List<String> unwalkable() {
        return new AbstractList<>() {

            @Override
            public String get(int index) {
                throw new IllegalStateException("detached");
            }

            @Override
            public int size() {
                return 1;
            }

        };
    }

    @Nested
    class Answers {

        @Test
        void lookup_indexedProperty_answersEveryMatchInSourceOrder() {
            List<Row> found = lookup(cacheOf(ALPHA_ONE, BETA_ONE, ALPHA_TWO), BY_MODE, "alpha");

            assertNotNull(found);
            assertEquals(List.of(ALPHA_ONE, ALPHA_TWO), found);
        }

        @Test
        void lookup_absentValue_answersAnEmptyListRatherThanAMiss() {
            // An empty list means "the index looked and found nothing"; null means "scan instead",
            // and the two must not be confused.
            List<Row> found = lookup(cacheOf(ALPHA_ONE, BETA_ONE), BY_MODE, "gamma");

            assertNotNull(found);
            assertTrue(found.isEmpty());
        }

        @Test
        void lookup_nullValue_isAnOrdinaryKey() {
            List<Row> found = lookup(cacheOf(ALPHA_ONE, NULL_MODE, BETA_ONE), BY_MODE, null);

            assertNotNull(found);
            assertEquals(List.of(NULL_MODE), found);
        }

        @Test
        void lookup_uniqueIndex_answersTheOneElement() {
            List<Row> found = lookup(cacheOf(ALPHA_ONE, ALPHA_TWO, BETA_ONE), BY_CODE, "A2");

            assertNotNull(found);
            assertEquals(List.of(ALPHA_TWO), found);
        }

        @Test
        void lookup_lambdaBodyExtractor_resolvesToTheSameIndex() {
            IndexCache<Row> cache = cacheOf(ALPHA_ONE, BETA_ONE, ALPHA_TWO);
            SearchFunction<Row, String> body = row -> row.getMode();

            assertEquals(List.of(ALPHA_ONE, ALPHA_TWO), lookup(cache, BY_MODE, "alpha"));
            assertEquals(List.of(ALPHA_ONE, ALPHA_TWO), lookup(cache, body, "alpha"));
        }

        @Test
        void lookup_accessorDeclaration_answersFromTheIndex() {
            // The declaration sits on getCode and the query is written as Accessed::getCode, and
            // both name the property "code" - which is the whole of why they meet.
            Accessed first = new Accessed("A1");
            Accessed second = new Accessed("A2");
            SearchFunction<Accessed, String> byCode = Accessed::getCode;

            IndexCache<Accessed> cache = IndexCache.over(new Accessed[] { first, second });

            assertEquals(
                List.of(second),
                cache.lookup(
                    List.of(PropertyReference.of(byCode)),
                    List.<SearchFunction<Accessed, ?>>of(byCode),
                    Collections.singletonList("A2")
                )
            );
        }

        @Test
        void lookup_nullElement_isFiledUnderNothing() {
            List<Row> found = lookup(cacheOf(ALPHA_ONE, null, ALPHA_TWO), BY_MODE, "alpha");

            assertNotNull(found);
            assertEquals(List.of(ALPHA_ONE, ALPHA_TWO), found);
        }

        @Test
        void lookup_extractorRaisingNullPointer_omitsThatElement() {
            // The scan reads a NullPointerException on the way to a property as a non-match, so the
            // index must file the element under nothing rather than under null.
            Fragile present = new Fragile("alpha");
            Fragile broken = new Fragile(null);
            IndexCache<Fragile> cache = IndexCache.over(new Fragile[] { present, broken });
            SearchFunction<Fragile, String> byMode = Fragile::getMode;

            assertEquals(
                List.of(present),
                cache.lookup(List.of(PropertyReference.of(byMode)), List.of(byMode), Collections.singletonList("alpha"))
            );
            assertEquals(
                List.of(),
                cache.lookup(List.of(PropertyReference.of(byMode)), List.of(byMode), Collections.singletonList(null))
            );
        }

    }

    @Nested
    class Tables {

        @Test
        void lookup_moreKeysThanTheTableStartedWith_answersEveryOneOfThem() {
            // Far past the width the table is laid out at, so it is rehashed several times over and
            // every key has to survive being re-filed.
            Row[] rows = new Row[600];

            for (int at = 0; at < rows.length; at++)
                rows[at] = new Row("mode-" + at % 300, "code-" + at, at, "label-" + at);

            IndexCache<Row> cache = cacheOf(rows);

            for (int at = 0; at < 300; at++)
                assertEquals(List.of(rows[at], rows[at + 300]), lookup(cache, BY_MODE, "mode-" + at));

            assertEquals(List.of(rows[417]), lookup(cache, BY_CODE, "code-417"));
            assertEquals(List.of(), lookup(cache, BY_MODE, "mode-300"));
        }

        @Test
        void lookup_nullKeyAmongManyOthers_survivesTheTableWidening() {
            Row[] rows = new Row[200];
            rows[0] = NULL_MODE;

            for (int at = 1; at < rows.length; at++)
                rows[at] = new Row("mode-" + at, "code-" + at, at, "label-" + at);

            assertEquals(List.of(NULL_MODE), lookup(cacheOf(rows), BY_MODE, null));
        }

        @Test
        void lookup_keysThatAllHashAlike_areStillToldApart() {
            Clashed[] rows = new Clashed[64];

            for (int at = 0; at < rows.length; at++)
                rows[at] = new Clashed("key-" + at);

            IndexCache<Clashed> cache = IndexCache.over(rows);
            SearchFunction<Clashed, Clashing> byKey = Clashed::getKey;

            for (int at = 0; at < rows.length; at++) {
                assertEquals(
                    List.of(rows[at]),
                    cache.lookup(List.of(PropertyReference.of(byKey)), List.of(byKey), List.of(new Clashing("key-" + at)))
                );
            }

            assertEquals(
                List.of(),
                cache.lookup(List.of(PropertyReference.of(byKey)), List.of(byKey), List.of(new Clashing("absent")))
            );
        }

        @Test
        void lookup_oneElementHeldTwice_answersItTwice() {
            // The scan hands back both occurrences, so a bucket that quietly folded them into one
            // would disagree with it.
            List<Row> found = lookup(cacheOf(ALPHA_ONE, ALPHA_ONE, BETA_ONE), BY_MODE, "alpha");

            assertEquals(List.of(ALPHA_ONE, ALPHA_ONE), found);
        }

        @Test
        void lookup_bucket_isUnmodifiable() {
            assertThrows(
                UnsupportedOperationException.class,
                () -> lookup(cacheOf(ALPHA_ONE, ALPHA_TWO, BETA_ONE), BY_MODE, "alpha").clear()
            );
            assertThrows(
                UnsupportedOperationException.class,
                () -> lookup(cacheOf(ALPHA_ONE, ALPHA_TWO, BETA_ONE), BY_MODE, "beta").clear()
            );
        }

    }

    @Nested
    class Composites {

        @Test
        void lookup_compositeInDeclaredOrder_answers() {
            IndexCache<Row> cache = cacheOf(ALPHA_ONE, ALPHA_TWO, BETA_ONE);

            List<Row> found = cache.lookup(
                List.of(PropertyReference.of(BY_GROUPED_MODE), PropertyReference.of(BY_TIER)),
                List.of(BY_GROUPED_MODE, BY_TIER),
                List.of("alpha", 2)
            );

            assertNotNull(found);
            assertEquals(List.of(ALPHA_TWO), found);
        }

        @Test
        void lookup_compositeInAnyOrder_answersTheSame() {
            IndexCache<Row> cache = cacheOf(ALPHA_ONE, ALPHA_TWO, BETA_ONE);

            List<Row> found = cache.lookup(
                List.of(PropertyReference.of(BY_TIER), PropertyReference.of(BY_GROUPED_MODE)),
                List.of(BY_TIER, BY_GROUPED_MODE),
                List.of(2, "alpha")
            );

            assertNotNull(found);
            assertEquals(List.of(ALPHA_TWO), found);
        }

        @Test
        void lookup_halfOfAComposite_isRefused() {
            // groupedMode is only ever declared as part of the pair, so naming it alone names no
            // index at all.
            assertNull(lookup(cacheOf(ALPHA_ONE, BETA_ONE), BY_GROUPED_MODE, "alpha"));
        }

        @Test
        void lookup_twoPredicatesOverOneProperty_isRefused() {
            IndexCache<Row> cache = cacheOf(ALPHA_ONE, BETA_ONE);

            assertNull(cache.lookup(
                List.of(PropertyReference.of(BY_MODE), PropertyReference.of(BY_MODE)),
                List.of(BY_MODE, BY_MODE),
                List.of("alpha", "beta")
            ));
        }

    }

    @Nested
    class Refuses {

        @Test
        void lookup_undeclaredProperty_isRefused() {
            assertNull(lookup(cacheOf(ALPHA_ONE, BETA_ONE), BY_LABEL, "first"));
        }

        @Test
        void lookup_unresolvableExtractor_isRefused() {
            SearchFunction<Row, String> capturing = row -> row.getMode() + "!";
            assertNull(lookup(cacheOf(ALPHA_ONE, BETA_ONE), capturing, "alpha!"));
        }

        @Test
        void lookup_classDeclaringNothing_isRefused() {
            IndexCache<Bare> cache = IndexCache.over(new Bare[] { new Bare("a"), new Bare("b") });

            // Nothing the class declares can ever be asked for, so there is nothing to hold and the
            // shared empty cache stands in for one.
            assertSame(IndexCache.none(), cache);
            assertTrue(cache.isEmpty());
            assertNull(cache.lookup(
                List.of(PropertyReference.of((SearchFunction<Bare, String>) Bare::id)),
                List.of(Bare::id),
                Collections.singletonList("a")
            ));
        }

        @Test
        void lookup_emptyCollection_isRefused() {
            IndexCache<Row> cache = IndexCache.over(new Row[0]);

            assertSame(IndexCache.none(), cache);
            assertTrue(cache.isEmpty());
            assertNull(lookup(cache, BY_MODE, "alpha"));
        }

        @Test
        void lookup_none_isRefused() {
            assertNull(lookup(IndexCache.none(), BY_MODE, "alpha"));
        }

        @Test
        void lookup_elementsOfMoreThanOneClass_isRefused() {
            // The schema is read off the first element present, and an element of another class need
            // not carry the property it names at all.
            Object[] mixed = { ALPHA_ONE, new Bare("b"), BETA_ONE };
            IndexCache<Row> cache = IndexCache.over(mixed);

            assertNull(cache.lookup(List.of(PropertyReference.of(BY_MODE)), List.of(BY_MODE), Collections.singletonList("alpha")));
        }

        @Test
        void lookup_uniqueIndexOverDuplicateValues_throws() {
            Row first = new Row("alpha", "SAME", 1, "first");
            Row second = new Row("beta", "SAME", 2, "second");

            IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> lookup(cacheOf(first, second), BY_CODE, "SAME")
            );

            assertTrue(thrown.getMessage().contains("unique"));
        }

    }

    @Nested
    class Mixed {

        @Test
        void lookup_extractorRaisingSomethingElse_isRefused() {
            // A first-match scan answers ALPHA_ONE without ever reaching the detached element, so a
            // build that let its exception out would throw where the scan answers.
            Detached detached = new Detached();
            IndexCache<Row> cache = cacheOf(ALPHA_ONE, detached, ALPHA_TWO);

            assertNull(assertDoesNotThrow(() -> lookup(cache, BY_MODE, "alpha")));
            assertNull(assertDoesNotThrow(() -> lookup(cache, BY_MODE, "alpha")));

            // The refusal is held for the snapshot, so the second query never reads it again.
            assertEquals(1, detached.reads);
        }

        @Test
        void lookupContaining_extractorRaisingSomethingElse_isRefused() {
            IndexCache<Tagged> cache = IndexCache.over(new Tagged[] { new Tagged(List.of("a", "b")), new BrokenTags() });

            assertNull(assertDoesNotThrow(() -> containing(cache, "a")));
            assertNull(assertDoesNotThrow(() -> containing(cache, "a")));
        }

        @Test
        void lookupContaining_listThatCannotBeWalked_isRefused() {
            IndexCache<Tagged> cache = IndexCache.over(new Tagged[] { new Tagged(List.of("a", "b")), new Tagged(unwalkable()) });

            assertNull(assertDoesNotThrow(() -> containing(cache, "a")));
            assertNull(assertDoesNotThrow(() -> containing(cache, "a")));
        }

        @Test
        void lookup_keyThatCannotBeHashed_isRefused() {
            // The scan compares with equals and never hashes, so it answers the first element and
            // passes over the second - where filing the second one's key would throw.
            IndexCache<Referencing> cache = IndexCache.over(new Referencing[] {
                new Referencing("a"),
                new Referencing(new Unhashable("b"))
            });

            assertNull(assertDoesNotThrow(() -> cache.lookup(PropertyReference.of(BY_TARGET), BY_TARGET, "a")));
        }

        @Test
        void lookup_duplicateUniqueValuesAheadOfAnUnreadableElement_stillThrows() {
            // A broken promise is the collection's to report, so a refusal over a later element must
            // not swallow it.
            Row first = new Row("alpha", "SAME", 1, "first");
            Row second = new Row("beta", "SAME", 2, "second");

            IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> lookup(cacheOf(first, second, new Detached()), BY_CODE, "SAME")
            );

            assertTrue(thrown.getMessage().contains("unique"));
        }

    }

}
