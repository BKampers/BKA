/*
 * © Bart Kampers
 */
package bka.math.graphs.finders;

import org.junit.jupiter.api.*;

public class RecursiveDirectedTrailFinderTest extends DirectedTrailFinderTestBase {

    @Test
    public void testFind() {
        testFind(new RecursiveDirectedTrailFinder());
    }
}
