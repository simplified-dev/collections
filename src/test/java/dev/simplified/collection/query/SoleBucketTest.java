package dev.simplified.collection.query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SoleBucket}, which stands in for the platform's one-element list and so has
 * to keep every promise {@link List} makes about one.
 */
class SoleBucketTest {

    private static final String ONLY = "alpha";

    /**
     * Value that answers equal to what it holds without being answered equal by it, so which side of
     * a comparison the asking value lands on is observable.
     */
    record Loose(String held) {

        @Override
        public boolean equals(Object other) {
            return other instanceof Loose loose ? this.held.equals(loose.held()) : this.held.equals(other);
        }

        @Override
        public int hashCode() {
            return this.held.hashCode();
        }

    }

    private static SoleBucket<String> bucket() {
        return new SoleBucket<>(ONLY);
    }

    @Nested
    class Reads {

        @Test
        void size_isOne() {
            assertEquals(1, bucket().size());
            assertFalse(bucket().isEmpty());
        }

        @Test
        void get_theOnlyPosition_answersTheElement() {
            assertEquals(ONLY, bucket().get(0));
            assertEquals(ONLY, bucket().getFirst());
            assertEquals(ONLY, bucket().getLast());
        }

        @Test
        void get_anyOtherPosition_isOutOfBounds() {
            assertThrows(IndexOutOfBoundsException.class, () -> bucket().get(1));
            assertThrows(IndexOutOfBoundsException.class, () -> bucket().get(-1));
        }

        @Test
        void contains_answersByEquality() {
            assertTrue(bucket().contains("alpha"));
            assertTrue(bucket().contains(new String("alpha")));
            assertFalse(bucket().contains("beta"));
            assertFalse(bucket().contains(null));
        }

        @Test
        void indexOf_answersTheOnlyPositionOrNone() {
            assertEquals(0, bucket().indexOf("alpha"));
            assertEquals(0, bucket().lastIndexOf("alpha"));
            assertEquals(-1, bucket().indexOf("beta"));
            assertEquals(-1, bucket().lastIndexOf("beta"));
        }

        @Test
        void containsAll_answersWhateverTheOneElementCovers() {
            assertTrue(bucket().containsAll(List.of()));
            assertTrue(bucket().containsAll(List.of("alpha")));
            assertTrue(bucket().containsAll(List.of("alpha", "alpha")));
            assertFalse(bucket().containsAll(List.of("alpha", "beta")));
        }

        @Test
        void toArray_answersOneElement() {
            assertArrayEquals(new Object[] { ONLY }, bucket().toArray());
            assertArrayEquals(new String[] { ONLY }, bucket().toArray(new String[0]));
        }

        @Test
        void toArray_intoALongerArray_endsTheRunWithANull() {
            String[] roomy = { "x", "y", "z" };
            String[] filled = bucket().toArray(roomy);

            assertSame(roomy, filled);
            assertEquals(ONLY, filled[0]);
            assertNull(filled[1]);
        }

        @Test
        void subList_answersTheWholeOrNothing() {
            SoleBucket<String> bucket = bucket();

            assertSame(bucket, bucket.subList(0, 1));
            assertEquals(List.of(), bucket.subList(0, 0));
            assertEquals(List.of(), bucket.subList(1, 1));
            assertThrows(IndexOutOfBoundsException.class, () -> bucket.subList(0, 2));
            assertThrows(IndexOutOfBoundsException.class, () -> bucket.subList(1, 0));
        }

        @Test
        void stream_walksTheOneElement() {
            assertEquals(List.of(ONLY), bucket().stream().toList());
        }

        @Test
        void forEach_visitsTheOneElement() {
            List<String> visited = new ArrayList<>();
            bucket().forEach(visited::add);
            assertEquals(List.of(ONLY), visited);
        }

        @Test
        void randomAccess_isAdvertised() {
            // What List.spliterator picks its implementation on, and what the platform's own
            // one-element list advertises.
            assertInstanceOf(RandomAccess.class, bucket());
        }

    }

    @Nested
    class AgreesWithThePlatform {

        @Test
        void equals_isSymmetricWithEveryOtherOneElementList() {
            assertEquals(List.of(ONLY), bucket());
            assertEquals(bucket(), List.of(ONLY));
            assertEquals(new LinkedList<>(List.of(ONLY)), bucket());
            assertEquals(bucket(), new ArrayList<>(List.of(ONLY)));
            assertEquals(bucket(), bucket());
        }

        @Test
        void equals_anythingElse_isFalse() {
            assertNotEquals(bucket(), List.of("beta"));
            assertNotEquals(bucket(), List.of(ONLY, ONLY));
            assertNotEquals(bucket(), List.of());
            assertNotEquals(bucket(), ONLY);
            assertNotEquals(null, bucket());
        }

