package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An immutable snapshot view of a {@link ConcurrentMap}. All mutating operations on
 * implementations of this interface reject with {@link UnsupportedOperationException}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface ConcurrentUnmodifiableMap<K, V> extends ConcurrentMap<K, V> {

	/**
	 * An immutable snapshot of a {@link ConcurrentMap.Impl}. The wrapper owns a fresh copy of the
	 * source's entries at construction time and never reflects subsequent mutations on the source.
	 * Reads on the snapshot are wait-free, backed by {@link NoOpReadWriteLock}.
	 *
	 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}, including
	 * those routed through view collections ({@code entrySet}, {@code keySet}, {@code values}),
	 * iterator removal, and {@code Entry.setValue}.</p>
	 *
	 * @param <K> the type of keys maintained by this map
	 * @param <V> the type of mapped values
	 */
	class Impl<K, V> extends ConcurrentMap.Impl<K, V> implements ConcurrentUnmodifiableMap<K, V> {

		/**
		 * Wraps the given pre-cloned snapshot reference. Callers should obtain {@code snapshot} by
		 * copying a source map's contents under that source's read lock.
		 *
		 * @param snapshot a freshly cloned backing map
		 */
		public Impl(@NotNull AbstractMap<K, V> snapshot) {
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
		public @NotNull ConcurrentUnmodifiableMap<K, V> toUnmodifiable() {
			return this;
		}

	}

}
