package dev.sbs.api.collection.stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class StreamCollector<T, A, R> implements Collector<T, A, R> {

    @SuppressWarnings("unchecked")
    private static <I, R> Function<I, R> castingIdentity() {
        return i -> (R) i;
    }

    private final @NotNull Supplier<A> supplier;
    private final @NotNull BiConsumer<A, T> accumulator;
    private final @NotNull BinaryOperator<A> combiner;
    private @NotNull Function<A, R> finisher = castingIdentity();
    private final @NotNull Set<Characteristics> characteristics;

}