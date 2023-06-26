/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs.finders;

import bka.math.graphs.*;
import java.util.*;


public class GraphExplorer<V, E extends Edge<V>> {

    public GraphExplorer(TrailFinder<V, E> finder) {
        this.finder = Objects.requireNonNull(finder);
    }

    /**
     * Find all trails between two vertices in a graph. A trail is a sequence of edges joining a sequence of vertices. Vertices in a trail may be
     * repeated but edges may not be repeated.
     *
     * @param <V> Type of vertex
     * @param <E> Type of edge extending Edge&lt;V&gt;
     * @param graph
     * @param start
     * @param end
     * @return a Collection of all trails, empty if no trail can be found
     */
    public Collection<List<E>> findAllTrails(GraphBase<V, E> graph, V start, V end) {
        requireVertex(graph, start);
        if (end != null) {
            requireVertex(graph, end);
        }
        return finder.find(graph.getEdges(), start, end, true);
    }

    /**
     * Find all circuits in a graph that contain a given vertex. A circuit is a sequence of edges joining a sequence of vertices where the tail of the
     * last edge is the same vertex as the head of the first vertex. Vertices in a circuit may be repeated but edges may not be repeated.
     *
     * @param <V> Type of vertex
     * @param <E> Type of edge extending Edge&lt;V&gt;
     * @param graph
     * @param vertex
     * @return a collection of all circuits that contain the vertex, empty if no cirtcuit can be found.
     */
    public Collection<List<E>> findAllCircuits(GraphBase<V, E> graph, V vertex) {
        requireVertex(graph, vertex);
        return finder.find(graph.getEdges(), vertex, vertex, true);
    }

    /**
     * Find all paths between two vertices in a graph. A path is a sequence of edges joining a sequence of vertices. Vertices and edges in a trail may
     * not be repeated.
     *
     * @param <V> Type of vertex
     * @param <E> Type of edge extending Edge&lt;V&gt;
     * @param graph
     * @param start
     * @param end
     * @return a Collection of all trails, empty if no trail can be found
     */
    public Collection<List<E>> findAllPaths(GraphBase<V, E> graph, V start, V end) {
        if (start.equals(end)) {
            return findAllCycles(graph, start);
        }
        requireVertex(graph, start);
        if (end != null) {
            requireVertex(graph, end);
        }
        return finder.find(graph.getEdges(), start, end, false);
    }

    /**
     * Find all cycles in a graph that contain a given vertex. A cycle is a sequence of edges joining a sequence of vertices where the tail of the
     * last edge is the same vertex as the head of the first vertex. Vertices and edges in a cycle may not be repeated.
     *
     * @param <V> Type of vertex
     * @param <E> Type of edge extending Edge&lt;V&gt;
     * @param graph
     * @param vertex
     * @return a collection of all cycles that contain the vertex, empty if no cycle can be found.
     */
    public Collection<List<E>> findAllCycles(GraphBase<V, E> graph, V vertex) {
        requireVertex(graph, vertex);
        return finder.find(graph.getEdges(), vertex, vertex, false);
    }

    private static <V, E extends Edge<V>> void requireVertex(GraphBase<V, E> graph, V vertex) {
        if (! graph.getVertices().contains(vertex)) {
            throw new NoSuchElementException(vertex.toString());
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '<' + finder.getClass().getName() + '>';
    }

    private final TrailFinder<V, E> finder;

}
