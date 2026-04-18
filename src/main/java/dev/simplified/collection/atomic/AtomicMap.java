package dev.simplified.collection.atomic;

import dev.simplified.collection.query.Searchable;
import dev.simplified.collection.tuple.pair.PairStream;
import dev.simplified.collection.StreamUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A thread-safe abstract map backed by a {@link ReadWriteLock} for concurrent access.
 * Provides atomic read and write operations on an underlying map of type {@code M}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @param <M> the type of the underlying map
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicMap<K, V, M extends AbstractMap<K, V>> extends AbstractMap<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>>, Searchable<Map.Entry<K, V>>, Serializable {

	protected final @NotNull M ref;
	protected final transient ReadWriteLock lock = new ReentrantReadWriteLock();

	protected AtomicMap(@NotNull M ref, @Nullable Map<? extends K, ? extends V> items) {
		if (Objects.nonNull(items)) ref.putAll(items);
		this.ref = ref;
	}

	protected AtomicMap(@NotNull M ref, @Nullable Map.Entry<? extends K, ? extends V>... items) {
		StreamUtil.ofArrays(items).filter(Objects::nonNull).forEach(entry -> ref.put(entry.getKey(), entry.getValue()));
		this.ref = ref;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		try {
			this.lock.writeLock().lock();
			this.ref.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		try {
			this.lock.writeLock().lock();
			return this.ref.compute(key, remappingFunction);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
		try {
			this.lock.writeLock().lock();
			return this.ref.computeIfAbsent(key, mappingFunction);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		try {
			this.lock.writeLock().lock();
			return this.ref.computeIfPresent(key, remappingFunction);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containsKey(Object key) {
		try {
			this.lock.readLock().lock();
			return this.ref.containsKey(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containsValue(Object value) {
		try {
			this.lock.readLock().lock();
			return this.ref.containsValue(value);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Creates a new empty {@link AtomicSet} suitable for holding map entries.
	 *
	 * @return a new empty set for {@link Entry} instances
	 */
	protected abstract @NotNull AtomicSet<Entry<K, V>, ?> createEmptyEntrySet();

	/**
	 * Creates a new empty {@link AtomicSet} suitable for holding map keys.
	 *
	 * @return a new empty set for keys
	 */
	protected abstract @NotNull AtomicSet<K, ?> createEmptyKeySet();

	/**
	 * Creates a new empty {@link AtomicList} suitable for holding map values.
	 *
	 * @return a new empty list for values
	 */
	protected abstract @NotNull AtomicList<V, ?> createEmptyValueList();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull Set<Entry<K, V>> entrySet() {
		try {
			this.lock.readLock().lock();
			AtomicSet<Entry<K, V>, ?> result = this.createEmptyEntrySet();
			result.ref.addAll(this.ref.entrySet());
			return result;
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof AtomicMap<?, ?, ?>) obj = ((AtomicMap<?, ?, ?>) obj).ref;
		return this.ref.equals(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final V get(Object key) {
		try {
			this.lock.readLock().lock();
			return this.ref.get(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns an {@link Optional} containing the value mapped to the specified key,
	 * or an empty {@code Optional} if no mapping exists.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return an {@code Optional} describing the mapped value, or an empty {@code Optional}
	 */
	public final @NotNull Optional<V> getOptional(Object key) {
		return Optional.ofNullable(this.get(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final V getOrDefault(Object key, V defaultValue) {
		try {
			this.lock.readLock().lock();
			return this.ref.getOrDefault(key, defaultValue);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		try {
			this.lock.readLock().lock();
			return this.ref.hashCode();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isEmpty() {
		try {
			this.lock.readLock().lock();
			return this.ref.isEmpty();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull Iterator<Entry<K, V>> iterator() {
		try {
			this.lock.readLock().lock();
			return new ConcurrentMapIterator(this.ref.entrySet().toArray(), 0);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull Set<K> keySet() {
		try {
			this.lock.readLock().lock();
			AtomicSet<K, ?> result = this.createEmptyKeySet();
			result.ref.addAll(this.ref.keySet());
			return result;
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns {@code true} if this map contains at least one key-value mapping.
	 *
	 * @return {@code true} if this map is not empty
	 */
	public final boolean notEmpty() {
		return !this.isEmpty();
	}

	/**
	 * Returns a parallel {@link PairStream} over the entries of this map.
	 *
	 * @return a parallel stream of this map's key-value pairs
	 */
	public final @NotNull PairStream<K, V> parallelStream() {
		return this.stream().parallel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V put(K key, V value) {
		try {
			this.lock.writeLock().lock();
			return this.ref.put(key, value);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Associates the key from the given entry with its value in this map.
	 *
	 * @param entry the entry containing the key-value pair to put
	 * @return the previous value associated with the key, or {@code null} if there was none
	 */
	public final V put(@NotNull Entry<K, V> entry) {
		return this.put(entry.getKey(), entry.getValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		try {
			this.lock.writeLock().lock();
			this.ref.putAll(map);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Puts the specified key-value pair into this map only if the given supplier returns {@code true}.
	 *
	 * @param predicate the supplier that determines whether the entry should be added
	 * @param key the key to associate
	 * @param value the value to associate with the key
	 * @return {@code true} if the entry was added
	 */
	public boolean putIf(@NotNull Supplier<Boolean> predicate, K key, V value) {
		try {
			this.lock.writeLock().lock();

			if (predicate.get()) {
				this.ref.put(key, value);
				return true;
			}

			return false;
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Puts the specified key-value pair into this map only if any existing entry matches the given bi-predicate.
	 *
	 * @param predicate the bi-predicate tested against existing keys and values
	 * @param key the key to associate
	 * @param value the value to associate with the key
	 * @return {@code true} if the entry was added
	 */
	public boolean putIf(@NotNull BiPredicate<? super K, ? super V> predicate, K key, V value) {
		return this.putIf(
			map -> map.entrySet()
				.stream()
				.anyMatch(e -> predicate.test(
					e.getKey(),
					e.getValue()
				)),
			key,
			value
		);
	}

	/**
	 * Puts the specified key-value pair into this map only if the given predicate,
	 * tested against the underlying map, returns {@code true}.
	 *
	 * @param predicate the predicate to test against the underlying map
	 * @param key the key to associate
	 * @param value the value to associate with the key
	 * @return {@code true} if the entry was added
	 */
	public boolean putIf(@NotNull Predicate<M> predicate, K key, V value) {
		try {
			this.lock.writeLock().lock();

			if (predicate.test(this.ref)) {
				this.ref.put(key, value);
				return true;
			}

			return false;
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V putIfAbsent(K key, V value) {
		try {
			this.lock.writeLock().lock();
			return this.ref.putIfAbsent(key, value);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V remove(Object key) {
		try {
			this.lock.writeLock().lock();
			return this.ref.remove(key);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Removes all entries from this map for which the given bi-predicate returns {@code true}.
	 *
	 * @param predicate the bi-predicate tested against each entry's key and value
	 * @return {@code true} if any entries were removed
	 */
	public boolean removeIf(@NotNull BiPredicate<? super K, ? super V> predicate) {
		return this.removeIf(entry -> predicate.test(entry.getKey(), entry.getValue()));
	}

	/**
	 * Removes all entries from this map for which the given entry predicate returns {@code true}.
	 *
	 * @param predicate the predicate tested against each entry
	 * @return {@code true} if any entries were removed
	 */
	public boolean removeIf(@NotNull Predicate<? super Entry<K, V>> predicate) {
		List<K> toRemove = new ArrayList<>();
		try {
			this.lock.readLock().lock();

			for (Entry<K, V> entry : this.ref.entrySet()) {
				if (predicate.test(entry))
					toRemove.add(entry.getKey());
			}
		} finally {
			this.lock.readLock().unlock();
		}

		if (toRemove.isEmpty())
			return false;

		try {
			this.lock.writeLock().lock();
			toRemove.forEach(this.ref::remove);
			return true;
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Removes and returns the value associated with the specified key,
	 * or returns the default value if no mapping exists.
	 *
	 * @param key the key whose mapping is to be removed
	 * @param defaultValue the value to return if no mapping exists for the key
	 * @return the removed value, or {@code defaultValue} if no mapping was found
	 */
	public final V removeOrGet(Object key, V defaultValue) {
		return Optional.ofNullable(this.remove(key)).orElse(defaultValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object key, Object value) {
		try {
			this.lock.writeLock().lock();
			return this.ref.remove(key, value);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size() {
		try {
			this.lock.readLock().lock();
			return this.ref.size();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns a sequential {@link PairStream} over the entries of this map.
	 *
	 * @return a sequential stream of this map's key-value pairs
	 */
	public final @NotNull PairStream<K, V> stream() {
		return PairStream.of(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull Collection<V> values() {
		try {
			this.lock.readLock().lock();
			AtomicList<V, ?> result = this.createEmptyValueList();
			result.ref.addAll(this.ref.values());
			return result;
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * A concurrent iterator over map entries backed by a snapshot of the entry set.
	 */
	protected class ConcurrentMapIterator extends AtomicIterator<Entry<K, V>> {

		protected ConcurrentMapIterator(Object[] snapshot, int index) {
			super(snapshot, index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			if (this.last < 0)
				throw new IllegalStateException();

			AtomicMap.this.remove(this.snapshot[this.last]);
			this.snapshot = AtomicMap.this.entrySet().toArray();
			this.cursor = this.last;
			this.last = -1;
		}

	}

}
