package dev.simplified.collection.sort;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.ConcurrentMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Stack;
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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Graph<T> {

    private final @NotNull ConcurrentList<Node<T>> nodes;
    private final @NotNull ConcurrentList<T> edges;
    private final @NotNull ConcurrentMap<T, ConcurrentList<T>> nodeEdges;

    /**
     * Creates a new graph builder parameterized by the given type instance.
     *
     * @param type a representative instance used for type inference
     * @param <T> the type of values stored in graph nodes
     * @return a new builder
     */
    public static <T> @NotNull Builder<T> builder(@NotNull T type) {
        return new Builder<>(type);
    }

    /**
     * Finds the node with the given value.
     *
     * @param value the value to search for
     * @return the matching node
     */
    private @NotNull Node<T> findNode(@NotNull T value) {
        return this.getNodes().findFirstOrNull(Node::getValue, value);
    }

    /**
     * Sort the nodes topologically.
     *
     * @return Sorted nodes.
     */
    public @NotNull ConcurrentList<T> topologicalSort() {
        Stack<T> stack = new Stack<>();

        // iterate through all the nodes and their neighbours if not already visited.
        for (Node<T> node : this.getNodes()) {
            if (node.notVisited())
                this.sort(node, stack);
        }

        return Concurrent.newUnmodifiableList(stack);
    }

    /**
     * Recursively iterates through all nodes and their neighbours and
     * pushes the visited items to the stack.
     *
     * @param node The current node.
     * @param stack The combined stack.
     */
    private void sort(@NotNull Node<T> node, @NotNull Stack<T> stack){
        node.setVisited(true);

        // The leaf nodes have no neighbours
        if (this.getNodeEdges().containsKey(node.getValue())) {
            // Get all neighbour nodes
            Iterator<T> iterator = this.getNodeEdges().get(node.getValue()).iterator();
            Node<T> neighborNode;

            while (iterator.hasNext()) {
                neighborNode = this.findNode(iterator.next());

                if (neighborNode.notVisited())
                    this.sort(neighborNode, stack); // Visit neighbour node
            }
        }

        // Push the latest node to the stack
        stack.push(node.getValue());
    }

    /**
     * A builder for constructing {@link Graph} instances with nodes and edges.
     *
     * @param <T> the type of values stored in graph nodes
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<T> {

        private final T type;
        private final ConcurrentList<T> values = Concurrent.newList();
        private final ConcurrentList<T> edges = Concurrent.newList();
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

            this.edges.add(right);
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
                this.values.stream()
                    .map(Node::new)
                    .collect(Concurrent.toUnmodifiableList()),
                this.edges.toUnmodifiable(),
                this.nodeEdges.toUnmodifiable()
            );
        }

    }

    /**
     * A node in the graph holding a value and a visited flag for traversal.
     *
     * @param <T> the type of the node value
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static class Node<T> {

        private final @NotNull T value;
        @Setter(AccessLevel.PRIVATE)
        private boolean visited;

        /**
         * Returns {@code true} if this node has not yet been visited during traversal.
         *
         * @return {@code true} if not visited
         */
        public boolean notVisited() {
            return !this.isVisited();
        }

    }

}
