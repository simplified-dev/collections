package dev.simplified.collection.unmodifiable;

import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.atomic.AtomicMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A live, unmodifiable view over any {@link AtomicMap}. Shares the source map's {@code ref}
 * and lock, so the wrapper reflects current source state (iteration order, size, contents),
 * but every mutating operation rejects with {@link UnsupportedOperationException}.
 * <p>
 * Because {@link AtomicMap}'s view classes ({@code entrySet}, {@code keySet}, {@code values})
 * route all mutations through overridable {@code AtomicMap} public methods, overriding those
 * methods here is sufficient to make iterator removals, {@code Entry.setValue}, and every
 * other view-level write throw as well.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentUnmodifiableMap<K, V> extends ConcurrentMap<K, V> {

	/**
	 * Creates a live, unmodifiable view over the given source. The wrapper shares the source's
	 * underlying map and lock, so source mutations are visible through this wrapper.
	 *
	 * @param source the source whose state is shared with this unmodifiable view
	 */
	public ConcurrentUnmodifiableMap(@NotNull AtomicMap<K, V, ? extends AbstractMap<K, V>> source) {
		super(source);
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
	public final @NotNull ConcurrentMap<K, V> toUnmodifiable() {
		return this;
	}

}
