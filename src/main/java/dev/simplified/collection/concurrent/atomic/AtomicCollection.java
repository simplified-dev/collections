package dev.sbs.api.collection.concurrent.atomic;

import dev.sbs.api.collection.concurrent.iterator.ConcurrentIterator;
import dev.sbs.api.collection.query.Searchable;
import dev.sbs.api.tuple.triple.TripleStream;
import dev.sbs.api.util.StreamUtil;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("all")
public abstract class AtomicCollection<E, T extends Collection<E>> extends AbstractCollection<E> implements Collection<E>, Searchable<E>, Serializable {

	protected final @NotNull T ref;
	protected final transient @NotNull ReadWriteLock lock = new ReentrantReadWriteLock();

	protected AtomicCollection(@NotNull T ref) {
		this.ref = ref;
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
			this.lock.writeLock().unlock();
		}
	}

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
			this.lock.writeLock().unlock();
		}
	}

	public boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element) {
		try {
			this.lock.writeLock().lock();

			if (predicate.get())
				return this.ref.add(element);

			return false;
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public boolean addIf(@NotNull Predicate<T> predicate, @NotNull E element) {
		try {
			this.lock.writeLock().lock();

			if (predicate.test(this.ref))
				return this.ref.add(element);

			return false;
		} finally {
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

	protected abstract @NotNull AtomicCollection<E, T> createEmpty();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof AtomicCollection) obj = ((AtomicCollection<?, ?>) obj).ref;
		return this.ref.equals(obj);
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

	public final @NotNull TripleStream<E, Long, Long> indexedStream() {
		return this.indexedStream(false);
	}

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
	 */
	@Override
	public @NotNull Iterator<E> iterator() {
		return new ConcurrentCollectionIterator(this.ref.toArray(), 0);
	}

	public final boolean notContains(Object item) {
		return !this.contains(item);
	}

	public final boolean notEmpty() {
		return !this.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final @NotNull Stream<E> parallelStream() {
		return StreamSupport.stream(this.spliterator(), true);
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
	public final @NotNull Stream<E> stream() {
		return StreamSupport.stream(this.spliterator(), false);
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
	 * A concurrent version of {@link CopyOnWriteArrayList.COWIterator}.
	 */
	protected class ConcurrentCollectionIterator extends ConcurrentIterator<E> {

		protected ConcurrentCollectionIterator(Object[] snapshot, int index) {
			super(snapshot, index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			if (this.last < 0)
				throw new IllegalStateException();

			try {
				AtomicCollection.this.remove(this.snapshot[this.last]);
				this.snapshot = AtomicCollection.this.toArray();
				this.cursor = this.last;
				this.last = -1;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

	}

}
