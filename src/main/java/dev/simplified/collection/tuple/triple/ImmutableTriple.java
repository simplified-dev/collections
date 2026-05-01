package dev.simplified.collection.tuple.triple;

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
 * @param left the left value, may be null
 * @param middle the middle value, may be null
 * @param right the right value, may be null
 */
public record ImmutableTriple<L, M, R>(@Nullable L left, @Nullable M middle, @Nullable R right) implements Triple<L, M, R> {

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
    public static <L, M, R> @NotNull ImmutableTriple<L, M, R> of(@Nullable L left, @Nullable M middle, @Nullable R right) {
        return new ImmutableTriple<>(left, middle, right);
    }

}
