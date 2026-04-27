package dev.simplified.collection.tree;

import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe concurrent map variant backed by a {@link TreeMap} that maintains its entries in
 * key-sorted order according to the keys' natural ordering or a {@link Comparator} provided at
 * construction time.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface ConcurrentTreeMap<K, V> extends ConcurrentMap<K, V>, NavigableMap<K, V> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableTreeMap<K, V> toUnmodifiable();

	/**
	 * A thread-safe map backed by a {@link TreeMap} with concurrent read and write access via
	 * {@link ReadWriteLock}. Maintains key ordering defined by a {@link Comparator} or the keys'
	 * natural ordering.
	 *
	 * @param <K> the type of keys maintained by this map
	 * @param <V> the type of mapped values
	 */
	class Impl<K, V> extends ConcurrentMap.Impl<K, V> implements ConcurrentTreeMap<K, V> {

		private transient volatile @Nullable NavigableMap<K, V> descendingMapView;
		private transient volatile @Nullable NavigableSet<K> navigableKeySetView;
		private transient volatile @Nullable NavigableSet<K> descendingKeySetView;

		/**
		 * Creates a new concurrent sorted map with natural key ordering.
		 */
		public Impl() {
			super(new TreeMap<>(), (Map<K, V>) null);
		}

		/**
		 * Creates a new concurrent sorted map with the given comparator.
		 *
		 * @param comparator the comparator used to order the keys
		 */
		public Impl(@NotNull Comparator<? super K> comparator) {
			super(new TreeMap<>(comparator), (Map<K, V>) null);
		}

		/**
		 * Creates a new concurrent sorted map with natural key ordering and fills it with the given
		 * pairs.
		 *
		 * @param pairs the entries to include
		 */
		@SafeVarargs
		public Impl(@Nullable Map.Entry<K, V>... pairs) {
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
		public Impl(@NotNull Comparator<? super K> comparator, @Nullable Map.Entry<K, V>... pairs) {
			super(new TreeMap<>(comparator), pairs);
		}

		/**
		 * Creates a new concurrent sorted map with natural key ordering and fills it with the given
		 * map.
		 *
		 * @param map the source map to copy from
		 */
		public Impl(@Nullable Map<? extends K, ? extends V> map) {
			super(new TreeMap<>(), map);
		}

		/**
		 * Creates a new concurrent sorted map with the given comparator and fills it with the given
		 * map.
		 *
		 * @param comparator the comparator used to order the keys
		 * @param map the source map to copy from
		 */
		public Impl(@NotNull Comparator<? super K> comparator, @Nullable Map<? extends K, ? extends V> map) {
			super(new TreeMap<>(comparator), map);
		}

		/**
		 * Constructs a {@code ConcurrentTreeMap.Impl} with a pre-built backing map and an explicit
		 * lock. Used by {@link ConcurrentUnmodifiableTreeMap.Impl} to install a snapshot map paired
		 * with a no-op lock for wait-free reads.
		 *
		 * @param backingMap the pre-built backing map
		 * @param lock the lock guarding {@code backingMap}
		 */
		protected Impl(@NotNull TreeMap<K, V> backingMap, @NotNull ReadWriteLock lock) {
			super(backingMap, lock);
		}

		/**
		 * {@inheritDoc}
		 *
		 * <p>Overrides {@link ConcurrentMap.Impl#cloneRef()} to produce a {@link TreeMap} snapshot.
		 * {@link TreeMap#TreeMap(java.util.SortedMap)} preserves the source's comparator when the
		 * source is itself a {@code SortedMap}.</p>
		 */
		@Override
		protected @NotNull AbstractMap<K, V> cloneRef() {
			try {
				this.lock.readLock().lock();
				return new TreeMap<>((TreeMap<K, V>) this.ref);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentTreeMap.Impl} preserving the
		 * source's comparator and sort order.
		 *
		 * @return an unmodifiable {@link ConcurrentTreeMap.Impl} containing a snapshot of the entries
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableTreeMap<K, V> toUnmodifiable() {
			return new ConcurrentUnmodifiableTreeMap.Impl<>((TreeMap<K, V>) this.cloneRef());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void onSnapshotInvalidated() {
			this.descendingMapView = null;
			this.navigableKeySetView = null;
			this.descendingKeySetView = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Comparator<? super K> comparator() {
			return ((TreeMap<K, V>) this.ref).comparator();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public K firstKey() {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).firstKey();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public K lastKey() {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).lastKey();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public K floorKey(K key) {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).floorKey(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public K ceilingKey(K key) {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).ceilingKey(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public K lowerKey(K key) {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).lowerKey(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public K higherKey(K key) {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).higherKey(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map.Entry<K, V> firstEntry() {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).firstEntry();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map.Entry<K, V> lastEntry() {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).lastEntry();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map.Entry<K, V> floorEntry(K key) {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).floorEntry(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map.Entry<K, V> ceilingEntry(K key) {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).ceilingEntry(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map.Entry<K, V> lowerEntry(K key) {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).lowerEntry(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map.Entry<K, V> higherEntry(K key) {
			try {
				this.lock.readLock().lock();
				return ((TreeMap<K, V>) this.ref).higherEntry(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map.Entry<K, V> pollFirstEntry() {
			try {
				this.lock.writeLock().lock();
				return ((TreeMap<K, V>) this.ref).pollFirstEntry();
			} finally {
				this.invalidateViewSnapshots();
				this.lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map.Entry<K, V> pollLastEntry() {
			try {
				this.lock.writeLock().lock();
				return ((TreeMap<K, V>) this.ref).pollLastEntry();
			} finally {
				this.invalidateViewSnapshots();
				this.lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableMap<K, V> descendingMap() {
			NavigableMap<K, V> view = this.descendingMapView;

			if (view != null)
				return view;

			try {
				this.lock.readLock().lock();
				view = this.descendingMapView;

				if (view == null) {
					view = new LockedNavigableMapView<>(((TreeMap<K, V>) this.ref).descendingMap(), this.lock);
					this.descendingMapView = view;
				}

				return view;
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableSet<K> navigableKeySet() {
			NavigableSet<K> view = this.navigableKeySetView;

			if (view != null)
				return view;

			try {
				this.lock.readLock().lock();
				view = this.navigableKeySetView;

				if (view == null) {
					view = new LockedNavigableSetView<>(((TreeMap<K, V>) this.ref).navigableKeySet(), this.lock);
					this.navigableKeySetView = view;
				}

				return view;
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableSet<K> descendingKeySet() {
			NavigableSet<K> view = this.descendingKeySetView;

			if (view != null)
				return view;

			try {
				this.lock.readLock().lock();
				view = this.descendingKeySetView;

				if (view == null) {
					view = new LockedNavigableSetView<>(((TreeMap<K, V>) this.ref).descendingKeySet(), this.lock);
					this.descendingKeySetView = view;
				}

				return view;
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive) {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableMapView<>(((TreeMap<K, V>) this.ref).subMap(from, fromInclusive, to, toInclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull SortedMap<K, V> subMap(K from, K to) {
			return this.subMap(from, true, to, false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableMap<K, V> headMap(K to, boolean inclusive) {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableMapView<>(((TreeMap<K, V>) this.ref).headMap(to, inclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull SortedMap<K, V> headMap(K to) {
			return this.headMap(to, false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull NavigableMap<K, V> tailMap(K from, boolean inclusive) {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableMapView<>(((TreeMap<K, V>) this.ref).tailMap(from, inclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull SortedMap<K, V> tailMap(K from) {
			return this.tailMap(from, true);
		}

	}

	/**
	 * Lock-guarded {@link NavigableMap} wrapper around a sub-view obtained from a backing
	 * {@link TreeMap}. All reads acquire the parent's read lock; mutations acquire the parent's
	 * write lock and propagate to the parent map.
	 *
	 * @param <K> the type of keys
	 * @param <V> the type of mapped values
	 */
	final class LockedNavigableMapView<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V> {

		private final NavigableMap<K, V> delegate;
		private final ReadWriteLock lock;

		LockedNavigableMapView(@NotNull NavigableMap<K, V> delegate, @NotNull ReadWriteLock lock) {
			this.delegate = delegate;
			this.lock = lock;
		}

		@Override
		public int size() {
			try {
				this.lock.readLock().lock();
				return this.delegate.size();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean isEmpty() {
			try {
				this.lock.readLock().lock();
				return this.delegate.isEmpty();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean containsKey(Object key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.containsKey(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean containsValue(Object value) {
			try {
				this.lock.readLock().lock();
				return this.delegate.containsValue(value);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public V get(Object key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.get(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public V put(K key, V value) {
			try {
				this.lock.writeLock().lock();
				return this.delegate.put(key, value);
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public V remove(Object key) {
			try {
				this.lock.writeLock().lock();
				return this.delegate.remove(key);
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public void putAll(@NotNull Map<? extends K, ? extends V> m) {
			try {
				this.lock.writeLock().lock();
				this.delegate.putAll(m);
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public void clear() {
			try {
				this.lock.writeLock().lock();
				this.delegate.clear();
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public Comparator<? super K> comparator() {
			return this.delegate.comparator();
		}

		@Override
		public K firstKey() {
			try {
				this.lock.readLock().lock();
				return this.delegate.firstKey();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public K lastKey() {
			try {
				this.lock.readLock().lock();
				return this.delegate.lastKey();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public Map.Entry<K, V> firstEntry() {
			try {
				this.lock.readLock().lock();
				return this.delegate.firstEntry();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public Map.Entry<K, V> lastEntry() {
			try {
				this.lock.readLock().lock();
				return this.delegate.lastEntry();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public Map.Entry<K, V> floorEntry(K key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.floorEntry(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public K floorKey(K key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.floorKey(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public Map.Entry<K, V> ceilingEntry(K key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.ceilingEntry(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public K ceilingKey(K key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.ceilingKey(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public Map.Entry<K, V> lowerEntry(K key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.lowerEntry(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public K lowerKey(K key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.lowerKey(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public Map.Entry<K, V> higherEntry(K key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.higherEntry(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public K higherKey(K key) {
			try {
				this.lock.readLock().lock();
				return this.delegate.higherKey(key);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public Map.Entry<K, V> pollFirstEntry() {
			try {
				this.lock.writeLock().lock();
				return this.delegate.pollFirstEntry();
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public Map.Entry<K, V> pollLastEntry() {
			try {
				this.lock.writeLock().lock();
				return this.delegate.pollLastEntry();
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public @NotNull NavigableMap<K, V> descendingMap() {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableMapView<>(this.delegate.descendingMap(), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull NavigableSet<K> navigableKeySet() {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableSetView<>(this.delegate.navigableKeySet(), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull NavigableSet<K> descendingKeySet() {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableSetView<>(this.delegate.descendingKeySet(), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull NavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive) {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableMapView<>(this.delegate.subMap(from, fromInclusive, to, toInclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull SortedMap<K, V> subMap(K from, K to) {
			return this.subMap(from, true, to, false);
		}

		@Override
		public @NotNull NavigableMap<K, V> headMap(K to, boolean inclusive) {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableMapView<>(this.delegate.headMap(to, inclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull SortedMap<K, V> headMap(K to) {
			return this.headMap(to, false);
		}

		@Override
		public @NotNull NavigableMap<K, V> tailMap(K from, boolean inclusive) {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableMapView<>(this.delegate.tailMap(from, inclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull SortedMap<K, V> tailMap(K from) {
			return this.tailMap(from, true);
		}

		@Override
		public @NotNull Set<K> keySet() {
			return this.navigableKeySet();
		}

		@Override
		public @NotNull Set<Map.Entry<K, V>> entrySet() {
			return new LockedEntrySetView<>(this.delegate, this.lock);
		}

		@Override
		public @NotNull Collection<V> values() {
			return new LockedValuesView<>(this.delegate, this.lock);
		}

	}

	/**
	 * Lock-guarded {@link NavigableSet} wrapper around a key set view from a backing
	 * {@link TreeMap} or {@link java.util.TreeSet}.
	 *
	 * @param <E> the type of elements
	 */
	final class LockedNavigableSetView<E> extends java.util.AbstractSet<E> implements NavigableSet<E> {

		private final NavigableSet<E> delegate;
		private final ReadWriteLock lock;

		LockedNavigableSetView(@NotNull NavigableSet<E> delegate, @NotNull ReadWriteLock lock) {
			this.delegate = delegate;
			this.lock = lock;
		}

		@Override
		public int size() {
			try {
				this.lock.readLock().lock();
				return this.delegate.size();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean isEmpty() {
			try {
				this.lock.readLock().lock();
				return this.delegate.isEmpty();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean contains(Object o) {
			try {
				this.lock.readLock().lock();
				return this.delegate.contains(o);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean add(E e) {
			try {
				this.lock.writeLock().lock();
				return this.delegate.add(e);
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public boolean remove(Object o) {
			try {
				this.lock.writeLock().lock();
				return this.delegate.remove(o);
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public void clear() {
			try {
				this.lock.writeLock().lock();
				this.delegate.clear();
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public @NotNull Iterator<E> iterator() {
			Object[] snapshot;

			try {
				this.lock.readLock().lock();
				snapshot = this.delegate.toArray();
			} finally {
				this.lock.readLock().unlock();
			}

			return new SnapshotIterator<>(snapshot);
		}

		@Override
		public Comparator<? super E> comparator() {
			return this.delegate.comparator();
		}

		@Override
		public E first() {
			try {
				this.lock.readLock().lock();
				return this.delegate.first();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public E last() {
			try {
				this.lock.readLock().lock();
				return this.delegate.last();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public E lower(E e) {
			try {
				this.lock.readLock().lock();
				return this.delegate.lower(e);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public E floor(E e) {
			try {
				this.lock.readLock().lock();
				return this.delegate.floor(e);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public E ceiling(E e) {
			try {
				this.lock.readLock().lock();
				return this.delegate.ceiling(e);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public E higher(E e) {
			try {
				this.lock.readLock().lock();
				return this.delegate.higher(e);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public E pollFirst() {
			try {
				this.lock.writeLock().lock();
				return this.delegate.pollFirst();
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public E pollLast() {
			try {
				this.lock.writeLock().lock();
				return this.delegate.pollLast();
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public @NotNull NavigableSet<E> descendingSet() {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableSetView<>(this.delegate.descendingSet(), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull Iterator<E> descendingIterator() {
			Object[] snapshot;

			try {
				this.lock.readLock().lock();
				snapshot = this.delegate.descendingSet().toArray();
			} finally {
				this.lock.readLock().unlock();
			}

			return new SnapshotIterator<>(snapshot);
		}

		@Override
		public @NotNull NavigableSet<E> subSet(E from, boolean fromInclusive, E to, boolean toInclusive) {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableSetView<>(this.delegate.subSet(from, fromInclusive, to, toInclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull NavigableSet<E> headSet(E to, boolean inclusive) {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableSetView<>(this.delegate.headSet(to, inclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull NavigableSet<E> tailSet(E from, boolean inclusive) {
			try {
				this.lock.readLock().lock();
				return new LockedNavigableSetView<>(this.delegate.tailSet(from, inclusive), this.lock);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public @NotNull java.util.SortedSet<E> subSet(E from, E to) {
			return this.subSet(from, true, to, false);
		}

		@Override
		public @NotNull java.util.SortedSet<E> headSet(E to) {
			return this.headSet(to, false);
		}

		@Override
		public @NotNull java.util.SortedSet<E> tailSet(E from) {
			return this.tailSet(from, true);
		}

	}

	/**
	 * Lock-guarded entry-set view backed by a {@link NavigableMap} delegate.
	 *
	 * @param <K> the type of keys
	 * @param <V> the type of mapped values
	 */
	final class LockedEntrySetView<K, V> extends AbstractSet<Map.Entry<K, V>> {

		private final NavigableMap<K, V> delegate;
		private final ReadWriteLock lock;

		LockedEntrySetView(@NotNull NavigableMap<K, V> delegate, @NotNull ReadWriteLock lock) {
			this.delegate = delegate;
			this.lock = lock;
		}

		@Override
		public int size() {
			try {
				this.lock.readLock().lock();
				return this.delegate.size();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean isEmpty() {
			try {
				this.lock.readLock().lock();
				return this.delegate.isEmpty();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean contains(Object o) {
			try {
				this.lock.readLock().lock();
				return this.delegate.entrySet().contains(o);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean remove(Object o) {
			try {
				this.lock.writeLock().lock();
				return this.delegate.entrySet().remove(o);
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public void clear() {
			try {
				this.lock.writeLock().lock();
				this.delegate.clear();
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public @NotNull Iterator<Map.Entry<K, V>> iterator() {
			Object[] snapshot;

			try {
				this.lock.readLock().lock();
				snapshot = this.delegate.entrySet().toArray();
			} finally {
				this.lock.readLock().unlock();
			}

			return new SnapshotIterator<>(snapshot);
		}

	}

	/**
	 * Lock-guarded values view backed by a {@link NavigableMap} delegate.
	 *
	 * @param <V> the type of mapped values
	 */
	final class LockedValuesView<V> extends java.util.AbstractCollection<V> {

		private final NavigableMap<?, V> delegate;
		private final ReadWriteLock lock;

		LockedValuesView(@NotNull NavigableMap<?, V> delegate, @NotNull ReadWriteLock lock) {
			this.delegate = delegate;
			this.lock = lock;
		}

		@Override
		public int size() {
			try {
				this.lock.readLock().lock();
				return this.delegate.size();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean isEmpty() {
			try {
				this.lock.readLock().lock();
				return this.delegate.isEmpty();
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public boolean contains(Object o) {
			try {
				this.lock.readLock().lock();
				return this.delegate.values().contains(o);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		@Override
		public void clear() {
			try {
				this.lock.writeLock().lock();
				this.delegate.clear();
			} finally {
				this.lock.writeLock().unlock();
			}
		}

		@Override
		public @NotNull Iterator<V> iterator() {
			Object[] snapshot;

			try {
				this.lock.readLock().lock();
				snapshot = this.delegate.values().toArray();
			} finally {
				this.lock.readLock().unlock();
			}

			return new SnapshotIterator<>(snapshot);
		}

	}

	/**
	 * Snapshot-backed iterator used by lock-guarded views.
	 *
	 * @param <E> the iterator element type
	 */
	final class SnapshotIterator<E> implements Iterator<E> {

		private final Object[] snapshot;
		private int cursor;

		SnapshotIterator(@NotNull Object[] snapshot) {
			this.snapshot = snapshot;
			this.cursor = 0;
		}

		@Override
		public boolean hasNext() {
			return this.cursor < this.snapshot.length;
		}

		@Override
		@SuppressWarnings("unchecked")
		public E next() {
			if (!this.hasNext())
				throw new java.util.NoSuchElementException();

			return (E) this.snapshot[this.cursor++];
		}

	}

}
