package dev.sbs.api.collection.concurrent.linked;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentSet;
import dev.sbs.api.collection.concurrent.atomic.AtomicCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

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
	 * Creates a new empty {@code ConcurrentLinkedSet} instance, used internally for copy operations.
	 *
	 * @return a new empty {@link ConcurrentLinkedSet}
	 */
	@Override
	protected @NotNull AtomicCollection<E, AbstractSet<E>> createEmpty() {
		return Concurrent.newLinkedSet();
	}

}
