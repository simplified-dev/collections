package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicMap;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe map backed by a {@link HashMap} with concurrent read and write access
 * via {@link ReadWriteLock}. Supports snapshot-based iteration over entries, keys, and values.
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
     * Constructs a {@code ConcurrentMap} with a pre-built backing map and an explicit lock.
     * Used by {@link ConcurrentUnmodifiableMap} (and its variants) to install a snapshot map
     * paired with a no-op lock for wait-free reads.
     *
     * @param backingMap the pre-built backing map
     * @param lock the lock guarding {@code backingMap}
     */
    protected ConcurrentMap(@NotNull AbstractMap<K, V> backingMap, @NotNull ReadWriteLock lock) {
        super(backingMap, lock);
    }

    /**
     * Returns a type-preserving snapshot of this map's backing reference, captured under the
     * read lock. Subclasses backed by a different concrete {@link AbstractMap} implementation
     * override this to return an instance of that type so iteration order is preserved on
     * the snapshot.
     *
     * @return a fresh {@link AbstractMap} containing the current entries
     */
    protected @NotNull AbstractMap<K, V> cloneRef() {
        try {
            this.lock.readLock().lock();
            return new HashMap<>(this.ref);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Returns an immutable snapshot of this {@code ConcurrentMap}.
     *
     * <p>The returned wrapper owns a fresh copy of the current entries - subsequent mutations
     * on this map are not reflected in the snapshot. Reads on the snapshot are wait-free.
     * The runtime type is {@link ConcurrentUnmodifiableMap}; the declared return type is the
     * mutable parent so subclasses can covariantly override to their own
     * {@code ConcurrentUnmodifiable*} variant.</p>
     *
     * @return an immutable snapshot - runtime type is {@link ConcurrentUnmodifiableMap}
     */
    public @NotNull ConcurrentMap<K, V> toUnmodifiable() {
        return new ConcurrentUnmodifiableMap<>(this.cloneRef());
    }

}
