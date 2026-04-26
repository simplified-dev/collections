package dev.simplified.collection.atomic;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Set;
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
public abstract class AtomicSet<E, T extends AbstractSet<E>> extends AtomicCollection<E, T> implements Set<E> {

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

}
