/*
** © Bart Kampers
*/

package bka.math.graphs.utils;

import bka.math.graphs.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.*;

@SuppressWarnings("unchecked")
public class EdgeUtilTest {

    @Test
    public void testGetAdjacentVertex() {
        assertEquals("2", EdgeUtil.getAdjacentVertex(new DirectedEdge<>("1", "2"), "1"));
        assertEquals("1", EdgeUtil.getAdjacentVertex(new DirectedEdge<>("1", "2"), "2"));
        assertEquals("0", EdgeUtil.getAdjacentVertex(new UndirectedEdge("0", "0"), "0"));
     }

    @Test
    public void testInvalidAdjacentVertex() {
        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> EdgeUtil.getAdjacentVertex(new UndirectedEdge("1", "2"), "x")
        );
    }

    @Test
    public void testIncident() {
        assertFalse(EdgeUtil.isIncidentWith(new DirectedEdge(1, 2), 0));
        assertTrue(EdgeUtil.isIncidentWith(new DirectedEdge(1, 2), 1));
        assertTrue(EdgeUtil.isIncidentWith(new DirectedEdge(1, 2), 2));
    }
 
    @Test
    public void testFrom() {
        Digraph graph = new DefaultMutableDigraph(Arrays.asList(AB, AC, BC));
        assertThat(filter(graph, EdgeUtil.from('A'))).containsExactlyInAnyOrder(AB, AC);
        assertThat(filter(graph, EdgeUtil.from('B'))).containsExactlyInAnyOrder(BC);
        assertThat(filter(graph, EdgeUtil.from('C'))).containsExactlyInAnyOrder();
    }

    @Test
    public void testTo() {
        Digraph graph = new DefaultMutableDigraph(Arrays.asList(AB, AC, BC));
        assertThat(filter(graph, EdgeUtil.to('A'))).containsExactlyInAnyOrder();
        assertThat(filter(graph, EdgeUtil.to('B'))).containsExactlyInAnyOrder(AB);
        assertThat(filter(graph, EdgeUtil.to('C'))).containsExactlyInAnyOrder(AC, BC);
    }

    private Collection<DirectedEdge> filter(Digraph<?> graph, Predicate<DirectedEdge> predicate) {
        return graph.getEdges().stream().filter(predicate).collect(Collectors.toList());
    }

    private static final DirectedEdge<Character> AB = new DirectedEdge('A', 'B');
    private static final DirectedEdge<Character> AC = new DirectedEdge('A', 'C');
    private static final DirectedEdge<Character> BC = new DirectedEdge('B', 'C');

}
