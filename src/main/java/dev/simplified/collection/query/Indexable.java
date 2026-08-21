package dev.simplified.collection.query;

import dev.simplified.collection.tuple.pair.Pair;
import dev.simplified.collection.tuple.single.SingleStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link Searchable} that can answer an equality query from a hash index instead of a scan.
 *
 * <p>Only the two terminals whose meaning is equality are overridden - {@link #findAll} and
 * {@link #containsAll} - so every finder above them is indexed without any of them being rewritten.
 * {@link #compare} and {@link #contains} keep scanning, because a caller-supplied comparison is not
 * a question an index over values can answer.
 *
 * <p>Indexing is a pure optimisation: an implementation that holds no index answers
 * {@link IndexCache#none()} from {@link #indexes()} and every query falls through to the inherited
 * scan, so this interface adds no abstract member and a {@link Searchable} can still be a lambda.
 *
 * @param <E> the element type of the indexable collection
 */
@FunctionalInterface
public interface Indexable<E> extends Searchable<E> {

    /**
     * The indexes this collection holds over its elements.
     *
     * @return the cache, or {@link IndexCache#none()} when nothing is indexed
     */
    default @NotNull IndexCache<E> indexes() {
        return IndexCache.none();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Answers from an index when one covers the query, and scans otherwise. The indexed answer
     * holds the same elements in the same order the scan would.
     */
    @Override
    default <S> @NotNull SingleStream<E> findAll(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<SearchFunction<E, S>, S>> predicates) {
        List<Pair<SearchFunction<E, S>, S>> listed = listed(predicates);
        List<E> answered = this.indexedFindAll(match, listed);

        return answered == null
            ? Searchable.super.findAll(match, listed)
            : SingleStream.of(answered.stream());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Answers from a containment index when one covers the query, and scans otherwise.
     */
    @Override
    default <S> @NotNull Stream<E> containsAll(@NotNull SearchFunction.Match match, @NotNull Iterable<Pair<SearchFunction<E, List<S>>, S>> predicates) {
        List<Pair<SearchFunction<E, List<S>>, S>> listed = listed(predicates);
        List<E> answered = this.indexedContainsAll(match, listed);

        return answered == null
            ? Searchable.super.containsAll(match, listed)
            : answered.stream();
    }

    /**
     * Answers an equality query from the indexes, when they cover it.
     *
     * @param match the match mode
     * @param predicates the field-extractor/value pairs to match
     * @param <S> the type of the compared value
     * @return the matching elements in source order, or {@code null} when the query must be scanned
     */
    private <S> @Nullable List<E> indexedFindAll(@NotNull SearchFunction.Match match, @NotNull List<Pair<SearchFunction<E, S>, S>> predicates) {
        IndexCache<E> indexes = this.indexes();

        if (!indexable(indexes, match, predicates))
            return null;

        List<PropertyReference> references = new ArrayList<>(predicates.size());
        List<SearchFunction<E, ?>> extractors = new ArrayList<>(predicates.size());
        List<Object> values = new ArrayList<>(predicates.size());

        for (Pair<SearchFunction<E, S>, S> predicate : predicates) {
            references.add(PropertyReference.of(predicate.left()));
            extractors.add(predicate.left());
            values.add(predicate.right());
        }

        // A composite index covering every predicate answers the whole query in one probe.
        List<E> exact = indexes.lookup(references, extractors, values);

        if (exact != null)
            return exact;

        int narrowest = -1;
        List<E> bucket = null;

        for (int at = 0; at < predicates.size(); at++) {
            List<E> candidate = indexes.lookup(
                List.of(references.get(at)),
                List.of(extractors.get(at)),
                Collections.singletonList(values.get(at))
            );

            if (candidate != null && (bucket == null || candidate.size() < bucket.size())) {
                bucket = candidate;
                narrowest = at;
            }
        }

        if (bucket == null)
            return null;

        return filter(bucket, predicates, narrowest, Indexable::equal);
    }

    /**
     * Answers a containment query from the indexes, when they cover it.
     *
     * @param match the match mode
     * @param predicates the list-field-extractor/value pairs to check
     * @param <S> the element type within the list field
     * @return the matching elements in source order, or {@code null} when the query must be scanned
     */
    private <S> @Nullable List<E> indexedContainsAll(@NotNull SearchFunction.Match match, @NotNull List<Pair<SearchFunction<E, List<S>>, S>> predicates) {
        IndexCache<E> indexes = this.indexes();

        if (!indexable(indexes, match, predicates))
            return null;

        int narrowest = -1;
        List<E> bucket = null;

        for (int at = 0; at < predicates.size(); at++) {
            Pair<SearchFunction<E, List<S>>, S> predicate = predicates.get(at);
            List<E> candidate = indexes.lookupContaining(PropertyReference.of(predicate.left()), predicate.left(), predicate.right());

            if (candidate != null && (bucket == null || candidate.size() < bucket.size())) {
                bucket = candidate;
                narrowest = at;
            }
        }

        if (bucket == null)
            return null;

        return filter(bucket, predicates, narrowest, Indexable::holds);
    }

    /**
     * Whether a query is the shape an index can answer at all.
     *
     * <p>{@link SearchFunction.Match#ANY ANY} over several predicates is a union rather than an
     * intersection, so narrowing on one of them would drop the elements the others match. With a
     * single predicate the two modes ask the same question.
     *
     * @return {@code true} when the indexes are worth probing
     */
    private static boolean indexable(@NotNull IndexCache<?> indexes, @NotNull SearchFunction.Match match, @NotNull List<?> predicates) {
        if (indexes.isEmpty() || predicates.isEmpty())
            return false;

        return match != SearchFunction.Match.ANY || predicates.size() == 1;
    }

    /**
     * Applies every predicate but the one already answered by the index, keeping source order.
     *
     * @return the elements the whole query matches
     */
    private static <E, P> @NotNull List<E> filter(@NotNull List<E> bucket, @NotNull List<P> predicates, int answered, @NotNull Residual<E, P> residual) {
        if (predicates.size() == 1)
            return bucket;

        List<E> matched = new ArrayList<>(bucket.size());

        for (E element : bucket) {
            boolean matches = true;

            for (int at = 0; matches && at < predicates.size(); at++) {
                if (at != answered)
                    matches = residual.test(predicates.get(at), element);
            }

            if (matches)
                matched.add(element);
        }

        return matched;
    }

    /**
     * Tests one equality predicate the way the scan does, reading a raised
     * {@link NullPointerException} as a non-match.
     *
     * @return {@code true} when the element's value equals the predicate's
     */
    private static <E, S> boolean equal(@NotNull Pair<SearchFunction<E, S>, S> predicate, E element) {
        try {
            return Objects.equals(predicate.left().apply(element), predicate.right());
        } catch (NullPointerException absent) {
            return false;
        }
    }

    /**
     * Tests one containment predicate the way the scan does.
     *
     * @return {@code true} when the element's list holds the predicate's value
     */
    private static <E, S> boolean holds(@NotNull Pair<SearchFunction<E, List<S>>, S> predicate, E element) {
        try {
            List<S> held = predicate.left().apply(element);
            return Objects.nonNull(held) && held.contains(predicate.right());
        } catch (NullPointerException absent) {
            return false;
        }
    }

    /**
     * Materialises the predicates once, so the index pass and any fallback scan read one list
     * rather than draining an iterable twice.
     *
     * @return the predicates as a list
     */
    private static <P> @NotNull List<P> listed(@NotNull Iterable<P> predicates) {
        if (predicates instanceof List<P> list)
            return list;

        List<P> listed = new ArrayList<>();
        predicates.forEach(listed::add);
        return listed;
    }

    /**
     * Tests one predicate that the index did not answer.
     *
     * @param <E> the element type
     * @param <P> the predicate type
     */
    @FunctionalInterface
    interface Residual<E, P> {

        /**
         * Tests one predicate against one element.
         *
         * @param predicate the predicate to apply
         * @param element the element to test
         * @return {@code true} when the element matches
         */
        boolean test(@NotNull P predicate, E element);

    }

}
