package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.atomic.AtomicSet;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe {@link Set} extension combining the {@link ConcurrentCollection} surface with
 * the no-duplicate semantics of {@link Set}.
 *
 * @param <E> the type of elements in this set
 */
public interface ConcurrentSet<E> extends ConcurrentCollection<E>, Set<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableSet<E> toUnmodifiable();

	/**
	 * Creates a new empty {@link ConcurrentSet} backed by a {@link HashSet}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent set
	 */
	static <E> @NotNull ConcurrentSet<E> empty() {
		return new Impl<E>();
	}

	/**
	 * Creates a new {@link ConcurrentSet} containing the given elements.
	 *
	 * @param elements the elements to include
	 * @param <E> the element type
	 * @return a new concurrent set containing the specified elements
	 */
	@SafeVarargs
	static <E> @NotNull ConcurrentSet<E> of(@NotNull E... elements) {
		return new Impl<>(elements);
	}

	/**
	 * Creates a new {@link ConcurrentSet} containing all elements of the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty set
	 * @param <E> the element type
	 * @return a new concurrent set containing the source's elements
	 */
	static <E> @NotNull ConcurrentSet<E> from(@Nullable Collection<? extends E> collection) {
		return new Impl<E>(collection);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentSet} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to
	 * {@code backing} bypass the read/write lock and may corrupt concurrent reads. Use this for
	 * zero-copy publication of single-threaded build results.
	 *
	 * @param backing the set to adopt
	 * @param <E> the element type
	 * @return a concurrent set backed by {@code backing}
	 */
	static <E> @NotNull ConcurrentSet<E> adopt(@NotNull AbstractSet<E> backing) {
		return new Impl<>(backing);
	}

	/**
	 * A thread-safe set backed by a {@link HashSet} with concurrent read and write access via
	 * {@link ReadWriteLock}. Enforces no-duplicate semantics with snapshot-based iteration.
	 *
	 * @param <E> the type of elements in this set
	 */
	class Impl<E> extends AtomicSet<E, AbstractSet<E>> implements ConcurrentSet<E> {

		/**
		 * Creates a new concurrent set.
		 */
		public Impl() {
			super(new HashSet<>());
		}

		/**
		 * Creates a new concurrent set and fills it with the given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			this(Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent set and fills it with the given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty set
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new HashSet<>() : new HashSet<>(collection));
		}

		/**
		 * Creates a new concurrent set with the given backing set.
		 *
		 * @param backingSet the backing set implementation
		 */
		protected Impl(@NotNull AbstractSet<E> backingSet) {
			super(backingSet);
		}

		/**
		 * Constructs a {@code ConcurrentSet.Impl} with a pre-built backing set and an explicit
		 * lock. Used by {@link ConcurrentUnmodifiableSet.Impl} (and its variants) to install a
		 * snapshot set paired with a no-op lock for wait-free reads.
		 *
		 * @param backingSet the pre-built backing set
		 * @param lock the lock guarding {@code backingSet}
		 */
		protected Impl(@NotNull AbstractSet<E> backingSet, @NotNull ReadWriteLock lock) {
			super(backingSet, lock);
		}

		/**
		 * Creates a new empty {@code ConcurrentSet.Impl} instance, used internally for copy
		 * operations.
		 *
		 * @return a new empty {@link ConcurrentSet.Impl}
		 */
		@Override
		protected @NotNull AtomicCollection<E, AbstractSet<E>> newEmpty() {
			return new ConcurrentSet.Impl<>();
		}

		/**
		 * Returns a type-preserving snapshot of this set's backing reference, captured under the
		 * read lock. Subclasses backed by a different concrete {@link AbstractSet} implementation
		 * override this to return an instance of that type so iteration order is preserved on the
		 * snapshot.
		 *
		 * @return a fresh {@link AbstractSet} containing the current elements
		 */
		protected @NotNull AbstractSet<E> cloneRef() {
			return this.withReadLock(() -> new HashSet<>(this.ref));
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentSet.Impl}.
		 *
		 * <p>The returned wrapper owns a fresh copy of the current elements - subsequent mutations
		 * on this set are not reflected in the snapshot. Reads on the snapshot are wait-free.
		 * The runtime type is {@link ConcurrentUnmodifiableSet.Impl}; the declared return type is
		 * the mutable parent so subclasses can covariantly override to their own
		 * {@code ConcurrentUnmodifiable*} variant.</p>
		 *
		 * @return an immutable snapshot - runtime type is {@link ConcurrentUnmodifiableSet.Impl}
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableSet<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableSet.Impl<>(this.cloneRef());
		}

	}

}
