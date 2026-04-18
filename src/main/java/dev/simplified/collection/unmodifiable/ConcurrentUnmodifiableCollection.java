package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentCollection;
import dev.simplified.collection.atomic.AtomicCollection;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A live, unmodifiable view over any {@link AtomicCollection}. Shares the source collection's
 * {@code ref} and lock, so the wrapper reflects current source state (size, contents,
 * iteration order), but every mutating operation rejects with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this collection
 */
public class ConcurrentUnmodifiableCollection<E> extends ConcurrentCollection<E> {

	/**
	 * Creates a live, unmodifiable view over the given source.
	 *
	 * @param source the source whose state is shared with this unmodifiable view
	 */
	public ConcurrentUnmodifiableCollection(@NotNull AtomicCollection<E, ? extends AbstractCollection<E>> source) {
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
	public final boolean addIf(@NotNull Predicate<AbstractCollection<E>> predicate, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean remove(Object element) {
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
	public final @NotNull ConcurrentCollection<E> toUnmodifiableCollection() {
		return this;
	}

}
