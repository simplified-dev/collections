package dev.simplified.collection.tuple.pair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A mutable pair consisting of two {@code Object} elements.
 * <p>
 * Not thread-safe.
 *
 * @param <L> the left element type
 * @param <R> the right element type
 */
public final class MutablePair<L, R> implements Pair<L, R> {

    private @Nullable L left;
    private @Nullable R right;

    /**
     * Creates a new mutable pair with both elements set to {@code null}.
     */
    public MutablePair() {
    }

    /**
     * Creates a new mutable pair with the specified values.
     *
     * @param left the left value, may be null
     * @param right the right value, may be null
     */
    public MutablePair(@Nullable L left, @Nullable R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns a mutable pair of two objects, inferring the generic types.
     *
     * @param <L> the left element type
     * @param <R> the right element type
     * @param left the left element, may be null
     * @param right the right element, may be null
     * @return a mutable pair formed from the two parameters, not null
     */
    public static <L, R> @NotNull MutablePair<L, R> of(@Nullable L left, @Nullable R right) {
        return new MutablePair<>(left, right);
    }

    @Override
    public @Nullable L left() {
        return this.left;
    }

    /**
     * Sets the left element and returns this pair for chaining.
     *
     * @param left the new left value, may be null
     * @return this pair
     */
    public @NotNull MutablePair<L, R> left(@Nullable L left) {
        this.left = left;
        return this;
    }

    @Override
    public @Nullable R right() {
        return this.right;
    }

    /**
     * Sets the right element and returns this pair for chaining.
     *
     * @param right the new right value, may be null
     * @return this pair
     */
    public @NotNull MutablePair<L, R> right(@Nullable R right) {
        this.right = right;
        return this;
    }

    /**
     * Sets the right element (value) and returns the previous value, implementing
     * {@link Map.Entry#setValue(Object)}.
     *
     * @param value the new right value, may be null
     * @return the previous right value, may be null
     */
    @Override
    public @Nullable R setValue(@Nullable R value) {
        R previous = this.right;
        this.right = value;
        return previous;
    }

}
