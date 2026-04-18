package dev.simplified.collection.linked;

import dev.simplified.collection.ConcurrentMap;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A thread-safe map backed by an insertion-ordered {@link LinkedHashMap} with concurrent read and
 * write access via {@link java.util.concurrent.locks.ReadWriteLock}. Supports an optional maximum
 * size with eldest-entry eviction.
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
	 * @param maxSize the maximum number of entries allowed in the map
	 */
	public ConcurrentLinkedMap(int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), (Map<K, V>) null);
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 *
	 * @param map map to fill the new map with
	 */
	public ConcurrentLinkedMap(@Nullable Map<? extends K, ? extends V> map) {
		super(new MaxSizeLinkedMap<>(), map);
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 *
	 * @param map map to fill the new map with
	 * @param maxSize the maximum number of entries allowed in the map
	 */
	public ConcurrentLinkedMap(@Nullable Map<? extends K, ? extends V> map, int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), map);
	}

	/**
	 * A {@link LinkedHashMap} that evicts the eldest entry once its size exceeds a fixed
	 * cap, or never evicts when the cap is {@code -1}. Final + private: the class cannot
	 * be subclassed and the containing map is the sole caller, so {@link #removeEldestEntry}
	 * is guaranteed to execute under the enclosing {@link ConcurrentMap}'s write lock.
	 *
	 * @param <K> the type of keys
	 * @param <V> the type of values
	 */
	private static final class MaxSizeLinkedMap<K, V> extends LinkedHashMap<K, V> {

		private final int maxSize;

		MaxSizeLinkedMap() {
			this(-1);
		}

		MaxSizeLinkedMap(int maxSize) {
			this.maxSize = maxSize;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return this.maxSize != -1 && this.size() > this.maxSize;
		}

	}

}
