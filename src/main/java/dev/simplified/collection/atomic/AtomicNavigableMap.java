package dev.simplified.collection.atomic;

import dev.simplified.collection.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
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
import java.util.SortedSet;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe abstract navigable map backed by a {@link ReadWriteLock} for concurrent access.
 * Extends {@link AtomicMap} to add the navigable-traversal surface ({@link NavigableMap#firstEntry},
 * {@link NavigableMap#ceilingEntry}, {@link NavigableMap#headMap}, etc.) with atomic guarantees.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @param <M> the type of the underlying map, which must be both an {@link AbstractMap} and a
 *            {@link NavigableMap}
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicNavigableMap<K, V, M extends AbstractMap<K, V> & NavigableMap<K, V>> extends AtomicMap<K, V, M> implements ConcurrentMap<K, V>, NavigableMap<K, V> {

	private transient volatile @Nullable NavigableMap<K, V> descendingMapView;
	private transient volatile @Nullable NavigableSet<K> navigableKeySetView;
	private transient volatile @Nullable NavigableSet<K> descendingKeySetView;

	protected AtomicNavigableMap(@NotNull M ref) {
		super(ref);
	}

	protected AtomicNavigableMap(@NotNull M ref, @Nullable Map<? extends K, ? extends V> items) {
		super(ref, items);
	}

	@SafeVarargs
	protected AtomicNavigableMap(@NotNull M ref, @Nullable Map.Entry<? extends K, ? extends V>... items) {
		super(ref, items);
	}

	/**
	 * Constructs an {@code AtomicNavigableMap} with an explicit lock - the pattern used by
	 * {@code ConcurrentUnmodifiableTreeMap} to install a snapshot map paired with a no-op lock for
	 * wait-free reads.
	 *
	 * @param ref the underlying map
	 * @param lock the lock guarding {@code ref}
	 */
	protected AtomicNavigableMap(@NotNull M ref, @NotNull ReadWriteLock lock) {
		super(ref, lock);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onSnapshotInvalidated() {
		if (this.descendingMapView != null) this.descendingMapView = null;
		if (this.navigableKeySetView != null) this.navigableKeySetView = null;
		if (this.descendingKeySetView != null) this.descendingKeySetView = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Comparator<? super K> comparator() {
		return this.ref.comparator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public K firstKey() {
		return this.withReadLock(this.ref::firstKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public K lastKey() {
		return this.withReadLock(this.ref::lastKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public K floorKey(K key) {
		return this.withReadLock(() -> this.ref.floorKey(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public K ceilingKey(K key) {
		return this.withReadLock(() -> this.ref.ceilingKey(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public K lowerKey(K key) {
		return this.withReadLock(() -> this.ref.lowerKey(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public K higherKey(K key) {
		return this.withReadLock(() -> this.ref.higherKey(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<K, V> firstEntry() {
		return this.withReadLock(this.ref::firstEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<K, V> lastEntry() {
		return this.withReadLock(this.ref::lastEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<K, V> floorEntry(K key) {
		return this.withReadLock(() -> this.ref.floorEntry(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<K, V> ceilingEntry(K key) {
		return this.withReadLock(() -> this.ref.ceilingEntry(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<K, V> lowerEntry(K key) {
		return this.withReadLock(() -> this.ref.lowerEntry(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<K, V> higherEntry(K key) {
		return this.withReadLock(() -> this.ref.higherEntry(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<K, V> pollFirstEntry() {
		return this.withWriteLock(this.ref::pollFirstEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<K, V> pollLastEntry() {
		return this.withWriteLock(this.ref::pollLastEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull NavigableMap<K, V> descendingMap() {
		NavigableMap<K, V> view = this.descendingMapView;

		if (view != null)
			return view;

		return this.withReadLock(() -> {
			NavigableMap<K, V> cached = this.descendingMapView;

			if (cached == null) {
				cached = new LockedNavigableMapView(this.ref.descendingMap());
				this.descendingMapView = cached;
			}

			return cached;
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull NavigableSet<K> navigableKeySet() {
		NavigableSet<K> view = this.navigableKeySetView;

		if (view != null)
			return view;

		return this.withReadLock(() -> {
			NavigableSet<K> cached = this.navigableKeySetView;

			if (cached == null) {
				cached = new LockedNavigableSetView(this.ref.navigableKeySet());
				this.navigableKeySetView = cached;
			}

			return cached;
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull NavigableSet<K> descendingKeySet() {
		NavigableSet<K> view = this.descendingKeySetView;

		if (view != null)
			return view;

		return this.withReadLock(() -> {
			NavigableSet<K> cached = this.descendingKeySetView;

			if (cached == null) {
				cached = new LockedNavigableSetView(this.ref.descendingKeySet());
				this.descendingKeySetView = cached;
			}

			return cached;
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull NavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive) {
		return this.withReadLock(() -> new LockedNavigableMapView(this.ref.subMap(from, fromInclusive, to, toInclusive)));
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
		return this.withReadLock(() -> new LockedNavigableMapView(this.ref.headMap(to, inclusive)));
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
		return this.withReadLock(() -> new LockedNavigableMapView(this.ref.tailMap(from, inclusive)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull SortedMap<K, V> tailMap(K from) {
		return this.tailMap(from, true);
	}

	/**
	 * Lock-guarded {@link NavigableMap} wrapper around a sub-view obtained from this map's
	 * backing {@link NavigableMap}. All reads acquire the enclosing map's read lock; mutations
	 * acquire its write lock and propagate to the backing map.
	 */
	protected final class LockedNavigableMapView extends AbstractMap<K, V> implements NavigableMap<K, V> {

		private final @NotNull NavigableMap<K, V> delegate;

		protected LockedNavigableMapView(@NotNull NavigableMap<K, V> delegate) {
			this.delegate = delegate;
		}

		@Override public int size() { return AtomicNavigableMap.this.withReadLock(this.delegate::size); }
		@Override public boolean isEmpty() { return AtomicNavigableMap.this.withReadLock(this.delegate::isEmpty); }
		@Override public boolean containsKey(Object key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.containsKey(key)); }
		@Override public boolean containsValue(Object value) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.containsValue(value)); }
		@Override public V get(Object key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.get(key)); }
		@Override public V put(K key, V value) { return AtomicNavigableMap.this.withWriteLock(() -> this.delegate.put(key, value)); }
		@Override public V remove(Object key) { return AtomicNavigableMap.this.withWriteLock(() -> this.delegate.remove(key)); }
		@Override public void putAll(@NotNull Map<? extends K, ? extends V> m) { AtomicNavigableMap.this.withWriteLock(() -> this.delegate.putAll(m)); }
		@Override public void clear() { AtomicNavigableMap.this.withWriteLock(this.delegate::clear); }
		@Override public Comparator<? super K> comparator() { return this.delegate.comparator(); }
		@Override public K firstKey() { return AtomicNavigableMap.this.withReadLock(this.delegate::firstKey); }
		@Override public K lastKey() { return AtomicNavigableMap.this.withReadLock(this.delegate::lastKey); }
		@Override public Map.Entry<K, V> firstEntry() { return AtomicNavigableMap.this.withReadLock(this.delegate::firstEntry); }
		@Override public Map.Entry<K, V> lastEntry() { return AtomicNavigableMap.this.withReadLock(this.delegate::lastEntry); }
		@Override public Map.Entry<K, V> floorEntry(K key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.floorEntry(key)); }
		@Override public K floorKey(K key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.floorKey(key)); }
		@Override public Map.Entry<K, V> ceilingEntry(K key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.ceilingEntry(key)); }
		@Override public K ceilingKey(K key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.ceilingKey(key)); }
		@Override public Map.Entry<K, V> lowerEntry(K key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.lowerEntry(key)); }
		@Override public K lowerKey(K key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.lowerKey(key)); }
		@Override public Map.Entry<K, V> higherEntry(K key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.higherEntry(key)); }
		@Override public K higherKey(K key) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.higherKey(key)); }
		@Override public Map.Entry<K, V> pollFirstEntry() { return AtomicNavigableMap.this.withWriteLock(this.delegate::pollFirstEntry); }
		@Override public Map.Entry<K, V> pollLastEntry() { return AtomicNavigableMap.this.withWriteLock(this.delegate::pollLastEntry); }

		@Override
		public @NotNull NavigableMap<K, V> descendingMap() {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableMapView(this.delegate.descendingMap()));
		}

		@Override
		public @NotNull NavigableSet<K> navigableKeySet() {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.navigableKeySet()));
		}

		@Override
		public @NotNull NavigableSet<K> descendingKeySet() {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.descendingKeySet()));
		}

		@Override
		public @NotNull NavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive) {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableMapView(this.delegate.subMap(from, fromInclusive, to, toInclusive)));
		}

		@Override
		public @NotNull SortedMap<K, V> subMap(K from, K to) {
			return this.subMap(from, true, to, false);
		}

		@Override
		public @NotNull NavigableMap<K, V> headMap(K to, boolean inclusive) {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableMapView(this.delegate.headMap(to, inclusive)));
		}

		@Override
		public @NotNull SortedMap<K, V> headMap(K to) {
			return this.headMap(to, false);
		}

		@Override
		public @NotNull NavigableMap<K, V> tailMap(K from, boolean inclusive) {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableMapView(this.delegate.tailMap(from, inclusive)));
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
			return new LockedEntrySetView(this.delegate);
		}

		@Override
		public @NotNull Collection<V> values() {
			return new LockedValuesView(this.delegate);
		}

	}

	/**
	 * Lock-guarded {@link NavigableSet} wrapper around a key-set view of this map.
	 */
	protected final class LockedNavigableSetView extends AbstractSet<K> implements NavigableSet<K> {

		private final @NotNull NavigableSet<K> delegate;

		protected LockedNavigableSetView(@NotNull NavigableSet<K> delegate) {
			this.delegate = delegate;
		}

		@Override public int size() { return AtomicNavigableMap.this.withReadLock(this.delegate::size); }
		@Override public boolean isEmpty() { return AtomicNavigableMap.this.withReadLock(this.delegate::isEmpty); }
		@Override public boolean contains(Object o) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.contains(o)); }
		@Override public boolean add(K e) { return AtomicNavigableMap.this.withWriteLock(() -> this.delegate.add(e)); }
		@Override public boolean remove(Object o) { return AtomicNavigableMap.this.withWriteLock(() -> this.delegate.remove(o)); }
		@Override public void clear() { AtomicNavigableMap.this.withWriteLock(this.delegate::clear); }
		@Override public Comparator<? super K> comparator() { return this.delegate.comparator(); }
		@Override public K first() { return AtomicNavigableMap.this.withReadLock(this.delegate::first); }
		@Override public K last() { return AtomicNavigableMap.this.withReadLock(this.delegate::last); }
		@Override public K lower(K e) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.lower(e)); }
		@Override public K floor(K e) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.floor(e)); }
		@Override public K ceiling(K e) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.ceiling(e)); }
		@Override public K higher(K e) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.higher(e)); }
		@Override public K pollFirst() { return AtomicNavigableMap.this.withWriteLock(this.delegate::pollFirst); }
		@Override public K pollLast() { return AtomicNavigableMap.this.withWriteLock(this.delegate::pollLast); }

		@Override
		public @NotNull Iterator<K> iterator() {
			Object[] snapshot = AtomicNavigableMap.this.withReadLock(() -> this.delegate.toArray());
			return new AtomicIterator<>(snapshot, 0);
		}

		@Override
		public @NotNull NavigableSet<K> descendingSet() {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.descendingSet()));
		}

		@Override
		public @NotNull Iterator<K> descendingIterator() {
			Object[] snapshot = AtomicNavigableMap.this.withReadLock(() -> this.delegate.descendingSet().toArray());
			return new AtomicIterator<>(snapshot, 0);
		}

		@Override
		public @NotNull NavigableSet<K> subSet(K from, boolean fromInclusive, K to, boolean toInclusive) {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.subSet(from, fromInclusive, to, toInclusive)));
		}

		@Override
		public @NotNull NavigableSet<K> headSet(K to, boolean inclusive) {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.headSet(to, inclusive)));
		}

		@Override
		public @NotNull NavigableSet<K> tailSet(K from, boolean inclusive) {
			return AtomicNavigableMap.this.withReadLock(() -> new LockedNavigableSetView(this.delegate.tailSet(from, inclusive)));
		}

		@Override
		public @NotNull SortedSet<K> subSet(K from, K to) {
			return this.subSet(from, true, to, false);
		}

		@Override
		public @NotNull SortedSet<K> headSet(K to) {
			return this.headSet(to, false);
		}

		@Override
		public @NotNull SortedSet<K> tailSet(K from) {
			return this.tailSet(from, true);
		}

	}

	/**
	 * Lock-guarded entry-set view backed by a {@link NavigableMap} delegate.
	 */
	protected final class LockedEntrySetView extends AbstractSet<Map.Entry<K, V>> {

		private final @NotNull NavigableMap<K, V> delegate;

		protected LockedEntrySetView(@NotNull NavigableMap<K, V> delegate) {
			this.delegate = delegate;
		}

		@Override public int size() { return AtomicNavigableMap.this.withReadLock(this.delegate::size); }
		@Override public boolean isEmpty() { return AtomicNavigableMap.this.withReadLock(this.delegate::isEmpty); }
		@Override public boolean contains(Object o) { return AtomicNavigableMap.this.withReadLock(() -> this.delegate.entrySet().contains(o)); }
		@Override public boolean remove(Object o) { return AtomicNavigableMap.this.withWriteLock(() -> this.delegate.entrySet().remove(o)); }
		@Override public void clear() { AtomicNavigableMap.this.withWriteLock(this.delegate::clear); }

		@Override
		public @NotNull Iterator<Map.Entry<K, V>> iterator() {
			Object[] snapshot = AtomicNavigableMap.this.withReadLock(() -> this.delegate.entrySet().toArray());
			return new AtomicIterator<>(snapshot, 0);
		}

	}

	/**
	 * Lock-guarded values view backed by a {@link NavigableMap} delegate.
	 */
	protected final class LockedValuesView extends AbstractCollection<V> {

		private final @NotNull NavigableMap<K, V> delegate;

		protected LockedValuesView(@NotNull NavigableMap<K, V> delegate) {
			this.delegate = delegate;
		}

		@Override public int size() { return AtomicNavigableMap.this.withReadLock(this.delegate::size); }
		@Override public boolean isEmpty() { return AtomicNavigableMap.this.withReadLock(this.delegate::isEmpty); }

		@Override
		@SuppressWarnings("SuspiciousMethodCalls")
		public boolean contains(Object o) {
			return AtomicNavigableMap.this.withReadLock(() -> this.delegate.containsValue(o));
		}

		@Override
		public void clear() {
			AtomicNavigableMap.this.withWriteLock(this.delegate::clear);
		}

		@Override
		public @NotNull Iterator<V> iterator() {
			Object[] snapshot = AtomicNavigableMap.this.withReadLock(() -> this.delegate.values().toArray());
			return new AtomicIterator<>(snapshot, 0);
		}

	}

}
