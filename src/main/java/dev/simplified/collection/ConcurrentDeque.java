package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicDeque;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableDeque;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.ReadWriteLock;

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
	 * Creates a new empty {@link ConcurrentDeque} backed by a {@link LinkedList}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent deque
	 */
	static <E> @NotNull ConcurrentDeque<E> empty() {
		return new Impl<E>();
	}

	/**
	 * Creates a new {@link ConcurrentDeque} containing the given elements.
	 *
	 * @param elements the elements to include
	 * @param <E> the element type
	 * @return a new concurrent deque containing the specified elements
	 */
	@SafeVarargs
	static <E> @NotNull ConcurrentDeque<E> of(@NotNull E... elements) {
		return new Impl<>(elements);
	}

	/**
	 * Creates a new {@link ConcurrentDeque} containing all elements of the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty deque
	 * @param <E> the element type
	 * @return a new concurrent deque containing the source's elements
	 */
	static <E> @NotNull ConcurrentDeque<E> from(@Nullable Collection<? extends E> collection) {
		return new Impl<E>(collection);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentDeque} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to
	 * {@code backing} bypass the read/write lock and may corrupt concurrent reads. Use this for
	 * zero-copy publication of single-threaded build results.
	 *
	 * @param backing the deque to adopt
	 * @param <E> the element type
	 * @return a concurrent deque backed by {@code backing}
	 */
	static <E> @NotNull ConcurrentDeque<E> adopt(@NotNull LinkedList<E> backing) {
		return new Impl<E>(backing);
	}

	/**
	 * A thread-safe double-ended queue backed by a {@link LinkedList} with concurrent access.
	 * Supports element insertion and removal at both ends with FIFO and LIFO semantics.
	 *
	 * @param <E> the type of elements in this deque
	 */
	class Impl<E> extends AtomicDeque<E, LinkedList<E>> {

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
		 * Constructs a {@code ConcurrentDeque.Impl} that adopts {@code backingDeque} as its storage
		 * with a fresh lock.
		 *
		 * @param backingDeque the backing deque to adopt
		 */
		protected Impl(@NotNull LinkedList<E> backingDeque) {
			super(backingDeque);
		}

		/**
		 * Constructs a {@code ConcurrentDeque.Impl} with a pre-built backing deque and an explicit
		 * lock. Used by {@link ConcurrentUnmodifiableDeque.Impl} to install a snapshot deque paired
		 * with a no-op lock for wait-free reads.
		 *
		 * @param backingDeque the pre-built backing deque
		 * @param lock the lock guarding {@code backingDeque}
		 */
		protected Impl(@NotNull LinkedList<E> backingDeque, @NotNull ReadWriteLock lock) {
			super(backingDeque, lock);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected @NotNull AtomicDeque<E, LinkedList<E>> newEmpty() {
			return new ConcurrentDeque.Impl<>();
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
			return new ConcurrentUnmodifiableDeque.Impl<>(this.withReadLock(() -> new LinkedList<>(this.ref)));
		}

	}

}
