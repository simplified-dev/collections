package dev.simplified.collection.atomic;

import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.tuple.pair.PairStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
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

	/**
	 * The backing map. Private rather than protected, and reachable only as the argument the
	 * {@code withReadLock} / {@code withWriteLock} helpers hand to an action, so a subclass cannot
	 * name it outside a lock at all - which is the only way this class can promise that what it
	 * guards stays guarded.
	 */
	private final @NotNull M ref;
	protected final @NotNull ReadWriteLock lock;

	/**
	 * Monitor guarding creation of the three lazy views.
	 * <p>
	 * An empty array rather than a bare {@link Object}, because a monitor has to be {@code final} to
	 * be a monitor at all and a plain object is not serializable - so a transient one would have to
	 * be reassigned after deserialization, which is the thing {@code final} forbids. An empty array
	 * costs the same, serializes, and is distinct per instance.
	 */
	private final @NotNull Object @NotNull [] viewLock = new Object[0];

	/**
	 * Lazily initialized live view of the entry set.
	 */
	private transient volatile @Nullable Set<Entry<K, V>> entrySetView;
	/**
	 * Lazily initialized live view of the key set.
	 */
	private transient volatile @Nullable Set<K> keySetView;
	/**
	 * Lazily initialized live view of the values collection.
	 */
	private transient volatile @Nullable Collection<V> valuesView;

	/**
	 * Cached iterator snapshot for the entry set view.
	 */
	private transient volatile @Nullable Object @Nullable [] entrySetSnapshot;
	/**
	 * Cached iterator snapshot for the key set view.
	 */
	private transient volatile @Nullable Object @Nullable [] keySetSnapshot;
	/**
	 * Cached iterator snapshot for the values collection view.
	 */
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
	 * Hook invoked before every write, from the two {@code withWriteLock} helpers that every
	 * mutator on this map, its subclasses and its views funnels through. Default is a no-op;
	 * {@code ConcurrentUnmodifiable*} subclasses override it to throw
	 * {@link UnsupportedOperationException}, which is the whole of how they refuse to be modified.
	 *
	 * <p>One hook rather than an override per mutator: a mutator that is missed cannot be refused,
	 * and this is the one place every write already passes through - so nothing can be missed, and
	 * a mutator added later is refused without anyone remembering to say so. It rejects before the
	 * lock is acquired, so a refused call costs no lock at all.
	 */
	protected void checkModificationAllowed() {}

	/**
	 * Returns the characteristic bits the {@link #entrySet()} spliterator advertises. Subclasses
	 * with insertion-ordered backings (e.g. {@link LinkedHashMap}) override to OR in
	 * {@link Spliterator#ORDERED}; navigable backings OR in {@link Spliterator#SORTED}.
	 *
	 * @return the entry-set spliterator characteristic bitmask
	 */
	protected int entrySetSpliteratorCharacteristics() {
		return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT;
	}

	/**
	 * Returns the characteristic bits the {@link #keySet()} spliterator advertises. Subclasses
	 * with insertion-ordered backings override to OR in {@link Spliterator#ORDERED}; navigable
	 * backings OR in {@link Spliterator#SORTED}.
	 *
	 * @return the key-set spliterator characteristic bitmask
	 */
	protected int keySetSpliteratorCharacteristics() {
		return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT;
	}

	/**
	 * Returns the characteristic bits the {@link #values()} spliterator advertises. Subclasses
	 * with insertion-ordered backings override to OR in {@link Spliterator#ORDERED}.
	 *
	 * @return the values-collection spliterator characteristic bitmask
	 */
	protected int valuesSpliteratorCharacteristics() {
		return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE;
	}

	/**
	 * Runs the given action with the read lock held and answers what it returns.
	 * <p>
	 * The lock helpers carry two names. {@code withXLock} is for an action that answers something
	 * and {@code execXLock} for one that answers nothing; either takes the backing map as
	 * its argument, or takes none at all when the action does not need it - a lock-guarded sub-view
	 * reads through itself rather than through what this class holds. The split falls there because
	 * two one-argument shapes cannot share one name: a lambda whose parameter type is inferred is
	 * not read for its body when the compiler chooses between overloads, so {@link Function} and
	 * {@link Consumer} would be ambiguous at every call site.
	 *
	 * @param action the action to run under the read lock
	 * @param <R> the result type
	 * @return the value {@code action} returns
	 */
	protected final <R> R withReadLock(@NotNull Supplier<R> action) {
		this.lock.readLock().lock();

		try {
			return action.get();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Runs the given action over the backing map with the read lock held and answers what it returns.
	 *
	 * @param action the action to run over the backing map under the read lock
	 * @param <R> the result type
	 * @return the value {@code action} returns
	 */
	protected final <R> R withReadLock(@NotNull Function<M, R> action) {
		this.lock.readLock().lock();

		try {
			return action.apply(this.ref);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Runs the given action with the read lock held.
	 *
	 * @param action the action to run under the read lock
	 */
	protected final void execReadLock(@NotNull Runnable action) {
		this.lock.readLock().lock();

		try {
			action.run();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Runs the given action over the backing map with the read lock held.
	 *
	 * @param action the action to run over the backing map under the read lock
	 */
	protected final void execReadLock(@NotNull Consumer<M> action) {
		this.lock.readLock().lock();

		try {
			action.accept(this.ref);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Runs the given action with the write lock held and answers what it returns.
	 * <p>
	 * Asks {@link #checkModificationAllowed()} first and drops the cached view-iteration snapshots
	 * before releasing the lock.
	 *
	 * @param action the action to run under the write lock
	 * @param <R> the result type
	 * @return the value {@code action} returns
	 * @throws UnsupportedOperationException if this map rejects modification
	 */
	protected final <R> R withWriteLock(@NotNull Supplier<R> action) {
		this.checkModificationAllowed();
		this.lock.writeLock().lock();

		try {
			return action.get();
		} finally {
			this.invalidateViewSnapshots();
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Runs the given action over the backing map with the write lock held and answers what it returns.
	 * <p>
	 * Asks {@link #checkModificationAllowed()} first and drops the cached view-iteration snapshots
	 * before releasing the lock.
	 *
	 * @param action the action to run over the backing map under the write lock
	 * @param <R> the result type
	 * @return the value {@code action} returns
	 * @throws UnsupportedOperationException if this map rejects modification
	 */
	protected final <R> R withWriteLock(@NotNull Function<M, R> action) {
		this.checkModificationAllowed();
		this.lock.writeLock().lock();

		try {
			return action.apply(this.ref);
		} finally {
			this.invalidateViewSnapshots();
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Runs the given action with the write lock held.
	 * <p>
	 * Asks {@link #checkModificationAllowed()} first and drops the cached view-iteration snapshots
	 * before releasing the lock.
	 *
	 * @param action the action to run under the write lock
	 * @throws UnsupportedOperationException if this map rejects modification
	 */
	protected final void execWriteLock(@NotNull Runnable action) {
		this.checkModificationAllowed();
		this.lock.writeLock().lock();

		try {
			action.run();
		} finally {
			this.invalidateViewSnapshots();
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Runs the given action over the backing map with the write lock held.
	 * <p>
	 * Asks {@link #checkModificationAllowed()} first and drops the cached view-iteration snapshots
	 * before releasing the lock.
	 *
	 * @param action the action to run over the backing map under the write lock
	 * @throws UnsupportedOperationException if this map rejects modification
	 */
	protected final void execWriteLock(@NotNull Consumer<M> action) {
		this.checkModificationAllowed();
		this.lock.writeLock().lock();

		try {
			action.accept(this.ref);
		} finally {
			this.invalidateViewSnapshots();
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		this.execWriteLock(AbstractMap::clear);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.withWriteLock(backing -> backing.compute(key, remappingFunction));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
		return this.withWriteLock(backing -> backing.computeIfAbsent(key, mappingFunction));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.withWriteLock(backing -> backing.computeIfPresent(key, remappingFunction));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containsKey(Object key) {
		return this.withReadLock(backing -> backing.containsKey(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containsValue(Object value) {
		return this.withReadLock(backing -> backing.containsValue(value));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns a lazily initialized live view of the entry set. Structural reads
	 * ({@code size}, {@code contains}, {@code isEmpty}) reflect the current map state.
	 * Structural writes ({@code remove}, {@code clear}, {@link Iterator#remove()},
	 * {@link Entry#setValue(Object)}) propagate to the map under the write lock.
	 * Iteration uses a read-locked snapshot cached until the next write, so consumers
	 * never observe a partially modified map and never throw {@link ConcurrentModificationException}.
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
	 * Returns a plain JDK copy of this map's contents, captured under this map's own read lock so
	 * the caller can walk the result without holding any lock at all.
	 * <p>
	 * A sorted backing is copied into a {@link TreeMap} carrying the same {@link Comparator}:
	 * map equality probes the copy with {@link Map#get(Object)}, so flattening a
	 * comparator-ordered map into a hash-ordered one would answer those probes under the wrong
	 * key semantics. Subclasses whose backing type defines equality over a different shape must
	 * override this so the copy is of that shape.
	 *
	 * @return an unshared copy of this map's current contents
	 */
	protected @NotNull Object comparisonSnapshot() {
		return this.withReadLock(backing -> {
			// A map is parameterized once, so a backing map that is also sorted is sorted over the
			// same two types - which the compiler cannot see from the bound alone.
			if (backing instanceof SortedMap) {
				@SuppressWarnings("unchecked")
				SortedMap<K, V> sorted = (SortedMap<K, V>) backing;
				TreeMap<K, V> copy = new TreeMap<>(sorted.comparator());
				copy.putAll(backing);
				return copy;
			}

			return new LinkedHashMap<K, V>(backing);
		});
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * A foreign {@code AtomicMap} is replaced by its own {@link #comparisonSnapshot()} before this
	 * map's read lock is taken, so exactly one lock is ever held. Probing the foreign backing map
	 * directly would read it while holding only this side's lock, which excludes nothing on that
	 * side and lets a concurrent mutation there yield a wrong answer.
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;

		if (obj instanceof AtomicMap<?, ?, ?> other) {
			if (this.ref == other.ref) return true;
			obj = other.comparisonSnapshot();
		}

		final Object target = obj;
		return this.withReadLock(backing -> backing.equals(target));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final V get(Object key) {
		return this.withReadLock(backing -> backing.get(key));
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
		return this.withReadLock(backing -> backing.getOrDefault(key, defaultValue));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return this.withReadLock(AbstractMap::hashCode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isEmpty() {
		return this.withReadLock(AbstractMap::isEmpty);
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
		return this.withWriteLock(backing -> backing.put(key, value));
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
		this.execWriteLock(backing -> backing.putAll(map));
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
		return this.withWriteLock(backing -> {
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
		return this.withWriteLock(backing -> {
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
		return this.withWriteLock(backing -> backing.putIfAbsent(key, value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable V remove(Object key) {
		return this.withWriteLock(backing -> backing.remove(key));
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
		return this.withWriteLock(backing -> backing.entrySet().removeIf(predicate));
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
		return this.withWriteLock(backing -> backing.remove(key, value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size() {
		return this.withReadLock(AbstractMap::size);
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
			snapshot = this.withReadLock(backing -> {
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
			snapshot = this.withReadLock(backing -> {
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
			snapshot = this.withReadLock(backing -> {
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

			return AtomicMap.this.withReadLock(backing -> backing.entrySet().contains(o));
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
				AtomicMap.this.entrySetSpliteratorCharacteristics());
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
		@SuppressWarnings("SuspiciousMethodCalls")
		public boolean remove(Object o) {
			// Set.remove takes any Object by contract, so asking the backing map about one is the
			// question this method exists to answer rather than a mistyped key.
			return AtomicMap.this.withWriteLock(backing -> {
				if (!backing.containsKey(o))
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
				AtomicMap.this.keySetSpliteratorCharacteristics());
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
			return AtomicMap.this.withWriteLock(backing -> {
				Iterator<Entry<K, V>> it = AtomicMap.this.ref.entrySet().iterator();
				while (it.hasNext()) {
					if (Objects.equals(it.next().getValue(), o)) {
						it.remove();
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
				AtomicMap.this.valuesSpliteratorCharacteristics());
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
		 * silent no-op - no {@link ConcurrentModificationException} is thrown.
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
		 * silent no-op - no {@link ConcurrentModificationException} is thrown.
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
		 * is a silent no-op - no {@link ConcurrentModificationException} is thrown.
		 */
		@Override
		public void remove() {
			if (this.last < 0)
				throw new IllegalStateException();

			Object value = this.snapshot[this.last];

			AtomicMap.this.execWriteLock(backing -> {
				Iterator<Entry<K, V>> it = backing.entrySet().iterator();
				while (it.hasNext()) {
					if (Objects.equals(it.next().getValue(), value)) {
						it.remove();
						break;
					}
				}
			});

			this.last = -1;
		}

	}

}
