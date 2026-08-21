package dev.simplified.collection.query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LambdaBodyReader}, pinning the exact set of bodies it accepts.
 *
 * <p>The reader answers raw accessor names rather than property names - normalising them is
 * {@link PropertyReference}'s job - so these tests assert {@code getId} where the decoded reference
 * would say {@code id}.
 */
class LambdaBodyReaderTest {

    /**
     * Bean-accessor fixture with a public field, so a field read can be exercised.
     */
    static final class Row {

        public String exposed = "exposed";

        private final String id;
        private final Leaf leaf;

        Row(String id, Leaf leaf) {
            this.id = id;
            this.leaf = leaf;
        }

        public String getId() {
            return this.id;
        }

        public Leaf getLeaf() {
            return this.leaf;
        }

        public int size() {
            return this.id.length();
        }

    }

    /**
     * Second hop for a chained body.
     */
    record Leaf(String label) {}

    /**
     * Generic carrier, so an erased accessor forces the compiler to emit a cast the reader has to
     * see through.
     */
    record Box<T>(T item) {}

    /**
     * Cracks an extractor to the serialized form naming the body to read.
     *
     * @param extractor the extractor to crack
     * @return its serialized form
     */
    private static SerializedLambda crack(SearchFunction<?, ?> extractor) {
        try {
            Method writeReplace = extractor.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            return (SerializedLambda) writeReplace.invoke(extractor);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("A SearchFunction is Serializable, so it carries a writeReplace", exception);
        }
    }

    @Nested
    class Accepts {

        @Test
        void read_singleAccessor_answersTheRawName() {
            LambdaBodyReader.Chain chain = LambdaBodyReader.read(crack((SearchFunction<Row, String>) row -> row.getId()));

            assertNotNull(chain);
            assertEquals(Row.class.getName().replace('.', '/'), chain.ownerInternalName());
            assertEquals(List.of("getId"), chain.accessors());
        }

        @Test
        void read_chainedAccessors_answersEveryHopInOrder() {
            LambdaBodyReader.Chain chain = LambdaBodyReader.read(crack((SearchFunction<Row, String>) row -> row.getLeaf().label()));

            assertNotNull(chain);
            assertEquals(Row.class.getName().replace('.', '/'), chain.ownerInternalName());
            assertEquals(List.of("getLeaf", "label"), chain.accessors());
        }

        @Test
        void read_boxedPrimitive_seesThroughTheValueOfCall() {
            LambdaBodyReader.Chain chain = LambdaBodyReader.read(crack((SearchFunction<Row, Integer>) row -> row.size()));

            assertNotNull(chain);
            assertEquals(List.of("size"), chain.accessors());
        }

        @Test
        void read_erasedAccessor_seesThroughTheCast() {
            // box.item() erases to Object, so the compiler inserts a checkcast before the next hop.
            LambdaBodyReader.Chain chain = LambdaBodyReader.read(crack((SearchFunction<Box<Leaf>, String>) box -> box.item().label()));

            assertNotNull(chain);
            assertEquals(Box.class.getName().replace('.', '/'), chain.ownerInternalName());
            assertEquals(List.of("item", "label"), chain.accessors());
        }

    }

    @Nested
    class Refuses {

        @Test
        void read_fieldRead_refuses() {
            assertNull(LambdaBodyReader.read(crack((SearchFunction<Row, String>) row -> row.exposed)));
        }

        @Test
        void read_constant_refuses() {
            assertNull(LambdaBodyReader.read(crack((SearchFunction<Row, String>) row -> "constant")));
        }

        @Test
        void read_arithmetic_refuses() {
            assertNull(LambdaBodyReader.read(crack((SearchFunction<Row, Integer>) row -> row.size() + 1)));
        }

        @Test
        void read_branch_refuses() {
            assertNull(LambdaBodyReader.read(crack((SearchFunction<Row, String>) row -> row.size() > 0 ? row.getId() : null)));
        }

        @Test
        void read_argumentTakingCall_refuses() {
            assertNull(LambdaBodyReader.read(crack((SearchFunction<Row, String>) row -> row.getId().substring(1))));
        }

        @Test
        void read_identity_refuses() {
            // The parameter is returned untouched, so the body names no accessor at all.
            assertNull(LambdaBodyReader.read(crack((SearchFunction<Row, Row>) row -> row)));
        }

        @Test
        void read_realAccessorBody_refuses() {
            // A method reference names a real method whose body reads a field, which is exactly the
            // shape this reader turns down - and why a method reference is decoded from the
            // constant pool instead.
            assertNull(LambdaBodyReader.read(crack((SearchFunction<Leaf, String>) Leaf::label)));
        }

    }

}
