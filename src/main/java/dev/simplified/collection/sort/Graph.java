package dev.simplified.collection.sort;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.ConcurrentMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A directed graph supporting topological sorting of nodes.
 * <p>
 * Nodes and edges are added via the {@link Builder}, and the resulting graph
 * can be topologically sorted using {@link #topologicalSort()}.
 *
 * @param <T> the type of values stored in graph nodes
 */
@Getter
public class Graph<T> {

    private final @NotNull ConcurrentList<T> nodes;
    private final @NotNull ConcurrentMap<T, ConcurrentList<T>> nodeEdges;

    private Graph(@NotNull ConcurrentList<T> nodes,
                  @NotNull ConcurrentMap<T, ConcurrentList<T>> nodeEdges) {
        this.nodes = nodes;
        this.nodeEdges = nodeEdges;
    }

    /**
     * Creates a new graph builder. Supply an explicit type witness
     * (e.g. {@code Graph.<Foo>builder()}) when call-site inference cannot determine {@code T}.
     *
     * @param <T> the type of values stored in graph nodes
     * @return a new builder
     */
    public static <T> @NotNull Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Returns the graph's nodes in topological sort order.
     *
     * <p>Performs an iterative post-order DFS using explicit stacks so deeply chained graphs do
     * not blow the JVM call stack. Cycles in the graph trigger an {@link IllegalStateException}.</p>
     *
     * @return an unmodifiable concurrent list of node values in topological order
     */
    public @NotNull ConcurrentList<T> topologicalSort() {
        List<T> result = new ArrayList<>(this.nodes.size());
        Set<T> visited = new HashSet<>();
        Set<T> onStack = new HashSet<>();
        Deque<Iterator<T>> iterStack = new ArrayDeque<>();
        Deque<T> nodeStack = new ArrayDeque<>();

        for (T root : this.nodes) {
            if (visited.contains(root))
                continue;

            nodeStack.push(root);
            onStack.add(root);
            iterStack.push(this.neighborIterator(root));

            while (!nodeStack.isEmpty()) {
                Iterator<T> it = iterStack.getFirst();

                if (it.hasNext()) {
                    T next = it.next();

                    if (onStack.contains(next))
                        throw new IllegalStateException("Cycle detected at: " + next);
                    if (visited.contains(next))
                        continue;

                    nodeStack.push(next);
                    onStack.add(next);
                    iterStack.push(this.neighborIterator(next));
                } else {
                    T done = nodeStack.pop();
                    iterStack.pop();
                    onStack.remove(done);
                    visited.add(done);
                    result.add(done);
                }
            }
        }

        return Concurrent.newUnmodifiableList(result);
    }

    private @NotNull Iterator<T> neighborIterator(@NotNull T value) {
        ConcurrentList<T> neighbors = this.nodeEdges.get(value);
        return neighbors == null ? Collections.emptyIterator() : neighbors.iterator();
    }

    /**
     * A builder for constructing {@link Graph} instances with nodes and edges.
     *
     * @param <T> the type of values stored in graph nodes
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<T> {

        private final ConcurrentList<T> values = Concurrent.newList();
        private final ConcurrentMap<T, ConcurrentList<T>> nodeEdges = Concurrent.newMap();
        private Optional<Function<T, Stream<T>>> edgeFunction = Optional.empty();

        /**
         * Adds a directed edge from {@code left} to {@code right}.
         *
         * @param left the source node value
         * @param right the target node value
         * @return this builder
         */
        public Builder<T> withEdge(@NotNull T left, @NotNull T right) {
            if (!this.nodeEdges.containsKey(left))
                this.nodeEdges.put(left, Concurrent.newList());

            this.nodeEdges.get(left).add(right);
            return this;
        }

        /**
         * Sets a function that computes edges for each node value during {@link #build()}.
         *
         * @param function the edge function, or {@code null} to clear
         * @return this builder
         */
        public Builder<T> withEdgeFunction(@Nullable Function<T, Stream<T>> function) {
            return this.withEdgeFunction(Optional.ofNullable(function));
        }

        /**
         * Sets a function that computes edges for each node value during {@link #build()}.
         *
         * @param function an optional edge function
         * @return this builder
         */
        public Builder<T> withEdgeFunction(@NotNull Optional<Function<T, Stream<T>>> function) {
            this.edgeFunction = function;
            return this;
        }

        /**
         * Adds the given values as nodes in the graph.
         *
         * @param values the node values to add
         * @return this builder
         */
        public Builder<T> withValues(@NotNull T... values) {
            return this.withValues(Arrays.asList(values));
        }

        /**
         * Adds the given values as nodes in the graph.
         *
         * @param values the node values to add
         * @return this builder
         */
        public Builder<T> withValues(@NotNull Iterable<T> values) {
            values.forEach(this.values::add);
            return this;
        }

        /**
         * Builds the graph. If an edge function was provided, it is applied to each node value
         * to compute edges before constructing the immutable graph.
         *
         * @return the constructed graph
         */
        public @NotNull Graph<T> build() {
            // Handle Edge Function
            this.edgeFunction.ifPresent(edgeFunction -> this.values.forEach(value -> edgeFunction.apply(value)
                .forEach(edge -> this.withEdge(value, edge))
            ));

            return new Graph<>(
                this.values.toUnmodifiable(),
                this.nodeEdges.toUnmodifiable()
            );
        }

    }

}
