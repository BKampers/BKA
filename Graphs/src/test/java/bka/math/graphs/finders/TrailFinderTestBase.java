/*
** © Bart Kampers
*/

package bka.math.graphs.finders;

import bka.math.graphs.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.fail;


@SuppressWarnings("unchecked")
public abstract class TrailFinderTestBase {

    protected abstract List<TestCase> getTestCases();

    protected void testFind(TrailFinder finder) {
        getTestCases().forEach(testCase -> testCase.expectedResults.forEach(expected -> assertTrails(finder, testCase, expected)));
    }

    protected static Collection<Edge> graph(Edge... edges) {
        return List.of(edges);
    }

    protected static List<Edge> trail(Edge... edges) {
        return List.of(edges);
    }

    private static void assertTrails(TrailFinder finder, TestCase testCase, ExpectedTrails expected) {
        assertTrails(finder, testCase, false, expected);
        assertTrails(finder, testCase, true, expected);
    }

    private static void assertTrails(TrailFinder finder, TestCase testCase, boolean revisit, ExpectedTrails expectedResult) {
        Collection<List<Edge>> expected = (revisit) ? expectedResult.trails : expectedResult.paths;
        Collection<List<Edge>> result = finder.find(testCase.graph, expectedResult.start, expectedResult.end, revisit);
        if (!equals(expected, result)) {
            fail(((revisit) ? "Trails" : "Paths") + ": " + expectedResult.start + "->" + expectedResult.end + " " + testCase.graph + " Expected: " + expected + " but was: " + result);
        }
    }

    private static boolean equals(Collection<List<Edge>> trails1, Collection<List<Edge>> trails2) {
        return trails1.size() == trails2.size() && trails1.containsAll(trails2);
    }

    protected class ExpectedTrails {

        ExpectedTrails(Object start, Object end, Collection<List<Edge>> paths, Collection<List<Edge>> trails) {
            this.start = start;
            this.end = end;
            this.paths = paths;
            this.trails = trails;
        }

        private final Object start;
        private final Object end;
        private final Collection<List<Edge>> paths;
        private final Collection<List<Edge>> trails;

    }

    protected class TestCase {

        TestCase(Collection<Edge> graph, Collection<ExpectedTrails> expectedResults) {
            this.graph = graph;
            this.expectedResults = expectedResults;
        }

        private final Collection<Edge> graph;
        private final Collection<ExpectedTrails> expectedResults;
    }


}
