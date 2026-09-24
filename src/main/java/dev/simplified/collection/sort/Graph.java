package dev.simplified.collection.sort;

import dev.simplified.annotations.AccessLevel;
import dev.simplified.annotations.Getter;
import dev.simplified.annotations.NoArgsConstructor;
import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.ConcurrentSet;
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
import java.util.LinkedHashSet;
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
 * their transitive {@link #ancestors} and {@link #descendants}, {@link #roots}, {@link #leaves},
 * etc.). The sorts refuse a cycle; the structural queries answer over one.
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

    /**
     * Constructs a graph over the given nodes and edges, copying the node list and every edge list
     * into unmodifiable snapshots, so neither direction of an edge can change once the graph exists.
     * <p>
     * The working structures are plain JDK collections; only what the graph publishes is a
     * concurrent type, and an unmodifiable one reads without taking a lock.
     *
     * @param nodes the node values, in registration order
     * @param nodeEdges the outgoing edges, keyed by the node they leave
     */
    private Graph(@NotNull List<T> nodes, @NotNull Map<T, List<T>> nodeEdges) {
        Map<T, ConcurrentList<T>> forward = HashMap.newHashMap(nodeEdges.size());
        Map<T, List<T>> reverse = new HashMap<>();

        nodeEdges.forEach((source, targets) -> {
            forward.put(source, Concurrent.newUnmodifiableList(targets));
            targets.forEach(target -> reverse.computeIfAbsent(target, k -> new ArrayList<>()).add(source));
        });

        Map<T, ConcurrentList<T>> backward = HashMap.newHashMap(reverse.size());
        reverse.forEach((target, sources) -> backward.put(target, Concurrent.newUnmodifiableList(sources)));

        this.nodes = Concurrent.newUnmodifiableList(nodes);
        this.nodeEdges = Concurrent.newUnmodifiableMap(forward);
        this.reverseAdjacency = Concurrent.newUnmodifiableMap(backward);
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
     * <p><b>Time:</b> {@code O(N)} - a scan of the node list.
     * <p><b>Space:</b> {@code O(1)} - nothing is allocated.
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
     * <p><b>Time:</b> {@code O(d)} - one lookup, then a scan of the {@code d} edges leaving
     * {@code from}.
     * <p><b>Space:</b> {@code O(1)} - nothing is allocated.
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
     * <p><b>Time:</b> {@code O(1)} - one lookup in the adjacency held since construction.
     * <p><b>Space:</b> {@code O(1)} - the held list is returned, not copied.
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
     * <p><b>Time:</b> {@code O(1)} - one lookup in the reverse adjacency built at construction.
     * <p><b>Space:</b> {@code O(1)} - the held list is returned, not copied.
     *
     * @param node the target node
     * @return an unmodifiable list of predecessors, or an empty list if none
     */
    public @NotNull ConcurrentList<T> predecessors(@NotNull T node) {
        ConcurrentList<T> sources = this.reverseAdjacency.get(node);
        return sources == null ? Concurrent.newUnmodifiableList() : sources;
    }

    /**
     * Returns every node {@code node} reaches along one or more outgoing edges - its dependencies,
     * direct and transitive.
     * <p>
     * Safe on any graph, cyclic or not: each node is visited once however many paths reach it, and
     * {@code node} itself is in the result exactly when it lies on a cycle, a self-edge included. A
     * node not in the graph reaches nothing.
     *
     * <p><b>Time:</b> {@code O(N + E)} over the reachable subgraph.
     * <p><b>Space:</b> {@code O(N)} for the visited set and the work queue.
     *
     * @param node the node to walk from
     * @return an unmodifiable set of the nodes reached, in breadth-first order
     */
    public @NotNull ConcurrentSet<T> descendants(@NotNull T node) {
        return reachable(node, this.nodeEdges);
    }

    /**
     * Returns every node that reaches {@code node} along one or more edges - its dependents, direct
     * and transitive.
     * <p>
     * The mirror of {@link #descendants}, walked over incoming edges, with the same cycle guarantees.
     *
     * <p><b>Time:</b> {@code O(N + E)} over the reaching subgraph.
     * <p><b>Space:</b> {@code O(N)} for the visited set and the work queue.
     *
     * @param node the node to walk back from
     * @return an unmodifiable set of the nodes reaching {@code node}, in breadth-first order
     */
    public @NotNull ConcurrentSet<T> ancestors(@NotNull T node) {
        return reachable(node, this.reverseAdjacency);
    }

    /**
     * Returns the number of outgoing edges from {@code node}.
     *
     * <p><b>Time:</b> {@code O(1)} - one lookup in the adjacency held since construction.
     * <p><b>Space:</b> {@code O(1)} - nothing is allocated.
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
     * <p><b>Time:</b> {@code O(1)} - one lookup in the reverse adjacency built at construction.
     * <p><b>Space:</b> {@code O(1)} - nothing is allocated.
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
     * <p><b>Time:</b> {@code O(N)} - one in-degree lookup per node.
     * <p><b>Space:</b> {@code O(N)} for the returned list.
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
     * <p><b>Time:</b> {@code O(N)} - one out-degree lookup per node.
     * <p><b>Space:</b> {@code O(N)} for the returned list.
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
     * <p><b>Time:</b> {@code O(N + E)} - every edge is added to a fresh builder once, then sealed.
     * <p><b>Space:</b> {@code O(N + E)} for the new graph's node list and both adjacencies.
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
     * Walks breadth-first from a node's neighbours, expanding each node once.
     * <p>
     * The start is not marked visited before the walk, so it joins the result only when an edge
     * leads back to it.
     *
     * @param start the node to walk from
     * @param adjacency the edges to follow, keyed by the node they leave
     * @param <T> the type of values stored in graph nodes
     * @return an unmodifiable set of the nodes reached, in breadth-first order
     */
    private static <T> @NotNull ConcurrentSet<T> reachable(
        @NotNull T start,
        @NotNull ConcurrentMap<T, ConcurrentList<T>> adjacency
    ) {
        Set<T> seen = new LinkedHashSet<>();
        Deque<T> pending = new ArrayDeque<>();
        ConcurrentList<T> first = adjacency.get(start);
        if (first != null) pending.addAll(first);

        while (!pending.isEmpty()) {
            T next = pending.poll();

            if (!seen.add(next))
                continue;

            ConcurrentList<T> neighbors = adjacency.get(next);
            if (neighbors != null) pending.addAll(neighbors);
        }

        return Concurrent.newUnmodifiableLinkedSet(seen);
    }

    /**
     * A builder for constructing {@link Graph} instances with nodes and edges.
     * <p>
     * A builder holds plain JDK collections and belongs to the thread building with it; the graph
     * it builds is immutable and safe to share.
     *
     * @param <T> the type of values stored in graph nodes
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<T> {

        private final @NotNull List<T> values = new ArrayList<>();
        private final @NotNull Map<T, List<T>> nodeEdges = new HashMap<>();
        private @NotNull Optional<Function<T, Stream<T>>> edgeFunction = Optional.empty();

        /**
         * Adds a directed edge from {@code left} to {@code right}.
         *
         * @param left the source node value
         * @param right the target node value
         * @return this builder
         */
        public @NotNull Builder<T> withEdge(@NotNull T left, @NotNull T right) {
            this.nodeEdges.computeIfAbsent(left, key -> new ArrayList<>()).add(right);
            return this;
        }

        /**
         * Sets a function that computes edges for each node value during {@link #build()}.
         *
         * @param function the edge function, or {@code null} to clear
         * @return this builder
         */
        public @NotNull Builder<T> withEdgeFunction(@Nullable Function<T, Stream<T>> function) {
            return this.withEdgeFunction(Optional.ofNullable(function));
        }

        /**
         * Sets a function that computes edges for each node value during {@link #build()}.
         *
         * @param function an optional edge function
         * @return this builder
         */
        public @NotNull Builder<T> withEdgeFunction(@NotNull Optional<Function<T, Stream<T>>> function) {
            this.edgeFunction = function;
            return this;
        }

        /**
         * Adds the given values as nodes in the graph.
         *
         * @param values the node values to add
         * @return this builder
         */
        public @NotNull Builder<T> withValues(@NotNull T... values) {
            return this.withValues(Arrays.asList(values));
        }

        /**
         * Adds the given values as nodes in the graph.
         *
         * @param values the node values to add
         * @return this builder
         */
        public @NotNull Builder<T> withValues(@NotNull Iterable<T> values) {
            values.forEach(this.values::add);
            return this;
        }

        /**
         * Builds the graph. If an edge function was provided, it is applied to each value
         * registered via {@link #withValues} to compute edges. Any node referenced by an edge but
         * not registered that way joins the graph (in edge-iteration order) before the immutable
         * graph is constructed.
         * <p>
         * Building reads this builder without changing it, so building again yields the same nodes
         * and edges, and a builder used again leaves every graph it built untouched.
         *
         * <p><b>Time:</b> {@code O(N + E)} plus the edge function's own cost - one call of it per
         * registered value, one pass registering the nodes the edges name, and one sealing every
         * edge list.
         * <p><b>Space:</b> {@code O(N + E)} for the working copies and the graph's node list and
         * both adjacencies.
         *
         * @return the constructed graph
         */
        public @NotNull Graph<T> build() {
            List<T> nodes = new ArrayList<>(this.values);
            Map<T, List<T>> edges = HashMap.newHashMap(this.nodeEdges.size());
            this.nodeEdges.forEach((source, targets) -> edges.put(source, new ArrayList<>(targets)));

            this.edgeFunction.ifPresent(edgeFunction -> this.values.forEach(value -> edgeFunction.apply(value)
                .forEach(edge -> edges.computeIfAbsent(value, key -> new ArrayList<>()).add(edge))
            ));

            Set<T> seen = new HashSet<>(nodes);
            edges.forEach((source, targets) -> {
                if (seen.add(source)) nodes.add(source);
                targets.forEach(target -> {
                    if (seen.add(target)) nodes.add(target);
                });
            });

            return new Graph<>(nodes, edges);
        }

    }

}
