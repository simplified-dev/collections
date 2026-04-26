package dev.simplified.collection.tree;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableTreeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe set backed by a {@link TreeSet} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Maintains element ordering defined
 * by a {@link Comparator} or the elements' natural ordering.
 *
 * @param <E> the type of elements in this set
 */
public class ConcurrentTreeSet<E> extends ConcurrentSet<E> {

	/**
	 * Create a new concurrent sorted set with natural element ordering.
	 */
	public ConcurrentTreeSet() {
		super(new TreeSet<>());
	}

	/**
	 * Create a new concurrent sorted set with the given comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 */
	public ConcurrentTreeSet(@NotNull Comparator<? super E> comparator) {
		super(new TreeSet<>(comparator));
	}

	/**
	 * Create a new concurrent sorted set with natural element ordering and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentTreeSet(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent sorted set with the given comparator and fill it with the given array.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentTreeSet(@NotNull Comparator<? super E> comparator, @NotNull E... array) {
		this(comparator, Arrays.asList(array));
	}

	/**
	 * Create a new concurrent sorted set with natural element ordering and fill it with the given collection.
	 */
	public ConcurrentTreeSet(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new TreeSet<>() : new TreeSet<>(collection));
	}

	/**
	 * Create a new concurrent sorted set with the given comparator and fill it with the given collection.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param collection the source collection to copy from
	 */
	public ConcurrentTreeSet(@NotNull Comparator<? super E> comparator, @Nullable Collection<? extends E> collection) {
		super(newTreeSet(comparator, collection));
	}

	/**
	 * Constructs a {@code ConcurrentTreeSet} with a pre-built backing set and an explicit
	 * lock. Used by {@link ConcurrentUnmodifiableTreeSet} to install a snapshot set paired
	 * with a no-op lock for wait-free reads.
	 *
	 * @param backingSet the pre-built backing set
	 * @param lock the lock guarding {@code backingSet}
	 */
	protected ConcurrentTreeSet(@NotNull TreeSet<E> backingSet, @NotNull ReadWriteLock lock) {
		super(backingSet, lock);
	}

	/**
	 * Creates a new {@link TreeSet} with the given comparator, populated from the collection.
	 */
	private static <E> @NotNull TreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator, @Nullable Collection<? extends E> collection) {
		TreeSet<E> set = new TreeSet<>(comparator);
		if (collection != null) set.addAll(collection);
		return set;
	}

	/**
	 * Creates a new empty {@code ConcurrentSortedSet} instance, used internally for copy operations.
	 *
	 * @return a new empty {@link ConcurrentTreeSet}
	 */
	@Override
	protected @NotNull AtomicCollection<E, AbstractSet<E>> createEmpty() {
		return Concurrent.newSortedSet();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Overrides {@link ConcurrentSet#cloneRef()} to produce a {@link TreeSet} snapshot.
	 * {@link TreeSet#TreeSet(java.util.SortedSet)} preserves the source's comparator when
	 * the source is itself a {@code SortedSet}.</p>
	 */
	@Override
	protected @NotNull AbstractSet<E> cloneRef() {
		try {
			this.lock.readLock().lock();
			return new TreeSet<>((TreeSet<E>) this.ref);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentTreeSet} preserving the source's
	 * comparator and sort order.
	 *
	 * @return an unmodifiable {@link ConcurrentTreeSet} containing a snapshot of the elements
	 */
	@Override
	public @NotNull ConcurrentTreeSet<E> toUnmodifiable() {
		return new ConcurrentUnmodifiableTreeSet<>((TreeSet<E>) this.cloneRef());
	}

}
