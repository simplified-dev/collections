package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The hash indexes one collection holds over the {@link Indexed} properties of its elements.
 *
 * <p>A cache is built over a snapshot of the elements in source order and never mutated afterwards,
 * so an indexed answer carries the same elements in the same order a scan over that snapshot would.
 * A collection drops its cache whenever it drops its iteration snapshot, and the next query builds
 * a fresh one.
 *
 * <p>Only the property a query actually names is built, and only when a field declares
 * {@link Indexed} for it. A class declaring three indexes that is ever asked about one pays for one.
 *
 * <p>Every refusal answers {@code null}, which the caller reads as "scan instead". Refusing is
 * always correct, so a property that cannot be indexed costs a hash probe more than it does today
 * and never a wrong answer.
 *
 * @param <E> the element type of the indexed collection
 */
public final class IndexCache<E> {

    private static final IndexCache<?> NONE = new IndexCache<>(new Object[0]);

    /**
     * Marks a property that was asked for and cannot be indexed, so the refusal is decided once
     * rather than on every query.
     */
    private static final Index<?> UNUSABLE = new Index<>(Map.of());

    private final @Nullable Object @NotNull [] elements;
    private final @Nullable Class<?> elementType;
    private final @NotNull IndexSchema schema;
    /**
     * The built indexes, keyed by the declaration they answer. Two maps rather than one keyed by a
     * declaration and a flag, so a lookup mints no key.
     */
    private final @NotNull ConcurrentHashMap<IndexSchema.Declaration, Index<E>> equality = new ConcurrentHashMap<>();
    private final @NotNull ConcurrentHashMap<IndexSchema.Declaration, Index<E>> containment = new ConcurrentHashMap<>();

    private IndexCache(@Nullable Object @NotNull [] elements) {
        this.elements = elements;
        this.elementType = typeOf(elements);
        this.schema = this.elementType == null ? IndexSchema.EMPTY : IndexSchema.of(this.elementType);
    }

    /**
     * The cache a collection that holds no index answers with, so every query falls through to a
     * scan.
     *
     * @param <E> the element type
     * @return the shared empty cache
     */
    @SuppressWarnings("unchecked")
    public static <E> @NotNull IndexCache<E> none() {
        return (IndexCache<E>) NONE;
    }

    /**
     * Builds a cache over one snapshot of a collection's elements.
     *
     * <p>The array is read in place rather than copied, so a caller must not mutate it afterwards -
     * which is already the contract of the iteration snapshot this is built from.
     *
     * @param snapshot the elements in source order
     * @param <E> the element type
     * @return a cache over those elements, holding no index until one is asked for
     */
    public static <E> @NotNull IndexCache<E> over(@Nullable Object @NotNull [] snapshot) {
        return snapshot.length == 0 ? none() : new IndexCache<>(snapshot);
    }

    /**
     * Whether this cache can answer anything at all, which it cannot when the elements declare no
     * {@link Indexed} field.
     *
     * @return {@code true} when no query can be answered from an index
     */
    public boolean isEmpty() {
        return this.elementType == null || this.schema.isEmpty();
    }

    /**
     * Answers the elements whose named properties carry the given values.
     *
     * <p>The three lists are parallel: entry {@code i} of each describes one equality predicate. A
     * query naming a composite index's fields in any order resolves to it, and the values are
     * rearranged into the declared probe order here.
     *
     * @param references the decoded property each predicate reads
     * @param extractors the extractor each predicate applies, used to build the index the first
     *        time a property is asked for
     * @param values the value each predicate compares against
     * @return the matching elements in source order, or {@code null} when no index covers the query
     * @throws IllegalStateException if an index declared unique finds two elements sharing a value
     */
    public @Nullable List<E> lookup(@NotNull List<PropertyReference> references, @NotNull List<SearchFunction<E, ?>> extractors, @NotNull List<Object> values) {
        if (this.isEmpty() || references.isEmpty() || references.size() != extractors.size() || references.size() != values.size())
            return null;

        if (references.size() == 1)
            return this.lookup(references.getFirst(), extractors.getFirst(), values.getFirst());

        List<PropertyReference> named = new ArrayList<>(references.size());

        for (PropertyReference reference : references) {
            PropertyReference against = reference.against(this.elementType);

            if (!against.isResolved())
                return null;

            named.add(against);
        }

        IndexSchema.Declaration declaration = this.schema.covering(named);

        if (declaration == null)
            return null;

        Index<E> index = usable(this.equality.computeIfAbsent(declaration, held -> this.build(held, ordered(held.components(), named, extractors))));

        if (index == null)
            return null;

        return index.matching(keyOf(declaration.components(), named, values));
    }

