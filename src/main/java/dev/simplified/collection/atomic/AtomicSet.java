package dev.simplified.collection.atomic;

import dev.simplified.collection.ConcurrentSet;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe abstract set backed by a {@link ReadWriteLock} for concurrent access.
 * Extends {@link AtomicCollection} to enforce set semantics (no duplicate elements) with atomic guarantees.
 *
 * @param <E> the type of elements in this set
 * @param <T> the type of the underlying set
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicSet<E, T extends AbstractSet<E>> extends AtomicCollection<E, T> implements ConcurrentSet<E> {

	protected AtomicSet(@NotNull T type) {
		super(type);
	}

	/**
	 * Constructs an {@code AtomicSet} with an explicit lock - the pattern used by
	 * {@code ConcurrentUnmodifiableSet} (and its variants) to install a snapshot set
	 * paired with a no-op lock for wait-free reads.
	 *
	 * @param ref the underlying set
	 * @param lock the lock guarding {@code ref}
	 */
	protected AtomicSet(@NotNull T ref, @NotNull java.util.concurrent.locks.ReadWriteLock lock) {
		super(ref, lock);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int spliteratorCharacteristics() {
		return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Copies into a {@link Set}, preserving iteration order. {@link AbstractSet#equals(Object)}
	 * answers {@code false} for anything that is not a {@code Set}, so inheriting the list-shaped
	 * copy would make every comparison against this set answer {@code false}.
	 */
	@Override
	protected @NotNull Object comparisonSnapshot() {
		return this.withReadLock(() -> new LinkedHashSet<>(this.ref));
	}

}
