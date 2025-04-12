package dev.sbs.api.collection.concurrent;

import dev.sbs.api.collection.concurrent.atomic.AtomicList;
import dev.sbs.api.collection.sort.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A concurrent list that allows for simultaneous fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the list by replacing the
 * entire list on each modification. This allows for maintaining the original
 * speed of {@link ArrayList#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
@SuppressWarnings("all")
public class ConcurrentList<E> extends AtomicList<E, ArrayList<E>> {

	/**
	 * Create a new concurrent list.
	 */
	public ConcurrentList() {
		super(new ArrayList<>());
	}

	/**
	 * Create a new concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentList(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent list and fill it with the given collection.
	 */
	public ConcurrentList(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new ArrayList<>() : new ArrayList<>(collection));
	}

	@Override
	public @NotNull ConcurrentList<E> inverse() {
		return Concurrent.newList(super.inverse());
	}

	@Override
	public @NotNull ConcurrentList<E> subList(int fromIndex, int toIndex) {
		return Concurrent.newList(super.subList(fromIndex, toIndex));
	}

	@Override
	public ConcurrentList<E> sorted(@NotNull Function<E, ? extends Comparable>... sortFunctions) {
		super.sorted(sortFunctions);
		return this;
	}

	@Override
	public @NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions) {
		super.sorted(sortOrder, functions);
		return this;
	}

	@Override
	public @NotNull ConcurrentList<E> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		super.sorted(functions);
		return this;
	}

	@Override
	public @NotNull ConcurrentList<E> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		super.sorted(sortOrder, functions);
		return this;
	}

	@Override
	public @NotNull ConcurrentList<E> sorted(Comparator<? super E> comparator) {
		super.sorted(comparator);
		return this;
	}

	public @NotNull ConcurrentList<E> toUnmodifiableList() {
		return Concurrent.newUnmodifiableList(this);
	}

}
