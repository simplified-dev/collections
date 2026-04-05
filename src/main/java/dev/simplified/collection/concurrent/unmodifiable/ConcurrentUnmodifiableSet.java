package dev.simplified.collection.concurrent.unmodifiable;

import dev.simplified.collection.concurrent.ConcurrentSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

/**
 * An unmodifiable thread-safe set backed by a {@link HashSet} that permits concurrent reads
 * but rejects all modifications. Mutating operations throw {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this set
 */
public class ConcurrentUnmodifiableSet<E> extends ConcurrentSet<E> {

	/**
	 * Create a new unmodifiable concurrent set.
	 */
	public ConcurrentUnmodifiableSet() {
		super();
	}

	/**
	 * Create a new unmodifiable concurrent set and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentUnmodifiableSet(@NotNull E... array) {
		super(array);
	}

	/**
	 * Create a new unmodifiable concurrent set and fill it with the given collection.
	 */
	public ConcurrentUnmodifiableSet(@Nullable Collection<? extends E> collection) {
		super(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean add(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean remove(Object item) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean removeAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

}
