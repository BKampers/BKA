/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;

import java.util.*;


public class UniqueDirectedEdge<V> extends DirectedEdge<V> {

    public UniqueDirectedEdge(V origin, V terminus) {
        super(origin, terminus);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null || !getClass().equals(object.getClass())) {
            return false;
        }
        UniqueDirectedEdge other = (UniqueDirectedEdge) object;
        return Objects.equals(getOrigin(), other.getOrigin()) && Objects.equals(getTerminus(), other.getTerminus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrigin(), getTerminus());
    }
}
