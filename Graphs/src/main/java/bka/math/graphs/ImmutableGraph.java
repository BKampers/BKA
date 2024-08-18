/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.math.graphs;

import java.util.*;

/**
 * Immutable graph of given vertextype and edge type. Once constructed, edges and vertices cannot be added or removed anymore. Does not contain duplicate edges or vertices. That is,
 * this graph contains no pair of edges {e1,e2} where e1.equals(e2), and no pair of vertices {v1,v2} where v1.equals(v2). Edges may be directed or
 * undirected.
 * @see bka.math.graphs.GraphBase
 * @param <V> Vertex type
 * @param <E> Edge of Vertex type
 */
public class ImmutableGraph<V, E extends Edge<V>> implements GraphBase<V, E> {

    /**
     * Create an immutable copy of given graph
     *
     * @param graph
     */
    public ImmutableGraph(GraphBase<V, E> graph) {
        this(graph.getVertices(), graph.getEdges());
    }

    /**
     * Create an immutable graph of given edges.
     *
     * @param edges
     */
    public ImmutableGraph(Collection<E> edges) {
        this(Collections.emptySet(), edges);
    }

    /**
     * Create an immutable graph of given edges and vertices
     *
     * @param vertices
     * @param edges
     */
    public ImmutableGraph(Collection<V> vertices, Collection<E> edges) {
        MutableGraph<V, E> graph = new MutableGraph<>(vertices, edges);
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
