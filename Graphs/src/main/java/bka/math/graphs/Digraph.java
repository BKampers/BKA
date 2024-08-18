/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.math.graphs;

/**
 * Directed graph of given vertex type.
 * Allows only directed edges.
 * @see bka.math.graph.GraphBase
 * @see bka.math.graph.DirectedEdge
 * @param <V> vertex
 */
public interface Digraph<V> extends GraphBase<V, DirectedEdge<V>> {
}
