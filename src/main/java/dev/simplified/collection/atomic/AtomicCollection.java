package dev.simplified.collection.atomic;

import dev.simplified.collection.ConcurrentCollection;
import dev.simplified.collection.StreamUtil;
import dev.simplified.collection.tuple.single.SingleStream;
import dev.simplified.collection.tuple.triple.TripleStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/**
 * A thread-safe abstract collection backed by a {@link ReadWriteLock} for concurrent access.
 * Provides atomic read and write operations on an underlying collection of type {@code T}.
 *
 * @param <E> the type of elements in this collection
 * @param <T> the type of the underlying collection
 *
 * @apiNote This is a low-level building block for custom concurrent implementations.
 * Most callers should use the corresponding {@code Concurrent*} type instead.
 */
@SuppressWarnings("all")
public abstract class AtomicCollection<E, T extends Collection<E>> extends AbstractCollection<E> implements ConcurrentCollection<E> {

	protected final @NotNull T ref;
	protected final @NotNull ReadWriteLock lock;

	/**
	 * Cached {@link #toArray} snapshot used by iterators. Published under the read lock,
	 * invalidated to {@code null} under the write lock. The volatile publishes the array
	 * safely to lock-free readers; iterators never mutate the snapshot so sharing it is safe.
	 */
	protected transient volatile @Nullable Object @Nullable [] snapshotCache;

	protected AtomicCollection(@NotNull T ref) {
		this(ref, new ReentrantReadWriteLock());
	}

	/**
	 * Constructs an {@code AtomicCollection} with an explicit lock, typically one shared with
	 * another {@link AtomicCollection} to provide a live, unmodifiable view of the same state.
	 *
	 * @param ref the underlying collection
	 * @param lock the lock guarding {@code ref}
	 */
	protected AtomicCollection(@NotNull T ref, @NotNull ReadWriteLock lock) {
		this.ref = ref;
		this.lock = lock;
	}

	/**
	 * Invalidates the cached iterator snapshot. Called from the {@code finally} block of every
	 * mutator on this collection, before the write lock is released.
	 */
	protected void invalidateSnapshot() {
		this.snapshotCache = null;
		this.onSnapshotInvalidated();
	}

