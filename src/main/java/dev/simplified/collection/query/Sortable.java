package dev.simplified.collection.query;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.tuple.pair.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * A functional interface extending {@link Searchable} with methods that return single results
 * ({@link Optional} or nullable) instead of streams. Provides {@code findFirst}, {@code findLast},
 * {@code containsFirst}, {@code matchFirst}, and {@code matchLast} families of query methods.
 *
 * @param <E> the element type of the sortable collection
 */
@FunctionalInterface
public interface Sortable<E> extends Searchable<E> {

    // --- CONTAINS FIRST ---

    /**
     * Returns the first element whose list-valued field contains the given value, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param function the list-field extractor
     * @param value the value to check for containment
     * @param <S>      the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull Function<E, List<S>> function, S value) {
        return this.containsFirst(SearchFunction.Match.ALL, function, value);
    }

    /**
     * Returns the first element whose list-valued field contains the given value, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the list-field extractor
     * @param value the value to check for containment
     * @param <S>      the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction.Match match, @NotNull Function<E, List<S>> function, S value) {
        return this.containsFirst(match, Pair.of(function, value));
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull Pair<Function<E, List<S>>, S>... predicates) {
        return this.containsFirst(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) {
        return this.containsFirst(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, List<S>>, S>... predicates) {
        return this.containsFirst(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, using the specified match mode.
     * This is the terminal overload that delegates to {@link #containsAll} and takes the first result.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) {
        return this.containsAll(match, predicates).findFirst();
    }

    /**
     * Returns the first element whose list-valued field contains the given value, or {@code null} if none match.
     *
     * @param function the list-field extractor
     * @param value the value to check for containment
     * @param <S>      the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull SearchFunction<E, List<S>> function, S value) {
        return this.containsFirst(function, value).orElse(null);
    }

    /**
     * Returns the first element whose list-valued field contains the given value using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the list-field extractor
     * @param value the value to check for containment
     * @param <S>      the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Function<E, List<S>> function, S value) {
        return this.containsFirstOrNull(match, Pair.of(function, value));
    }

    /**
     * Returns the first element whose list-valued fields contain the given values using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, List<S>>, S>... predicates) {
        return this.containsFirstOrNull(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the first element whose list-valued fields contain the given values using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) {
        return this.containsFirst(match, predicates).orElse(null);
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, or {@code null} if none match.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull Pair<Function<E, List<S>>, S>... predicates) {
        return this.containsFirstOrNull(Concurrent.newList(predicates));
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, or {@code null} if none match.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) {
        return this.containsFirst(predicates).orElse(null);
    }

    // --- FIND FIRST ---

    /**
     * Returns the first element whose extracted field value equals the given value, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull Function<E, S> function, S value) {
        return this.findFirst(SearchFunction.Match.ALL, function, value);
    }

    /**
     * Returns the first element whose extracted field value equals the given value, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) {
        return this.findFirst(match, Pair.of(function, value));
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findFirst(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        return this.findFirst(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findFirst(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, using the specified match mode.
     * This is the terminal overload that performs null-safe equality comparison and returns the first result.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        return this.compare(
            match,
            (predicate, it, value) -> {
                try {
                    return Objects.equals(predicate.apply(it), value);
                } catch (NullPointerException nullPointerException) {
                    return false;
                }
            },
            predicates
        ).findFirst();
    }

    /**
     * Returns the first element whose extracted field value equals the given value, or {@code null} if none match.
     *
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull SearchFunction<E, S> function, S value) {
        return this.findFirst(function, value).orElse(null);
    }

    /**
     * Returns the first element whose extracted field value equals the given value using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) {
        return this.findFirstOrNull(match, Pair.of(function, value));
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findFirstOrNull(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        return this.findFirst(match, predicates).orElse(null);
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, or {@code null} if none match.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findFirstOrNull(Concurrent.newList(predicates));
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, or {@code null} if none match.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        return this.findFirst(predicates).orElse(null);
    }

    // --- FIND LAST ---

    /**
     * Returns the last element whose extracted field value equals the given value, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull Function<E, S> function, S value) {
        return this.findLast(SearchFunction.Match.ALL, function, value);
    }

    /**
     * Returns the last element whose extracted field value equals the given value, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) {
        return this.findLast(match, Pair.of(function, value));
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findLast(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        return this.findLast(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findLast(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, using the specified match mode.
     * This is the terminal overload that performs null-safe equality comparison and reduces to the last result.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        return this.compare(
            match,
            (predicate, it, value) -> {
                try {
                    return Objects.equals(predicate.apply(it), value);
                } catch (NullPointerException nullPointerException) {
                    return false;
                }
            },
            predicates
        ).reduce((first, second) -> second);
    }

    // --- FIND LAST OR NULL ---

    /**
     * Returns the last element whose extracted field value equals the given value, or {@code null} if none match.
     *
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull SearchFunction<E, S> function, S value) {
        return this.findLast(function, value).orElse(null);
    }

    /**
     * Returns the last element whose extracted field value equals the given value using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) {
        return this.findLastOrNull(match, Pair.of(function, value));
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findLastOrNull(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        return this.findLast(match, predicates).orElse(null);
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, or {@code null} if none match.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findLastOrNull(Concurrent.newList(predicates));
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, or {@code null} if none match.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        return this.findLast(predicates).orElse(null);
    }

    // --- MATCH FIRST ---

    /**
     * Returns the first element that satisfies the given predicates, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the predicates to test against each element
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default @NotNull Optional<E> matchFirst(@NotNull Predicate<E>... predicates) {
        return this.matchFirst(Concurrent.newList(predicates));
    }

    /**
     * Returns the first element that satisfies the given predicates, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the predicates to test against each element
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default @NotNull Optional<E> matchFirst(@NotNull Iterable<Predicate<E>> predicates) {
        return this.matchFirst(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element that satisfies the given predicates, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default @NotNull Optional<E> matchFirst(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) {
        return this.matchFirst(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the first element that satisfies the given predicates, using the specified match mode.
     * This is the terminal overload that converts predicates to comparison pairs and returns the first result.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default @NotNull Optional<E> matchFirst(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) {
        return this.compare(
            match,
            (predicate, it, value) -> {
                try {
                    return predicate.apply(it);
                } catch (NullPointerException nullPointerException) {
                    return false;
                }
            },
            StreamSupport.stream(predicates.spliterator(), false)
                .map(predicate -> Pair.<Function<E, Boolean>, Boolean>of(predicate::test, true))
                .collect(Concurrent.toList())
        ).findFirst();
    }

    /**
     * Returns the first element that satisfies the given predicates, or {@code null} if none match.
     *
     * @param predicates the predicates to test against each element
     * @return the first matching element, or {@code null}
     */
    default E matchFirstOrNull(@NotNull Predicate<E>... predicates) {
        return this.matchFirstOrNull(Concurrent.newList(predicates));
    }

