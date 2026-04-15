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
     * Creates a new empty {@code ConcurrentSet} for holding map entries, used internally by entry set operations.
     * <p>
     * Overridable so subclasses backed by an insertion-ordered map (e.g. {@link dev.simplified.collection.linked.ConcurrentLinkedMap ConcurrentLinkedMap})
     * can return an insertion-ordered set type - otherwise {@link AtomicMap#entrySet()} copies the
     * backing map's entries into a {@link java.util.HashSet}-backed set and silently loses order,
     * which also breaks the default {@link java.util.Map#forEach} implementation.
     *
     * @return a new empty {@link ConcurrentSet} of entries
     */
    @Override
    protected @NotNull ConcurrentSet<Entry<K, V>> createEmptyEntrySet() {
        return Concurrent.newSet();
    }

    /**
     * Creates a new empty {@code ConcurrentSet} for holding map keys, used internally by key set operations.
     * <p>
     * Overridable for the same reason as {@link #createEmptyEntrySet()}.
     *
     * @return a new empty {@link ConcurrentSet} of keys
     */
    @Override
    protected @NotNull ConcurrentSet<K> createEmptyKeySet() {
        return Concurrent.newSet();
    }

    /**
     * Creates a new empty {@code ConcurrentList} for holding map values, used internally by values operations.
     * <p>
     * Overridable so subclasses can match the backing map's value iteration order.
     *
     * @return a new empty {@link ConcurrentList} of values
     */
    @Override
    protected @NotNull ConcurrentList<V> createEmptyValueList() {
        return Concurrent.newList();
    }

    /**
     * Returns an unmodifiable view of this {@code ConcurrentMap}.
     * Attempts to modify the returned map will throw {@link UnsupportedOperationException}.
     *
     * @return an unmodifiable {@link ConcurrentMap} containing the same entries
     */
    public @NotNull ConcurrentMap<K, V> toUnmodifiableMap() {
        return Concurrent.newUnmodifiableMap(this);
    }

}
