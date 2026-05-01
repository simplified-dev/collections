package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link ConcurrentList} backed by an {@link ArrayList} with concurrent read and
 * write access via {@link ReadWriteLock}. Supports indexed access, sorting, and snapshot-based
 * iteration.
 *
 * @param <E> the type of elements in this list
 */
public class ConcurrentArrayList<E> extends AtomicList<E, List<E>> implements ConcurrentList<E> {

	/**
	 * Creates a new concurrent list.
	 */
	public ConcurrentArrayList() {
		super(new ArrayList<>());
	}

	/**
	 * Creates a new concurrent list and fills it with the given array.
	 *
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentArrayList(@NotNull E... array) {
		super(new ArrayList<>(Arrays.asList(array)));
	}

	/**
	 * Creates a new concurrent list with an initial capacity.
	 *
	 * @param initialCapacity the initial capacity of the backing list
	 */
	public ConcurrentArrayList(int initialCapacity) {
		super(new ArrayList<>(initialCapacity));
	}

	/**
	 * Creates a new concurrent list and fills it with the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty list
	 */
	public ConcurrentArrayList(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new ArrayList<>() : new ArrayList<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentArrayList} that adopts {@code backingList} as its storage with a
	 * fresh lock. Public callers should go through {@link #adopt(List)}.
	 *
	 * @param backingList the backing list to adopt
	 */
	protected ConcurrentArrayList(@NotNull List<E> backingList) {
		super(backingList);
	}

	/**
	 * Constructs a {@code ConcurrentArrayList} with a pre-built backing list and an explicit lock.
	 * Used by {@code ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList} (and its variants) to
	 * install a snapshot list paired with a no-op lock for wait-free reads.
	 *
	 * @param backingList the pre-built backing list
	 * @param lock the lock guarding {@code backingList}
	 */
	protected ConcurrentArrayList(@NotNull List<E> backingList, @NotNull ReadWriteLock lock) {
		super(backingList, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentArrayList} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results.
	 *
	 * @param backing the list to adopt
	 * @param <E> the element type
	 * @return a concurrent array list backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentArrayList<E> adopt(@NotNull List<E> backing) {
		return new ConcurrentArrayList<>(backing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected @NotNull AtomicList<E, List<E>> newEmpty() {
		return new ConcurrentArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected @NotNull AtomicList<E, List<E>> adoptSnapshot(@NotNull List<E> snapshot) {
		return new ConcurrentArrayList<>(snapshot);
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentArrayList}.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations on
	 * this list are not reflected in the snapshot. Reads on the snapshot are wait-free.</p>
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentList<E> toUnmodifiable() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentArrayList<>(this.snapshot());
	}

}
