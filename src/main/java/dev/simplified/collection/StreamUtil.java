package dev.simplified.collection;

import dev.simplified.collection.function.IndexedFunction;
import dev.simplified.collection.tuple.triple.Triple;
import dev.simplified.collection.tuple.triple.TripleStream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility methods for working with {@link Stream} instances, including indexed mapping,
 * element modification, and specialized collectors.
 */
@UtilityClass
public final class StreamUtil {

    /**
     * Returns a stateful predicate that filters elements to only those with distinct keys,
     * as extracted by the given function.
     *
     * @param <T> the type of input to the predicate
     * @param keyExtractor a function that extracts a key from an element
     * @return a predicate that returns {@code true} for elements with unique keys and {@code false} otherwise
     */
    public static <T> @NotNull Predicate<T> distinctByKey(@NotNull Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Concatenates the contents of a variable number of arrays into a single unified stream.
     *
     * <p>Null arrays are filtered out.
     *
     * @param <T> the type of elements in the arrays and the resulting stream
     * @param arrays the arrays to be combined into a single stream; may include {@code null} entries
     * @return a stream containing all the elements from the provided arrays
     */
    public static <T> @NotNull Stream<T> ofArrays(@Nullable T[]... arrays) {
        return Arrays.stream(arrays)
            .filter(Objects::nonNull)
            .flatMap(Arrays::stream);
    }

    /**
     * Zips the specified stream with its indices, producing a {@link TripleStream} of
     * {@code (element, index, size)}.
     *
     * @param <T> the element type
     * @param stream the source stream
     * @return a triple stream where each triple contains the element, its zero-based index, and the estimated size
     */
    public static <T> @NotNull TripleStream<T, Long, Long> zipWithIndex(@NotNull Stream<T> stream) {
        return zipWithIndex(stream.spliterator(), stream.isParallel());
    }

    /**
     * Zips the specified spliterator with its indices, producing a {@link TripleStream} of
     * {@code (element, index, size)}.
     *
     * @param <T> the element type
     * @param spliterator the source spliterator
     * @param parallel whether the resulting stream should be parallel
     * @return a triple stream where each triple contains the element, its zero-based index, and the estimated size
     */
    public static <T> @NotNull TripleStream<T, Long, Long> zipWithIndex(@NotNull Spliterator<T> spliterator, boolean parallel) {
        return TripleStream.of(mapWithIndex(spliterator, parallel, Triple::of));
    }

    /**
     * Wraps the given stream into an indexed stream of {@link Triple} elements containing
     * {@code (element, index, size)}.
     *
     * @param <T> the element type
     * @param stream the source stream
     * @return a stream of triples containing each element, its zero-based index, and the estimated size
     */
    public static <T> @NotNull Stream<Triple<T, Long, Long>> indexedStream(@NotNull Stream<T> stream) {
        return indexedStream(stream.spliterator(), stream.isParallel()).onClose(stream::close);
    }

    /**
     * Wraps the given spliterator into an indexed stream of {@link Triple} elements containing
     * {@code (element, index, size)}.
     *
     * @param <T> the element type
     * @param spliterator the source spliterator
     * @param parallel whether the resulting stream should be parallel
     * @return a stream of triples containing each element, its zero-based index, and the estimated size
     */
    public static <T> @NotNull Stream<Triple<T, Long, Long>> indexedStream(@NotNull Spliterator<T> spliterator, boolean parallel) {
        return mapWithIndex(spliterator, parallel, Triple::of);
    }

    /**
     * Applies the given function to each element of the stream along with its zero-based index
     * and the estimated stream size. The index and size are primitive {@code long}s - no
     * boxing per element.
     *
     * @param <T> the input element type
     * @param <R> the output element type
     * @param stream the source stream
     * @param function a function accepting {@code (element, index, size)} and producing the mapped result
     * @return a stream of mapped results
     */
    @SuppressWarnings("unchecked")
    public static <T, R> @NotNull Stream<R> mapWithIndex(@NotNull Stream<T> stream, @NotNull IndexedFunction<? super T, ? extends R> function) {
        return (Stream<R>) mapWithIndex(stream.spliterator(), stream.isParallel(), function).onClose(stream::close);
    }

    /**
     * Returns a stream consisting of the results of applying the given function to the elements
     * of the source spliterator and their indices. For example,
     *
     * <pre>{@code
     * mapWithIndex(
     *     Stream.of("a", "b", "c").spliterator(), false,
     *     (str, index, size) -> str + ":" + index + "/" + size)
     * }</pre>
     *
     * <p>would return {@code Stream.of("a:0/3", "b:1/3", "c:2/3")}.
     *
     * <p>The resulting stream is <a
     * href="http://gee.cs.oswego.edu/dl/html/StreamParallelGuidance.html">efficiently splittable</a>
     * if and only if the source was efficiently splittable and its underlying spliterator
     * reported {@link Spliterator#SUBSIZED}. This is generally the case if the underlying stream
     * comes from a data structure supporting efficient indexed random access, typically an array
     * or list.
     *
     * <p>The order of the resulting stream is defined if and only if the order of the original
     * stream was defined.
     *
     * <p>Index and size are passed as primitive {@code long}s - no boxing per element.
     *
     * @param <T> the input element type
     * @param <R> the output element type
     * @param spliterator the source spliterator
     * @param parallel whether the resulting stream should be parallel
     * @param function a function accepting {@code (element, index, size)} and producing the mapped result
     * @return a stream of mapped results
     */
    public static <T, R> @NotNull Stream<R> mapWithIndex(@NotNull Spliterator<T> spliterator, boolean parallel, @NotNull IndexedFunction<? super T, ? extends R> function) {
        long size = spliterator.estimateSize();

        if (!spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<>(size, spliterator.characteristics() & (Spliterator.ORDERED | Spliterator.SIZED)) {
                    private long index = 0;
                    private @Nullable T holder;
                    private final Consumer<T> capture = t -> this.holder = t;

                    @Override
                    public boolean tryAdvance(Consumer<? super R> action) {
                        if (spliterator.tryAdvance(this.capture)) {
                            try {
                                action.accept(function.apply(this.holder, this.index++, size));
                                return true;
                            } finally {
                                this.holder = null;
                            }
                        }

                        return false;
                    }
                },
                parallel
            );
        } else {
            class Splitr extends MapWithIndexSpliterator<Spliterator<T>, R, Splitr> implements Consumer<T> {

                private @Nullable T holder;

                Splitr(Spliterator<T> splitr, long index) {
                    super(splitr, index);
                }

                @Override
                public void accept(@NotNull T t) {
                    this.holder = t;
                }

                @Override
                public boolean tryAdvance(Consumer<? super R> action) {
                    if (fromSpliterator.tryAdvance(this)) {
                        try {
                            // The cast is safe because tryAdvance puts a T into `holder`.
                            action.accept(function.apply(this.holder, this.index++, size));
                            return true;
                        } finally {
                            holder = null;
                        }
                    }
                    return false;
                }

                @Override
                @NotNull Splitr createSplit(Spliterator<T> from, long i) {
                    return new Splitr(from, i);
                }

            }

            return StreamSupport.stream(new Splitr(spliterator, 0), parallel);
        }
    }

