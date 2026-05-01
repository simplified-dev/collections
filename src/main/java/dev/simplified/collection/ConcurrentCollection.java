package dev.simplified.collection;

import dev.simplified.collection.query.Searchable;
import dev.simplified.collection.tuple.single.SingleStream;
import dev.simplified.collection.tuple.triple.TripleStream;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
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
	@NotNull ConcurrentCollection<E> toUnmodifiable();

}
