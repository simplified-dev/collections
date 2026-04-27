package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.query.Searchable;
import dev.simplified.collection.tuple.single.SingleStream;
import dev.simplified.collection.tuple.triple.TripleStream;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A thread-safe {@link Collection} extension exposing the project-specific concurrent surface
 * shared by every concurrent collection variant in this library.
 *
 * <p>Implementations carry atomic read and write semantics, snapshot-based iteration, and
 * conditional mutation primitives in addition to the standard JDK {@link Collection} contract.</p>
 *
 * @param <E> the type of elements in this collection
 */
public interface ConcurrentCollection<E> extends Collection<E>, Searchable<E>, Serializable {

	/**
	 * Adds all of the specified elements to this collection.
	 *
	 * @param collection the elements to be added to this collection
	 * @return {@code true} if this collection changed as a result of the call
	 */
	boolean addAll(@NotNull E... collection);

	/**
	 * Adds the specified element to this collection only if the given supplier returns {@code true}.
	 *
	 * @param predicate the supplier that determines whether the element should be added
	 * @param element the element to add
	 * @return {@code true} if the element was added to this collection
	 */
	boolean addIf(@NotNull Supplier<Boolean> predicate, @NotNull E element);

	/**
	 * Returns {@code true} if this collection contains an element whose value, extracted by the
	 * given function, equals the specified value.
	 *
	 * @param <S> the type of the extracted value
	 * @param function the function to extract a value from each element
	 * @param value the value to search for
	 * @return {@code true} if a matching element is found
	 */
	<S> boolean contains(@NotNull Function<E, S> function, S value);

	/**
	 * Returns a sequential {@link TripleStream} where each element is paired with its index and
	 * the total size.
	 *
	 * @return a sequential indexed stream of this collection's elements
	 */
	@NotNull TripleStream<E, Long, Long> indexedStream();

	/**
	 * Returns a {@link TripleStream} where each element is paired with its index and the total
	 * size, optionally in parallel.
	 *
	 * @param parallel {@code true} to create a parallel stream, {@code false} for sequential
	 * @return an indexed stream of this collection's elements
	 */
	@NotNull TripleStream<E, Long, Long> indexedStream(boolean parallel);

	/**
	 * Returns {@code true} if this collection does not contain the specified element.
	 *
	 * @param item the element whose absence is to be tested
	 * @return {@code true} if this collection does not contain the specified element
	 */
	boolean notContains(Object item);

	/**
	 * Returns {@code true} if this collection contains at least one element.
	 *
	 * @return {@code true} if this collection is not empty
	 */
	boolean notEmpty();

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
	boolean replace(@NotNull E existingElement, @NotNull E replaceWith);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull SingleStream<E> stream();

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull SingleStream<E> parallelStream();

	/**
	 * Returns an immutable snapshot of this collection.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current contents, so subsequent mutations
	 * on this collection are not reflected in the snapshot.</p>
	 *
	 * @return an immutable snapshot of this collection
	 */
	@NotNull ConcurrentUnmodifiableCollection<E> toUnmodifiable();

	/**
	 * Creates a new empty {@link ConcurrentCollection} backed by an {@link ArrayList}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent collection
	 */
	static <E> @NotNull ConcurrentCollection<E> empty() {
		return new Impl<>();
	}

	/**
	 * Creates a new {@link ConcurrentCollection} containing the given elements.
	 *
	 * @param elements the elements to include
	 * @param <E> the element type
	 * @return a new concurrent collection containing the specified elements
	 */
	@SafeVarargs
	static <E> @NotNull ConcurrentCollection<E> of(@NotNull E... elements) {
		return new Impl<>(elements);
	}

	/**
	 * Creates a new {@link ConcurrentCollection} containing all elements of the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty collection
	 * @param <E> the element type
	 * @return a new concurrent collection containing the source's elements
	 */
	static <E> @NotNull ConcurrentCollection<E> from(@Nullable Collection<? extends E> collection) {
		return new Impl<>(collection);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentCollection} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to
	 * {@code backing} bypass the read/write lock and may corrupt concurrent reads. Use this for
	 * zero-copy publication of single-threaded build results.
	 *
	 * @param backing the collection to adopt
	 * @param <E> the element type
	 * @return a concurrent collection backed by {@code backing}
	 */
	static <E> @NotNull ConcurrentCollection<E> adopt(@NotNull AbstractCollection<E> backing) {
		return new Impl<>(backing);
	}

	/**
	 * A thread-safe collection backed by an {@link AbstractCollection} with concurrent read and
	 * write access via {@link ReadWriteLock}. Provides the base concrete implementation of
	 * {@link AtomicCollection}.
	 *
	 * @param <E> the type of elements in this collection
	 */
	class Impl<E> extends AtomicCollection<E, AbstractCollection<E>> implements ConcurrentCollection<E> {

		/**
		 * Creates a new concurrent collection.
		 */
		public Impl() {
			super(new ArrayList<>());
		}

		/**
		 * Creates a new concurrent collection and fills it with the given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			this(Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent collection and fills it with the given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty
		 *                   collection
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new ArrayList<>() : new ArrayList<>(collection));
		}

		/**
		 * Constructs a {@code ConcurrentCollection.Impl} that adopts {@code backingCollection} as
		 * its storage with a fresh lock. Public callers should go through
		 * {@link ConcurrentCollection#adopt(AbstractCollection)}.
		 *
		 * @param backingCollection the backing collection to adopt
		 */
		protected Impl(@NotNull AbstractCollection<E> backingCollection) {
			super(backingCollection);
		}

		/**
		 * Constructs a {@code ConcurrentCollection.Impl} with a pre-built backing collection and an
		 * explicit lock. Used by {@link ConcurrentUnmodifiableCollection.Impl} to install a snapshot
		 * collection paired with a no-op lock for wait-free reads.
		 *
		 * @param backingCollection the pre-built backing collection
		 * @param lock the lock guarding {@code backingCollection}
		 */
		protected Impl(@NotNull AbstractCollection<E> backingCollection, @NotNull ReadWriteLock lock) {
			super(backingCollection, lock);
		}

		/**
		 * Creates a new empty {@code ConcurrentCollection.Impl} instance, used internally for copy
		 * operations.
		 *
		 * @return a new empty {@link ConcurrentCollection.Impl}
		 */
		@Override
		protected final @NotNull AtomicCollection<E, AbstractCollection<E>> newEmpty() {
			return new ConcurrentCollection.Impl<>();
		}

		/**
		 * Returns a type-preserving snapshot of this collection's backing reference, captured under
		 * the read lock.
		 *
		 * @return a fresh {@link AbstractCollection} containing the current elements
		 */
		protected @NotNull AbstractCollection<E> cloneRef() {
			return this.withReadLock(() -> new ArrayList<>(this.ref));
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentCollection.Impl}.
		 *
		 * <p>The returned wrapper owns a fresh copy of the current contents - subsequent mutations
		 * on this collection are not reflected in the snapshot. Reads on the snapshot are wait-free.
		 * The runtime type is {@link ConcurrentUnmodifiableCollection.Impl}; the declared return
		 * type is the mutable parent so subclasses can covariantly override to their own
		 * {@code ConcurrentUnmodifiable*} variant.</p>
		 *
		 * @return an immutable snapshot - runtime type is {@link ConcurrentUnmodifiableCollection.Impl}
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableCollection<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableCollection.Impl<>(this.cloneRef());
		}

	}

}
