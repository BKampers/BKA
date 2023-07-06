/*
 * © Bart Kampers
 */

package bka.math.graphs.finders;

import bka.math.graphs.*;
import java.util.*;
import java.util.function.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.*;


@SuppressWarnings("unchecked")
public class TrailFinderTest {

    @BeforeAll
    public static void setup() {
        Locale.setDefault(Locale.ENGLISH); // Decimal points instead of commas in printouts
    }

    @Test
    public void testFilters() {
        for (TrailFinder finder : getTrailFinders()) {
            long startTime = System.nanoTime();
            testFilters(finder);
            long duration = System.nanoTime() - startTime;
            System.out.printf("Class %s tested in %.3f ms\n", finder.getClass().getSimpleName(), duration / 1000000.0);
        }
    }

    @Test
    public void testLimiter() {
        final int length = 4;
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>(List.of(
            new UndirectedEdge<>("a", "b"),
            new UndirectedEdge<>("b", "c"),
            new UndirectedEdge<>("a", "c")));
        NonRecursiveTrailFinder<String, Edge<String>> finder = new NonRecursiveTrailFinder<>();
        finder.setLimiter(trails -> trails.size() >= length);
        GraphExplorer<String, Edge<String>> explorer = new GraphExplorer<>(finder);
        assertEquals(length, explorer.findAllTrails(graph, "a", null).size());
    }

