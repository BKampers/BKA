/*
** © Bart Kampers
*/

package bka.math.graphs;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public abstract class GraphTestBase {

    protected static <T> void assertEqualCollections(Collection<T> expected, Collection<T> actual) {
        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
    }
}
