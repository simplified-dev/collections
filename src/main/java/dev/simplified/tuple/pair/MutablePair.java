package dev.sbs.api.tuple.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>A mutable pair consisting of two {@code Object} elements.</p>
 *
 * <p>Not #ThreadSafe#</p>
 *
 * @param <L> the left element type
 * @param <R> the right element type
 */
@Getter
@Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public final class MutablePair<L, R> extends Pair<L, R> {

    /**
     * Left object
     */
    public L left;
    /**
     * Right object
     */
    public R right;

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
    public static <L, R> MutablePair<L, R> of(L left, R right) {
        return new MutablePair<>(left, right);
    }

    //-----------------------------------------------------------------------

    /**
     * Sets the {@code Map.Entry} value.
     * This sets the right element of the pair.
     *
     * @param value the right value to set, not null
     * @return the old value for the right element
     */
    public R setValue(R value) {
        R result = getRight();
        setRight(value);
        return result;
    }

}
