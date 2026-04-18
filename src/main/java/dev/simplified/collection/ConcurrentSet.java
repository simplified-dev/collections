package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.atomic.AtomicSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * A thread-safe set backed by a {@link HashSet} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Enforces no-duplicate semantics
 * with snapshot-based iteration.
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
	 * Constructs a {@code ConcurrentSet} sharing the given source's {@code ref} and lock.
	 * Used by {@link dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableSet} to
	 * present a live, unmodifiable view over any existing {@link AtomicSet}.
	 *
	 * @param source the source set whose state is shared
	 */
	protected ConcurrentSet(@NotNull AtomicSet<E, ? extends AbstractSet<E>> source) {
		super(source);
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
	 * Returns an unmodifiable view of this {@code ConcurrentSet}.
	 * Attempts to modify the returned set will throw {@link UnsupportedOperationException}.
	 *
	 * @return an unmodifiable {@link ConcurrentSet} containing the same elements
	 */
	public @NotNull ConcurrentSet<E> toUnmodifiableSet() {
		return Concurrent.newUnmodifiableSet(this);
	}

}
