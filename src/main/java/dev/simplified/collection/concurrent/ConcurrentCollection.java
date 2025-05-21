package dev.sbs.api.collection.concurrent;

import dev.sbs.api.collection.concurrent.atomic.AtomicCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent collection that allows for simultaneous fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the set by replacing the
 * entire set each modification. This allows for maintaining the original speed
 * of {@link HashSet#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
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

}
