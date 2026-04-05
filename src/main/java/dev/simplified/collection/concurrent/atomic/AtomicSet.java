package dev.simplified.collection.concurrent.atomic;

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
 */
public abstract class AtomicSet<E, T extends AbstractSet<E>> extends AtomicCollection<E, T> implements Set<E> {

	protected AtomicSet(@NotNull T type) {
		super(type);
	}

}
