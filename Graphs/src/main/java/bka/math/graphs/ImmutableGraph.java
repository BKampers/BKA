/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;

import java.util.*;


public class ImmutableGraph<V, E extends Edge<V>> implements GraphBase<V, E> {

    public ImmutableGraph(GraphBase<V, E> graph) {
        this(graph.getVertices(), graph.getEdges());
    }

    public ImmutableGraph(Collection<E> edges) {
        this(Collections.emptySet(), edges);
    }

    public ImmutableGraph(Collection<V> vertices, Collection<E> edges) {
        GraphBase<V, E> graph = new MutableGraph<>(vertices, edges);
        this.vertices = graph.getVertices();
        this.edges = graph.getEdges();
    }

    @Override
    final public Set<V> getVertices() {
        return vertices;
    }

    @Override
    final public Set<E> getEdges() {
        return edges;
    }

    private final Set<V> vertices;
    private final Set<E> edges;

}
