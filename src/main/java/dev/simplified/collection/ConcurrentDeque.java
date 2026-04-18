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
	 * Constructs a {@code ConcurrentDeque} sharing the given source's underlying storage.
	 * Used by {@link ConcurrentUnmodifiableDeque} to present a live, unmodifiable view
	 * over any existing {@link AtomicDeque}.
	 *
	 * @param source the source deque whose storage is shared
	 */
	protected ConcurrentDeque(@NotNull AtomicDeque<E> source) {
		super(source);
	}

	/**
	 * Returns a live, unmodifiable view of this {@code ConcurrentDeque}.
	 *
	 * @return an unmodifiable {@link ConcurrentDeque} view over the same state
	 */
	public @NotNull ConcurrentDeque<E> toUnmodifiable() {
		return Concurrent.newUnmodifiableDeque(this);
	}

}
