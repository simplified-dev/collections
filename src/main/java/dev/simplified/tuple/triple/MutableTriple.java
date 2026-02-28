package dev.sbs.api.tuple.triple;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * <p>A mutable triple consisting of three {@code Object} elements.</p>
 *
 * <p>Not #ThreadSafe#</p>
 *
 * @param <L> the left element type
 * @param <M> the middle element type
 * @param <R> the right element type
 */
@Getter
@Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public final class MutableTriple<L, M, R> extends Triple<L, M, R> {

    public L left;
    public M middle;
    public R right;

    /**
     * <p>Obtains a mutable triple of three objects inferring the generic types.</p>
     *
     * <p>This factory allows the triple to be created using inference to
     * obtain the generic types.</p>
     *
     * @param <L>    the left element type
     * @param <M>    the middle element type
     * @param <R>    the right element type
     * @param left   the left element, may be null
     * @param middle the middle element, may be null
     * @param right  the right element, may be null
     * @return a triple formed from the three parameters, not null
     */
    public static <L, M, R> @NotNull MutableTriple<L, M, R> of(final L left, final M middle, final R right) {
        return new MutableTriple<>(left, middle, right);
    }

}
