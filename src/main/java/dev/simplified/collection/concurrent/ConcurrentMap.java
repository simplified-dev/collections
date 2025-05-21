package dev.sbs.api.collection.concurrent;

import dev.sbs.api.collection.concurrent.atomic.AtomicMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent map that allows for simultaneous fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the map by replacing the
 * entire map each modification. This allows for maintaining the original speed
 * of {@link HashMap#containsKey(Object)} and {@link HashMap#containsValue(Object)} and makes it cross-thread-safe.
 *
 * @param <K> type of keys
 * @param <V> type of values
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

    public @NotNull ConcurrentMap<K, V> toUnmodifiableMap() {
        return Concurrent.newUnmodifiableMap(this);
    }

}
