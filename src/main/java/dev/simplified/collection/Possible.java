package dev.simplified.collection;

import dev.simplified.annotations.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A container object which may contain a non-{@code null} value, or may carry one of two distinct
 * reasons for carrying none. If a value is present, {@link #isPresent()} returns {@code true}.
 * If no value is present, the object is either <i>empty</i> or <i>absent</i>, and those two are not
 * the same object, are not equal to one another, and are told apart by {@link #isEmpty()} and
 * {@link #isAbsent()}.
 * <p>
 * <i>Empty</i> means the question was well-formed and the answer is genuinely nothing.
 * <i>Absent</i> means there was nothing to ask about, so no answer exists to give. A map lookup is
 * the clearest case of the difference: a key bound to nothing answers empty, and a key that is not in
 * the map at all answers absent, where {@link Optional} collapses both onto
 * {@link Optional#empty()} and leaves the caller to recover the difference by asking a second
 * question.
 * <p>
 * Additional methods that depend on which of the three states holds are provided, such as
 * {@link #orElse(Object) orElse()} (returns a default value if no value is present),
 * {@link #ifPresent(Consumer) ifPresent()} (performs an action if a value is present), and
 * {@link #orAbsent(Supplier) orAbsent()} (substitutes only for the absent state, leaving an empty
 * one empty).
 *
 * @apiNote
 * {@code Possible} is primarily intended for use as a method return type where "no result" has more
 * than one meaning and the caller must be able to act on which one it was, and where using
 * {@code null} to encode the second meaning is likely to cause errors. A variable whose type is
 * {@code Possible} should never itself be {@code null}; it should always point to a {@code Possible}
 * instance.
 *
 * @apiNote
 * <b>{@link #isEmpty()} is not the negation of {@link #isPresent()}</b>, which is the one place this
 * type does not behave like {@link Optional}. It answers {@code true} for the empty state alone, so
 * code translated from {@code Optional} by replacing the type keeps compiling and stops handling the
 * absent state. {@link #isMissing()} is the negation, and is what a caller that does not care why
 * should ask.
 *
 * @param <T> the type of value
 */
@EqualsAndHashCode
public final class Possible<T> {

    /**
     * Common instance for {@link #empty()}.
     */
    private static final Possible<?> EMPTY = new Possible<>(State.EMPTY, null);

    /**
     * Common instance for {@link #absent()}.
     */
    private static final Possible<?> ABSENT = new Possible<>(State.ABSENT, null);

    /**
     * Which of the three states this instance holds. It is compared, so an empty and an absent
     * instance are unequal despite both carrying no value.
     */
    private final @NotNull State state;

    /**
     * The wrapped value; {@code null} in both value-less states.
     */
    private final @Nullable T value;

    private Possible(@NotNull State state, @Nullable T value) {
        this.state = state;
        this.value = value;
    }

    /**
     * Which of the three things a {@code Possible} can be.
     */
    private enum State {

        /** A value is present. */
        PRESENT,

        /** No value is present, and the question was well-formed. */
        EMPTY,

        /** No value is present, because there was nothing to ask about. */
        ABSENT

    }

    // Create

    /**
     * Returns an empty {@code Possible} instance. No value is present, and the reason is that the
     * answer is genuinely nothing.
     *
     * @apiNote
     * Avoid testing emptiness by comparing with {@code ==} against instances returned by
     * {@code Possible.empty()}. There is no guarantee it is a singleton. Use {@link #isEmpty()} or
     * {@link #isPresent()} instead.
     *
     * @param <T> the type of the non-existent value
     * @return an empty {@code Possible}
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull Possible<T> empty() {
        return (Possible<T>) EMPTY;
    }

    /**
     * Returns an absent {@code Possible} instance. No value is present, and the reason is that there
     * was nothing to ask about.
     *
     * @apiNote
     * Avoid testing absence by comparing with {@code ==} against instances returned by
     * {@code Possible.absent()}. There is no guarantee it is a singleton. Use {@link #isAbsent()}
     * instead.
     *
     * @param <T> the type of the non-existent value
     * @return an absent {@code Possible}
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull Possible<T> absent() {
        return (Possible<T>) ABSENT;
    }

    /**
     * Returns a {@code Possible} describing the given non-{@code null} value.
     *
     * @param value the value to describe, must be non-{@code null}
     * @param <T> the type of the value
     * @return a {@code Possible} with the value present
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static <T> @NotNull Possible<T> of(@NotNull T value) {
        if (value == null)
            throw new NullPointerException("A present Possible carries a value");

        return new Possible<>(State.PRESENT, value);
    }

    /**
     * Returns a {@code Possible} describing the given value, or an empty {@code Possible} if it is
     * {@code null}.
     *
     * @apiNote
     * The {@code null} maps to empty rather than absent, because a {@code null} says the answer was
     * nothing and says nothing about whether there was a question. A caller that means absent says so
     * with {@link #absent()}.
     *
     * @param value the possibly-{@code null} value to describe
     * @param <T> the type of the value
     * @return a {@code Possible} with the value present if it is non-{@code null}, otherwise an empty
     *         {@code Possible}
     */
    public static <T> @NotNull Possible<T> ofNullable(@Nullable T value) {
        return value == null ? empty() : new Possible<>(State.PRESENT, value);
    }

    /**
     * Returns a {@code Possible} describing the value contained in the given {@link Optional}, or an
     * empty {@code Possible} if the {@code Optional} is empty.
     *
     * @apiNote
     * An empty {@link Optional} maps to empty rather than absent, which is the faithful reading:
     * {@link Optional} cannot express absence, so it never carries one to recover. A caller who knows
     * the empty meant absence converts it with {@code orEmpty(Possible::absent)}.
     *
     * @param value a non-null {@code Optional} that may or may not contain a value
     * @param <T> the type of the value
     * @return a {@code Possible} with the value present if the {@code Optional} contains one,
     *         otherwise an empty {@code Possible}
     */
    public static <T> @NotNull Possible<T> ofOptional(@NotNull Optional<? extends T> value) {
        return value.<Possible<T>>map(Possible::of).orElseGet(Possible::empty);
    }

    // Get

    /**
     * If a value is present, returns the value, otherwise throws {@link NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code Possible}
     * @throws NoSuchElementException if no value is present
     */
    public @NotNull T get() {
        if (this.value == null)
            throw new NoSuchElementException(this.state == State.ABSENT ? "No value absent" : "No value present");

        return this.value;
    }

    // Present / Empty / Absent

    /**
     * Returns {@code true} if a value is present, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent() {
        return this.state == State.PRESENT;
    }

    /**
     * Returns {@code true} if no value is present and the answer is genuinely nothing, otherwise
     * {@code false}.
     *
     * @apiNote
     * This answers {@code false} for an absent {@code Possible}, unlike {@link Optional#isEmpty()},
     * which answers for every value-less state. {@link #isMissing()} is the one that behaves the way
     * {@code Optional} does.
     *
     * @return {@code true} if this is empty, otherwise {@code false}
     */
    public boolean isEmpty() {
        return this.state == State.EMPTY;
    }

    /**
     * Returns {@code true} if no value is present because there was nothing to ask about, otherwise
     * {@code false}.
     *
     * @return {@code true} if this is absent, otherwise {@code false}
     */
    public boolean isAbsent() {
        return this.state == State.ABSENT;
    }

    /**
     * Returns {@code true} if no value is present, for either reason, otherwise {@code false}.
     *
     * @return {@code true} if this is empty or absent, otherwise {@code false}
     */
    public boolean isMissing() {
        return this.state != State.PRESENT;
    }

    // IfPresent

    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.
     *
     * @param action the action to be performed if a value is present
     */
    public void ifPresent(@NotNull Consumer<? super T> action) {
        if (this.value != null)
            action.accept(this.value);
    }

    /**
     * If a value is present, performs the given action with the value, otherwise performs the given
     * missing-based action.
     *
     * @param action the action to be performed if a value is present
     * @param missingAction the action to be performed if no value is present, for either reason
     */
    public void ifPresentOrElse(@NotNull Consumer<? super T> action, @NotNull Runnable missingAction) {
        if (this.value != null)
            action.accept(this.value);
        else
            missingAction.run();
    }

    /**
     * If a value is present, performs the given action with the value, otherwise performs whichever
     * of the two value-less actions matches this instance's reason.
     *
     * @param action the action to be performed if a value is present
     * @param emptyAction the action to be performed if this is empty
     * @param absentAction the action to be performed if this is absent
     */
    public void ifPresentOrElse(
        @NotNull Consumer<? super T> action, @NotNull Runnable emptyAction, @NotNull Runnable absentAction) {
        switch (this.state) {
            case PRESENT -> action.accept(this.value);
            case EMPTY -> emptyAction.run();
            case ABSENT -> absentAction.run();
        }
    }

    /**
     * If this is absent, performs the given action, otherwise does nothing.
     *
     * @param action the action to be performed if this is absent
     */
    public void ifAbsent(@NotNull Runnable action) {
        if (this.state == State.ABSENT)
            action.run();
    }

    // Filter

    /**
     * If a value is present and it matches the given predicate, returns this {@code Possible},
     * otherwise returns an empty {@code Possible}. A value-less {@code Possible} is returned as it is,
     * so filtering never turns an absent one into an empty one.
     *
     * @apiNote
     * A present value the predicate rejects yields empty rather than absent, because the value was
     * there to test - the question was well-formed and the answer, after filtering, is nothing.
     *
     * @param predicate the predicate to apply to the value, if present
     * @return this {@code Possible} if a value is present and matches, otherwise empty
     */
    public @NotNull Possible<T> filter(@NotNull Predicate<? super T> predicate) {
        if (this.value == null) return this;
        return predicate.test(this.value) ? this : empty();
    }

    // Map

    /**
     * If a value is present, applies the given mapping function to it and returns a {@code Possible}
     * describing the result, otherwise returns a {@code Possible} in this one's state. If the mapping
     * function returns {@code null}, returns an empty {@code Possible}.
     * <p>
     * A value-less {@code Possible} keeps its reason across a mapping, so an absent one maps to an
     * absent one. That is what lets a caller postpone the decision about what absence means until
     * after a chain of transformations.
     *
     * @param mapper the mapping function to apply to the value, if present
     * @param <U> the type of the value returned from the mapping function
     * @param <R> the widened result type
     * @return a {@code Possible} describing the mapped result, or this one's value-less state
     */
    @SuppressWarnings("unchecked")
    public <U extends R, R> @NotNull Possible<R> map(@NotNull Function<? super T, ? extends U> mapper) {
        if (this.value == null) return (Possible<R>) this;
        return ofNullable(mapper.apply(this.value));
    }

    // FlatMap

    /**
     * If a value is present, applies the given {@code Possible}-bearing mapping function to it and
     * returns the result, otherwise returns a {@code Possible} in this one's state.
     * <p>
     * Unlike {@link #map(Function)}, the mapping function already returns a {@code Possible}, so
     * {@code flatMap} does not wrap it in an additional layer - and the mapper decides for itself
     * which value-less state its own result carries.
     *
     * @param mapper the mapping function to apply to the value, if present
     * @param <U> the type of the value of the {@code Possible} returned by the mapping function
     * @return the result of applying the mapping function, or this one's value-less state
     */
    @SuppressWarnings("unchecked")
    public <U> @NotNull Possible<U> flatMap(@NotNull Function<? super T, ? extends Possible<? extends U>> mapper) {
        if (this.value == null) return (Possible<U>) this;
        return (Possible<U>) mapper.apply(this.value);
    }

    // Or

    /**
     * If a value is present, returns this {@code Possible}, otherwise returns the {@code Possible}
     * produced by the supplying function. Both value-less states are replaced.
     *
     * @param supplier a function that produces a fallback {@code Possible}
     * @return this {@code Possible} if a value is present, otherwise the result of {@code supplier}
     */
    @SuppressWarnings("unchecked")
    public @NotNull Possible<T> or(@NotNull Supplier<? extends Possible<? extends T>> supplier) {
        return this.isPresent() ? this : (Possible<T>) supplier.get();
    }

    /**
     * If this is absent, returns the {@code Possible} produced by the supplying function, otherwise
     * returns this {@code Possible}. A present one and an empty one are both returned as they are.
     *
     * @apiNote
     * This is the method the two states exist for. It lets a caller answer for absence without
     * disturbing an empty result, which {@link #or(Supplier)} cannot do and which {@link Optional}
     * cannot express at all.
     *
     * @param supplier a function that produces a fallback {@code Possible} for the absent state
     * @return the result of {@code supplier} if this is absent, otherwise this {@code Possible}
     */
    @SuppressWarnings("unchecked")
    public @NotNull Possible<T> orAbsent(@NotNull Supplier<? extends Possible<? extends T>> supplier) {
        return this.state == State.ABSENT ? (Possible<T>) supplier.get() : this;
    }

    /**
     * If this is empty, returns the {@code Possible} produced by the supplying function, otherwise
     * returns this {@code Possible}. A present one and an absent one are both returned as they are.
     *
     * @param supplier a function that produces a fallback {@code Possible} for the empty state
     * @return the result of {@code supplier} if this is empty, otherwise this {@code Possible}
     */
    @SuppressWarnings("unchecked")
    public @NotNull Possible<T> orEmpty(@NotNull Supplier<? extends Possible<? extends T>> supplier) {
        return this.state == State.EMPTY ? (Possible<T>) supplier.get() : this;
    }

    // OrElse

    /**
     * If a value is present, returns the value, otherwise returns {@code other}.
     *
     * @param other the value to be returned if no value is present, may be {@code null}
     * @return the value if present, otherwise {@code other}
     */
    public @Nullable T orElse(@Nullable T other) {
        return this.value != null ? this.value : other;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result produced by the
     * supplying function.
     *
     * @param supplier the supplying function that produces a fallback value
     * @return the value if present, otherwise the result of {@code supplier}
     */
    public @Nullable T orElseGet(@NotNull Supplier<? extends T> supplier) {
        return this.value != null ? this.value : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws {@link NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code Possible}
     * @throws NoSuchElementException if no value is present
     */
    public @NotNull T orElseThrow() {
        return this.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws an exception produced by the
     * exception supplying function.
     *
     * @apiNote
     * A method reference to the exception constructor with an empty argument list can be used as the
     * supplier. For example, {@code IllegalStateException::new}.
     *
     * @param <X> the type of the exception to be thrown
     * @param exceptionSupplier the supplying function that produces an exception to be thrown
     * @return the value, if present
     * @throws X if no value is present
     */
    public <X extends Throwable> @NotNull T orElseThrow(@NotNull Supplier<? extends X> exceptionSupplier) throws X {
        if (this.value == null)
            throw exceptionSupplier.get();

        return this.value;
    }

    // Optional / Stream

    /**
     * Returns an {@link Optional} describing this one's value, or an empty {@link Optional} if no
     * value is present.
     *
     * @apiNote
     * Both value-less states collapse onto {@link Optional#empty()}, because that is all
     * {@link Optional} can carry. Converting is therefore lossy in one direction and should happen at
     * the edge of an API rather than inside one.
     *
     * @return an {@link Optional} describing the value, or empty if no value is present
     */
    public @NotNull Optional<T> toOptional() {
        return Optional.ofNullable(this.value);
    }

    /**
     * If a value is present, returns a {@link Stream} containing only that value, otherwise returns an
     * empty {@link Stream}.
     *
     * @return a {@code Stream} containing the value if present, otherwise empty
     */
    public @NotNull Stream<T> stream() {
        return this.value != null ? Stream.of(this.value) : Stream.empty();
    }

    // Object

    /**
     * Returns a string representation of this {@code Possible} suitable for debugging.
     *
     * @return a non-empty string representation of this instance
     */
    @Override
    public @NotNull String toString() {
        return switch (this.state) {
            case PRESENT -> String.format("Possible[%s]", this.value);
            case EMPTY -> "Possible.empty";
            case ABSENT -> "Possible.absent";
        };
    }

}
