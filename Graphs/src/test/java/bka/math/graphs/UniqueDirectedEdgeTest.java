/*
** © Bart Kampers
*/

package bka.math.graphs;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class UniqueDirectedEdgeTest {

    @Test
    public void testEquals() {
        assertEquals(new UniqueDirectedEdge('a', 'b'), new UniqueDirectedEdge('a', 'b'));
        assertNotEquals(new UniqueDirectedEdge('a', 'b'), new UniqueDirectedEdge('b', 'a'));
        assertNotEquals(new UniqueDirectedEdge('a', 'b'), new UniqueDirectedEdge('a', 'c'));
    }

    @Test
    public void testHashCode() {
        assertEquals(new UniqueDirectedEdge('a', 'b').hashCode(), new UniqueDirectedEdge('a', 'b').hashCode());
    }

}