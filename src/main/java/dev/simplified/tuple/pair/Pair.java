package dev.sbs.api.tuple.pair;

import dev.sbs.api.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * <p>A pair consisting of two elements.</p>
 *
 * <p>This class is an abstract implementation defining the basic API.
 * It refers to the elements as 'left' and 'right'. It also implements the
 * {@code Map.Entry} interface where the key is 'left' and the value is 'right'.</p>
 *
 * <p>Subclass implementations may be mutable or immutable.
 * However, there is no restriction on the type of the stored objects that may be stored.
 * If mutable objects are stored in the pair, then the pair itself effectively becomes mutable.</p>
 *
 * @param <L> the left element type
 * @param <R> the right element type
 */
public abstract class Pair<L, R> implements Map.Entry<L, R>, Comparable<Pair<L, R>> {

    /**
     * <p>Obtains an immutable pair of from two objects inferring the generic types.</p>
     *
     * <p>This factory allows the pair to be created using inference to
     * obtain the generic types.</p>
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
     * @param left  the left element, may be null
     * @param right the right element, may be null
     * @return a pair formed from the two parameters, not null
     */
    public static <L, R> Pair<L, R> of(@Nullable L left, @Nullable R right) {
        return new ImmutablePair<>(left, right);
    }

    /**
     * <p>Obtains an immutable pair of from an entry object inferring the generic types.</p>
     *
     * <p>This factory allows the pair to be created using inference to
     * obtain the generic types.</p>
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
     * @param entry the entry, may not be null
     * @return a pair formed from the entry parameter, not null
     */
    public static <L, R> Pair<L, R> from(@Nullable Map.Entry<? extends L, ? extends R> entry) {
        return entry != null ? new ImmutablePair<>(entry.getKey(), entry.getValue()) : empty();
    }

    /**
     * <p>Obtains an immutable pair of null objects.</p>
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
     * @return a pair formed from null parameters
     */
    public static <L, R> Pair<L, R> empty() {
        return new ImmutablePair<>(null, null);
    }

    //-----------------------------------------------------------------------

    /**
     * <p>Gets the left element from this pair.</p>
     *
     * <p>When treated as a key-value pair, this is the key.</p>
     *
     * @return the left element, may be null
     */
    public abstract L getLeft();

    /**
     * <p>Gets the right element from this pair.</p>
     *
     * <p>When treated as a key-value pair, this is the value.</p>
     *
     * @return the right element, may be null
     */
    public abstract R getRight();

    /**
     * <p>Gets the key from this pair.</p>
     *
     * <p>This method implements the {@code Map.Entry} interface returning the
     * left element as the key.</p>
     *
     * @return the left element as the key, may be null
     */
    public final L getKey() {
        return this.getLeft();
    }

    /**
     * <p>Gets the value from this pair.</p>
     *
     * <p>This method implements the {@code Map.Entry} interface returning the
     * right element as the value.</p>
     *
     * @return the right element as the value, may be null
     */
    public R getValue() {
        return this.getRight();
    }

    /**
     * <p>Gets if the left and right values are null.</p>
     *
     * @return true if both values are null
     */
    public boolean isEmpty() {
        return Objects.isNull(this.getLeft()) && Objects.isNull(this.getRight());
    }

    //-----------------------------------------------------------------------

    /**
     * <p>Compares the pair based on the left element followed by the right element.
     * The types must be {@code Comparable}.</p>
     *
     * @param other the other pair, not null
     * @return negative if this is less, zero if equal, positive if greater
     */
    public int compareTo(Pair<L, R> other) {
        return new CompareToBuilder()
            .append(getLeft(), other.getLeft())
            .append(getRight(), other.getRight())
            .toComparison();
    }

    /**
     * <p>Compares this pair to another based on the two elements.</p>
     *
     * @param obj the object to compare to, null returns false
     * @return true if the elements of the pair are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;

        if (obj instanceof Map.Entry<?, ?> other)
            return this.getKey().equals(other.getKey()) && this.getValue().equals(other.getValue());

        return false;
    }

    /**
     * <p>Returns a suitable hash code.
     * The hash code follows the definition in {@code Map.Entry}.</p>
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        // see Map.Entry API specification
        return (getKey() == null ? 0 : getKey().hashCode()) ^
            (getValue() == null ? 0 : getValue().hashCode());
    }

    /**
     * <p>Returns a String representation of this pair using the format {@code ($left,$right)}.</p>
     *
     * @return a string describing this object, not null
     */
    @Override
    public @NotNull String toString() {
        return this.toString("(%s,%s)");
    }

    /**
     * <p>Formats the receiver using the given format.</p>
     *
     * <p>This uses {@link java.util.Formattable} to perform the formatting. Two variables may
     * be used to embed the left and right elements. Use {@code {0}} for the left
     * element (key) and {@code {1}} for the right element (value).
     * The default format used by {@code toString()} is {@code ({0},{1})}.</p>
     *
     * @param format the format string, optionally containing {@code %1$s} and {@code %2$s}, not null
     * @return the formatted string, not null
     */
    public @NotNull String toString(String format) {
        return String.format(format, getLeft(), getRight());
    }

}
