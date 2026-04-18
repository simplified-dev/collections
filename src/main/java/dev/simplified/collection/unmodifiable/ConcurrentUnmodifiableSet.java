package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.atomic.AtomicSet;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A live, unmodifiable view over any {@link AtomicSet}. Shares the source set's
 * {@code ref} and lock, so the wrapper reflects current source state (iteration order, size,
 * contents), but every mutating operation rejects with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this set
 */
public class ConcurrentUnmodifiableSet<E> extends ConcurrentSet<E> {

	/**
	 * Creates a live, unmodifiable view over the given source.
	 *
	 * @param source the source whose state is shared with this unmodifiable view
	 */
	public ConcurrentUnmodifiableSet(@NotNull AtomicSet<E, ? extends AbstractSet<E>> source) {
		super(source);
	}

	/** {@inheritDoc} */
	@Override
	public final boolean add(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean addIf(@NotNull Predicate<AbstractSet<E>> predicate, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean remove(Object item) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean removeAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean removeIf(@NotNull Predicate<? super E> filter) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @NotNull ConcurrentSet<E> toUnmodifiableSet() {
		return this;
	}

}
