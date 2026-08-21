package dev.simplified.collection.query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IndexSchema}, covering what a class can declare, what it can reach through
 * a followed field, and every way two declarations can contradict each other.
 */
class IndexSchemaTest {

    /**
     * Supertype carrying a declaration, so inheritance can be exercised.
     */
    static class Base {

        @Indexed
        private String region = "";

    }

    /**
     * Full-shape fixture - one plain index, one unique index, one composite, and one field joining
     * both a plain index and the composite.
     */
    static class Row extends Base {

        @Indexed(unique = true)
        private String code = "";

        @Indexed
        @Indexed(group = "modeAndTier", order = 0)
        private String mode = "";

        @Indexed(group = "modeAndTier", order = 1)
        private int tier;

        private String label = "";

    }

    /**
     * Record fixture, proving a component's annotation reaches the field it is compiled to.
     */
    record Point(@Indexed(unique = true) String id, int x) {}

    /**
     * Fixture declaring nothing.
     */
    static class Bare {

        private String anything = "";

    }

    // --- Followed references ---

    /**
     * Target of a follow, declaring one unique key, one plain index and one composite.
     */
    static class Department {

        @Indexed(unique = true)
        private String name = "";

        @Indexed
        private String floor = "";

        @Indexed(group = "pair", order = 0)
        private String left = "";

        @Indexed(group = "pair", order = 1)
        private String right = "";

    }

    /**
     * Holder reaching a department's own keys through the field holding it.
     */
    static class Person {

        @Indexed
        @Indexed(follow = true)
        private Department department;

        @Indexed
        private String badge = "";

    }

    /**
     * Holder whose target declares nothing worth reaching.
     */
    static class Visitor {

        @Indexed(follow = true)
        private Bare host;

    }

    /**
     * Three links, so the depth bound can be seen to stop the walk.
     */
    static class Third {

        @Indexed
        private String deep = "";

    }

    /**
     * Second link in the chain.
     */
    static class Second {

        @Indexed(follow = true)
        private Third third;

    }

    /**
     * First link in the chain.
     */
    static class First {

        @Indexed(follow = true)
        private Second second;

    }

    /**
     * A reference reaching back to its own type, which the depth bound has to terminate.
     */
    static class Node {

        @Indexed
        private String id = "";

        @Indexed(follow = true)
        private Node next;

    }

    /**
     * Fixture following a field that holds many values rather than one.
     */
    static class Joined {

        @Indexed(follow = true)
        private List<Department> departments = List.of();

    }

    // --- Contradictions ---

    /**
     * Fixture whose group members disagree about uniqueness.
     */
    static class Disagreeing {

        @Indexed(group = "pair", order = 0, unique = true)
        private String left = "";

        @Indexed(group = "pair", order = 1)
        private String right = "";

    }

    /**
     * Fixture whose group members claim the same position.
     */
    static class Colliding {

        @Indexed(group = "pair")
        private String left = "";

        @Indexed(group = "pair")
        private String right = "";

    }

    /**
     * Fixture declaring one field both unique and not.
     */
    static class Contradicting {

        @Indexed
        @Indexed(unique = true)
        private String id = "";

    }

    /**
     * Names a component tuple, each entry a dotted property path off {@code type}.
     */
    private static List<PropertyReference> tuple(Class<?> type, String... dotted) {
        List<PropertyReference> components = new ArrayList<>(dotted.length);

        for (String path : dotted)
            components.add(PropertyReference.of(type, path.split("\\.")));

        return components;
    }

    @Nested
    class Reads {

        @Test
        void of_plainField_declaresANonUniqueSingleIndex() {
            IndexSchema.Declaration declaration = IndexSchema.of(Row.class).declaring(tuple(Row.class, "mode"));

            assertNotNull(declaration);
            assertFalse(declaration.unique());
            assertEquals(tuple(Row.class, "mode"), declaration.components());
            assertEquals(Row.class, declaration.components().getFirst().owner());
        }

        @Test
        void of_uniqueField_carriesThePromise() {
            IndexSchema.Declaration declaration = IndexSchema.of(Row.class).declaring(tuple(Row.class, "code"));

            assertNotNull(declaration);
            assertTrue(declaration.unique());
        }

        @Test
        void of_inheritedField_isRead() {
            assertNotNull(IndexSchema.of(Row.class).declaring(tuple(Row.class, "region")));
        }

        @Test
        void of_group_assemblesTheTupleInDeclaredOrder() {
            IndexSchema.Declaration declaration = IndexSchema.of(Row.class).declaring(tuple(Row.class, "mode", "tier"));

            assertNotNull(declaration);
            assertEquals(tuple(Row.class, "mode", "tier"), declaration.components());
            assertFalse(declaration.unique());
        }

        @Test
        void of_repeatedAnnotation_putsOneFieldInBothIndexes() {
            IndexSchema schema = IndexSchema.of(Row.class);

            assertNotNull(schema.declaring(tuple(Row.class, "mode")));
            assertNotNull(schema.declaring(tuple(Row.class, "mode", "tier")));
        }

        @Test
        void of_undeclaredField_carriesNoIndex() {
            assertNull(IndexSchema.of(Row.class).declaring(tuple(Row.class, "label")));
        }