        @Test
        void contains_asksFromTheSideEveryOtherListAsksFrom() {
            // List reads "an element e such that Objects.equals(o, e)", so the value being looked
            // for is the one asked. A bucket of two seals as an unmodifiable ArrayList and compares
            // from that side, and one query must not answer differently for having found one
            // element rather than two.
            Loose asking = new Loose(ONLY);

            assertTrue(new ArrayList<>(List.of(ONLY)).contains(asking));
            assertTrue(bucket().contains(asking));
            assertEquals(0, bucket().indexOf(asking));
            assertTrue(bucket().containsAll(List.of(asking)));
        }

        @Test
        void hashCode_matchesTheListContract() {
            assertEquals(List.of(ONLY).hashCode(), bucket().hashCode());
            assertEquals(new ArrayList<>(List.of(ONLY)).hashCode(), bucket().hashCode());
        }

        @Test
        void toString_matchesTheListContract() {
            assertEquals(List.of(ONLY).toString(), bucket().toString());
        }

    }

    @Nested
    class Walks {

        @Test
        void iterator_yieldsTheElementOnceAndThenStops() {
            Iterator<String> walk = bucket().iterator();

            assertTrue(walk.hasNext());
            assertEquals(ONLY, walk.next());
            assertFalse(walk.hasNext());
            assertThrows(NoSuchElementException.class, walk::next);
        }

        @Test
        void listIterator_walksBothWays() {
            ListIterator<String> walk = bucket().listIterator();

            assertEquals(0, walk.nextIndex());
            assertEquals(-1, walk.previousIndex());
            assertFalse(walk.hasPrevious());
            assertEquals(ONLY, walk.next());
            assertEquals(1, walk.nextIndex());
            assertEquals(0, walk.previousIndex());
            assertTrue(walk.hasPrevious());
            assertEquals(ONLY, walk.previous());
            assertFalse(walk.hasPrevious());
            assertThrows(NoSuchElementException.class, walk::previous);
        }

        @Test
        void listIterator_startedPastTheEnd_hasOnlyAPrevious() {
            ListIterator<String> walk = bucket().listIterator(1);

            assertFalse(walk.hasNext());
            assertTrue(walk.hasPrevious());
            assertEquals(ONLY, walk.previous());
        }

        @Test
        void listIterator_outsideTheList_isOutOfBounds() {
            assertThrows(IndexOutOfBoundsException.class, () -> bucket().listIterator(2));
            assertThrows(IndexOutOfBoundsException.class, () -> bucket().listIterator(-1));
        }

    }

    @Nested
    class Refuses {

        @Test
        void everyMutator_throws() {
            SoleBucket<String> bucket = bucket();

            assertThrows(UnsupportedOperationException.class, () -> bucket.add("beta"));
            assertThrows(UnsupportedOperationException.class, () -> bucket.add(0, "beta"));
            assertThrows(UnsupportedOperationException.class, () -> bucket.addAll(List.of("beta")));
            assertThrows(UnsupportedOperationException.class, () -> bucket.addAll(0, List.of("beta")));
            assertThrows(UnsupportedOperationException.class, bucket::clear);
            assertThrows(UnsupportedOperationException.class, () -> bucket.remove("alpha"));
            assertThrows(UnsupportedOperationException.class, () -> bucket.remove(0));
            assertThrows(UnsupportedOperationException.class, () -> bucket.removeAll(List.of("alpha")));
            assertThrows(UnsupportedOperationException.class, () -> bucket.removeIf(held -> false));
            assertThrows(UnsupportedOperationException.class, () -> bucket.replaceAll(held -> held));
            assertThrows(UnsupportedOperationException.class, () -> bucket.retainAll(List.of()));
            assertThrows(UnsupportedOperationException.class, () -> bucket.set(0, "beta"));
            assertThrows(UnsupportedOperationException.class, () -> bucket.sort(Comparator.naturalOrder()));
        }

        @Test
        void everySequencedMutator_throws() {
            SoleBucket<String> bucket = bucket();

            assertThrows(UnsupportedOperationException.class, () -> bucket.addFirst("beta"));
            assertThrows(UnsupportedOperationException.class, () -> bucket.addLast("beta"));
            assertThrows(UnsupportedOperationException.class, bucket::removeFirst);
            assertThrows(UnsupportedOperationException.class, bucket::removeLast);
        }

        @Test
        void everyCursorMutator_throws() {
            ListIterator<String> walk = bucket().listIterator();
            walk.next();

            assertThrows(UnsupportedOperationException.class, walk::remove);
            assertThrows(UnsupportedOperationException.class, () -> walk.set("beta"));
            assertThrows(UnsupportedOperationException.class, () -> walk.add("beta"));
        }

    }

}
