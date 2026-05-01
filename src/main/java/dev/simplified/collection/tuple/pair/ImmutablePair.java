package dev.simplified.collection.tuple.pair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable pair consisting of two {@code Object} elements.
 * <p>
 * Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the pair, then the pair itself
 * effectively becomes mutable.
 * <p>
 * Thread-safe if the stored objects are thread-safe.
 *
 * @param <L> the left element type
 * @param <R> the right element type
 * @param left the left value, may be null
 * @param right the right value, may be null
 */
public record ImmutablePair<L, R>(@Nullable L left, @Nullable R right) implements Pair<L, R> {

    /**
     * Returns an immutable pair of two objects, inferring the generic types.
     *
     * @param <L> the left element type
     * @param <R> the right element type
     * @param left the left element, may be null
     * @param right the right element, may be null
     * @return an immutable pair formed from the two parameters, not null
     */
    public static <L, R> @NotNull ImmutablePair<L, R> of(@Nullable L left, @Nullable R right) {
        return new ImmutablePair<>(left, right);
    }

    /**
     * Throws {@code UnsupportedOperationException}.
     * <p>
     * This pair is immutable, so this operation is not supported.
     *
     * @param value the value to set
     * @return never
     * @throws UnsupportedOperationException always
     */
    @Override
    public R setValue(R value) {
        throw new UnsupportedOperationException();
    }

}
