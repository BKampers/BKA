/*
** © Bart Kampers
*/

package bka.math.graphs.utils;

import bka.math.graphs.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.*;

public class GraphUtilTest {

    @Test
    public void testDegree() {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addVertex("i");
        graph.addEdge(new UndirectedEdge<>("A", "B"));
        graph.addEdge(new UndirectedEdge<>("B", "C"));
        graph.addEdge(new UndirectedEdge<>("C", "D"));
        graph.addEdge(new UndirectedEdge<>("B", "D"));
        graph.addEdge(new UndirectedEdge<>("D", "D"));
        assertEquals(0, GraphUtil.degree(graph, "i"));
        assertEquals(1, GraphUtil.degree(graph, "A"));
        assertEquals(3, GraphUtil.degree(graph, "B"));
        assertEquals(2, GraphUtil.degree(graph, "C"));
        assertEquals(4, GraphUtil.degree(graph, "D"));
    }

    @Test
    public void testUndirected() {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addEdge(new UndirectedEdge<>("1", "2"));
        graph.addEdge(new UndirectedEdge<>("3", "4"));
        assertTrue(GraphUtil.isUndirected(graph));
        assertFalse(GraphUtil.isDirected(graph));
        assertFalse(GraphUtil.isMixed(graph));
    }

    @Test
    public void testDirected() {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addEdge(new DirectedEdge<>("1", "2"));
        graph.addEdge(new DirectedEdge<>("3", "4"));
        assertTrue(GraphUtil.isDirected(graph));
        assertFalse(GraphUtil.isUndirected(graph));
        assertFalse(GraphUtil.isMixed(graph));
    }

    @Test
    public void testMixed() {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addEdge(new DirectedEdge<>("1", "2"));
        graph.addEdge(new UndirectedEdge<>("3", "4"));
        assertTrue(GraphUtil.isMixed(graph));
        assertFalse(GraphUtil.isDirected(graph));
        assertFalse(GraphUtil.isUndirected(graph));
    }
    
}