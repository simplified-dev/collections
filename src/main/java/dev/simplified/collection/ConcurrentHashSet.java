package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.atomic.AtomicSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link ConcurrentSet} backed by a {@link HashSet} with concurrent read and write
 * access via {@link ReadWriteLock}. Enforces no-duplicate semantics with snapshot-based
 * iteration.
 *
 * @param <E> the type of elements in this set
 */
public class ConcurrentHashSet<E> extends AtomicSet<E, AbstractSet<E>> implements ConcurrentSet<E> {

	/**
	 * Creates a new concurrent set.
	 */
	public ConcurrentHashSet() {
		super(new HashSet<>());
	}

	/**
	 * Creates a new concurrent set and fills it with the given array.
	 *
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentHashSet(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Creates a new concurrent set and fills it with the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty set
	 */
	public ConcurrentHashSet(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new HashSet<>() : new HashSet<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentHashSet} that adopts {@code backingSet} as its storage with a
	 * fresh lock. Public callers should go through {@link #adopt(AbstractSet)}.
	 *
	 * @param backingSet the backing set to adopt
	 */
	protected ConcurrentHashSet(@NotNull AbstractSet<E> backingSet) {
		super(backingSet);
	}

	/**
	 * Constructs a {@code ConcurrentHashSet} with a pre-built backing set and an explicit lock.
	 * Used by {@code ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet} (and its variants) to
	 * install a snapshot set paired with a no-op lock for wait-free reads.
	 *
	 * @param backingSet the pre-built backing set
	 * @param lock the lock guarding {@code backingSet}
	 */
	protected ConcurrentHashSet(@NotNull AbstractSet<E> backingSet, @NotNull ReadWriteLock lock) {
		super(backingSet, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentHashSet} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results.
	 *
	 * @param backing the set to adopt
	 * @param <E> the element type
	 * @return a concurrent hash set backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentHashSet<E> adopt(@NotNull AbstractSet<E> backing) {
		return new ConcurrentHashSet<>(backing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected @NotNull AtomicCollection<E, AbstractSet<E>> newEmpty() {
		return new ConcurrentHashSet<>();
	}

	/**
	 * Returns a type-preserving snapshot of this set's backing reference, captured under the read
	 * lock. Subclasses backed by a different concrete {@link AbstractSet} implementation override
	 * this to return an instance of that type so iteration order is preserved on the snapshot.
	 *
	 * @return a fresh {@link AbstractSet} containing the current elements
	 */
	protected @NotNull AbstractSet<E> cloneRef() {
		return this.withReadLock(() -> new HashSet<>(this.ref));
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentHashSet}.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations on
	 * this set are not reflected in the snapshot. Reads on the snapshot are wait-free.</p>
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentSet<E> toUnmodifiable() {
		return new ConcurrentUnmodifiable.UnmodifiableConcurrentHashSet<>(this.cloneRef());
	}

}