    /**
     * Returns the first element that satisfies the given predicates, or {@code null} if none match.
     *
     * @param predicates the predicates to test against each element
     * @return the first matching element, or {@code null}
     */
    default E matchFirstOrNull(@NotNull Iterable<Predicate<E>> predicates) {
        return this.matchFirstOrNull(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element that satisfies the given predicates using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return the first matching element, or {@code null}
     */
    default E matchFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) {
        return this.matchFirstOrNull(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the first element that satisfies the given predicates using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return the first matching element, or {@code null}
     */
    default E matchFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) {
        return this.matchFirst(match, predicates).orElse(null);
    }

    // --- MATCH LAST ---

    /**
     * Returns the last element that satisfies the given predicates, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the predicates to test against each element
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default @NotNull Optional<E> matchLast(@NotNull Predicate<E>... predicates) {
        return this.matchLast(Concurrent.newList(predicates));
    }

    /**
     * Returns the last element that satisfies the given predicates, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the predicates to test against each element
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default @NotNull Optional<E> matchLast(@NotNull Iterable<Predicate<E>> predicates) {
        return this.matchLast(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the last element that satisfies the given predicates, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default @NotNull Optional<E> matchLast(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) {
        return this.matchLast(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the last element that satisfies the given predicates, using the specified match mode.
     * This is the terminal overload that converts predicates to comparison pairs and reduces to the last result.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default @NotNull Optional<E> matchLast(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) {
        return this.compare(
            match,
            (predicate, it, value) -> {
                try {
                    return predicate.apply(it);
                } catch (NullPointerException nullPointerException) {
                    return false;
                }
            },
            StreamSupport.stream(predicates.spliterator(), false)
                .map(predicate -> Pair.<Function<E, Boolean>, Boolean>of(predicate::test, true))
                .collect(Concurrent.toList())
        ).reduce((first, second) -> second);
    }

    /**
     * Returns the last element that satisfies the given predicates, or {@code null} if none match.
     *
     * @param predicates the predicates to test against each element
     * @return the last matching element, or {@code null}
     */
    default E matchLastOrNull(@NotNull Predicate<E>... predicates) {
        return this.matchLastOrNull(Concurrent.newList(predicates));
    }

    /**
     * Returns the last element that satisfies the given predicates, or {@code null} if none match.
     *
     * @param predicates the predicates to test against each element
     * @return the last matching element, or {@code null}
     */
    default E matchLastOrNull(@NotNull Iterable<Predicate<E>> predicates) {
        return this.matchLastOrNull(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the last element that satisfies the given predicates using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return the last matching element, or {@code null}
     */
    default E matchLastOrNull(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) {
        return this.matchLastOrNull(match, Concurrent.newList(predicates));
    }

    /**
     * Returns the last element that satisfies the given predicates using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return the last matching element, or {@code null}
     */
    default E matchLastOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) {
        return this.matchLast(match, predicates).orElse(null);
    }

}
