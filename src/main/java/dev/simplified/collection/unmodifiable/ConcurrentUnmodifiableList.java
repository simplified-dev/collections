package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.atomic.AtomicList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A live, unmodifiable view over any {@link AtomicList}. Shares the source list's
 * {@code ref} and lock, so the wrapper reflects current source state (iteration order, size,
 * contents), but every mutating operation rejects with {@link UnsupportedOperationException}.
 *
 * @param <E> the type of elements in this list
 */
public class ConcurrentUnmodifiableList<E> extends ConcurrentList<E> {

	/**
	 * Creates a live, unmodifiable view over the given source.
	 *
	 * @param source the source whose state is shared with this unmodifiable view
	 */
	public ConcurrentUnmodifiableList(@NotNull AtomicList<E, ? extends List<E>> source) {
		super(source);
	}

	/** {@inheritDoc} */
	@Override
	public final boolean add(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void add(int index, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean addAll(int index, @NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void addFirst(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void addLast(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean addIf(@NotNull Predicate<List<E>> predicate, @NotNull E element) {
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
	public final E remove(int index) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final E removeFirst() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final E removeLast() {
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
	public final void replaceAll(@NotNull UnaryOperator<E> operator) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void sort(Comparator<? super E> comparator) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @NotNull ConcurrentList<E> toUnmodifiable() {
		return this;
	}

}
