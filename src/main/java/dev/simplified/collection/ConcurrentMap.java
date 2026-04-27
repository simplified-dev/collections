package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicMap;
import dev.simplified.collection.query.Searchable;
import dev.simplified.collection.tuple.pair.PairStream;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * A thread-safe {@link Map} extension exposing the project-specific concurrent surface shared
 * by every concurrent map variant in this library.
 *
 * <p>Implementations carry atomic read and write semantics, snapshot-based iteration, and
 * conditional mutation primitives in addition to the standard JDK {@link Map} contract.</p>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface ConcurrentMap<K, V> extends Map<K, V>, Iterable<Map.Entry<K, V>>, Searchable<Map.Entry<K, V>>, Serializable {

	/**
	 * Returns an {@link Optional} containing the value mapped to the specified key, or an empty
	 * {@code Optional} if no mapping exists.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return an {@code Optional} describing the mapped value, or an empty {@code Optional}
	 */
	@NotNull Optional<V> getOptional(Object key);

	/**
	 * Returns {@code true} if this map contains at least one key-value mapping.
	 *
	 * @return {@code true} if this map is not empty
	 */
	boolean notEmpty();

	/**
	 * Returns a parallel {@link PairStream} over the entries of this map.
	 *
	 * @return a parallel stream of this map's key-value pairs
	 */
	@NotNull PairStream<K, V> parallelStream();

	/**
	 * Associates the key from the given entry with its value in this map.
	 *
	 * @param entry the entry containing the key-value pair to put
	 * @return the previous value associated with the key, or {@code null} if there was none
	 */
	V put(@NotNull Entry<K, V> entry);

	/**
	 * Puts the specified key-value pair into this map only if the given supplier returns
	 * {@code true}.
	 *
	 * @param predicate the supplier that determines whether the entry should be added
	 * @param key the key to associate
	 * @param value the value to associate with the key
	 * @return {@code true} if the entry was added
	 */
	boolean putIf(@NotNull Supplier<Boolean> predicate, K key, V value);

	/**
	 * Puts the specified key-value pair into this map only if any existing entry matches the
	 * given bi-predicate.
	 *
	 * @param predicate the bi-predicate tested against existing keys and values
	 * @param key the key to associate
	 * @param value the value to associate with the key
	 * @return {@code true} if the entry was added
	 */
	boolean putIf(@NotNull BiPredicate<? super K, ? super V> predicate, K key, V value);

	/**
	 * Removes all entries from this map for which the given bi-predicate returns {@code true}.
	 *
	 * @param predicate the bi-predicate tested against each entry's key and value
	 * @return {@code true} if any entries were removed
	 */
	boolean removeIf(@NotNull BiPredicate<? super K, ? super V> predicate);

	/**
	 * Removes all entries from this map for which the given entry predicate returns {@code true}.
	 *
	 * @param predicate the predicate tested against each entry
	 * @return {@code true} if any entries were removed
	 */
	boolean removeIf(@NotNull java.util.function.Predicate<? super Entry<K, V>> predicate);

	/**
	 * Removes and returns the value associated with the specified key, or returns the default
	 * value if no mapping exists.
	 *
	 * @param key the key whose mapping is to be removed
	 * @param defaultValue the value to return if no mapping exists for the key
	 * @return the removed value, or {@code defaultValue} if no mapping was found
	 */
	V removeOrGet(Object key, V defaultValue);

	/**
	 * Returns a sequential {@link PairStream} over the entries of this map.
	 *
	 * @return a sequential stream of this map's key-value pairs
	 */
	@NotNull PairStream<K, V> stream();

	/**
	 * Returns an immutable snapshot of this map.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current entries, so subsequent mutations
	 * on this map are not reflected in the snapshot.</p>
	 *
	 * @return an immutable snapshot of this map
	 */
	@NotNull ConcurrentUnmodifiableMap<K, V> toUnmodifiable();

	/**
	 * Wraps {@code backing} as a {@link ConcurrentMap} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to
	 * {@code backing} bypass the read/write lock and may corrupt concurrent reads. Use this for
	 * zero-copy publication of single-threaded build results.
	 *
	 * @param backing the map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent map backed by {@code backing}
	 */
	static <K, V> @NotNull ConcurrentMap<K, V> adopt(@NotNull AbstractMap<K, V> backing) {
		return new Impl<>(backing);
	}

	/**
	 * A thread-safe map backed by a {@link HashMap} with concurrent read and write access via
	 * {@link ReadWriteLock}. Supports snapshot-based iteration over entries, keys, and values.
	 *
	 * @param <K> the type of keys maintained by this map
	 * @param <V> the type of mapped values
	 */
	class Impl<K, V> extends AtomicMap<K, V, AbstractMap<K, V>> implements ConcurrentMap<K, V> {

		/**
		 * Creates a new concurrent map.
		 */
		public Impl() {
			super(new HashMap<>(), (Map<K, V>) null);
		}

		/**
		 * Creates a new concurrent map and fills it with the given pairs.
		 *
		 * @param pairs the entries to include
		 */
		@SafeVarargs
		public Impl(@Nullable Map.Entry<K, V>... pairs) {
			super(new HashMap<>(), pairs);
		}

		/**
		 * Creates a new concurrent map and fills it with the given map.
		 *
		 * @param map the source map to copy from
		 */
		public Impl(@Nullable Map<? extends K, ? extends V> map) {
			super(new HashMap<>(), map);
		}

		/**
		 * Constructs a {@code ConcurrentMap.Impl} that adopts {@code backingMap} as its storage
		 * with a fresh lock. Public callers should go through {@link ConcurrentMap#adopt(AbstractMap)}.
		 *
		 * @param backingMap the backing map to adopt
		 */
		protected Impl(@NotNull AbstractMap<K, V> backingMap) {
			super(backingMap);
		}

		/**
		 * Creates a new concurrent map with the given backing map.
		 *
		 * @param backingMap the backing map implementation
		 * @param map the source map to copy from, or {@code null} for an empty map
		 */
		protected Impl(@NotNull AbstractMap<K, V> backingMap, @Nullable Map<? extends K, ? extends V> map) {
			super(backingMap, map);
		}

		/**
		 * Creates a new concurrent map with the given backing map and fills it with the given
		 * pairs.
		 *
		 * @param backingMap the backing map implementation
		 * @param pairs the entries to include
		 */
		@SafeVarargs
		protected Impl(@NotNull AbstractMap<K, V> backingMap, @Nullable Map.Entry<K, V>... pairs) {
			super(backingMap, pairs);
		}

		/**
		 * Constructs a {@code ConcurrentMap.Impl} with a pre-built backing map and an explicit
		 * lock. Used by {@link ConcurrentUnmodifiableMap.Impl} (and its variants) to install a
		 * snapshot map paired with a no-op lock for wait-free reads.
		 *
		 * @param backingMap the pre-built backing map
		 * @param lock the lock guarding {@code backingMap}
		 */
		protected Impl(@NotNull AbstractMap<K, V> backingMap, @NotNull ReadWriteLock lock) {
			super(backingMap, lock);
		}

		/**
		 * Returns a type-preserving snapshot of this map's backing reference, captured under the
		 * read lock. Subclasses backed by a different concrete {@link AbstractMap} implementation
		 * override this to return an instance of that type so iteration order is preserved on the
		 * snapshot.
		 *
		 * @return a fresh {@link AbstractMap} containing the current entries
		 */
		protected @NotNull AbstractMap<K, V> cloneRef() {
			try {
				this.lock.readLock().lock();
				return new HashMap<>(this.ref);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentMap.Impl}.
		 *
		 * <p>The returned wrapper owns a fresh copy of the current entries - subsequent mutations
		 * on this map are not reflected in the snapshot. Reads on the snapshot are wait-free.
		 * The runtime type is {@link ConcurrentUnmodifiableMap.Impl}; the declared return type is
		 * the mutable parent so subclasses can covariantly override to their own
		 * {@code ConcurrentUnmodifiable*} variant.</p>
		 *
		 * @return an immutable snapshot - runtime type is {@link ConcurrentUnmodifiableMap.Impl}
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableMap<K, V> toUnmodifiable() {
			return new ConcurrentUnmodifiableMap.Impl<>(this.cloneRef());
		}

	}

}
