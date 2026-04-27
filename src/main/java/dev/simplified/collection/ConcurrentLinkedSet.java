package dev.simplified.collection;

import dev.simplified.collection.atomic.AtomicCollection;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableLinkedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe concurrent set variant backed by a {@link LinkedHashSet} that maintains insertion
 * order while enforcing no-duplicate semantics.
 *
 * @param <E> the type of elements in this set
 */
public interface ConcurrentLinkedSet<E> extends ConcurrentSet<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull ConcurrentUnmodifiableLinkedSet<E> toUnmodifiable();

	/**
	 * Creates a new empty {@link ConcurrentLinkedSet} backed by a {@link LinkedHashSet}.
	 *
	 * @param <E> the element type
	 * @return a new empty concurrent linked set
	 */
	static <E> @NotNull ConcurrentLinkedSet<E> empty() {
		return new Impl<E>();
	}

	/**
	 * Creates a new {@link ConcurrentLinkedSet} containing the given elements.
	 *
	 * @param elements the elements to include
	 * @param <E> the element type
	 * @return a new concurrent linked set containing the specified elements
	 */
	@SafeVarargs
	static <E> @NotNull ConcurrentLinkedSet<E> of(@NotNull E... elements) {
		return new Impl<>(elements);
	}

	/**
	 * Creates a new {@link ConcurrentLinkedSet} containing all elements of the given collection.
	 *
	 * @param collection the source collection to copy from, or {@code null} for an empty set
	 * @param <E> the element type
	 * @return a new concurrent linked set containing the source's elements
	 */
	static <E> @NotNull ConcurrentLinkedSet<E> from(@Nullable Collection<? extends E> collection) {
		return new Impl<E>(collection);
	}

	/**
	 * Wraps {@code backing} as a {@link ConcurrentLinkedSet} without copying.
	 * <p>
	 * The caller relinquishes exclusive ownership: subsequent direct mutations to
	 * {@code backing} bypass the read/write lock and may corrupt concurrent reads. Use this for
	 * zero-copy publication of single-threaded build results.
	 *
	 * @param backing the linked hash set to adopt
	 * @param <E> the element type
	 * @return a concurrent linked set backed by {@code backing}
	 */
	static <E> @NotNull ConcurrentLinkedSet<E> adopt(@NotNull LinkedHashSet<E> backing) {
		return new Impl<>(backing);
	}

	/**
	 * A thread-safe set backed by a {@link LinkedHashSet} with concurrent read and write access via
	 * {@link ReadWriteLock}. Maintains insertion order while enforcing no-duplicate semantics.
	 *
	 * @param <E> the type of elements in this set
	 */
	class Impl<E> extends ConcurrentSet.Impl<E> implements ConcurrentLinkedSet<E> {

		/**
		 * Creates a new concurrent linked set.
		 */
		public Impl() {
			super(new LinkedHashSet<>());
		}

		/**
		 * Creates a new concurrent linked set and fills it with the given array.
		 *
		 * @param array the elements to include
		 */
		@SafeVarargs
		public Impl(@NotNull E... array) {
			this(Arrays.asList(array));
		}

		/**
		 * Creates a new concurrent linked set and fills it with the given collection.
		 *
		 * @param collection the source collection to copy from, or {@code null} for an empty set
		 */
		public Impl(@Nullable Collection<? extends E> collection) {
			super(collection == null ? new LinkedHashSet<>() : new LinkedHashSet<>(collection));
		}

		/**
		 * Constructs a {@code ConcurrentLinkedSet.Impl} that adopts {@code backingSet} as its
		 * storage with a fresh lock. Public callers should go through
		 * {@link ConcurrentLinkedSet#adopt(LinkedHashSet)}.
		 *
		 * @param backingSet the backing linked hash set to adopt
		 */
		protected Impl(@NotNull LinkedHashSet<E> backingSet) {
			super(backingSet);
		}

		/**
		 * Constructs a {@code ConcurrentLinkedSet.Impl} with a pre-built backing set and an
		 * explicit lock. Used by {@link ConcurrentUnmodifiableLinkedSet.Impl} to install a snapshot
		 * set paired with a no-op lock for wait-free reads.
		 *
		 * @param backingSet the pre-built backing set
		 * @param lock the lock guarding {@code backingSet}
		 */
		protected Impl(@NotNull LinkedHashSet<E> backingSet, @NotNull ReadWriteLock lock) {
			super(backingSet, lock);
		}

		/**
		 * Creates a new empty {@code ConcurrentLinkedSet.Impl} instance, used internally for copy
		 * operations.
		 *
		 * @return a new empty {@link ConcurrentLinkedSet.Impl}
		 */
		@Override
		protected @NotNull AtomicCollection<E, AbstractSet<E>> newEmpty() {
			return new ConcurrentLinkedSet.Impl<>();
		}

		/**
		 * {@inheritDoc}
		 *
		 * <p>Overrides {@link ConcurrentSet.Impl#cloneRef()} to produce a {@link LinkedHashSet}
		 * snapshot preserving the source's insertion-order traversal characteristics.</p>
		 */
		@Override
		protected @NotNull AbstractSet<E> cloneRef() {
			return this.withReadLock(() -> new LinkedHashSet<>(this.ref));
		}

		/**
		 * Returns an immutable snapshot of this {@code ConcurrentLinkedSet.Impl} preserving
		 * insertion order.
		 *
		 * @return an unmodifiable {@link ConcurrentLinkedSet.Impl} containing a snapshot of the
		 *         elements
		 */
		@Override
		public @NotNull ConcurrentUnmodifiableLinkedSet<E> toUnmodifiable() {
			return new ConcurrentUnmodifiableLinkedSet.Impl<>((LinkedHashSet<E>) this.cloneRef());
		}

	}

}
