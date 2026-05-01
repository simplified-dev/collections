package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link ConcurrentList} backed by a {@link LinkedList} that preserves the source's
 * insertion-order traversal characteristics. Mirrors the JDK relationship between
 * {@link java.util.LinkedList} and {@link java.util.ArrayList} - both are siblings of
 * {@link AtomicList}, neither extends the other.
 *
 * @param <E> the type of elements in this list
 */
public class ConcurrentLinkedList<E> extends AtomicList<E, List<E>> implements ConcurrentList<E> {

	/**
	 * Creates a new concurrent linked list.
	 */
	public ConcurrentLinkedList() {
		super(new LinkedList<>());
	}

	/**
	 * Creates a new concurrent linked list and fills it with the given array.
	 *
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentLinkedList(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Creates a new concurrent linked list and fills it with the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty list
	 */
	public ConcurrentLinkedList(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new LinkedList<>() : new LinkedList<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentLinkedList} that adopts {@code backingList} as its storage
	 * with a fresh lock. Public callers should go through {@link #adopt(LinkedList)}.
	 *
	 * @param backingList the backing linked list to adopt
	 */
	protected ConcurrentLinkedList(@NotNull LinkedList<E> backingList) {
		super(backingList);
	}

	/**
	 * Constructs a {@code ConcurrentLinkedList} with a pre-built backing list and an explicit
	 * lock. Used by {@code ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList} to install a
	 * snapshot list paired with a no-op lock for wait-free reads.
	 *
	 * @param backingList the pre-built backing list
	 * @param lock the lock guarding {@code backingList}
	 */
	protected ConcurrentLinkedList(@NotNull LinkedList<E> backingList, @NotNull ReadWriteLock lock) {
		super(backingList, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedList} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results.
	 *
	 * @param backing the linked list to adopt
	 * @param <E> the element type
	 * @return a concurrent linked list backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentLinkedList<E> adopt(@NotNull LinkedList<E> backing) {
		return new ConcurrentLinkedList<>(backing);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Produces a {@link LinkedList} snapshot preserving the source's insertion-order traversal
	 * characteristics.</p>
	 */
	@Override
	protected @NotNull List<E> snapshot() {
		return this.withReadLock(() -> new LinkedList<>(this.ref));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected @NotNull AtomicList<E, List<E>> newEmpty() {
		return new ConcurrentLinkedList<>();
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentLinkedList} preserving insertion
	 * order.
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentList<E> toUnmodifiable() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentLinkedList<>((LinkedList<E>) this.snapshot());
	}

}
