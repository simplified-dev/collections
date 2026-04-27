package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicQueue;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link Queue} extension exposing the project-specific concurrent surface for
 * FIFO queue variants.
 *
 * <p>Implementations carry atomic FIFO semantics with element-ordering guarantees on top of the
 * standard JDK {@link Queue} contract.</p>
 *
 * @param <E> the type of elements in this queue
 */
public interface ConcurrentQueue<E> extends ConcurrentCollection<E>, Queue<E> {

	/**
	 * Returns an immutable snapshot of this queue.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current contents, so subsequent mutations
	 * on this queue are not reflected in the snapshot.</p>
	 *
	 * @return an immutable snapshot of this queue
	 */
	@Override
	@NotNull ConcurrentUnmodifiableQueue<E> toUnmodifiable();

	/**
	 * A thread-safe FIFO queue backed by a {@link LinkedList} with concurrent access. Supports
	 * standard queue operations: offer, peek, poll, and element retrieval.
	 *
	 * @param <E> the type of elements in this queue
	 */
	class Impl<E> extends AtomicQueue<E, LinkedList<E>> {

		/**
		 * Creates a new concurrent queue.
		 */
		public Impl() {
			super(new LinkedList<>());
		}

		/**
		 * Creates a new concurrent queue and fills it with the given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			this(Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent queue and fills it with the given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty queue
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new LinkedList<>() : new LinkedList<>(collection));
		}

		/**
		 * Constructs a {@code ConcurrentQueue.Impl} that adopts {@code backingQueue} as its storage
		 * with a fresh lock.
		 *
		 * @param backingQueue the backing queue to adopt
		 */
		protected Impl(@NotNull LinkedList<E> backingQueue) {
			super(backingQueue);
		}

		/**
		 * Constructs a {@code ConcurrentQueue.Impl} with a pre-built backing queue and an explicit
		 * lock. Used by {@link ConcurrentUnmodifiableQueue.Impl} to install a snapshot queue paired
		 * with a no-op lock for wait-free reads.
		 *
		 * @param backingQueue the pre-built backing queue
		 * @param lock the lock guarding {@code backingQueue}
		 */
		protected Impl(@NotNull LinkedList<E> backingQueue, @NotNull ReadWriteLock lock) {
			super(backingQueue, lock);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected @NotNull AtomicQueue<E, LinkedList<E>> newEmpty() {
			return new ConcurrentQueue.Impl<>();
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentQueue.Impl}.
		 *
		 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations
		 * on this queue are not reflected in the snapshot.</p>
		 *
		 * @return an unmodifiable {@link ConcurrentQueue.Impl} containing a snapshot of the elements
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableQueue<E> toUnmodifiable() {
			LinkedList<E> snapshot;

			try {
				this.lock.readLock().lock();
				snapshot = new LinkedList<>(this.ref);
			} finally {
				this.lock.readLock().unlock();
			}

			return new ConcurrentUnmodifiableQueue.Impl<>(snapshot);
		}

	}

}
