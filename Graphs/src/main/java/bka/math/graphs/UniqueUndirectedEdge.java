/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.math.graphs;

import java.util.*;


/**
 * Default implementation of an undirected edge for given vertex type.
 * To be used in graphs where multiple edges are not allowed. That is two or more edges that join the same vertices, are not allowed.
 * @see bka.math.graph.DirectedEdge
 * @param <V> vertex type
 */
public class UniqueUndirectedEdge<V> extends UndirectedEdge<V> {

    public UniqueUndirectedEdge(V vertex1, V vertex2) {
        super(vertex1, vertex2);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof UniqueUndirectedEdge other) {
            return vertexSet().equals(other.vertexSet());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return vertexSet().hashCode();
    }
    
    private HashSet<V> vertexSet() {
        return new HashSet<>(getVertices());
    }

}
