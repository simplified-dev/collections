package dev.sbs.api.collection.search;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.persistence.exception.SessionException;
import dev.sbs.api.stream.pair.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@FunctionalInterface
public interface Sortable<E> extends Searchable<E> {

    // --- CONTAINS FIRST ---
    default <S> @NotNull Optional<E> containsFirst(@NotNull Function<E, List<S>> function, S value) throws SessionException {
        return this.containsFirst(SearchFunction.Match.ALL, function, value);
    }

    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction.Match match, @NotNull Function<E, List<S>> function, S value) throws SessionException {
        return this.containsFirst(match, Pair.of(function, value));
    }

    default <S> @NotNull Optional<E> containsFirst(@NotNull Pair<Function<E, List<S>>, S>... predicates) throws SessionException {
        return this.containsFirst(SearchFunction.Match.ALL, predicates);
    }

    default <S> @NotNull Optional<E> containsFirst(@NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) throws SessionException {
        return this.containsFirst(SearchFunction.Match.ALL, predicates);
    }

    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, List<S>>, S>... predicates) throws SessionException {
        return this.containsFirst(match, Concurrent.newList(predicates));
    }

    default <S> @NotNull Optional<E> containsFirst(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) throws SessionException {
        return this.containsAll(match, predicates).findFirst();
    }

    default <S> E containsFirstOrNull(@NotNull SearchFunction<E, List<S>> function, S value) throws SessionException {
        return this.containsFirst(function, value).orElse(null);
    }

    default <S> E containsFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Function<E, List<S>> function, S value) throws SessionException {
        return this.containsFirstOrNull(match, Pair.of(function, value));
    }

    default <S> E containsFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, List<S>>, S>... predicates) throws SessionException {
        return this.containsFirstOrNull(match, Concurrent.newList(predicates));
    }

    default <S> E containsFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) throws SessionException {
        return this.containsFirst(match, predicates).orElse(null);
    }

    default <S> E containsFirstOrNull(@NotNull Pair<Function<E, List<S>>, S>... predicates) throws SessionException {
        return this.containsFirstOrNull(Concurrent.newList(predicates));
    }

    default <S> E containsFirstOrNull(@NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) throws SessionException {
        return this.containsFirst(predicates).orElse(null);
    }

    // --- FIND FIRST ---
    default <S> @NotNull Optional<E> findFirst(@NotNull Function<E, S> function, S value) throws SessionException {
        return this.findFirst(SearchFunction.Match.ALL, function, value);
    }

    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) throws SessionException {
        return this.findFirst(match, Pair.of(function, value));
    }

    default <S> @NotNull Optional<E> findFirst(@NotNull Pair<Function<E, S>, S>... predicates) throws SessionException {
        return this.findFirst(SearchFunction.Match.ALL, predicates);
    }

    default <S> @NotNull Optional<E> findFirst(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws SessionException {
        return this.findFirst(SearchFunction.Match.ALL, predicates);
    }

    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) throws SessionException {
        return this.findFirst(match, Concurrent.newList(predicates));
    }

    default <S> @NotNull Optional<E> findFirst(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws SessionException {
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

    default <S> E findFirstOrNull(@NotNull SearchFunction<E, S> function, S value) throws SessionException {
        return this.findFirst(function, value).orElse(null);
    }

    default <S> E findFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) throws SessionException {
        return this.findFirstOrNull(match, Pair.of(function, value));
    }

    default <S> E findFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) throws SessionException {
        return this.findFirstOrNull(match, Concurrent.newList(predicates));
    }

    default <S> E findFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws SessionException {
        return this.findFirst(match, predicates).orElse(null);
    }

    default <S> E findFirstOrNull(@NotNull Pair<Function<E, S>, S>... predicates) throws SessionException {
        return this.findFirstOrNull(Concurrent.newList(predicates));
    }

    default <S> E findFirstOrNull(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws SessionException {
        return this.findFirst(predicates).orElse(null);
    }

    // --- FIND LAST ---
    default <S> @NotNull Optional<E> findLast(@NotNull Function<E, S> function, S value) throws SessionException {
        return this.findLast(SearchFunction.Match.ALL, function, value);
    }

    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) throws SessionException {
        return this.findLast(match, Pair.of(function, value));
    }

    default <S> @NotNull Optional<E> findLast(@NotNull Pair<Function<E, S>, S>... predicates) throws SessionException {
        return this.findLast(SearchFunction.Match.ALL, predicates);
    }

    default <S> @NotNull Optional<E> findLast(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws SessionException {
        return this.findLast(SearchFunction.Match.ALL, predicates);
    }

    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) throws SessionException {
        return this.findLast(match, Concurrent.newList(predicates));
    }

    default <S> @NotNull Optional<E> findLast(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws SessionException {
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

    // --- FIND LAST ---
    default <S> E findLastOrNull(@NotNull SearchFunction<E, S> function, S value) throws SessionException {
        return this.findLast(function, value).orElse(null);
    }

    default <S> E findLastOrNull(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) throws SessionException {
        return this.findLastOrNull(match, Pair.of(function, value));
    }

    default <S> E findLastOrNull(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) throws SessionException {
        return this.findLastOrNull(match, Concurrent.newList(predicates));
    }

    default <S> E findLastOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws SessionException {
        return this.findLast(match, predicates).orElse(null);
    }

    default <S> E findLastOrNull(@NotNull Pair<Function<E, S>, S>... predicates) throws SessionException {
        return this.findLastOrNull(Concurrent.newList(predicates));
    }

    default <S> E findLastOrNull(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws SessionException {
        return this.findLast(predicates).orElse(null);
    }

    // --- MATCH FIRST ---
    default @NotNull Optional<E> matchFirst(@NotNull Predicate<E>... predicates) throws SessionException {
        return this.matchFirst(Concurrent.newList(predicates));
    }

    default @NotNull Optional<E> matchFirst(@NotNull Iterable<Predicate<E>> predicates) throws SessionException {
        return this.matchFirst(SearchFunction.Match.ALL, predicates);
    }

    default @NotNull Optional<E> matchFirst(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) throws SessionException {
        return this.matchFirst(match, Concurrent.newList(predicates));
    }

    default @NotNull Optional<E> matchFirst(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) throws SessionException {
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

    default E matchFirstOrNull(@NotNull Predicate<E>... predicates) throws SessionException {
        return this.matchFirstOrNull(Concurrent.newList(predicates));
    }

    default E matchFirstOrNull(@NotNull Iterable<Predicate<E>> predicates) throws SessionException {
        return this.matchFirstOrNull(SearchFunction.Match.ALL, predicates);
    }

    default E matchFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) throws SessionException {
        return this.matchFirstOrNull(match, Concurrent.newList(predicates));
    }

    default E matchFirstOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) throws SessionException {
        return this.matchFirst(match, predicates).orElse(null);
    }

    // --- MATCH LAST ---
    default @NotNull Optional<E> matchLast(@NotNull Predicate<E>... predicates) throws SessionException {
        return this.matchLast(Concurrent.newList(predicates));
    }

    default @NotNull Optional<E> matchLast(@NotNull Iterable<Predicate<E>> predicates) throws SessionException {
        return this.matchLast(SearchFunction.Match.ALL, predicates);
    }

    default @NotNull Optional<E> matchLast(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) throws SessionException {
        return this.matchLast(match, Concurrent.newList(predicates));
    }

    default @NotNull Optional<E> matchLast(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) throws SessionException {
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

    default E matchLastOrNull(@NotNull Predicate<E>... predicates) throws SessionException {
        return this.matchLastOrNull(Concurrent.newList(predicates));
    }

    default E matchLastOrNull(@NotNull Iterable<Predicate<E>> predicates) throws SessionException {
        return this.matchLastOrNull(SearchFunction.Match.ALL, predicates);
    }

    default E matchLastOrNull(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) throws SessionException {
        return this.matchLastOrNull(match, Concurrent.newList(predicates));
    }

    default E matchLastOrNull(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) throws SessionException {
        return this.matchLast(match, predicates).orElse(null);
    }

}
