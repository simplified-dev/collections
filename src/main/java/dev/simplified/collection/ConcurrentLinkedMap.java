package dev.simplified.collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link ConcurrentMap} backed by an insertion-ordered {@link LinkedHashMap} with
 * concurrent read and write access via {@link ReadWriteLock}. Supports an optional maximum size
 * with eldest-entry eviction.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentLinkedMap<K, V> extends ConcurrentHashMap<K, V> {

	/**
	 * Creates a new concurrent linked map.
	 */
	public ConcurrentLinkedMap() {
		super(new MaxSizeLinkedMap<>(), (Map<K, V>) null);
	}

	/**
	 * Creates a new concurrent linked map with the given maximum size.
	 *
	 * @param maxSize the maximum number of entries allowed in the map
	 */
	public ConcurrentLinkedMap(int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), (Map<K, V>) null);
	}

	/**
	 * Creates a new concurrent linked map and fills it with the given map.
	 *
	 * @param map the source map to copy from
	 */
	public ConcurrentLinkedMap(@Nullable Map<? extends K, ? extends V> map) {
		super(MaxSizeLinkedMap.sized(-1, map == null ? 0 : map.size()), map);
	}

	/**
	 * Creates a new concurrent linked map and fills it with the given map.
	 *
	 * @param map the source map to copy from
	 * @param maxSize the maximum number of entries allowed in the map
	 */
	public ConcurrentLinkedMap(@Nullable Map<? extends K, ? extends V> map, int maxSize) {
		super(MaxSizeLinkedMap.sized(maxSize, map == null ? 0 : map.size()), map);
	}

	/**
	 * Constructs a {@code ConcurrentLinkedHashMap} that adopts {@code backingMap} as its storage
	 * with a fresh lock. Public callers should go through {@link #adopt(LinkedHashMap)}.
	 *
	 * @param backingMap the backing linked hash map to adopt
	 */
	protected ConcurrentLinkedMap(@NotNull LinkedHashMap<K, V> backingMap) {
		super(backingMap);
	}

	/**
	 * Constructs a {@code ConcurrentLinkedHashMap} with a pre-built backing map and an explicit
	 * lock. Used by {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap} to install
	 * a snapshot map paired with a no-op lock for wait-free reads.
	 *
	 * @param backingMap the pre-built backing map
	 * @param lock the lock guarding {@code backingMap}
	 */
	protected ConcurrentLinkedMap(@NotNull LinkedHashMap<K, V> backingMap, @NotNull ReadWriteLock lock) {
		super(backingMap, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedMap} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results. The eldest-entry eviction cap of the in-class
	 * {@code MaxSizeLinkedMap} is not applied to externally adopted backing maps.
	 *
	 * @param backing the linked hash map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent linked hash map backed by {@code backing}
	 */
	public static <K, V> @NotNull ConcurrentLinkedMap<K, V> adopt(@NotNull LinkedHashMap<K, V> backing) {
		return new ConcurrentLinkedMap<>(backing);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Overrides {@link ConcurrentHashMap#cloneRef()} to produce a {@link LinkedHashMap}
	 * snapshot preserving the source's insertion-order traversal characteristics. The
	 * eldest-entry eviction cap is intentionally not carried into the snapshot - the snapshot is
	 * immutable, so eviction is moot.</p>
	 */
	@Override
	protected @NotNull AbstractMap<K, V> cloneRef() {
		return this.withReadLock(() -> new LinkedHashMap<>(this.ref));
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentLinkedHashMap} preserving insertion
	 * order.
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentMap<K, V> toUnmodifiable() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedMap<>((LinkedHashMap<K, V>) this.cloneRef());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Adds {@link Spliterator#ORDERED} so the entry-set spliterator preserves the
	 * {@link LinkedHashMap} insertion order on parallel-stream consumers.
	 */
	@Override
	protected int entrySetSpliteratorCharacteristics() {
		return super.entrySetSpliteratorCharacteristics() | Spliterator.ORDERED;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Adds {@link Spliterator#ORDERED} so the key-set spliterator preserves the
	 * {@link LinkedHashMap} insertion order on parallel-stream consumers.
	 */
	@Override
	protected int keySetSpliteratorCharacteristics() {
		return super.keySetSpliteratorCharacteristics() | Spliterator.ORDERED;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Adds {@link Spliterator#ORDERED} so the values-collection spliterator preserves the
	 * {@link LinkedHashMap} insertion order on parallel-stream consumers.
	 */
	@Override
	protected int valuesSpliteratorCharacteristics() {
		return super.valuesSpliteratorCharacteristics() | Spliterator.ORDERED;
	}

	/**
	 * A {@link LinkedHashMap} that evicts the eldest entry once its size exceeds a fixed cap, or
	 * never evicts when the cap is {@code -1}. Final + private: the class cannot be subclassed
	 * and the containing map is the sole caller, so {@link #removeEldestEntry} is guaranteed to
	 * execute under the enclosing {@link ConcurrentLinkedMap}'s write lock.
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

		private MaxSizeLinkedMap(int maxSize, int initialCapacity, float loadFactor) {
			super(initialCapacity, loadFactor);
			this.maxSize = maxSize;
		}

		static <K, V> @NotNull MaxSizeLinkedMap<K, V> sized(int maxSize, int numMappings) {
			if (numMappings <= 0) return new MaxSizeLinkedMap<>(maxSize);
			return new MaxSizeLinkedMap<>(maxSize, (int) Math.ceil(numMappings / 0.75d), 0.75f);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return this.maxSize != -1 && this.size() > this.maxSize;
		}

	}

}
