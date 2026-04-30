package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicNavigableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link ConcurrentMap} backed by a {@link TreeMap} that maintains its entries in
 * key-sorted order according to the keys' natural ordering or a {@link Comparator} provided at
 * construction time.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentTreeMap<K, V> extends AtomicNavigableMap<K, V, TreeMap<K, V>> implements ConcurrentMap<K, V>, NavigableMap<K, V> {

	/**
	 * Creates a new concurrent sorted map with natural key ordering.
	 */
	public ConcurrentTreeMap() {
		super(new TreeMap<>(), (Map<K, V>) null);
	}

	/**
	 * Creates a new concurrent sorted map with the given comparator.
	 *
	 * @param comparator the comparator used to order the keys
	 */
	public ConcurrentTreeMap(@NotNull Comparator<? super K> comparator) {
		super(new TreeMap<>(comparator), (Map<K, V>) null);
	}

	/**
	 * Creates a new concurrent sorted map with natural key ordering and fills it with the given
	 * pairs.
	 *
	 * @param pairs the entries to include
	 */
	@SafeVarargs
	public ConcurrentTreeMap(@Nullable Map.Entry<K, V>... pairs) {
		super(new TreeMap<>(), pairs);
	}

	/**
	 * Creates a new concurrent sorted map with the given comparator and fills it with the given
	 * pairs.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param pairs the entries to include
	 */
	@SafeVarargs
	public ConcurrentTreeMap(@NotNull Comparator<? super K> comparator, @Nullable Map.Entry<K, V>... pairs) {
		super(new TreeMap<>(comparator), pairs);
	}

	/**
	 * Creates a new concurrent sorted map with natural key ordering and fills it with the given
	 * map.
	 *
	 * @param map the source map to copy from
	 */
	public ConcurrentTreeMap(@Nullable Map<? extends K, ? extends V> map) {
		super(new TreeMap<>(), map);
	}

	/**
	 * Creates a new concurrent sorted map with the given comparator and fills it with the given
	 * map.
	 *
	 * @param comparator the comparator used to order the keys
	 * @param map the source map to copy from
	 */
	public ConcurrentTreeMap(@NotNull Comparator<? super K> comparator, @Nullable Map<? extends K, ? extends V> map) {
		super(new TreeMap<>(comparator), map);
	}

	/**
	 * Constructs a {@code ConcurrentTreeMap} that adopts {@code backingMap} as its storage with a
	 * fresh lock. Public callers should go through {@link #adopt(TreeMap)}.
	 *
	 * @param backingMap the backing tree map to adopt
	 */
	protected ConcurrentTreeMap(@NotNull TreeMap<K, V> backingMap) {
		super(backingMap);
	}

	/**
	 * Constructs a {@code ConcurrentTreeMap} with a pre-built backing map and an explicit lock.
	 * Used by {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap} to install a snapshot
	 * map paired with a no-op lock for wait-free reads.
	 *
	 * @param backingMap the pre-built backing map
	 * @param lock the lock guarding {@code backingMap}
	 */
	protected ConcurrentTreeMap(@NotNull TreeMap<K, V> backingMap, @NotNull ReadWriteLock lock) {
		super(backingMap, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentTreeMap} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results. The adopted map's comparator is preserved.
	 *
	 * @param backing the tree map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent tree map backed by {@code backing}
	 */
	public static <K, V> @NotNull ConcurrentTreeMap<K, V> adopt(@NotNull TreeMap<K, V> backing) {
		return new ConcurrentTreeMap<>(backing);
	}

	/**
	 * Returns a {@link TreeMap}-typed snapshot of this map's backing reference, captured under
	 * the read lock. {@link TreeMap#TreeMap(java.util.SortedMap)} preserves the source's
	 * comparator when the source is itself a {@code SortedMap}.
	 *
	 * @return a fresh {@link TreeMap} containing the current entries
	 */
	protected @NotNull TreeMap<K, V> cloneRef() {
		return this.withReadLock(() -> new TreeMap<>(this.ref));
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentTreeMap} preserving the source's
	 * comparator and sort order.
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentMap<K, V> toUnmodifiable() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeMap<>(this.cloneRef());
	}

}
