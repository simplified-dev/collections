package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    private static final IndexCache<?> NONE = new IndexCache<>(new Object[0], Object.class, IndexSchema.EMPTY);

    /**
     * Marks a property that was asked for and cannot be indexed over these elements, so the refusal
     * is decided once per snapshot rather than on every query.
     */
    private static final Index<?> UNUSABLE = new Index<>(0);

    private final @Nullable Object @NotNull [] elements;

    /**
     * The class the schema was read from, which is the class of the first element present. The
     * shared empty cache stands one in that declares nothing, so a cache that can answer anything
     * always names a real type.
     */
    private final @NotNull Class<?> elementType;
    private final @NotNull IndexSchema schema;
    /**
     * The built indexes, keyed by the declaration they answer. Two maps rather than one keyed by a
     * declaration and a flag, so a lookup mints no key.
     */
    private final @NotNull ConcurrentHashMap<IndexSchema.Declaration, Index<E>> equality = new ConcurrentHashMap<>();
    private final @NotNull ConcurrentHashMap<IndexSchema.Declaration, Index<E>> containment = new ConcurrentHashMap<>();

    private IndexCache(@Nullable Object @NotNull [] elements, @NotNull Class<?> elementType, @NotNull IndexSchema schema) {
        this.elements = elements;
        this.elementType = elementType;
        this.schema = schema;
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
     * <p>Elements declaring no {@link Indexed} field answer the shared empty cache, so a collection
     * of them carries nothing at all rather than a fresh cache per write that no query could ever
     * be answered from.
     *
     * @param snapshot the elements in source order
     * @param <E> the element type
     * @return a cache over those elements, holding no index until one is asked for
     */
    public static <E> @NotNull IndexCache<E> over(@Nullable Object @NotNull [] snapshot) {
        Class<?> elementType = typeOf(snapshot);

        if (elementType == null)
            return none();

        IndexSchema schema = IndexSchema.of(elementType);
        return schema.isEmpty() ? none() : new IndexCache<>(snapshot, elementType, schema);
    }

    /**
     * Whether this cache can answer anything at all, which it cannot when the elements declare no
     * {@link Indexed} field.
     *
     * @return {@code true} when no query can be answered from an index
     */
    public boolean isEmpty() {
        return this.schema.isEmpty();
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

        Index<E> built = this.equality.get(declaration);

        if (built == null)
            built = this.equality.computeIfAbsent(declaration, absent -> this.build(absent, ordered(absent.components(), named, extractors)));

        Index<E> index = usable(built);

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

        // Read before the build is offered, because a mapping function that closes over anything
        // is minted at the call site whether or not it runs, and this runs on every query where
        // the build runs once.
        Index<E> built = this.equality.get(declaration);

        if (built == null)
            built = this.equality.computeIfAbsent(declaration, absent -> this.build(absent, List.of(extractor)));

        Index<E> index = usable(built);
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

        Index<E> built = this.containment.get(declaration);

        if (built == null)
            built = this.containment.computeIfAbsent(declaration, absent -> this.buildContaining(extractor));

        Index<E> index = usable(built);
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
        // An owner is present exactly when the reference is resolved, so naming it is also that test.
        Class<?> owner = reference.owner();

        if (this.isEmpty() || owner == null || !owner.isAssignableFrom(this.elementType))
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
     * <p>An element that is no instance of the class the schema was read from, or whose extractor
     * raises anything but a {@link NullPointerException}, refuses the whole index. The scan that
     * takes the query raises that exception exactly when it reaches the element, so a finder
     * stopping at an earlier match answers where a build would throw. A value whose hash cannot be
     * taken refuses the same way, because the scan compares with {@code equals} and never asks for
     * one.
     *
     * @return the built index, or the {@link #UNUSABLE} marker
     * @throws IllegalStateException if the declaration promises uniqueness the elements do not keep
     */
    @SuppressWarnings("unchecked")
    private @NotNull Index<E> build(@NotNull IndexSchema.Declaration declaration, @NotNull List<SearchFunction<E, ?>> extractors) {
        boolean unique = declaration.unique();

        // A unique index carries one key per element, so its table is sized once rather than
        // rehashed all the way up. A shared index has no such bound and starts small, because
        // sizing it for one key per element would allocate a table the width of the collection to
        // hold a handful of distinct values.
        Index<E> index = new Index<>(unique ? this.elements.length : 0);

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
            } catch (RuntimeException unreadable) {
                // Raising it here would throw where a first-match scan answers.
                return (Index<E>) UNUSABLE;
            }

            boolean fresh;

            try {
                fresh = index.file(key, element);
            } catch (RuntimeException unhashable) {
                // A throw part way through filing can leave the table half rehashed, so even a
                // NullPointerException refuses here rather than reading as a non-match.
                return (Index<E>) UNUSABLE;
            }

            if (!fresh && unique)
                throw new IllegalStateException(String.format(
                    "Index '%s' on '%s' is declared unique and two elements carry '%s'",
                    declaration.describe(),
                    this.elementType.getSimpleName(),
                    key
                ));
        }

        index.seal();
        return index;
    }

    /**
     * Walks every element once, filing it under every member of the list its extractor reads.
     *
     * <p>Uniqueness is not enforced here: a promise that no two elements carry one list says
     * nothing about how many carry one member of it, which is the question this index answers.
     *
     * <p>An element that is no instance of the class the schema was read from, or whose extractor
     * raises anything but a {@link NullPointerException}, refuses the whole index, and so does a
     * list that cannot be walked. The scan that takes the query raises that exception exactly when
     * it reaches the element, so a finder stopping at an earlier match answers where a build would
     * throw. A member whose hash cannot be taken refuses the same way, because the scan asks the
     * list whether it holds a value and never hashes a member.
     *
     * @return the built index, or the {@link #UNUSABLE} marker
     */
    @SuppressWarnings("unchecked")
    private @NotNull Index<E> buildContaining(@NotNull SearchFunction<E, ?> extractor) {
        Index<E> index = new Index<>(0);

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
            } catch (RuntimeException unreadable) {
                // Raising it here would throw where a first-match scan answers.
                return (Index<E>) UNUSABLE;
            }

            // The scan reads a null list as holding nothing, so the element belongs in no bucket.
            if (!(read instanceof Iterable<?> members))
                continue;

            try {
                for (Object member : members)
                    index.fileOnce(member, element);
            } catch (RuntimeException unwalkable) {
                // A list that cannot be walked is the scan's to report, when it reaches it. Filing
                // may have taken some members already, so nothing raised here reads as a non-match.
                return (Index<E>) UNUSABLE;
            }
        }

        index.seal();
        return index;
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
     * One built index, holding the elements filed under each value of an indexed property.
     *
     * <p>The buckets live in one flat, open-addressed table - a key at every even slot and whatever
     * is filed under it at the odd slot after it - probed linearly from the slot a mixed hash names.
     * A build walks every element of the collection, so the per-entry node a chained map would mint
     * for each of them is the largest cost of building at all, and a flat table has none.
     *
     * <p>An index is filled once and sealed, and neither the table nor a bucket is touched
     * afterwards.
     *
     * @param <E> the element type of the indexed collection
     */
    private static final class Index<E> {

        /**
         * Stands in for a null key, so an empty slot stays distinguishable from one holding the key
         * that a null property value files under.
         */
        private static final Object NULL_KEY = new Object();

        /**
         * How full the table is allowed to get. Linear probing degrades steeply past three quarters,
         * and a table twice the width of what it holds is the price of carrying no nodes.
         */
        private static final float LOAD = 0.75F;

        /**
         * The widest table that can be addressed, the array holding two slots per entry.
         */
        private static final int LIMIT = 1 << 29;

        private @Nullable Object @NotNull [] table;
        private int mask;
        private int filled;
        private int ceiling;

        /**
         * Builds an empty index whose table is wide enough for the given number of keys.
         *
         * @param keys how many keys the table is expected to hold
         */
        private Index(int keys) {
            int capacity = 4;

            while (capacity < LIMIT && capacity * LOAD < keys)
                capacity <<= 1;

            this.reset(capacity);
        }

        /**
         * Files one element under a key, joining whatever is filed there already.
         *
         * @param key the value the element carries
         * @param element the element to file
         * @return {@code true} when nothing was filed under that key yet
         */
        private boolean file(@Nullable Object key, @NotNull Object element) {
            Object probe = probeOf(key);
            int at = this.slotFor(probe);
            Object filed = this.table[at + 1];

            if (filed == null) {
                this.take(at, probe, element);
                return true;
            }

            this.join(at, filed, element);
            return false;
        }

        /**
         * Files one element under a key unless it is the element filed there most recently, which is
         * what a list holding one value twice reaches.
         *
         * @param key the value the element carries
         * @param element the element to file
         */
        private void fileOnce(@Nullable Object key, @NotNull Object element) {
            Object probe = probeOf(key);
            int at = this.slotFor(probe);
            Object filed = this.table[at + 1];

            if (filed == null) {
                this.take(at, probe, element);
                return;
            }

            if (last(filed) != element)
                this.join(at, filed, element);
        }

        /**
         * Seals every bucket, so a lookup can hand one out without copying or wrapping it.
         *
         * <p>Sealing on the way in rather than on the way out is what keeps a lookup free of
         * allocation, which is the side of the trade a read-mostly collection is on. A key only one
         * element carries - the shape of a reference table, and of every unique index - never grows
         * a bucket at all, and seals as an immutable singleton straight off the element.
         */
        private void seal() {
            for (int at = 1; at < this.table.length; at += 2) {
                Object filed = this.table[at];

                if (filed == null)
                    continue;

                this.table[at] = filed instanceof Bucket<?> bucket
                    ? Collections.unmodifiableList(bucket)
                    : new SoleBucket<>(filed);
            }
        }

        /**
         * Answers the elements filed under one key.
         *
         * @param key the value to probe with
         * @return the elements in source order, empty when nothing carries the value
         */
        @SuppressWarnings("unchecked")
        private @NotNull List<E> matching(@Nullable Object key) {
            Object[] table = this.table;
            int mask = this.mask;
            Object probe = probeOf(key);
            int at = mix(probe.hashCode()) & mask;

            while (true) {
                Object held = table[at];

                if (held == null)
                    return List.of();

                if (held == probe || probe.equals(held))
                    return (List<E>) table[at + 1];

                at = (at + 2) & mask;
            }
        }

        /**
         * Finds the slot a key belongs at, which is the one already holding it or the first free one
         * after where its hash lands.
         *
         * @param probe the key, or the stand-in a null one is held under
         * @return the index of the key slot, the bucket living at the slot after it
         */
        private int slotFor(@NotNull Object probe) {
            Object[] table = this.table;
            int mask = this.mask;
            int at = mix(probe.hashCode()) & mask;

            while (true) {
                Object held = table[at];

                if (held == null || held == probe || probe.equals(held))
                    return at;

                at = (at + 2) & mask;
            }
        }

        /**
         * Claims a free slot for a key nothing is filed under yet, widening the table when it fills.
         *
         * <p>Widening rehashes, so the slot means nothing afterwards and no caller may keep it.
         *
         * @param at the free slot
         * @param probe the key to hold there
         * @param element the first element to file under it
         */
        private void take(int at, @NotNull Object probe, @NotNull Object element) {
            this.table[at] = probe;
            this.table[at + 1] = element;

            if (++this.filled > this.ceiling)
                this.widen();
        }

        /**
         * Adds one more element to a key something is already filed under.
         *
         * @param at the slot holding the key
         * @param filed what is filed there, a bare element until a second one joins it
         * @param element the element to add
         */
        @SuppressWarnings("unchecked")
        private void join(int at, @NotNull Object filed, @NotNull Object element) {
            if (filed instanceof Bucket<?> bucket)
                ((Bucket<Object>) bucket).add(element);
            else
                this.table[at + 1] = new Bucket<>(filed, element);
        }

        /**
         * Doubles the table and re-files everything into it.
         */
        private void widen() {
            Object[] narrow = this.table;
            this.reset(Math.min(LIMIT, (this.mask + 2)));

            for (int at = 0; at < narrow.length; at += 2) {
                Object probe = narrow[at];

                if (probe == null)
                    continue;

                int slot = this.slotFor(probe);
                this.table[slot] = probe;
                this.table[slot + 1] = narrow[at + 1];
            }
        }

        /**
         * Lays out an empty table wide enough for the given number of keys.
         *
         * @param capacity the number of keys, a power of two
         */
        private void reset(int capacity) {
            this.table = new Object[capacity << 1];
            // Every key sits at an even slot, so the low bit is cleared out of the mask and a probe
            // steps two at a time.
            this.mask = (this.table.length - 1) & ~1;
            this.ceiling = (int) (capacity * LOAD);
        }

        /**
         * Reads the element a bucket most recently took.
         *
         * @param filed what is filed under a key, a bare element until a second one joins it
         * @return the element filed most recently
         */
        private static @NotNull Object last(@NotNull Object filed) {
            return filed instanceof Bucket<?> bucket ? bucket.getLast() : filed;
        }

        /**
         * Names the key a value is held under.
         *
         * @param key the value, which may be null
         * @return the key, or the stand-in a null one is held under
         */
        private static @NotNull Object probeOf(@Nullable Object key) {
            return key == null ? NULL_KEY : key;
        }

        /**
         * Spreads a hash across the whole width of the table, because linear probing turns a hash
         * that clusters in the low bits into a run of occupied slots.
         *
         * @param hash the key's own hash
         * @return the spread hash
         */
        private static int mix(int hash) {
            hash *= 0x9E3779B9;
            return hash ^ (hash >>> 15);
        }

        /**
         * The growable bucket a key more than one element carries is filed in.
         *
         * <p>A type of its own rather than a plain list, so a bucket is told apart from a bare
         * element by what it is - an element can be any list at all, and testing for one would file
         * it wrongly.
         *
         * @param <E> the element type
         */
        private static final class Bucket<E> extends ArrayList<E> {

            private Bucket(@NotNull E first, @NotNull E second) {
                super(4);
                this.add(first);
                this.add(second);
            }

        }

    }

}
