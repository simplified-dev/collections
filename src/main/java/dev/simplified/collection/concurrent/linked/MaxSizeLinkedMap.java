package dev.simplified.collection.concurrent.linked;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maximum size linked HashMap for cached data.
 *
 * @param <K> Key
 * @param <V> Value
 */
public class MaxSizeLinkedMap<K, V> extends LinkedHashMap<K, V> {

    /** The maximum number of entries allowed, or {@code -1} for unlimited. */
    private final int maxSize;

    /**
     * Creates a new map with no size limit.
     */
    public MaxSizeLinkedMap() {
        this(-1);
    }

    /**
     * Creates a new map with the specified maximum size.
     *
     * @param maxSize the maximum number of entries, or {@code -1} for unlimited
     */
    public MaxSizeLinkedMap(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Creates a new map populated with entries from the given map, with no size limit.
     *
     * @param map the map whose entries are to be copied
     */
    public MaxSizeLinkedMap(@NotNull Map<? extends K, ? extends V> map) {
        this(map, -1);
    }

    /**
     * Creates a new map populated with entries from the given map, with the specified maximum size.
     *
     * @param map the map whose entries are to be copied
     * @param maxSize the maximum number of entries, or {@code -1} for unlimited
     */
    public MaxSizeLinkedMap(@NotNull Map<? extends K, ? extends V> map, int maxSize) {
        super(map);
        this.maxSize = maxSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.maxSize != -1 && this.size() > this.maxSize;
    }

}
