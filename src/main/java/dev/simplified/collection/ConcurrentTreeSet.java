package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.atomic.AtomicNavigableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link ConcurrentSet} backed by a {@link TreeSet} that maintains its elements in
 * sorted order according to their natural ordering or a {@link Comparator} provided at
 * construction time.
 *
 * @param <E> the type of elements in this set
 */
public class ConcurrentTreeSet<E> extends AtomicNavigableSet<E, TreeSet<E>> implements ConcurrentSet<E>, NavigableSet<E> {

	/**
	 * Creates a new concurrent sorted set with natural element ordering.
	 */
	public ConcurrentTreeSet() {
		super(new TreeSet<>());
	}

	/**
	 * Creates a new concurrent sorted set with the given comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 */
	public ConcurrentTreeSet(@NotNull Comparator<? super E> comparator) {
		super(new TreeSet<>(comparator));
	}

	/**
	 * Creates a new concurrent sorted set with natural element ordering and fills it with the
	 * given array.
	 *
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentTreeSet(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Creates a new concurrent sorted set with the given comparator and fills it with the given
	 * array.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentTreeSet(@NotNull Comparator<? super E> comparator, @NotNull E... array) {
		this(comparator, Arrays.asList(array));
	}

	/**
	 * Creates a new concurrent sorted set with natural element ordering and fills it with the
	 * given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty set
	 */
	public ConcurrentTreeSet(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new TreeSet<>() : new TreeSet<>(collection));
	}

	/**
	 * Creates a new concurrent sorted set with the given comparator and fills it with the given
	 * collection.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param collection the source collection to copy from
	 */
	public ConcurrentTreeSet(@NotNull Comparator<? super E> comparator, @Nullable Collection<? extends E> collection) {
		super(newTreeSet(comparator, collection));
	}

	/**
	 * Constructs a {@code ConcurrentTreeSet} that adopts {@code backingSet} as its storage with a
	 * fresh lock. Public callers should go through {@link #adopt(TreeSet)}.
	 *
	 * @param backingSet the backing tree set to adopt
	 */
	protected ConcurrentTreeSet(@NotNull TreeSet<E> backingSet) {
		super(backingSet);
	}

	/**
	 * Constructs a {@code ConcurrentTreeSet} with a pre-built backing set and an explicit lock.
	 * Used by {@code ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet} to install a snapshot
	 * set paired with a no-op lock for wait-free reads.
	 *
	 * @param backingSet the pre-built backing set
	 * @param lock the lock guarding {@code backingSet}
	 */
	protected ConcurrentTreeSet(@NotNull TreeSet<E> backingSet, @NotNull ReadWriteLock lock) {
		super(backingSet, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentTreeSet} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results. The adopted set's comparator is preserved.
	 *
	 * @param backing the tree set to adopt
	 * @param <E> the element type
	 * @return a concurrent tree set backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentTreeSet<E> adopt(@NotNull TreeSet<E> backing) {
		return new ConcurrentTreeSet<>(backing);
	}

	private static <E> @NotNull TreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator, @Nullable Collection<? extends E> collection) {
		TreeSet<E> set = new TreeSet<>(comparator);
		if (collection != null) set.addAll(collection);
		return set;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("DataFlowIssue")
	protected @NotNull AtomicCollection<E, TreeSet<E>> newEmpty() {
		return new ConcurrentTreeSet<>(this.ref.comparator());
	}

	/**
	 * Returns a {@link TreeSet}-typed snapshot of this set's backing reference, captured under
	 * the read lock. {@link TreeSet#TreeSet(java.util.SortedSet)} preserves the source's
	 * comparator when the source is itself a {@code SortedSet}.
	 *
	 * @return a fresh {@link TreeSet} containing the current elements
	 */
	protected @NotNull TreeSet<E> cloneRef() {
		return this.withReadLock(() -> new TreeSet<>(this.ref));
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentTreeSet} preserving the source's
	 * comparator and sort order.
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentSet<E> toUnmodifiable() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentTreeSet<>(this.cloneRef());
	}

}
