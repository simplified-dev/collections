package dev.sbs.api.collection.stream.triple;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts two input arguments and returns no
 * result.  This is the two-arity specialization of {@link Consumer}.
 * Unlike most other functional interfaces, {@code BiConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object)}.
 *
 * @param <L> the type of the first argument to the operation
 * @param <M> the type of the second argument to the operation
 * @param <R> the type of the third argument to the operation
 *
 * @see Consumer
 */
@FunctionalInterface
public interface TriConsumer<L, M, R> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param l the first input argument
     * @param m the second input argument
     * @param r the third input argument
     */
    void accept(L l, M m, R r);

    /**
     * Returns a composed {@code TriConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code TriConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default TriConsumer<L, M, R> andThen(@NotNull TriConsumer<? super L, ? super M, ? super R> after) {
        Objects.requireNonNull(after);

        return (l, m, r) -> {
            accept(l, m, r);
            after.accept(l, m, r);
        };
    }
}
