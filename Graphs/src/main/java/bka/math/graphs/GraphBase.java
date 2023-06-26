/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;

import java.util.*;


public interface GraphBase<V, E extends Edge<V>> {

    Set<V> getVertices();
    Set<E> getEdges();

}
