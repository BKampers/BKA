/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;


public class UndirectedEdge<V> extends AbstractEdge<V> {

    public UndirectedEdge(V vertex1, V vertex2) {
        super(vertex1, vertex2);
    }

    public UndirectedEdge(Edge<V> other) {
        super(other);
    }

    @Override
    public final boolean isDirected() {
        return false;
    }

}
