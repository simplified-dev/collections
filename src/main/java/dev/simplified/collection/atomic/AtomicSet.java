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
	 * Constructs an {@code AtomicSet} sharing the given source's {@code ref} and lock - the
	 * pattern used by {@code ConcurrentUnmodifiableSet} to provide a live unmodifiable view.
	 *
	 * @param source the source set whose state is shared
	 */
	protected AtomicSet(@NotNull AtomicSet<E, ? extends T> source) {
		super(source);
	}

}
