package dev.simplified.collection.atomic;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import dev.simplified.collection.tuple.single.SingleStream;
import dev.simplified.collection.tuple.triple.TripleStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A thread-safe abstract queue backed by a {@link ConcurrentLinkedList.Impl} for concurrent access.
 * Provides atomic FIFO queue operations with element ordering guarantees.
 *
 * @param <E> the type of elements held in this queue
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
public abstract class AtomicQueue<E> extends AbstractQueue<E> implements Queue<E> {

	protected final @NotNull ConcurrentLinkedList.Impl<E> storage;

	protected AtomicQueue(@NotNull Collection<? extends E> collection) {
		this.storage = (ConcurrentLinkedList.Impl<E>) Concurrent.newLinkedList(collection);
	}

	/**
	 * Constructs an {@code AtomicQueue} with a pre-built backing storage - the pattern used
	 * by {@code ConcurrentUnmodifiableQueue} to install a snapshot storage at construction.
	 *
	 * @param storage the pre-built backing storage
	 */
	protected AtomicQueue(@NotNull ConcurrentLinkedList.Impl<E> storage) {
		this.storage = storage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(E element) {
		return super.add(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(@NotNull Collection<? extends E> collection) {
		return this.storage.addAll(collection);
	}

	/**
	 * Adds all of the specified elements to this queue.
	 *
	 * @param collection the elements to be added to this queue
	 * @return {@code true} if this queue changed as a result of the call
	 */
	@SuppressWarnings("unchecked")
	public boolean addAll(@NotNull E... collection) {
		return this.storage.addAll(collection);
	}

	/**
	 * Adds the specified element to this queue only if the given supplier returns {@code true}.
	 *
	 * @param predicate the supplier that determines whether the element should be added
	 * @param element the element to add
	 * @return {@code true} if the element was added to this queue
	 */
	public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		return this.storage.addIf(predicate, element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		super.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean contains(Object obj) {
		return this.storage.contains(obj);
	}

	/**
	 * Returns {@code true} if this queue contains an element whose value, extracted by the
	 * given function, equals the specified value.
	 *
	 * @param <S> the type of the extracted value
	 * @param function the function to extract a value from each element
	 * @param value the value to search for
	 * @return {@code true} if a matching element is found
	 */
	public final <S> boolean contains(@NotNull Function<E, S> function, S value) {
		return this.storage.contains(function, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containsAll(@NotNull Collection<?> collection) {
		return this.storage.containsAll(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final E element() {
		return super.element();
	}

	/**
	 * Returns a sequential {@link TripleStream} where each element is paired with its index and
	 * the total size.
	 *
	 * @return a sequential indexed stream of this queue's elements
	 */
	public final @NotNull TripleStream<E, Long, Long> indexedStream() {
		return this.storage.indexedStream();
	}

	/**
	 * Returns a {@link TripleStream} where each element is paired with its index and the total
	 * size, optionally in parallel.
	 *
	 * @param parallel {@code true} to create a parallel stream, {@code false} for sequential
	 * @return an indexed stream of this queue's elements
	 */
	public final @NotNull TripleStream<E, Long, Long> indexedStream(boolean parallel) {
		return this.storage.indexedStream(parallel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isEmpty() {
		return this.storage.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull Iterator<E> iterator() {
		return this.storage.iterator();
	}

	/**
	 * Returns {@code true} if this queue does not contain the specified element.
	 *
	 * @param item the element whose absence is to be tested
	 * @return {@code true} if this queue does not contain the specified element
	 */
	public final boolean notContains(Object item) {
		return !this.storage.contains(item);
	}

	/**
	 * Returns {@code true} if this queue contains at least one element.
	 *
	 * @return {@code true} if this queue is not empty
	 */
	public final boolean notEmpty() {
		return !this.storage.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull SingleStream<E> parallelStream() {
		return this.storage.parallelStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull SingleStream<E> stream() {
		return this.storage.stream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offer(E element) {
		return this.storage.add(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable E peek() {
		try {
			this.storage.lock.readLock().lock();
			return this.storage.ref.isEmpty() ? null : this.storage.ref.get(0);
		} finally {
			this.storage.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable E poll() {
		try {
			this.storage.lock.writeLock().lock();
			return this.storage.ref.isEmpty() ? null : this.storage.ref.remove(0);
		} finally {
			this.storage.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull E remove() {
		return super.remove();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object obj) {
		return super.remove(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		return this.storage.removeAll(collection);
	}

	/**
	 * Replaces the given element with the provided new element.
	 *
	 * <p>Only adds the {@code replaceWith} element if the existing element was successfully
	 * removed.</p>
	 *
	 * @param existingElement the element to be replaced
	 * @param replaceWith the element to replace with
	 * @return {@code true} if the element was replaced
	 */
	public boolean replace(@NotNull E existingElement, @NotNull E replaceWith) {
		return this.storage.replace(existingElement, replaceWith);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		return this.storage.retainAll(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size() {
		return this.storage.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Object @NotNull [] toArray() {
		return this.storage.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final <T> T @NotNull [] toArray(T @NotNull [] array) {
		return this.storage.toArray(array);
	}

}
