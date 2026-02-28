package dev.sbs.api.tuple.pair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A container object which may or may not contain a non-{@code null} value.
 * If a value is present, {@code isPresent()} returns {@code true}. If no
 * value is present, the object is considered <i>empty</i> and
 * {@code isPresent()} returns {@code false}.
 *
 * <p>Additional methods that depend on the presence or absence of a contained
 * value are provided, such as {@link #orElse(Pair) orElse()}
 * (returns a default value if no value is present) and
 * {@link #ifPresent(Consumer) ifPresent()} (performs an
 * action if a value is present).
 *
 * <p>This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; programmers should treat instances that are
 * {@linkplain #equals(Object) equal} as interchangeable and should not
 * use instances for synchronization, or unpredictable behavior may
 * occur. For example, in a future release, synchronization may fail.
 *
 * @apiNote
 * {@code PairOptional} is primarily intended for use as a method return type where
 * there is a clear need to represent "no result," and where using {@code null}
 * is likely to cause errors. A variable whose type is {@code PairOptional} should
 * never itself be {@code null}; it should always point to an {@code PairOptional}
 * instance.
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 * @since 1.8
 */
public final class PairOptional<L, R> {

    /**
     * Common instance for {@code empty()}.
     */
    private static final PairOptional<?, ?> EMPTY = new PairOptional<>(null);

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final Pair<L, R> value;

    /**
     * Returns an empty {@code PairOptional} instance.  No value is present for this
     * {@code PairOptional}.
     *
     * @apiNote
     * Though it may be tempting to do so, avoid testing if an object is empty
     * by comparing with {@code ==} or {@code !=} against instances returned by
     * {@code PairOptional.empty()}.  There is no guarantee that it is a singleton.
     * Instead, use {@link #isEmpty()} or {@link #isPresent()}.
     *
     * @param <L> The left type of the non-existent value
     * @param <R> The left type of the non-existent value
     * @return an empty {@code PairOptional}
     */
    @SuppressWarnings("unchecked")
    public static <L, R> PairOptional<L, R> empty() {
        return (PairOptional<L, R>) EMPTY;
    }

    /**
     * Constructs an instance with the described value.
     *
     * @param pair the pair value to describe.
     */
    private PairOptional(@Nullable Map.Entry<? extends L, ? extends R> pair) {
        this.value = Pair.from(pair);
    }

    /**
     * Constructs an instance with the described value.
     *
     * @param left the left value to describe.
     * @param right the right value to describe.
     */
    private PairOptional(@NotNull L left, @Nullable R right) {
        this.value = Pair.of(left, right);
    }

    /**
     * Returns an {@code PairOptional} describing the given non-{@code null}
     * value.
     *
     * @param left the left value to describe, which must be non-{@code null}
     * @param right the right value to describe, which can be {@code null}
     * @param <L> the type of the left value
     * @param <R> the type of the right value
     * @return an {@code PairOptional} with the value present
     * @throws NullPointerException if value is {@code null}
     */
    public static <L, R> PairOptional<L, R> of(@NotNull L left, @Nullable R right) {
        return new PairOptional<>(left, right);
    }

    /**
     * Returns an {@code PairOptional} describing the given {@code null}
     * value.
     *
     * @param pair the pair to describe, which can be {@code null}
     * @param <L> the type of the left value
     * @param <R> the type of the right value
     * @return an {@code PairOptional} with the value present
     * @throws NullPointerException if value is {@code null}
     */
    public static <L, R> PairOptional<L, R> of(@Nullable Map.Entry<? extends L, ? extends R> pair) {
        return new PairOptional<>(pair);
    }

    /**
     * Returns an {@code PairOptional} describing the given non-{@code null}
     * value.
     *
     * @param pair the pair to describe, which must be non-{@code null}
     * @param <L> the type of the left value
     * @param <R> the type of the right value
     * @return an {@code PairOptional} with the value present
     * @throws NullPointerException if value is {@code null}
     */
    public static <L, R> PairOptional<L, R> of(@NotNull Optional<? extends Map.Entry<L, R>> pair) {
        return pair.map(PairOptional::new).orElse(empty());
    }

    /**
     * Returns an {@code PairOptional} describing the given value, if
     * non-{@code null}, otherwise returns an empty {@code PairOptional}.
     *
     * @param left the possibly-{@code null} left value to describe
     * @param right the possibly-{@code null} right value to describe
     * @param <L> the type of the left value
     * @param <R> the type of the right value
     * @return an {@code PairOptional} with a present value if the specified value
     *         is non-{@code null}, otherwise an empty {@code PairOptional}
     */
    @SuppressWarnings("unchecked")
    public static <L, R> PairOptional<L, R> ofNullable(@Nullable L left, @Nullable R right) {
        return left == null ? (PairOptional<L, R>) EMPTY : new PairOptional<>(left, right);
    }

    /**
     * Returns an {@code PairOptional} describing the given value, if
     * non-{@code null}, otherwise returns an empty {@code PairOptional}.
     *
     * @param pair the possibly-{@code null} pair to describe
     * @param <L> the type of the left value
     * @param <R> the type of the right value
     * @return an {@code PairOptional} with a present value if the specified value
     *         is non-{@code null}, otherwise an empty {@code PairOptional}
     */
    @SuppressWarnings("unchecked")
    public static <L, R> PairOptional<L, R> ofNullable(@Nullable Pair<L, R> pair) {
        return pair == null || pair.getLeft() == null ? (PairOptional<L, R>) EMPTY : new PairOptional<>(pair);
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @return the non-{@code null} value described by this {@code PairOptional}
     * @throws NoSuchElementException if no value is present
     */
    public Pair<L, R> get() {
        if (this.value == null)
            throw new NoSuchElementException("No value present");

        return this.value;
    }

    public @NotNull L getKey() {
        return this.get().getLeft();
    }

    public @NotNull L getLeft() {
        return this.get().getLeft();
    }

    public @Nullable R getRight() {
        return this.get().getRight();
    }

    public @Nullable R getValue() {
        return this.get().getRight();
    }

    /**
     * If a value is present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent() {
        return this.value != null;
    }

    /**
     * If a value is  not present, returns {@code true}, otherwise
     * {@code false}.
     *
     * @return  {@code true} if a value is not present, otherwise {@code false}
     */
    public boolean isEmpty() {
        return this.value == null;
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise does nothing.
     *
     * @param action the action to be performed, if a value is present
     */
    public void ifPresent(@NotNull Consumer<? super Pair<L, R>> action) {
        if (this.value != null)
            action.accept(this.value);
    }

    public void ifPresent(@NotNull BiConsumer<? super L, ? super R> action) {
        if (this.value != null)
            action.accept(this.value.getLeft(), this.value.getRight());
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the given empty-based action.
     *
     * @param action the action to be performed, if a value is present
     * @param emptyAction the empty-based action to be performed, if no value is
     *        present
     * @throws NullPointerException if a value is present and the given action
     *         is {@code null}, or no value is present and the given empty-based
     *         action is {@code null}.
     * @since 9
     */
    public void ifPresentOrElse(Consumer<? super Pair<L, R>> action, @NotNull Runnable emptyAction) {
        if (this.value != null)
            action.accept(this.value);
        else
            emptyAction.run();
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the given empty-based action.
     *
     * @param action the action to be performed, if a value is present
     * @param emptyAction the empty-based action to be performed, if no value is
     *        present
     * @throws NullPointerException if a value is present and the given action
     *         is {@code null}, or no value is present and the given empty-based
     *         action is {@code null}.
     * @since 9
     */
    public void ifPresentOrElse(@NotNull BiConsumer<? super L, ? super R> action, @NotNull Runnable emptyAction) {
        if (this.value != null)
            action.accept(this.value.getLeft(), this.value.getRight());
        else
            emptyAction.run();
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * returns an {@code PairOptional} describing the value, otherwise returns an
     * empty {@code PairOptional}.
     *
     * @param predicate the predicate to apply to a value, if present
     * @return an {@code PairOptional} describing the value of this
     *         {@code PairOptional}, if a value is present and the value matches the
     *         given predicate, otherwise an empty {@code PairOptional}
     * @throws NullPointerException if the predicate is {@code null}
     */
    public @NotNull PairOptional<L, R> filter(@NotNull Predicate<? super Pair<L, R>> predicate) {
        return this.isPresent() ? (predicate.test(this.value) ? this : empty()) : this;
    }

    public @NotNull PairOptional<L, R> filter(@NotNull BiPredicate<? super L, ? super R> predicate) {
        return this.isPresent() ? (predicate.test(this.value.getLeft(), this.value.getRight()) ? this : empty()) : this;
    }

    public @NotNull PairOptional<L, R> filterKey(@NotNull Predicate<? super L> predicate) {
        return this.isPresent() ? (predicate.test(this.value.getLeft()) ? this : empty()) : this;
    }

    public @NotNull PairOptional<L, R> filterValue(@NotNull Predicate<? super R> predicate) {
        return this.isPresent() ? (predicate.test(this.value.getRight()) ? this : empty()) : this;
    }

    /**
     * If a value is present, returns an {@code PairOptional} describing (as if by
     * {@link #ofNullable}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code PairOptional}.
     *
     * <p>If the mapping function returns a {@code null} result then this method
     * returns an empty {@code PairOptional}.
     *
     * @apiNote
     * This method supports post-processing on {@code PairOptional} values, without
     * the need to explicitly check for a return status.  For example, the
     * following code traverses a stream of URIs, selects one that has not
     * yet been processed, and creates a path from that URI, returning
     * an {@code PairOptional<Path>}:
     *
     * <pre>{@code
     *     Optional<Path> p =
     *         uris.stream().filter(uri -> !isProcessedYet(uri))
     *                       .findFirst()
     *                       .map(Paths::get);
     * }</pre>
     *
     * Here, {@code findFirst} returns an {@code PairOptional<URI>}, and then
     * {@code map} returns an {@code PairOptional<Path>} for the desired
     * URI if one exists.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @param <U> The type of the value returned from the mapping function
     * @return an {@code PairOptional} describing the result of applying a mapping
     *         function to the value of this {@code PairOptional}, if a value is
     *         present, otherwise an empty {@code PairOptional}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public <U> @NotNull Optional<U> map(@NotNull Function<? super Pair<L, R>, ? extends U> mapper) {
        return this.isPresent() ? Optional.ofNullable(mapper.apply(this.value)) : Optional.empty();
    }

    public <U> @NotNull Optional<U> map(@NotNull BiFunction<? super L, ? super R, ? extends U> mapper) {
        return this.isPresent() ? Optional.ofNullable(mapper.apply(this.value.getLeft(), this.value.getRight())) : Optional.empty();
    }

    public <U, V> @NotNull PairOptional<U, V> mapPair(@NotNull BiFunction<? super L, ? super R, ? extends Pair<U, V>> mapper) {
        return this.isPresent() ? PairOptional.ofNullable(mapper.apply(this.value.getLeft(), this.value.getRight())) : empty();
    }

    public <U> @NotNull PairOptional<U, R> mapKey(@NotNull Function<? super L, ? extends U> mapper) {
        return this.isPresent() ? PairOptional.ofNullable(mapper.apply(this.value.getLeft()), this.value.getRight()) : empty();
    }

    public <U> @NotNull PairOptional<L, U> mapValue(@NotNull Function<? super R, ? extends U> mapper) {
        return this.isPresent() ? PairOptional.ofNullable(this.value.getLeft(), mapper.apply(this.value.getRight())) : empty();
    }


    /**
     * If a value is present, returns the result of applying the given
     * {@code PairOptional}-bearing mapping function to the value, otherwise returns
     * an empty {@code PairOptional}.
     *
     * <p>This method is similar to {@link #map(Function)}, but the mapping
     * function is one whose result is already an {@code PairOptional}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code PairOptional}.
     *
     * @param <UL> The left type of value of the {@code Pair} returned by the
     *            mapping function
     * @param <UR> The right type of value of the {@code Pair} returned by the
     *            mapping function
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code PairOptional}-bearing mapping
     *         function to the value of this {@code PairOptional}, if a value is
     *         present, otherwise an empty {@code PairOptional}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    @SuppressWarnings("unchecked")
    public <UL, UR> @NotNull Optional<Pair<UL, UR>> flatMap(@NotNull Function<? super Pair<L, R>, ? extends Optional<? extends Pair<UL, UR>>> mapper) {
        if (!isPresent())
            return Optional.empty();

        return (Optional<Pair<UL, UR>>) mapper.apply(this.value);
    }

    public <UL, UR> @NotNull PairOptional<UL, UR> flatMap(@NotNull BiFunction<? super L, ? super R, ? extends PairOptional<UL, UR>> mapper) {
        if (!isPresent())
            return empty();

        return mapper.apply(this.value.getLeft(), this.value.getRight());
    }

    /**
     * If a value is present, returns an {@code PairOptional} describing the value,
     * otherwise returns an {@code PairOptional} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code PairOptional}
     *        to be returned
     * @return returns an {@code PairOptional} describing the value of this
     *         {@code PairOptional}, if a value is present, otherwise an
     *         {@code PairOptional} produced by the supplying function.
     */
    @SuppressWarnings("unchecked")
    public @NotNull PairOptional<L, R> or(@NotNull Supplier<? extends PairOptional<? extends L, ? extends R>> supplier) {
        return this.isPresent() ? this : (PairOptional<L, R>) supplier.get();
    }

    /**
     * If a value is present, returns an {@code PairOptional} describing the value,
     * otherwise returns an {@code PairOptional} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code PairOptional}
     *        to be returned
     * @return returns an {@code PairOptional} describing the value of this
     *         {@code PairOptional}, if a value is present, otherwise an
     *         {@code PairOptional} produced by the supplying function.
     */
    @SuppressWarnings("unchecked")
    public @NotNull Optional<L> orKey(@NotNull Supplier<? extends Optional<? extends L>> supplier) {
        return this.isPresent() ? Optional.ofNullable(this.value.getKey()) : (Optional<L>) supplier.get();
    }

    /**
     * If a value is present, returns an {@code PairOptional} describing the value,
     * otherwise returns an {@code PairOptional} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code PairOptional}
     *        to be returned
     * @return returns an {@code PairOptional} describing the value of this
     *         {@code PairOptional}, if a value is present, otherwise an
     *         {@code PairOptional} produced by the supplying function.
     */
    @SuppressWarnings("unchecked")
    public @NotNull Optional<R> orValue(@NotNull Supplier<? extends Optional<? extends R>> supplier) {
        return this.isPresent() ? Optional.ofNullable(this.value.getRight()) : (Optional<R>) supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise returns
     * {@code other}.
     *
     * @param other the value to be returned, if no value is present.
     *        May be {@code null}.
     * @return the value, if present, otherwise {@code other}
     */
    public @Nullable Pair<L, R> orElse(@Nullable Pair<L, R> other) {
        return this.value != null ? this.value : other;
    }

    /**
     * If a key is present, returns the key, otherwise returns
     * {@code other}.
     *
     * @param other the value to be returned, if no value is present.
     *        May be {@code null}.
     * @return the value, if present, otherwise {@code other}
     */
    public @Nullable L orElseKey(@Nullable L other) {
        return this.value != null ? this.value.getKey() : other;
    }

    /**
     * If a value is present, returns the value, otherwise returns
     * {@code other}.
     *
     * @param other the value to be returned, if no value is present.
     *        May be {@code null}.
     * @return the value, if present, otherwise {@code other}
     */
    public @Nullable R orElseValue(@Nullable R other) {
        return this.value != null ? this.value.getValue() : other;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the
     *         supplying function
     * @throws NullPointerException if no value is present and the supplying
     *         function is {@code null}
     */
    public Pair<L, R> orElseGet(@NotNull Supplier<? extends Pair<L, R>> supplier) {
        return this.value != null ? this.value : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the
     *         supplying function
     * @throws NullPointerException if no value is present and the supplying
     *         function is {@code null}
     */
    public L orElseGetKey(@NotNull Supplier<? extends L> supplier) {
        return this.value != null ? this.value.getKey() : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the
     *         supplying function
     * @throws NullPointerException if no value is present and the supplying
     *         function is {@code null}
     */
    public R orElseGetValue(@NotNull Supplier<? extends R> supplier) {
        return this.value != null ? this.value.getValue() : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code PairOptional}
     * @throws NoSuchElementException if no value is present
     * @since 10
     */
    public Pair<L, R> orElseThrow() {
        return this.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws an exception
     * produced by the exception supplying function.
     *
     * @apiNote
     * A method reference to the exception constructor with an empty argument
     * list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier the supplying function that produces an
     *        exception to be thrown
     * @return the value, if present
     * @throws X if no value is present
     */
    public <X extends Throwable> Pair<L, R> orElseThrow(@NotNull Supplier<? extends X> exceptionSupplier) throws X {
        if (this.value != null) {
            return this.value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * If a value is present, returns a sequential {@link Stream} containing
     * only that value, otherwise returns an empty {@code Stream}.
     *
     * @apiNote
     * This method can be used to transform a {@code Stream} of optional
     * elements to a {@code Stream} of present value elements:
     * <pre>{@code
     *     Stream<Optional<T>> os = ..
     *     Stream<T> s = os.flatMap(Optional::stream)
     * }</pre>
     *
     * @return the optional value as a {@code Stream}
     * @since 9
     */
    public PairStream<L, R> stream() {
        return this.isPresent() ? PairStream.of(Stream.of(this.value)) : PairStream.empty();
    }

    /**
     * Indicates whether some other object is "equal to" this {@code PairOptional}.
     * The other object is considered equal if:
     * <ul>
     * <li>it is also an {@code PairOptional} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {@code true} if the other object is "equal to" this object
     *         otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof PairOptional<?, ?> other
                && Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code of the value, if present, otherwise {@code 0}
     * (zero) if no value is present.
     *
     * @return hash code value of the present value or {@code 0} if no value is
     *         present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a non-empty string representation of this {@code PairOptional}
     * suitable for debugging.  The exact presentation format is unspecified and
     * may vary between implementations and versions.
     *
     * @implSpec
     * If a value is present the result must include its string representation
     * in the result.  Empty and present {@code PairOptional}s must be unambiguously
     * differentiable.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return value != null
            ? String.format("PairOptional%s", value)
            : "PairOptional.empty";
    }
}
