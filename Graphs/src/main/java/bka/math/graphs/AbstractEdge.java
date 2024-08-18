/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.math.graphs;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Abstract base class for directed and undirected edges, including loops. 
 * An edge that joins a vertex with itself reperesents a loop.
 * @see bka.math.graphs.Edge
 * @param <V> vertex type
 */
public abstract class AbstractEdge<V> implements Edge<V> {

    public AbstractEdge(V vertex1, V vertex2) {
        vertices = List.of(Objects.requireNonNull(vertex1), Objects.requireNonNull(vertex2));
    }

    public AbstractEdge(Edge<V> other) {
        this(other.getVertices());
    }

    public AbstractEdge(Collection<V> vertices) {
        if (vertices.size() != VERTEX_COUNT) {
            throw new IllegalArgumentException("Illegal vertex count: " + vertices.size());
        }
        this.vertices = vertices.stream()
            .map(Objects::requireNonNull)
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<V> getVertices() {
        return vertices;
    }

    protected V getVertex1() {
        return vertices.get(VERTEX_1);
    }

    protected V getVertex2() {
        return vertices.get(VERTEX_2);
    }

    @Override
    public String toString() {
        return toString(Objects::toString);
    }

    public String toString(Function<V, String> vertexToString) {
        return vertices.stream()
            .map(vertexToString)
            .collect(getCollector());
    }

    private Collector<CharSequence, ?, String> getCollector() {
        return (isDirected())
            ? Collectors.joining(",", "(", ")")
            : Collectors.joining(",", "{", "}");
    }
    
    private final List<V> vertices;

    private static final int VERTEX_1 = 0;
    private static final int VERTEX_2 = 1;
    private static final int VERTEX_COUNT = 2;
}
