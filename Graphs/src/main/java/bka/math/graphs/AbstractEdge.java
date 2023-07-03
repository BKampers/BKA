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
        vertices = List.of(vertex1, vertex2);
    }

    public AbstractEdge(Edge<V> other) {
        if (other.getVertices().size() != VERTEX_COUNT) {
            throw new IllegalArgumentException("Invalid vertex count");
        }
        vertices = Collections.unmodifiableList(new ArrayList<>(other.getVertices()));
    }

    @Override
    public Collection<V> getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        return String.format(stringPattern(), toString(vertices.get(VERTEX_1)), toString(vertices.get(VERTEX_2)));
    }

    protected String toString(V vertex) {
        return vertex.toString();
    }

    protected String stringPattern() {
        return "{%s,%s}";
    }

    protected V getVertex1() {
        return vertices.get(VERTEX_1);
    }

    protected V getVertex2() {
        return vertices.get(VERTEX_2);
    }

    private final List<V> vertices;

    private static final int VERTEX_1 = 0;
    private static final int VERTEX_2 = 1;
    private static final int VERTEX_COUNT = 2;
}
