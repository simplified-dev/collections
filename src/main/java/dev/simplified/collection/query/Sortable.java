package dev.simplified.collection.query;

import dev.simplified.collection.tuple.pair.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A functional interface extending {@link Indexable} with methods that return single results
 * ({@link Optional} or nullable) instead of streams. Provides {@code findFirst}, {@code findLast},
 * {@code containsFirst}, {@code matchFirst}, and {@code matchLast} families of query methods.
 *
 * <p>Every equality family here funnels through {@link Searchable#findAll} or
 * {@link Searchable#containsAll}, which is what lets an index serve all of them without any of them
 * knowing an index exists.
 *
 * @param <E> the element type of the sortable collection
 */
@FunctionalInterface
public interface Sortable<E> extends Indexable<E> {

    // --- CONTAINS FIRST ---

    /**
     * Returns the first element whose list-valued field contains the given value, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param function the list-field extractor
     * @param value the value to check for containment
     * @param <S> the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction<E, List<S>> function, S value) {
        return this.containsFirst(SearchFunction.Match.ALL, function, value);
    }

    /**
     * Returns the first element whose list-valued field contains the given value, using the specified match mode.
     *
     * <p>Read off the containment index directly when one covers the property, and otherwise
     * scanned through the pair the fall-through builds.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the list-field extractor
     * @param value the value to check for containment
     * @param <S> the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction.Match match, @NotNull SearchFunction<E, List<S>> function, S value) {
        List<E> indexed = this.indexes().lookupContaining(PropertyReference.of(function), function, value);

        if (indexed == null)
            return this.containsFirst(match, Pair.of(function, value));

        return indexed.isEmpty() ? Optional.empty() : Optional.of(indexed.getFirst());
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S> the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull Pair<SearchFunction<E, List<S>>, S>... predicates) {
        return this.containsFirst(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S> the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull Iterable<Pair<SearchFunction<E, List<S>>, S>> predicates) {
        return this.containsFirst(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S> the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction.Match match, @NotNull Pair<SearchFunction<E, List<S>>, S>... predicates) {
        return this.containsFirst(match, Arrays.asList(predicates));
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, using the specified match mode.
     * This is the terminal overload that delegates to {@link #containsAll} and takes the first result.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S> the element type within the list field
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<SearchFunction<E, List<S>>, S>> predicates) {
        return this.containsAll(match, predicates).findFirst();
    }

    /**
     * Returns the first element whose list-valued field contains the given value, or {@code null} if none match.
     *
     * @param function the list-field extractor
     * @param value the value to check for containment
     * @param <S> the element type within the list field
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
     * @param <S> the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull SearchFunction.Match match, @NotNull SearchFunction<E, List<S>> function, S value) {
        return this.containsFirst(match, function, value).orElse(null);
    }

    /**
     * Returns the first element whose list-valued fields contain the given values using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S> the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Pair<SearchFunction<E, List<S>>, S>... predicates) {
        return this.containsFirstOrNull(match, Arrays.asList(predicates));
    }

    /**
     * Returns the first element whose list-valued fields contain the given values using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S> the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<SearchFunction<E, List<S>>, S>> predicates) {
        return this.containsFirst(match, predicates).orElse(null);
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, or {@code null} if none match.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S> the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull Pair<SearchFunction<E, List<S>>, S>... predicates) {
        return this.containsFirstOrNull(Arrays.asList(predicates));
    }

    /**
     * Returns the first element whose list-valued fields contain the given values, or {@code null} if none match.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S> the element type within the list field
     * @return the first matching element, or {@code null}
     */
    default <S> E containsFirstOrNull(@NotNull Iterable<Pair<SearchFunction<E, List<S>>, S>> predicates) {
        return this.containsFirst(predicates).orElse(null);
    }

    // --- FIND FIRST ---

    /**
     * Returns the first element whose extracted field value equals the given value, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction<E, S> function, S value) {
        return this.findFirst(SearchFunction.Match.ALL, function, value);
    }

    /**
     * Returns the first element whose extracted field value equals the given value, using the specified match mode.
     *
     * <p>Read off the index directly when one covers the property. This is the shape nearly every
     * query takes, and the answer is one element, so pairing the extractor with its value and
     * running a stream over the bucket to reach it costs several times what the probe does. A
     * property no index covers falls through to the scan, which the pair and the stream are still
     * how to reach.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction.Match match, @NotNull SearchFunction<E, S> function, S value) {
        // One predicate asks the same question in either match mode, so the mode is only carried as
        // far as the scan.
        List<E> indexed = this.indexes().lookup(PropertyReference.of(function), function, value);

        if (indexed == null)
            return this.findFirst(match, Pair.of(function, value));

        return indexed.isEmpty() ? Optional.empty() : Optional.of(indexed.getFirst());
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull Pair<SearchFunction<E, S>, S>... predicates) {
        return this.findFirst(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull Iterable<Pair<SearchFunction<E, S>, S>> predicates) {
        return this.findFirst(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction.Match match, @NotNull Pair<SearchFunction<E, S>, S>... predicates) {
        return this.findFirst(match, Arrays.asList(predicates));
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, using the specified match mode.
     * This is the terminal overload that delegates to {@link #findAll} and takes the first result.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the first matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<SearchFunction<E, S>, S>> predicates) {
        return this.findAll(match, predicates).findFirst();
    }

    /**
     * Returns the first element whose extracted field value equals the given value, or {@code null} if none match.
     *
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S> the type of the compared value
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
     * @param <S> the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull SearchFunction.Match match, @NotNull SearchFunction<E, S> function, S value) {
        return this.findFirst(match, function, value).orElse(null);
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Pair<SearchFunction<E, S>, S>... predicates) {
        return this.findFirstOrNull(match, Arrays.asList(predicates));
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<SearchFunction<E, S>, S>> predicates) {
        return this.findFirst(match, predicates).orElse(null);
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, or {@code null} if none match.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull Pair<SearchFunction<E, S>, S>... predicates) {
        return this.findFirstOrNull(Arrays.asList(predicates));
    }

    /**
     * Returns the first element matching the given field-extractor/value pairs, or {@code null} if none match.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return the first matching element, or {@code null}
     */
    default <S> E findFirstOrNull(@NotNull Iterable<Pair<SearchFunction<E, S>, S>> predicates) {
        return this.findFirst(predicates).orElse(null);
    }

    // --- FIND LAST ---

    /**
     * Returns the last element whose extracted field value equals the given value, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction<E, S> function, S value) {
        return this.findLast(SearchFunction.Match.ALL, function, value);
    }

    /**
     * Returns the last element whose extracted field value equals the given value, using the specified match mode.
     *
     * <p>Read off the index directly when one covers the property. A bucket holds its elements in
     * source order, so the last of them is the answer a reduction over the whole stream arrives at.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction.Match match, @NotNull SearchFunction<E, S> function, S value) {
        List<E> indexed = this.indexes().lookup(PropertyReference.of(function), function, value);

        if (indexed == null)
            return this.findLast(match, Pair.of(function, value));

        return indexed.isEmpty() ? Optional.empty() : Optional.of(indexed.getLast());
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull Pair<SearchFunction<E, S>, S>... predicates) {
        return this.findLast(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull Iterable<Pair<SearchFunction<E, S>, S>> predicates) {
        return this.findLast(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction.Match match, @NotNull Pair<SearchFunction<E, S>, S>... predicates) {
        return this.findLast(match, Arrays.asList(predicates));
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, using the specified match mode.
     * This is the terminal overload that delegates to {@link #findAll} and reduces to the last result.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return an {@link Optional} containing the last matching element, or empty if none match
     */
    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<SearchFunction<E, S>, S>> predicates) {
        return this.findAll(match, predicates).reduce((first, second) -> second);
    }

    // --- FIND LAST OR NULL ---

    /**
     * Returns the last element whose extracted field value equals the given value, or {@code null} if none match.
     *
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S> the type of the compared value
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
     * @param <S> the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull SearchFunction.Match match, @NotNull SearchFunction<E, S> function, S value) {
        return this.findLast(match, function, value).orElse(null);
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull SearchFunction.Match match, @NotNull Pair<SearchFunction<E, S>, S>... predicates) {
        return this.findLastOrNull(match, Arrays.asList(predicates));
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs using the specified match mode, or {@code null} if none match.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<SearchFunction<E, S>, S>> predicates) {
        return this.findLast(match, predicates).orElse(null);
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, or {@code null} if none match.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull Pair<SearchFunction<E, S>, S>... predicates) {
        return this.findLastOrNull(Arrays.asList(predicates));
    }

    /**
     * Returns the last element matching the given field-extractor/value pairs, or {@code null} if none match.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return the last matching element, or {@code null}
     */
    default <S> E findLastOrNull(@NotNull Iterable<Pair<SearchFunction<E, S>, S>> predicates) {
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
        return this.matchFirst(Arrays.asList(predicates));
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
        return this.matchFirst(match, Arrays.asList(predicates));
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
        return this.matchAll(match, predicates).findFirst();
    }

    /**
     * Returns the first element that satisfies the given predicates, or {@code null} if none match.
     *
     * @param predicates the predicates to test against each element
     * @return the first matching element, or {@code null}
     */
    default E matchFirstOrNull(@NotNull Predicate<E>... predicates) {
        return this.matchFirstOrNull(Arrays.asList(predicates));
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
        return this.matchFirstOrNull(match, Arrays.asList(predicates));
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
        return this.matchLast(Arrays.asList(predicates));
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
        return this.matchLast(match, Arrays.asList(predicates));
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
        return this.matchAll(match, predicates).reduce((first, second) -> second);
    }

    /**
     * Returns the last element that satisfies the given predicates, or {@code null} if none match.
     *
     * @param predicates the predicates to test against each element
     * @return the last matching element, or {@code null}
     */
    default E matchLastOrNull(@NotNull Predicate<E>... predicates) {
        return this.matchLastOrNull(Arrays.asList(predicates));
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
        return this.matchLastOrNull(match, Arrays.asList(predicates));
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
