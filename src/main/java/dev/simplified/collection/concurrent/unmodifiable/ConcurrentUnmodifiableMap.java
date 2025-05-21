package dev.sbs.api.collection.concurrent.unmodifiable;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * A concurrent map that allows for simultaneous fast reading, iteration utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the map by replacing the
 * entire map each modification. This allows for maintaining the original speed
 * of {@link HashMap#containsKey(Object)} and {@link HashMap#containsValue(Object)} and makes it cross-thread-safe.
 *
 * @param <K> type of keys
 * @param <V> type of values
 */
public class ConcurrentUnmodifiableMap<K, V> extends ConcurrentMap<K, V> {

    private transient Set<K> unmodifiableKeySet;
    private transient Set<Map.Entry<K,V>> unmodifiableEntrySet;
    private transient Collection<V> unmodifiableValues;

    /**
     * Create a new unmodifiable concurrent map.
     */
    public ConcurrentUnmodifiableMap() {
        super();
    }

    /**
     * Create a new unmodifiable concurrent map and fill it with the given map.
     */
    public ConcurrentUnmodifiableMap(@Nullable Map<? extends K, ? extends V> map) {
        super(map);
    }

    /**
     * Create a new concurrent map and fill it with the given pairs.
     */
    @SafeVarargs
    public ConcurrentUnmodifiableMap(@Nullable Map.Entry<K, V>... pairs) {
        super(pairs);
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override @NotNull
    public final Set<Map.Entry<K, V>> entrySet() {
        if (this.unmodifiableEntrySet == null)
            this.unmodifiableEntrySet = new UnmodifiableEntrySet<>(super.entrySet());

        return this.unmodifiableEntrySet;
    }

    @Override @NotNull
    public final Set<K> keySet() {
        if (this.unmodifiableKeySet == null)
            this.unmodifiableKeySet = Concurrent.newUnmodifiableSet(super.keySet());

        return this.unmodifiableKeySet;
    }

    @Override
    public final V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void putAll(@NotNull Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

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
    private static class UnmodifiableEntrySet<K, V> extends ConcurrentUnmodifiableSet<Map.Entry<K, V>> {

        public UnmodifiableEntrySet(Set<Map.Entry<K, V>> entries) {
            super(entries);
        }

        static <K, V> Consumer<Map.Entry<K, V>> entryConsumer(Consumer<? super Map.Entry<K, V>> action) {
            return e -> action.accept(new UnmodifiableEntry<>(e));
        }

        public void forEach(Consumer<? super Map.Entry<K, V>> action) {
            Objects.requireNonNull(action);
            this.ref.forEach(entryConsumer(action));
        }

        @Override
        public @NotNull Spliterator<Map.Entry<K, V>> spliterator() {
            return new UnmodifiableSpliterator<>(this.ref.spliterator());
        }

        @AllArgsConstructor
        static class UnmodifiableSpliterator<K, V> implements Spliterator<Map.Entry<K,V>> {

            private final Spliterator<Map.Entry<K, V>> spliterator;

            @Override
            public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
                Objects.requireNonNull(action);
                return this.spliterator.tryAdvance(entryConsumer(action));
            }

            @Override
            public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
                Objects.requireNonNull(action);
                this.spliterator.forEachRemaining(entryConsumer(action));
            }

            @Override
            public Spliterator<Map.Entry<K, V>> trySplit() {
                Spliterator<Map.Entry<K, V>> split = this.spliterator.trySplit();
                return split == null ? null : new UnmodifiableSpliterator<>(split);
            }

            @Override
            public long estimateSize() {
                return this.spliterator.estimateSize();
            }

            @Override
            public long getExactSizeIfKnown() {
                return this.spliterator.getExactSizeIfKnown();
            }

            @Override
            public int characteristics() {
                return this.spliterator.characteristics();
            }

            @Override
            public boolean hasCharacteristics(int characteristics) {
                return this.spliterator.hasCharacteristics(characteristics);
            }

            @Override
            public Comparator<? super Map.Entry<K, V>> getComparator() {
                return this.spliterator.getComparator();
            }

        }

        @Override @NotNull
        public Iterator<Map.Entry<K,V>> iterator() {
            return new Iterator<>() {

                private final Iterator<? extends Map.Entry<? extends K, ? extends V>> iterator = UnmodifiableEntrySet.this.ref.iterator();

                public boolean hasNext() {
                    return this.iterator.hasNext();
                }

                public Map.Entry<K, V> next() {
                    return new UnmodifiableEntry<>(this.iterator.next());
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull Object @NotNull [] toArray() {
            Object[] a = this.ref.toArray();

            for (int i = 0; i < a.length; i++)
                a[i] = new UnmodifiableEntry<>((Map.Entry<? extends K, ? extends V>) a[i]);

            return a;
        }

        @Override
        @SuppressWarnings("all")
        public <T> @NotNull T[] toArray(T[] array) {
            // We don't pass a to ref.toArray, to avoid window of
            // vulnerability wherein an unscrupulous multithreaded client
            // could get his hands on raw (unwrapped) Entries from ref.
            Object[] arr = this.ref.toArray(array.length == 0 ? array : Arrays.copyOf(array, 0));

            for (int i = 0; i < arr.length; i++)
                arr[i] = new UnmodifiableEntry<>((Map.Entry<? extends K, ? extends V>)arr[i]);

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
            return this.ref.contains(new UnmodifiableEntry<>((Map.Entry<?,?>) item));
        }

        /**
         * The next two methods are overridden to protect against
         * an unscrupulous List whose contains(Object o) method senses
         * when o is a Map.Entry, and calls o.setValue.
         */
        @Override
        public boolean containsAll(Collection<?> coll) {
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
    private static class UnmodifiableEntry<K, V> implements Map.Entry<K, V> {

        private final Map.Entry<? extends K, ? extends V> entry;

        @Override
        public K getKey() {
            return this.entry.getKey();
        }

        @Override
        public V getValue() {
            return this.entry.getValue();
        }

        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public int hashCode() {
            return this.entry.hashCode();
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Map.Entry<?, ?> t)) return false;

            return (this.entry.getKey() == null ? t.getKey() == null : this.entry.getKey().equals(t.getKey())) &&
                    (this.entry.getValue() == null ? t.getValue() == null : this.entry.getValue().equals(t.getValue()));
        }

        public String toString() {
            return this.entry.toString();
        }

    }

}
