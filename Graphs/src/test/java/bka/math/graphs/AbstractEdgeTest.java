/*
** © Bart Kampers
*/
package bka.math.graphs;

import java.util.*;
import java.util.stream.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractEdgeTest { 
   
    @Test
    public void testToString() {
        assertEquals("{a,b}", UNDIRECTED_EDGE.toString());
        assertEquals("(a,b)", DIRECTED_EDGE.toString());
        assertEquals("{97,98}", UNDIRECTED_EDGE.toString(vertex -> Integer.toString(vertex)));
        assertEquals("(97,98)", DIRECTED_EDGE.toString(vertex -> Integer.toString(vertex)));
    }
    
    @Test
    public void testGetVertices() {
        assertEquals(List.of('a', 'b'), DIRECTED_EDGE.getVertices().stream().sorted().collect(Collectors.toList()));
        assertEquals(List.of('a', 'b'), DIRECTED_EDGE.getVertices());
    }
    
    
    private static final AbstractEdge<Character> UNDIRECTED_EDGE = new AbstractEdge<>('a', 'b') {
        @Override
        public boolean isDirected() {
            return false;
        }
    };
    
    private static final AbstractEdge<Character> DIRECTED_EDGE = new AbstractEdge<>(UNDIRECTED_EDGE) {
        @Override
        public boolean isDirected() {
            return true;
        }
    };

}
