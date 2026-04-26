package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicDeque;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableDeque;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * A thread-safe double-ended queue backed by a {@link ConcurrentLinkedList} with concurrent access.
 * Supports element insertion and removal at both ends with FIFO and LIFO semantics.
 *
 * @param <E> the type of elements in this deque
 */
public class ConcurrentDeque<E> extends AtomicDeque<E> {

	/**
	 * Create a new concurrent deque.
	 */
	public ConcurrentDeque() {
		super(new LinkedList<>());
	}

	/**
	 * Create a new concurrent deque and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentDeque(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent deque and fill it with the given collection.
	 */
	public ConcurrentDeque(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new LinkedList<>() : new LinkedList<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentDeque} with a pre-built backing storage. Used by
	 * {@link ConcurrentUnmodifiableDeque} to install snapshot storage.
	 *
	 * @param storage the pre-built backing storage
	 */
	protected ConcurrentDeque(@NotNull ConcurrentLinkedList<E> storage) {
		super(storage);
	}

	/**
	 * Returns an immutable snapshot of this {@code ConcurrentDeque}.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations
	 * on this deque are not reflected in the snapshot.</p>
	 *
	 * @return an unmodifiable {@link ConcurrentDeque} containing a snapshot of the elements
	 */
	public @NotNull ConcurrentDeque<E> toUnmodifiable() {
		return new ConcurrentUnmodifiableDeque<>(this);
	}

}
