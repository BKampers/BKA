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

    private GraphUtil() throws IllegalAccessException {
    }

    /**
     * @param <V> vertex type
     * @param graph
     * @return true if given graph is not empty and contains only directed edges
     */
    public static <V> boolean isDirected(GraphBase<V, Edge<V>> graph) {
        return ! graph.getEdges().isEmpty() && graph.getEdges().stream().allMatch(edge -> edge.isDirected());
    }

    /**
     * @param <V> vertex type
     * @param graph
     * @return true if given graph is not empty and contains only undirected edges
     */
    public static <V> boolean isUndirected(GraphBase<V, Edge<V>> graph) {
        return ! graph.getEdges().isEmpty() && graph.getEdges().stream().allMatch(edge -> ! edge.isDirected());
    }

    /**
     * @param <V> vertex type
     * @param graph
     * @return true if given graph has at least one directed edge and at least one undirected edge
     */
    public static <V> boolean isMixed(GraphBase<V, Edge<V>> graph) {
        boolean hasDirectedEdge = false;
        boolean hasUndirectedEdge = false;
        for (Edge<V> edge : graph.getEdges()) {
            if (edge.isDirected()) {
                hasDirectedEdge = true;
            }
            else {
                hasUndirectedEdge = true;
            }
            if (hasDirectedEdge && hasUndirectedEdge) {
                return true;
            }
        }
        return false;
    }

    public static <V> int degree(GraphBase<V, Edge<V>> graph, V vertex) {
        int count = 0;
        for (Edge<V> edge : graph.getEdges()) {
            count += edge.getVertices().stream().filter(v -> vertex.equals(v)).count();
        }
        return count;
    }

    public static <V> String toString(Edge<V> edge) {
        return toString(edge, Objects::toString);
    }

    public static <V> String toString(Edge<V> edge, Function<V, String> vertexToString) {
        StringBuilder builder = new StringBuilder();
        edge.getVertices().forEach(v -> {
            if (! builder.isEmpty()) {
                builder.append(',');
            }
            builder.append(vertexToString.apply(v));
        });
        builder.insert(0, '{');
        builder.append('}');
        return builder.toString();
    }

}
