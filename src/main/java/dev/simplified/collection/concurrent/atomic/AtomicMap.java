package dev.sbs.api.collection.concurrent.atomic;

import dev.sbs.api.collection.concurrent.iterator.ConcurrentIterator;
import dev.sbs.api.collection.search.Searchable;
import dev.sbs.api.collection.stream.PairStream;
import dev.sbs.api.collection.stream.StreamUtil;
import dev.sbs.api.mutable.primitive.MutableBoolean;
import dev.sbs.api.util.NumberUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AtomicMap<K, V, M extends AbstractMap<K, V>> extends AbstractMap<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>>, Searchable<Map.Entry<K, V>>, Serializable {

	protected final @NotNull M ref;
	protected final transient ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * Create a new concurrent map.
	 */
	protected AtomicMap(@NotNull M ref, @Nullable Map<? extends K, ? extends V> items) {
		if (Objects.nonNull(items)) ref.putAll(items);
		this.ref = ref;
	}

	/**
	 * Create a new concurrent map.
	 */
	protected AtomicMap(@NotNull M ref, @Nullable Map.Entry<? extends K, ? extends V>... items) {
		StreamUtil.ofArrays(items).filter(Objects::nonNull).forEach(entry -> ref.put(entry.getKey(), entry.getValue()));
		this.ref = ref;
	}

	@Override
	public void clear() {
		try {
			this.lock.writeLock().lock();
			this.ref.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public final boolean containsKey(Object key) {
		try {
			this.lock.readLock().lock();
			return this.ref.containsKey(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public final boolean containsValue(Object value) {
		try {
			this.lock.readLock().lock();
			return this.ref.containsValue(value);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public @NotNull Set<Entry<K, V>> entrySet() {
		return this.ref.entrySet();
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof AtomicMap<?, ?, ?>) obj = ((AtomicMap<?, ?, ?>) obj).ref;
		return this.ref.equals(obj);
	}

	@Override
	public final V get(Object key) {
		try {
			this.lock.readLock().lock();
			return this.ref.get(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	public final @NotNull Optional<V> getOptional(Object key) {
		return Optional.ofNullable(this.get(key));
	}

	@Override
	public final V getOrDefault(Object key, V defaultValue) {
		try {
			this.lock.readLock().lock();
			return this.ref.getOrDefault(key, defaultValue);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public final int hashCode() {
		try {
			this.lock.readLock().lock();
			return this.ref.hashCode();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public final boolean isEmpty() {
		return this.size() == 0;
	}

	@Override
	public final @NotNull Iterator<Entry<K, V>> iterator() {
		return new ConcurrentMapIterator(this.entrySet().toArray(), 0);
	}

	@Override
	public @NotNull Set<K> keySet() {
		return this.ref.keySet();
	}

	public final boolean notEmpty() {
		return !this.isEmpty();
	}

	public final @NotNull Stream<Entry<K, V>> parallelStream() {
		return this.entrySet().parallelStream();
	}

	@Override
	public V put(K key, V value) {
		try {
			this.lock.writeLock().lock();
			return this.ref.put(key, value);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public final V put(@NotNull Entry<K, V> entry) {
		return this.put(entry.getKey(), entry.getValue());
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		try {
			this.lock.writeLock().lock();
			this.ref.putAll(map);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public V putIfAbsent(K key, V value) {
		try {
			this.lock.writeLock().lock();
			return this.ref.putIfAbsent(key, value);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public V remove(Object key) {
		try {
			this.lock.readLock().lock();
			return this.ref.remove(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	public boolean removeIf(@NotNull BiPredicate<? super K, ? super V> predicate) {
		return this.removeIf(entry -> predicate.test(entry.getKey(), entry.getValue()));
	}

	public boolean removeIf(@NotNull Predicate<? super Entry<K, V>> predicate) {
		MutableBoolean removed = new MutableBoolean(false);

		try {
			this.lock.readLock().lock();

			for (Entry<K, V> entry : this) {
				if (predicate.test(entry))
					this.remove(entry.getKey());
			}

			return removed.get();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	public final V removeOrGet(Object key, V defaultValue) {
		return Optional.ofNullable(this.remove(key)).orElse(defaultValue);
	}

	@Override
	public boolean remove(Object key, Object value) {
		try {
			this.lock.readLock().lock();
			return this.ref.remove(key, value);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public final int size() {
		return this.ref.size();
	}

	public final @NotNull PairStream<K, V> stream() {
		return PairStream.of(this);
	}

	@Override
	public @NotNull Collection<V> values() {
		return this.ref.values();
	}

	protected class ConcurrentMapIterator extends ConcurrentIterator<Entry<K, V>> {

		protected ConcurrentMapIterator(Object[] snapshot, int index) {
			super(snapshot, index);
		}

		@Override
		public void remove() {
			AtomicMap.this.remove(this.snapshot[this.cursor]);
			this.snapshot = AtomicMap.this.entrySet().toArray();
			this.cursor = NumberUtil.ensureRange(this.cursor, 0, this.snapshot.length - 1);
		}

	}

}
