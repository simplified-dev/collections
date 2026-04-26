package dev.simplified.collection.tree;

import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe map backed by a {@link TreeMap} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Maintains key ordering defined
 * by a {@link Comparator} or the keys' natural ordering.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentTreeMap<K, V> extends ConcurrentMap<K, V> {

    /**
     * Create a new concurrent sorted map with natural key ordering.
     */
    public ConcurrentTreeMap() {
        super(new TreeMap<>(), (Map<K, V>) null);
    }

    /**
     * Create a new concurrent sorted map with the given comparator.
     *
     * @param comparator the comparator used to order the keys
     */
    public ConcurrentTreeMap(@NotNull Comparator<? super K> comparator) {
        super(new TreeMap<>(comparator), (Map<K, V>) null);
    }

    /**
     * Create a new concurrent sorted map with natural key ordering and fill it with the given pairs.
     */
    @SafeVarargs
    public ConcurrentTreeMap(@Nullable Map.Entry<K, V>... pairs) {
        super(new TreeMap<>(), pairs);
    }

    /**
     * Create a new concurrent sorted map with the given comparator and fill it with the given pairs.
     *
     * @param comparator the comparator used to order the keys
     * @param pairs the entries to include
     */
    @SafeVarargs
    public ConcurrentTreeMap(@NotNull Comparator<? super K> comparator, @Nullable Map.Entry<K, V>... pairs) {
        super(new TreeMap<>(comparator), pairs);
    }

    /**
     * Create a new concurrent sorted map with natural key ordering and fill it with the given map.
     */
    public ConcurrentTreeMap(@Nullable Map<? extends K, ? extends V> map) {
        super(new TreeMap<>(), map);
    }

    /**
     * Create a new concurrent sorted map with the given comparator and fill it with the given map.
     *
     * @param comparator the comparator used to order the keys
     * @param map the source map to copy from
     */
    public ConcurrentTreeMap(@NotNull Comparator<? super K> comparator, @Nullable Map<? extends K, ? extends V> map) {
        super(new TreeMap<>(comparator), map);
    }

    /**
     * Constructs a {@code ConcurrentTreeMap} with a pre-built backing map and an explicit
     * lock. Used by {@link ConcurrentUnmodifiableTreeMap} to install a snapshot map paired
     * with a no-op lock for wait-free reads.
     *
     * @param backingMap the pre-built backing map
     * @param lock the lock guarding {@code backingMap}
     */
    protected ConcurrentTreeMap(@NotNull TreeMap<K, V> backingMap, @NotNull ReadWriteLock lock) {
        super(backingMap, lock);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overrides {@link ConcurrentMap#cloneRef()} to produce a {@link TreeMap} snapshot.
     * {@link TreeMap#TreeMap(java.util.SortedMap)} preserves the source's comparator when
     * the source is itself a {@code SortedMap}.</p>
     */
    @Override
    protected @NotNull AbstractMap<K, V> cloneRef() {
        try {
            this.lock.readLock().lock();
            return new TreeMap<>((TreeMap<K, V>) this.ref);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Returns an immutable snapshot of this {@code ConcurrentTreeMap} preserving the source's
     * comparator and sort order.
     *
     * @return an unmodifiable {@link ConcurrentTreeMap} containing a snapshot of the entries
     */
    @Override
    public @NotNull ConcurrentTreeMap<K, V> toUnmodifiable() {
        return new ConcurrentUnmodifiableTreeMap<>((TreeMap<K, V>) this.cloneRef());
    }

}
