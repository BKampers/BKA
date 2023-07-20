/*
** © Bart Kampers
*/

package bka.math.graphs;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class UniqueUndirectedEdgeTest {

    @Test
    public void testEquals() {
        assertEquals(new UniqueUndirectedEdge('a', 'b'), new UniqueUndirectedEdge('a', 'b'));
        assertEquals(new UniqueUndirectedEdge('a', 'b'), new UniqueUndirectedEdge('b', 'a'));
        assertNotEquals(new UniqueUndirectedEdge('a', 'b'), new UniqueUndirectedEdge('a', 'c'));
        assertNotEquals(new UniqueUndirectedEdge('a', 'b'), new UniqueUndirectedEdge('c', 'b'));
    }

    @Test
    public void testHashCode() {
        assertEquals(new UniqueUndirectedEdge('a', 'b').hashCode(), new UniqueUndirectedEdge('a', 'b').hashCode());
        assertEquals(new UniqueUndirectedEdge('a', 'b').hashCode(), new UniqueUndirectedEdge('b', 'a').hashCode());
    }

}