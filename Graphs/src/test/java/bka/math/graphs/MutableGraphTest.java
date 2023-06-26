/*
** © Bart Kampers
*/
package bka.math.graphs;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.*;

@SuppressWarnings("unchecked")
public class MutableGraphTest {


    @Test
    public void testInitial() {
        graph = new MutableGraph();
        assertTrue(graph.getEdges().isEmpty());
        assertTrue(graph.getVertices().isEmpty());
    }

    @Test
    public void testAddVertex() {
        graph = new MutableGraph();
        assertTrue(graph.addVertex(1));
        assertEqualCollections(List.of(1), graph.getVertices());
        assertTrue(graph.addVertex(2));
        assertEqualCollections(List.of(1, 2), graph.getVertices());
        assertFalse(graph.addVertex(1));
        assertEqualCollections(List.of(1, 2), graph.getVertices());
        assertTrue(graph.getEdges().isEmpty());
    }

    @Test
    public void testAddVertices() {
        graph = new MutableGraph();
        assertTrue(graph.addVertices(List.of('a', 'b')));
        assertEqualCollections(List.of('a', 'b'), graph.getVertices());
        assertTrue(graph.addVertices(List.of('b', 'c', 'd')));
        assertEqualCollections(List.of('a', 'b', 'c', 'd'), graph.getVertices());
        assertFalse(graph.addVertices(List.of('a', 'b', 'c', 'd')));
        assertEqualCollections(List.of('a', 'b', 'c', 'd'), graph.getVertices());
        assertTrue(graph.getEdges().isEmpty());
    }

    @Test
    public void testAddEdge() {
        Edge ab = new UndirectedEdge("A", "B");
        Edge bc = new UndirectedEdge("B", "C");
        graph = new MutableGraph();
        assertTrue(graph.addEdge(ab));
        assertEqualCollections(List.of(ab), graph.getEdges());
        assertEqualCollections(List.of("A", "B"), graph.getVertices());
        assertTrue(graph.addEdge(bc));
        assertEqualCollections(List.of(ab, bc), graph.getEdges());
        assertEqualCollections(List.of("A", "B", "C"), graph.getVertices());
        assertFalse(graph.addEdge(bc));
        assertEqualCollections(List.of(ab, bc), graph.getEdges());
        assertEqualCollections(List.of("A", "B", "C"), graph.getVertices());
    }

    @Test
    public void testAddEdges() {
        Edge ab = new UndirectedEdge("A", "B");
        Edge ac = new UndirectedEdge("A", "C");
        Edge bc = new UndirectedEdge("B", "C");
        graph = new MutableGraph();
        assertTrue(graph.addEdges(List.of(ab, ac)));
        assertEqualCollections(List.of(ab, ac), graph.getEdges());
        assertEqualCollections(List.of("A", "B", "C"), graph.getVertices());
        assertTrue(graph.addEdges(List.of(ab, bc)));
        assertEqualCollections(List.of(ab, ac, bc), graph.getEdges());
        assertEqualCollections(List.of("A", "B", "C"), graph.getVertices());
        assertFalse(graph.addEdges(List.of(ac, bc)));
        assertEqualCollections(List.of(ab, ac, bc), graph.getEdges());
        assertEqualCollections(List.of("A", "B", "C"), graph.getVertices());
    }

    @Test
    public void testRemoveVertex() {
        Edge ab = new UndirectedEdge('a', 'b');
        Edge ac = new UndirectedEdge('a', 'c');
        Edge bc = new UndirectedEdge('b', 'c');
        graph = new MutableGraph(List.of(ab, ac, bc));
        assertTrue(graph.removeVertex('c'));
        assertEqualCollections(List.of('a', 'b'), graph.getVertices());
        assertEqualCollections(List.of(ab), graph.getEdges());
        assertTrue(graph.removeVertex('b'));
        assertEqualCollections(List.of('a'), graph.getVertices());
        assertEqualCollections(List.of(), graph.getEdges());
        assertFalse(graph.removeVertex('b'));
        assertEqualCollections(List.of('a'), graph.getVertices());
        assertEqualCollections(List.of(), graph.getEdges());
    }

    @Test
    public void testRemoveVertices() {
        Edge ab = new UndirectedEdge('a', 'b');
        Edge ac = new UndirectedEdge('a', 'c');
        Edge bc = new UndirectedEdge('b', 'c');
        graph = new MutableGraph(List.of(ab, ac, bc));
        assertTrue(graph.removeVertices(List.of('a', 'b')));
        assertEqualCollections(List.of('c'), graph.getVertices());
        assertEqualCollections(List.of(), graph.getEdges());
        assertFalse(graph.removeVertices(List.of('a', 'b')));
        assertEqualCollections(List.of('c'), graph.getVertices());
        assertEqualCollections(List.of(), graph.getEdges());
        assertTrue(graph.removeVertices(List.of('c', 'd')));
        assertEqualCollections(List.of(), graph.getVertices());
        assertEqualCollections(List.of(), graph.getEdges());
    }

    @Test
    public void testRemoveEdge() {
        Edge ab = new UndirectedEdge('a', 'b');
        Edge ac = new UndirectedEdge('a', 'c');
        Edge bc = new UndirectedEdge('b', 'c');
        graph = new MutableGraph(List.of(ab, ac, bc));
        assertTrue(graph.removeEdge(ab));
        assertEqualCollections(List.of(ac, bc), graph.getEdges());
        assertEqualCollections(List.of('a', 'b', 'c'), graph.getVertices());
        assertFalse(graph.removeEdge(ab));
        assertEqualCollections(List.of(ac, bc), graph.getEdges());
        assertEqualCollections(List.of('a', 'b', 'c'), graph.getVertices());
        assertTrue(graph.removeEdge(bc));
        assertEqualCollections(List.of(ac), graph.getEdges());
        assertEqualCollections(List.of('a', 'b', 'c'), graph.getVertices());
        assertTrue(graph.removeEdge(ac));
        assertEqualCollections(List.of(), graph.getEdges());
        assertEqualCollections(List.of('a', 'b', 'c'), graph.getVertices());
    }

    @Test
    public void testRemoveEdges() {
        Edge ab = new UndirectedEdge('a', 'b');
        Edge ac = new UndirectedEdge('a', 'c');
        Edge bc = new UndirectedEdge('b', 'c');
        graph = new MutableGraph(List.of(ab, ac, bc));
        assertTrue(graph.removeEdges(List.of(ab, bc)));
        assertEqualCollections(List.of(ac), graph.getEdges());
        assertEqualCollections(List.of('a', 'b', 'c'), graph.getVertices());
        assertTrue(graph.removeEdges(List.of(ab, ac)));
        assertEqualCollections(List.of(), graph.getEdges());
        assertEqualCollections(List.of('a', 'b', 'c'), graph.getVertices());
        assertFalse(graph.removeEdges(List.of(ab, ac, bc)));
        assertEqualCollections(List.of(), graph.getEdges());
        assertEqualCollections(List.of('a', 'b', 'c'), graph.getVertices());
    }

    @Test
    public void testClear() {
        graph = new MutableGraph(
            List.of(Float.POSITIVE_INFINITY, Double.NaN, Boolean.TRUE),
            List.of(new UndirectedEdge('a', 0), new UndirectedEdge('a', "B"), new UndirectedEdge("B", new Object())));
        graph.clear();
        assertTrue(graph.getEdges().isEmpty());
        assertTrue(graph.getVertices().isEmpty());
    }

    private static <T> void assertEqualCollections(Collection<T> expected, Set<T> actual) {
        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
    }

    private MutableGraph graph;


}