    /**
     * Answers the elements whose named property carries the given value.
     *
     * <p>This is the shape nearly every query takes, and it allocates nothing: the declaration is
     * found by the path the caller already holds, the built index is keyed by that declaration, and
     * the bucket was sealed when it was built.
     *
     * @param reference the decoded property the predicate reads
     * @param extractor the extractor the predicate applies, used to build the index the first time
     *        the property is asked for
     * @param value the value the property must carry
     * @return the matching elements in source order, or {@code null} when the property carries no
     *         index
     * @throws IllegalStateException if an index declared unique finds two elements sharing a value
     */
    public @Nullable List<E> lookup(@NotNull PropertyReference reference, @NotNull SearchFunction<E, ?> extractor, @Nullable Object value) {
        IndexSchema.Declaration declaration = this.declaring(reference);

        if (declaration == null)
            return null;

        Index<E> index = usable(this.equality.computeIfAbsent(declaration, held -> this.build(held, List.of(extractor))));
        return index == null ? null : index.matching(value);
    }

    /**
     * Answers the elements whose named list-valued property holds the given value.
     *
     * <p>This is a second index over the same field: an equality index files an element under the
     * list it carries, a containment index files it under every member of that list, and the two
     * answer different questions about one property.
     *
     * @param reference the decoded property the predicate reads
     * @param extractor the extractor the predicate applies
     * @param value the value the list must hold
     * @return the matching elements in source order, or {@code null} when the property carries no
     *         index
     */
    public @Nullable List<E> lookupContaining(@NotNull PropertyReference reference, @NotNull SearchFunction<E, ?> extractor, @Nullable Object value) {
        IndexSchema.Declaration declaration = this.declaring(reference);

        if (declaration == null)
            return null;

        Index<E> index = usable(this.containment.computeIfAbsent(declaration, held -> this.buildContaining(extractor)));
        return index == null ? null : index.matching(value);
    }

    /**
     * Finds what this collection declares about one property a query names.
     *
     * <p>Matched on the property path rather than on a {@link PropertyReference} restated against
     * the element type, because restating one allocates and this runs on every query.
     *
     * @param reference the decoded property
     * @return the declaration, or {@code null} when nothing covers it
     */
    private @Nullable IndexSchema.Declaration declaring(@NotNull PropertyReference reference) {
        if (this.isEmpty() || !reference.isResolved() || !reference.owner().isAssignableFrom(this.elementType))
            return null;

        return this.schema.coveringPath(reference.properties());
    }

    /**
     * Unwraps the refusal marker.
     *
     * @param index the held index
     * @return the index, or {@code null} when these elements cannot carry it
     */
    private static <E> @Nullable Index<E> usable(@NotNull Index<E> index) {
        return index == UNUSABLE ? null : index;
    }

    /**
     * Walks every element once, filing it under the value its extractors read.
     *
     * @return the built index, or the {@link #UNUSABLE} marker
     * @throws IllegalStateException if the declaration promises uniqueness the elements do not keep
     */
    @SuppressWarnings("unchecked")
    private @NotNull Index<E> build(@NotNull IndexSchema.Declaration declaration, @NotNull List<SearchFunction<E, ?>> extractors) {
        boolean unique = declaration.unique();

        // A unique index carries one key per element, so it is sized once rather than rehashed all
        // the way up. A shared index has no such bound and is left to grow, because sizing it for
        // one key per element would allocate a table the width of the collection to hold a handful
        // of distinct values.
        Map<Object, Object> buckets = unique ? HashMap.newHashMap(this.elements.length) : new HashMap<>();

        for (Object element : this.elements) {
            // A null element carries no property, and applying any accessor to it raises the
            // NullPointerException the scan already reads as a non-match.
            if (element == null)
                continue;

            if (!this.elementType.isInstance(element))
                return (Index<E>) UNUSABLE;

            Object key;

            try {
                key = read((E) element, extractors);
            } catch (NullPointerException absent) {
                // The scan treats a null on the way to the property as a non-match, so an element
                // that raises one belongs in no bucket at all.
                continue;
            }

            if (unique) {
                // A list of one rather than a growable list of one: an immutable singleton carries
                // its element in a field instead of an array, so it is one small object per element
                // where an ArrayList is two larger ones plus a wrapper.
                Object held = buckets.putIfAbsent(key, List.of(element));

                if (held != null)
                    throw new IllegalStateException(String.format(
                        "Index '%s' on '%s' is declared unique and two elements carry '%s'",
                        declaration.describe(),
                        this.elementType.getSimpleName(),
                        key
                    ));
            } else
                ((List<Object>) buckets.computeIfAbsent(key, held -> new ArrayList<>(4))).add(element);
        }

        return seal(buckets, unique);
    }

