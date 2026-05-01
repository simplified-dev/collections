package dev.simplified.collection;

import dev.simplified.collection.query.Searchable;
import dev.simplified.collection.tuple.pair.PairStream;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * A thread-safe {@link Map} extension exposing the project-specific concurrent surface shared
 * by every concurrent map variant in this library.
 *
 * <p>Implementations carry atomic read and write semantics, snapshot-based iteration, and
 * conditional mutation primitives in addition to the standard JDK {@link Map} contract.</p>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface ConcurrentMap<K, V> extends Map<K, V>, Iterable<Map.Entry<K, V>>, Searchable<Map.Entry<K, V>>, Serializable {

	/**
	 * Returns an {@link Optional} containing the value mapped to the specified key, or an empty
	 * {@code Optional} if no mapping exists.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return an {@code Optional} describing the mapped value, or an empty {@code Optional}
	 */
	@NotNull Optional<V> getOptional(Object key);

	/**
	 * Returns {@code true} if this map contains at least one key-value mapping.
	 *
	 * @return {@code true} if this map is not empty
	 */
	boolean notEmpty();

	/**
	 * Returns a parallel {@link PairStream} over the entries of this map.
	 *
	 * @return a parallel stream of this map's key-value pairs
	 */
	@NotNull PairStream<K, V> parallelStream();

	/**
	 * Associates the key from the given entry with its value in this map.
	 *
	 * @param entry the entry containing the key-value pair to put
	 * @return the previous value associated with the key, or {@code null} if there was none
	 */
	V put(@NotNull Entry<K, V> entry);

	/**
	 * Puts the specified key-value pair into this map only if the given supplier returns
	 * {@code true}.
	 *
	 * @param predicate the supplier that determines whether the entry should be added
	 * @param key the key to associate
	 * @param value the value to associate with the key
	 * @return {@code true} if the entry was added
	 */
	boolean putIf(@NotNull Supplier<Boolean> predicate, K key, V value);

	/**
	 * Puts the specified key-value pair into this map only if any existing entry matches the
	 * given bi-predicate.
	 *
	 * @param predicate the bi-predicate tested against existing keys and values
	 * @param key the key to associate
	 * @param value the value to associate with the key
	 * @return {@code true} if the entry was added
	 */
	boolean putIf(@NotNull BiPredicate<? super K, ? super V> predicate, K key, V value);

	/**
	 * Removes all entries from this map for which the given bi-predicate returns {@code true}.
	 *
	 * @param predicate the bi-predicate tested against each entry's key and value
	 * @return {@code true} if any entries were removed
	 */
	boolean removeIf(@NotNull BiPredicate<? super K, ? super V> predicate);

	/**
	 * Removes all entries from this map for which the given entry predicate returns {@code true}.
	 *
	 * @param predicate the predicate tested against each entry
	 * @return {@code true} if any entries were removed
	 */
	boolean removeIf(@NotNull java.util.function.Predicate<? super Entry<K, V>> predicate);

	/**
	 * Removes and returns the value associated with the specified key, or returns the default
	 * value if no mapping exists.
	 *
	 * @param key the key whose mapping is to be removed
	 * @param defaultValue the value to return if no mapping exists for the key
	 * @return the removed value, or {@code defaultValue} if no mapping was found
	 */
	V removeOrGet(Object key, V defaultValue);

	/**
	 * Returns a sequential {@link PairStream} over the entries of this map.
	 *
	 * @return a sequential stream of this map's key-value pairs
	 */
	@NotNull PairStream<K, V> stream();

	/**
	 * Returns an immutable snapshot of this map.
	 *
	 * <p>The returned wrapper owns a fresh copy of the current entries, so subsequent mutations
	 * on this map are not reflected in the snapshot.</p>
	 *
	 * @return an immutable snapshot of this map
	 */
	@NotNull ConcurrentMap<K, V> toUnmodifiable();

}
