/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.math.graphs;

/**
 * Graph of verices and edges of given type. 
 * @see <a href="https://en.wikipedia.org/wiki/Graph_theory">Grahp theory on Wikipedia</a>
 * @param <V> vertex type
 */
public interface Graph<V> extends GraphBase<V, Edge<V>> {
}