    public void testFilters(TrailFinder<String, Edge<String>> finder) {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>(List.of(
            new UndirectedEdge<>("a", "b"),
            new UndirectedEdge<>("b", "c"),
            new UndirectedEdge<>("a", "c")));
        GraphExplorer<String, Edge<String>> explorer = new GraphExplorer<>(finder);
        finder.setRestriction(excludeVertex("c"));
        String[][][] expectedTrails = {
            { AB }
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", null));
        assertEqualTrails(expectedTrails, explorer.findAllPaths(graph, "a", null));
        assertTrue(explorer.findAllCircuits(graph, "a").isEmpty());
        assertTrue(explorer.findAllCycles(graph, "a").isEmpty());
        finder.setRestriction(maxLength(2));
        expectedTrails = new String[][][]{
            { AB },
            { AB, BC },
            { AC },
            { AC, BC }
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", null));
        assertEqualTrails(expectedTrails, explorer.findAllPaths(graph, "a", null));
        assertTrue(explorer.findAllCircuits(graph, "a").isEmpty());
        assertTrue(explorer.findAllCycles(graph, "a").isEmpty());
        finder.setRestriction(maxLength(3));
        String[][][] expectedPaths = expectedTrails;
        expectedTrails = new String[][][] {
            { AB },
            { AB, BC },
            { AB, BC, AC },
            { AC },
            { AC, BC },
            { AC, BC, AB }
        };
        String[][][] expectedCircuits = {
            { AB, BC, AC },
            { AC, BC, AB }
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", null));
        assertEqualTrails(expectedPaths, explorer.findAllPaths(graph, "a", null));
        assertEqualTrails(expectedCircuits, explorer.findAllCircuits(graph, "a"));
        assertEqualTrails(expectedCircuits, explorer.findAllCycles(graph, "a"));
        finder.setFilter(minLength(2));
        expectedTrails = new String[][][] {
            { AB, BC },
            { AB, BC, AC },
            { AC, BC },
            { AC, BC, AB }
        };
        expectedPaths = new String[][][] {
            { AB, BC },
            { AC, BC }
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", null));
        assertEqualTrails(expectedPaths, explorer.findAllPaths(graph, "a", null));
        assertEqualTrails(expectedCircuits, explorer.findAllCircuits(graph, "a"));
        assertEqualTrails(expectedCircuits, explorer.findAllCycles(graph, "a"));
    }

    private Predicate<List<Edge<String>>> excludeVertex(String vertex) {
        return trail -> ! trail.stream().anyMatch(edge -> edge.getVertices().contains(vertex));
    }

    private <V> Predicate<List<Edge<V>>> maxLength(int length) {
        return trail -> trail.size() <= length;
    }

    private <V> Predicate<List<Edge<V>>> minLength(int length) {
        return trail -> trail.size() >= length;
    }

    @Test
    public void testFindAllEmpty() {
        for (GraphExplorer<String, Edge<String>> explorer : getExplorers()) {
            testFindAllEmpty(explorer);
        }
    }

    private void testFindAllEmpty(GraphExplorer<String, Edge<String>> explorer) {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addEdge(new UndirectedEdge("a", "b"));
        graph.addVertex("z");
        assertTrue(explorer.findAllCircuits(graph, "a").isEmpty());
        assertTrue(explorer.findAllCircuits(graph, "b").isEmpty());
        assertTrue(explorer.findAllCircuits(graph, "z").isEmpty());
        assertTrue(explorer.findAllCycles(graph, "a").isEmpty());
        assertTrue(explorer.findAllCycles(graph, "b").isEmpty());
        assertTrue(explorer.findAllCycles(graph, "z").isEmpty());
    }


    @Test
    public void testFindAllComplex() {
        for (GraphExplorer<String, Edge<String>> explorer : getExplorers()) {
            testFindAllComplex(explorer);
        }
    }

    private void testFindAllComplex(GraphExplorer<String, Edge<String>> explorer) {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addEdge(new UndirectedEdge("a", "b"));
        graph.addEdge(new UndirectedEdge("a", "d"));
        graph.addEdge(new UndirectedEdge("b", "c"));
        graph.addEdge(new UndirectedEdge("b", "d"));
        graph.addEdge(new UndirectedEdge("c", "d"));
        graph.addEdge(new UndirectedEdge("c", "e"));
        graph.addEdge(new UndirectedEdge("c", "f"));
        graph.addEdge(new UndirectedEdge("d", "e"));
        graph.addEdge(new UndirectedEdge("e", "e"));
        graph.addEdge(new UndirectedEdge("f", "f"));
        String[][][] expectedTrails = {
            { AB, BC, CD, DE },
            { AB, BC, CD, DE, EE },
            { AB, BC, CE },
            { AB, BC, CE, EE },
            { AD, BD, BC, CE },
            { AD, BD, BC, CE, EE },
            { AB, BD, CD, CE },
            { AB, BD, CD, CE, EE },
            { AB, BD, DE },
            { AB, BD, DE, EE },
            { AD, BD, BC, CD, DE },
            { AD, BD, BC, CD, DE, EE },
            { AD, CD, BC, BD, DE },
            { AD, CD, BC, BD, DE, EE },
            { AD, CD, CE },
            { AD, CD, CE, EE },
            { AD, DE },
            { AD, DE, EE }
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", "e"));
        String[][][] expectedPaths = {
            { AB, BC, CD, DE },
            { AB, BC, CE },
            { AD, BD, BC, CE },
            { AB, BD, CD, CE },
            { AB, BD, DE },
            { AD, CD, CE },
            { AD, DE }
        };
        assertEqualTrails(expectedPaths, explorer.findAllPaths(graph, "a", "e"));
        String[][][] expectedCycles = withReversed( new String[][][] {
            { AB, BC, CD, AD },
            { AB, BC, CE, DE, AD },
            { AB, BD, AD }
        });
        assertEqualTrails(expectedCycles, explorer.findAllCycles(graph, "a"));
        String[][][] expectedCircuits = withReversed( new String[][][] {
            { AB, BC, CD, AD },
            { AB, BC, CE, DE, AD },
            { AB, BC, CE, EE, DE, AD },
            { AB, BD, AD },
            { AB, BD, CD, CE, DE, AD },
            { AB, BD, CD, CE, EE, DE, AD },
            { AB, BD, DE, CE, CD, AD },
            { AB, BD, DE, EE, CE, CD, AD }
        });
        assertEqualTrails(expectedCircuits, explorer.findAllCircuits(graph, "a"));
    }


    @Test
    public void testFindAllShort() {
        for (GraphExplorer<String, Edge<String>> explorer : getExplorers()) {
            testFindAllShort(explorer);
        }
    }

    private void testFindAllShort(GraphExplorer<String, Edge<String>> explorer) {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addEdge(new UndirectedEdge("a", "a"));
        graph.addEdge(new UndirectedEdge("a", "b"));
        graph.addEdge(new UndirectedEdge("b", "a"));
        graph.addEdge(new UndirectedEdge("b", "b"));
        String[][][] expectedTrails = {
            { AA, AB },
            { AA, AB, BB },
            { AA, BA },
            { AA, BA, BB },
            { AB },
            { AB, BB },
            { BA },
            { BA, BB }
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", "b"));
        String[][][] expectedCycles = {
            { AA },
            { AB, BA },
            { BA, AB }
        };
        assertEqualTrails(expectedCycles, explorer.findAllCycles(graph, "a"));
        String[][][] expectedPaths = {
            { AB },
            { BA }
        };
        assertEqualTrails(expectedPaths, explorer.findAllPaths(graph, "a", "b"));
        String[][][] expectedCircuits = {
            { AA },
            { AA, AB, BA },
            { AA, AB, BB, BA },
            { AA, BA, AB },
            { AA, BA, BB, AB },
            { AB, BA },
            { AB, BA, AA },
            { AB, BB, BA },
            { AB, BB, BA, AA },
            { BA, AB },
            { BA, AB, AA },
            { BA, BB, AB },
            { BA, BB, AB, AA }
        };
        assertEqualTrails(expectedCircuits, explorer.findAllCircuits(graph, "a"));
    }


    @Test
    public void testFindAllDoubleLoop() {
        for (GraphExplorer<String, Edge<String>> explorer : getExplorers()) {
            testFindAllDoubleLoop(explorer);
        }
    }

    private void testFindAllDoubleLoop(GraphExplorer<String, Edge<String>> explorer) {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>(List.of(
            new UndirectedEdge("a", "b"),
            new UndirectedEdge("b", "a"),
            new UndirectedEdge("a", "c"),
            new UndirectedEdge("c", "a")));
        String[][][] expectedCircuits = {
            { AB, BA },
            { AB, BA, AC, CA },
            { AB, BA, CA, AC },
            { AC, CA },
            { AC, CA, AB, BA },
            { AC, CA, BA, AB },
            { BA, AB },
            { BA, AB, AC, CA },
            { BA, AB, CA, AC },
            { CA, AC },
            { CA, AC, AB, BA },
            { CA, AC, BA, AB }
        };
        String[][][] expectedCycles = {
            { AB, BA },
            { AC, CA },
            { BA, AB },
            { CA, AC }
        };
        assertEqualTrails(expectedCircuits, explorer.findAllCircuits(graph, "a"));
        assertEqualTrails(expectedCycles, explorer.findAllCycles(graph, "a"));
    }


    @Test
    public void findAllMultyLoop() {
        for (GraphExplorer<String, Edge<String>> explorer : getExplorers()) {
            findAllMultyLoop(explorer);
        }
    }

    private void findAllMultyLoop(GraphExplorer<String, Edge<String>> explorer) {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addEdge(new UndirectedEdge("a", "a"));
        String[][][] expectedCircuits = {
            { AA }
        };
        assertEqualTrails(expectedCircuits, explorer.findAllCircuits(graph, "a"));
        assertEqualTrails(expectedCircuits, explorer.findAllCycles(graph, "a"));
        graph.addEdge(new UndirectedEdge("a", "a"));
        expectedCircuits = new String[][][] {
            { AA },
            { AA },
            { AA, AA },
            { AA, AA }
        };
        String[][][] expectedCycles = {
            { AA },
            { AA }
        };
        assertEqualTrails(expectedCircuits, explorer.findAllCircuits(graph, "a"));
        assertEqualTrails(expectedCycles, explorer.findAllCycles(graph, "a"));
    }


    @Test
    public void findAllDoubleLoop() {
        for (GraphExplorer<String, Edge<String>> explorer : getExplorers()) {
            findAllDoubleLoop(explorer);
        }
    }

    private void findAllDoubleLoop(GraphExplorer<String, Edge<String>> explorer) {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addEdge(new UndirectedEdge("a", "a"));
        String[][][] expectedTrails = {
            {AA} // The only edge
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", "a"));
        assertEqualTrails(expectedTrails, explorer.findAllCircuits(graph, "a"));
        assertEqualTrails(expectedTrails, explorer.findAllPaths(graph, "a", "a"));
        assertEqualTrails(expectedTrails, explorer.findAllCycles(graph, "a"));
        graph.addEdge(new UndirectedEdge("a", "a"));
        String[][][] expectedCircuits = {
            {AA}, // The first edge
            {AA}, // The second edge
            {AA, AA}, // The first, then the second edge
            {AA, AA} // The second, then the first edge
        };
        assertEqualTrails(expectedCircuits, explorer.findAllTrails(graph, "a", "a"));
        assertEqualTrails(expectedCircuits, explorer.findAllCircuits(graph, "a"));
        String[][][] expectedCycles = {
            {AA}, // Edge 1
            {AA} // Edge 2
        };
        assertEqualTrails(expectedCycles, explorer.findAllPaths(graph, "a", "a"));
        assertEqualTrails(expectedCycles, explorer.findAllCycles(graph, "a"));
        graph.addEdge(new UndirectedEdge("a", "a"));
        expectedCircuits = new String[][][]{
            {AA}, // 1
            {AA}, // 2
            {AA}, // 3
            {AA, AA}, // 1, 2
            {AA, AA}, // 1, 3
            {AA, AA}, // 2, 1
            {AA, AA}, // 2, 3
            {AA, AA}, // 3, 1
            {AA, AA}, // 3, 2
            {AA, AA, AA}, // 1, 2, 3
            {AA, AA, AA}, // 1, 3, 2
            {AA, AA, AA}, // 2, 1, 3
            {AA, AA, AA}, // 2, 3, 1
            {AA, AA, AA}, // 3, 1, 2
            {AA, AA, AA}  // 3, 2, 1
        };
        assertEqualTrails(expectedCircuits, explorer.findAllCircuits(graph, "a"));
        expectedCycles = new String[][][]{
            {AA}, // 1
            {AA}, // 2
            {AA}  // 3
        };
        assertEqualTrails(expectedCycles, explorer.findAllPaths(graph, "a", "a"));
        assertEqualTrails(expectedCycles, explorer.findAllCycles(graph, "a"));
    }


    @Test
    public void testFindAllFrom() {
        for (GraphExplorer<String, Edge<String>> explorer : getExplorers()) {
            testFindAllFrom(explorer);
        }
    }

    private void testFindAllFrom(GraphExplorer<String, Edge<String>> explorer) {
        DefaultMutableGraph<String> graph = new DefaultMutableGraph<>();
        graph.addEdge(new UndirectedEdge("a", "b"));
        String[][][] expectedTrails = {
            {AB}
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", null));
        assertEqualTrails(expectedTrails, explorer.findAllPaths(graph, "a", null));
        graph.addEdge(new UndirectedEdge("b", "c"));
        expectedTrails = new String[][][] {
            {AB},
            {AB, BC}
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", null));
        assertEqualTrails(expectedTrails, explorer.findAllPaths(graph, "a", null));
        graph.addEdge(new UndirectedEdge("a", "c"));
        expectedTrails = new String[][][] {
            {AB},
            {AB, BC},
            {AB, BC, AC},
            {AC},
            {AC, BC},
            {AC, BC, AB}
        };
        String[][][] expectedPaths = {
            {AB},
            {AB, BC},
            {AC},
            {AC, BC}
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", null));
        assertEqualTrails(expectedPaths, explorer.findAllPaths(graph, "a", null));
        graph.addEdge(new UndirectedEdge("c", "c"));
        expectedTrails = new String[][][] {
            {AB},
            {AB, BC},
            {AB, BC, CC},
            {AB, BC, AC},
            {AB, BC, CC, AC},
            {AC},
            {AC, CC},
            {AC, BC},
            {AC, CC, BC},
            {AC, BC, AB},
            {AC, CC, BC, AB}
        };
        expectedPaths = new String[][][] {
            {AB},
            {AB, BC},
            {AC},
            {AC, BC}
        };
        assertEqualTrails(expectedTrails, explorer.findAllTrails(graph, "a", null));
        assertEqualTrails(expectedPaths, explorer.findAllPaths(graph, "a", null));
    }


    @Test
    public void testCompleteGraph() {
        for (GraphExplorer<Integer, Edge<Integer>> explorer : getExplorers()) {
            System.out.println(explorer.toString());
            testCompleteGraph(explorer);
            System.out.println("==========");
        }
    }

    private void testCompleteGraph(GraphExplorer<Integer, Edge<Integer>> explorer) {
        DefaultMutableGraph<Integer> graph = new DefaultMutableGraph<>();
        graph.addVertex(1);
        assertTrue(explorer.findAllPaths(graph, 1, null).isEmpty());
        boolean done = false;
        while (!done) {
            ArrayList<Integer> vertices = new ArrayList<>(graph.getVertices());
            Integer v2 = vertices.size() + 1;
            vertices.forEach(v1 -> graph.addEdge(new UndirectedEdge<>(v1, v2)));
            long startTime = System.nanoTime();
            Collection<List<Edge<Integer>>> trails = explorer.findAllPaths(graph, 1, null);
            long duration = System.nanoTime() - startTime;
            System.out.printf("%d vertices: %d trails\n", v2, trails.size());
            System.out.printf("Duration: %.3f ms\n", duration / 1000000.0);
            done = v2.equals(9);
        }
    }


    private String[][][] withReversed(String[][][] trails) {
        String[][][] withReversed = new String[trails.length * 2][][];
        for (int i = 0; i < trails.length; ++i) {
            withReversed[i] = trails[i];
            int length = trails[i].length;
            String[][] reversed = new String[length][];
            for (int j = 0; j < length; ++j) {
                reversed[length - j - 1] = trails[i][j];
            }
            withReversed[i + trails.length] = reversed;
        }
        return withReversed;
    }

    private void assertEqualTrails(String[][][] expectedTrails, Collection<List<Edge<String>>> foundTrails) {
        String mismatches = findMismatches(Arrays.copyOf(expectedTrails, expectedTrails.length), foundTrails);
        assertTrue(mismatches.isEmpty(), mismatches);
    }

    private String findMismatches(String[][][] expectedTrails, Collection<List<Edge<String>>> foundTrails) {
        StringBuilder mismatches = new StringBuilder();
        foundTrails.forEach((List<Edge<String>> foundTrail) -> {
            int index = indexOf(foundTrail, expectedTrails);
            if (index < 0) {
                mismatches.append("Trail ").append(foundTrail).append(" was found but not expected\r\n");
            }
            else {
                expectedTrails[index] = null;
            }
        });
        for (String[][] expectedTrail : expectedTrails) {
            if (expectedTrail != null) {
                mismatches.append("Trail ");
                for (String[] e : expectedTrail) {
                    mismatches.append(Arrays.toString(e));
                }
                mismatches.append(" was expected but not found\r\n");
            }
        }
        return mismatches.toString();
    }

    private int indexOf(List<Edge<String>> foundTrail, String[][][] expectedTrails) {
        for (int i = 0; i < expectedTrails.length; ++i) {
            if (equals(foundTrail, expectedTrails[i])) {
                return i;
            }
        }
        return -1;
    }

    private boolean equals(List<Edge<String>> foundTrail, String[][] expectedTrail) {
        if (expectedTrail == null || foundTrail.size() != expectedTrail.length) {
            return false;
        }
        for (int i = 0; i < expectedTrail.length; ++i) {
            List vertices = new ArrayList(foundTrail.get(i).getVertices());
            if (! vertices.get(0).equals(expectedTrail[i][0]) || ! vertices.get(1).equals(expectedTrail[i][1])) {
                return false;
            }
        }
        return true;
    }

    private static GraphExplorer[] getExplorers() {
        TrailFinder[] trailFinders = getTrailFinders();
        GraphExplorer[] explorers = new GraphExplorer[trailFinders.length];
        for (int i = 0; i < trailFinders.length; ++i) {
            explorers[i] = new GraphExplorer(trailFinders[i]);
        }
        return explorers;
    }

    private static TrailFinder[] getTrailFinders() {
        return new TrailFinder[]{
            new NonRecursiveTrailFinder(),
            new RecursiveTrailFinder() };
    }

    private static final String[] AA = { "a", "a" };
    private static final String[] AB = { "a", "b" };
    private static final String[] AC = { "a", "c" };
    private static final String[] BA = { "b", "a"};
    private static final String[] BB = { "b", "b" };
    private static final String[] AD = { "a", "d" };
    private static final String[] BC = { "b", "c" };
    private static final String[] BD = { "b", "d" };
    private static final String[] CA = { "c", "a" };
    private static final String[] CC = { "c", "c" };
    private static final String[] CD = { "c", "d" };
    private static final String[] CE = { "c", "e" };
    private static final String[] DE = { "d", "e" };
    private static final String[] EE = { "e", "e" };

}
