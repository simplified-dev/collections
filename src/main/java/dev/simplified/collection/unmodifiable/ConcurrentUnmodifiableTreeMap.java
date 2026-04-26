package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.tree.ConcurrentTreeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An immutable snapshot of a {@link ConcurrentTreeMap} preserving the source's comparator
 * and sort order. The wrapper owns a fresh {@link TreeMap} copy and never reflects subsequent
 * mutations on the source. Reads on the snapshot are wait-free, backed by
 * {@link NoOpReadWriteLock}.
 *
 * <p>Every mutating operation rejects with {@link UnsupportedOperationException}.</p>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentUnmodifiableTreeMap<K, V> extends ConcurrentTreeMap<K, V> {

	/**
	 * Wraps the given pre-cloned snapshot reference. Callers should obtain {@code snapshot}
	 * by copying a source map's entries under that source's read lock.
	 *
	 * @param snapshot a freshly cloned backing map
	 */
	public ConcurrentUnmodifiableTreeMap(@NotNull TreeMap<K, V> snapshot) {
		super(snapshot, NoOpReadWriteLock.INSTANCE);
	}

	/** {@inheritDoc} */
	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @Nullable V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @Nullable V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @Nullable V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final void putAll(@NotNull Map<? extends K, ? extends V> map) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean putIf(@NotNull Supplier<Boolean> predicate, K key, V value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean putIf(@NotNull BiPredicate<? super K, ? super V> predicate, K key, V value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean putIf(@NotNull Predicate<AbstractMap<K, V>> predicate, K key, V value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @Nullable V putIfAbsent(K key, V value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final @Nullable V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean remove(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean removeIf(@NotNull BiPredicate<? super K, ? super V> predicate) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean removeIf(@NotNull Predicate<? super Entry<K, V>> predicate) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public @NotNull ConcurrentTreeMap<K, V> toUnmodifiable() {
		return this;
	}

}
