/*
** © Bart Kampers
*/

package bka.math.graphs;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.*;

@SuppressWarnings("unchecked")
public class ImmutableGraphTest extends GraphTestBase {


    @Test
    public void testConstructEmpty() {
        ImmutableGraph graph = new ImmutableGraph(Collections.emptyList());
        assertTrue(graph.getVertices().isEmpty());
        assertTrue(graph.getEdges().isEmpty());
    }

    public void testConstructFromEdges() {
        Collection<Edge> edges = List.of(edge(1, 2), edge(2, 3));
        ImmutableGraph graph = new ImmutableGraph(edges);
        assertEqualCollections(edges, graph.getEdges());
        assertEqualCollections(List.of(1, 2, 3), graph.getVertices());
    }

    public void testConstructFromEdgesAndVertices() {
        Collection<Edge> edges = List.of(edge(1, 2), edge(2, 3));
        ImmutableGraph graph = new ImmutableGraph(List.of(3, 4, 5), edges);
        assertEqualCollections(edges, graph.getEdges());
        assertEqualCollections(List.of(1, 2, 3, 4, 5), graph.getVertices());
    }

    @Test
    public void testContructFromGraph() {
        GraphBase origin = createGraphBase(edge(1, 2));
        ImmutableGraph graph = new ImmutableGraph(origin);
        assertEqualCollections(origin.getEdges(), graph.getEdges());
        assertEqualCollections(origin.getVertices(), graph.getVertices());
    }

    private static GraphBase createGraphBase(Edge edge) {
        return new GraphBase() {
            @Override
            public Set<Object> getVertices() {
                return new HashSet(edge.getVertices());
            }

            @Override
            public Set<Edge> getEdges() {
                return Set.of(edge);
            }
        };
    }

    private static Edge edge(Object vertex1, Object vertex2) {
        return new Edge() {
            @Override
            public Collection getVertices() {
                return List.of(vertex1, vertex2);
            }

            @Override
            public boolean isDirected() {
                throw new UnsupportedOperationException("Not supposed to be called in this test");
            }

        };
    }

}