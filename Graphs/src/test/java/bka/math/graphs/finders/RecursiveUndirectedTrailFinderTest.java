/*
 * © Bart Kampers
 */
package bka.math.graphs.finders;

import org.junit.jupiter.api.*;

public class RecursiveUndirectedTrailFinderTest extends UndirectedTrailFinderTestBase {

    @Test
    public void testFind() {
        testFind(new RecursiveTrailFinder());
    }

}
