package dev.simplified.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Deque;

/**
 * A thread-safe {@link Deque} extension combining the {@link ConcurrentQueue} surface with the
 * double-ended semantics of {@link Deque}.
 *
 * @param <E> the type of elements in this deque
 */
public interface ConcurrentDeque<E> extends ConcurrentQueue<E>, Deque<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentDeque<E> toUnmodifiable();

}
