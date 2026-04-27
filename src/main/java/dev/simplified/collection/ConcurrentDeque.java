package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicDeque;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableDeque;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

/**
 * A thread-safe {@link Deque} extension combining the {@link ConcurrentQueue} surface with the
 * double-ended semantics of {@link Deque}.
 *
 * @param <E> the type of elements in this deque
 */
public interface ConcurrentDeque<E> extends ConcurrentQueue<E>, Deque<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableDeque<E> toUnmodifiable();

	/**
	 * A thread-safe double-ended queue backed by a {@link ConcurrentLinkedList.Impl} with
	 * concurrent access. Supports element insertion and removal at both ends with FIFO and LIFO
	 * semantics.
	 *
	 * @param <E> the type of elements in this deque
	 */
	class Impl<E> extends AtomicDeque<E> implements ConcurrentDeque<E> {

		/**
		 * Creates a new concurrent deque.
		 */
		public Impl() {
			super(new LinkedList<>());
		}

		/**
		 * Creates a new concurrent deque and fills it with the given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			this(Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent deque and fills it with the given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty deque
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new LinkedList<>() : new LinkedList<>(collection));
		}

		/**
		 * Constructs a {@code ConcurrentDeque.Impl} with a pre-built backing storage. Used by
		 * {@link ConcurrentUnmodifiableDeque.Impl} to install snapshot storage.
		 *
		 * @param storage the pre-built backing storage
		 */
		protected Impl(@NotNull ConcurrentLinkedList.Impl<E> storage) {
			super(storage);
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentDeque.Impl}.
		 *
		 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations
		 * on this deque are not reflected in the snapshot.</p>
		 *
		 * @return an unmodifiable {@link ConcurrentDeque.Impl} containing a snapshot of the elements
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableDeque<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableDeque.Impl<>(this);
		}

	}

}
