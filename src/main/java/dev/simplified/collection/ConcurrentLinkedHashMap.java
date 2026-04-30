package dev.simplified.collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link ConcurrentMap} backed by an insertion-ordered {@link LinkedHashMap} with
 * concurrent read and write access via {@link ReadWriteLock}. Supports an optional maximum size
 * with eldest-entry eviction.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentLinkedHashMap<K, V> extends ConcurrentHashMap<K, V> {

	/**
	 * Creates a new concurrent linked map.
	 */
	public ConcurrentLinkedHashMap() {
		super(new MaxSizeLinkedMap<>(), (Map<K, V>) null);
	}

	/**
	 * Creates a new concurrent linked map with the given maximum size.
	 *
	 * @param maxSize the maximum number of entries allowed in the map
	 */
	public ConcurrentLinkedHashMap(int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), (Map<K, V>) null);
	}

	/**
	 * Creates a new concurrent linked map and fills it with the given map.
	 *
	 * @param map the source map to copy from
	 */
	public ConcurrentLinkedHashMap(@Nullable Map<? extends K, ? extends V> map) {
		super(new MaxSizeLinkedMap<>(), map);
	}

	/**
	 * Creates a new concurrent linked map and fills it with the given map.
	 *
	 * @param map the source map to copy from
	 * @param maxSize the maximum number of entries allowed in the map
	 */
	public ConcurrentLinkedHashMap(@Nullable Map<? extends K, ? extends V> map, int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), map);
	}

	/**
	 * Constructs a {@code ConcurrentLinkedHashMap} that adopts {@code backingMap} as its storage
	 * with a fresh lock. Public callers should go through {@link #adopt(LinkedHashMap)}.
	 *
	 * @param backingMap the backing linked hash map to adopt
	 */
	protected ConcurrentLinkedHashMap(@NotNull LinkedHashMap<K, V> backingMap) {
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
	protected ConcurrentLinkedHashMap(@NotNull LinkedHashMap<K, V> backingMap, @NotNull ReadWriteLock lock) {
		super(backingMap, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedHashMap} without copying.
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
	public static <K, V> @NotNull ConcurrentLinkedHashMap<K, V> adopt(@NotNull LinkedHashMap<K, V> backing) {
		return new ConcurrentLinkedHashMap<>(backing);
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
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedHashMap<>((LinkedHashMap<K, V>) this.cloneRef());
	}

	/**
	 * A {@link LinkedHashMap} that evicts the eldest entry once its size exceeds a fixed cap, or
	 * never evicts when the cap is {@code -1}. Final + private: the class cannot be subclassed
	 * and the containing map is the sole caller, so {@link #removeEldestEntry} is guaranteed to
	 * execute under the enclosing {@link ConcurrentLinkedHashMap}'s write lock.
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
