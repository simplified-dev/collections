package dev.simplified.collection.concurrent;

import dev.simplified.collection.concurrent.atomic.AtomicQueue;
import dev.simplified.collection.concurrent.linked.ConcurrentLinkedList;
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

}
