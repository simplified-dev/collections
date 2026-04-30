package dev.simplified.collection.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import dev.simplified.collection.ConcurrentArrayDeque;
import dev.simplified.collection.ConcurrentArrayList;
import dev.simplified.collection.ConcurrentArrayQueue;
import dev.simplified.collection.ConcurrentCollection;
import dev.simplified.collection.ConcurrentDeque;
import dev.simplified.collection.ConcurrentHashMap;
import dev.simplified.collection.ConcurrentHashSet;
import dev.simplified.collection.ConcurrentLinkedHashMap;
import dev.simplified.collection.ConcurrentLinkedHashSet;
import dev.simplified.collection.ConcurrentLinkedList;
import dev.simplified.collection.ConcurrentLinkedMap;
import dev.simplified.collection.ConcurrentLinkedSet;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.ConcurrentQueue;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.ConcurrentTreeMap;
import dev.simplified.collection.ConcurrentTreeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Gson {@link TypeAdapterFactory} that teaches Gson how to deserialize the
 * {@code dev.simplified.collection.Concurrent*} interface types.
 * <p>
 * Each interface is mapped to its inner {@code Impl} class - a concrete
 * {@link java.util.Collection} or {@link java.util.Map} subtype with a public
 * no-arg constructor that Gson's built-in collection and map adapters can
 * instantiate. When Gson resolves a field declared as one of the supported
 * interfaces, this factory returns the adapter for the matching {@code Impl},
 * preserving the declared generic arguments so element, key, and value types
 * round-trip correctly.
 * <p>
 * The factory is registered as a Gson SPI via
 * {@code META-INF/services/com.google.gson.TypeAdapterFactory}, so any consumer
 * that loads {@link TypeAdapterFactory} via {@link java.util.ServiceLoader}
 * picks it up automatically. Manual users can also register an instance
 * directly with {@link com.google.gson.GsonBuilder#registerTypeAdapterFactory}.
 *
 * @see TypeAdapterFactory
 */
public final class ConcurrentTypeAdapterFactory implements TypeAdapterFactory {

    private static final Map<Class<?>, Class<?>> INTERFACE_TO_IMPL = Map.ofEntries(
        Map.entry(ConcurrentCollection.class, ConcurrentArrayList.class),
        Map.entry(ConcurrentList.class, ConcurrentArrayList.class),
        Map.entry(ConcurrentLinkedList.class, ConcurrentLinkedList.Impl.class),
        Map.entry(ConcurrentSet.class, ConcurrentHashSet.class),
        Map.entry(ConcurrentLinkedSet.class, ConcurrentLinkedHashSet.class),
        Map.entry(ConcurrentTreeSet.class, ConcurrentTreeSet.Impl.class),
        Map.entry(ConcurrentQueue.class, ConcurrentArrayQueue.class),
        Map.entry(ConcurrentDeque.class, ConcurrentArrayDeque.class),
        Map.entry(ConcurrentMap.class, ConcurrentHashMap.class),
        Map.entry(ConcurrentLinkedMap.class, ConcurrentLinkedHashMap.class),
        Map.entry(ConcurrentTreeMap.class, ConcurrentTreeMap.Impl.class)
    );

    /** {@inheritDoc} */
    @Override
    public <T> @Nullable TypeAdapter<T> create(@NotNull Gson gson, @NotNull TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        Class<?> impl = INTERFACE_TO_IMPL.get(rawType);

        if (impl == null)
            return null;

        TypeToken<?> implToken = implToken(impl, typeToken.getType());

        @SuppressWarnings("unchecked")
        TypeAdapter<T> delegate = (TypeAdapter<T>) gson.getAdapter(implToken);
        return delegate;
    }

    private static @NotNull TypeToken<?> implToken(@NotNull Class<?> impl, @NotNull Type declaredType) {
        if (declaredType instanceof ParameterizedType parameterized)
            return TypeToken.getParameterized(impl, parameterized.getActualTypeArguments());

        return TypeToken.get(impl);
    }

}