    /**
     * Walks every element once, filing it under every member of the list its extractor reads.
     *
     * <p>Uniqueness is not enforced here: a promise that no two elements carry one list says
     * nothing about how many carry one member of it, which is the question this index answers.
     *
     * @return the built index, or the {@link #UNUSABLE} marker
     */
    @SuppressWarnings("unchecked")
    private @NotNull Index<E> buildContaining(@NotNull SearchFunction<E, ?> extractor) {
        Map<Object, Object> buckets = new HashMap<>();

        for (Object element : this.elements) {
            if (element == null)
                continue;

            if (!this.elementType.isInstance(element))
                return (Index<E>) UNUSABLE;

            Object read;

            try {
                read = extractor.apply((E) element);
            } catch (NullPointerException absent) {
                continue;
            }

            // The scan reads a null list as holding nothing, so the element belongs in no bucket.
            if (!(read instanceof Iterable<?> members))
                continue;

            for (Object member : members) {
                List<Object> bucket = (List<Object>) buckets.computeIfAbsent(member, held -> new ArrayList<>(4));

                // A list holding one value twice still puts its element in the bucket once.
                if (bucket.isEmpty() || bucket.getLast() != element)
                    bucket.add(element);
            }
        }

        return seal(buckets, false);
    }

    /**
     * Seals every growable bucket, so a lookup can hand one out without copying or wrapping it.
     *
     * <p>A unique index is already sealed, each of its buckets being an immutable singleton built
     * in place. Sealing on the way in rather than on the way out is what keeps a lookup free of
     * allocation, which is the side of the trade a read-mostly collection is on.
     *
     * @param buckets the buckets to seal
     * @param unique whether the buckets were built as immutable singletons
     * @return the sealed index
     */
    @SuppressWarnings("unchecked")
    private static <E> @NotNull Index<E> seal(@NotNull Map<Object, Object> buckets, boolean unique) {
        if (!unique)
            buckets.replaceAll((key, bucket) -> Collections.unmodifiableList((List<E>) bucket));

        return new Index<>(buckets);
    }

    /**
     * Reads one element's key, a bare value for a single-property index and a value tuple for a
     * composite.
     *
     * @return the key the element files under
     */
    private static <E> @Nullable Object read(@NotNull E element, @NotNull List<SearchFunction<E, ?>> extractors) {
        if (extractors.size() == 1)
            return extractors.getFirst().apply(element);

        Object[] key = new Object[extractors.size()];

        for (int position = 0; position < key.length; position++)
            key[position] = extractors.get(position).apply(element);

        // Arrays.asList rather than List.of, because a component may legitimately be null.
        return Arrays.asList(key);
    }

    /**
     * Builds the probe key from the values a query supplied, in the index's declared order.
     *
     * @return the key to probe with
     */
    private static @Nullable Object keyOf(@NotNull List<PropertyReference> components, @NotNull List<PropertyReference> named, @NotNull List<Object> values) {
        if (components.size() == 1)
            return values.get(named.indexOf(components.getFirst()));

        Object[] key = new Object[components.size()];

        for (int position = 0; position < key.length; position++)
            key[position] = values.get(named.indexOf(components.get(position)));

        return Arrays.asList(key);
    }

    /**
     * Rearranges the extractors a query supplied into the index's declared order.
     *
     * @return the extractors, one per component of the tuple
     */
    private static <E> @NotNull List<SearchFunction<E, ?>> ordered(@NotNull List<PropertyReference> components, @NotNull List<PropertyReference> named, @NotNull List<SearchFunction<E, ?>> extractors) {
        List<SearchFunction<E, ?>> reordered = new ArrayList<>(components.size());
        components.forEach(component -> reordered.add(extractors.get(named.indexOf(component))));
        return reordered;
    }

    /**
     * Reads the class the schema is taken from, which is the class of the first element present.
     *
     * @return the element class, or {@code null} when nothing is present to read one from
     */
    private static @Nullable Class<?> typeOf(@Nullable Object @NotNull [] elements) {
        for (Object element : elements) {
            if (element != null)
                return element.getClass();
        }

        return null;
    }

    /**
     * One built index, sealed at construction and never mutated after it.
     *
     * @param buckets the elements under each value, each bucket in source order and unmodifiable
     */
    private record Index<E>(@NotNull Map<Object, Object> buckets) {

        /**
         * Answers the elements filed under one key.
         *
         * @param key the value to probe with
         * @return the elements in source order, empty when nothing carries the value
         */
        @SuppressWarnings("unchecked")
        private @NotNull List<E> matching(@Nullable Object key) {
            Object held = this.buckets().get(key);
            return held == null ? List.of() : (List<E>) held;
        }

    }

}
