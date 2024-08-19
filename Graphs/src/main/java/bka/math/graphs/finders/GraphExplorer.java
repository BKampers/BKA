/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs.finders;

import bka.math.graphs.*;
import java.util.*;
import java.util.function.*;

/**
 * Finds trails, paths, cycles and circuits in graphs.
 * Implementation is backed by a TrailFinder that is able to filter and restrict the found resolutions.
 * 
 * @param <V> vertex type 
 * @param <E> edge type
 * @see bka.math.graphs.GraphBase
 * @see bka.math.graphs.finders.TrailFinder
 */
public class GraphExplorer<V, E extends Edge<V>> {

    public GraphExplorer(TrailFinder<V, E> finder) {
        this.finder = Objects.requireNonNull(finder);
    }
    
    /**
     * @return GraphExplorer without filter, restriction or limit, for an undirected graph
     */
    public static GraphExplorer undirected() {
        return new GraphExplorer(new RecursiveTrailFinder());
    }
    
    /**
     * @param <E> edge type
     * @param restriction Condition trails have to meet in order to be included in the resolutions. 
     *                    When finding trails, this explorer will abandon incomplete trails that do not meet given restrcition condition.
     * @return GraphExplorer with given restriction for an undirected graph of given edge type.
     */
    public static <E> GraphExplorer undirected(Predicate<List<E>> restriction) {
        RecursiveTrailFinder finder = new RecursiveTrailFinder();
        finder.setRestriction(restriction);
        return new GraphExplorer(finder);
    }
    
    /**
     * @param <E> edge type
     * @param limiter Condition the collection of resolutions has to meet in order to stop searching for more resolutions. 
     * @return GraphExplorer with given limiter for an undirected graph of given edge type.
     */
    public static <E> GraphExplorer undirectedLimited(Predicate<Collection<List<Edge>>> limiter) {
        NonRecursiveTrailFinder finder = new NonRecursiveTrailFinder();
        finder.setLimiter(limiter);
        return new GraphExplorer(finder);
    }
    
    /**
     * @param <E> edge type
     * @param restriction Condition trails have to meet in order to be included in the resolutions. 
     *                    When finding trails, this explorer will abandon incomplete trails that do not meet given restrcition condition.
     * @param limiter Condition the collection of resolutions has to meet in order to stop searching for more resolutions. 
     * @return GraphExplorer with given limiter for an undirected graph of given edge type.
     */
    public static <E> GraphExplorer undirectedLimited(Predicate<List<E>> restriction, Predicate<Collection<List<Edge>>> limiter) {
        NonRecursiveTrailFinder finder = new NonRecursiveTrailFinder();
        finder.setRestriction(restriction);
        finder.setLimiter(limiter);
        return new GraphExplorer(finder);
    }
    
    /**
     * @return GraphExplorer without filter, restriction or limit, for a directed graph
     */
    public static GraphExplorer directed() {
        return new GraphExplorer(new RecursiveDirectedTrailFinder());
    }
    
    /**
     * @param <E> edge type
     * @param restriction Condition trails have to meet in order to be included in the resolutions. 
     *                    When finding trails, this explorer will abandon incomplete trails that do not meet given restrcition condition.
     * @return GraphExplorer with given restriction for a directed graph of given edge type.
     */
    public static <E> GraphExplorer directed(Predicate<List<E>> restriction) {
        RecursiveDirectedTrailFinder finder = new RecursiveDirectedTrailFinder();
        finder.setRestriction(restriction);
        return new GraphExplorer(finder);
    }
        
    /**
     * Find all trails from given vertex in a graph to any end vertex. A trail is a sequence of edges joining a sequence of vertices. Vertices in a trail may be
     * repeated but edges may not be repeated.
     *
     * @param graph
     * @param vertex
     * @return a Collection of all trails, empty if no trail can be found
     */
    public Collection<List<E>> findAllTrails(GraphBase<V, E> graph, V vertex) {
        return findAllTrails(graph, vertex, null);
    }
    
    /**
     * Find all trails between two vertices in a graph. A trail is a sequence of edges joining a sequence of vertices. Vertices in a trail may be
     * repeated but edges may not be repeated.
     *
     * @param graph
     * @param start
     * @param end
     * @return a Collection of all trails, empty if no trail can be found
     */
    public Collection<List<E>> findAllTrails(GraphBase<V, E> graph, V start, V end) {
        requireVertex(graph, start);
        if (end != null) {
            requireVertex(graph, end);
        }
        return finder.find(graph.getEdges(), start, end, true);
    }

    /**
     * Find all circuits in a graph that contain a given vertex. A circuit is a sequence of edges joining a sequence of vertices where the tail of the
     * last edge is the same vertex as the head of the first vertex. Vertices in a circuit may be repeated but edges may not be repeated.
     *
     * @param graph
     * @param vertex
     * @return a collection of all circuits that contain the vertex, empty if no cirtcuit can be found.
     */
    public Collection<List<E>> findAllCircuits(GraphBase<V, E> graph, V vertex) {
        requireVertex(graph, vertex);
        return finder.find(graph.getEdges(), vertex, vertex, true);
    }

    /**
     * Find all paths from given vertex in a graph to any end vertex. A path is a sequence of edges joining a sequence of vertices. Vertices in a path may
     * not be repeated, which also means that edges may not be repeated.
     *
     * @param graph
     * @param vertex
     * @return a Collection of all trails, empty if no trail can be found
     */
    public Collection<List<E>> findAllPaths(GraphBase<V, E> graph, V vertex) {
        return findAllPaths(graph, vertex, null);
    }
        
    /**
     * Find all paths between two vertices in a graph. A path is a sequence of edges joining a sequence of vertices. Vertices in a path may
     * not be repeated, which also means that edges may not be repeated.
     *
     * @param graph
     * @param start
     * @param end
     * @return a Collection of all trails, empty if no trail can be found
     */
    public Collection<List<E>> findAllPaths(GraphBase<V, E> graph, V start, V end) {
        if (start.equals(end)) {
            return findAllCycles(graph, start);
        }
        requireVertex(graph, start);
        if (end != null) {
            requireVertex(graph, end);
        }
        return finder.find(graph.getEdges(), start, end, false);
    }

    /**
     * Find all cycles in a graph that contain a given vertex. A cycle is a sequence of edges joining a sequence of vertices where the tail of the
     * last edge is the same vertex as the head of the first vertex. Vertices and edges in a cycle may not be repeated.
     *
     * @param graph
     * @param vertex
     * @return a collection of all cycles that contain the vertex, empty if no cycle can be found.
     */
    public Collection<List<E>> findAllCycles(GraphBase<V, E> graph, V vertex) {
        requireVertex(graph, vertex);
        return finder.find(graph.getEdges(), vertex, vertex, false);
    }

    private static <V, E extends Edge<V>> void requireVertex(GraphBase<V, E> graph, V vertex) {
        if (! graph.getVertices().contains(vertex)) {
            throw new NoSuchElementException(vertex.toString());
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '<' + finder.getClass().getName() + '>';
    }

    private final TrailFinder<V, E> finder;

}