    /**
     * Transforms each element in the stream using a function that also receives the element's
     * zero-based index and the estimated stream size. The index and size are primitive
     * {@code long}s - no boxing per element.
     *
     * @param <T> the element type
     * @param stream the source stream
     * @param modFunction a function accepting {@code (element, index, size)} and returning the modified element
     * @return a stream of modified elements
     */
    public static <T> @NotNull Stream<T> modifyStream(@NotNull Stream<T> stream, @NotNull IndexedFunction<T, T> modFunction) {
        return mapWithIndex(stream, modFunction);
    }

    /**
     * Appends the given value to each element in the string stream.
     *
     * @param stringStream the source stream of strings
     * @param entryValue the value to append to every element
     * @return a stream with the value appended to each element
     */
    public static @NotNull Stream<String> appendEach(@NotNull Stream<String> stringStream, @NotNull String entryValue) {
        return appendEach(stringStream, entryValue, entryValue);
    }

    /**
     * Appends a value to each element in the string stream, using a different value for
     * the last element.
     *
     * @param stringStream the source stream of strings
     * @param entryValue the value to append to all elements except the last
     * @param lastEntry the value to append to the last element
     * @return a stream with the appropriate value appended to each element
     */
    public static @NotNull Stream<String> appendEach(@NotNull Stream<String> stringStream, @NotNull String entryValue, @NotNull String lastEntry) {
        return modifyStream(stringStream, (value, index, size) -> value + (index < size - 1 ? entryValue : lastEntry));
    }

