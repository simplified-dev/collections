package dev.simplified.collection.linked;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe concurrent list variant backed by a {@link LinkedList} that preserves the
 * source's insertion-order traversal characteristics.
 *
 * @param <E> the type of elements in this list
 */
public interface ConcurrentLinkedList<E> extends ConcurrentList<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableLinkedList<E> toUnmodifiable();

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedList} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to
	 * {@code backing} bypass the read/write lock and may corrupt concurrent reads. Use this for
	 * zero-copy publication of single-threaded build results.
	 *
	 * @param backing the linked list to adopt
	 * @param <E> the element type
	 * @return a concurrent linked list backed by {@code backing}
	 */
	static <E> @NotNull ConcurrentLinkedList<E> adopt(@NotNull LinkedList<E> backing) {
		return new Impl<>(backing);
	}

	/**
	 * A thread-safe list backed by a {@link LinkedList} with concurrent read and write access via
	 * {@link ReadWriteLock}. Supports indexed access, sorting, and snapshot-based iteration with
	 * linked-list insertion characteristics.
	 *
	 * @param <E> the type of elements in this list
	 */
	class Impl<E> extends ConcurrentList.Impl<E> implements ConcurrentLinkedList<E> {

		/**
		 * Creates a new concurrent linked list.
		 */
		public Impl() {
			super(new LinkedList<>());
		}

		/**
		 * Creates a new concurrent linked list and fills it with the given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			this(Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent linked list and fills it with the given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty list
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new LinkedList<>() : new LinkedList<>(collection));
		}

		/**
		 * Constructs a {@code ConcurrentLinkedList.Impl} that adopts {@code backingList} as its
		 * storage with a fresh lock. Public callers should go through
		 * {@link ConcurrentLinkedList#adopt(LinkedList)}.
		 *
		 * @param backingList the backing linked list to adopt
		 */
		protected Impl(@NotNull LinkedList<E> backingList) {
			super(backingList);
		}

		/**
		 * Constructs a {@code ConcurrentLinkedList.Impl} with a pre-built backing list and an
		 * explicit lock. Used by {@link ConcurrentUnmodifiableLinkedList.Impl} to install a
		 * snapshot list paired with a no-op lock for wait-free reads.
		 *
		 * @param backingList the pre-built backing list
		 * @param lock the lock guarding {@code backingList}
		 */
		protected Impl(@NotNull LinkedList<E> backingList, @NotNull ReadWriteLock lock) {
			super(backingList, lock);
		}

		/**
		 * {@inheritDoc}
		 *
		 * <p>Overrides {@link ConcurrentList.Impl#snapshot()} to produce a {@link LinkedList}
		 * snapshot preserving the source's insertion-order traversal characteristics.</p>
		 */
		@Override
		public @NotNull List<E> snapshot() {
			try {
				this.lock.readLock().lock();
				return new LinkedList<>(this.ref);
			} finally {
				this.lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public @NotNull ConcurrentLinkedList<E> newEmpty() {
			return Concurrent.newLinkedList();
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentLinkedList.Impl} preserving
		 * insertion order.
		 *
		 * @return an unmodifiable {@link ConcurrentLinkedList.Impl} containing a snapshot of the
		 *         elements
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableLinkedList<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableLinkedList.Impl<>((LinkedList<E>) this.snapshot());
		}

	}

}
