/*
** © Bart Kampers
*/

package bka.math.graphs;

import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class UndirectedEdgeTest extends GraphTestBase {

    @Test
    public void testEdge() {
        UndirectedEdge edge = new UndirectedEdge('A', 'B');
        assertUndirectedEdge(edge, 'A', 'B');
    }

    @Test
    public void testCopyFromDirectedEdge() {
        UndirectedEdge edge = new UndirectedEdge(new DirectedEdge('A', 'B'));
        assertUndirectedEdge(edge, 'A', 'B');
    }

    @Test
    public void testLoop() {
        UndirectedEdge edge = new UndirectedEdge('A', 'A');
        assertUndirectedEdge(edge, 'A', 'A');
    }

    private static void assertUndirectedEdge(UndirectedEdge edge, Object vertex1, Object vertex2) {
        assertFalse(edge.isDirected());
        assertEqualCollections(List.of(vertex1, vertex2), edge.getVertices());
    }

}
