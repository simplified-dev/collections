package dev.sbs.api.util;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentSet;
import dev.sbs.api.tuple.triple.TriFunction;
import dev.sbs.api.tuple.triple.Triple;
import dev.sbs.api.tuple.triple.TripleStream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public final class StreamUtil {

    /**
     * Returns a predicate that maintains state about previously encountered keys
     * and allows distinct elements based on the key extracted using the provided keyExtractor function.
     *
     * @param <T> the type of input to the predicate
     * @param keyExtractor a function that extracts a key from an element
     * @return a predicate that returns {@code true} for elements with unique keys and {@code false} otherwise
     */
    public static <T> @NotNull Predicate<T> distinctByKey(@NotNull Function<? super T, ?> keyExtractor) {
        ConcurrentSet<Object> seen = Concurrent.newSet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Concatenates the contents of a variable number of arrays into a single unified stream.
     * <p>
     * Null arrays are filtered out.
     *
     * @param <T>    The type of elements in the arrays and the resulting stream.
     * @param arrays The arrays to be combined into a single stream. Can include NULL.
     * @return A stream containing all the elements from the provided arrays.
     */
    public static <T> @NotNull Stream<T> ofArrays(@Nullable T[]... arrays) {
        Stream<T> stream = Stream.empty();

        for (T[] array : arrays)
            stream = Stream.concat(stream, (array == null ? Stream.empty() : Arrays.stream(array)));

        return stream;
    }

    /**
     * Zips the specified stream with its indices.
     */
    public static <T> @NotNull TripleStream<T, Long, Long> zipWithIndex(@NotNull Stream<T> stream) {
        return zipWithIndex(stream.spliterator(), stream.isParallel());
    }

    /**
     * Zips the specified spliterator with its indices.
     */
    public static <T> @NotNull TripleStream<T, Long, Long> zipWithIndex(@NotNull Spliterator<T> spliterator, boolean parallel) {
        return TripleStream.of(mapWithIndex(spliterator, parallel, Triple::of));
    }

    public static <T> @NotNull Stream<Triple<T, Long, Long>> indexedStream(@NotNull Stream<T> stream) {
        return indexedStream(stream.spliterator(), stream.isParallel()).onClose(stream::close);
    }

    public static <T> @NotNull Stream<Triple<T, Long, Long>> indexedStream(@NotNull Spliterator<T> spliterator, boolean parallel) {
        return mapWithIndex(spliterator, parallel, Triple::of);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> @NotNull Stream<R> mapWithIndex(@NotNull Stream<T> stream, @NotNull TriFunction<? super T, Long, Long, ? extends R> function) {
        return (Stream<R>) mapWithIndex(stream.spliterator(), stream.isParallel(), function).onClose(stream::close);
    }

    /**
     * Returns a stream consisting of the results of applying the given function to the elements of
     * {@code stream} and their indices in the stream. For example,
     *
     * <pre>{@code
     * mapWithIndex(
     *     Stream.of("a", "b", "c"),
     *     (str, index, size) -> str + ":" + index + "/" + size)
     * }</pre>
     *
     * <p>would return {@code Stream.of("a:0/3", "b:1/3", "c:2/3")}.
     *
     * <p>The resulting stream is <a
     * href="http://gee.cs.oswego.edu/dl/html/StreamParallelGuidance.html">efficiently splittable</a>
     * if and only if {@code stream} was efficiently splittable and its underlying spliterator
     * reported {@link Spliterator#SUBSIZED}. This is generally the case if the underlying stream
     * comes from a data structure supporting efficient indexed random access, typically an array or
     * list.
     *
     * <p>The order of the resulting stream is defined if and only if the order of the original stream
     * was defined.
     */
    public static <T, R> @NotNull Stream<R> mapWithIndex(@NotNull Spliterator<T> spliterator, boolean parallel, @NotNull TriFunction<? super T, Long, Long, ? extends R> function) {
        long size = spliterator.estimateSize();

        if (!spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<>(size, spliterator.characteristics() & (Spliterator.ORDERED | Spliterator.SIZED)) {
                    private Iterator<T> fromIterator = Spliterators.iterator(spliterator);
                    private long index = 0;

                    @Override
                    public boolean tryAdvance(Consumer<? super R> action) {
                        if (this.fromIterator.hasNext()) {
                            action.accept(function.apply(this.fromIterator.next(), this.index++, size));
                            return true;
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
                Splitr createSplit(Spliterator<T> from, long i) {
                    return new Splitr(from, i);
                }

            }

            return StreamSupport.stream(new Splitr(spliterator, 0), parallel);
        }
    }

    public static <T> @NotNull Stream<T> modifyStream(@NotNull Stream<T> stream, @NotNull TriFunction<T, Long, Long, T> modFunction) {
        return mapWithIndex(stream, modFunction);
    }

    public static @NotNull Stream<String> appendEach(@NotNull Stream<String> stringStream, @NotNull String entryValue) {
        return appendEach(stringStream, entryValue, entryValue);
    }

    public static @NotNull Stream<String> appendEach(@NotNull Stream<String> stringStream, @NotNull String entryValue, @NotNull String lastEntry) {
        return modifyStream(stringStream, (value, index, size) -> value + (index < size - 1 ? entryValue : lastEntry));
    }

    public static @NotNull Stream<String> prependEach(@NotNull Stream<String> stringStream, @NotNull String entryValue) {
        return prependEach(stringStream, entryValue, entryValue);
    }

    public static @NotNull Stream<String> prependEach(@NotNull Stream<String> stringStream, @NotNull String entryValue, @NotNull String lastEntry) {
        return modifyStream(stringStream, (value, index, size) -> (index < size - 1 ? entryValue : lastEntry) + value);
    }

    public static <E> @NotNull Collector<E, ?, StringBuilder> toStringBuilder() {
        return toStringBuilder(true);
    }

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

    public static <E> @NotNull Collector<E, ?, StringBuilder> toStringBuilder(@NotNull String separator) {
        return Collector.of(
            StringBuilder::new,
            (builder, element) -> builder.append(element.toString()).append(separator),
            (left, right) -> left.append(right.toString())
        );
    }

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
