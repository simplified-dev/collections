package dev.simplified.collection.concurrent.sorted;

import dev.simplified.collection.concurrent.Concurrent;
import dev.simplified.collection.concurrent.ConcurrentSet;
import dev.simplified.collection.concurrent.atomic.AtomicCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * A thread-safe set backed by a {@link TreeSet} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Maintains element ordering defined
 * by a {@link Comparator} or the elements' natural ordering.
 *
 * @param <E> the type of elements in this set
 */
public class ConcurrentSortedSet<E> extends ConcurrentSet<E> {

	/**
	 * Create a new concurrent sorted set with natural element ordering.
	 */
	public ConcurrentSortedSet() {
		super(new TreeSet<>());
	}

	/**
	 * Create a new concurrent sorted set with the given comparator.
	 *
	 * @param comparator the comparator used to order the elements
	 */
	public ConcurrentSortedSet(@NotNull Comparator<? super E> comparator) {
		super(new TreeSet<>(comparator));
	}

	/**
	 * Create a new concurrent sorted set with natural element ordering and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentSortedSet(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent sorted set with the given comparator and fill it with the given array.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param array the elements to include
	 */
	@SafeVarargs
	public ConcurrentSortedSet(@NotNull Comparator<? super E> comparator, @NotNull E... array) {
		this(comparator, Arrays.asList(array));
	}

	/**
	 * Create a new concurrent sorted set with natural element ordering and fill it with the given collection.
	 */
	public ConcurrentSortedSet(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new TreeSet<>() : new TreeSet<>(collection));
	}

	/**
	 * Create a new concurrent sorted set with the given comparator and fill it with the given collection.
	 *
	 * @param comparator the comparator used to order the elements
	 * @param collection the source collection to copy from
	 */
	public ConcurrentSortedSet(@NotNull Comparator<? super E> comparator, @Nullable Collection<? extends E> collection) {
		super(newTreeSet(comparator, collection));
	}

	/**
	 * Creates a new {@link TreeSet} with the given comparator, populated from the collection.
	 */
	private static <E> @NotNull TreeSet<E> newTreeSet(@NotNull Comparator<? super E> comparator, @Nullable Collection<? extends E> collection) {
		TreeSet<E> set = new TreeSet<>(comparator);
		if (collection != null) set.addAll(collection);
		return set;
	}

	/**
	 * Creates a new empty {@code ConcurrentSortedSet} instance, used internally for copy operations.
	 *
	 * @return a new empty {@link ConcurrentSortedSet}
	 */
	@Override
	protected @NotNull AtomicCollection<E, AbstractSet<E>> createEmpty() {
		return Concurrent.newSortedSet();
	}

}
