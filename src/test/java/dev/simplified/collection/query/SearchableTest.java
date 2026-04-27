package dev.simplified.collection.query;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.function.TriPredicate;
import dev.simplified.collection.tuple.pair.Pair;
import dev.simplified.collection.tuple.single.SingleStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Searchable}, exercising every public default overload across
 * {@code compare}/{@code contains} engines and the {@code containsAll}/{@code findAll}/{@code matchAll}
 * families. A small {@link Person} fixture with overlapping names, ids, and tags is used so
 * {@link SearchFunction.Match#ALL} and {@link SearchFunction.Match#ANY} produce different results.
 */
class SearchableTest {

    /**
     * Test fixture record used as the searchable element type. Includes a department reference
     * so {@link SearchFunction#combine} can exercise nested-property traversal.
     */
    record Person(int id, String name, List<String> tags, Department department) {

        Person(int id, String name, List<String> tags) {
            this(id, name, tags, null);
        }

    }

    /** Nested fixture record used to exercise {@link SearchFunction#combine}. */
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
    class Compare {

        @Test
        void compare_all_returnsElementsMatchingEveryPredicate() {
            TriPredicate<Function<Person, String>, Person, String> eq =
                (f, p, v) -> Objects.equals(f.apply(p), v);
            List<Person> result = people.compare(
                SearchFunction.Match.ALL,
                eq,
                List.of(Pair.of(NAME, "alice"))
            ).toList();
            assertEquals(3, result.size());
            assertTrue(result.stream().allMatch(p -> "alice".equals(p.name())));
        }

        @Test
        void compare_any_returnsElementsMatchingAtLeastOnePredicate() {
            TriPredicate<Function<Person, String>, Person, String> eq =
                (f, p, v) -> Objects.equals(f.apply(p), v);
            List<Person> result = people.compare(
                SearchFunction.Match.ANY,
                eq,
                List.of(Pair.of(NAME, "alice"), Pair.of(NAME, "bob"))
            ).toList();
            assertEquals(5, result.size());
            assertTrue(result.stream().allMatch(p -> "alice".equals(p.name()) || "bob".equals(p.name())));
        }

        @Test
        void compare_all_intersectsPredicates() {
            // ALL with two predicates against the same single-valued field cannot match anything
            TriPredicate<Function<Person, String>, Person, String> eq =
                (f, p, v) -> Objects.equals(f.apply(p), v);
            List<Person> result = people.compare(
                SearchFunction.Match.ALL,
                eq,
                List.of(Pair.of(NAME, "alice"), Pair.of(NAME, "bob"))
            ).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        void compare_emptyPredicateIterable_all_returnsAll() {
            TriPredicate<Function<Person, String>, Person, String> eq =
                (f, p, v) -> Objects.equals(f.apply(p), v);
            List<Person> result = people.compare(
                SearchFunction.Match.ALL,
                eq,
                List.<Pair<Function<Person, String>, String>>of()
            ).toList();
            assertEquals(people.size(), result.size());
        }

        @Test
        void compare_emptyPredicateIterable_any_returnsEmpty() {
            // ANY with no predicates means no element passes the OR-fold of zero terms
            TriPredicate<Function<Person, String>, Person, String> eq =
                (f, p, v) -> Objects.equals(f.apply(p), v);
            List<Person> result = people.compare(
                SearchFunction.Match.ANY,
                eq,
                List.<Pair<Function<Person, String>, String>>of()
            ).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        void compare_returnsSingleStream() {
            TriPredicate<Function<Person, String>, Person, String> eq =
                (f, p, v) -> Objects.equals(f.apply(p), v);
            SingleStream<Person> stream = people.compare(
                SearchFunction.Match.ALL,
                eq,
                List.of(Pair.of(NAME, "alice"))
            );
            assertNotNull(stream);
            assertEquals(3L, stream.count());
        }

        // Note: cannot construct a third Match enum value, so the IllegalArgumentException
        // branch in compare(...) is unreachable from user code.
    }

    @Nested
    class Contains {

        @Test
        void contains_all_returnsElementsWhereListContainsValueOfEveryPredicate() {
            TriPredicate<Function<Person, List<String>>, Person, String> listContains =
                (f, p, v) -> {
                    List<String> list = f.apply(p);
                    return list != null && list.contains(v);
                };
            List<Person> result = people.contains(
                SearchFunction.Match.ALL,
                listContains,
                List.of(Pair.of(TAGS, "admin"), Pair.of(TAGS, "ops"))
            ).toList();
            // alice(1) has admin,ops; dave(5) has admin,ops,guest
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(p -> p.id() == 1 || p.id() == 5));
        }

        @Test
        void contains_any_returnsElementsWhereListContainsAtLeastOnePredicateValue() {
            TriPredicate<Function<Person, List<String>>, Person, String> listContains =
                (f, p, v) -> {
                    List<String> list = f.apply(p);
                    return list != null && list.contains(v);
                };
            List<Person> result = people.contains(
                SearchFunction.Match.ANY,
                listContains,
                List.of(Pair.of(TAGS, "admin"), Pair.of(TAGS, "guest"))
            ).toList();
            // admin: 1,3,5,9; guest: 4,5,6 -> union {1,3,4,5,6,9}
            assertEquals(6, result.size());
        }

        @Test
        void contains_skipsNullListsViaPredicate() {
            TriPredicate<Function<Person, List<String>>, Person, String> listContains =
                (f, p, v) -> {
                    List<String> list = f.apply(p);
                    return list != null && list.contains(v);
                };
            List<Person> result = people.contains(
                SearchFunction.Match.ANY,
                listContains,
                List.of(Pair.of(TAGS, "admin"))
            ).toList();
            assertFalse(result.stream().anyMatch(p -> p.id() == 8));
        }
    }

    @Nested
    class ContainsAll {

        @Test
        void containsAll_function_value_defaultsToAll() {
            Stream<Person> result = people.containsAll(TAGS, "admin");
            List<Person> list = result.toList();
            assertEquals(4, list.size());
            assertTrue(list.stream().allMatch(p -> p.tags() != null && p.tags().contains("admin")));
        }

        @SuppressWarnings("unchecked")
        @Test
        void containsAll_varargsPairs_defaultsToAll() {
            Stream<Person> result = people.containsAll(
                Pair.of(TAGS, "admin"),
                Pair.of(TAGS, "ops")
            );
            List<Person> list = result.toList();
            assertEquals(2, list.size());
        }

        @Test
        void containsAll_iterablePairs_defaultsToAll() {
            // Upcast to Searchable to disambiguate from List#containsAll(Collection<?>)
            Searchable<Person> searchable = people;
            Stream<Person> result = searchable.containsAll(
                List.of(Pair.of(TAGS, "admin"), Pair.of(TAGS, "ops"))
            );
            List<Person> list = result.toList();
            assertEquals(2, list.size());
        }

        @Test
        void containsAll_match_function_value_all() {
            List<Person> list = people.containsAll(SearchFunction.Match.ALL, TAGS, "ops").toList();
            assertEquals(4, list.size()); // 1, 2, 5, 7
        }

        @Test
        void containsAll_match_function_value_any() {
            List<Person> list = people.containsAll(SearchFunction.Match.ANY, TAGS, "guest").toList();
            assertEquals(3, list.size()); // 4, 5, 6
        }

        @SuppressWarnings("unchecked")
        @Test
        void containsAll_match_varargs_all() {
            List<Person> list = people.containsAll(
                SearchFunction.Match.ALL,
                Pair.of(TAGS, "admin"),
                Pair.of(TAGS, "guest")
            ).toList();
            // Only dave(5) has both
            assertEquals(1, list.size());
            assertEquals(5, list.get(0).id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void containsAll_match_varargs_any() {
            List<Person> list = people.containsAll(
                SearchFunction.Match.ANY,
                Pair.of(TAGS, "admin"),
                Pair.of(TAGS, "guest")
            ).toList();
            // admin: 1,3,5,9 + guest: 4,5,6 -> {1,3,4,5,6,9}
            assertEquals(6, list.size());
        }

        @Test
        void containsAll_match_iterable_all() {
            List<Person> list = people.containsAll(
                SearchFunction.Match.ALL,
                List.of(Pair.of(TAGS, "admin"), Pair.of(TAGS, "ops"))
            ).toList();
            assertEquals(2, list.size());
        }

        @Test
        void containsAll_match_iterable_any() {
            List<Person> list = people.containsAll(
                SearchFunction.Match.ANY,
                List.of(Pair.of(TAGS, "admin"), Pair.of(TAGS, "ops"))
            ).toList();
            // admin {1,3,5,9} U ops {1,2,5,7} = {1,2,3,5,7,9}
            assertEquals(6, list.size());
        }

        @Test
        void containsAll_returnsJdkStreamType() {
            // Per the interface contract, containsAll returns java.util.stream.Stream (not SingleStream)
            Stream<Person> stream = people.containsAll(TAGS, "admin");
            assertNotNull(stream);
            assertEquals(4L, stream.count());
        }

        @Test
        void containsAll_noMatch_returnsEmpty() {
            List<Person> list = people.containsAll(TAGS, "nonexistent").toList();
            assertTrue(list.isEmpty());
        }

        @Test
        void containsAll_filtersOutNullLists() {
            List<Person> list = people.containsAll(TAGS, "ops").toList();
            assertFalse(list.stream().anyMatch(p -> p.id() == 8));
        }
    }

    @Nested
    class FindAll {

        @Test
        void findAll_function_value_defaultsToAll() {
            List<Person> list = people.findAll(NAME, "alice").toList();
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(p -> "alice".equals(p.name())));
        }

        @SuppressWarnings("unchecked")
        @Test
        void findAll_varargsPairs_defaultsToAll() {
            // Two predicates against same field: ALL means equal to both - impossible
            List<Person> list = people.findAll(
                Pair.of(NAME, "alice"),
                Pair.of(NAME, "bob")
            ).toList();
            assertTrue(list.isEmpty());
        }

        @Test
        void findAll_iterablePairs_defaultsToAll() {
            List<Person> list = people.findAll(
                List.<Pair<Function<Person, String>, String>>of(Pair.of(NAME, "alice"))
            ).toList();
            assertEquals(3, list.size());
        }

        @Test
        void findAll_match_function_value_all() {
            List<Person> list = people.findAll(SearchFunction.Match.ALL, NAME, "bob").toList();
            assertEquals(2, list.size());
        }

        @Test
        void findAll_match_function_value_any() {
            List<Person> list = people.findAll(SearchFunction.Match.ANY, NAME, "carol").toList();
            assertEquals(1, list.size());
            assertEquals(4, list.get(0).id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findAll_match_varargs_all_singlePredicate() {
            List<Person> list = people.findAll(
                SearchFunction.Match.ALL,
                Pair.of(NAME, "alice")
            ).toList();
            assertEquals(3, list.size());
        }

        @SuppressWarnings("unchecked")
        @Test
        void findAll_match_varargs_any_unionsResults() {
            List<Person> list = people.findAll(
                SearchFunction.Match.ANY,
                Pair.of(NAME, "alice"),
                Pair.of(NAME, "bob")
            ).toList();
            assertEquals(5, list.size());
        }

        @Test
        void findAll_match_iterable_all() {
            List<Person> list = people.findAll(
                SearchFunction.Match.ALL,
                List.of(Pair.of(NAME, "alice"))
            ).toList();
            assertEquals(3, list.size());
        }

        @Test
        void findAll_match_iterable_any() {
            List<Person> list = people.findAll(
                SearchFunction.Match.ANY,
                List.of(Pair.of(NAME, "alice"), Pair.of(NAME, "carol"))
            ).toList();
            assertEquals(4, list.size());
        }

        @Test
        void findAll_returnsSingleStreamType() {
            SingleStream<Person> stream = people.findAll(NAME, "alice");
            assertNotNull(stream);
            assertEquals(3L, stream.count());
        }

        @Test
        void findAll_noMatch_returnsEmpty() {
            assertEquals(0L, people.findAll(NAME, "zzz").count());
        }

        @Test
        void findAll_matchesNullField() {
            // Person 7 has a null name - findAll uses Objects.equals which is null-safe
            List<Person> list = people.findAll(NAME, (String) null).toList();
            assertEquals(1, list.size());
            assertEquals(7, list.get(0).id());
        }

        @Test
        void findAll_combinedNestedExtractor() {
            // SearchFunction.combine traverses Person::department then Department::name.
            // Use a fixture without null departments since Searchable.findAll does not swallow NPE.
            ConcurrentList<Person> nonNullDepts = Concurrent.newList();
            for (Person p : people)
                if (p.department() != null) nonNullDepts.add(p);
            SearchFunction<Person, String> deptName = SearchFunction.combine(DEPT, Department::name);
            List<Person> list = nonNullDepts.findAll(deptName, "eng").toList();
            // 1, 2, 5, 6, 9 are eng
            assertEquals(5, list.size());
        }

        @Test
        void findAll_combinedNestedExtractor_propagatesNpeOnNullIntermediate() {
            SearchFunction<Person, String> deptName = SearchFunction.combine(DEPT, Department::name);
            // Person 7 has null department; combine -> Department::name applied to null throws NPE.
            // Searchable.findAll does not catch NPE; expect propagation.
            assertThrows(NullPointerException.class, () -> people.findAll(deptName, "sales").toList());
        }
    }

    @Nested
    class MatchAll {

        @SuppressWarnings("unchecked")
        @Test
        void matchAll_varargs_defaultsToAll() {
            Predicate<Person> idLowerHalf = p -> p.id() <= 5;
            Predicate<Person> nameAlice = p -> "alice".equals(p.name());
            List<Person> list = people.matchAll(idLowerHalf, nameAlice).toList();
            // alice with id <= 5 -> id 1, 3
            assertEquals(2, list.size());
        }

        @Test
        void matchAll_iterable_defaultsToAll() {
            Predicate<Person> idHigherHalf = p -> p.id() >= 5;
            List<Predicate<Person>> preds = List.of(idHigherHalf);
            List<Person> list = people.matchAll(preds).toList();
            assertEquals(5, list.size());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchAll_match_varargs_all() {
            Predicate<Person> idEven = p -> p.id() % 2 == 0;
            Predicate<Person> nameBob = p -> "bob".equals(p.name());
            List<Person> list = people.matchAll(SearchFunction.Match.ALL, idEven, nameBob).toList();
            // even bobs - bob is id 2 and 9; 2 is even
            assertEquals(1, list.size());
            assertEquals(2, list.get(0).id());
        }

        @SuppressWarnings("unchecked")
        @Test
        void matchAll_match_varargs_any() {
            Predicate<Person> idEven = p -> p.id() % 2 == 0;
            Predicate<Person> nameAlice = p -> "alice".equals(p.name());
            List<Person> list = people.matchAll(SearchFunction.Match.ANY, idEven, nameAlice).toList();
            // even {2,4,6,8} U alice {1,3,6} = {1,2,3,4,6,8}
            assertEquals(6, list.size());
        }

        @Test
        void matchAll_match_iterable_all() {
            Predicate<Person> idLow = p -> p.id() < 5;
            List<Predicate<Person>> preds = List.of(idLow);
            assertEquals(4L, people.matchAll(SearchFunction.Match.ALL, preds).count());
        }

        @Test
        void matchAll_match_iterable_any() {
            Predicate<Person> idLow = p -> p.id() <= 2;
            Predicate<Person> nameDave = p -> "dave".equals(p.name());
            List<Predicate<Person>> preds = List.of(idLow, nameDave);
            // {1,2} U {5} = {1,2,5}
            assertEquals(3L, people.matchAll(SearchFunction.Match.ANY, preds).count());
        }

        @Test
        void matchAll_returnsSingleStreamType() {
            SingleStream<Person> stream = people.matchAll(p -> p.id() == 1);
            assertNotNull(stream);
            assertEquals(1L, stream.count());
        }

        @Test
        void matchAll_skipsNullElements() {
            // The matchAll terminal predicate guards Objects.nonNull(it) before invoking the user predicate.
            // A user predicate that would NPE on null tags must not see them.
            Predicate<Person> hasOpsTag = p -> p.tags().contains("ops");
            // Person 8 has null tags - this would normally NPE, but matchAll skips nulls only
            // not null-fields. So this should still throw because p is non-null.
            assertThrows(NullPointerException.class, () -> people.matchAll(hasOpsTag).toList());
        }

        @Test
        void matchAll_noMatch_returnsEmpty() {
            assertEquals(0L, people.matchAll(p -> p.id() > 1000).count());
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void emptyFixture_findAll_returnsEmpty() {
            ConcurrentList<Person> empty = Concurrent.newList();
            assertEquals(0L, empty.findAll(NAME, "alice").count());
        }

        @Test
        void emptyFixture_containsAll_returnsEmpty() {
            ConcurrentList<Person> empty = Concurrent.newList();
            assertEquals(0L, empty.containsAll(TAGS, "admin").count());
        }

        @Test
        void emptyFixture_matchAll_returnsEmpty() {
            ConcurrentList<Person> empty = Concurrent.newList();
            assertEquals(0L, empty.matchAll(p -> true).count());
        }

        @Test
        void handRolledSearchable_streamProvidesAllElements() {
            // Narrow test of the core compare engine via a hand-rolled lambda.
            List<Person> source = List.of(
                new Person(10, "x", List.of("a")),
                new Person(11, "y", List.of("b"))
            );
            Searchable<Person> custom = () -> SingleStream.of(source);
            List<Person> result = custom.findAll(NAME, "x").toList();
            assertEquals(1, result.size());
            assertEquals(10, result.get(0).id());
        }

        @Test
        void handRolledSearchable_compareEngineHonorsAllMode() {
            List<Person> source = List.of(
                new Person(10, "x", List.of("a")),
                new Person(11, "x", List.of("b")),
                new Person(12, "y", List.of("a"))
            );
            Searchable<Person> custom = () -> SingleStream.of(source);
            TriPredicate<Function<Person, String>, Person, String> eq =
                (f, p, v) -> Objects.equals(f.apply(p), v);
            List<Person> result = custom.compare(
                SearchFunction.Match.ALL,
                eq,
                List.of(Pair.of(NAME, "x"))
            ).toList();
            assertEquals(2, result.size());
        }

        @Test
        void containsAll_emptyPredicates_all_returnsAllElements() {
            // ALL with no predicates means the for-loop never runs - all elements pass
            List<Person> list = people.containsAll(
                SearchFunction.Match.ALL,
                List.<Pair<Function<Person, List<String>>, String>>of()
            ).toList();
            assertEquals(people.size(), list.size());
        }

        @Test
        void findAll_emptyPredicates_all_returnsAllElements() {
            List<Person> list = people.findAll(
                SearchFunction.Match.ALL,
                List.<Pair<Function<Person, String>, String>>of()
            ).toList();
            assertEquals(people.size(), list.size());
        }

        @Test
        void matchAll_emptyPredicates_all_returnsAllElements() {
            // The internal matchAll converts predicates to Pair list. Empty predicates -> ALL passes.
            List<Person> list = people.matchAll(
                SearchFunction.Match.ALL,
                List.<Predicate<Person>>of()
            ).toList();
            assertEquals(people.size(), list.size());
        }
    }
}
