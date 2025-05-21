package dev.sbs.api.collection.concurrent.atomic;

import dev.sbs.api.collection.concurrent.iterator.ConcurrentIterator;
import dev.sbs.api.collection.search.Searchable;
import dev.sbs.api.collection.stream.StreamUtil;
import dev.sbs.api.collection.stream.triple.TripleStream;
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
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("all")
public abstract class AtomicCollection<E, T extends Collection<E>> extends AbstractCollection<E> implements Collection<E>, Searchable<E>, Serializable {

	protected final @NotNull T ref;
	protected final transient ReadWriteLock lock = new ReentrantReadWriteLock();

	protected AtomicCollection(@NotNull T ref) {
		this.ref = ref;
	}

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
		return (predicate.get() && this.add(element));
	}

	@Override
	public void clear() {
		try {
			this.lock.writeLock().lock();
			this.ref.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

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

	@Override
	public boolean containsAll(@NotNull Collection<?> collection) {
		try {
			this.lock.readLock().lock();
			return this.ref.containsAll(collection);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof AtomicCollection) obj = ((AtomicCollection<?, ?>) obj).ref;
		return this.ref.equals(obj);
	}

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

	@Override
	public final boolean isEmpty() {
		try {
			this.lock.readLock().lock();
			return this.ref.isEmpty();
		} finally {
			this.lock.readLock().unlock();
		}
	}

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

	@Override
	public final @NotNull Stream<E> parallelStream() {
		return StreamSupport.stream(this.spliterator(), true);
	}

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
	 * <br><br>
	 * This only adds the replaceWith element if it removes the
	 * existing element successfully.
	 *
	 * @param existingElement The element to be replaced.
	 * @param replaceWith The element to replace with.
	 */
	public final boolean replace(@NotNull E existingElement, @NotNull E replaceWith) {
		if (this.remove(existingElement))
			return this.add(replaceWith);

		return false;
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		try {
			this.lock.writeLock().lock();
			return this.ref.removeAll(collection);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		try {
			this.lock.writeLock().lock();
			return this.ref.retainAll(collection);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public final int size() {
		return this.ref.size();
	}

	@Override
	public final @NotNull Stream<E> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}

	@Override
	public @NotNull Object[] toArray() {
		return this.ref.toArray();
	}

	@Override
	@SuppressWarnings("SuspiciousToArrayCall")
	public <U> @NotNull U[] toArray(@NotNull U[] array) {
		return this.ref.toArray(array);
	}

	/**
	 * A concurrent version of {@link CopyOnWriteArrayList.COWIterator}.
	 */
	protected class ConcurrentCollectionIterator extends ConcurrentIterator<E> {

		protected ConcurrentCollectionIterator(Object[] snapshot, int index) {
			super(snapshot, index);
		}

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
