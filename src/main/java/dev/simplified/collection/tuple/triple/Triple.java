package dev.simplified.collection.tuple.triple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * A triple consisting of three elements.
 * <p>
 * This interface defines the basic API for a three-element tuple, referring to
 * the elements as 'left', 'middle', and 'right'.
 * <p>
 * Implementations may be mutable or immutable. If mutable objects are stored in
 * the triple, then the triple itself effectively becomes mutable.
 *
 * @param <L> the left element type
 * @param <M> the middle element type
 * @param <R> the right element type
 * @see ImmutableTriple
 * @see MutableTriple
 */
public interface Triple<L, M, R> extends Comparable<Triple<L, M, R>> {

    /**
     * Returns an immutable triple of three objects, inferring the generic types.
     *
     * @param <L> the left element type
     * @param <M> the middle element type
     * @param <R> the right element type
     * @param left the left element, may be null
     * @param middle the middle element, may be null
     * @param right the right element, may be null
     * @return an immutable triple formed from the three parameters, not null
     */
    static <L, M, R> @NotNull Triple<L, M, R> of(@Nullable L left, @Nullable M middle, @Nullable R right) {
        return new ImmutableTriple<>(left, middle, right);
    }

    /** The left element of this triple. */
    L left();

    /** The middle element of this triple. */
    M middle();

    /** The right element of this triple. */
    R right();

    /**
     * Compares this triple to another in natural order, first by the left element,
     * then by the middle element, then by the right element.
     * The element types must be {@link Comparable}.
     *
     * @param other the other triple, not null
     * @return negative if this is less, zero if equal, positive if greater
     */
    @Override
    default int compareTo(@NotNull Triple<L, M, R> other) {
        @SuppressWarnings("unchecked")
        Comparator<Object> nullSafeComparator = (Comparator<Object>) (Comparator<?>) Comparator.nullsFirst(Comparator.naturalOrder());
        int result = nullSafeComparator.compare(left(), other.left());
        if (result != 0) return result;
        result = nullSafeComparator.compare(middle(), other.middle());
        if (result != 0) return result;
        return nullSafeComparator.compare(right(), other.right());
    }

    /**
     * Formats this triple using the given format string.
     * <p>
     * Use {@code %1$s} for the left element, {@code %2$s} for the middle element,
     * and {@code %3$s} for the right element.
     * The default format used by {@code toString()} is {@code (%1$s,%2$s,%3$s)}.
     *
     * @param format the format string, not null
     * @return the formatted string, not null
     */
    default @NotNull String toString(@NotNull String format) {
        return String.format(format, left(), middle(), right());
    }

}
