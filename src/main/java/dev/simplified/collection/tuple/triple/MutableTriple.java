package dev.simplified.collection.tuple.triple;

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
public final class MutableTriple<L, M, R> implements Triple<L, M, R> {

    private @Nullable L left;
    private @Nullable M middle;
    private @Nullable R right;

    /**
     * Creates a new mutable triple with all three elements set to {@code null}.
     */
    public MutableTriple() {
    }

    /**
     * Creates a new mutable triple with the specified values.
     *
     * @param left the left value, may be null
     * @param middle the middle value, may be null
     * @param right the right value, may be null
     */
    public MutableTriple(@Nullable L left, @Nullable M middle, @Nullable R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    /**
     * Returns a mutable triple of three objects, inferring the generic types.
     *
     * @param <L> the left element type
     * @param <M> the middle element type
     * @param <R> the right element type
     * @param left the left element, may be null
     * @param middle the middle element, may be null
     * @param right the right element, may be null
     * @return a mutable triple formed from the three parameters, not null
     */
    public static <L, M, R> @NotNull MutableTriple<L, M, R> of(@Nullable L left, @Nullable M middle, @Nullable R right) {
        return new MutableTriple<>(left, middle, right);
    }

    @Override
    public @Nullable L left() {
        return this.left;
    }

    /**
     * Sets the left element and returns this triple for chaining.
     *
     * @param left the new left value, may be null
     * @return this triple
     */
    public @NotNull MutableTriple<L, M, R> left(@Nullable L left) {
        this.left = left;
        return this;
    }

    @Override
    public @Nullable M middle() {
        return this.middle;
    }

    /**
     * Sets the middle element and returns this triple for chaining.
     *
     * @param middle the new middle value, may be null
     * @return this triple
     */
    public @NotNull MutableTriple<L, M, R> middle(@Nullable M middle) {
        this.middle = middle;
        return this;
    }

    @Override
    public @Nullable R right() {
        return this.right;
    }

    /**
     * Sets the right element and returns this triple for chaining.
     *
     * @param right the new right value, may be null
     * @return this triple
     */
    public @NotNull MutableTriple<L, M, R> right(@Nullable R right) {
        this.right = right;
        return this;
    }

}
