package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link ConcurrentMap} backed by a {@link HashMap} with concurrent read and write
 * access via {@link ReadWriteLock}. Supports snapshot-based iteration over entries, keys, and
 * values.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentHashMap<K, V> extends AtomicMap<K, V, AbstractMap<K, V>> implements ConcurrentMap<K, V> {

	/**
	 * Creates a new concurrent map.
	 */
	public ConcurrentHashMap() {
		super(new HashMap<>(), (Map<K, V>) null);
	}

	/**
	 * Creates a new concurrent map and fills it with the given pairs.
	 *
	 * @param pairs the entries to include
	 */
	@SafeVarargs
	public ConcurrentHashMap(@Nullable Map.Entry<K, V>... pairs) {
		super(new HashMap<>(), pairs);
	}

	/**
	 * Creates a new concurrent map and fills it with the given map.
	 *
	 * @param map the source map to copy from
	 */
	public ConcurrentHashMap(@Nullable Map<? extends K, ? extends V> map) {
		super(new HashMap<>(), map);
	}

	/**
	 * Constructs a {@code ConcurrentHashMap} that adopts {@code backingMap} as its storage with a
	 * fresh lock. Public callers should go through {@link #adopt(AbstractMap)}.
	 *
	 * @param backingMap the backing map to adopt
	 */
	protected ConcurrentHashMap(@NotNull AbstractMap<K, V> backingMap) {
		super(backingMap);
	}

	/**
	 * Creates a new concurrent map with the given backing map.
	 *
	 * @param backingMap the backing map implementation
	 * @param map the source map to copy from, or {@code null} for an empty map
	 */
	protected ConcurrentHashMap(@NotNull AbstractMap<K, V> backingMap, @Nullable Map<? extends K, ? extends V> map) {
		super(backingMap, map);
	}

	/**
	 * Creates a new concurrent map with the given backing map and fills it with the given pairs.
	 *
	 * @param backingMap the backing map implementation
	 * @param pairs the entries to include
	 */
	@SafeVarargs
	protected ConcurrentHashMap(@NotNull AbstractMap<K, V> backingMap, @Nullable Map.Entry<K, V>... pairs) {
		super(backingMap, pairs);
	}

	/**
	 * Constructs a {@code ConcurrentHashMap} with a pre-built backing map and an explicit lock.
	 * Used by {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap} (and its variants) to
	 * install a snapshot map paired with a no-op lock for wait-free reads.
	 *
	 * @param backingMap the pre-built backing map
	 * @param lock the lock guarding {@code backingMap}
	 */
	protected ConcurrentHashMap(@NotNull AbstractMap<K, V> backingMap, @NotNull ReadWriteLock lock) {
		super(backingMap, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentHashMap} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results.
	 *
	 * @param backing the map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent hash map backed by {@code backing}
	 */
	public static <K, V> @NotNull ConcurrentHashMap<K, V> adopt(@NotNull AbstractMap<K, V> backing) {
		return new ConcurrentHashMap<>(backing);
	}

	/**
	 * Returns a type-preserving snapshot of this map's backing reference, captured under the read
	 * lock. Subclasses backed by a different concrete {@link AbstractMap} implementation override
	 * this to return an instance of that type so iteration order is preserved on the snapshot.
	 *
	 * @return a fresh {@link AbstractMap} containing the current entries
	 */
	protected @NotNull AbstractMap<K, V> cloneRef() {
		return this.withReadLock(() -> new HashMap<>(this.ref));
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentHashMap}.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current entries - subsequent mutations on
	 * this map are not reflected in the snapshot. Reads on the snapshot are wait-free.</p>
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentMap<K, V> toUnmodifiable() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentHashMap<>(this.cloneRef());
	}

}