    /**
     * Prepends the given value to each element in the string stream.
     *
     * @param stringStream the source stream of strings
     * @param entryValue the value to prepend to every element
     * @return a stream with the value prepended to each element
     */
    public static @NotNull Stream<String> prependEach(@NotNull Stream<String> stringStream, @NotNull String entryValue) {
        return prependEach(stringStream, entryValue, entryValue);
    }

    /**
     * Prepends a value to each element in the string stream, using a different value for
     * the last element.
     *
     * @param stringStream the source stream of strings
     * @param entryValue the value to prepend to all elements except the last
     * @param lastEntry the value to prepend to the last element
     * @return a stream with the appropriate value prepended to each element
     */
    public static @NotNull Stream<String> prependEach(@NotNull Stream<String> stringStream, @NotNull String entryValue, @NotNull String lastEntry) {
        return modifyStream(stringStream, (value, index, size) -> (index < size - 1 ? entryValue : lastEntry) + value);
    }

    /**
     * Collects stream elements into a {@link StringBuilder}, appending a system line separator
     * after each element.
     *
     * @param <E> the element type
     * @return a collector that produces a {@link StringBuilder}
     */
    public static <E> @NotNull Collector<E, ?, StringBuilder> toStringBuilder() {
        return toStringBuilder(true);
    }

    /**
     * Collects stream elements into a {@link StringBuilder}, optionally appending a system
     * line separator after each element.
     *
     * @param <E> the element type
     * @param newLine {@code true} to append a line separator after each element, {@code false} to concatenate directly
     * @return a collector that produces a {@link StringBuilder}
     */
    public static <E> @NotNull Collector<E, ?, StringBuilder> toStringBuilder(boolean newLine) {
        return Collector.of(
            StringBuilder::new,
            newLine ? (builder, element) -> builder.append(element).append(System.lineSeparator()) : StringBuilder::append,
            (left, right) -> {
                if (newLine)
                    left.append(right).append(System.lineSeparator());
                else
                    left.append(right);

                return left;
            }
        );
    }

    /**
     * Collects stream elements into a {@link StringBuilder}, inserting the given separator
     * after each element.
     *
     * @param <E> the element type
     * @param separator the string to insert between elements
     * @return a collector that produces a {@link StringBuilder}
     */
    public static <E> @NotNull Collector<E, ?, StringBuilder> toStringBuilder(@NotNull String separator) {
        return Collector.of(
            StringBuilder::new,
            (builder, element) -> builder.append(element).append(separator),
            StringBuilder::append
        );
    }

    /**
     * Abstract spliterator that tracks a running index while delegating element traversal
     * to a wrapped source spliterator.
     *
     * @param <F> the type of the source spliterator
     * @param <R> the type of elements produced by this spliterator
     * @param <S> the self type for recursive split creation
     */
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    private static abstract class MapWithIndexSpliterator<F extends Spliterator<?>, R, S extends StreamUtil.MapWithIndexSpliterator<F, R, S>> implements Spliterator<R> {

        protected final F fromSpliterator;
        protected long index;

        abstract S createSplit(F from, long i);

        @Override
        @SuppressWarnings("unchecked")
        public S trySplit() {
            Spliterator<?> splitOrNull = this.fromSpliterator.trySplit();

            if (splitOrNull == null)
                return null;

            F split = (F) splitOrNull;
            S result = createSplit(split, index);
            this.index += split.getExactSizeIfKnown();
            return result;
        }

        @Override
        public long estimateSize() {
            return this.fromSpliterator.estimateSize();
        }

        @Override
        public int characteristics() {
            return this.fromSpliterator.characteristics() & (Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);
        }

    }

}
