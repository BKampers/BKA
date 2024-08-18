/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs.utils;

import bka.math.graphs.*;
import java.util.function.*;


public final class EdgeUtil {

    private EdgeUtil() throws IllegalAccessException {
    }

    /**
     * @param <V> Vertex type
     * @param <E> Edge of vertex type
     * @param edge
     * @param vertex incident on given edge
     * @return The vertex that is connected to given vertex by given edge.
     * @throws IllegalArgumentException if given vertex is not incident on (not connected by) given edge.
     */
    public static <V, E extends Edge<V>> V getAdjacentVertex(E edge, V vertex) {
        if (!isIncidentWith(edge, vertex)) {
            throw new IllegalArgumentException("Edge " + edge + " is not incident on vertex " + vertex);
        }
        return edge.getVertices().stream().filter(v -> !vertex.equals(v)).findAny().orElse(vertex);
    }

    /**
     * @param <V> Vertex type
     * @param <E> Edge of vertex type
     * @param vertex
     * @return Predicate to distinguish edges incident on (connecting) given vertex
     */
    public static <V, E extends Edge<V>> Predicate<E> isIncidentWith(V vertex) {
        return edge -> isIncidentWith(edge, vertex);
    }

    /**
     * @param <V> Vertex type
     * @param <E> Edge of vertex type
     * @param edge
     * @param vertex
     * @return true if given vertex is incident on (is connected by) edge, false otherwise
     */
    public static <V, E extends Edge<V>> boolean isIncidentWith(E edge, V vertex) {
        return edge.getVertices().contains(vertex);
    }

    /**
     * @param <V> Vertex type
     * @param <E> Edge of vertex type
     * @param edge
     * @return true if given edge connects a vertex to itself, false otherwise
     */
    public static <V, E extends Edge<V>> boolean isLoop(E edge) {
        return edge.getVertices().stream().distinct().count() == 1;
    }

    /**
     * @param <V> Vertex type
     * @param <E> DirectedEdge of vertex type
     * @param origin
     * @return Predicate to distinguish egdes that have given origin
     */
    public static <V, E extends DirectedEdge<V>> Predicate<E> from(V origin) {
        return edge -> origin.equals(edge.getOrigin());
    }

    /**
     * @param <V> Vertex type
     * @param <E> DirectedEdge of vertex type
     * @param terminus
     * @return Predicate to distinguish egdes that have given terminus
     */
    public static <V, E extends DirectedEdge<V>> Predicate<E> to(V terminus) {
        return edge -> terminus.equals(edge.getTerminus());
    }

}
