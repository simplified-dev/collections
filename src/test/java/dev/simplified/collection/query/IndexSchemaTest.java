package dev.simplified.collection.query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IndexSchema}, covering what a class can declare, what it can reach through
 * an indexed reference, and every way two declarations can contradict each other.
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

    // --- Reached references ---

    /**
     * Target of a reference, declaring one unique key, one plain index and one composite.
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
        private Department department;

        @Indexed
        private String badge = "";

    }

    /**
     * Holder whose target declares nothing worth reaching.
     */
    static class Visitor {

        @Indexed
        private Bare host;

    }

    /**
     * Three links, so the hop bound can be seen to stop the walk.
     */
    static class Third {

        @Indexed
        private String deep = "";

    }

    /**
     * Second link in the chain.
     */
    static class Second {

        @Indexed
        private Third third;

    }

    /**
     * First link in the chain.
     */
    static class First {

        @Indexed
        private Second second;

    }

    /**
     * A reference reaching back to its own type, which the hop bound has to terminate.
     */
    static class Node {

        @Indexed
        private String id = "";

        @Indexed
        private Node next;

    }

    /**
     * Fixture whose indexed field holds many values rather than one.
     */
    static class Joined {

        @Indexed
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
    class ReachesThrough {

        @Test
        void of_indexedReference_declaresThePathTheTargetExposes() {
            IndexSchema.Declaration declaration = IndexSchema.of(Person.class).declaring(tuple(Person.class, "department.name"));

            assertNotNull(declaration);
            assertEquals(List.of("department", "name"), declaration.components().getFirst().properties());
            assertEquals(Person.class, declaration.components().getFirst().owner());
        }

        @Test
        void of_reachedPath_doesNotInheritUniqueness() {
            // Department.name is unique across departments; two people can still share one
            // department, so the reached index promises nothing.
            IndexSchema.Declaration declaration = IndexSchema.of(Person.class).declaring(tuple(Person.class, "department.name"));

            assertNotNull(declaration);
            assertFalse(declaration.unique());
        }

        @Test
        void of_indexedReference_reachesEveryPlainIndexTheTargetDeclares() {
            IndexSchema schema = IndexSchema.of(Person.class);

            assertNotNull(schema.declaring(tuple(Person.class, "department.name")));
            assertNotNull(schema.declaring(tuple(Person.class, "department.floor")));
        }

        @Test
        void of_reachedPath_doesNotExportTheTargetsComposites() {
            // A composite is a key over several values of one department; a person cannot probe it
            // by naming several values of a department it merely points at.
            assertNull(IndexSchema.of(Person.class).declaring(tuple(Person.class, "department.left", "department.right")));
        }

        @Test
        void of_indexedReference_declaresBothTheObjectAndItsKeys() {
            // One annotation, two questions: find the person by the department they hold, and find
            // them by what the department is itself worth finding by.
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
        void of_targetDeclaringNothing_addsOnlyTheFieldItself() {
            IndexSchema schema = IndexSchema.of(Visitor.class);

            assertNotNull(schema.declaring(tuple(Visitor.class, "host")));
            assertEquals(1, schema.declarations().size());
        }

        @Test
        void of_collectionField_isIndexedButNotReachedThrough() {
            // The field is still indexed - by containment - but one element holding many
            // departments reaches many rows, which is a join rather than a property path.
            IndexSchema schema = IndexSchema.of(Joined.class);

            assertNotNull(schema.declaring(tuple(Joined.class, "departments")));
            assertNull(schema.declaring(tuple(Joined.class, "departments.name")));
            assertEquals(1, schema.declarations().size());
        }

        @Test
        void of_chainedReferences_reachThroughEveryStep() {
            IndexSchema.Declaration declaration = IndexSchema.of(First.class).declaring(tuple(First.class, "second.third.deep"));

            assertNotNull(declaration);
            assertEquals(List.of("second", "third", "deep"), declaration.components().getFirst().properties());
            assertEquals(3, IndexSchema.of(First.class).declarations().size());
        }

        @Test
        void of_selfReference_stopsAtThreeHops() {
            IndexSchema schema = IndexSchema.of(Node.class);

            assertNotNull(schema.declaring(tuple(Node.class, "id")));
            assertNotNull(schema.declaring(tuple(Node.class, "next")));
            assertNotNull(schema.declaring(tuple(Node.class, "next.id")));
            assertNotNull(schema.declaring(tuple(Node.class, "next.next")));
            assertNotNull(schema.declaring(tuple(Node.class, "next.next.id")));
            assertNotNull(schema.declaring(tuple(Node.class, "next.next.next")));

            // Bounded rather than refused: a field reaching back to its holder is an ordinary shape
            // and the paths through it are real, but a path stops at three accessors.
            assertNull(schema.declaring(tuple(Node.class, "next.next.next.id")));
            assertEquals(6, schema.declarations().size());
        }

    }

    /**
     * Fixture declaring through its accessors rather than its fields, the way a persistence mapping
     * written against properties does.
     */
    static class Accessed {

        private final String code;
        private final Held held;
        private final String label;

        Accessed(String code, Held held, String label) {
            this.code = code;
            this.held = held;
            this.label = label;
        }

        @Indexed(unique = true)
        public String getCode() {
            return this.code;
        }

        @Indexed
        public Held getHeld() {
            return this.held;
        }

        public String getLabel() {
            return this.label;
        }

    }

    /**
     * The object an accessed fixture holds, declaring through an accessor of its own.
     */
    static class Held {

        private final String zone = "";

        @Indexed
        public String getZone() {
            return this.zone;
        }

    }

    /**
     * Fixture carrying a declaration on a boolean accessor, whose prefix is {@code is}.
     */
    static class Asked {

        @Indexed
        public boolean isActive() {
            return true;
        }

    }

    /**
     * Fixture declaring one component, which a record propagates to the field and the accessor
     * both.
     */
    record Component(@Indexed(group = "pair", order = 0) String mode, @Indexed(group = "pair", order = 1) int tier) {}

    @Nested
    class Accessors {

        @Test
        void of_accessorDeclaration_namesTheProperty() {
            IndexSchema schema = IndexSchema.of(Accessed.class);

            // getCode declares what a field named code would, because the name a query decodes to
            // is the one the declaration is filed under
            assertNotNull(schema.coveringPath(List.of("code")));
            assertTrue(schema.coveringPath(List.of("code")).unique());
            assertNull(schema.coveringPath(List.of("label")));
        }

        @Test
        void of_accessorHoldingAnIndexedObject_isReachedThrough() {
            IndexSchema schema = IndexSchema.of(Accessed.class);

            assertNotNull(schema.coveringPath(List.of("held")));
            assertNotNull(schema.coveringPath(List.of("held", "zone")));
            assertFalse(schema.coveringPath(List.of("held", "zone")).unique());
        }

        @Test
        void of_askedAccessor_dropsTheIsPrefixToo() {
            assertNotNull(IndexSchema.of(Asked.class).coveringPath(List.of("active")));
        }

        @Test
        void of_recordComponent_isOneDeclarationNotTwo() {
            // The annotation lands on the field and on the accessor; declaring the group twice
            // would collide on position rather than describe one key.
            IndexSchema schema = IndexSchema.of(Component.class);

            assertEquals(1, schema.declarations().size());
            assertNotNull(schema.declaring(tuple(Component.class, "mode", "tier")));
        }

        @Test
        void of_staticAccessor_declaresNothing() {
            assertTrue(IndexSchema.of(Constant.class).isEmpty());
        }

    }

    /**
     * Fixture whose declaration sits on a static accessor, which answers the same for every element.
     */
    static class Constant {

        @Indexed
        public static String shared() {
            return "";
        }

    }

    @Nested
    class Refuses {

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

    // --- Hierarchies ---

    /**
     * Supertype declaring a composite on its accessors.
     */
    static class GroupBase {

        @Indexed(group = "pair", order = 0)
        public String getMode() {
            return "";
        }

        @Indexed(group = "pair", order = 1)
        public int getTier() {
            return 0;
        }

    }

    /**
     * Subtype repeating one member of its supertype's composite on an override.
     */
    static class GroupSub extends GroupBase {

        @Override
        @Indexed(group = "pair", order = 0)
        public String getMode() {
            return "sub";
        }

    }

    /**
     * Supertype declaring a composite on its fields.
     */
    static class ShadowBase {

        @Indexed(group = "pair", order = 0)
        private String mode = "";

        @Indexed(group = "pair", order = 1)
        private int tier;

    }

    /**
     * Subtype repeating one member of its supertype's composite on a field shadowing the one it
     * sits on.
     */
    static class ShadowSub extends ShadowBase {

        @Indexed(group = "pair", order = 0)
        private String mode = "";

    }

    /**
     * Supertype declaring a plain index on its accessor.
     */
    static class CodeBase {

        @Indexed
        public String getCode() {
            return "";
        }

    }

    /**
     * Subtype whose override promises the same property unique.
     */
    static class UniqueCode extends CodeBase {

        @Override
        @Indexed(unique = true)
        public String getCode() {
            return "";
        }

    }

    /**
     * Subtype overriding a declared accessor with no annotation, the way a runtime proxy of an
     * entity overrides every accessor it intercepts.
     */
    static class QuietAccessed extends Accessed {

        QuietAccessed() {
            super("", null, "");
        }

        @Override
        public String getCode() {
            return super.getCode();
        }

    }

    /**
     * Supertype declaring one accessor both as an index of its own and as a member of a composite.
     */
    static class PairBase {

        @Indexed
        @Indexed(group = "pair", order = 0)
        public String getMode() {
            return "";
        }

        @Indexed(group = "pair", order = 1)
        public int getTier() {
            return 0;
        }

    }

    /**
     * Subtype restating that accessor as an index of its own and nothing more.
     */
    static class PlainPair extends PairBase {

        @Override
        @Indexed
        public String getMode() {
            return "";
        }

    }

    /**
     * Subtype declaring nothing of its own, the shape of a runtime proxy or of a subclass that
     * only overrides behaviour.
     */
    static class Quiet extends Row {}

    /**
     * Supertype holding one member of a unique group, whose other member only its subclass
     * declares.
     */
    static class SplitBase {

        @Indexed(group = "key", order = 0, unique = true)
        private String region = "";

    }

    /**
     * Subtype declaring the rest of its supertype's group, so the whole key is seen from here alone.
     */
    static class SplitSub extends SplitBase {

        @Indexed(group = "key", order = 1, unique = true)
        private String code = "";

    }

    /**
     * Supertype promising a unique key over two accessors.
     */
    static class KeyBase {

        @Indexed(group = "key", order = 0, unique = true)
        public String getA() {
            return "";
        }

        @Indexed(group = "key", order = 1, unique = true)
        public String getB() {
            return "";
        }

    }

    /**
     * Subtype restating one member of that key as an index of its own, which takes it out of the
     * key.
     */
    static class RestatedKey extends KeyBase {

        @Override
        @Indexed
        public String getB() {
            return "";
        }

    }

    @Nested
    class Hierarchy {

        @Test
        void of_overrideRepeatingAGroupMember_isOneComposite() {
            // The override restates mode, so the supertype's mode is not read a second time and
            // each position in the group is held once.
            IndexSchema schema = IndexSchema.of(GroupSub.class);

            assertEquals(1, schema.declarations().size());
            assertNotNull(schema.declaring(tuple(GroupSub.class, "mode", "tier")));
        }

        @Test
        void of_shadowingFieldRepeatingAGroupMember_isOneComposite() {
            IndexSchema schema = IndexSchema.of(ShadowSub.class);

            assertEquals(1, schema.declarations().size());
            assertNotNull(schema.declaring(tuple(ShadowSub.class, "mode", "tier")));
        }

        @Test
        void of_overrideChangingUnique_carriesTheSubclassPromise() {
            // The most derived declaration is the one read, so the subclass's promise holds for its
            // instances and the supertype's holds for its own.
            IndexSchema.Declaration derived = IndexSchema.of(UniqueCode.class).coveringPath(List.of("code"));
            IndexSchema.Declaration base = IndexSchema.of(CodeBase.class).coveringPath(List.of("code"));

            assertNotNull(derived);
            assertTrue(derived.unique());
            assertNotNull(base);
            assertFalse(base.unique());
        }

        @Test
        void of_unannotatedOverride_keepsTheSupertypeDeclaration() {
            // Only an annotation restates a property, so an override carrying none - all a runtime
            // proxy's overrides carry - leaves the supertype's declaration standing.
            IndexSchema.Declaration declaration = IndexSchema.of(QuietAccessed.class).coveringPath(List.of("code"));

            assertNotNull(declaration);
            assertTrue(declaration.unique());
        }

        @Test
        void of_overrideRestatingAProperty_dropsItsSupertypeGroup() {
            // The supertype joins mode to a composite; the override declares it on its own and
            // nothing more, so the composite does not apply to the subclass, and the tier it leaves
            // behind is one member of a group, which declares nothing.
            assertNotNull(IndexSchema.of(PairBase.class).declaring(tuple(PairBase.class, "mode", "tier")));

            IndexSchema schema = IndexSchema.of(PlainPair.class);

            assertNotNull(schema.coveringPath(List.of("mode")));
            assertNull(schema.declaring(tuple(PlainPair.class, "mode", "tier")));
            assertNull(schema.coveringPath(List.of("tier")));
        }

        @Test
        void declaringClass_isTheMostDerivedClassDeclaringAnything() {
            // Base declares region and Row declares its own on top, so Row is the most derived
            // class declaring anything, for itself and for a subclass declaring nothing.
            assertEquals(Base.class, IndexSchema.of(Base.class).declaringClass());
            assertEquals(Row.class, IndexSchema.of(Row.class).declaringClass());
            assertEquals(Row.class, IndexSchema.of(Quiet.class).declaringClass());
        }

        @Test
        void of_subclassDeclaringNothing_declaresWhatItsSuperclassDoes() {
            // Every declaration is inherited, with its components restated on the subclass.
            IndexSchema schema = IndexSchema.of(Quiet.class);
            IndexSchema.Declaration code = schema.coveringPath(List.of("code"));

            assertEquals(4, schema.declarations().size());
            assertNotNull(schema.declaring(tuple(Quiet.class, "mode", "tier")));
            assertNotNull(code);
            assertTrue(code.unique());
        }

        @Test
        void of_groupSeenAsOneMember_declaresNothing() {
            // From the supertype the key is one value, which is no composite, and a unique over it
            // would promise that no two elements share a region - a promise nobody wrote.
            assertSame(IndexSchema.EMPTY, IndexSchema.of(SplitBase.class));

            IndexSchema.Declaration key = IndexSchema.of(SplitSub.class).declaring(tuple(SplitSub.class, "region", "code"));

            assertNotNull(key);
            assertTrue(key.unique());
        }

        @Test
        void of_overrideTakingAMemberOutOfAUniqueGroup_leavesNoPartialKey() {
            // The override restates b on its own, so a is all the subclass sees of the key, and two
            // elements sharing a while differing in b keep the promise the supertype made.
            IndexSchema schema = IndexSchema.of(RestatedKey.class);
            IndexSchema.Declaration b = schema.coveringPath(List.of("b"));

            assertNull(schema.coveringPath(List.of("a")));
            assertNotNull(b);
            assertFalse(b.unique());
        }

    }

}
