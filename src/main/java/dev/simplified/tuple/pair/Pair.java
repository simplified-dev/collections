package dev.sbs.api.tuple.pair;

import dev.sbs.api.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A pair consisting of two elements.
 * <p>
 * This interface defines the basic API for a two-element tuple, referring to the
 * elements as 'left' and 'right'. It also implements the {@link Map.Entry} interface
 * where the left element is the key and the right element is the value.
 * <p>
 * Implementations may be mutable or immutable. If mutable objects are stored in the
 * pair, then the pair itself effectively becomes mutable.
 *
 * @param <L> the left element type
 * @param <R> the right element type
 * @see ImmutablePair
 * @see MutablePair
 */
public interface Pair<L, R> extends Map.Entry<L, R>, Comparable<Pair<L, R>> {

    /**
     * Returns an immutable pair of two objects, inferring the generic types.
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
     * @param left  the left element, may be null
     * @param right the right element, may be null
     * @return an immutable pair formed from the two parameters, not null
     */
    static <L, R> @NotNull Pair<L, R> of(@Nullable L left, @Nullable R right) {
        return new ImmutablePair<>(left, right);
    }

    /**
     * Returns an immutable pair copied from a {@link Map.Entry}, inferring the generic types.
     * If {@code entry} is {@code null}, returns an empty pair.
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
     * @param entry the entry to copy, may be null
     * @return an immutable pair formed from the entry, not null
     */
    static <L, R> @NotNull Pair<L, R> from(@Nullable Map.Entry<? extends L, ? extends R> entry) {
        return entry != null ? new ImmutablePair<>(entry.getKey(), entry.getValue()) : empty();
    }

    /**
     * Returns an immutable pair where both elements are {@code null}.
     *
     * @param <L> the left element type
     * @param <R> the right element type
     * @return an empty pair, not null
     */
    static <L, R> @NotNull Pair<L, R> empty() {
        return new ImmutablePair<>(null, null);
    }

    /**
     * Gets the left element from this pair.
     * <p>
     * When treated as a key-value pair, this is the key.
     *
     * @return the left element, may be null
     */
    L getLeft();

    /**
     * Gets the right element from this pair.
     * <p>
     * When treated as a key-value pair, this is the value.
     *
     * @return the right element, may be null
     */
    R getRight();

    /**
     * Gets the key from this pair, implementing {@link Map.Entry#getKey()}.
     * <p>
     * Returns the same value as {@link #getLeft()}.
     *
     * @return the left element as the key, may be null
     */
    @Override
    default L getKey() {
        return getLeft();
    }

    /**
     * Gets the value from this pair, implementing {@link Map.Entry#getValue()}.
     * <p>
     * Returns the same value as {@link #getRight()}.
     *
     * @return the right element as the value, may be null
     */
    @Override
    default R getValue() {
        return getRight();
    }

    /**
     * Returns {@code true} if both the left and right elements are {@code null}.
     *
     * @return {@code true} if both elements are null, {@code false} otherwise
     */
    default boolean isEmpty() {
        return getLeft() == null && getRight() == null;
    }

    /**
     * Compares this pair to another in natural order, first by the left element,
     * then by the right element. The element types must be {@link Comparable}.
     *
     * @param other the other pair, not null
     * @return negative if this is less, zero if equal, positive if greater
     */
    @Override
    default int compareTo(@NotNull Pair<L, R> other) {
        return new CompareToBuilder()
            .append(getLeft(), other.getLeft())
            .append(getRight(), other.getRight())
            .toComparison();
    }

    /**
     * Formats this pair using the given format string.
     * <p>
     * Use {@code %1$s} for the left element and {@code %2$s} for the right element.
     * The default format used by {@code toString()} is {@code (%1$s,%2$s)}.
     *
     * @param format the format string, not null
     * @return the formatted string, not null
     */
    default @NotNull String toString(@NotNull String format) {
        return String.format(format, getLeft(), getRight());
    }

}