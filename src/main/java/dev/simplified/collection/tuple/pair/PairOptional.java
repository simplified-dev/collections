package dev.simplified.collection.tuple.pair;

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
 * A container object which may or may not contain a non-{@code null} {@link Pair} value.
 * If a value is present, {@link #isPresent()} returns {@code true}. If no value is present,
 * the object is considered <i>empty</i> and {@link #isPresent()} returns {@code false}.
 * <p>
 * Additional methods that depend on the presence or absence of a contained value are provided,
 * such as {@link #orElse(Pair) orElse()} (returns a default value if no value is present) and
 * {@link #ifPresent(Consumer) ifPresent()} (performs an action if a value is present).
 *
 * @apiNote
 * {@code PairOptional} is primarily intended for use as a method return type where there is a
 * clear need to represent "no result", and where using {@code null} is likely to cause errors.
 * A variable whose type is {@code PairOptional} should never itself be {@code null}; it should
 * always point to a {@code PairOptional} instance.
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 */
public final class PairOptional<L, R> {

    /** Common instance for {@link #empty()}. */
    private static final PairOptional<?, ?> EMPTY = new PairOptional<>((Pair<?, ?>) null);

    /** The wrapped pair; {@code null} indicates no value is present. */
    private final @Nullable Pair<L, R> pair;

    private PairOptional(@Nullable Pair<L, R> pair) {
        this.pair = pair;
    }

    // Create

    /**
     * Returns an empty {@code PairOptional} instance. No value is present.
     *
     * @apiNote
     * Avoid testing emptiness by comparing with {@code ==} against instances returned by
     * {@code PairOptional.empty()}. There is no guarantee it is a singleton.
     * Use {@link #isEmpty()} or {@link #isPresent()} instead.
     *
     * @param <L> the left type of the non-existent value
     * @param <R> the right type of the non-existent value
     * @return an empty {@code PairOptional}
     */
    @SuppressWarnings("unchecked")
    public static <L, R> @NotNull PairOptional<L, R> empty() {
        return (PairOptional<L, R>) EMPTY;
    }

    /**
     * Returns a {@code PairOptional} describing the given key-value pair.
     * The left value must be non-{@code null}; the right value may be {@code null}.
     *
     * @param left the left value to describe, must be non-{@code null}
     * @param right the right value to describe, may be {@code null}
     * @param <L>   the type of the left value
     * @param <R>   the type of the right value
     * @return a {@code PairOptional} with the pair present
     * @throws NullPointerException if {@code left} is {@code null}
     */
    public static <L, R> @NotNull PairOptional<L, R> of(@NotNull L left, @Nullable R right) {
        return new PairOptional<>(Pair.of(left, right));
    }

    /**
     * Returns a {@code PairOptional} describing the given {@link Map.Entry}.
     * If {@code pair} is {@code null}, returns an empty {@code PairOptional}.
     *
     * @param pair the entry to describe, may be {@code null}
     * @param <L>  the type of the left value
     * @param <R>  the type of the right value
     * @return a {@code PairOptional} with the pair present if {@code pair} is non-null,
     *         otherwise an empty {@code PairOptional}
     */
    public static <L, R> @NotNull PairOptional<L, R> of(@Nullable Map.Entry<? extends L, ? extends R> pair) {
        return pair != null ? new PairOptional<>(Pair.from(pair)) : empty();
    }

    /**
     * Returns a {@code PairOptional} describing the entry contained in the given
     * {@link Optional}, or an empty {@code PairOptional} if the {@code Optional} is empty.
     *
     * @param pair a non-null {@code Optional} that may or may not contain a {@link Map.Entry}
     * @param <L>  the type of the left value
     * @param <R>  the type of the right value
     * @return a {@code PairOptional} with the pair present if the {@code Optional} contains a
     *         value, otherwise an empty {@code PairOptional}
     */
    public static <L, R> @NotNull PairOptional<L, R> of(@NotNull Optional<? extends Map.Entry<L, R>> pair) {
        return pair.map(lrEntry -> new PairOptional<>(Pair.from(lrEntry))).orElseGet(PairOptional::empty);
    }

    /**
     * Returns a {@code PairOptional} describing the given left and right values, or an empty
     * {@code PairOptional} if {@code left} is {@code null}.
     *
     * @param left the possibly-{@code null} left value to describe
     * @param right the possibly-{@code null} right value to describe
     * @param <L>   the type of the left value
     * @param <R>   the type of the right value
     * @return a {@code PairOptional} with a present pair if {@code left} is non-{@code null},
     *         otherwise an empty {@code PairOptional}
     */
    public static <L, R> @NotNull PairOptional<L, R> ofNullable(@Nullable L left, @Nullable R right) {
        return left == null ? empty() : new PairOptional<>(Pair.of(left, right));
    }

    /**
     * Returns a {@code PairOptional} describing the given pair, or an empty {@code PairOptional}
     * if {@code pair} is {@code null} or its left element is {@code null}.
     *
     * @param pair the possibly-{@code null} pair to describe
     * @param <L>  the type of the left value
     * @param <R>  the type of the right value
     * @return a {@code PairOptional} with a present pair if {@code pair} is non-{@code null}
     *         and its left element is non-{@code null}, otherwise an empty {@code PairOptional}
     */
    public static <L, R> @NotNull PairOptional<L, R> ofNullable(@Nullable Pair<L, R> pair) {
        return pair == null || pair.left() == null ? empty() : new PairOptional<>(pair);
    }

    // Get

    /**
     * If a value is present, returns the pair, otherwise throws {@link NoSuchElementException}.
     *
     * @return the non-{@code null} pair described by this {@code PairOptional}
     * @throws NoSuchElementException if no value is present
     */
    public @NotNull Pair<L, R> get() {
        if (this.pair == null)
            throw new NoSuchElementException("No value present");

        return this.pair;
    }

    /**
     * If a value is present, returns the left element (key) of the pair,
     * otherwise throws {@link NoSuchElementException}.
     *
     * @return the left element of the present pair, may be null
     * @throws NoSuchElementException if no value is present
     */
    public @Nullable L getKey() {
        return this.get().left();
    }

    /**
     * If a value is present, returns the left element of the pair,
     * otherwise throws {@link NoSuchElementException}.
     *
     * @return the left element of the present pair, may be null
     * @throws NoSuchElementException if no value is present
     */
    public @Nullable L left() {
        return this.get().left();
    }

    /**
     * If a value is present, returns the right element of the pair,
     * otherwise throws {@link NoSuchElementException}.
     *
     * @return the right element of the present pair, may be null
     * @throws NoSuchElementException if no value is present
     */
    public @Nullable R right() {
        return this.get().right();
    }

    /**
     * If a value is present, returns the right element (value) of the pair,
     * otherwise throws {@link NoSuchElementException}.
     *
     * @return the right element of the present pair, may be null
     * @throws NoSuchElementException if no value is present
     */
    public @Nullable R getValue() {
        return this.get().right();
    }

    // Present / Empty

    /**
     * Returns {@code true} if a value is present, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent() {
        return this.pair != null;
    }

    /**
     * Returns {@code true} if no value is present, otherwise {@code false}.
     *
     * @return {@code true} if no value is present, otherwise {@code false}
     */
    public boolean isEmpty() {
        return this.pair == null;
    }

    // IfPresent

    /**
     * If a value is present, performs the given action with the pair,
     * otherwise does nothing.
     *
     * @param action the action to be performed if a value is present
     */
    public void ifPresent(@NotNull Consumer<? super Pair<L, R>> action) {
        if (this.pair != null)
            action.accept(this.pair);
    }

    /**
     * If a value is present, performs the given action with the left and right elements
     * supplied separately, otherwise does nothing.
     *
     * @param action a {@link BiConsumer} receiving the left and right elements
     */
    public void ifPresent(@NotNull BiConsumer<? super L, ? super R> action) {
        if (this.pair != null)
            action.accept(this.pair.left(), this.pair.right());
    }

    /**
     * If a value is present, performs the given action with the pair, otherwise performs
     * the given empty-based action.
     *
     * @param action the action to be performed if a value is present
     * @param emptyAction the action to be performed if no value is present
     */
    public void ifPresentOrElse(@NotNull Consumer<? super Pair<L, R>> action, @NotNull Runnable emptyAction) {
        if (this.pair != null)
            action.accept(this.pair);
        else
            emptyAction.run();
    }

    /**
     * If a value is present, performs the given action with the left and right elements
     * supplied separately, otherwise performs the given empty-based action.
     *
     * @param action a {@link BiConsumer} receiving the left and right elements
     * @param emptyAction the action to be performed if no value is present
     */
    public void ifPresentOrElse(@NotNull BiConsumer<? super L, ? super R> action, @NotNull Runnable emptyAction) {
        if (this.pair != null)
            action.accept(this.pair.left(), this.pair.right());
        else
            emptyAction.run();
    }

    // Filter

    /**
     * If a value is present and the pair matches the given predicate, returns this
     * {@code PairOptional}, otherwise returns an empty {@code PairOptional}.
     *
     * @param predicate the predicate to apply to the pair, if present
     * @return this {@code PairOptional} if a value is present and matches, otherwise empty
     */
    public @NotNull PairOptional<L, R> filter(@NotNull Predicate<? super Pair<L, R>> predicate) {
        return this.isPresent() ? (predicate.test(this.pair) ? this : empty()) : this;
    }

    /**
     * If a value is present and the left and right elements match the given predicate,
     * returns this {@code PairOptional}, otherwise returns an empty {@code PairOptional}.
     *
     * @param predicate a {@link BiPredicate} receiving the left and right elements
     * @return this {@code PairOptional} if a value is present and matches, otherwise empty
     */
    public @NotNull PairOptional<L, R> filter(@NotNull BiPredicate<? super L, ? super R> predicate) {
        return this.isPresent() ? (predicate.test(this.pair.left(), this.pair.right()) ? this : empty()) : this;
    }

    /**
     * If a value is present and the left element matches the given predicate, returns this
     * {@code PairOptional}, otherwise returns an empty {@code PairOptional}.
     *
     * @param predicate a predicate to test the left element
     * @return this {@code PairOptional} if a value is present and the left element matches,
     *         otherwise empty
     */
    public @NotNull PairOptional<L, R> filterKey(@NotNull Predicate<? super L> predicate) {
        return this.isPresent() ? (predicate.test(this.pair.left()) ? this : empty()) : this;
    }

    /**
     * If a value is present and the right element matches the given predicate, returns this
     * {@code PairOptional}, otherwise returns an empty {@code PairOptional}.
     *
     * @param predicate a predicate to test the right element
     * @return this {@code PairOptional} if a value is present and the right element matches,
     *         otherwise empty
     */
    public @NotNull PairOptional<L, R> filterValue(@NotNull Predicate<? super R> predicate) {
        return this.isPresent() ? (predicate.test(this.pair.right()) ? this : empty()) : this;
    }

    // Map

    /**
     * If a value is present, applies the given mapping function to the pair and returns an
     * {@link Optional} describing the result, otherwise returns an empty {@link Optional}.
     * If the mapping function returns {@code null}, returns an empty {@link Optional}.
     *
     * @param mapper the mapping function to apply to the pair, if present
     * @param <U>    the type of the value returned from the mapping function
     * @return an {@link Optional} describing the mapped result, or empty if not present
     */
    public <U> @NotNull Optional<U> map(@NotNull Function<? super Pair<L, R>, ? extends U> mapper) {
        return this.isPresent() ? Optional.ofNullable(mapper.apply(this.pair)) : Optional.empty();
    }

    /**
     * If a value is present, applies the given mapping function to the left and right elements
     * and returns an {@link Optional} describing the result, otherwise returns an empty
     * {@link Optional}. If the mapping function returns {@code null}, returns empty.
     *
     * @param mapper a function receiving the left and right elements, returning the mapped result
     * @param <U>    the type of the value returned from the mapping function
     * @return an {@link Optional} describing the mapped result, or empty if not present
     */
    public <U> @NotNull Optional<U> map(@NotNull BiFunction<? super L, ? super R, ? extends U> mapper) {
        return this.isPresent() ? Optional.ofNullable(mapper.apply(this.pair.left(), this.pair.right())) : Optional.empty();
    }

    /**
     * If a value is present, applies the given mapping function to the left and right elements
     * and returns a {@code PairOptional} describing the resulting pair. If the mapped pair is
     * {@code null} or has a {@code null} left element, returns an empty {@code PairOptional}.
     *
     * @param mapper a function receiving the left and right elements, returning a new {@link Pair}
     * @param <U>    the left type of the resulting pair
     * @param <V>    the right type of the resulting pair
     * @return a {@code PairOptional} describing the mapped pair, or empty if not present
     */
    public <U, V> @NotNull PairOptional<U, V> mapPair(@NotNull BiFunction<? super L, ? super R, ? extends Pair<U, V>> mapper) {
        return this.isPresent() ? PairOptional.ofNullable(mapper.apply(this.pair.left(), this.pair.right())) : empty();
    }

    /**
     * If a value is present, applies the given mapping function to the left element and returns
     * a {@code PairOptional} with the mapped key and the original right element. If the mapped
     * key is {@code null}, returns an empty {@code PairOptional}.
     *
     * @param mapper a function to apply to the left element
     * @param <U>    the new left element type
     * @return a {@code PairOptional} with the mapped key, or empty if not present or key is null
     */
    public <U> @NotNull PairOptional<U, R> mapKey(@NotNull Function<? super L, ? extends U> mapper) {
        return this.isPresent() ? PairOptional.ofNullable(mapper.apply(this.pair.left()), this.pair.right()) : empty();
    }

    /**
     * If a value is present, applies the given mapping function to the right element and returns
     * a {@code PairOptional} with the original left element and the mapped value.
     * If the original left element is {@code null}, returns an empty {@code PairOptional}.
     *
     * @param mapper a function to apply to the right element
     * @param <U>    the new right element type
     * @return a {@code PairOptional} with the mapped value, or empty if not present
     */
    public <U> @NotNull PairOptional<L, U> mapValue(@NotNull Function<? super R, ? extends U> mapper) {
        return this.isPresent() ? PairOptional.ofNullable(this.pair.left(), mapper.apply(this.pair.right())) : empty();
    }

    // FlatMap

    /**
     * If a value is present, applies the given {@code PairOptional}-bearing mapping function to
     * the pair and returns the result, otherwise returns an empty {@link Optional}.
     * <p>
     * Unlike {@link #map(Function)}, the mapping function already returns an {@link Optional},
     * so {@code flatMap} does not wrap it in an additional layer.
     *
     * @param mapper the mapping function to apply to the pair, if present
     * @param <UL>    the left type of the pair returned by the mapping function
     * @param <UR>    the right type of the pair returned by the mapping function
     * @return the result of applying the mapping function, or an empty {@link Optional}
     */
    @SuppressWarnings("unchecked")
    public <UL, UR> @NotNull Optional<Pair<UL, UR>> flatMap(@NotNull Function<? super Pair<L, R>, ? extends Optional<? extends Pair<UL, UR>>> mapper) {
        if (!isPresent())
            return Optional.empty();

        return (Optional<Pair<UL, UR>>) mapper.apply(this.pair);
    }

    /**
     * If a value is present, applies the given {@code PairOptional}-bearing mapping function to
     * the left and right elements and returns the result, otherwise returns an empty
     * {@code PairOptional}.
     *
     * @param mapper a function receiving the left and right elements, returning a {@code PairOptional}
     * @param <UL>    the left type of the resulting {@code PairOptional}
     * @param <UR>    the right type of the resulting {@code PairOptional}
     * @return the result of applying the mapping function, or an empty {@code PairOptional}
     */
    public <UL, UR> @NotNull PairOptional<UL, UR> flatMap(@NotNull BiFunction<? super L, ? super R, ? extends PairOptional<UL, UR>> mapper) {
        return this.isPresent() ? mapper.apply(this.pair.left(), this.pair.right()) : empty();
    }

    // Or

    /**
     * If a value is present, returns this {@code PairOptional}, otherwise returns the
     * {@code PairOptional} produced by the supplying function.
     *
     * @param supplier a function that produces a fallback {@code PairOptional}
     * @return this {@code PairOptional} if present, otherwise the result of {@code supplier}
     */
    @SuppressWarnings("unchecked")
    public @NotNull PairOptional<L, R> or(@NotNull Supplier<? extends PairOptional<? extends L, ? extends R>> supplier) {
        return this.isPresent() ? this : (PairOptional<L, R>) supplier.get();
    }

    /**
     * If a value is present, returns an {@link Optional} describing the left element,
     * otherwise returns the {@link Optional} produced by the supplying function.
     *
     * @param supplier a function that produces a fallback {@link Optional} for the left element
     * @return an {@link Optional} of the left element if present, otherwise the result of {@code supplier}
     */
    @SuppressWarnings("unchecked")
    public @NotNull Optional<L> orKey(@NotNull Supplier<? extends Optional<? extends L>> supplier) {
        return this.isPresent() ? Optional.ofNullable(this.pair.left()) : (Optional<L>) supplier.get();
    }

    /**
     * If a value is present, returns an {@link Optional} describing the right element,
     * otherwise returns the {@link Optional} produced by the supplying function.
     *
     * @param supplier a function that produces a fallback {@link Optional} for the right element
     * @return an {@link Optional} of the right element if present, otherwise the result of {@code supplier}
     */
    @SuppressWarnings("unchecked")
    public @NotNull Optional<R> orValue(@NotNull Supplier<? extends Optional<? extends R>> supplier) {
        return this.isPresent() ? Optional.ofNullable(this.pair.right()) : (Optional<R>) supplier.get();
    }

    // OrElse

    /**
     * If a value is present, returns the pair, otherwise returns {@code other}.
     *
     * @param other the value to be returned if no value is present, may be {@code null}
     * @return the pair if present, otherwise {@code other}
     */
    public @Nullable Pair<L, R> orElse(@Nullable Pair<L, R> other) {
        return this.pair != null ? this.pair : other;
    }

    /**
     * If a value is present, returns the left element (key), otherwise returns {@code other}.
     *
     * @param other the value to be returned if no value is present, may be {@code null}
     * @return the left element if present, otherwise {@code other}
     */
    public @Nullable L orElseKey(@Nullable L other) {
        return this.pair != null ? this.pair.left() : other;
    }

    /**
     * If a value is present, returns the right element (value), otherwise returns {@code other}.
     *
     * @param other the value to be returned if no value is present, may be {@code null}
     * @return the right element if present, otherwise {@code other}
     */
    public @Nullable R orElseValue(@Nullable R other) {
        return this.pair != null ? this.pair.right() : other;
    }

    /**
     * If a value is present, returns the pair, otherwise returns the result produced by
     * the supplying function.
     *
     * @param supplier the supplying function that produces a fallback pair
     * @return the pair if present, otherwise the result of {@code supplier}
     */
    public @Nullable Pair<L, R> orElseGet(@NotNull Supplier<? extends Pair<L, R>> supplier) {
        return this.pair != null ? this.pair : supplier.get();
    }

    /**
     * If a value is present, returns the left element (key), otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a fallback left value
     * @return the left element if present, otherwise the result of {@code supplier}
     */
    public @Nullable L orElseGetKey(@NotNull Supplier<? extends L> supplier) {
        return this.pair != null ? this.pair.left() : supplier.get();
    }

    /**
     * If a value is present, returns the right element (value), otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a fallback right value
     * @return the right element if present, otherwise the result of {@code supplier}
     */
    public @Nullable R orElseGetValue(@NotNull Supplier<? extends R> supplier) {
        return this.pair != null ? this.pair.right() : supplier.get();
    }

    /**
     * If a value is present, returns the pair, otherwise throws {@link NoSuchElementException}.
     *
     * @return the non-{@code null} pair described by this {@code PairOptional}
     * @throws NoSuchElementException if no value is present
     */
    public @NotNull Pair<L, R> orElseThrow() {
        return this.get();
    }

    /**
     * If a value is present, returns the pair, otherwise throws an exception produced by
     * the exception supplying function.
     *
     * @apiNote
     * A method reference to the exception constructor with an empty argument list can be used
     * as the supplier. For example, {@code IllegalStateException::new}.
     *
     * @param <X>               the type of the exception to be thrown
     * @param exceptionSupplier the supplying function that produces an exception to be thrown
     * @return the pair, if present
     * @throws X if no value is present
     */
    public <X extends Throwable> @NotNull Pair<L, R> orElseThrow(@NotNull Supplier<? extends X> exceptionSupplier) throws X {
        if (this.pair == null)
            throw exceptionSupplier.get();

        return this.pair;
    }

    // Stream

    /**
     * If a value is present, returns a {@link PairStream} containing only that pair,
     * otherwise returns an empty {@link PairStream}.
     *
     * @return a {@code PairStream} containing the pair if present, otherwise empty
     */
    public @NotNull PairStream<L, R> stream() {
        return this.isPresent() ? PairStream.of(Stream.<Map.Entry<L, R>>of(this.pair)) : PairStream.empty();
    }

    // Object

    /**
     * Returns {@code true} if the other object is a {@code PairOptional} with an equal value.
     *
     * @param obj an object to be tested for equality
     * @return {@code true} if the other object is equal to this {@code PairOptional}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        return obj instanceof PairOptional<?, ?> other
            && Objects.equals(pair, other.pair);
    }

    /**
     * Returns the hash code of the pair if present, otherwise {@code 0}.
     *
     * @return the hash code of the present pair, or {@code 0} if empty
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(pair);
    }

    /**
     * Returns a string representation of this {@code PairOptional} suitable for debugging.
     *
     * @return a non-empty string representation of this instance
     */
    @Override
    public @NotNull String toString() {
        return pair != null
            ? String.format("PairOptional%s", pair)
            : "PairOptional.empty";
    }

}
