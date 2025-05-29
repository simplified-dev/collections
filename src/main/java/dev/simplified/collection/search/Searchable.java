package dev.sbs.api.collection.search;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.data.exception.DataException;
import dev.sbs.api.stream.pair.Pair;
import dev.sbs.api.stream.triple.TriPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@FunctionalInterface
public interface Searchable<E> {

    @NotNull Stream<E> stream() throws DataException;

    default <S> Stream<E> compare(SearchFunction.Match match, TriPredicate<Function<E, S>, E, S> compare, Iterable<Pair<Function<E, S>, S>> predicates) throws DataException {
        Stream<E> itemsCopy = this.stream();

        if (match == SearchFunction.Match.ANY) {
            itemsCopy = itemsCopy.filter(it -> {
                boolean matches = false;

                for (Pair<Function<E, S>, S> predicate : predicates)
                    matches |= compare.test(predicate.getLeft(), it, predicate.getValue());

                return matches;
            });
        } else if (match == SearchFunction.Match.ALL) {
            for (Pair<Function<E, S>, S> predicate : predicates)
                itemsCopy = itemsCopy.filter(it -> compare.test(predicate.getLeft(), it, predicate.getRight()));
        } else
            throw new DataException("Invalid match type '%s'.", match);

        return itemsCopy;
    }

    default <S> Stream<E> contains(SearchFunction.Match match, TriPredicate<Function<E, List<S>>, E, S> compare, Iterable<Pair<Function<E, List<S>>, S>> predicates) throws DataException {
        Stream<E> itemsCopy = this.stream();

        if (match == SearchFunction.Match.ANY) {
            itemsCopy = itemsCopy.filter(it -> {
                boolean matches = false;

                for (Pair<Function<E, List<S>>, S> predicate : predicates)
                    matches |= compare.test(predicate.getLeft(), it, predicate.getValue());

                return matches;
            });
        } else if (match == SearchFunction.Match.ALL) {
            for (Pair<Function<E, List<S>>, S> predicate : predicates)
                itemsCopy = itemsCopy.filter(it -> compare.test(predicate.getLeft(), it, predicate.getRight()));
        } else
            throw new DataException("Invalid match type '%s'.", match);

        return itemsCopy;
    }

    // --- CONTAINS ALL ---
    default <S> @NotNull Stream<E> containsAll(@NotNull Function<E, List<S>> function, S value) throws DataException {
        return this.containsAll(SearchFunction.Match.ALL, function, value);
    }

    default <S> @NotNull Stream<E> containsAll(@NotNull Pair<Function<E, List<S>>, S>... predicates) throws DataException {
        return this.containsAll(Concurrent.newList(predicates));
    }

    default <S> @NotNull Stream<E> containsAll(@NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) throws DataException {
        return this.containsAll(SearchFunction.Match.ALL, predicates);
    }

    default <S> @NotNull Stream<E> containsAll(@NotNull SearchFunction.Match match, @NotNull Function<E, List<S>> function, S value) throws DataException {
        return this.containsAll(match, Concurrent.newList(Pair.of(function, value)));
    }

    default <S> @NotNull Stream<E> containsAll(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, List<S>>, S>... predicates) throws DataException {
        return this.containsAll(match, Concurrent.newList(predicates));
    }

    default <S> @NotNull Stream<E> containsAll(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, List<S>>, S>> predicates) throws DataException {
        return this.contains(
            match,
            (predicate, it, value) -> {
                List<S> list = predicate.apply(it);
                return Objects.nonNull(list) && list.contains(value);
            },
            predicates
        );
    }

    // --- FIND ALL ---
    default <S> @NotNull Stream<E> findAll(@NotNull Function<E, S> function, S value) throws DataException {
        return this.findAll(SearchFunction.Match.ALL, function, value);
    }

    default <S> @NotNull Stream<E> findAll(@NotNull Pair<Function<E, S>, S>... predicates) throws DataException {
        return this.findAll(Concurrent.newList(predicates));
    }

    default <S> @NotNull Stream<E> findAll(@NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws DataException {
        return this.findAll(SearchFunction.Match.ALL, predicates);
    }

    default <S> @NotNull Stream<E> findAll(@NotNull SearchFunction.Match match, @NotNull Function<E, S> function, S value) throws DataException {
        return this.findAll(match, Concurrent.newList(Pair.of(function, value)));
    }

    default <S> @NotNull Stream<E> findAll(@NotNull SearchFunction.Match match, @NotNull Pair<Function<E, S>, S>... predicates) throws DataException {
        return this.findAll(match, Concurrent.newList(predicates));
    }

    default <S> @NotNull Stream<E> findAll(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<Function<E, S>, S>> predicates) throws DataException {
        return this.compare(
            match,
            (predicate, it, value) -> Objects.equals(predicate.apply(it), value),
            predicates
        );
    }

    // --- MATCH ALL ---
    default @NotNull Stream<E> matchAll(@NotNull Predicate<E>... predicates) throws DataException {
        return this.matchAll(Concurrent.newList(predicates));
    }

    default @NotNull Stream<E> matchAll(@NotNull Iterable<Predicate<E>> predicates) throws DataException {
        return this.matchAll(SearchFunction.Match.ALL, predicates);
    }

    default @NotNull Stream<E> matchAll(@NotNull SearchFunction.Match match, @NotNull Predicate<E>... predicates) throws DataException {
        return this.matchAll(match, Concurrent.newList(predicates));
    }

    default @NotNull Stream<E> matchAll(@NotNull SearchFunction.Match match, @NotNull Iterable<Predicate<E>> predicates) throws DataException {
        return this.compare(
            match,
            (predicate, it, value) -> Objects.nonNull(it) && predicate.apply(it),
            StreamSupport.stream(predicates.spliterator(), false)
                .map(predicate -> Pair.<Function<E, Boolean>, Boolean>of(predicate::test, true))
                .collect(Concurrent.toList())
        );
    }

}
