package dev.simplified.collection.atomic;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Spliterator wrapper that delegates traversal to a snapshot-backed array spliterator while
 * exposing the source set or map's {@link Comparator} via {@link #getComparator()}, satisfying
 * the {@link Spliterator#SORTED} contract that {@link java.util.Spliterators#spliterator}
 * implementations do not honor for custom comparators.
 *
 * @param <E> the type of elements traversed by this spliterator
 */
final class SortedSnapshotSpliterator<E> implements Spliterator<E> {

	private final @NotNull Spliterator<E> delegate;
	private final Comparator<? super E> comparator;

	SortedSnapshotSpliterator(@NotNull Spliterator<E> delegate, Comparator<? super E> comparator) {
		this.delegate = delegate;
		this.comparator = comparator;
	}

	@Override
	public boolean tryAdvance(@NotNull Consumer<? super E> action) {
		return this.delegate.tryAdvance(action);
	}

	@Override
	public void forEachRemaining(@NotNull Consumer<? super E> action) {
		this.delegate.forEachRemaining(action);
	}

	@Override
	public Spliterator<E> trySplit() {
		Spliterator<E> split = this.delegate.trySplit();
		return split == null ? null : new SortedSnapshotSpliterator<>(split, this.comparator);
	}

	@Override
	public long estimateSize() {
		return this.delegate.estimateSize();
	}

	@Override
	public long getExactSizeIfKnown() {
		return this.delegate.getExactSizeIfKnown();
	}

	@Override
	public int characteristics() {
		return this.delegate.characteristics();
	}

	@Override
	public Comparator<? super E> getComparator() {
		return this.comparator;
	}

}
