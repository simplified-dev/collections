package dev.simplified.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A thread-safe {@link Set} extension combining the {@link ConcurrentCollection} surface with
 * the no-duplicate semantics of {@link Set}.
 *
 * @param <E> the type of elements in this set
 */
public interface ConcurrentSet<E> extends ConcurrentCollection<E>, Set<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentSet<E> toUnmodifiable();

}
