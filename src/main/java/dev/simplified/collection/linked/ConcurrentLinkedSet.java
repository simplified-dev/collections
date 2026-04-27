package dev.simplified.collection.linked;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe concurrent set variant backed by a {@link LinkedHashSet} that maintains insertion
 * order while enforcing no-duplicate semantics.
 *
 * @param <E> the type of elements in this set
 */
public interface ConcurrentLinkedSet<E> extends ConcurrentSet<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableLinkedSet<E> toUnmodifiable();

	/**
	 * A thread-safe set backed by a {@link LinkedHashSet} with concurrent read and write access via
	 * {@link ReadWriteLock}. Maintains insertion order while enforcing no-duplicate semantics.
	 *
	 * @param <E> the type of elements in this set
	 */
	class Impl<E> extends ConcurrentSet.Impl<E> implements ConcurrentLinkedSet<E> {

		/**
		 * Creates a new concurrent linked set.
		 */
		public Impl() {
			super(new LinkedHashSet<>());
		}

		/**
		 * Creates a new concurrent linked set and fills it with the given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			this(Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent linked set and fills it with the given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty set
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new LinkedHashSet<>() : new LinkedHashSet<>(collection));
		}

		/**
		 * Constructs a {@code ConcurrentLinkedSet.Impl} with a pre-built backing set and an
		 * explicit lock. Used by {@link ConcurrentUnmodifiableLinkedSet.Impl} to install a snapshot
		 * set paired with a no-op lock for wait-free reads.
		 *
		 * @param backingSet the pre-built backing set
		 * @param lock the lock guarding {@code backingSet}
		 */
		protected Impl(@NotNull LinkedHashSet<E> backingSet, @NotNull ReadWriteLock lock) {
			super(backingSet, lock);
		}

		/**
		 * Creates a new empty {@code ConcurrentLinkedSet.Impl} instance, used internally for copy
		 * operations.
		 *
		 * @return a new empty {@link ConcurrentLinkedSet.Impl}
		 */
		@Override
		protected @NotNull AtomicCollection<E, AbstractSet<E>> createEmpty() {
			return (ConcurrentLinkedSet.Impl<E>) Concurrent.newLinkedSet();
		}

		/**
		 * {@inheritDoc}
		 *
		 * <p>Overrides {@link ConcurrentSet.Impl#cloneRef()} to produce a {@link LinkedHashSet}
		 * snapshot preserving the source's insertion-order traversal characteristics.</p>
		 */
		@Override
		protected @NotNull AbstractSet<E> cloneRef() {
			try {
				this.lock.readLock().lock();
				return new LinkedHashSet<>(this.ref);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentLinkedSet.Impl} preserving
		 * insertion order.
		 *
		 * @return an unmodifiable {@link ConcurrentLinkedSet.Impl} containing a snapshot of the
		 *         elements
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableLinkedSet<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableLinkedSet.Impl<>((LinkedHashSet<E>) this.cloneRef());
		}

	}

}
