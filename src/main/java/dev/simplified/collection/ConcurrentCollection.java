package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe collection backed by an {@link AbstractCollection} with concurrent read and write access
 * via {@link ReadWriteLock}. Provides the base concrete implementation of {@link AtomicCollection}.
 *
 * @param <E> the type of elements in this collection
 */
public class ConcurrentCollection<E> extends AtomicCollection<E, AbstractCollection<E>> {

	/**
	 * Create a new concurrent set.
	 */
	public ConcurrentCollection() {
		super(new ArrayList<>());
	}

	/**
	 * Create a new concurrent set and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentCollection(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent set and fill it with the given collection.
	 */
	public ConcurrentCollection(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new ArrayList<>() : new ArrayList<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentCollection} with a pre-built backing collection and an
	 * explicit lock. Used by {@link ConcurrentUnmodifiableCollection} to install a snapshot
	 * collection paired with a no-op lock for wait-free reads.
	 *
	 * @param backingCollection the pre-built backing collection
	 * @param lock the lock guarding {@code backingCollection}
	 */
	protected ConcurrentCollection(@NotNull AbstractCollection<E> backingCollection, @NotNull ReadWriteLock lock) {
		super(backingCollection, lock);
	}

	/**
	 * Creates a new empty {@code ConcurrentCollection} instance, used internally for copy operations.
	 *
	 * @return a new empty {@link ConcurrentCollection}
	 */
	@Override
	protected final @NotNull AtomicCollection<E, AbstractCollection<E>> createEmpty() {
		return Concurrent.newCollection();
	}

	/**
	 * Returns a type-preserving snapshot of this collection's backing reference, captured under
	 * the read lock.
	 *
	 * @return a fresh {@link AbstractCollection} containing the current elements
	 */
	protected @NotNull AbstractCollection<E> cloneRef() {
		try {
			this.lock.readLock().lock();
			return new ArrayList<>(this.ref);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentCollection}.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current contents - subsequent mutations
	 * on this collection are not reflected in the snapshot. Reads on the snapshot are wait-free.
	 * The runtime type is {@link ConcurrentUnmodifiableCollection}; the declared return type
	 * is the mutable parent so subclasses can covariantly override to their own
	 * {@code ConcurrentUnmodifiable*} variant.</p>
	 *
	 * @return an immutable snapshot - runtime type is {@link ConcurrentUnmodifiableCollection}
	 */
	public @NotNull ConcurrentCollection<E> toUnmodifiable() {
		return new ConcurrentUnmodifiableCollection<>(this.cloneRef());
	}

}
