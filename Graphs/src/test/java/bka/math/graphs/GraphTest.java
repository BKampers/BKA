/*
** © Bart Kampers
*/
package bka.math.graphs;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.*;

public class GraphTest {

    @Test
    public void testGraphs() {
        Edge<Character> ab = new UndirectedEdge<>('A', 'B');
        Edge<Character> ac = new UndirectedEdge<>('A', 'C');
        Edge<Character> bc = new UndirectedEdge<>('B', 'C');
        Edge<Character> bd = new UndirectedEdge<>('B', 'D');
        GraphBase<Character, Edge<Character>> immutable = new ImmutableGraph<>(Arrays.asList(ab, ac));
        MutableGraph<Character, Edge<Character>> mutable = new MutableGraph<>(immutable);
        assertEquals(immutable.getVertices(), mutable.getVertices());
        assertEquals(immutable.getEdges(), mutable.getEdges());
        mutable.addEdge(bc);
        assertEquals(immutable.getVertices(), mutable.getVertices());
        assertNotEquals(immutable.getEdges(), mutable.getEdges());
        DefaultMutableGraph<Character> default1 = new DefaultMutableGraph<>(immutable);
        assertEquals(immutable.getVertices(), default1.getVertices());
        assertEquals(immutable.getEdges(), default1.getEdges());
        DefaultMutableGraph<Character> default2 = new DefaultMutableGraph<>(mutable.getVertices(), mutable.getEdges());
        assertEquals(mutable.getVertices(), default2.getVertices());
        assertEquals(mutable.getEdges(), default2.getEdges());
    }

}
