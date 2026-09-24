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
     * Stand-in for a runtime proxy of a row: a subclass declaring nothing of its own, whose
     * unannotated overrides hand every read to the row it stands for.
     */
    static class RowProxy extends Row {

        private final Row target;

        RowProxy(Row target) {
            super(null, null, 0, null);
            this.target = target;
        }

        @Override
        String getMode() {
            return this.target.getMode();
        }

        @Override
        String getCode() {
            return this.target.getCode();
        }

        @Override
        String getGroupedMode() {
            return this.target.getGroupedMode();
        }

        @Override
        int getTier() {
            return this.target.getTier();
        }

        @Override
        String getLabel() {
            return this.target.getLabel();
        }

    }

    /**
     * Supertype carrying every declaration its subclasses share - one plain index and one unique
     * index.
     */
    static class Animal {

        @Indexed
        private final String name;

        @Indexed(unique = true)
        private final String tag;

        Animal(String name, String tag) {
            this.name = name;
            this.tag = tag;
        }

        String getName() {
            return this.name;
        }

        String getTag() {
            return this.tag;
        }

    }

    /**
     * Subclass declaring nothing of its own.
     */
    static class Dog extends Animal {

        private final String breed;

        Dog(String name, String tag, String breed) {
            super(name, tag);
            this.breed = breed;
        }

        String getBreed() {
            return this.breed;
        }

    }

    /**
     * Sibling of {@link Dog}, declaring nothing of its own either.
     */
    static class Cat extends Animal {

        Cat(String name, String tag) {
            super(name, tag);
        }

    }

    /**
     * Subclass declaring an index of its own on an override.
     */
    static class Hound extends Dog {

        Hound(String name, String tag, String breed) {
            super(name, tag, breed);
        }

        @Override
        @Indexed
        String getBreed() {
            return super.getBreed();
        }

    }

    /**
     * Supertype declaring nothing.
     */
    static class Pet {

        private final String name;

        Pet(String name) {
            this.name = name;
        }

        String getName() {
            return this.name;
        }

    }

    /**
     * Subclass promising on an override that no two of its instances share a name.
     */
    static class UniqueDog extends Pet {

        UniqueDog(String name) {
            super(name);
        }

        @Override
        @Indexed(unique = true)
        String getName() {
            return super.getName();
        }

    }

    /**
     * Sibling of {@link UniqueDog}, declaring nothing.
     */
    static class PlainCat extends Pet {

        PlainCat(String name) {
            super(name);
        }

    }

    /**
     * Supertype holding one member of a unique group, whose other member only its subclass
     * declares.
     */
    static class SplitBase {

        @Indexed(group = "key", order = 0, unique = true)
        private final String region;

        SplitBase(String region) {
            this.region = region;
        }

        String getRegion() {
            return this.region;
        }

    }

    /**
     * Subclass declaring the rest of its supertype's group, so the whole key is seen from here
     * alone.
     */
    static class SplitSub extends SplitBase {

        @Indexed(group = "key", order = 1, unique = true)
        private final String code;

        SplitSub(String region, String code) {
            super(region);
            this.code = code;
        }

        String getCode() {
            return this.code;
        }

    }

    /**
     * Supertype promising a unique key over two fields.
     */
    static class NarrowKey {

        @Indexed(group = "key", order = 0, unique = true)
        private final int a;

        @Indexed(group = "key", order = 1, unique = true)
        private final int b;

        NarrowKey(int a, int b) {
            this.a = a;
            this.b = b;
        }

        int getA() {
            return this.a;
        }

        int getB() {
            return this.b;
        }

    }

    /**
     * Subclass widening its supertype's key with a third field, so it promises the wider key and
     * not the narrower one.
     */
    static class WideKey extends NarrowKey {

        @Indexed(group = "key", order = 2, unique = true)
        private final int c;

        WideKey(int a, int b, int c) {
            super(a, b);
            this.c = c;
        }

        int getC() {
            return this.c;
        }

    }

    /**
     * Subclass restating the widening field as an index of its own, which takes it out of the key
     * and leaves the two members above it behind.
     */
    static class RestatedWide extends WideKey {

        RestatedWide(int a, int b, int c) {
            super(a, b, c);
        }

        @Override
        @Indexed
        int getC() {
            return super.getC();
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
    private static final SearchFunction<Animal, String> BY_NAME = Animal::getName;
    private static final SearchFunction<Animal, String> BY_TAG = Animal::getTag;
    private static final SearchFunction<Pet, String> BY_PET_NAME = Pet::getName;
    private static final SearchFunction<SplitBase, String> BY_REGION = SplitBase::getRegion;
    private static final SearchFunction<NarrowKey, Integer> BY_A = NarrowKey::getA;
    private static final SearchFunction<NarrowKey, Integer> BY_B = NarrowKey::getB;
    private static final SearchFunction<RestatedWide, Integer> BY_RESTATED_C = RestatedWide::getC;

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
     * Probes one single-property index over any fixture.
     */
    private static <T> List<T> lookup(Object[] elements, SearchFunction<T, ?> extractor, Object value) {
        IndexCache<T> cache = IndexCache.over(elements);
        return cache.lookup(PropertyReference.of(extractor), extractor, value);
    }

    /**
     * Probes the two-field key over any mix of key fixtures.
     */
    private static List<NarrowKey> byKey(Object[] elements, int a, int b) {
        IndexCache<NarrowKey> cache = IndexCache.over(elements);
        return cache.lookup(List.of(PropertyReference.of(BY_A), PropertyReference.of(BY_B)), List.of(BY_A, BY_B), List.of(a, b));
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
            // Neither class extends the other, so no class some element has covers them all, and an
            // element of another class need not carry the property the query names at all.
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

        @Test
        void lookup_uniqueCompositeOverDuplicateKeys_throws() {
            // Both elements are of the class that promised the key, so the promise is broken.
            IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> byKey(new NarrowKey[] { new NarrowKey(1, 1), new NarrowKey(1, 1) }, 1, 1)
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

        @Test
        void lookup_proxyFirst_answersFromTheIndex() {
            // The proxy declares nothing of its own, so the row class it stands for is read in its
            // place and the plain rows behind it are filed beside it.
            RowProxy proxy = new RowProxy(ALPHA_ONE);
            List<Row> found = lookup(cacheOf(proxy, BETA_ONE, ALPHA_TWO), BY_MODE, "alpha");

            assertEquals(List.of(proxy, ALPHA_TWO), found);
            assertSame(proxy, found.getFirst());
        }

        @Test
        void lookup_proxyFirstComposite_answersFromTheIndex() {
            RowProxy proxy = new RowProxy(ALPHA_ONE);
            IndexCache<Row> cache = cacheOf(proxy, BETA_ONE, ALPHA_TWO);
            List<PropertyReference> references = List.of(PropertyReference.of(BY_GROUPED_MODE), PropertyReference.of(BY_TIER));
            List<SearchFunction<Row, ?>> extractors = List.of(BY_GROUPED_MODE, BY_TIER);

            assertEquals(List.of(proxy), cache.lookup(references, extractors, List.of("alpha", 1)));
            assertEquals(List.of(ALPHA_TWO), cache.lookup(references, extractors, List.of("alpha", 2)));
        }

        @Test
        void lookup_proxyFirstOverDuplicateUniqueValues_throwsNamingTheEntity() {
            // A plain row first throws over these values, so a proxy first throws the same and
            // names the class that made the promise rather than its own.
            Row first = new Row("alpha", "SAME", 1, "first");
            Row second = new Row("beta", "SAME", 2, "second");

            IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> lookup(cacheOf(new RowProxy(first), second), BY_CODE, "SAME")
            );

            assertTrue(thrown.getMessage().contains("'Row'"));
            assertFalse(thrown.getMessage().contains("RowProxy"));
        }

        @Test
        void lookup_everyProxy_answers() {
            RowProxy alpha = new RowProxy(ALPHA_ONE);
            RowProxy beta = new RowProxy(BETA_ONE);

            assertEquals(List.of(beta), lookup(cacheOf(alpha, beta), BY_MODE, "beta"));
        }

        @Test
        void lookup_proxyAheadOfAnotherClass_isStillRefused() {
            Object[] mixed = { new RowProxy(ALPHA_ONE), new Bare("b"), BETA_ONE };

            assertNull(lookup(mixed, BY_MODE, "alpha"));
        }

        @Test
        void lookup_subclassFirstWithItsSuperclassPresent_answers() {
            Dog dog = new Dog("rex", "T1", "beagle");
            Cat cat = new Cat("tom", "T2");
            Animal animal = new Animal("rex", "T3");

            assertEquals(List.of(dog, animal), lookup(new Animal[] { dog, cat, animal }, BY_NAME, "rex"));
        }

        @Test
        void lookup_siblingsWithNoInstanceOfTheirSuperclass_isRefused() {
            // Reading the superclass neither element has would enforce its unique tag across two
            // classes that no order of these elements enforces it on.
            Dog dog = new Dog("rex", "SAME", "beagle");
            Cat cat = new Cat("rex", "SAME");

            assertNull(assertDoesNotThrow(() -> lookup(new Animal[] { dog, cat }, BY_NAME, "rex")));
            assertNull(assertDoesNotThrow(() -> lookup(new Animal[] { cat, dog }, BY_NAME, "rex")));
            assertNull(assertDoesNotThrow(() -> lookup(new Animal[] { dog, cat }, BY_TAG, "SAME")));
            assertNull(assertDoesNotThrow(() -> lookup(new Animal[] { cat, dog }, BY_TAG, "SAME")));
        }

        @Test
        void lookup_subclassDeclaringItsOwnAheadOfItsSuperclass_isRefused() {
            // The hound reads a declaration the animal does not, so nothing stands in for it.
            Hound hound = new Hound("rex", "T1", "bloodhound");
            Animal animal = new Animal("rex", "T2");

            assertNull(assertDoesNotThrow(() -> lookup(new Animal[] { hound, animal }, BY_NAME, "rex")));
        }

        @Test
        void lookup_uniqueOnASubclass_isNotEnforcedOnASibling() {
            UniqueDog dog = new UniqueDog("rex");
            PlainCat cat = new PlainCat("rex");

            assertNull(assertDoesNotThrow(() -> lookup(new Pet[] { dog, cat }, BY_PET_NAME, "rex")));
            assertNull(assertDoesNotThrow(() -> lookup(new Pet[] { cat, dog }, BY_PET_NAME, "rex")));
        }

        @Test
        void lookup_castPastTheDeclaringClass_isRefusedBeforeItRuns() {
            // The extractor casts, so applying it to the cat would throw. The scan stops at the
            // hound, so the index has to refuse before it ever applies the extractor.
            SearchFunction<Animal, String> byBreed = element -> ((Hound) element).getBreed();
            Hound hound = new Hound("rex", "T1", "bloodhound");
            Cat cat = new Cat("tom", "T2");
            Animal animal = new Animal("sam", "T3");

            assertNull(assertDoesNotThrow(() -> lookup(new Animal[] { hound, cat }, byBreed, "bloodhound")));
            assertNull(assertDoesNotThrow(() -> lookup(new Animal[] { cat, hound, animal }, byBreed, "bloodhound")));
        }

        @Test
        void lookup_groupSplitAcrossTheHierarchy_scansWithoutThrowing() {
            // From the supertype the key is one value, which is no composite. Read as an index of
            // its own it would promise that no two elements share a region, which nobody wrote,
            // and the scan answers both of these.
            SplitBase base = new SplitBase("x");
            SplitSub sub = new SplitSub("x", "c1");

            assertNull(assertDoesNotThrow(() -> lookup(new SplitBase[] { base, sub }, BY_REGION, "x")));
            assertSame(IndexCache.none(), IndexCache.over(new SplitBase[] { base, sub }));

            // Led by the subclass, the region alone is half of a composite, which names no index.
            assertNull(assertDoesNotThrow(() -> lookup(new SplitBase[] { sub, base }, BY_REGION, "x")));
        }

        @Test
        void lookup_keyASubclassWidened_scansWhereOnlyOneElementPromisedIt() {
            // Read from the supertype the key is (a, b), and the subclass promises (a, b, c) and
            // nothing narrower, so sharing (a, b) with it breaks no promise it made. The scan
            // answers both.
            NarrowKey narrow = new NarrowKey(1, 1);
            WideKey wide = new WideKey(1, 1, 2);

            assertNull(assertDoesNotThrow(() -> byKey(new NarrowKey[] { narrow, wide }, 1, 1)));
        }

        @Test
        void lookup_keyASubclassWidened_answersWhileNoKeyRepeats() {
            NarrowKey narrow = new NarrowKey(1, 1);
            WideKey wide = new WideKey(1, 2, 3);
            NarrowKey[] elements = { narrow, wide };

            assertEquals(List.of(narrow), byKey(elements, 1, 1));
            assertEquals(List.of(wide), byKey(elements, 1, 2));
        }

        @Test
        void lookup_groupAnOverrideTookAMemberFrom_declaresNoKeyToBreak() {
            // The override restates c on its own, so the key is broken on this class and the two
            // members left behind promise nothing - two elements sharing them keep every promise.
            RestatedWide first = new RestatedWide(1, 1, 2);
            RestatedWide second = new RestatedWide(1, 1, 3);
            RestatedWide[] elements = { first, second };

            assertNull(assertDoesNotThrow(() -> byKey(elements, 1, 1)));
            assertEquals(List.of(first), lookup(elements, BY_RESTATED_C, 2));
        }

    }

}
