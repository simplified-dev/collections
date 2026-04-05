package dev.simplified.collection.concurrent.sorted;

import dev.simplified.collection.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A thread-safe map backed by a {@link TreeMap} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Maintains key ordering defined
 * by a {@link Comparator} or the keys' natural ordering.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentSortedMap<K, V> extends ConcurrentMap<K, V> {

    /**
     * Create a new concurrent sorted map with natural key ordering.
     */
    public ConcurrentSortedMap() {
        super(new TreeMap<>(), (Map<K, V>) null);
    }

    /**
     * Create a new concurrent sorted map with the given comparator.
     *
     * @param comparator the comparator used to order the keys
     */
    public ConcurrentSortedMap(@NotNull Comparator<? super K> comparator) {
        super(new TreeMap<>(comparator), (Map<K, V>) null);
    }

    /**
     * Create a new concurrent sorted map with natural key ordering and fill it with the given pairs.
     */
    @SafeVarargs
    public ConcurrentSortedMap(@Nullable Map.Entry<K, V>... pairs) {
        super(new TreeMap<>(), pairs);
    }

    /**
     * Create a new concurrent sorted map with the given comparator and fill it with the given pairs.
     *
     * @param comparator the comparator used to order the keys
     * @param pairs the entries to include
     */
    @SafeVarargs
    public ConcurrentSortedMap(@NotNull Comparator<? super K> comparator, @Nullable Map.Entry<K, V>... pairs) {
        super(new TreeMap<>(comparator), pairs);
    }

    /**
     * Create a new concurrent sorted map with natural key ordering and fill it with the given map.
     */
    public ConcurrentSortedMap(@Nullable Map<? extends K, ? extends V> map) {
        super(new TreeMap<>(), map);
    }

    /**
     * Create a new concurrent sorted map with the given comparator and fill it with the given map.
     *
     * @param comparator the comparator used to order the keys
     * @param map the source map to copy from
     */
    public ConcurrentSortedMap(@NotNull Comparator<? super K> comparator, @Nullable Map<? extends K, ? extends V> map) {
        super(new TreeMap<>(comparator), map);
    }

}
