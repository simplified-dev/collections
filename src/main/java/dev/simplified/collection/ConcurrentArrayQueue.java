package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicQueue;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe FIFO {@link ConcurrentQueue} backed by an {@link ArrayDeque} with concurrent
 * access. Supports standard queue operations: offer, peek, poll, and element retrieval.
 *
 * <p><b>Null-element behavior:</b> {@link ArrayDeque} rejects {@code null} elements with a
 * {@link NullPointerException}, unlike {@link LinkedList} which accepts them. Callers migrating
 * from a {@code LinkedList}-backed queue must ensure no {@code null} values flow through this
 * queue.</p>
 *
 * <p>The queue exposes only the {@link ConcurrentQueue} surface; consumers cannot downcast to
 * {@link java.util.Deque}. Use {@link ConcurrentArrayDeque} when double-ended access is
 * required.</p>
 *
 * @param <E> the type of elements in this queue
 */
public class ConcurrentArrayQueue<E> extends AtomicQueue<E, ArrayDeque<E>> implements ConcurrentQueue<E> {

	/**
	 * Creates a new concurrent queue.
	 */
	public ConcurrentArrayQueue() {
		super(new ArrayDeque<>());
	}

	/**
	 * Creates a new concurrent queue and fills it with the given array.
	 *
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentArrayQueue(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Creates a new concurrent queue and fills it with the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty queue
	 */
	public ConcurrentArrayQueue(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new ArrayDeque<>() : new ArrayDeque<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentArrayQueue} that adopts {@code backingQueue} as its storage
	 * with a fresh lock.
	 *
	 * @param backingQueue the backing queue to adopt
	 */
	protected ConcurrentArrayQueue(@NotNull ArrayDeque<E> backingQueue) {
		super(backingQueue);
	}

	/**
	 * Constructs a {@code ConcurrentArrayQueue} with a pre-built backing queue and an explicit
	 * lock. Used by {@link ConcurrentUnmodifiableQueue.Impl} to install a snapshot queue paired
	 * with a no-op lock for wait-free reads.
	 *
	 * @param backingQueue the pre-built backing queue
	 * @param lock the lock guarding {@code backingQueue}
	 */
	protected ConcurrentArrayQueue(@NotNull ArrayDeque<E> backingQueue, @NotNull ReadWriteLock lock) {
		super(backingQueue, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentArrayQueue} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results.
	 *
	 * @param backing the queue to adopt
	 * @param <E> the element type
	 * @return a concurrent array queue backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentArrayQueue<E> adopt(@NotNull ArrayDeque<E> backing) {
		return new ConcurrentArrayQueue<>(backing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected @NotNull AtomicQueue<E, ArrayDeque<E>> newEmpty() {
		return new ConcurrentArrayQueue<>();
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentArrayQueue}.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations on
	 * this queue are not reflected in the snapshot.</p>
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentQueue<E> toUnmodifiable() {
		return new ConcurrentUnmodifiableQueue.Impl<>(this.withReadLock(() -> new LinkedList<>(this.ref)));
	}

}
