package dev.simplified.collection.query;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.function.TriPredicate;
import dev.simplified.collection.tuple.pair.Pair;
import dev.simplified.collection.tuple.single.SingleStream;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A functional interface providing stream-based search operations over a collection of elements.
 * Supports equality-based searching ({@link #findAll}), list-containment searching ({@link #containsAll}),
 * and predicate-based matching ({@link #matchAll}) with configurable {@link SearchFunction.Match} modes.
 *
 * @param <E> the element type of the searchable collection
 */
@FunctionalInterface
public interface Searchable<E> {

    /** A {@link SingleStream} over the elements of this searchable collection. */
    @NotNull SingleStream<E> stream();

    /**
     * Filters the stream using the given {@link TriPredicate} comparison against each predicate pair.
     * When {@code match} is {@link SearchFunction.Match#ALL ALL}, every predicate must match;
     * when {@link SearchFunction.Match#ANY ANY}, at least one predicate must match.
     *
     * @param match the match mode (ALL or ANY)
     * @param compare the comparison function applied per predicate
     * @param predicates the field-extractor/value pairs to compare against
     * @param <S>        the type of the compared value
     * @return a filtered stream of matching elements
     * @throws dev.simplified.persistence.exception.JpaException if an invalid match type is provided
     */
    default <S> @NotNull SingleStream<E> compare(@NotNull SearchFunction.Match match, @NotNull TriPredicate<Function<E, S>, E, S> compare, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        SingleStream<E> itemsCopy = this.stream();

        if (match == SearchFunction.Match.ANY) {
            itemsCopy = itemsCopy.filter(it -> {
                boolean matches = false;

                for (Pair<Function<E, S>, S> predicate : predicates)
                    matches |= compare.test(predicate.left(), it, predicate.getValue());

                return matches;
            });
        } else if (match == SearchFunction.Match.ALL) {
            for (Pair<Function<E, S>, S> predicate : predicates)
                itemsCopy = itemsCopy.filter(it -> compare.test(predicate.left(), it, predicate.right()));
        } else
            throw new IllegalArgumentException(String.format("Invalid match type '%s'", match));

        return itemsCopy;
    }

    /**
     * Filters the stream using the given {@link TriPredicate} comparison for list-containment
     * checks against each predicate pair. Behaves like {@link #compare} but operates on
     * list-valued fields.
     *
     * @param match the match mode (ALL or ANY)
     * @param compare the containment comparison function applied per predicate
     * @param predicates the list-field-extractor/value pairs to check containment against
     * @param <S>        the element type within the list field
     * @return a filtered stream of matching elements
     * @throws dev.simplified.persistence.exception.JpaException if an invalid match type is provided
     */
    default <S> @NotNull SingleStream<E> contains(@NotNull SearchFunction.Match match, @NotNull TriPredicate<Function<E, List<S>>, E, S> compare, @NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) {
        SingleStream<E> itemsCopy = this.stream();

        if (match == SearchFunction.Match.ANY) {
            itemsCopy = itemsCopy.filter(it -> {
                boolean matches = false;

                for (Pair<Function<E, List<S>>, S> predicate : predicates)
                    matches |= compare.test(predicate.left(), it, predicate.getValue());

                return matches;
            });
        } else if (match == SearchFunction.Match.ALL) {
            for (Pair<Function<E, List<S>>, S> predicate : predicates)
                itemsCopy = itemsCopy.filter(it -> compare.test(predicate.left(), it, predicate.right()));
        } else
            throw new IllegalArgumentException(String.format("Invalid match type '%s'", match));

        return itemsCopy;
    }

    // --- CONTAINS ALL ---

    /**
     * Returns all elements whose list-valued field contains the given value, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param function the list-field extractor
     * @param value the value to check for containment
     * @param <S>      the element type within the list field
     * @return a stream of elements whose list field contains the value
     */
    default <S> @NotNull Stream<E> containsAll(@NotNull Function<E, List<S>> function, S value) {
        return this.containsAll(SearchFunction.Match.ALL, function, value);
    }

    /**
     * Returns all elements whose list-valued fields contain the given values, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return a stream of elements matching all containment predicates
     */
    default <S> @NotNull Stream<E> containsAll(@NotNull Pair<Function<E, List<S>>, S>... predicates) {
        return this.containsAll(Concurrent.newList(predicates));
    }

    /**
     * Returns all elements whose list-valued fields contain the given values, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return a stream of elements matching all containment predicates
     */
    default <S> @NotNull Stream<E> containsAll(@NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) {
        return this.containsAll(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns all elements whose list-valued field contains the given value, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the list-field extractor
     * @param value the value to check for containment
     * @param <S>      the element type within the list field
     * @return a stream of elements whose list field contains the value
     */
    default <S> @NotNull Stream<E> containsAll(@NotNull SearchFunction.Match match, @NotNull Function<E, List<S>> function, S value) {
        return this.containsAll(match, Concurrent.newList(Pair.of(function, value)));
    }

    /**
     * Returns all elements whose list-valued fields contain the given values, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return a stream of elements matching the containment predicates
     */
    default <S> @NotNull Stream<E> containsAll(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, List<S>>, S>... predicates) {
        return this.containsAll(match, Concurrent.newList(predicates));
    }

    /**
     * Returns all elements whose list-valued fields contain the given values, using the specified match mode.
     * This is the terminal overload that performs the actual containment check.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S>        the element type within the list field
     * @return a stream of elements matching the containment predicates
     */
    default <S> @NotNull Stream<E> containsAll(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) {
        return this.contains(
            match,
            (predicate, it, value) -> {
                try {
                    List<S> list = predicate.apply(it);
                    return Objects.nonNull(list) && list.contains(value);
                } catch (NullPointerException nullPointerException) {
                    return false;
                }
            },
            predicates
        );
    }

    // --- FIND ALL ---

    /**
     * Returns all elements whose extracted field value equals the given value, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return a stream of elements whose field equals the value
     */
    default <S> @NotNull SingleStream<E> findAll(@NotNull Function<E, S> function, S value) {
        return this.findAll(SearchFunction.Match.ALL, function, value);
    }

    /**
     * Returns all elements matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return a stream of elements matching all predicates
     */
    default <S> @NotNull SingleStream<E> findAll(@NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findAll(Concurrent.newList(predicates));
    }

    /**
     * Returns all elements matching the given field-extractor/value pairs, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return a stream of elements matching all predicates
     */
    default <S> @NotNull SingleStream<E> findAll(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
        return this.findAll(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns all elements whose extracted field value equals the given value, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param function the field extractor
     * @param value the value to compare against
     * @param <S>      the type of the compared value
     * @return a stream of elements whose field equals the value
     */
    default <S> @NotNull SingleStream<E> findAll(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) {
        return this.findAll(match, Concurrent.newList(Pair.of(function, value)));
    }

    /**
     * Returns all elements matching the given field-extractor/value pairs, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return a stream of elements matching the predicates
     */
    default <S> @NotNull SingleStream<E> findAll(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) {
        return this.findAll(match, Concurrent.newList(predicates));
    }

    /**
     * Returns all elements matching the given field-extractor/value pairs, using the specified match mode.
     * This is the terminal overload that performs the actual equality comparison via {@link Objects#equals}.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the field-extractor/value pairs to match
     * @param <S>        the type of the compared value
     * @return a stream of elements matching the predicates
     */
    default <S> @NotNull SingleStream<E> findAll(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) {
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
        );
    }

    // --- MATCH ALL ---

    /**
     * Returns all elements that satisfy the given predicates, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the predicates to test against each element
     * @return a stream of elements satisfying all predicates
     */
    default @NotNull SingleStream<E> matchAll(@NotNull Predicate<E>... predicates) {
        return this.matchAll(Concurrent.newList(predicates));
    }

    /**
     * Returns all elements that satisfy the given predicates, using {@link SearchFunction.Match#ALL ALL} mode.
     *
     * @param predicates the predicates to test against each element
     * @return a stream of elements satisfying all predicates
     */
    default @NotNull SingleStream<E> matchAll(@NotNull Iterable<Predicate<E>> predicates) {
        return this.matchAll(SearchFunction.Match.ALL, predicates);
    }

    /**
     * Returns all elements that satisfy the given predicates, using the specified match mode.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return a stream of elements satisfying the predicates
     */
    default @NotNull SingleStream<E> matchAll(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) {
        return this.matchAll(match, Concurrent.newList(predicates));
    }

    /**
     * Returns all elements that satisfy the given predicates, using the specified match mode.
     * This is the terminal overload that converts predicates to comparison pairs and delegates to {@link #compare}.
     *
     * @param match the match mode (ALL or ANY)
     * @param predicates the predicates to test against each element
     * @return a stream of elements satisfying the predicates
     */
    default @NotNull SingleStream<E> matchAll(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) {
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
        );
    }

}
