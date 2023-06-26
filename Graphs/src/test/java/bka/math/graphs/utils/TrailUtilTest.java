/*
** © Bart Kampers
*/

package bka.math.graphs.utils;

import bka.math.graphs.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.*;


public class TrailUtilTest {
  
     @Test
     public void testFindTrail() {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
         graph.addEdge(new UndirectedEdge<>("a", "a"));
         graph.addEdge(new UndirectedEdge<>("a", "b"));
         graph.addEdge(new UndirectedEdge<>("a", "d"));
         graph.addEdge(new UndirectedEdge<>("b", "c"));
         graph.addEdge(new UndirectedEdge<>("b", "d"));
         graph.addEdge(new UndirectedEdge<>("c", "d"));
         graph.addEdge(new UndirectedEdge<>("c", "e"));
         graph.addEdge(new UndirectedEdge<>("c", "f"));
         graph.addEdge(new UndirectedEdge<>("d", "e"));
         graph.addEdge(new UndirectedEdge<>("f", "f"));
         graph.addEdge(new UndirectedEdge<>("y", "z"));
        validateTrail(graph, "a", "a");
        validateTrail(graph, "a", "b");
        validateTrail(graph, "a", "c");
        validateTrail(graph, "a", "d");
        validateTrail(graph, "a", "e");
        validateTrail(graph, "a", "f");
        validateTrail(graph, "b", "a");
        validateTrail(graph, "b", "b");
        validateTrail(graph, "b", "c");
        validateTrail(graph, "b", "d");
        validateTrail(graph, "b", "e");
        validateTrail(graph, "b", "f");
        validateTrail(graph, "c", "a");
        validateTrail(graph, "c", "b");
        validateTrail(graph, "c", "c");
        validateTrail(graph, "c", "d");
        validateTrail(graph, "c", "e");
        validateTrail(graph, "c", "f");
        validateTrail(graph, "d", "a");
        validateTrail(graph, "d", "b");
        validateTrail(graph, "d", "c");
        validateTrail(graph, "d", "d");
        validateTrail(graph, "d", "e");
        validateTrail(graph, "d", "f");
        validateTrail(graph, "e", "a");
        validateTrail(graph, "e", "b");
        validateTrail(graph, "e", "c");
        validateTrail(graph, "e", "d");
        validateTrail(graph, "e", "e");
        validateTrail(graph, "e", "f");
        validateTrail(graph, "f", "a");
        validateTrail(graph, "f", "b");
        validateTrail(graph, "f", "c");
        validateTrail(graph, "f", "d");
        validateTrail(graph, "f", "e");
        validateTrail(graph, "f", "f");
        validateTrail(graph, "y", "z");
        validateTrail(graph, "z", "y");
        assertNull(TrailUtil.findTrail(graph, "a", "z"));
        assertNull(TrailUtil.findTrail(graph, "y", "f"));
    }
     
    private void validateTrail(DefaultMutableGraph<String> graph, String start, String end) {
        List<Edge<String>> trail = TrailUtil.findTrail(graph, start, end);
        assertNotNull(trail);
        assertFalse(trail.isEmpty());
        assertTrue(trail.get(trail.size() - 1).getVertices().contains(end));
        String vertex = start;
        for (Edge<String> edge : trail) {
            assertTrue(edge.getVertices().contains(vertex));
            vertex = EdgeUtil.getAdjacentVertex(edge, vertex);
        }
    }

}