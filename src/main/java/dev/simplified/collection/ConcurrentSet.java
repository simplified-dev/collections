package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.atomic.AtomicSet;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe set backed by a {@link HashSet} with concurrent read and write access
 * via {@link ReadWriteLock}. Enforces no-duplicate semantics with snapshot-based iteration.
 *
 * @param <E> the type of elements in this set
 */
public class ConcurrentSet<E> extends AtomicSet<E, AbstractSet<E>> {

	/**
	 * Create a new concurrent set.
	 */
	public ConcurrentSet() {
		super(new HashSet<>());
	}

	/**
	 * Create a new concurrent set and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentSet(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent set and fill it with the given collection.
	 */
	public ConcurrentSet(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new HashSet<>() : new HashSet<>(collection));
	}

	/**
	 * Create a new concurrent set with the given backing set.
	 *
	 * @param backingSet the backing set implementation
	 */
	protected ConcurrentSet(@NotNull AbstractSet<E> backingSet) {
		super(backingSet);
	}

	/**
	 * Constructs a {@code ConcurrentSet} with a pre-built backing set and an explicit lock.
	 * Used by {@link ConcurrentUnmodifiableSet} (and its variants) to install a snapshot set
	 * paired with a no-op lock for wait-free reads.
	 *
	 * @param backingSet the pre-built backing set
	 * @param lock the lock guarding {@code backingSet}
	 */
	protected ConcurrentSet(@NotNull AbstractSet<E> backingSet, @NotNull ReadWriteLock lock) {
		super(backingSet, lock);
	}

	/**
	 * Creates a new empty {@code ConcurrentSet} instance, used internally for copy operations.
	 *
	 * @return a new empty {@link ConcurrentSet}
	 */
	@Override
	protected @NotNull AtomicCollection<E, AbstractSet<E>> createEmpty() {
		return Concurrent.newSet();
	}

	/**
	 * Returns a type-preserving snapshot of this set's backing reference, captured under the
	 * read lock. Subclasses backed by a different concrete {@link AbstractSet} implementation
	 * override this to return an instance of that type so iteration order is preserved on
	 * the snapshot.
	 *
	 * @return a fresh {@link AbstractSet} containing the current elements
	 */
	protected @NotNull AbstractSet<E> cloneRef() {
		try {
			this.lock.readLock().lock();
			return new HashSet<>(this.ref);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentSet}.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations
	 * on this set are not reflected in the snapshot. Reads on the snapshot are wait-free.
	 * The runtime type is {@link ConcurrentUnmodifiableSet}; the declared return type is the
	 * mutable parent so subclasses can covariantly override to their own
	 * {@code ConcurrentUnmodifiable*} variant.</p>
	 *
	 * @return an immutable snapshot - runtime type is {@link ConcurrentUnmodifiableSet}
	 */
	public @NotNull ConcurrentSet<E> toUnmodifiable() {
		return new ConcurrentUnmodifiableSet<>(this.cloneRef());
	}

}
