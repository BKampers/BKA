/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.math.graphs.utils;

import bka.math.graphs.*;
import java.util.*;
import java.util.function.*;


public final class GraphUtil {

    private GraphUtil() {
    }

    /**
     * @param <V> vertex type
     * @param graph
     * @return true iff given graph is not empty and contains only directed edges
     */
    public static <V> boolean isDirected(GraphBase<V, Edge<V>> graph) {
        return ! graph.getEdges().isEmpty() && graph.getEdges().stream().allMatch(edge -> edge.isDirected());
    }

    /**
     * @param <V> vertex type
     * @param graph
     * @return true iff given graph is not empty and contains only undirected edges
     */
    public static <V> boolean isUndirected(GraphBase<V, Edge<V>> graph) {
        return ! graph.getEdges().isEmpty() && graph.getEdges().stream().allMatch(edge -> ! edge.isDirected());
    }

    /**
     * @param <V> vertex type
     * @param graph
     * @return true iff given graph has at least one directed edge and at least one undirected edge
     */
    public static <V> boolean isMixed(GraphBase<V, Edge<V>> graph) {
        Iterator<Edge<V>> it = graph.getEdges().iterator();
        if (!it.hasNext()) {
            return false;
        }
        boolean isDirected = it.next().isDirected();
        while (it.hasNext()) {
            if (it.next().isDirected() != isDirected) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param <V> vertex type
     * @param graph
     * @param vertex
     * @return the number of edges from given graph that are incident on given vertex, where a loop is counted twice.
     */
    public static <V> long degree(GraphBase<V, Edge<V>> graph, V vertex) {
        return graph.getEdges().stream()
            .map(count(vertex))
            .reduce(0L, (total, count) -> total + count);
    }

    private static <V> Function<Edge<V>, Long> count(V vertex) {
        return edge -> edge.getVertices().stream().filter(vertex::equals).count();
    }
    
    public static <V, E extends Edge<V>> List<V> vertexPath(List<E> edgePath, V start) {
        if (edgePath.isEmpty()) {
            return Collections.emptyList();
        }
        List<V> path = new ArrayList<>(edgePath.size() + 1);
        path.add(start);
        edgePath.forEach(edge -> path.add(EdgeUtil.getAdjacentVertex(edge, path.getLast()))); 
        return path;
    }

}
