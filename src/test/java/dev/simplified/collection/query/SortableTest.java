package dev.simplified.collection.query;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.tuple.pair.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Sortable}, exercising every public default overload across the
 * {@code containsFirst}, {@code findFirst}, {@code findLast}, {@code matchFirst}, and
 * {@code matchLast} families plus their {@code *OrNull} unwrapping variants. Verifies that
 * First/Last differ on multi-match queries, that null-field NPEs from extractors are silently
 * filtered (per the implementation's NPE-swallow), and that {@code *OrNull} variants correctly
 * unwrap the {@link Optional}-returning equivalents.
 */
class SortableTest {

    /** Test fixture record used as the sortable element type. */
    record Person(int id, String name, List<String> tags, Department department) {

        Person(int id, String name, List<String> tags) {
            this(id, name, tags, null);
        }

    }

    /** Nested fixture record. */
    record Department(String name) {}

    private ConcurrentList<Person> people;

    private static final SearchFunction<Person, String> NAME = Person::name;
    private static final SearchFunction<Person, Integer> ID = Person::id;
    private static final SearchFunction<Person, List<String>> TAGS = Person::tags;
    private static final SearchFunction<Person, Department> DEPT = Person::department;

    @BeforeEach
    void setup() {
        people = Concurrent.newList();
        people.add(new Person(1, "alice", List.of("admin", "ops"), new Department("eng")));
        people.add(new Person(2, "bob", List.of("ops"), new Department("eng")));
        people.add(new Person(3, "alice", List.of("admin"), new Department("sales")));
        people.add(new Person(4, "carol", List.of("guest"), new Department("sales")));
        people.add(new Person(5, "dave", List.of("admin", "ops", "guest"), new Department("eng")));
        people.add(new Person(6, "alice", List.of("guest"), new Department("eng")));
        people.add(new Person(7, null, List.of("ops"), null));
        people.add(new Person(8, "eve", null, new Department("sales")));
        people.add(new Person(9, "bob", List.of("admin"), new Department("eng")));
    }

    @Nested
    class ContainsFirst {

        @Test
        void containsFirst_function_value_defaultsToAll() {
            Optional<Person> result = people.containsFirst(TAGS, "admin");
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void containsFirst_match_function_value_all() {
            Optional<Person> result = people.containsFirst(SearchFunction.Match.ALL, TAGS, "guest");
            assertTrue(result.isPresent());
            assertEquals(4, result.get().id());
        }

        @Test
        void containsFirst_match_function_value_any() {
            Optional<Person> result = people.containsFirst(SearchFunction.Match.ANY, TAGS, "ops");
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void containsFirst_varargsPairs_defaultsToAll() {
            Optional<Person> result = people.containsFirst(
                Pair.of(TAGS, "admin"),
                Pair.of(TAGS, "ops")
            );
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void containsFirst_iterablePairs_defaultsToAll() {
            Optional<Person> result = people.containsFirst(
                List.of(Pair.of(TAGS, "admin"), Pair.of(TAGS, "guest"))
            );
            assertTrue(result.isPresent());
            assertEquals(5, result.get().id()); // dave is the only one with both
        }

        @SuppressWarnings("unchecked")
        @Test
        void containsFirst_match_varargs_all() {
            Optional<Person> result = people.containsFirst(
                SearchFunction.Match.ALL,
                Pair.of(TAGS, "admin"),
                Pair.of(TAGS, "ops")
            );
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void containsFirst_match_varargs_any() {
            Optional<Person> result = people.containsFirst(
                SearchFunction.Match.ANY,
                Pair.of(TAGS, "admin"),
                Pair.of(TAGS, "guest")
            );
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void containsFirst_match_iterable_all() {
            Optional<Person> result = people.containsFirst(
                SearchFunction.Match.ALL,
                List.of(Pair.of(TAGS, "guest"))
            );
            assertTrue(result.isPresent());
            assertEquals(4, result.get().id());
        }

        @Test
        void containsFirst_match_iterable_any() {
            Optional<Person> result = people.containsFirst(
                SearchFunction.Match.ANY,
                List.of(Pair.of(TAGS, "admin"), Pair.of(TAGS, "guest"))
            );
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void containsFirst_noMatch_returnsEmpty() {
            assertTrue(people.containsFirst(TAGS, "missing").isEmpty());
        }

        @Test
        void containsFirst_returnsOptionalNotNull() {
            Optional<Person> result = people.containsFirst(TAGS, "missing");
            assertNotNull(result);
        }
    }

    @Nested
    class ContainsFirstOrNull {

        @Test
        void containsFirstOrNull_searchFunction_value_present() {
            Person p = people.containsFirstOrNull(TAGS, "admin");
            assertNotNull(p);
            assertEquals(1, p.id());
        }

        @Test
        void containsFirstOrNull_searchFunction_value_absent() {
            assertNull(people.containsFirstOrNull(TAGS, "missing"));
        }

        @Test
        void containsFirstOrNull_match_function_value_present() {
            Person p = people.containsFirstOrNull(SearchFunction.Match.ANY, (Function<Person, List<String>>) TAGS, "ops");
            assertNotNull(p);
            assertEquals(1, p.id());
        }

        @Test
        void containsFirstOrNull_match_function_value_absent() {
            Person p = people.containsFirstOrNull(SearchFunction.Match.ALL, (Function<Person, List<String>>) TAGS, "missing");
            assertNull(p);
        }

        @SuppressWarnings("unchecked")
        @Test
        void containsFirstOrNull_match_varargs_present() {
            Person p = people.containsFirstOrNull(
                SearchFunction.Match.ALL,
                Pair.of(TAGS, "admin"),
                Pair.of(TAGS, "ops")
            );
            assertNotNull(p);
            assertEquals(1, p.id());
        }

        @Test
        void containsFirstOrNull_match_iterable_present() {
            Person p = people.containsFirstOrNull(
                SearchFunction.Match.ANY,
                List.of(Pair.of(TAGS, "admin"))
            );
            assertNotNull(p);
            assertEquals(1, p.id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void containsFirstOrNull_varargsPairs_present() {
            Person p = people.containsFirstOrNull(
                Pair.of(TAGS, "admin"),
                Pair.of(TAGS, "ops")
            );
            assertNotNull(p);
            assertEquals(1, p.id());
        }

        @Test
        void containsFirstOrNull_iterablePairs_absent() {
            // Upcast to Sortable to disambiguate from List#containsAll(Collection<?>)
            Sortable<Person> sortable = people;
            Person p = sortable.containsFirstOrNull(
                List.of(Pair.of(TAGS, "missing"))
            );
            assertNull(p);
        }
    }

    @Nested
    class FindFirst {

        @Test
        void findFirst_function_value_defaultsToAll() {
            Optional<Person> result = people.findFirst(NAME, "alice");
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void findFirst_match_function_value_all() {
            Optional<Person> result = people.findFirst(SearchFunction.Match.ALL, NAME, "bob");
            assertTrue(result.isPresent());
            assertEquals(2, result.get().id());
        }

        @Test
        void findFirst_match_function_value_any() {
            Optional<Person> result = people.findFirst(SearchFunction.Match.ANY, NAME, "bob");
            assertTrue(result.isPresent());
            assertEquals(2, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findFirst_varargsPairs_defaultsToAll() {
            Optional<Person> result = people.findFirst(Pair.of(NAME, "alice"));
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void findFirst_iterablePairs_defaultsToAll() {
            Optional<Person> result = people.findFirst(
                List.<Pair<Function<Person, String>, String>>of(Pair.of(NAME, "bob"))
            );
            assertTrue(result.isPresent());
            assertEquals(2, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findFirst_match_varargs_all_unmatchable() {
            Optional<Person> result = people.findFirst(
                SearchFunction.Match.ALL,
                Pair.of(NAME, "alice"),
                Pair.of(NAME, "bob")
            );
            assertTrue(result.isEmpty());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findFirst_match_varargs_any_unionsResults() {
            Optional<Person> result = people.findFirst(
                SearchFunction.Match.ANY,
                Pair.of(NAME, "carol"),
                Pair.of(NAME, "alice")
            );
            assertTrue(result.isPresent());
            // First in fixture order: alice id=1
            assertEquals(1, result.get().id());
        }

        @Test
        void findFirst_match_iterable_all() {
            Optional<Person> result = people.findFirst(
                SearchFunction.Match.ALL,
                List.of(Pair.of(NAME, "alice"))
            );
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void findFirst_match_iterable_any() {
            Optional<Person> result = people.findFirst(
                SearchFunction.Match.ANY,
                List.of(Pair.of(NAME, "carol"), Pair.of(NAME, "dave"))
            );
            assertTrue(result.isPresent());
            assertEquals(4, result.get().id()); // carol comes before dave in fixture
        }

        @Test
        void findFirst_noMatch_returnsEmpty() {
            assertTrue(people.findFirst(NAME, "zzz").isEmpty());
        }

        @Test
        void findFirst_swallowsNpeFromExtractor() {
            // findFirst's compare lambda catches NPE from the extractor; null-name elements
            // simply do not match instead of propagating NPE.
            Function<Person, Integer> npeExtractor = p -> p.name().length();
            Optional<Person> result = people.findFirst(npeExtractor, 5);
            // alice has length 5; first alice is id=1
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void findFirst_matchesNullField() {
            // Person 7 has null name - findFirst uses Objects.equals which is null-safe
            Optional<Person> result = people.findFirst(NAME, (String) null);
            assertTrue(result.isPresent());
            assertEquals(7, result.get().id());
        }
    }

    @Nested
    class FindFirstOrNull {

        @Test
        void findFirstOrNull_searchFunction_value_present() {
            Person p = people.findFirstOrNull(NAME, "alice");
            assertNotNull(p);
            assertEquals(1, p.id());
        }

        @Test
        void findFirstOrNull_searchFunction_value_absent() {
            assertNull(people.findFirstOrNull(NAME, "zzz"));
        }

        @Test
        void findFirstOrNull_match_function_value_present() {
            Person p = people.findFirstOrNull(SearchFunction.Match.ANY, (Function<Person, String>) NAME, "alice");
            assertNotNull(p);
            assertEquals(1, p.id());
        }

        @Test
        void findFirstOrNull_match_function_value_absent() {
            assertNull(people.findFirstOrNull(SearchFunction.Match.ALL, (Function<Person, String>) NAME, "zzz"));
        }

        @SuppressWarnings("unchecked")
        @Test
        void findFirstOrNull_match_varargs_present() {
            Person p = people.findFirstOrNull(
                SearchFunction.Match.ANY,
                Pair.of(NAME, "carol"),
                Pair.of(NAME, "dave")
            );
            assertNotNull(p);
            assertEquals(4, p.id());
        }

        @Test
        void findFirstOrNull_match_iterable_present() {
            Person p = people.findFirstOrNull(
                SearchFunction.Match.ALL,
                List.of(Pair.of(NAME, "alice"))
            );
            assertNotNull(p);
            assertEquals(1, p.id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findFirstOrNull_varargsPairs_present() {
            Person p = people.findFirstOrNull(Pair.of(NAME, "bob"));
            assertNotNull(p);
            assertEquals(2, p.id());
        }

        @Test
        void findFirstOrNull_iterablePairs_absent() {
            assertNull(people.findFirstOrNull(
                List.<Pair<Function<Person, String>, String>>of(Pair.of(NAME, "zzz"))
            ));
        }
    }

    @Nested
    class FindLast {

        @Test
        void findLast_function_value_defaultsToAll() {
            Optional<Person> result = people.findLast(NAME, "alice");
            assertTrue(result.isPresent());
            // last alice = id 6
            assertEquals(6, result.get().id());
        }

        @Test
        void findLast_match_function_value_all() {
            Optional<Person> result = people.findLast(SearchFunction.Match.ALL, NAME, "bob");
            assertTrue(result.isPresent());
            assertEquals(9, result.get().id());
        }

        @Test
        void findLast_match_function_value_any() {
            Optional<Person> result = people.findLast(SearchFunction.Match.ANY, NAME, "alice");
            assertTrue(result.isPresent());
            assertEquals(6, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findLast_varargsPairs_defaultsToAll() {
            Optional<Person> result = people.findLast(Pair.of(NAME, "alice"));
            assertTrue(result.isPresent());
            assertEquals(6, result.get().id());
        }

        @Test
        void findLast_iterablePairs_defaultsToAll() {
            Optional<Person> result = people.findLast(
                List.<Pair<Function<Person, String>, String>>of(Pair.of(NAME, "bob"))
            );
            assertTrue(result.isPresent());
            assertEquals(9, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findLast_match_varargs_all_unmatchable() {
            Optional<Person> result = people.findLast(
                SearchFunction.Match.ALL,
                Pair.of(NAME, "alice"),
                Pair.of(NAME, "bob")
            );
            assertTrue(result.isEmpty());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findLast_match_varargs_any() {
            Optional<Person> result = people.findLast(
                SearchFunction.Match.ANY,
                Pair.of(NAME, "alice"),
                Pair.of(NAME, "bob")
            );
            assertTrue(result.isPresent());
            // last among {1,2,3,6,9} that match alice or bob => id 9 (bob)
            assertEquals(9, result.get().id());
        }

        @Test
        void findLast_match_iterable_all() {
            Optional<Person> result = people.findLast(
                SearchFunction.Match.ALL,
                List.of(Pair.of(NAME, "alice"))
            );
            assertTrue(result.isPresent());
            assertEquals(6, result.get().id());
        }

        @Test
        void findLast_match_iterable_any() {
            Optional<Person> result = people.findLast(
                SearchFunction.Match.ANY,
                List.of(Pair.of(NAME, "carol"), Pair.of(NAME, "dave"))
            );
            assertTrue(result.isPresent());
            assertEquals(5, result.get().id()); // dave appears after carol
        }

        @Test
        void findLast_noMatch_returnsEmpty() {
            assertTrue(people.findLast(NAME, "zzz").isEmpty());
        }

        @Test
        void findLast_differsFromFindFirstOnMultiMatch() {
            int first = people.findFirst(NAME, "alice").orElseThrow().id();
            int last = people.findLast(NAME, "alice").orElseThrow().id();
            assertNotEquals(first, last);
            assertEquals(1, first);
            assertEquals(6, last);
        }

        @Test
        void findLast_swallowsNpeFromExtractor() {
            Function<Person, Integer> npeExtractor = p -> p.name().length();
            // alice = 5 letters; last alice id=6
            Optional<Person> result = people.findLast(npeExtractor, 5);
            assertTrue(result.isPresent());
            assertEquals(6, result.get().id());
        }
    }

    @Nested
    class FindLastOrNull {

        @Test
        void findLastOrNull_searchFunction_value_present() {
            Person p = people.findLastOrNull(NAME, "alice");
            assertNotNull(p);
            assertEquals(6, p.id());
        }

        @Test
        void findLastOrNull_searchFunction_value_absent() {
            assertNull(people.findLastOrNull(NAME, "zzz"));
        }

        @Test
        void findLastOrNull_match_function_value_present() {
            Person p = people.findLastOrNull(SearchFunction.Match.ALL, (Function<Person, String>) NAME, "bob");
            assertNotNull(p);
            assertEquals(9, p.id());
        }

        @Test
        void findLastOrNull_match_function_value_absent() {
            assertNull(people.findLastOrNull(SearchFunction.Match.ANY, (Function<Person, String>) NAME, "zzz"));
        }

        @SuppressWarnings("unchecked")
        @Test
        void findLastOrNull_match_varargs_present() {
            Person p = people.findLastOrNull(
                SearchFunction.Match.ALL,
                Pair.of(NAME, "alice")
            );
            assertNotNull(p);
            assertEquals(6, p.id());
        }

        @Test
        void findLastOrNull_match_iterable_present() {
            Person p = people.findLastOrNull(
                SearchFunction.Match.ANY,
                List.of(Pair.of(NAME, "alice"), Pair.of(NAME, "bob"))
            );
            assertNotNull(p);
            assertEquals(9, p.id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findLastOrNull_varargsPairs_present() {
            Person p = people.findLastOrNull(Pair.of(NAME, "alice"));
            assertNotNull(p);
            assertEquals(6, p.id());
        }

        @Test
        void findLastOrNull_iterablePairs_absent() {
            assertNull(people.findLastOrNull(
                List.<Pair<Function<Person, String>, String>>of(Pair.of(NAME, "zzz"))
            ));
        }
    }

    @Nested
    class MatchFirst {

        @SuppressWarnings("unchecked")
        @Test
        void matchFirst_varargs_defaultsToAll() {
            Optional<Person> result = people.matchFirst(p -> p.id() > 3);
            assertTrue(result.isPresent());
            assertEquals(4, result.get().id());
        }

        @Test
        void matchFirst_iterable_defaultsToAll() {
            List<Predicate<Person>> preds = List.of(p -> p.id() > 3);
            Optional<Person> result = people.matchFirst(preds);
            assertTrue(result.isPresent());
            assertEquals(4, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchFirst_match_varargs_all() {
            Optional<Person> result = people.matchFirst(
                SearchFunction.Match.ALL,
                p -> p.id() > 3,
                p -> "alice".equals(p.name())
            );
            assertTrue(result.isPresent());
            // alice with id > 3 -> id=6
            assertEquals(6, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchFirst_match_varargs_any() {
            Optional<Person> result = people.matchFirst(
                SearchFunction.Match.ANY,
                p -> p.id() > 7,
                p -> "alice".equals(p.name())
            );
            assertTrue(result.isPresent());
            // alice id=1 satisfies one branch
            assertEquals(1, result.get().id());
        }

        @Test
        void matchFirst_match_iterable_all() {
            Optional<Person> result = people.matchFirst(
                SearchFunction.Match.ALL,
                List.of(p -> p.id() > 5)
            );
            assertTrue(result.isPresent());
            assertEquals(6, result.get().id());
        }

        @Test
        void matchFirst_match_iterable_any() {
            Optional<Person> result = people.matchFirst(
                SearchFunction.Match.ANY,
                List.of(p -> p.id() == 5, p -> p.id() == 9)
            );
            assertTrue(result.isPresent());
            assertEquals(5, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchFirst_swallowsNpeFromPredicate() {
            // matchFirst's compare lambda catches NPE; null-tag elements are filtered out.
            Predicate<Person> hasOpsTag = p -> p.tags().contains("ops");
            Optional<Person> result = people.matchFirst(hasOpsTag);
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id()); // first with "ops"
        }

        @Test
        void matchFirst_noMatch_returnsEmpty() {
            assertTrue(people.matchFirst(p -> p.id() > 1000).isEmpty());
        }
    }

    @Nested
    class MatchFirstOrNull {

        @SuppressWarnings("unchecked")
        @Test
        void matchFirstOrNull_varargs_present() {
            Person p = people.matchFirstOrNull(person -> person.id() == 5);
            assertNotNull(p);
            assertEquals(5, p.id());
        }

        @Test
        void matchFirstOrNull_iterable_present() {
            Person p = people.matchFirstOrNull(List.<Predicate<Person>>of(person -> person.id() == 9));
            assertNotNull(p);
            assertEquals(9, p.id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchFirstOrNull_match_varargs_present() {
            Person p = people.matchFirstOrNull(
                SearchFunction.Match.ANY,
                person -> "carol".equals(person.name()),
                person -> person.id() == 9
            );
            assertNotNull(p);
            assertEquals(4, p.id());
        }

        @Test
        void matchFirstOrNull_match_iterable_absent() {
            Person p = people.matchFirstOrNull(
                SearchFunction.Match.ALL,
                List.<Predicate<Person>>of(person -> person.id() > 1000)
            );
            assertNull(p);
        }
    }

    @Nested
    class MatchLast {

        @SuppressWarnings("unchecked")
        @Test
        void matchLast_varargs_defaultsToAll() {
            Optional<Person> result = people.matchLast(p -> "alice".equals(p.name()));
            assertTrue(result.isPresent());
            assertEquals(6, result.get().id());
        }

        @Test
        void matchLast_iterable_defaultsToAll() {
            List<Predicate<Person>> preds = List.of(p -> "bob".equals(p.name()));
            Optional<Person> result = people.matchLast(preds);
            assertTrue(result.isPresent());
            assertEquals(9, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchLast_match_varargs_all() {
            Optional<Person> result = people.matchLast(
                SearchFunction.Match.ALL,
                p -> "alice".equals(p.name()),
                p -> p.id() > 3
            );
            assertTrue(result.isPresent());
            assertEquals(6, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchLast_match_varargs_any() {
            Optional<Person> result = people.matchLast(
                SearchFunction.Match.ANY,
                p -> "alice".equals(p.name()),
                p -> "bob".equals(p.name())
            );
            assertTrue(result.isPresent());
            assertEquals(9, result.get().id());
        }

        @Test
        void matchLast_match_iterable_all() {
            Optional<Person> result = people.matchLast(
                SearchFunction.Match.ALL,
                List.of(p -> p.id() < 5)
            );
            assertTrue(result.isPresent());
            assertEquals(4, result.get().id());
        }

        @Test
        void matchLast_match_iterable_any() {
            Optional<Person> result = people.matchLast(
                SearchFunction.Match.ANY,
                List.of(p -> p.id() == 1, p -> p.id() == 5)
            );
            assertTrue(result.isPresent());
            assertEquals(5, result.get().id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchLast_swallowsNpeFromPredicate() {
            Predicate<Person> hasOpsTag = p -> p.tags().contains("ops");
            // ops-tagged: 1, 2, 5, 7 - last is id 7
            Optional<Person> result = people.matchLast(hasOpsTag);
            assertTrue(result.isPresent());
            assertEquals(7, result.get().id());
        }

        @Test
        void matchLast_differsFromMatchFirstOnMultiMatch() {
            int first = people.matchFirst(p -> "alice".equals(p.name())).orElseThrow().id();
            int last = people.matchLast(p -> "alice".equals(p.name())).orElseThrow().id();
            assertNotEquals(first, last);
            assertEquals(1, first);
            assertEquals(6, last);
        }

        @Test
        void matchLast_noMatch_returnsEmpty() {
            assertTrue(people.matchLast(p -> p.id() > 1000).isEmpty());
        }
    }

    @Nested
    class MatchLastOrNull {

        @SuppressWarnings("unchecked")
        @Test
        void matchLastOrNull_varargs_present() {
            Person p = people.matchLastOrNull(person -> "alice".equals(person.name()));
            assertNotNull(p);
            assertEquals(6, p.id());
        }

        @Test
        void matchLastOrNull_iterable_present() {
            Person p = people.matchLastOrNull(List.<Predicate<Person>>of(person -> "bob".equals(person.name())));
            assertNotNull(p);
            assertEquals(9, p.id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchLastOrNull_match_varargs_present() {
            Person p = people.matchLastOrNull(
                SearchFunction.Match.ANY,
                person -> "alice".equals(person.name()),
                person -> "bob".equals(person.name())
            );
            assertNotNull(p);
            assertEquals(9, p.id());
        }

        @Test
        void matchLastOrNull_match_iterable_absent() {
            Person p = people.matchLastOrNull(
                SearchFunction.Match.ALL,
                List.<Predicate<Person>>of(person -> person.id() > 1000)
            );
            assertNull(p);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void emptyFixture_findFirst_returnsEmpty() {
            ConcurrentList<Person> empty = Concurrent.newList();
            assertTrue(empty.findFirst(NAME, "alice").isEmpty());
        }

        @Test
        void emptyFixture_findLast_returnsEmpty() {
            ConcurrentList<Person> empty = Concurrent.newList();
            assertTrue(empty.findLast(NAME, "alice").isEmpty());
        }

        @Test
        void emptyFixture_containsFirst_returnsEmpty() {
            ConcurrentList<Person> empty = Concurrent.newList();
            assertTrue(empty.containsFirst(TAGS, "admin").isEmpty());
        }

        @Test
        void emptyFixture_matchFirst_returnsEmpty() {
            ConcurrentList<Person> empty = Concurrent.newList();
            assertTrue(empty.matchFirst(p -> true).isEmpty());
        }

        @Test
        void emptyFixture_matchLast_returnsEmpty() {
            ConcurrentList<Person> empty = Concurrent.newList();
            assertTrue(empty.matchLast(p -> true).isEmpty());
        }

        @Test
        void findFirst_combinedNestedExtractor_swallowsNpeOnNullIntermediate() {
            // Sortable.findFirst catches NPE - person 7's null department should be filtered, not throw.
            SearchFunction<Person, String> deptName = SearchFunction.combine(DEPT, Department::name);
            Optional<Person> result = people.findFirst(deptName, "eng");
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void findLast_combinedNestedExtractor_swallowsNpeOnNullIntermediate() {
            SearchFunction<Person, String> deptName = SearchFunction.combine(DEPT, Department::name);
            Optional<Person> result = people.findLast(deptName, "eng");
            assertTrue(result.isPresent());
            assertEquals(9, result.get().id());
        }

        @Test
        void containsFirst_filtersNullList() {
            // Person 8 has null tags - the containsFirst pipeline checks list != null
            Optional<Person> result = people.containsFirst(TAGS, "ops");
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id()); // first ops-tagged
        }

        @Test
        void findFirst_matchesNullValueAgainstNullField() {
            // Searchable's null-safe Objects.equals matches null == null
            Optional<Person> result = people.findFirst(NAME, (String) null);
            assertTrue(result.isPresent());
            assertEquals(7, result.get().id());
        }

        @Test
        void findLast_matchesNullValueAgainstNullField() {
            Optional<Person> result = people.findLast(NAME, (String) null);
            assertTrue(result.isPresent());
            assertEquals(7, result.get().id());
        }

        @Test
        void handRolledSortable_findFirstReturnsFirst() {
            // Narrow test using a hand-rolled Sortable lambda over a fixed source.
            List<Person> source = List.of(
                new Person(10, "x", List.of()),
                new Person(11, "y", List.of()),
                new Person(12, "x", List.of())
            );
            Sortable<Person> custom = () -> dev.simplified.collection.tuple.single.SingleStream.of(source);
            assertEquals(10, custom.findFirst(NAME, "x").orElseThrow().id());
            assertEquals(12, custom.findLast(NAME, "x").orElseThrow().id());
        }

        @Test
        void containsFirst_emptyPredicates_all_returnsFirstElement() {
            // ALL with empty predicates means every element passes - first is id 1
            Optional<Person> result = people.containsFirst(
                SearchFunction.Match.ALL,
                List.<Pair<Function<Person, List<String>>, String>>of()
            );
            assertTrue(result.isPresent());
            assertEquals(1, result.get().id());
        }

        @Test
        void findFirstOrNull_nullField_returnsMatch() {
            Person p = people.findFirstOrNull(NAME, (String) null);
            assertNotNull(p);
            assertEquals(7, p.id());
        }
    }
}
