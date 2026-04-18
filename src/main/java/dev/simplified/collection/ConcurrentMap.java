package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A thread-safe map backed by a {@link HashMap} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Supports snapshot-based iteration
 * over entries, keys, and values.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentMap<K, V> extends AtomicMap<K, V, AbstractMap<K, V>> {

    /**
     * Create a new concurrent map.
     */
    public ConcurrentMap() {
        super(new HashMap<>(), (Map<K, V>) null);
    }

    /**
     * Create a new concurrent map and fill it with the given pairs.
     */
    @SafeVarargs
    public ConcurrentMap(@Nullable Map.Entry<K, V>... pairs) {
        super(new HashMap<>(), pairs);
    }

    /**
     * Create a new concurrent map and fill it with the given map.
     */
    public ConcurrentMap(@Nullable Map<? extends K, ? extends V> map) {
        super(new HashMap<>(), map);
    }

    /**
     * Create a new concurrent map with the given backing map.
     *
     * @param backingMap the backing map implementation
     * @param map the source map to copy from, or {@code null} for an empty map
     */
    protected ConcurrentMap(@NotNull AbstractMap<K, V> backingMap, @Nullable Map<? extends K, ? extends V> map) {
        super(backingMap, map);
    }

    /**
     * Create a new concurrent map with the given backing map and fill it with the given pairs.
     *
     * @param backingMap the backing map implementation
     * @param pairs the entries to include
     */
    @SafeVarargs
    protected ConcurrentMap(@NotNull AbstractMap<K, V> backingMap, @Nullable Map.Entry<K, V>... pairs) {
        super(backingMap, pairs);
    }

    /**
     * Constructs a {@code ConcurrentMap} sharing the given source's {@code ref} and lock.
     * Used by {@link dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableMap} to
     * present a live, unmodifiable view over any existing {@link AtomicMap}.
     *
     * @param source the source map whose state is shared
     */
    protected ConcurrentMap(@NotNull AtomicMap<K, V, ? extends AbstractMap<K, V>> source) {
        super(source);
    }

    /**
     * Returns a live, unmodifiable view of this {@code ConcurrentMap}. The returned wrapper
     * shares this map's state, so subsequent mutations on this map are visible through the
     * wrapper. Mutations attempted on the wrapper throw {@link UnsupportedOperationException}.
     *
     * @return an unmodifiable {@link ConcurrentMap} view over the same state
     */
    public @NotNull ConcurrentMap<K, V> toUnmodifiableMap() {
        return Concurrent.newUnmodifiableMap(this);
    }

}
