package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicDeque;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableDeque;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe double-ended {@link ConcurrentDeque} backed by an {@link ArrayDeque} with
 * concurrent access. Supports element insertion and removal at both ends with FIFO and LIFO
 * semantics.
 *
 * <p><b>Null-element behavior:</b> {@link ArrayDeque} rejects {@code null} elements with a
 * {@link NullPointerException}, unlike {@link java.util.LinkedList} which accepts them. Callers
 * migrating from a {@code LinkedList}-backed deque must ensure no {@code null} values flow
 * through this deque.</p>
 *
 * @param <E> the type of elements in this deque
 */
public class ConcurrentArrayDeque<E> extends AtomicDeque<E, ArrayDeque<E>> implements ConcurrentDeque<E> {

	/**
	 * Creates a new concurrent deque.
	 */
	public ConcurrentArrayDeque() {
		super(new ArrayDeque<>());
	}

	/**
	 * Creates a new concurrent deque and fills it with the given array.
	 *
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentArrayDeque(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Creates a new concurrent deque and fills it with the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty deque
	 */
	public ConcurrentArrayDeque(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new ArrayDeque<>() : new ArrayDeque<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentArrayDeque} that adopts {@code backingDeque} as its storage
	 * with a fresh lock.
	 *
	 * @param backingDeque the backing deque to adopt
	 */
	protected ConcurrentArrayDeque(@NotNull ArrayDeque<E> backingDeque) {
		super(backingDeque);
	}

	/**
	 * Constructs a {@code ConcurrentArrayDeque} with a pre-built backing deque and an explicit
	 * lock. Used by {@link ConcurrentUnmodifiableDeque.Impl} to install a snapshot deque paired
	 * with a no-op lock for wait-free reads.
	 *
	 * @param backingDeque the pre-built backing deque
	 * @param lock the lock guarding {@code backingDeque}
	 */
	protected ConcurrentArrayDeque(@NotNull ArrayDeque<E> backingDeque, @NotNull ReadWriteLock lock) {
		super(backingDeque, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentArrayDeque} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results.
	 *
	 * @param backing the deque to adopt
	 * @param <E> the element type
	 * @return a concurrent array deque backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentArrayDeque<E> adopt(@NotNull ArrayDeque<E> backing) {
		return new ConcurrentArrayDeque<>(backing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected @NotNull AtomicDeque<E, ArrayDeque<E>> newEmpty() {
		return new ConcurrentArrayDeque<>();
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentArrayDeque}.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations on
	 * this deque are not reflected in the snapshot.</p>
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentDeque<E> toUnmodifiable() {
		return new ConcurrentUnmodifiableDeque.Impl<>(this.withReadLock(() -> new ArrayDeque<>(this.ref)));
	}

}
