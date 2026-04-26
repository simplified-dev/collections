package dev.simplified.collection.linked;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe set backed by a {@link LinkedHashSet} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Maintains insertion order while
 * enforcing no-duplicate semantics.
 *
 * @param <E> the type of elements in this set
 */
public class ConcurrentLinkedSet<E> extends ConcurrentSet<E> {

	/**
	 * Create a new concurrent set.
	 */
	public ConcurrentLinkedSet() {
		super(new LinkedHashSet<>());
	}

	/**
	 * Create a new concurrent set and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentLinkedSet(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent set and fill it with the given collection.
	 */
	public ConcurrentLinkedSet(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new LinkedHashSet<>() : new LinkedHashSet<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentLinkedSet} with a pre-built backing set and an explicit
	 * lock. Used by {@link ConcurrentUnmodifiableLinkedSet} to install a snapshot set paired
	 * with a no-op lock for wait-free reads.
	 *
	 * @param backingSet the pre-built backing set
	 * @param lock the lock guarding {@code backingSet}
	 */
	protected ConcurrentLinkedSet(@NotNull LinkedHashSet<E> backingSet, @NotNull ReadWriteLock lock) {
		super(backingSet, lock);
	}

	/**
	 * Creates a new empty {@code ConcurrentLinkedSet} instance, used internally for copy operations.
	 *
	 * @return a new empty {@link ConcurrentLinkedSet}
	 */
	@Override
	protected @NotNull AtomicCollection<E, AbstractSet<E>> createEmpty() {
		return Concurrent.newLinkedSet();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Overrides {@link ConcurrentSet#cloneRef()} to produce a {@link LinkedHashSet} snapshot
	 * preserving the source's insertion-order traversal characteristics.</p>
	 */
	@Override
	protected @NotNull AbstractSet<E> cloneRef() {
		try {
			this.lock.readLock().lock();
			return new LinkedHashSet<>(this.ref);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentLinkedSet} preserving insertion order.
	 *
	 * @return an unmodifiable {@link ConcurrentLinkedSet} containing a snapshot of the elements
	 */
	@Override
	public @NotNull ConcurrentLinkedSet<E> toUnmodifiable() {
		return new ConcurrentUnmodifiableLinkedSet<>((LinkedHashSet<E>) this.cloneRef());
	}

}
