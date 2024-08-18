/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.math.graphs;

import java.util.*;

/**
 * Edge, directed or undirected, joining two vertices in a graph.
 * @see <a href="https://en.wikipedia.org/wiki/Glossary_of_graph_theory#edge"> Glossary of graph theory on Wikipedia</a>
 * @see bka.math.graphs.GraphBase
 * @see bka.math.graphs.Graph
 * @param <V> vertex type
 */
public interface Edge<V> {

    /**
     * @return unmodifiable collection of the two vertices that this edge connetcs. 
     *         If this edge is directed, the collection is ordered, having origin first and terminius second.
     */
    Collection<V> getVertices();

    /**
     * @return true if this edge is directed, false if this edge is undirected.
     */
    boolean isDirected();

}
