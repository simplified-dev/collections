package dev.sbs.api.collection.concurrent.unmodifiable;

import dev.sbs.api.collection.concurrent.ConcurrentCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * An unmodifiable concurrent list that allows for simultaneous fast reading and iteration utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the list by replacing the
 * entire list on each modification. This allows for maintaining the original
 * speed of {@link ArrayList#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
public class ConcurrentUnmodifiableCollection<E> extends ConcurrentCollection<E> {

	/**
	 * Create a new unmodifiable concurrent list.
	 */
	public ConcurrentUnmodifiableCollection() {
		super();
	}

	/**
	 * Create a new unmodifiable concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentUnmodifiableCollection(@NotNull E... array) {
		super(array);
	}

	/**
	 * Create a new unmodifiable concurrent list and fill it with the given collection.
	 */
	public ConcurrentUnmodifiableCollection(@Nullable Collection<? extends E> collection) {
		super(collection);
	}

	@Override
	public final boolean add(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("all")
	public final boolean remove(Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean removeAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

}
