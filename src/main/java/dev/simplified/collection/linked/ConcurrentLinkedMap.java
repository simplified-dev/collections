package dev.simplified.collection.linked;

import dev.simplified.collection.ConcurrentMap;
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
			try {
				this.lock.readLock().lock();
				return new LinkedHashMap<>(this.ref);
			} finally {
				this.lock.readLock().unlock();
			}
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
