package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A thread-safe collection backed by an {@link AbstractCollection} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Provides the base concrete implementation of
 * {@link AtomicCollection}.
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
	 * Constructs a {@code ConcurrentCollection} sharing the given source's {@code ref} and lock.
	 * Used by {@link dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableCollection} to
	 * present a live, unmodifiable view over any existing {@link AtomicCollection}.
	 *
	 * @param source the source collection whose state is shared
	 */
	protected ConcurrentCollection(@NotNull AtomicCollection<E, ? extends AbstractCollection<E>> source) {
		super(source);
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
	 * Returns a live, unmodifiable view of this {@code ConcurrentCollection}.
	 *
	 * @return an unmodifiable {@link ConcurrentCollection} view over the same state
	 */
	public @NotNull ConcurrentCollection<E> toUnmodifiableCollection() {
		return Concurrent.newUnmodifiableCollection(this);
	}

}
