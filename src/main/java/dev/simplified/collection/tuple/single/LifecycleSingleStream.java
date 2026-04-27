package dev.simplified.collection.tuple.single;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.linked.ConcurrentLinkedList;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A lifecycle-aware {@link SingleStream} that owns an external resource (typically an open
 * Hibernate {@code Session}) and closes it after the first terminal operation completes.
 *
 * <p>Every terminal operation listed in the Phase 2d locked scope is overridden here to wrap
 * its body in {@code try { ... } finally { this.close(); }}, so callers do not need a
 * try-with-resources block. The {@link #iterator()} and {@link #spliterator()} methods are
 * intentionally NOT wrapped - they hand a cursor to the caller, and auto-closing in
 * {@code finally} would close the cursor before the caller iterates. Callers using those two
 * methods are responsible for an explicit {@link #close()} call or a try-with-resources block.</p>
 *
 * <p>The internal {@code closed} flag guards against multiple {@code close()} invocations.
 * Hibernate's {@code Session.close()} is not idempotent and throws {@code IllegalStateException}
 * on a double-close, so this guard is required. The underlying JDK stream's pre-registered close
 * handlers (the Hibernate cursor close registered by {@code AbstractSelectionQuery.stream()}) and
 * any user-registered handlers run on the first {@link #close()} call only.</p>
 *
 * <p>Streams are not safe for concurrent use across threads; this constraint is inherited
 * unchanged from {@link java.util.stream.BaseStream}.</p>
 *
 * @param <E> the type of elements in the stream
 * @see SingleStream
 */
public final class LifecycleSingleStream<E> implements SingleStream<E> {

    /** The underlying JDK stream produced by Hibernate's {@code getResultStream()}. */
    private final @NotNull Stream<E> underlying;

    /** Guard flag preventing the lifecycle resource from being closed twice. */
    private boolean closed = false;

    /**
     * Constructs a new {@code LifecycleSingleStream} that wraps the given JDK stream and owns
     * the given lifecycle resource. The resource is closed in {@link #close()} after the
     * underlying stream's own close handlers have run.
     *
     * @param underlying the JDK stream to wrap, typically the result of {@code Query.getResultStream()}
     * @param lifecycleResource the resource to close after the first terminal operation
     */
    public LifecycleSingleStream(@NotNull Stream<E> underlying, @NotNull AutoCloseable lifecycleResource) {
        // Register the lifecycle resource close as a JDK onClose handler so derived streams
        // produced by intermediate operations (filter, map, peek, sorted, distinct, limit, skip,
        // flatMap, flatMapMany) inherit it via JDK handler-chaining semantics. Closing any derived
        // stream walks the handler chain back to this registration and fires it. The closed flag
        // makes the callback idempotent so Hibernate Session.close() (which is NOT idempotent and
        // throws IllegalStateException on a second call) is only invoked once.
        this.underlying = underlying.onClose(() -> {
            if (this.closed) return;
            this.closed = true;
            try {
                lifecycleResource.close();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    /**
     * Creates a new {@code LifecycleSingleStream} from a JDK stream and an {@link AutoCloseable}
     * lifecycle resource. Equivalent to {@code new LifecycleSingleStream<>(stream, resource)}.
     *
     * @param <E> the element type
     * @param stream the JDK stream to wrap
     * @param resource the lifecycle resource closed after the first terminal
     * @return a new lifecycle-aware single stream
     */
    public static <E> @NotNull LifecycleSingleStream<E> of(@NotNull Stream<E> stream, @NotNull AutoCloseable resource) {
        return new LifecycleSingleStream<>(stream, resource);
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Stream<E> underlying() {
        return this.underlying;
    }

    /**
     * Closes the underlying JDK stream, which fires all registered onClose handlers in
     * registration order: first the Hibernate cursor close registered by
     * {@code AbstractSelectionQuery.stream()}, then the lifecycle resource close
     * registered by this class's constructor. If any handler throws, the first exception
     * is relayed to the caller with subsequent exceptions attached via
     * {@link Throwable#addSuppressed}. The constructor-registered handler is guarded by
     * {@link #closed} so subsequent {@code close()} invocations are safe no-ops.
     */
    @Override
    public void close() {
        this.underlying.close();
    }

    // ----- Auto-closing terminal operations -----

    /** {@inheritDoc} */
    @Override
    public @NotNull Optional<E> findFirst() {
        try {
            return this.underlying.findFirst();
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Optional<E> findAny() {
        try {
            return this.underlying.findAny();
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void forEach(@NotNull Consumer<? super E> action) {
        try {
            this.underlying.forEach(action);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void forEachOrdered(@NotNull Consumer<? super E> action) {
        try {
            this.underlying.forEachOrdered(action);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public long count() {
        try {
            return this.underlying.count();
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean anyMatch(@NotNull Predicate<? super E> predicate) {
        try {
            return this.underlying.anyMatch(predicate);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean allMatch(@NotNull Predicate<? super E> predicate) {
        try {
            return this.underlying.allMatch(predicate);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean noneMatch(@NotNull Predicate<? super E> predicate) {
        try {
            return this.underlying.noneMatch(predicate);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Optional<E> min(@NotNull Comparator<? super E> comparator) {
        try {
            return this.underlying.min(comparator);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Optional<E> max(@NotNull Comparator<? super E> comparator) {
        try {
            return this.underlying.max(comparator);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull E reduce(@NotNull E identity, @NotNull BinaryOperator<E> accumulator) {
        try {
            return this.underlying.reduce(identity, accumulator);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Optional<E> reduce(@NotNull BinaryOperator<E> accumulator) {
        try {
            return this.underlying.reduce(accumulator);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public <U> @NotNull U reduce(@NotNull U identity, @NotNull BiFunction<U, ? super E, U> accumulator, @NotNull BinaryOperator<U> combiner) {
        try {
            return this.underlying.reduce(identity, accumulator, combiner);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public <R, A> R collect(@NotNull Collector<? super E, A, R> collector) {
        try {
            return this.underlying.collect(collector);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public <R> R collect(@NotNull Supplier<R> supplier, @NotNull BiConsumer<R, ? super E> accumulator, @NotNull BiConsumer<R, R> combiner) {
        try {
            return this.underlying.collect(supplier, accumulator, combiner);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Object @NotNull [] toArray() {
        try {
            return this.underlying.toArray();
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public <A> @NotNull A @NotNull [] toArray(@NotNull IntFunction<A[]> generator) {
        try {
            return this.underlying.toArray(generator);
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull ConcurrentList<E> toList() {
        try {
            return this.underlying.collect(Concurrent.toList());
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull ConcurrentLinkedList<E> toLinkedList() {
        try {
            return this.underlying.collect(Concurrent.toLinkedList());
        } finally {
            this.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull ConcurrentList<E> toUnmodifiableList() {
        try {
            return this.underlying.collect(Concurrent.toUnmodifiableList());
        } finally {
            this.close();
        }
    }

    // ----- Pass-through (NOT auto-closed) -----

    /**
     * Returns the iterator of the underlying stream WITHOUT auto-closing the lifecycle
     * resource. Callers who use this method are responsible for calling {@link #close()}
     * explicitly or wrapping this stream in a try-with-resources block.
     */
    @Override
    public @NotNull Iterator<E> iterator() {
        return this.underlying.iterator();
    }

    /**
     * Returns the spliterator of the underlying stream WITHOUT auto-closing the lifecycle
     * resource. Callers who use this method are responsible for calling {@link #close()}
     * explicitly or wrapping this stream in a try-with-resources block.
     */
    @Override
    public @NotNull Spliterator<E> spliterator() {
        return this.underlying.spliterator();
    }

}
