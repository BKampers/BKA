/*
 * © Bart Kampers
 */
package bka.math.graphs.finders;

import org.junit.jupiter.api.*;

public class NonRecursiveUndirectedTrailFinderTest extends UndirectedTrailFinderTestBase {

    @Test
    public void testFind() {
        testFind(new NonRecursiveTrailFinder());
    }

}
