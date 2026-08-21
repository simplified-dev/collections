package dev.simplified.collection.query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IndexSchema}, covering what a class can declare and every way two
 * declarations can contradict each other.
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

    @Nested
    class Reads {

        @Test
        void of_plainField_declaresANonUniqueSingleIndex() {
            IndexSchema.Declaration declaration = IndexSchema.of(Row.class).declaring(List.of("mode"));

            assertNotNull(declaration);
            assertFalse(declaration.unique());
            assertEquals(List.of("mode"), declaration.path());
            assertEquals(Row.class, declaration.reference().owner());
            assertEquals(PropertyReference.Kind.DECLARED, declaration.reference().kind());
        }

        @Test
        void of_uniqueField_carriesThePromise() {
            IndexSchema.Declaration declaration = IndexSchema.of(Row.class).declaring(List.of("code"));

            assertNotNull(declaration);
            assertTrue(declaration.unique());
        }

        @Test
        void of_inheritedField_isRead() {
            assertNotNull(IndexSchema.of(Row.class).declaring(List.of("region")));
        }

        @Test
        void of_group_assemblesThePathInDeclaredOrder() {
            IndexSchema.Declaration declaration = IndexSchema.of(Row.class).declaring(List.of("mode", "tier"));

            assertNotNull(declaration);
            assertEquals(List.of("mode", "tier"), declaration.path());
            assertFalse(declaration.unique());
        }

        @Test
        void of_repeatedAnnotation_putsOneFieldInBothIndexes() {
            IndexSchema schema = IndexSchema.of(Row.class);

            assertNotNull(schema.declaring(List.of("mode")));
            assertNotNull(schema.declaring(List.of("mode", "tier")));
        }

        @Test
        void of_undeclaredField_carriesNoIndex() {
            assertNull(IndexSchema.of(Row.class).declaring(List.of("label")));
        }

        @Test
        void of_reversedGroupPath_isNotADeclaration() {
            // A composite is probed in its declared order, so the reversed path names nothing.
            assertNull(IndexSchema.of(Row.class).declaring(List.of("tier", "mode")));
        }

        @Test
        void of_row_declaresExactlyFourIndexes() {
            assertEquals(4, IndexSchema.of(Row.class).declarations().size());
        }

        @Test
        void of_recordComponent_isRead() {
            IndexSchema.Declaration declaration = IndexSchema.of(Point.class).declaring(List.of("id"));

            assertNotNull(declaration);
            assertTrue(declaration.unique());
            assertNull(IndexSchema.of(Point.class).declaring(List.of("x")));
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

}
