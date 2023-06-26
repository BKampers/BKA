/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;

import java.util.*;


public abstract class AbstractEdge<V> implements Edge<V> {

    public AbstractEdge(V vertex1, V vertex2) {
        vertices.add(vertex1);
        vertices.add(vertex2);
    }

    public AbstractEdge(Edge<V> other) {
        vertices.addAll(other.getVertices());
    }

    @Override
    public Collection<V> getVertices() {
        return Collections.unmodifiableCollection(vertices);
    }

    @Override
    public String toString() {
        return String.format(stringPattern(), toString(vertices.get(0)), toString(vertices.get(1)));
    }

    protected String toString(V vertex) {
        return vertex.toString();
    }

    protected String stringPattern() {
        return "{%s,%s}";
    }

    protected List<V> vertices() {
        return vertices;
    }

    private final List<V> vertices = new ArrayList<>(2);
}
