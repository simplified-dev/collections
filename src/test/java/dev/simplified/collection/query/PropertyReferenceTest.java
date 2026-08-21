package dev.simplified.collection.query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PropertyReference}, covering the three shapes an extractor can take - an
 * unbound method reference, a lambda body, and a composition - plus every shape that must be
 * refused rather than guessed at.
 */
class PropertyReferenceTest {

    /**
     * Fluent-accessor fixture, matching the record shape the query tests already use.
     */
    record Person(int id, String name, Department department) {}

    /**
     * Nested fixture reached through {@link Person#department()} for multi-hop paths.
     */
    record Department(String name) {}

    /**
     * Bean-accessor fixture, matching the shape the real consumers of this library use.
     */
    static final class Bean {

        private final String id;
        private final boolean active;

        Bean(String id, boolean active) {
            this.id = id;
            this.active = active;
        }

        public String getId() {
            return this.id;
        }

        public boolean isActive() {
            return this.active;
        }

        public String getURL() {
            return "https://example.invalid";
        }

        public String get() {
            return this.id;
        }

    }

    /**
     * Receiver fixture for a bound method reference, which reads a property through a captured
     * receiver rather than off the element it is handed.
     */
    static final class Directory {

        String lookup(Person person) {
            return person.name();
        }

    }

    @Nested
    class MethodReferences {

