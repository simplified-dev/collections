package dev.sbs.api.tuple.pair;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * A mutable pair consisting of two {@code Object} elements.
 * <p>
 * Not thread-safe.
 *
 * @param <L> the left element type
 * @param <R> the right element type
 */
@Getter
@Setter
@NoArgsConstructor
public final class MutablePair<L, R> implements Pair<L, R> {

    /** Left object, may be null. */
    public @Nullable L left;
    /** Right object, may be null. */
    public @Nullable R right;

    /**
     * Creates a new mutable pair with the specified values.
     *
     * @param left  the left value, may be null
     * @param right the right value, may be null
     */
    public MutablePair(@Nullable L left, @Nullable R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns a mutable pair of two objects, inferring the generic types.
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
     * @param left  the left element, may be null
     * @param right the right element, may be null
     * @return a mutable pair formed from the two parameters, not null
     */
    public static <L, R> @NotNull MutablePair<L, R> of(@Nullable L left, @Nullable R right) {
        return new MutablePair<>(left, right);
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
        R previous = getRight();
        setRight(value);
        return previous;
    }

    /**
     * Compares this pair to another based on the two elements, satisfying the
     * {@link Map.Entry} equality contract.
     *
     * @param obj the object to compare to, null returns false
     * @return {@code true} if the other object is a {@link Map.Entry} with equal key and value
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Map.Entry<?, ?> other)) return false;
        return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(), other.getValue());
    }

    /**
     * Returns a hash code following the {@link Map.Entry} specification.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return (left == null ? 0 : left.hashCode()) ^ (right == null ? 0 : right.hashCode());
    }

    /**
     * Returns a string representation of this pair in the format {@code (left,right)}.
     *
     * @return a string describing this pair, not null
     */
    @Override
    public @NotNull String toString() {
        return String.format("(%s,%s)", left, right);
    }

}