package dev.simplified.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Queue;

/**
 * A thread-safe {@link Queue} extension exposing the project-specific concurrent surface for
 * FIFO queue variants.
 *
 * <p>Implementations carry atomic FIFO semantics with element-ordering guarantees on top of the
 * standard JDK {@link Queue} contract.</p>
 *
 * @param <E> the type of elements in this queue
 */
public interface ConcurrentQueue<E> extends ConcurrentCollection<E>, Queue<E> {

	/**
	 * Returns an immutable snapshot of this queue.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current contents, so subsequent mutations
	 * on this queue are not reflected in the snapshot.</p>
	 *
	 * @return an immutable snapshot of this queue
	 */
	@Override
	@NotNull ConcurrentQueue<E> toUnmodifiable();

}
