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
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
 * variant ({@link #layeredTopologicalSort()}) suitable for parallel scheduling, the same orderings
 * projected as {@link SortAlgorithm} strategies via {@link #asLinearSort()} and
 * {@link #asLayeredSort()}, and structural queries ({@link #predecessors}, {@link #successors},
 * {@link #roots}, {@link #leaves}, etc.).
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
     * Sorts the graph's nodes into a single linearized topological order via iterative DFS
     * post-order with explicit stacks - producing one valid sequence where every node follows
     * all of its dependencies.
     * <p>
     * Reach for this when sequential processing requires strict dependency-first ordering and
     * only one node can be acted on at a time (e.g. Hibernate entity registration over a
     * high-latency link). For graphs with multiple valid orderings (independent branches,
     * diamonds), the chosen order follows the DFS post-order seeded by {@link #getNodes()
     * registration order}. Cycles trigger an {@link IllegalStateException} naming the first
     * back-edge target encountered; for full-cycle reporting prefer
     * {@link #layeredTopologicalSort()}.
     *
     * <p><b>Time:</b> {@code O(N + E)} - each node visited once, each edge traversed once.
     * <p><b>Space:</b> {@code O(N)} for the visited and on-stack sets plus the explicit DFS
     * stacks (replacing the JVM call stack so deeply chained graphs don't overflow).
     *
     * @return an unmodifiable concurrent list of node values in topological order
     * @throws IllegalStateException if the graph contains a cycle
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
     * Sorts the graph's nodes into topological layers via Kahn's algorithm - layer 0 contains
     * nodes with no outgoing edges, layer {@code N} contains nodes whose dependencies all live
     * in layers {@code 0..N-1}.
     * <p>
     * Reach for this when scheduling work in parallel: every node within a single layer is
     * mutually independent and may be processed concurrently. Flattening the result yields a
     * valid topological order, though not necessarily the same one as
     * {@link #linearTopologicalSort()}. Cycles trigger an {@link IllegalStateException} naming
     * the full set of unprocessed nodes - more diagnostic than the linear variant's
     * first-cycle-element message.
     *
     * <p><b>Time:</b> {@code O(N + E)} via BFS over remaining-dependency counters.
     * <p><b>Space:</b> {@code O(N)} for the remaining-dependency counter map and the
     * per-layer queues.
     *
     * @return an unmodifiable concurrent list of layers, each itself an unmodifiable concurrent list
     * @throws IllegalStateException if the graph contains a cycle
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

    /**
     * Builds a {@link SortAlgorithm} that orders any list of {@code T} by this graph's linear
     * topological position - elements appear in dependency-first order, equivalent to indexing
     * each input element into the result of {@link #linearTopologicalSort()}.
     * <p>
     * Reach for this when reordering an arbitrary subset of the graph's nodes (or feeding graph
     * topology into {@code AtomicList.sorted(SortAlgorithm)}) without re-running the topological
     * sort per call. The returned algorithm caches the topological index map at construction
     * time and is reusable across many lists. Elements not registered in this graph trigger a
     * {@link NoSuchElementException} at sort time.
     *
     * <p><b>Time:</b> {@code O(N + E)} once at construction (delegates to
     * {@link #linearTopologicalSort()}); {@code O(m log m)} per sort invocation where {@code m}
     * is the input list size.
     * <p><b>Space:</b> {@code O(N)} for the cached {@code T -> position} index map.
     *
     * @return a {@link SortAlgorithm} that orders lists by linear topological position
     * @throws IllegalStateException if the graph contains a cycle
     */
    public @NotNull SortAlgorithm<T> asLinearSort() {
        ConcurrentList<T> ordered = this.linearTopologicalSort();
        Map<T, Integer> index = HashMap.newHashMap(ordered.size());

        for (int i = 0; i < ordered.size(); i++)
            index.put(ordered.get(i), i);

        return list -> list.sort(Comparator.comparingInt(t -> {
            Integer pos = index.get(t);
            if (pos == null) throw new NoSuchElementException("Element not in graph: " + t);
            return pos;
        }));
    }

    /**
     * Builds a {@link SortAlgorithm} that orders any list of {@code T} by this graph's
     * topological layer index - elements within the same layer keep their input order (Timsort
     * stability), elements in earlier layers come first.
     * <p>
     * Reach for this when the input list already has a meaningful order (priority, insertion
     * order, alphabetical) and you want to enforce graph-layer bucketing while preserving that
     * intra-layer order. Useful for hybrid pipelines where layer index gates parallelism but
     * within each layer some other ordering criterion still matters. Elements not registered in
     * this graph trigger a {@link NoSuchElementException} at sort time.
     *
     * <p><b>Time:</b> {@code O(N + E)} once at construction (delegates to
     * {@link #layeredTopologicalSort()}); {@code O(m log m)} per sort invocation where
     * {@code m} is the input list size.
     * <p><b>Space:</b> {@code O(N)} for the cached {@code T -> layer index} map.
     *
     * @return a {@link SortAlgorithm} that orders lists by topological layer index, stable within layers
     * @throws IllegalStateException if the graph contains a cycle
     */
    public @NotNull SortAlgorithm<T> asLayeredSort() {
        ConcurrentList<ConcurrentList<T>> layers = this.layeredTopologicalSort();
        Map<T, Integer> layerIndex = HashMap.newHashMap(this.nodes.size());

        for (int layer = 0; layer < layers.size(); layer++) {
            for (T node : layers.get(layer))
                layerIndex.put(node, layer);
        }

        return list -> list.sort(Comparator.comparingInt(t -> {
            Integer layer = layerIndex.get(t);
            if (layer == null) throw new NoSuchElementException("Element not in graph: " + t);
            return layer;
        }));
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
