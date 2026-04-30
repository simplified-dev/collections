package dev.simplified.collection;

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
 * A thread-safe {@link ConcurrentSet} backed by a {@link LinkedHashSet} with concurrent read and
 * write access via {@link ReadWriteLock}. Maintains insertion order while enforcing no-duplicate
 * semantics.
 *
 * @param <E> the type of elements in this set
 */
public class ConcurrentLinkedHashSet<E> extends ConcurrentHashSet<E> {

	/**
	 * Creates a new concurrent linked set.
	 */
	public ConcurrentLinkedHashSet() {
		super(new LinkedHashSet<>());
	}

	/**
	 * Creates a new concurrent linked set and fills it with the given array.
	 *
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentLinkedHashSet(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Creates a new concurrent linked set and fills it with the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty set
	 */
	public ConcurrentLinkedHashSet(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new LinkedHashSet<>() : new LinkedHashSet<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentLinkedHashSet} that adopts {@code backingSet} as its storage
	 * with a fresh lock. Public callers should go through {@link #adopt(LinkedHashSet)}.
	 *
	 * @param backingSet the backing linked hash set to adopt
	 */
	protected ConcurrentLinkedHashSet(@NotNull LinkedHashSet<E> backingSet) {
		super(backingSet);
	}

	/**
	 * Constructs a {@code ConcurrentLinkedHashSet} with a pre-built backing set and an explicit
	 * lock. Used by {@link ConcurrentUnmodifiableLinkedSet.Impl} to install a snapshot set paired
	 * with a no-op lock for wait-free reads.
	 *
	 * @param backingSet the pre-built backing set
	 * @param lock the lock guarding {@code backingSet}
	 */
	protected ConcurrentLinkedHashSet(@NotNull LinkedHashSet<E> backingSet, @NotNull ReadWriteLock lock) {
		super(backingSet, lock);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedHashSet} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to {@code backing}
	 * bypass the read/write lock and may corrupt concurrent reads. Use this for zero-copy
	 * publication of single-threaded build results.
	 *
	 * @param backing the linked hash set to adopt
	 * @param <E> the element type
	 * @return a concurrent linked hash set backed by {@code backing}
	 */
	public static <E> @NotNull ConcurrentLinkedHashSet<E> adopt(@NotNull LinkedHashSet<E> backing) {
		return new ConcurrentLinkedHashSet<>(backing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected @NotNull AtomicCollection<E, AbstractSet<E>> newEmpty() {
		return new ConcurrentLinkedHashSet<>();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Overrides {@link ConcurrentHashSet#cloneRef()} to produce a {@link LinkedHashSet}
	 * snapshot preserving the source's insertion-order traversal characteristics.</p>
	 */
	@Override
	protected @NotNull AbstractSet<E> cloneRef() {
		return this.withReadLock(() -> new LinkedHashSet<>(this.ref));
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentLinkedHashSet} preserving insertion
	 * order.
	 *
	 * @return an immutable snapshot
	 */
	@Override
	public @NotNull ConcurrentSet<E> toUnmodifiable() {
		return new ConcurrentUnmodifiableLinkedSet.Impl<>((LinkedHashSet<E>) this.cloneRef());
	}

}
