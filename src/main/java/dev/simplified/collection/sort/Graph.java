package dev.sbs.api.collection.sort;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
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

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Graph<T> {

    private final @NotNull ConcurrentList<Node<T>> nodes;
    private final @NotNull ConcurrentList<T> edges;
    private final @NotNull ConcurrentMap<T, ConcurrentList<T>> nodeEdges;

    public static <T> @NotNull Builder<T> builder(@NotNull T type) {
        return new Builder<>(type);
    }

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

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<T> implements dev.sbs.api.util.builder.Builder<Graph<T>> {

        private final T type;
        private final ConcurrentList<T> values = Concurrent.newList();
        private final ConcurrentList<T> edges = Concurrent.newList();
        private final ConcurrentMap<T, ConcurrentList<T>> nodeEdges = Concurrent.newMap();
        private Optional<Function<T, Stream<T>>> edgeFunction = Optional.empty();

        public Builder<T> withEdge(@NotNull T left, @NotNull T right) {
            if (!this.nodeEdges.containsKey(left))
                this.nodeEdges.put(left, Concurrent.newList());

            this.edges.add(right);
            this.nodeEdges.get(left).add(right);
            return this;
        }

        public Builder<T> withEdgeFunction(@Nullable Function<T, Stream<T>> function) {
            return this.withEdgeFunction(Optional.ofNullable(function));
        }

        public Builder<T> withEdgeFunction(@NotNull Optional<Function<T, Stream<T>>> function) {
            this.edgeFunction = function;
            return this;
        }

        public Builder<T> withValues(@NotNull T... values) {
            return this.withValues(Arrays.asList(values));
        }

        public Builder<T> withValues(@NotNull Iterable<T> values) {
            values.forEach(this.values::add);
            return this;
        }

        @Override
        public @NotNull Graph<T> build() {
            // Handle Edge Function
            this.edgeFunction.ifPresent(edgeFunction -> this.values.forEach(value -> edgeFunction.apply(value)
                .forEach(edge -> this.withEdge(value, edge))
            ));

            return new Graph<>(
                this.values.stream()
                    .map(Node::new)
                    .collect(Concurrent.toUnmodifiableList()),
                this.edges.toUnmodifiableList(),
                this.nodeEdges.toUnmodifiableMap()
            );
        }

    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static class Node<T> {

        private final @NotNull T value;
        @Setter(AccessLevel.PRIVATE)
        private boolean visited;

        public boolean notVisited() {
            return !this.isVisited();
        }

    }

}
