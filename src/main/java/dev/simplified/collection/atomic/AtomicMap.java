package dev.simplified.collection.atomic;

import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.tuple.pair.PairStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.Lock;
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
public abstract class AtomicMap<K, V, M extends AbstractMap<K, V>> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

	protected final @NotNull M ref;
	protected final @NotNull ReadWriteLock lock;
	private final @NotNull Lock readLockView;
	private final @NotNull Lock writeLockView;
	private final @NotNull Object viewLock = new Object();

	/** Lazily initialized live view of the entry set. */
	private transient volatile @Nullable Set<Entry<K, V>> entrySetView;
	/** Lazily initialized live view of the key set. */
	private transient volatile @Nullable Set<K> keySetView;
	/** Lazily initialized live view of the values collection. */
	private transient volatile @Nullable Collection<V> valuesView;

	/** Cached iterator snapshot for the entry set view. */
	private transient volatile @Nullable Object @Nullable [] entrySetSnapshot;
	/** Cached iterator snapshot for the key set view. */
	private transient volatile @Nullable Object @Nullable [] keySetSnapshot;
	/** Cached iterator snapshot for the values collection view. */
	private transient volatile @Nullable Object @Nullable [] valuesSnapshot;

	/**
	 * Constructs an {@code AtomicMap} that adopts {@code ref} as its backing storage with a
	 * fresh {@link ReentrantReadWriteLock}. No copy is made; the caller relinquishes exclusive
	 * ownership of {@code ref}.
	 *
	 * @param ref the backing map to adopt
	 */
	protected AtomicMap(@NotNull M ref) {
		this(ref, new ReentrantReadWriteLock());
	}

	protected AtomicMap(@NotNull M ref, @Nullable Map<? extends K, ? extends V> items) {
		this(ref);
		if (items != null) ref.putAll(items);
	}

	protected AtomicMap(@NotNull M ref, @Nullable Map.Entry<? extends K, ? extends V>... items) {
		this(ref);
		if (items != null) {
			for (Map.Entry<? extends K, ? extends V> e : items)
				if (e != null) ref.put(e.getKey(), e.getValue());
		}
	}

	/**
	 * Constructs an {@code AtomicMap} with an explicit lock, typically a no-op lock paired
	 * with a snapshot {@code ref} for wait-free reads in {@code ConcurrentUnmodifiable*}
	 * wrappers.
	 *
	 * @param ref the underlying map
	 * @param lock the lock guarding {@code ref}
	 */
	protected AtomicMap(@NotNull M ref, @NotNull ReadWriteLock lock) {
		this.ref = ref;
		this.lock = lock;
		this.readLockView = lock.readLock();
		this.writeLockView = lock.writeLock();
	}

	/**
	 * Invalidates all cached view iteration snapshots. Must be called from every write path
	 * while still holding the write lock so the nullify is ordered before the unlock.
	 */
	protected void invalidateViewSnapshots() {
		if (this.entrySetSnapshot != null) this.entrySetSnapshot = null;
		if (this.keySetSnapshot != null) this.keySetSnapshot = null;
		if (this.valuesSnapshot != null) this.valuesSnapshot = null;
		this.onSnapshotInvalidated();
	}

	/**
	 * Hook invoked from {@link #invalidateViewSnapshots()} after the built-in view caches are
	 * cleared. Subclasses may override to invalidate additional cached views.
	 */
	protected void onSnapshotInvalidated() {}

	/**
	 * Executes the given action with the read lock held and returns its result.
	 *
	 * @param action the action to execute under the read lock
	 * @param <R> the result type
	 * @return the value returned by {@code action}
	 */
	protected final <R> R withReadLock(@NotNull Supplier<R> action) {
		this.readLockView.lock();

		try {
			return action.get();
		} finally {
			this.readLockView.unlock();
		}
	}

	/**
	 * Executes the given action with the read lock held.
	 *
	 * @param action the action to execute under the read lock
	 */
	protected final void withReadLock(@NotNull Runnable action) {
		this.readLockView.lock();

		try {
			action.run();
		} finally {
			this.readLockView.unlock();
		}
	}

	/**
	 * Executes the given action with the write lock held and returns its result. Invalidates the
	 * view-iteration snapshots in the {@code finally} block before releasing the lock.
	 *
	 * @param action the action to execute under the write lock
	 * @param <R> the result type
	 * @return the value returned by {@code action}
	 */
	protected final <R> R withWriteLock(@NotNull Supplier<R> action) {
		this.writeLockView.lock();

		try {
			return action.get();
		} finally {
			this.invalidateViewSnapshots();
			this.writeLockView.unlock();
		}
	}

	/**
	 * Executes the given action with the write lock held. Invalidates the view-iteration
	 * snapshots in the {@code finally} block before releasing the lock.
	 *
	 * @param action the action to execute under the write lock
	 */
	protected final void withWriteLock(@NotNull Runnable action) {
		this.writeLockView.lock();

		try {
			action.run();
		} finally {
			this.invalidateViewSnapshots();
			this.writeLockView.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		this.withWriteLock(this.ref::clear);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.withWriteLock(() -> this.ref.compute(key, remappingFunction));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
		return this.withWriteLock(() -> this.ref.computeIfAbsent(key, mappingFunction));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.withWriteLock(() -> this.ref.computeIfPresent(key, remappingFunction));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containsKey(Object key) {
		return this.withReadLock(() -> this.ref.containsKey(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containsValue(Object value) {
		return this.withReadLock(() -> this.ref.containsValue(value));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns a lazily initialized live view of the entry set. Structural reads
	 * ({@code size}, {@code contains}, {@code isEmpty}) reflect the current map state.
	 * Structural writes ({@code remove}, {@code clear}, {@link Iterator#remove()},
	 * {@link Entry#setValue(Object)}) propagate to the map under the write lock.
	 * Iteration uses a read-locked snapshot cached until the next write, so consumers
	 * never observe a partially modified map and never throw {@link java.util.ConcurrentModificationException}.
	 */
	@Override
	public @NotNull Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> view = this.entrySetView;

		if (view == null) {
			synchronized (this.viewLock) {
				view = this.entrySetView;

				if (view == null) {
					view = new EntrySetView();
					this.entrySetView = view;
				}
			}
		}

		return view;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof AtomicMap<?, ?, ?>) obj = ((AtomicMap<?, ?, ?>) obj).ref;

		final Object target = obj;
		return this.withReadLock(() -> this.ref.equals(target));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final V get(Object key) {
		return this.withReadLock(() -> this.ref.get(key));
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
		return this.withReadLock(() -> this.ref.getOrDefault(key, defaultValue));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return this.withReadLock(this.ref::hashCode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isEmpty() {
		return this.withReadLock(this.ref::isEmpty);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Equivalent to {@code entrySet().iterator()}, so the two paths share the same
	 * cached iteration snapshot.
	 */
	@Override
	public final @NotNull Iterator<Entry<K, V>> iterator() {
		return this.entrySet().iterator();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns a lazily initialized live view of the key set.
	 */
	@Override
	public @NotNull Set<K> keySet() {
		Set<K> view = this.keySetView;

		if (view == null) {
			synchronized (this.viewLock) {
				view = this.keySetView;

				if (view == null) {
					view = new KeySetView();
					this.keySetView = view;
				}
			}
		}

		return view;
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
		return this.withWriteLock(() -> this.ref.put(key, value));
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
		this.withWriteLock(() -> this.ref.putAll(map));
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
		return this.withWriteLock(() -> {
			if (predicate.get()) {
				this.ref.put(key, value);
				return true;
			}

			return false;
		});
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
		return this.withWriteLock(() -> {
			if (predicate.test(this.ref)) {
				this.ref.put(key, value);
				return true;
			}

			return false;
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V putIfAbsent(K key, V value) {
		return this.withWriteLock(() -> this.ref.putIfAbsent(key, value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V remove(Object key) {
		return this.withWriteLock(() -> this.ref.remove(key));
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
		return this.withWriteLock(() -> this.ref.entrySet().removeIf(predicate));
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
		return this.withWriteLock(() -> this.ref.remove(key, value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size() {
		return this.withReadLock(this.ref::size);
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
	 * <p>
	 * Returns a lazily initialized live view of the values collection.
	 */
	@Override
	public @NotNull Collection<V> values() {
		Collection<V> view = this.valuesView;

		if (view == null) {
			synchronized (this.viewLock) {
				view = this.valuesView;

				if (view == null) {
					view = new ValuesView();
					this.valuesView = view;
				}
			}
		}

		return view;
	}

	/**
	 * Loads the cached entry-set snapshot, populating it under the read lock on first call
	 * after any write. Snapshot elements are {@link SnapshotEntry} instances holding the
	 * key and value captured at snapshot time.
	 */
	private Object[] entrySetSnapshot() {
		Object[] snapshot = this.entrySetSnapshot;

		if (snapshot == null) {
			snapshot = this.withReadLock(() -> {
				Object[] cached = this.entrySetSnapshot;

				if (cached == null) {
					Set<Entry<K, V>> source = this.ref.entrySet();
					Object[] built = new Object[source.size()];
					int i = 0;

					for (Entry<K, V> entry : source)
						built[i++] = new SnapshotEntry<>(entry.getKey(), entry.getValue());

					cached = built;
					this.entrySetSnapshot = cached;
				}

				return cached;
			});
		}

		return snapshot;
	}

	/**
	 * Loads the cached key-set snapshot, populating it under the read lock on first call
	 * after any write.
	 */
	private Object[] keySetSnapshot() {
		Object[] snapshot = this.keySetSnapshot;

		if (snapshot == null) {
			snapshot = this.withReadLock(() -> {
				Object[] cached = this.keySetSnapshot;

				if (cached == null) {
					cached = this.ref.keySet().toArray();
					this.keySetSnapshot = cached;
				}

				return cached;
			});
		}

		return snapshot;
	}

	/**
	 * Loads the cached values snapshot, populating it under the read lock on first call
	 * after any write.
	 */
	private Object[] valuesSnapshot() {
		Object[] snapshot = this.valuesSnapshot;

		if (snapshot == null) {
			snapshot = this.withReadLock(() -> {
				Object[] cached = this.valuesSnapshot;

				if (cached == null) {
					cached = this.ref.values().toArray();
					this.valuesSnapshot = cached;
				}

				return cached;
			});
		}

		return snapshot;
	}

	/**
	 * An immutable (key, value) pair captured at snapshot time. Shared across iterators.
	 * Throws {@link UnsupportedOperationException} on {@link #setValue(Object)} - callers
	 * receive a {@link LiveEntry} wrapper from the entry-set iterator instead, which
	 * provides a write-locked {@code setValue}.
	 */
	private static final class SnapshotEntry<K, V> implements Entry<K, V> {

		private final K key;
		private final V value;
		private int hash;

		SnapshotEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Entry<?, ?> e)) return false;
			return Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue());
		}

		@Override
		public int hashCode() {
			int h = this.hash;
			if (h == 0) {
				h = (this.key == null ? 0 : this.key.hashCode())
					^ (this.value == null ? 0 : this.value.hashCode());
				this.hash = h;
			}
			return h;
		}

		@Override
		public String toString() {
			return this.key + "=" + this.value;
		}

	}

	/**
	 * A per-iterator mutable {@link Entry} wrapper. {@link #setValue(Object)} propagates
	 * to the backing map via {@link AtomicMap#put(Object, Object)} and updates the
	 * locally cached value. Each call to {@code iterator.next()} returns a fresh instance,
	 * so concurrent {@code setValue} calls on different entries never race.
	 */
	private final class LiveEntry implements Entry<K, V> {

		private final K key;
		private V value;

		LiveEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(V newValue) {
			V old = AtomicMap.this.put(this.key, newValue);
			this.value = newValue;
			return old;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Entry<?, ?> e)) return false;
			return Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue());
		}

		@Override
		public int hashCode() {
			return (this.key == null ? 0 : this.key.hashCode())
				^ (this.value == null ? 0 : this.value.hashCode());
		}

		@Override
		public String toString() {
			return this.key + "=" + this.value;
		}

	}

	/**
	 * Live view over the map's entry set. All reads delegate to the backing map under
	 * the read lock; mutations delegate under the write lock and invalidate all three
	 * view snapshots. Iteration is snapshot-based.
	 */
	private final class EntrySetView extends AbstractSet<Entry<K, V>> {

		@Override
		public int size() {
			return AtomicMap.this.size();
		}

		@Override
		public boolean isEmpty() {
			return AtomicMap.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Entry<?, ?>))
				return false;

			return AtomicMap.this.withReadLock(() -> AtomicMap.this.ref.entrySet().contains(o));
		}

		@Override
		public boolean remove(Object o) {
			if (!(o instanceof Entry<?, ?> entry))
				return false;

			return AtomicMap.this.remove(entry.getKey(), entry.getValue());
		}

		@Override
		public void clear() {
			AtomicMap.this.clear();
		}

		@Override
		public @NotNull Iterator<Entry<K, V>> iterator() {
			return new EntrySetIterator(AtomicMap.this.entrySetSnapshot());
		}

		@Override
		public @NotNull Spliterator<Entry<K, V>> spliterator() {
			return Spliterators.spliterator(AtomicMap.this.entrySetSnapshot(),
				Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT);
		}

	}

	/**
	 * Live view over the map's key set.
	 */
	private final class KeySetView extends AbstractSet<K> {

		@Override
		public int size() {
			return AtomicMap.this.size();
		}

		@Override
		public boolean isEmpty() {
			return AtomicMap.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return AtomicMap.this.containsKey(o);
		}

		@Override
		public boolean remove(Object o) {
			return AtomicMap.this.withWriteLock(() -> {
				if (!AtomicMap.this.ref.containsKey(o))
					return false;

				AtomicMap.this.remove(o);
				return true;
			});
		}

		@Override
		public void clear() {
			AtomicMap.this.clear();
		}

		@Override
		public @NotNull Iterator<K> iterator() {
			return new KeySetIterator(AtomicMap.this.keySetSnapshot());
		}

		@Override
		public @NotNull Spliterator<K> spliterator() {
			return Spliterators.spliterator(AtomicMap.this.keySetSnapshot(),
				Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT);
		}

	}

	/**
	 * Live view over the map's values collection. {@link #remove(Object)} removes the first
	 * entry whose value equals {@code o} (matching the JDK contract), routed through
	 * {@link AtomicMap#remove(Object)} so {@code Unmodifiable} subclasses only need to
	 * override that one public method to reject all mutations.
	 */
	private final class ValuesView extends AbstractCollection<V> {

		@Override
		public int size() {
			return AtomicMap.this.size();
		}

		@Override
		public boolean isEmpty() {
			return AtomicMap.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return AtomicMap.this.containsValue(o);
		}

		@Override
		public boolean remove(Object o) {
			return AtomicMap.this.withWriteLock(() -> {
				for (Entry<K, V> entry : AtomicMap.this.ref.entrySet()) {
					if (Objects.equals(entry.getValue(), o)) {
						AtomicMap.this.remove(entry.getKey());
						return true;
					}
				}

				return false;
			});
		}

		@Override
		public void clear() {
			AtomicMap.this.clear();
		}

		@Override
		public @NotNull Iterator<V> iterator() {
			return new ValuesIterator(AtomicMap.this.valuesSnapshot());
		}

		@Override
		public @NotNull Spliterator<V> spliterator() {
			return Spliterators.spliterator(AtomicMap.this.valuesSnapshot(),
				Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE);
		}

	}

	/**
	 * Snapshot-backed iterator over the entry set view. {@link #next()} wraps each
	 * snapshot entry in a fresh {@link LiveEntry} so {@code setValue} propagates to the
	 * map; {@link #remove()} removes the current entry from the map by key.
	 */
	private final class EntrySetIterator extends AtomicIterator<Entry<K, V>> {

		EntrySetIterator(Object[] snapshot) {
			super(snapshot, 0);
		}

		@Override
		@SuppressWarnings("unchecked")
		public @NotNull Entry<K, V> next() {
			if (!this.hasNext())
				throw new NoSuchElementException();

			SnapshotEntry<K, V> snap = (SnapshotEntry<K, V>) this.snapshot[this.last = this.cursor++];
			return new LiveEntry(snap.getKey(), snap.getValue());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * If the entry was concurrently removed before this call, the operation is a
		 * silent no-op - no {@link java.util.ConcurrentModificationException} is thrown.
		 */
		@Override
		@SuppressWarnings("unchecked")
		public void remove() {
			if (this.last < 0)
				throw new IllegalStateException();

			SnapshotEntry<K, V> snap = (SnapshotEntry<K, V>) this.snapshot[this.last];
			AtomicMap.this.remove(snap.getKey());
			this.last = -1;
		}

	}

	/**
	 * Snapshot-backed iterator over the key set view.
	 */
	private final class KeySetIterator extends AtomicIterator<K> {

		KeySetIterator(Object[] snapshot) {
			super(snapshot, 0);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * If the key was concurrently removed before this call, the operation is a
		 * silent no-op - no {@link java.util.ConcurrentModificationException} is thrown.
		 */
		@Override
		public void remove() {
			if (this.last < 0)
				throw new IllegalStateException();

			AtomicMap.this.remove(this.snapshot[this.last]);
			this.last = -1;
		}

	}

	/**
	 * Snapshot-backed iterator over the values view. {@link #remove()} removes the first
	 * entry whose value equals the just-returned element, routed through
	 * {@link AtomicMap#remove(Object)} so {@code Unmodifiable} subclasses only need to
	 * override that one public method to reject all mutations.
	 */
	private final class ValuesIterator extends AtomicIterator<V> {

		ValuesIterator(Object[] snapshot) {
			super(snapshot, 0);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * If no entry still maps to this value by the time the call runs, the operation
		 * is a silent no-op - no {@link java.util.ConcurrentModificationException} is thrown.
		 */
		@Override
		public void remove() {
			if (this.last < 0)
				throw new IllegalStateException();

			Object value = this.snapshot[this.last];

			AtomicMap.this.withWriteLock(() -> {
				for (Entry<K, V> entry : AtomicMap.this.ref.entrySet()) {
					if (Objects.equals(entry.getValue(), value)) {
						AtomicMap.this.remove(entry.getKey());
						break;
					}
				}
			});

			this.last = -1;
		}

	}

}
