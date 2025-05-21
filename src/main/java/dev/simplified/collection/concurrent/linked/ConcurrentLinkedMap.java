package dev.sbs.api.collection.concurrent.linked;

import dev.sbs.api.collection.MaxSizeLinkedMap;
import dev.sbs.api.collection.concurrent.atomic.AtomicMap;
import org.jetbrains.annotations.Nullable;

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
public class ConcurrentLinkedMap<K, V> extends AtomicMap<K, V, MaxSizeLinkedMap<K, V>> {

	/**
	 * Create a new concurrent map.
	 */
	public ConcurrentLinkedMap() {
		super(new MaxSizeLinkedMap<>(), (HashMap<K, V>) null);
	}

	/**
	 * Create a new concurrent map.
	 *
	 * @param maxSize The maximum number of entries allowed in the map.
	 */
	public ConcurrentLinkedMap(int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), (HashMap<K, V>) null);
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
