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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A directed graph supporting topological sorting and structural queries.
 * <p>
 * Nodes and edges are added via the {@link Builder}; nodes referenced only via {@code withEdge}
 * or the edge function are auto-registered at {@link Builder#build() build} time. The resulting
 * graph exposes a flat {@link #linearTopologicalSort() linear topological sort}, a layered
 * variant ({@link #layeredTopologicalSort()}) suitable for parallel scheduling, and structural
 * queries ({@link #predecessors}, {@link #successors}, {@link #roots}, {@link #leaves}, etc.).
 *
 * <p>An edge {@code A -> B} is interpreted as "{@code A} depends on {@code B}" - so {@code B}
 * appears earlier in the topological order than {@code A}.
 *
 * @param <T> the type of values stored in graph nodes
 */
@Getter
public class Graph<T> {

    private final @NotNull ConcurrentList<T> nodes;
    private final @NotNull ConcurrentMap<T, ConcurrentList<T>> nodeEdges;
    @Getter(AccessLevel.NONE)
    private final @NotNull ConcurrentMap<T, ConcurrentList<T>> reverseAdjacency;

    private Graph(@NotNull ConcurrentList<T> nodes, @NotNull ConcurrentMap<T, ConcurrentList<T>> nodeEdges) {
        this.nodes = nodes;
        this.nodeEdges = nodeEdges;

        ConcurrentMap<T, ConcurrentList<T>> reverse = Concurrent.newMap();
        nodeEdges.forEach((source, targets) -> targets.forEach(target ->
            reverse.computeIfAbsent(target, k -> Concurrent.newList()).add(source)
        ));
        reverse.replaceAll((target, sources) -> sources.toUnmodifiable());
        this.reverseAdjacency = reverse.toUnmodifiable();
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
     * Returns whether the graph contains the given node.
     *
     * @param node the node to test
     * @return {@code true} if {@code node} is a registered node in this graph
     */
    public boolean contains(@NotNull T node) {
        return this.nodes.contains(node);
    }

    /**
     * Returns whether a directed edge {@code from -> to} exists in the graph.
     *
     * @param from the source node
     * @param to the target node
     * @return {@code true} if an edge from {@code from} to {@code to} is present
     */
    public boolean hasEdge(@NotNull T from, @NotNull T to) {
        ConcurrentList<T> targets = this.nodeEdges.get(from);
        return targets != null && targets.contains(to);
    }

    /**
     * Returns the nodes reachable via outgoing edges from {@code node} (its dependencies).
     *
     * @param node the source node
     * @return an unmodifiable list of successors, or an empty list if none
     */
    public @NotNull ConcurrentList<T> successors(@NotNull T node) {
        ConcurrentList<T> targets = this.nodeEdges.get(node);
        return targets == null ? Concurrent.newUnmodifiableList() : targets;
    }

    /**
     * Returns the nodes that have an outgoing edge to {@code node} (its dependents).
     *
     * @param node the target node
     * @return an unmodifiable list of predecessors, or an empty list if none
     */
    public @NotNull ConcurrentList<T> predecessors(@NotNull T node) {
        ConcurrentList<T> sources = this.reverseAdjacency.get(node);
        return sources == null ? Concurrent.newUnmodifiableList() : sources;
    }

    /**
     * Returns the number of outgoing edges from {@code node}.
     *
     * @param node the node to inspect
     * @return the out-degree of {@code node}
     */
    public int outDegree(@NotNull T node) {
        ConcurrentList<T> targets = this.nodeEdges.get(node);
        return targets == null ? 0 : targets.size();
    }

    /**
     * Returns the number of incoming edges to {@code node}.
     *
     * @param node the node to inspect
     * @return the in-degree of {@code node}
     */
    public int inDegree(@NotNull T node) {
        ConcurrentList<T> sources = this.reverseAdjacency.get(node);
        return sources == null ? 0 : sources.size();
    }

    /**
     * Returns the nodes with no incoming edges - the entry points of the dependency graph.
     *
     * @return an unmodifiable list of root nodes, in registration order
     */
    public @NotNull ConcurrentList<T> roots() {
        return this.nodes.stream()
            .filter(node -> this.inDegree(node) == 0)
            .collect(Concurrent.toUnmodifiableList());
    }

    /**
     * Returns the nodes with no outgoing edges - the leaves of the dependency graph.
     *
     * @return an unmodifiable list of leaf nodes, in registration order
     */
    public @NotNull ConcurrentList<T> leaves() {
        return this.nodes.stream()
            .filter(node -> this.outDegree(node) == 0)
            .collect(Concurrent.toUnmodifiableList());
    }

    /**
     * Returns a new graph with the same node set and every edge direction flipped.
     *
     * @return a fresh {@code Graph} whose edges run opposite to this one's
     */
    public @NotNull Graph<T> reverse() {
        Builder<T> reversed = Graph.<T>builder().withValues(this.nodes);
        this.nodeEdges.forEach((from, tos) -> tos.forEach(to -> reversed.withEdge(to, from)));
        return reversed.build();
    }

    /**
     * Returns the graph's nodes in a single linearized topological order, suitable for sequential
     * processing where each node must be handled strictly after all of its dependencies.
     *
     * <p>Each node appears exactly once and is preceded by every node it depends on. The output
     * is one valid topological ordering; for graphs with multiple valid orderings (independent
     * branches, diamonds), the chosen order follows the iterative DFS post-order traversal seeded
     * by {@link #getNodes() registration order}.
     *
     * <p>Implemented as an iterative post-order DFS using explicit stacks in O(N + E) so deeply
     * chained graphs do not blow the JVM call stack. Cycles trigger an
     * {@link IllegalStateException} naming the first back-edge target encountered. For parallel
     * scheduling or full-cycle reporting, prefer {@link #layeredTopologicalSort()}.
     *
     * @return an unmodifiable concurrent list of node values in topological order
     */
    public @NotNull ConcurrentList<T> linearTopologicalSort() {
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

    /**
     * Returns the graph's nodes grouped by topological layer, suitable for parallel scheduling.
     *
     * <p>Layer 0 contains nodes with no outgoing edges; layer {@code N} contains nodes whose
     * dependencies all lie in layers {@code 0..N-1}. All nodes within a single layer are
     * mutually independent and may be processed concurrently. Flattening the result yields a
     * valid topological order (though not necessarily the same one as
     * {@link #linearTopologicalSort()}).
     *
     * <p>Implemented via Kahn's algorithm in O(N + E) using BFS over remaining-dependency
     * counters. Cycles trigger an {@link IllegalStateException} naming the unprocessed nodes.
     *
     * @return an unmodifiable concurrent list of layers, each itself an unmodifiable concurrent list
     */
    public @NotNull ConcurrentList<ConcurrentList<T>> layeredTopologicalSort() {
        Map<T, Integer> remaining = HashMap.newHashMap(this.nodes.size());
        for (T node : this.nodes) remaining.put(node, this.outDegree(node));

        List<ConcurrentList<T>> layers = new ArrayList<>();
        List<T> current = new ArrayList<>();
        for (T node : this.nodes) {
            if (remaining.get(node) == 0) current.add(node);
        }

        int processed = 0;
        while (!current.isEmpty()) {
            layers.add(Concurrent.newUnmodifiableList(current));
            processed += current.size();

            List<T> next = new ArrayList<>();
            for (T node : current) {
                for (T predecessor : this.predecessors(node)) {
                    if (remaining.merge(predecessor, -1, Integer::sum) == 0)
                        next.add(predecessor);
                }
            }
            current = next;
        }

        if (processed != this.nodes.size()) {
            Set<T> stuck = new HashSet<>(this.nodes);
            for (ConcurrentList<T> layer : layers) layer.forEach(stuck::remove);
            throw new IllegalStateException("Cycle detected involving: " + stuck);
        }

        return Concurrent.newUnmodifiableList(layers);
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
         * to compute edges. Any node referenced by an edge but not previously registered via
         * {@link #withValues} is auto-registered (in edge-iteration order) before the immutable
         * graph is constructed.
         *
         * @return the constructed graph
         */
        public @NotNull Graph<T> build() {
            this.edgeFunction.ifPresent(edgeFunction -> this.values.forEach(value -> edgeFunction.apply(value)
                .forEach(edge -> this.withEdge(value, edge))
            ));

            Set<T> seen = new HashSet<>(this.values);
            this.nodeEdges.forEach((source, targets) -> {
                if (seen.add(source)) this.values.add(source);
                targets.forEach(target -> {
                    if (seen.add(target)) this.values.add(target);
                });
            });

            return new Graph<>(
                this.values.toUnmodifiable(),
                this.nodeEdges.toUnmodifiable()
            );
        }

    }

}
