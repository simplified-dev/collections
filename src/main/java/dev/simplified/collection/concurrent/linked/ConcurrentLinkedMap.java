package dev.simplified.collection.concurrent.linked;

import dev.simplified.collection.concurrent.ConcurrentMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A thread-safe map backed by a {@link MaxSizeLinkedMap} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Maintains insertion order and supports
 * an optional maximum size with eldest-entry eviction.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentLinkedMap<K, V> extends ConcurrentMap<K, V> {

	/**
	 * Create a new concurrent map.
	 */
	public ConcurrentLinkedMap() {
		super(new MaxSizeLinkedMap<>(), (Map<K, V>) null);
	}

	/**
	 * Create a new concurrent map.
	 *
	 * @param maxSize The maximum number of entries allowed in the map.
	 */
	public ConcurrentLinkedMap(int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), (Map<K, V>) null);
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 *
	 * @param map Map to fill the new map with.
	 */
	public ConcurrentLinkedMap(@Nullable Map<? extends K, ? extends V> map) {
		super(new MaxSizeLinkedMap<>(), map);
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 *
	 * @param map Map to fill the new map with.
	 * @param maxSize The maximum number of entries allowed in the map.
	 */
	public ConcurrentLinkedMap(@Nullable Map<? extends K, ? extends V> map, int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), map);
	}

}
