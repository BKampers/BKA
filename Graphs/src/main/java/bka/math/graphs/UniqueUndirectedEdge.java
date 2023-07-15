/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;

import java.util.*;
import java.util.stream.*;


public class UniqueUndirectedEdge<V> extends UndirectedEdge<V> {

    public UniqueUndirectedEdge(V vertex1, V vertex2) {
        super(vertex1, vertex2);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null || !getClass().equals(object.getClass())) {
            return false;
        }
        Collection<?> thisVertices = new ArrayList(getVertices());
        Collection<?> otherVertices = ((UniqueUndirectedEdge) object).getVertices();
        return otherVertices.containsAll(thisVertices) && thisVertices.containsAll(otherVertices);
    }

    @Override
    public int hashCode() {
        return getVertices().stream().map(vertex -> vertex.hashCode()).sorted().collect(Collectors.toList()).hashCode();
    }

}
