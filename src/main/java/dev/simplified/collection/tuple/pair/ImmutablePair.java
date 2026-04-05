package dev.simplified.collection.tuple.pair;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

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
 */
@Getter
public final class ImmutablePair<L, R> implements Pair<L, R> {

    /** Left object, may be null. */
    public final @Nullable L left;
    /** Right object, may be null. */
    public final @Nullable R right;

    /**
     * Creates a new immutable pair.
     *
     * @param left the left value, may be null
     * @param right the right value, may be null
     */
    public ImmutablePair(@Nullable L left, @Nullable R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns an immutable pair of two objects, inferring the generic types.
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
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