package dev.sbs.api.tuple.triple;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable triple consisting of three {@code Object} elements.
 * <p>
 * Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the triple, then the triple
 * itself effectively becomes mutable.
 * <p>
 * Thread-safe if all three stored objects are thread-safe.
 *
 * @param <L> the left element type
 * @param <M> the middle element type
 * @param <R> the right element type
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ImmutableTriple<L, M, R> implements Triple<L, M, R> {

    /** Left object, may be null. */
    public final @Nullable L left;
    /** Middle object, may be null. */
    public final @Nullable M middle;
    /** Right object, may be null. */
    public final @Nullable R right;

    /**
     * Returns an immutable triple of three objects, inferring the generic types.
     *
     * @param <L>    the left element type
     * @param <M>    the middle element type
     * @param <R>    the right element type
     * @param left   the left element, may be null
     * @param middle the middle element, may be null
     * @param right  the right element, may be null
     * @return an immutable triple formed from the three parameters, not null
     */
    public static <L, M, R> @NotNull ImmutableTriple<L, M, R> of(@Nullable L left, @Nullable M middle, @Nullable R right) {
        return new ImmutableTriple<>(left, middle, right);
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