	/**
	 * Executes the given action with the read lock held and returns its result.
	 *
	 * @param action the action to execute under the read lock
	 * @param <R> the result type
	 * @return the value returned by {@code action}
	 */
	protected final <R> R withReadLock(@NotNull java.util.function.Supplier<R> action) {
		this.lock.readLock().lock();

		try {
			return action.get();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Executes the given action with the read lock held.
	 *
	 * @param action the action to execute under the read lock
	 */
	protected final void withReadLock(@NotNull Runnable action) {
		this.lock.readLock().lock();

		try {
			action.run();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Executes the given action with the write lock held and returns its result. Invalidates the
	 * iterator snapshot in the {@code finally} block before releasing the lock.
	 *
	 * @param action the action to execute under the write lock
	 * @param <R> the result type
	 * @return the value returned by {@code action}
	 */
	protected final <R> R withWriteLock(@NotNull java.util.function.Supplier<R> action) {
		this.lock.writeLock().lock();

		try {
			return action.get();
		} finally {
			this.invalidateSnapshot();
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Executes the given action with the write lock held. Invalidates the iterator snapshot in
	 * the {@code finally} block before releasing the lock.
	 *
	 * @param action the action to execute under the write lock
	 */
	protected final void withWriteLock(@NotNull Runnable action) {
		this.lock.writeLock().lock();

		try {
			action.run();
		} finally {
			this.invalidateSnapshot();
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Hook invoked from {@link #invalidateSnapshot()} after the iterator snapshot is cleared.
	 * Subclasses may override to invalidate additional cached views.
	 */
	protected void onSnapshotInvalidated() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(@NotNull E element) {
		return this.withWriteLock(() -> this.ref.add(element));
	}

	/**
	 * Adds all of the specified elements to this collection.
	 *
	 * @param collection the elements to be added to this collection
	 * @return {@code true} if this collection changed as a result of the call
	 */
	public final boolean addAll(@NotNull E... collection) {
		return this.addAll(Arrays.asList(collection));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(@NotNull Collection<? extends E> collection) {
		return this.withWriteLock(() -> this.ref.addAll(collection));
	}

	/**
	 * Adds the specified element to this collection only if the given supplier returns {@code true}.
	 *
	 * @param predicate the supplier that determines whether the element should be added
	 * @param element the element to add
	 * @return {@code true} if the element was added to this collection
	 */
	public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		return this.withWriteLock(() -> predicate.get() && this.ref.add(element));
	}

	/**
	 * Adds the specified element to this collection only if the given predicate,
	 * tested against the underlying collection, returns {@code true}.
	 *
	 * @param predicate the predicate to test against the underlying collection
	 * @param element the element to add
	 * @return {@code true} if the element was added to this collection
	 */
	public boolean addIf(@NotNull Predicate<T> predicate, @NotNull E element) {
		return this.withWriteLock(() -> predicate.test(this.ref) && this.ref.add(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		this.withWriteLock(this.ref::clear);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object item) {
		return this.withReadLock(() -> this.ref.contains(item));
	}

	/**
	 * Returns {@code true} if this collection contains an element whose value,
	 * extracted by the given function, equals the specified value.
	 *
	 * @param <S> the type of the extracted value
	 * @param function the function to extract a value from each element
	 * @param value the value to search for
	 * @return {@code true} if a matching element is found
	 */
	public final <S> boolean contains(@NotNull Function<E, S> function, S value) {
		return this.withReadLock(() -> {
			for (E element : this.ref) {
				if (Objects.equals(function.apply(element), value))
					return true;
			}

			return false;
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(@NotNull Collection<?> collection) {
		return this.withReadLock(() -> this.ref.containsAll(collection));
	}

	/**
	 * Creates a new empty instance of this atomic collection type. Used by snapshot-and-mutate
	 * operations like {@link AtomicList#sorted}, {@link AtomicList#reversed}, and
	 * {@link AtomicList#subList} to materialize their result so the result preserves the source's
	 * backing-collection characteristics.
	 *
	 * @return a new empty {@code AtomicCollection} of the same concrete type
	 */
	protected abstract @NotNull AtomicCollection<E, T> newEmpty();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof AtomicCollection) obj = ((AtomicCollection<?, ?>) obj).ref;

		final Object target = obj;
		return this.withReadLock(() -> this.ref.equals(target));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return this.withReadLock(this.ref::hashCode);
	}

	/**
	 * Returns a sequential {@link TripleStream} where each element is paired with its index and the total size.
	 *
	 * @return a sequential indexed stream of this collection's elements
	 */
	public final @NotNull TripleStream<E, Long, Long> indexedStream() {
		return this.indexedStream(false);
	}

	/**
	 * Returns a {@link TripleStream} where each element is paired with its index and the total size,
	 * optionally in parallel.
	 *
	 * @param parallel {@code true} to create a parallel stream, {@code false} for sequential
	 * @return an indexed stream of this collection's elements
	 */
	public final @NotNull TripleStream<E, Long, Long> indexedStream(boolean parallel) {
		return StreamUtil.zipWithIndex(this.spliterator(), parallel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isEmpty() {
		return this.withReadLock(this.ref::isEmpty);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Fast path: a volatile read of the cached snapshot. If present, returns an iterator
	 * over the shared array with no lock acquisition. Otherwise falls back to a read-locked
	 * double-checked populate, so the snapshot is only computed once per write generation.
	 */
	@Override
	public @NotNull Iterator<E> iterator() {
		Object[] snapshot = this.snapshotCache;

		if (snapshot == null) {
			snapshot = this.withReadLock(() -> {
				Object[] cached = this.snapshotCache;

				if (cached == null) {
					cached = this.ref.toArray();
					this.snapshotCache = cached;
				}

				return cached;
			});
		}

		return new ConcurrentCollectionIterator(snapshot, 0);
	}

	/**
	 * Returns {@code true} if this collection does not contain the specified element.
	 *
	 * @param item the element whose absence is to be tested
	 * @return {@code true} if this collection does not contain the specified element
	 */
	public final boolean notContains(Object item) {
		return !this.contains(item);
	}

	/**
	 * Returns {@code true} if this collection contains at least one element.
	 *
	 * @return {@code true} if this collection is not empty
	 */
	public final boolean notEmpty() {
		return !this.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull SingleStream<E> parallelStream() {
		return SingleStream.of(StreamSupport.stream(this.spliterator(), true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object element) {
		return this.withWriteLock(() -> this.ref.remove(element));
	}

	/**
	 * Replaces the given element with the provided new element.
	 * <p>
	 * Only adds the {@code replaceWith} element if the existing element was successfully removed.
	 *
	 * @param existingElement the element to be replaced
	 * @param replaceWith the element to replace with
	 * @return {@code true} if the element was replaced
	 */
	public final boolean replace(@NotNull E existingElement, @NotNull E replaceWith) {
		return this.withWriteLock(() -> this.ref.remove(existingElement) && this.ref.add(replaceWith));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		return this.withWriteLock(() -> this.ref.removeAll(collection));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		return this.withWriteLock(() -> this.ref.retainAll(collection));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size() {
		return this.withReadLock(this.ref::size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull SingleStream<E> stream() {
		return SingleStream.of(StreamSupport.stream(this.spliterator(), false));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract @NotNull ConcurrentCollection<E> toUnmodifiable();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object @NotNull [] toArray() {
		return this.withReadLock((java.util.function.Supplier<Object[]>) this.ref::toArray);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("SuspiciousToArrayCall")
	public <U> U @NotNull [] toArray(@NotNull U @NotNull [] array) {
		return this.withReadLock(() -> this.ref.toArray(array));
	}

	/**
	 * A concurrent snapshot-backed iterator. Iteration is weakly consistent: the snapshot
	 * is captured at iterator creation and never refreshed, so self-modifications via
	 * {@link #remove()} update the backing collection but are not reflected in subsequent
	 * {@code next()} calls.
	 *
	 * @see CopyOnWriteArrayList.COWIterator
	 */
	protected class ConcurrentCollectionIterator extends AtomicIterator<E> {

		protected ConcurrentCollectionIterator(Object[] snapshot, int index) {
			super(snapshot, index);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * If the element was concurrently removed before this call, the operation is a
		 * silent no-op - no {@link java.util.ConcurrentModificationException} is thrown.
		 * The post-condition ("the element returned by the last {@code next()} is absent
		 * from the collection") is already satisfied either way.
		 */
		@Override
		public void remove() {
			if (this.last < 0)
				throw new IllegalStateException();

			AtomicCollection.this.remove(this.snapshot[this.last]);
			this.last = -1;
		}

	}

}
