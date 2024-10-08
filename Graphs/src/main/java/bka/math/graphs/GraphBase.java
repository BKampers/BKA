/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.math.graphs;

import java.util.*;

/**
 * Root interface for graphs of given vertex type and edge type.
 * @see <a href="https://en.wikipedia.org/wiki/Graph_theory">Grahp theory on Wikipedia</a>
 * @param <V> Vertex type
 * @param <E> {@code Edge} of vertex type
 */
public interface GraphBase<V, E extends Edge<V>> {

    /**
     * @return all vertices in this graph. That is, at all vertices that are connected by the graph's edges and all isolated (unconnected) vertices.
     */
    Set<V> getVertices();

    /**
     * @return all edges in this graph.
     */
    Set<E> getEdges();

}