        @Test
        void of_reversedGroupTuple_isNotADeclaration() {
            // A composite is probed in its declared order, so the reversed tuple names nothing.
            assertNull(IndexSchema.of(Row.class).declaring(tuple(Row.class, "tier", "mode")));
        }

        @Test
        void of_row_declaresExactlyFourIndexes() {
            assertEquals(4, IndexSchema.of(Row.class).declarations().size());
        }

        @Test
        void of_recordComponent_isRead() {
            IndexSchema.Declaration declaration = IndexSchema.of(Point.class).declaring(tuple(Point.class, "id"));

            assertNotNull(declaration);
            assertTrue(declaration.unique());
            assertNull(IndexSchema.of(Point.class).declaring(tuple(Point.class, "x")));
        }

        @Test
        void of_classDeclaringNothing_isEmpty() {
            assertTrue(IndexSchema.of(Bare.class).isEmpty());
            assertSame(IndexSchema.EMPTY, IndexSchema.of(Bare.class));
        }

        @Test
        void of_sameClassTwice_answersFromTheCache() {
            assertSame(IndexSchema.of(Row.class), IndexSchema.of(Row.class));
        }

    }

    @Nested
    class Follows {

        @Test
        void of_followedField_declaresThePathTheTargetExposes() {
            IndexSchema.Declaration declaration = IndexSchema.of(Person.class).declaring(tuple(Person.class, "department.name"));

            assertNotNull(declaration);
            assertEquals(List.of("department", "name"), declaration.components().getFirst().properties());
            assertEquals(Person.class, declaration.components().getFirst().owner());
        }

        @Test
        void of_followedField_doesNotInheritUniqueness() {
            // Department.name is unique across departments; two people can still share one
            // department, so the derived index promises nothing.
            IndexSchema.Declaration declaration = IndexSchema.of(Person.class).declaring(tuple(Person.class, "department.name"));

            assertNotNull(declaration);
            assertFalse(declaration.unique());
        }

        @Test
        void of_followedField_reachesEveryPlainIndexTheTargetDeclares() {
            IndexSchema schema = IndexSchema.of(Person.class);

            assertNotNull(schema.declaring(tuple(Person.class, "department.name")));
            assertNotNull(schema.declaring(tuple(Person.class, "department.floor")));
        }

        @Test
        void of_followedField_doesNotExportTheTargetsComposites() {
            // A composite is a key over several values of one department; a person cannot probe it
            // by naming several values of a department it merely points at.
            assertNull(IndexSchema.of(Person.class).declaring(tuple(Person.class, "department.left", "department.right")));
        }

        @Test
        void of_fieldFollowedAndIndexed_declaresBoth() {
            IndexSchema schema = IndexSchema.of(Person.class);

            assertNotNull(schema.declaring(tuple(Person.class, "department")));
            assertNotNull(schema.declaring(tuple(Person.class, "department.name")));
        }

        @Test
        void of_person_declaresExactlyFourIndexes() {
            // department, department.name, department.floor, badge
            assertEquals(4, IndexSchema.of(Person.class).declarations().size());
        }

        @Test
        void of_followingATargetDeclaringNothing_addsNothing() {
            assertTrue(IndexSchema.of(Visitor.class).isEmpty());
        }

        @Test
        void of_chainedFollows_reachThroughEveryStep() {
            IndexSchema.Declaration declaration = IndexSchema.of(First.class).declaring(tuple(First.class, "second.third.deep"));

            assertNotNull(declaration);
            assertEquals(List.of("second", "third", "deep"), declaration.components().getFirst().properties());
        }

        @Test
        void of_selfReference_terminatesAtTheDepthBound() {
            IndexSchema schema = IndexSchema.of(Node.class);

            assertNotNull(schema.declaring(tuple(Node.class, "id")));
            assertNotNull(schema.declaring(tuple(Node.class, "next.id")));
            assertNotNull(schema.declaring(tuple(Node.class, "next.next.id")));
            assertNotNull(schema.declaring(tuple(Node.class, "next.next.next.id")));

            // Bounded rather than refused: a field reaching back to its holder is an ordinary shape
            // and the paths through it are real, but the walk has to stop somewhere.
            assertNull(schema.declaring(tuple(Node.class, "next.next.next.next.id")));
            assertEquals(4, schema.declarations().size());
        }

    }

    @Nested
    class Refuses {

        @Test
        void of_followingACollection_throws() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> IndexSchema.of(Joined.class));
            assertTrue(thrown.getMessage().contains("join"));
        }

        @Test
        void of_groupDisagreeingOnUniqueness_throws() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> IndexSchema.of(Disagreeing.class));
            assertTrue(thrown.getMessage().contains("'pair'"));
        }

        @Test
        void of_groupSharingAPosition_throws() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> IndexSchema.of(Colliding.class));
            assertTrue(thrown.getMessage().contains("'pair'"));
        }

        @Test
        void of_onePathDeclaredBothWays_throws() {
            assertThrows(IllegalArgumentException.class, () -> IndexSchema.of(Contradicting.class));
        }

    }

}
