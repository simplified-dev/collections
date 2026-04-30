package dev.simplified.collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Internal mega-factory housing every immutable snapshot wrapper. Mirrors the JDK pattern from
 * {@link Collections} where {@code UnmodifiableMap}, {@code UnmodifiableSortedMap}, etc. all live
 * as nested classes inside a single utility class.
 *
 * <p>The wrappers are package-private; consumers obtain them through {@code toUnmodifiable()} on
 * any mutable concurrent collection or through the {@link Concurrent} factory finishers.</p>
 */
final class ConcurrentUnmodifiable {

	private ConcurrentUnmodifiable() {}

	/**
	 * A no-op {@link ReadWriteLock} used by snapshot wrappers to bypass synchronization on read
	 * paths. Snapshot wrappers own a freshly cloned, never-mutated backing collection; the JMM
	 * already guarantees safe publication via {@code final} field assignment, so acquiring a real
	 * lock on every read would be pure overhead. Mutation paths throw
	 * {@link UnsupportedOperationException} before reaching {@link #writeLock()}, so the write
	 * lock is never actually exercised.
	 */
	static final class NoOpReadWriteLock implements ReadWriteLock, Serializable {

		static final @NotNull NoOpReadWriteLock INSTANCE = new NoOpReadWriteLock();

		private static final @NotNull Lock NO_OP_LOCK = new NoOpLock();

		private NoOpReadWriteLock() {}

		@Override
		public @NotNull Lock readLock() {
			return NO_OP_LOCK;
		}

		@Override
		public @NotNull Lock writeLock() {
			return NO_OP_LOCK;
		}

		@Serial
		private Object readResolve() {
			return INSTANCE;
		}

		private static final class NoOpLock implements Lock {

			@Override
			public void lock() {}

			@Override
			public void lockInterruptibly() {}

			@Override
			public boolean tryLock() {
				return true;
			}

			@Override
			public boolean tryLock(long time, @NotNull TimeUnit unit) {
				return true;
			}

			@Override
			public void unlock() {}

			@Override
			public @NotNull Condition newCondition() {
				throw new UnsupportedOperationException("Conditions not supported on NoOpReadWriteLock");
			}

		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentArrayList}. Reads are wait-free; mutating
	 * operations reject with {@link UnsupportedOperationException}.
	 *
	 * @param <E> the type of elements in this list
	 */
	static final class UnmodifiableConcurrentArrayList<E> extends ConcurrentArrayList<E> {

