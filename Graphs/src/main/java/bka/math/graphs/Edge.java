/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;

import java.util.*;


public interface Edge<V> {

    /**
     * @return a collection of the two vertices that this edge connetcs.
     */
    Collection<V> getVertices();

    /**
     * @return true if this edge is directed, false if this edge is undirected.
     */
    boolean isDirected();

}
