package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.tree.ConcurrentTreeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An immutable snapshot view of a {@link ConcurrentTreeMap}. All mutating operations on
 * implementations of this interface reject with {@link UnsupportedOperationException}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface ConcurrentUnmodifiableTreeMap<K, V> extends ConcurrentTreeMap<K, V>, ConcurrentUnmodifiableMap<K, V> {

	/**
	 * An immutable snapshot of a {@link ConcurrentTreeMap.Impl} preserving the source's comparator
	 * and sort order. The wrapper owns a fresh {@link TreeMap} copy and never reflects subsequent
	 * mutations on the source. Reads on the snapshot are wait-free, backed by
	 * {@link NoOpReadWriteLock}.
	 *
	 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}.</p>
	 *
	 * @param <K> the type of keys maintained by this map
	 * @param <V> the type of mapped values
	 */
	class Impl<K, V> extends ConcurrentTreeMap.Impl<K, V> implements ConcurrentUnmodifiableTreeMap<K, V> {

		/**
		 * Wraps the given pre-cloned snapshot reference. Callers should obtain {@code snapshot} by
		 * copying a source map's entries under that source's read lock.
		 *
		 * @param snapshot a freshly cloned backing map
		 */
		public Impl(@NotNull TreeMap<K, V> snapshot) {
			super(snapshot, NoOpReadWriteLock.INSTANCE);
		}

		/** {@inheritDoc} */
		@Override
		public final void clear() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final @Nullable V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final @Nullable V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final @Nullable V put(K key, V value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final void putAll(@NotNull Map<? extends K, ? extends V> map) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean putIf(@NotNull Supplier<Boolean> predicate, K key, V value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean putIf(@NotNull BiPredicate<? super K, ? super V> predicate, K key, V value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean putIf(@NotNull Predicate<AbstractMap<K, V>> predicate, K key, V value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final @Nullable V putIfAbsent(K key, V value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final @Nullable V remove(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean remove(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean removeIf(@NotNull BiPredicate<? super K, ? super V> predicate) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean removeIf(@NotNull Predicate<? super Entry<K, V>> predicate) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final Map.Entry<K, V> pollFirstEntry() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final Map.Entry<K, V> pollLastEntry() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableMap<K, V> descendingMap() {
			return Collections.unmodifiableNavigableMap(super.descendingMap());
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableSet<K> navigableKeySet() {
			return Collections.unmodifiableNavigableSet(super.navigableKeySet());
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableSet<K> descendingKeySet() {
			return Collections.unmodifiableNavigableSet(super.descendingKeySet());
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive) {
			return Collections.unmodifiableNavigableMap(super.subMap(from, fromInclusive, to, toInclusive));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull SortedMap<K, V> subMap(K from, K to) {
			return Collections.unmodifiableSortedMap(super.subMap(from, to));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableMap<K, V> headMap(K to, boolean inclusive) {
			return Collections.unmodifiableNavigableMap(super.headMap(to, inclusive));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull SortedMap<K, V> headMap(K to) {
			return Collections.unmodifiableSortedMap(super.headMap(to));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull NavigableMap<K, V> tailMap(K from, boolean inclusive) {
			return Collections.unmodifiableNavigableMap(super.tailMap(from, inclusive));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull SortedMap<K, V> tailMap(K from) {
			return Collections.unmodifiableSortedMap(super.tailMap(from));
		}

		/** {@inheritDoc} */
		@Override
		public @NotNull ConcurrentUnmodifiableTreeMap<K, V> toUnmodifiable() {
			return this;
		}

	}

}
