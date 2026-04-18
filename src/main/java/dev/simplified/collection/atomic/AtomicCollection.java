package dev.simplified.collection.atomic;

import dev.simplified.collection.query.Searchable;
import dev.simplified.collection.tuple.single.SingleStream;
import dev.simplified.collection.tuple.triple.TripleStream;
import dev.simplified.collection.StreamUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
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
public abstract class AtomicCollection<E, T extends Collection<E>> extends AbstractCollection<E> implements Collection<E>, Searchable<E>, Serializable {

	protected final @NotNull T ref;
	protected final transient @NotNull ReadWriteLock lock;

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
	 * Constructs an {@code AtomicCollection} sharing the given source's {@code ref} and lock.
	 * Reads and writes go through the same state as the source, giving live-view semantics.
	 *
	 * @param source the source collection whose state is shared
	 */
	protected AtomicCollection(@NotNull AtomicCollection<E, ? extends T> source) {
		this.ref = source.ref;
		this.lock = source.lock;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(@NotNull E element) {
		try {
			this.lock.writeLock().lock();
			return this.ref.add(element);
		} finally {
			this.snapshotCache = null;
			this.lock.writeLock().unlock();
		}
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
		try {
			this.lock.writeLock().lock();
			return this.ref.addAll(collection);
		} finally {
			this.snapshotCache = null;
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Adds the specified element to this collection only if the given supplier returns {@code true}.
	 *
	 * @param predicate the supplier that determines whether the element should be added
	 * @param element the element to add
	 * @return {@code true} if the element was added to this collection
	 */
	public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		try {
			this.lock.writeLock().lock();

			if (predicate.get())
				return this.ref.add(element);

			return false;
		} finally {
			this.snapshotCache = null;
			this.lock.writeLock().unlock();
		}
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
		try {
			this.lock.writeLock().lock();

			if (predicate.test(this.ref))
				return this.ref.add(element);

			return false;
		} finally {
			this.snapshotCache = null;
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		try {
			this.lock.writeLock().lock();
			this.ref.clear();
		} finally {
			this.snapshotCache = null;
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object item) {
		try {
			this.lock.readLock().lock();
			return this.ref.contains(item);
		} finally {
			this.lock.readLock().unlock();
		}
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
		try {
			this.lock.readLock().lock();

			for (E element : this.ref) {
				if (Objects.equals(function.apply(element), value))
					return true;
			}
		} finally {
			this.lock.readLock().unlock();
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(@NotNull Collection<?> collection) {
		try {
			this.lock.readLock().lock();
			return this.ref.containsAll(collection);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Creates a new empty instance of this atomic collection type.
	 *
	 * @return a new empty {@code AtomicCollection} of the same concrete type
	 */
	protected abstract @NotNull AtomicCollection<E, T> createEmpty();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof AtomicCollection) obj = ((AtomicCollection<?, ?>) obj).ref;

		try {
			this.lock.readLock().lock();
			return this.ref.equals(obj);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		try {
			this.lock.readLock().lock();
			return this.ref.hashCode();
		} finally {
			this.lock.readLock().unlock();
		}
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
		try {
			this.lock.readLock().lock();
			return this.ref.isEmpty();
		} finally {
			this.lock.readLock().unlock();
		}
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
			try {
				this.lock.readLock().lock();
				snapshot = this.snapshotCache;

				if (snapshot == null) {
					snapshot = this.ref.toArray();
					this.snapshotCache = snapshot;
				}
			} finally {
				this.lock.readLock().unlock();
			}
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
		try {
			this.lock.writeLock().lock();
			return this.ref.remove(element);
		} finally {
			this.snapshotCache = null;
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Replaces the given element with the provided new element.
	 * <p>
	 * This only adds the replaceWith element if it removes the
	 * existing element successfully.
	 *
	 * @param existingElement The element to be replaced.
	 * @param replaceWith The element to replace with.
	 */
	public final boolean replace(@NotNull E existingElement, @NotNull E replaceWith) {
		try {
			this.lock.writeLock().lock();

			if (this.ref.remove(existingElement))
				return this.ref.add(replaceWith);

			return false;
		} finally {
			this.snapshotCache = null;
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		try {
			this.lock.writeLock().lock();
			return this.ref.removeAll(collection);
		} finally {
			this.snapshotCache = null;
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		try {
			this.lock.writeLock().lock();
			return this.ref.retainAll(collection);
		} finally {
			this.snapshotCache = null;
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size() {
		try {
			this.lock.readLock().lock();
			return this.ref.size();
		} finally {
			this.lock.readLock().unlock();
		}
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
	public Object @NotNull [] toArray() {
		try {
			this.lock.readLock().lock();
			return this.ref.toArray();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("SuspiciousToArrayCall")
	public <U> U @NotNull [] toArray(@NotNull U @NotNull [] array) {
		try {
			this.lock.readLock().lock();
			return this.ref.toArray(array);
		} finally {
			this.lock.readLock().unlock();
		}
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
