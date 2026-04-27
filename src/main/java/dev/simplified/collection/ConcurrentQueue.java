package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicQueue;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

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
	 * A thread-safe FIFO queue backed by a {@link ConcurrentLinkedList.Impl} with concurrent
	 * access. Supports standard queue operations: offer, peek, poll, and element retrieval.
	 *
	 * @param <E> the type of elements in this queue
	 */
	class Impl<E> extends AtomicQueue<E> implements ConcurrentQueue<E> {

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
		 * Constructs a {@code ConcurrentQueue.Impl} with a pre-built backing storage. Used by
		 * {@link ConcurrentUnmodifiableQueue.Impl} to install snapshot storage.
		 *
		 * @param storage the pre-built backing storage
		 */
		protected Impl(@NotNull ConcurrentLinkedList.Impl<E> storage) {
			super(storage);
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
			return new ConcurrentUnmodifiableQueue.Impl<>(this);
		}

	}

}