		UnmodifiableConcurrentArrayList(@NotNull List<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public boolean add(@NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void add(int index, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(@NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(int index, @NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
		@Override public void addFirst(@NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void addLast(@NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Predicate<List<E>> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object element) { throw new UnsupportedOperationException(); }
		@Override public E remove(int index) { throw new UnsupportedOperationException(); }
		@Override public E removeFirst() { throw new UnsupportedOperationException(); }
		@Override public E removeLast() { throw new UnsupportedOperationException(); }
		@Override public boolean removeAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull Predicate<? super E> filter) { throw new UnsupportedOperationException(); }
		@Override public boolean retainAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public void replaceAll(@NotNull UnaryOperator<E> operator) { throw new UnsupportedOperationException(); }
		@Override public E set(int index, E element) { throw new UnsupportedOperationException(); }
		@Override public void sort(Comparator<? super E> comparator) { throw new UnsupportedOperationException(); }

		@Override
		public @NotNull ConcurrentList<E> toUnmodifiable() {
			return this;
		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentHashSet}. Reads are wait-free; mutating operations
	 * reject with {@link UnsupportedOperationException}.
	 *
	 * @param <E> the type of elements in this set
	 */
	static final class UnmodifiableConcurrentHashSet<E> extends ConcurrentHashSet<E> {

		UnmodifiableConcurrentHashSet(@NotNull java.util.AbstractSet<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public boolean add(@NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(@NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Predicate<java.util.AbstractSet<E>> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object item) { throw new UnsupportedOperationException(); }
		@Override public boolean removeAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull Predicate<? super E> filter) { throw new UnsupportedOperationException(); }
		@Override public boolean retainAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }

		@Override
		public @NotNull ConcurrentSet<E> toUnmodifiable() {
			return this;
		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentLinkedHashSet} preserving the source's insertion
	 * order. Reads are wait-free; mutating operations reject with
	 * {@link UnsupportedOperationException}.
	 *
	 * @param <E> the type of elements in this set
	 */
	static final class UnmodifiableConcurrentLinkedHashSet<E> extends ConcurrentLinkedHashSet<E> {

		UnmodifiableConcurrentLinkedHashSet(@NotNull LinkedHashSet<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public boolean add(@NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(@NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Predicate<java.util.AbstractSet<E>> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object item) { throw new UnsupportedOperationException(); }
		@Override public boolean removeAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull Predicate<? super E> filter) { throw new UnsupportedOperationException(); }
		@Override public boolean retainAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }

		@Override
		public @NotNull ConcurrentSet<E> toUnmodifiable() {
			return this;
		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentHashMap}. Reads are wait-free; mutating operations
	 * reject with {@link UnsupportedOperationException}.
	 *
	 * @param <K> the type of keys
	 * @param <V> the type of mapped values
	 */
	static final class UnmodifiableConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

		UnmodifiableConcurrentHashMap(@NotNull java.util.AbstractMap<K, V> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public @Nullable V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) { throw new UnsupportedOperationException(); }
		@Override public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V put(K key, V value) { throw new UnsupportedOperationException(); }
		@Override public void putAll(@NotNull Map<? extends K, ? extends V> map) { throw new UnsupportedOperationException(); }
		@Override public boolean putIf(@NotNull Supplier<Boolean> predicate, K key, V value) { throw new UnsupportedOperationException(); }
		@Override public boolean putIf(@NotNull BiPredicate<? super K, ? super V> predicate, K key, V value) { throw new UnsupportedOperationException(); }
		@Override public boolean putIf(@NotNull Predicate<java.util.AbstractMap<K, V>> predicate, K key, V value) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V putIfAbsent(K key, V value) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V remove(Object key) { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object key, Object value) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull BiPredicate<? super K, ? super V> predicate) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull Predicate<? super Entry<K, V>> predicate) { throw new UnsupportedOperationException(); }

		@Override
		public @NotNull ConcurrentMap<K, V> toUnmodifiable() {
			return this;
		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentLinkedHashMap} preserving the source's insertion
	 * order. Reads are wait-free; mutating operations reject with
	 * {@link UnsupportedOperationException}.
	 *
	 * @param <K> the type of keys
	 * @param <V> the type of mapped values
	 */
	static final class UnmodifiableConcurrentLinkedHashMap<K, V> extends ConcurrentLinkedHashMap<K, V> {

		UnmodifiableConcurrentLinkedHashMap(@NotNull LinkedHashMap<K, V> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public @Nullable V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) { throw new UnsupportedOperationException(); }
		@Override public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V put(K key, V value) { throw new UnsupportedOperationException(); }
		@Override public void putAll(@NotNull Map<? extends K, ? extends V> map) { throw new UnsupportedOperationException(); }
		@Override public boolean putIf(@NotNull Supplier<Boolean> predicate, K key, V value) { throw new UnsupportedOperationException(); }
		@Override public boolean putIf(@NotNull BiPredicate<? super K, ? super V> predicate, K key, V value) { throw new UnsupportedOperationException(); }
		@Override public boolean putIf(@NotNull Predicate<java.util.AbstractMap<K, V>> predicate, K key, V value) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V putIfAbsent(K key, V value) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V remove(Object key) { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object key, Object value) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull BiPredicate<? super K, ? super V> predicate) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull Predicate<? super Entry<K, V>> predicate) { throw new UnsupportedOperationException(); }

		@Override
		public @NotNull ConcurrentMap<K, V> toUnmodifiable() {
			return this;
		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentArrayQueue}. Reads are wait-free; mutating
	 * operations reject with {@link UnsupportedOperationException}.
	 *
	 * @param <E> the type of elements in this queue
	 */
	static final class UnmodifiableConcurrentArrayQueue<E> extends ConcurrentArrayQueue<E> {

		UnmodifiableConcurrentArrayQueue(@NotNull ArrayDeque<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public boolean add(E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(@NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public boolean offer(E element) { throw new UnsupportedOperationException(); }
		@Override public @Nullable E poll() { throw new UnsupportedOperationException(); }
		@Override public @NotNull E remove() { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object obj) { throw new UnsupportedOperationException(); }
		@Override public boolean removeAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean retainAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }

		@Override
		public @NotNull ConcurrentQueue<E> toUnmodifiable() {
			return this;
		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentArrayDeque}. Reads are wait-free; mutating
	 * operations reject with {@link UnsupportedOperationException}.
	 *
	 * @param <E> the type of elements in this deque
	 */
	static final class UnmodifiableConcurrentArrayDeque<E> extends ConcurrentArrayDeque<E> {

		UnmodifiableConcurrentArrayDeque(@NotNull ArrayDeque<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public boolean add(E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(@NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
		@Override public void addFirst(E element) { throw new UnsupportedOperationException(); }
		@Override public void addLast(E element) { throw new UnsupportedOperationException(); }
		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public boolean offer(E element) { throw new UnsupportedOperationException(); }
		@Override public boolean offerFirst(E element) { throw new UnsupportedOperationException(); }
		@Override public boolean offerLast(E element) { throw new UnsupportedOperationException(); }
		@Override public @Nullable E poll() { throw new UnsupportedOperationException(); }
		@Override public E pollFirst() { throw new UnsupportedOperationException(); }
		@Override public @Nullable E pollLast() { throw new UnsupportedOperationException(); }
		@Override public @NotNull E pop() { throw new UnsupportedOperationException(); }
		@Override public void push(E element) { throw new UnsupportedOperationException(); }
		@Override public @NotNull E remove() { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object obj) { throw new UnsupportedOperationException(); }
		@Override public boolean removeAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public E removeFirst() { throw new UnsupportedOperationException(); }
		@Override public boolean removeFirstOccurrence(Object obj) { throw new UnsupportedOperationException(); }
		@Override public E removeLast() { throw new UnsupportedOperationException(); }
		@Override public boolean removeLastOccurrence(Object obj) { throw new UnsupportedOperationException(); }
		@Override public boolean retainAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }

		@Override
		public @NotNull ConcurrentDeque<E> toUnmodifiable() {
			return this;
		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentLinkedList} preserving the source's insertion
	 * order. Reads are wait-free; mutating operations reject with
	 * {@link UnsupportedOperationException}.
	 *
	 * @param <E> the type of elements in this list
	 */
	static final class UnmodifiableConcurrentLinkedList<E> extends ConcurrentLinkedList.Impl<E> {

		UnmodifiableConcurrentLinkedList(@NotNull LinkedList<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public boolean add(@NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void add(int index, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(@NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(int index, @NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
		@Override public void addFirst(@NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void addLast(@NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Predicate<List<E>> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object element) { throw new UnsupportedOperationException(); }
		@Override public E remove(int index) { throw new UnsupportedOperationException(); }
		@Override public E removeFirst() { throw new UnsupportedOperationException(); }
		@Override public E removeLast() { throw new UnsupportedOperationException(); }
		@Override public boolean removeAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull Predicate<? super E> filter) { throw new UnsupportedOperationException(); }
		@Override public boolean retainAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public void replaceAll(@NotNull UnaryOperator<E> operator) { throw new UnsupportedOperationException(); }
		@Override public E set(int index, E element) { throw new UnsupportedOperationException(); }
		@Override public void sort(Comparator<? super E> comparator) { throw new UnsupportedOperationException(); }

		@Override
		public @NotNull ConcurrentList<E> toUnmodifiable() {
			return this;
		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentTreeSet} preserving the source's comparator and
	 * sort order. Reads are wait-free; mutating operations reject with
	 * {@link UnsupportedOperationException}.
	 *
	 * @param <E> the type of elements in this set
	 */
	static final class UnmodifiableConcurrentTreeSet<E> extends ConcurrentTreeSet.Impl<E> {

		UnmodifiableConcurrentTreeSet(@NotNull TreeSet<E> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public boolean add(@NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(@NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public boolean addIf(@NotNull Predicate<TreeSet<E>> predicate, @NotNull E element) { throw new UnsupportedOperationException(); }
		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object item) { throw new UnsupportedOperationException(); }
		@Override public boolean removeAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull Predicate<? super E> filter) { throw new UnsupportedOperationException(); }
		@Override public boolean retainAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
		@Override public E pollFirst() { throw new UnsupportedOperationException(); }
		@Override public E pollLast() { throw new UnsupportedOperationException(); }

		@Override
		public @NotNull NavigableSet<E> descendingSet() {
			return Collections.unmodifiableNavigableSet(super.descendingSet());
		}

		@Override
		public @NotNull Iterator<E> descendingIterator() {
			Iterator<E> iterator = super.descendingIterator();
			return new Iterator<>() {
				@Override public boolean hasNext() { return iterator.hasNext(); }
				@Override public E next() { return iterator.next(); }
				@Override public void remove() { throw new UnsupportedOperationException(); }
			};
		}

		@Override public @NotNull NavigableSet<E> subSet(E from, boolean fromInclusive, E to, boolean toInclusive) { return Collections.unmodifiableNavigableSet(super.subSet(from, fromInclusive, to, toInclusive)); }
		@Override public @NotNull NavigableSet<E> headSet(E to, boolean inclusive) { return Collections.unmodifiableNavigableSet(super.headSet(to, inclusive)); }
		@Override public @NotNull NavigableSet<E> tailSet(E from, boolean inclusive) { return Collections.unmodifiableNavigableSet(super.tailSet(from, inclusive)); }
		@Override public @NotNull SortedSet<E> subSet(E from, E to) { return Collections.unmodifiableSortedSet(super.subSet(from, to)); }
		@Override public @NotNull SortedSet<E> headSet(E to) { return Collections.unmodifiableSortedSet(super.headSet(to)); }
		@Override public @NotNull SortedSet<E> tailSet(E from) { return Collections.unmodifiableSortedSet(super.tailSet(from)); }

		@Override
		public @NotNull ConcurrentSet<E> toUnmodifiable() {
			return this;
		}

	}

	/**
	 * Immutable snapshot of a {@link ConcurrentTreeMap} preserving the source's comparator and
	 * sort order. Reads are wait-free; mutating operations reject with
	 * {@link UnsupportedOperationException}.
	 *
	 * @param <K> the type of keys
	 * @param <V> the type of mapped values
	 */
	static final class UnmodifiableConcurrentTreeMap<K, V> extends ConcurrentTreeMap.Impl<K, V> {

		UnmodifiableConcurrentTreeMap(@NotNull TreeMap<K, V> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public @Nullable V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) { throw new UnsupportedOperationException(); }
		@Override public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V put(K key, V value) { throw new UnsupportedOperationException(); }
		@Override public void putAll(@NotNull Map<? extends K, ? extends V> map) { throw new UnsupportedOperationException(); }
		@Override public boolean putIf(@NotNull Supplier<Boolean> predicate, K key, V value) { throw new UnsupportedOperationException(); }
		@Override public boolean putIf(@NotNull BiPredicate<? super K, ? super V> predicate, K key, V value) { throw new UnsupportedOperationException(); }
		@Override public boolean putIf(@NotNull Predicate<TreeMap<K, V>> predicate, K key, V value) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V putIfAbsent(K key, V value) { throw new UnsupportedOperationException(); }
		@Override public @Nullable V remove(Object key) { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object key, Object value) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull BiPredicate<? super K, ? super V> predicate) { throw new UnsupportedOperationException(); }
		@Override public boolean removeIf(@NotNull Predicate<? super Entry<K, V>> predicate) { throw new UnsupportedOperationException(); }
		@Override public Map.Entry<K, V> pollFirstEntry() { throw new UnsupportedOperationException(); }
		@Override public Map.Entry<K, V> pollLastEntry() { throw new UnsupportedOperationException(); }

		@Override public @NotNull NavigableMap<K, V> descendingMap() { return Collections.unmodifiableNavigableMap(super.descendingMap()); }
		@Override public @NotNull NavigableSet<K> navigableKeySet() { return Collections.unmodifiableNavigableSet(super.navigableKeySet()); }
		@Override public @NotNull NavigableSet<K> descendingKeySet() { return Collections.unmodifiableNavigableSet(super.descendingKeySet()); }
		@Override public @NotNull NavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive) { return Collections.unmodifiableNavigableMap(super.subMap(from, fromInclusive, to, toInclusive)); }
		@Override public @NotNull SortedMap<K, V> subMap(K from, K to) { return Collections.unmodifiableSortedMap(super.subMap(from, to)); }
		@Override public @NotNull NavigableMap<K, V> headMap(K to, boolean inclusive) { return Collections.unmodifiableNavigableMap(super.headMap(to, inclusive)); }
		@Override public @NotNull SortedMap<K, V> headMap(K to) { return Collections.unmodifiableSortedMap(super.headMap(to)); }
		@Override public @NotNull NavigableMap<K, V> tailMap(K from, boolean inclusive) { return Collections.unmodifiableNavigableMap(super.tailMap(from, inclusive)); }
		@Override public @NotNull SortedMap<K, V> tailMap(K from) { return Collections.unmodifiableSortedMap(super.tailMap(from)); }

		@Override
		public @NotNull ConcurrentMap<K, V> toUnmodifiable() {
			return this;
		}

	}

}
