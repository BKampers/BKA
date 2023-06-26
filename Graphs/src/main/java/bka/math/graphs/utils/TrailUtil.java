/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs.utils;

import bka.math.graphs.*;
import java.util.*;

/**
 * Util to find trails, paths, circuits and cycles in undirected graphs.
 *
 * @see https://en.wikipedia.org/wiki/Glossary_of_graph_theory_terms
 * @author bartkampers
 */
public final class TrailUtil {

    private TrailUtil() throws IllegalAccessException {
    }

    /**
     * Find a trial between two vertices in a graph. A trail is a sequence of
     * edges joining a sequence of vertices. Vertices in a trail may be repeated
     * but edges may not be repeated.
     *
     * @param <V> Type of vertex
     * @param <E> Type of edge extending Edge&lt;V&gt;
     * @param graph
     * @param start
     * @param end
     * @return a trail in given graph from start to end if possible, null if no trail can be found,
     */
    public static <V, E extends Edge<V>> List<E> findTrail(GraphBase<V, E> graph, V start, V end) {
        LinkedList<E> trail = new LinkedList<>();
        Collection<E> edgesToExplore = new HashSet<>(graph.getEdges());
        V vertex = start;
        while (! edgesToExplore.isEmpty()) {
            final V v = vertex;
            E edge = edgesToExplore.stream().filter(EdgeUtil.isIncidentWith(v)).findFirst().orElse(null);
            if (edge != null) {
                edgesToExplore.remove(edge);
                trail.add(edge);
                vertex = EdgeUtil.getAdjacentVertex(edge, vertex);
                if (vertex.equals(end)) {
                    return trail;
                }
            }
            else if (trail.isEmpty()) {
                return null;
            }
            else {
                E removedEdge = trail.removeLast();
                vertex = EdgeUtil.getAdjacentVertex(removedEdge, vertex);
            }
        }
        return null;
    }

}
