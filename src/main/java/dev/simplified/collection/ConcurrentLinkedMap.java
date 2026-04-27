package dev.simplified.collection;

import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe concurrent map variant backed by an insertion-ordered {@link LinkedHashMap} that
 * preserves the source's insertion-order traversal characteristics.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface ConcurrentLinkedMap<K, V> extends ConcurrentMap<K, V> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableLinkedMap<K, V> toUnmodifiable();

	/**
	 * Creates a new empty {@link ConcurrentLinkedMap} backed by a {@link LinkedHashMap}.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new empty concurrent linked map
	 */
	static <K, V> @NotNull ConcurrentLinkedMap<K, V> empty() {
		return new Impl<K, V>();
	}

	/**
	 * Creates a new {@link ConcurrentLinkedMap} containing all entries of the given map.
	 *
	 * @param map the source map to copy from, or {@code null} for an empty map
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new concurrent linked map containing the source's entries
	 */
	static <K, V> @NotNull ConcurrentLinkedMap<K, V> from(@Nullable Map<? extends K, ? extends V> map) {
		return new Impl<K, V>(map);
	}

	/**
	 * Creates a new empty {@link ConcurrentLinkedMap} with the given maximum size; entries
	 * exceeding that size evict the eldest insertion-ordered entry.
	 *
	 * @param maxSize the maximum number of entries to retain
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new size-capped concurrent linked map
	 */
	static <K, V> @NotNull ConcurrentLinkedMap<K, V> withMaxSize(int maxSize) {
		return new Impl<K, V>(maxSize);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedMap} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to
	 * {@code backing} bypass the read/write lock and may corrupt concurrent reads. Use this for
	 * zero-copy publication of single-threaded build results. The eldest-entry eviction cap of
	 * the in-class {@code MaxSizeLinkedMap} is not applied to externally adopted backing maps.
	 *
	 * @param backing the linked hash map to adopt
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a concurrent linked map backed by {@code backing}
	 */
	static <K, V> @NotNull ConcurrentLinkedMap<K, V> adopt(@NotNull LinkedHashMap<K, V> backing) {
		return new Impl<>(backing);
	}

	/**
	 * A thread-safe map backed by an insertion-ordered {@link LinkedHashMap} with concurrent read
	 * and write access via {@link ReadWriteLock}. Supports an optional maximum size with
	 * eldest-entry eviction.
	 *
	 * @param <K> the type of keys maintained by this map
	 * @param <V> the type of mapped values
	 */
	class Impl<K, V> extends ConcurrentMap.Impl<K, V> implements ConcurrentLinkedMap<K, V> {

		/**
		 * Creates a new concurrent linked map.
		 */
		public Impl() {
			super(new MaxSizeLinkedMap<>(), (Map<K, V>) null);
		}

		/**
		 * Creates a new concurrent linked map with the given maximum size.
		 *
		 * @param maxSize the maximum number of entries allowed in the map
		 */
		public Impl(int maxSize) {
			super(new MaxSizeLinkedMap<>(maxSize), (Map<K, V>) null);
		}

		/**
		 * Creates a new concurrent linked map and fills it with the given map.
		 *
		 * @param map the source map to copy from
		 */
		public Impl(@Nullable Map<? extends K, ? extends V> map) {
			super(new MaxSizeLinkedMap<>(), map);
		}

		/**
		 * Creates a new concurrent linked map and fills it with the given map.
		 *
		 * @param map the source map to copy from
		 * @param maxSize the maximum number of entries allowed in the map
		 */
		public Impl(@Nullable Map<? extends K, ? extends V> map, int maxSize) {
			super(new MaxSizeLinkedMap<>(maxSize), map);
		}

		/**
		 * Constructs a {@code ConcurrentLinkedMap.Impl} that adopts {@code backingMap} as its
		 * storage with a fresh lock. Public callers should go through
		 * {@link ConcurrentLinkedMap#adopt(LinkedHashMap)}.
		 *
		 * @param backingMap the backing linked hash map to adopt
		 */
		protected Impl(@NotNull LinkedHashMap<K, V> backingMap) {
			super(backingMap);
		}

		/**
		 * Constructs a {@code ConcurrentLinkedMap.Impl} with a pre-built backing map and an
		 * explicit lock. Used by {@link ConcurrentUnmodifiableLinkedMap.Impl} to install a snapshot
		 * map paired with a no-op lock for wait-free reads.
		 *
		 * @param backingMap the pre-built backing map
		 * @param lock the lock guarding {@code backingMap}
		 */
		protected Impl(@NotNull LinkedHashMap<K, V> backingMap, @NotNull ReadWriteLock lock) {
			super(backingMap, lock);
		}

		/**
		 * {@inheritDoc}
		 *
		 * <p>Overrides {@link ConcurrentMap.Impl#cloneRef()} to produce a {@link LinkedHashMap}
		 * snapshot preserving the source's insertion-order traversal characteristics. The
		 * eldest-entry eviction cap is intentionally not carried into the snapshot - the snapshot
		 * is immutable, so eviction is moot.</p>
		 */
		@Override
		protected @NotNull AbstractMap<K, V> cloneRef() {
			return this.withReadLock(() -> new LinkedHashMap<>(this.ref));
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentLinkedMap.Impl} preserving
		 * insertion order.
		 *
		 * @return an unmodifiable {@link ConcurrentLinkedMap.Impl} containing a snapshot of the
		 *         entries
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableLinkedMap<K, V> toUnmodifiable() {
			return new ConcurrentUnmodifiableLinkedMap.Impl<>((LinkedHashMap<K, V>) this.cloneRef());
		}

		/**
		 * A {@link LinkedHashMap} that evicts the eldest entry once its size exceeds a fixed cap,
		 * or never evicts when the cap is {@code -1}. Final + private: the class cannot be
		 * subclassed and the containing map is the sole caller, so {@link #removeEldestEntry} is
		 * guaranteed to execute under the enclosing {@link ConcurrentMap.Impl}'s write lock.
		 *
		 * @param <K> the type of keys
		 * @param <V> the type of values
		 */
		private static final class MaxSizeLinkedMap<K, V> extends LinkedHashMap<K, V> {

			private final int maxSize;

			MaxSizeLinkedMap() {
				this(-1);
			}

			MaxSizeLinkedMap(int maxSize) {
				this.maxSize = maxSize;
			}

			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return this.maxSize != -1 && this.size() > this.maxSize;
			}

		}

	}

}
