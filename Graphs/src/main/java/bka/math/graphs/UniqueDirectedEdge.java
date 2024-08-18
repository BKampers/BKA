/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.math.graphs;

import java.util.*;


/**
 * Default implementation of a directed edge for given vertex type.
 * To be used in graphs where multiple edges are not allowed. That is, two or more edges that join the same origin to the same terminus, are not allowed.
 * @see bka.math.graph.DirectedEdge
 * @param <V> vertex type
 */
public class UniqueDirectedEdge<V> extends DirectedEdge<V> {

    public UniqueDirectedEdge(V origin, V terminus) {
        super(origin, terminus);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof UniqueDirectedEdge other) {
            return getOrigin().equals(other.getOrigin()) && getTerminus().equals(other.getTerminus());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrigin(), getTerminus());
    }
}
