package dev.simplified.collection.sorted;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableSet;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * An unmodifiable thread-safe map backed by a {@link TreeMap} that permits concurrent reads
 * but rejects all modifications. Mutating operations throw {@link UnsupportedOperationException}.
 * Entry sets, key sets, and values views are also unmodifiable. Maintains key ordering defined
 *  * by a {@link Comparator} or the keys' natural ordering.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentUnmodifiableSortedMap<K, V> extends ConcurrentSortedMap<K, V> {

    /** Lazily initialized unmodifiable view of the key set. */
    private transient Set<K> unmodifiableKeySet;
    /** Lazily initialized unmodifiable view of the entry set. */
    private transient Set<Entry<K,V>> unmodifiableEntrySet;
    /** Lazily initialized unmodifiable view of the values collection. */
    private transient Collection<V> unmodifiableValues;

    /**
     * Create a new concurrent sorted map with natural key ordering.
     */
    public ConcurrentUnmodifiableSortedMap() {
        super();
    }

    /**
     * Create a new concurrent sorted map with the given comparator.
     *
     * @param comparator the comparator used to order the keys
     */
    public ConcurrentUnmodifiableSortedMap(@NotNull Comparator<? super K> comparator) {
        super(comparator);
    }

    /**
     * Create a new concurrent sorted map with natural key ordering and fill it with the given pairs.
     */
    @SafeVarargs
    public ConcurrentUnmodifiableSortedMap(@Nullable Map.Entry<K, V>... pairs) {
        super(pairs);
    }

    /**
     * Create a new concurrent sorted map with the given comparator and fill it with the given pairs.
     *
     * @param comparator the comparator used to order the keys
     * @param pairs the entries to include
     */
    @SafeVarargs
    public ConcurrentUnmodifiableSortedMap(@NotNull Comparator<? super K> comparator, @Nullable Map.Entry<K, V>... pairs) {
        super(comparator, pairs);
    }

    /**
     * Create a new concurrent sorted map with natural key ordering and fill it with the given map.
     */
    public ConcurrentUnmodifiableSortedMap(@Nullable Map<? extends K, ? extends V> map) {
        super(map);
    }

    /**
     * Create a new concurrent sorted map with the given comparator and fill it with the given map.
     *
     * @param comparator the comparator used to order the keys
     * @param map the source map to copy from
     */
    public ConcurrentUnmodifiableSortedMap(@NotNull Comparator<? super K> comparator, @Nullable Map<? extends K, ? extends V> map) {
        super(comparator, map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a lazily initialized unmodifiable view of the entry set, where each entry
     * prevents modification of the backing map via {@link Entry#setValue(Object)}.
     */
    @Override
    public final @NotNull Set<Entry<K, V>> entrySet() {
        if (this.unmodifiableEntrySet == null)
            this.unmodifiableEntrySet = new UnmodifiableEntrySet<>(super.entrySet());

        return this.unmodifiableEntrySet;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a lazily initialized unmodifiable view of the key set.
     */
    @Override
    public final @NotNull Set<K> keySet() {
        if (this.unmodifiableKeySet == null)
            this.unmodifiableKeySet = Concurrent.newUnmodifiableSet(super.keySet());

        return this.unmodifiableKeySet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final @Nullable V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void putAll(@NotNull Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final @Nullable V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final @Nullable V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a lazily initialized unmodifiable view of the values collection.
     */
    @Override
    public final @NotNull Collection<V> values() {
        if (this.unmodifiableValues == null)
            this.unmodifiableValues = Concurrent.newUnmodifiableCollection(super.values());

        return this.unmodifiableValues;
    }

    /**
     * We need this class in addition to UnmodifiableSet as
     * Map.Entries themselves permit modification of the backing Map
     * via their setValue operation.  This class is subtle: there are
     * many possible attacks that must be thwarted.
     */
    @AllArgsConstructor
    private static class UnmodifiableEntrySet<K, V> extends ConcurrentUnmodifiableSet<Entry<K, V>> {

        private @NotNull Iterator<Entry<K, V>> iterator;

        /**
         * Creates a new unmodifiable entry set backed by the given set of entries.
         *
         * @param entries the backing entry set, or {@code null} for an empty set
         */
        public UnmodifiableEntrySet(@Nullable Set<Entry<K, V>> entries) {
            super(entries);
        }

        /**
         * {@inheritDoc}
         */
        public void forEach(@NotNull Consumer<? super Entry<K, V>> action) {
            Objects.requireNonNull(action);
            this.ref.forEach(UnmodifiableEntry.wrap(action));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull Spliterator<Entry<K, V>> spliterator() {
            return new UnmodifiableSpliterator<>(this.ref.spliterator());
        }

        /**
         * {@inheritDoc}
         * <p>
         * The returned iterator wraps each entry in an {@link UnmodifiableEntry} to prevent
         * modification via {@link Entry#setValue(Object)}.
         */
        @Override
        public @NotNull Iterator<Entry<K, V>> iterator() {
            return new Iterator<>() {

                private final Iterator<? extends Entry<? extends K, ? extends V>> iterator = ConcurrentUnmodifiableSortedMap.UnmodifiableEntrySet.this.ref.iterator();

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean hasNext() {
                    return this.iterator.hasNext();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Entry<K, V> next() {
                    return new UnmodifiableEntry<>(this.iterator.next());
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public @NotNull Object @NotNull [] toArray() {
            Object[] a = this.ref.toArray();

            for (int i = 0; i < a.length; i++)
                a[i] = new UnmodifiableEntry<>((Entry<? extends K, ? extends V>) a[i]);

            return a;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("all")
        public <T> T @NotNull [] toArray(T @NotNull [] array) {
            // We don't pass ref.toArray, to avoid window of
            // vulnerability wherein an unscrupulous multithreaded client
            // could get his hands on raw (unwrapped) Entries from ref
            Object[] arr = this.ref.toArray(array.length == 0 ? array : Arrays.copyOf(array, 0));

            for (int i = 0; i < arr.length; i++)
                arr[i] = new UnmodifiableEntry<>((Entry<? extends K, ? extends V>) arr[i]);

            if (arr.length > array.length)
                return (T[]) arr;

            System.arraycopy(arr, 0, array, 0, arr.length);
            if (array.length > arr.length)
                array[arr.length] = null;

            return array;
        }

        /**
         * This method is overridden to protect the backing set against
         * an object with a nefarious equals function that senses
         * that the equality-candidate is Map.Entry and calls its
         * setValue method.
         */
        @Override
        public boolean contains(Object item) {
            if (!(item instanceof Map.Entry)) return false;
            return this.ref.contains(new UnmodifiableEntry<>((Entry<?,?>) item));
        }

        /**
         * The next two methods are overridden to protect against
         * an unscrupulous List which contains(Object o) method senses
         * when o is a Map.Entry, and calls o.setValue.
         */
        @Override
        public boolean containsAll(@NotNull Collection<?> coll) {
            for (Object e : coll) {
                if (!this.contains(e)) // Invokes safe contains() above
                    return false;
            }

            return true;
        }

    }

    /**
     * This "wrapper class" serves two purposes: it prevents
     * the client from modifying the backing Map, by short-circuiting
     * the setValue method, and it protects the backing Map against
     * an ill-behaved Map.Entry that attempts to modify another
     * Map Entry when asked to perform an equality check.
     */
    @AllArgsConstructor
    private static class UnmodifiableEntry<K, V> implements Entry<K, V> {

        private final Entry<? extends K, ? extends V> entry;

        /**
         * {@inheritDoc}
         */
        @Override
        public K getKey() {
            return this.entry.getKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V getValue() {
            return this.entry.getValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.entry.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Map.Entry<?, ?> t)) return false;

            return (this.entry.getKey() == null ? t.getKey() == null : this.entry.getKey().equals(t.getKey())) &&
                    (this.entry.getValue() == null ? t.getValue() == null : this.entry.getValue().equals(t.getValue()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.entry.toString();
        }

        /**
         * Wraps a consumer so that each entry passed to it is wrapped in an {@link UnmodifiableEntry}.
         *
         * @param action the consumer to wrap
         * @param <K> the key type
         * @param <V> the value type
         * @return a consumer that wraps entries before passing them to the given action
         */
        private static <K, V> @NotNull Consumer<Entry<K, V>> wrap(@NotNull Consumer<? super Entry<K, V>> action) {
            return entry -> action.accept(new UnmodifiableEntry<>(entry));
        }

    }

    /**
     * A {@link Spliterator} wrapper that wraps each entry in an {@link UnmodifiableEntry}
     * to prevent modification of the backing map.
     */
    @AllArgsConstructor
    private static class UnmodifiableSpliterator<K, V> implements Spliterator<Entry<K,V>> {

        private final @NotNull Spliterator<Entry<K, V>> spliterator;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean tryAdvance(@NotNull Consumer<? super Entry<K, V>> action) {
            Objects.requireNonNull(action);
            return this.spliterator.tryAdvance(UnmodifiableEntry.wrap(action));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void forEachRemaining(@NotNull Consumer<? super Entry<K, V>> action) {
            Objects.requireNonNull(action);
            this.spliterator.forEachRemaining(UnmodifiableEntry.wrap(action));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @Nullable Spliterator<Entry<K, V>> trySplit() {
            Spliterator<Entry<K, V>> split = this.spliterator.trySplit();
            return split == null ? null : new UnmodifiableSpliterator<>(split);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long estimateSize() {
            return this.spliterator.estimateSize();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getExactSizeIfKnown() {
            return this.spliterator.getExactSizeIfKnown();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int characteristics() {
            return this.spliterator.characteristics();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasCharacteristics(int characteristics) {
            return this.spliterator.hasCharacteristics(characteristics);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull Comparator<? super Entry<K, V>> getComparator() {
            return this.spliterator.getComparator();
        }

    }

}
