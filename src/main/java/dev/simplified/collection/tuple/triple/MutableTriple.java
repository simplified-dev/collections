package dev.simplified.collection.tuple.triple;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A mutable triple consisting of three {@code Object} elements.
 * <p>
 * Not thread-safe.
 *
 * @param <L> the left element type
 * @param <M> the middle element type
 * @param <R> the right element type
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public final class MutableTriple<L, M, R> implements Triple<L, M, R> {

    /** Left object, may be null. */
    public @Nullable L left;
    /** Middle object, may be null. */
    public @Nullable M middle;
    /** Right object, may be null. */
    public @Nullable R right;

    /**
     * Returns a mutable triple of three objects, inferring the generic types.
     *
     * @param <L>    the left element type
     * @param <M>    the middle element type
     * @param <R>    the right element type
     * @param left the left element, may be null
     * @param middle the middle element, may be null
     * @param right the right element, may be null
     * @return a mutable triple formed from the three parameters, not null
     */
    public static <L, M, R> @NotNull MutableTriple<L, M, R> of(@Nullable L left, @Nullable M middle, @Nullable R right) {
        return new MutableTriple<>(left, middle, right);
    }

    /**
     * Returns a string representation of this triple in the format {@code (left,middle,right)}.
     *
     * @return a string describing this triple, not null
     */
    @Override
    public @NotNull String toString() {
        return String.format("(%s,%s,%s)", left, middle, right);
    }

}