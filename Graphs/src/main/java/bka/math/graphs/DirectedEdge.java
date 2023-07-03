/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;

public class DirectedEdge<V> extends AbstractEdge<V> {
    
    public DirectedEdge(V origin, V terminus) {
        super(origin, terminus);
    }

    public DirectedEdge(DirectedEdge<V> other) {
        super(other.getOrigin(), other.getTerminus());
    }

    public V getOrigin() {
        return getVertex1();
    }

    public V getTerminus() {
        return getVertex2();
    }

    @Override
    public final boolean isDirected() {
        return true;
    }

    @Override
    protected String stringPattern() {
        return "(%s,%s)";
    }

}
