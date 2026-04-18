package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicQueue;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * A thread-safe FIFO queue backed by a {@link ConcurrentLinkedList} with concurrent access.
 * Supports standard queue operations: offer, peek, poll, and element retrieval.
 *
 * @param <E> the type of elements in this queue
 */
public class ConcurrentQueue<E> extends AtomicQueue<E> {

	/**
	 * Create a new concurrent queue.
	 */
	public ConcurrentQueue() {
		super(new LinkedList<>());
	}

	/**
	 * Create a new concurrent queue and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentQueue(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent queue and fill it with the given collection.
	 */
	public ConcurrentQueue(@Nullable Collection<? extends E> collection) {
		super(collection == null ? new LinkedList<>() : new LinkedList<>(collection));
	}

	/**
	 * Constructs a {@code ConcurrentQueue} sharing the given source's underlying storage.
	 * Used by {@link dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableQueue} to
	 * present a live, unmodifiable view over any existing {@link AtomicQueue}.
	 *
	 * @param source the source queue whose storage is shared
	 */
	protected ConcurrentQueue(@NotNull AtomicQueue<E> source) {
		super(source);
	}

	/**
	 * Returns a live, unmodifiable view of this {@code ConcurrentQueue}.
	 *
	 * @return an unmodifiable {@link ConcurrentQueue} view over the same state
	 */
	public @NotNull ConcurrentQueue<E> toUnmodifiableQueue() {
		return Concurrent.newUnmodifiableQueue(this);
	}

}
