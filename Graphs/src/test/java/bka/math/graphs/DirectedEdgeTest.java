/*
** © Bart Kampers
*/

package bka.math.graphs;

import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class DirectedEdgeTest extends GraphTestBase {

    @Test
    public void testEdge() {
        DirectedEdge edge = new DirectedEdge('A', 'B');
        assertDirectedEdge(edge, 'A', 'B');
    }

    @Test
    public void testCopyFromDirectedEdge() {
        DirectedEdge edge = new DirectedEdge(new DirectedEdge('A', 'B'));
        assertDirectedEdge(edge, 'A', 'B');
    }

    @Test
    public void testLoop() {
        DirectedEdge edge = new DirectedEdge('A', 'A');
        assertDirectedEdge(edge, 'A', 'A');
    }

    private static void assertDirectedEdge(DirectedEdge edge, Object origin, Object terminus) {
        assertTrue(edge.isDirected());
        assertEquals(origin, edge.getOrigin());
        assertEquals(terminus, edge.getTerminus());
        assertEqualCollections(List.of(origin, terminus), edge.getVertices());
        assertEquals("(" + origin.toString() + "," + terminus.toString() + ")", edge.toString());
    }

  

}