        @Test
        void of_unboundFluentAccessor_namesTheComponent() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, String>) Person::name);
            assertEquals(PropertyReference.Kind.METHOD_REFERENCE, reference.kind());
            assertEquals(Person.class, reference.owner());
            assertEquals(List.of("name"), reference.properties());
            assertTrue(reference.isDirect());
        }

        @Test
        void of_unboundBeanAccessor_stripsTheGetPrefix() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Bean, String>) Bean::getId);
            assertEquals(PropertyReference.Kind.METHOD_REFERENCE, reference.kind());
            assertEquals(List.of("id"), reference.properties());
        }

        @Test
        void of_unboundBooleanAccessor_stripsTheIsPrefix() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Bean, Boolean>) Bean::isActive);
            assertEquals(List.of("active"), reference.properties());
        }

        @Test
        void of_acronymAccessor_keepsTheStemAsWritten() {
            // The bean convention leaves a stem whose second letter is capitalised alone, so getURL
            // reads the property URL rather than uRL.
            PropertyReference reference = PropertyReference.of((SearchFunction<Bean, String>) Bean::getURL);
            assertEquals(List.of("URL"), reference.properties());
        }

        @Test
        void of_bareGet_isNotTreatedAsAPrefix() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Bean, String>) Bean::get);
            assertEquals(List.of("get"), reference.properties());
        }

        @Test
        void of_boundReference_isRefused() {
            // The receiver is captured, so what the extractor reads depends on the receiver rather
            // than on any property of the element, and no property may be indexed under it.
            Directory directory = new Directory();
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, String>) directory::lookup);
            assertEquals(PropertyReference.Kind.UNRESOLVED, reference.kind());
            assertSame(PropertyReference.UNRESOLVED, reference);
        }

        @Test
        void of_twoCallSitesNamingOneAccessor_agree() {
            SearchFunction<Person, String> first = Person::name;
            SearchFunction<Person, String> second = Person::name;

            assertNotSame(first.getClass(), second.getClass());
            assertEquals(PropertyReference.of(first), PropertyReference.of(second));
            assertEquals(PropertyReference.of(first).hashCode(), PropertyReference.of(second).hashCode());
        }

        @Test
        void of_sameExtractorTwice_answersFromTheMemo() {
            SearchFunction<Person, String> extractor = Person::name;
            assertSame(PropertyReference.of(extractor), PropertyReference.of(extractor));
        }

    }

    @Nested
    class LambdaBodies {

        @Test
        void of_singleAccessorBody_namesTheAccessor() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, String>) person -> person.name());
            assertEquals(PropertyReference.Kind.LAMBDA_BODY, reference.kind());
            assertEquals(Person.class, reference.owner());
            assertEquals(List.of("name"), reference.properties());
        }

        @Test
        void of_chainedAccessorBody_namesEveryHop() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, String>) person -> person.department().name());
            assertEquals(PropertyReference.Kind.LAMBDA_BODY, reference.kind());
            assertEquals(Person.class, reference.owner());
            assertEquals(List.of("department", "name"), reference.properties());
            assertFalse(reference.isDirect());
        }

        @Test
        void of_boxedPrimitiveBody_seesThroughTheBoxing() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, Integer>) person -> person.id());
            assertEquals(List.of("id"), reference.properties());
        }

        @Test
        void of_capturingBody_isRefused() {
            String captured = "alice";
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, Boolean>) person -> person.name().equals(captured));
            assertEquals(PropertyReference.Kind.UNRESOLVED, reference.kind());
        }

        @Test
        void of_arithmeticBody_isRefused() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, Integer>) person -> person.id() + 1);
            assertEquals(PropertyReference.Kind.UNRESOLVED, reference.kind());
        }

        @Test
        void of_branchingBody_isRefused() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, String>) person -> person.id() > 0 ? person.name() : null);
            assertEquals(PropertyReference.Kind.UNRESOLVED, reference.kind());
        }

        @Test
        void of_constantBody_isRefused() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, String>) person -> "alice");
            assertEquals(PropertyReference.Kind.UNRESOLVED, reference.kind());
        }

        @Test
        void of_identity_isRefused() {
            // Reading the argument itself names no property of it.
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, Person>) person -> person);
            assertEquals(PropertyReference.Kind.UNRESOLVED, reference.kind());
        }

        @Test
        void of_argumentTakingCall_isRefused() {
            PropertyReference reference = PropertyReference.of((SearchFunction<Person, String>) person -> person.name().substring(1));
            assertEquals(PropertyReference.Kind.UNRESOLVED, reference.kind());
        }

    }

    @Nested
    class Compositions {

        @Test
        void of_combine_joinsBothHalves() {
            SearchFunction<Person, String> departmentName = SearchFunction.combine(Person::department, Department::name);
            PropertyReference reference = PropertyReference.of(departmentName);

            assertEquals(PropertyReference.Kind.COMPOSED, reference.kind());
            assertEquals(Person.class, reference.owner());
            assertEquals(List.of("department", "name"), reference.properties());
        }

        @Test
        void of_combineWithARefusedHalf_isRefused() {
            SearchFunction<Person, Integer> refused = person -> person.id() + 1;
            PropertyReference reference = PropertyReference.of(SearchFunction.combine(refused, Object::toString));
            assertEquals(PropertyReference.Kind.UNRESOLVED, reference.kind());
        }

        @Test
        void of_andThenWithAPlainFunction_isRefused() {
            // The Function overload cannot be decoded, because a method reference bound to it
            // carries no writeReplace at all.
            SearchFunction<Person, String> composed = ((SearchFunction<Person, Department>) Person::department)
                .andThen((Function<Department, String>) Department::name);

            assertEquals(PropertyReference.Kind.UNRESOLVED, PropertyReference.of(composed).kind());
        }

        @Test
        void of_composition_isNotMemoisedAcrossInstances() {
            SearchFunction<Person, String> byDepartment = SearchFunction.combine(Person::department, Department::name);
            SearchFunction<Person, Integer> byIdLength = SearchFunction.combine(Person::name, String::length);

            assertEquals(List.of("department", "name"), PropertyReference.of(byDepartment).properties());
            assertEquals(List.of("name", "length"), PropertyReference.of(byIdLength).properties());
        }

    }

    @Nested
    class Declared {

        @Test
        void of_namedTuple_isDeclared() {
            PropertyReference reference = PropertyReference.of(Person.class, "name");
            assertEquals(PropertyReference.Kind.DECLARED, reference.kind());
            assertEquals(Person.class, reference.owner());
            assertEquals(List.of("name"), reference.properties());
        }

        @Test
        void of_namedTuple_equalsADecodedReferenceForTheSameProperty() {
            // The kind is a component, so a declared tuple and a decoded reference are distinct
            // values even when they name one property - the index keys on the property itself.
            PropertyReference declared = PropertyReference.of(Person.class, "name");
            PropertyReference decoded = PropertyReference.of((SearchFunction<Person, String>) Person::name);

            assertNotEquals(declared, decoded);
            assertEquals(declared.owner(), decoded.owner());
            assertEquals(declared.properties(), decoded.properties());
        }

        @Test
        void of_compositeTuple_keepsProbeOrder() {
            PropertyReference reference = PropertyReference.of(Person.class, "name", "id");
            assertEquals(List.of("name", "id"), reference.properties());
            assertFalse(reference.isDirect());
        }

        @Test
        void of_noProperties_throws() {
            assertThrows(IllegalArgumentException.class, () -> PropertyReference.of(Person.class));
        }

    }

    @Nested
    class Invariants {

        @Test
        void unresolved_namesNothing() {
            assertNull(PropertyReference.UNRESOLVED.owner());
            assertEquals(List.of(), PropertyReference.UNRESOLVED.properties());
            assertFalse(PropertyReference.UNRESOLVED.isResolved());
            assertFalse(PropertyReference.UNRESOLVED.isDirect());
        }

        @Test
        void construct_resolvedWithoutOwner_throws() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new PropertyReference(null, List.of("name"), PropertyReference.Kind.METHOD_REFERENCE)
            );
        }

        @Test
        void construct_unresolvedWithAProperty_throws() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new PropertyReference(Person.class, List.of("name"), PropertyReference.Kind.UNRESOLVED)
            );
        }

        @Test
        void construct_freezesTheChain() {
            List<String> mutable = new ArrayList<>(List.of("name"));
            PropertyReference reference = new PropertyReference(Person.class, mutable, PropertyReference.Kind.DECLARED);
            mutable.add("id");

            assertEquals(List.of("name"), reference.properties());
            assertThrows(UnsupportedOperationException.class, () -> reference.properties().add("id"));
        }

    }

}
