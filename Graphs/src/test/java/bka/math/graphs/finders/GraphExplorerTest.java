package bka.math.graphs.finders;

import bka.math.graphs.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author bartkampers
 */
public class GraphExplorerTest {

     @Test
     public void testUndirected() {
        GraphExplorer explorer = GraphExplorer.undirected();
        assertEquals(
            Set.of(
                List.of(U12),
                List.of(U12, U23, U34, U24),
                List.of(U12, U24, U34, U23),
                List.of(U13, U23),
                List.of(U13, U34, U24)), 
            new HashSet<>(explorer.findAllTrails(graph(U12, U13, U23, U24, U34), 1, 2)));
        assertEquals(
            Set.of(
                List.of(U12),
                List.of(U13, U23),
                List.of(U13, U34, U24)), 
            new HashSet<>(explorer.findAllPaths(graph(U12, U13, U23, U24, U34), 1, 2)));
        assertEquals(
            Set.of(
                List.of(U12, U23, U13),
                List.of(U13, U23, U12),
                List.of(U12, U24, U34, U13),
                List.of(U13, U34, U24, U12)), 
            new HashSet<>(explorer.findAllCircuits(graph(U12, U13, U23, U24, U34), 1)));
    }
     
    @Test
    public void testUndirectedRestricted() {
        GraphExplorer explorer = GraphExplorer.undirected(trail -> trail.size() <= 2);
        assertEquals(
            Set.of(
                List.of(U12),
                List.of(U12, U23),
                List.of(U12, U24),
                List.of(U13),
                List.of(U13, U23),
                List.of(U13, U34)), 
            new HashSet<>(explorer.findAllTrails(graph(U12, U13, U23, U24, U34), 1)));
    }

    @Test
    public void testUndirectedLimited() {
        GraphExplorer explorer = GraphExplorer.undirectedLimited(trails -> trails.size() == 3);
        Collection<List<UndirectedEdge<Integer>>> paths = explorer.findAllPaths(graph(U12, U13, U23, U24, U34), 1);
        assertEquals(3, paths.size());
    }

    @Test
    public void testUndirectedRestrictedLimited() {
        GraphExplorer explorer = GraphExplorer.undirectedLimited(trail -> trail.size() <= 3, trails -> !trails.isEmpty());
        Collection<List<UndirectedEdge<Integer>>> cycles = explorer.findAllCycles(graph(U12, U13, U23, U24, U34), 1);
        assertEquals(1, cycles.size());
        assertTrue(cycles.stream().allMatch(cycle -> cycle.size() <= 3));
    }
    
    @Test
    public void testDirected() {
        GraphExplorer explorer = GraphExplorer.directed();
        assertEquals(
            Set.of(
                List.of(D12, D24),
                List.of(D12, D24, D45, D57, D76, D64)), 
            new HashSet<>(explorer.findAllTrails(graph(D12, D31, D24, D43, D45, D64, D57, D76), 1, 4)));
        assertEquals(
            Set.of(
                List.of(D12, D24)), 
            new HashSet<>(explorer.findAllPaths(graph(D12, D31, D24, D43, D45, D64, D57, D76), 1, 4)));
        assertEquals(
            Set.of(
                List.of(D12, D24, D43, D31),
                List.of(D12, D24, D45, D57, D76, D64, D43, D31)), 
            new HashSet<>(explorer.findAllCircuits(graph(D12, D31, D24, D43, D45, D64, D57, D76), 1)));
    }
    
        @Test
    public void testDirectedRestricted() {
        GraphExplorer explorer = GraphExplorer.directed(trail -> trail.size() <= 3);
        assertEquals(
            Set.of(
                List.of(D12),
                List.of(D12, D23),
                List.of(D12, D23, D31),
                List.of(D12, D24),
                List.of(D12, D24, D45)),
            new HashSet<>(explorer.findAllTrails(graph(D12, D23, D24, D31, D45), 1)));
        assertEquals(
            Set.of(
                List.of(D12),
                List.of(D12, D23),
                List.of(D12, D24),
                List.of(D12, D24, D45)),
            new HashSet<>(explorer.findAllPaths(graph(D12, D23, D24,  D31, D45), 1)));
        assertEquals(
            Set.of(
                List.of(D12, D23, D31)),
            new HashSet<>(explorer.findAllCycles(graph(D12, D23, D24,  D31, D43), 1)));
    }
    
    private static Graph<Integer> graph(Edge<Integer>... edges) {
        return new DefaultMutableGraph<>(Arrays.asList(edges));
    }

    private static final UndirectedEdge<Integer> U12 = new UndirectedEdge<>(1, 2);
    private static final UndirectedEdge<Integer> U13 = new UndirectedEdge<>(1, 3);
    private static final UndirectedEdge<Integer> U23 = new UndirectedEdge<>(2, 3);
    private static final UndirectedEdge<Integer> U24 = new UndirectedEdge<>(2, 4);
    private static final UndirectedEdge<Integer> U34 = new UndirectedEdge<>(3, 4);

    private static final DirectedEdge<Integer> D12 = new DirectedEdge<>(1, 2);
    private static final DirectedEdge<Integer> D23 = new DirectedEdge<>(2, 3);
    private static final DirectedEdge<Integer> D24 = new DirectedEdge<>(2, 4);
    private static final DirectedEdge<Integer> D31 = new DirectedEdge<>(3, 1);
    private static final DirectedEdge<Integer> D43 = new DirectedEdge<>(4, 3);
    private static final DirectedEdge<Integer> D45 = new DirectedEdge<>(4, 5);
    private static final DirectedEdge<Integer> D64 = new DirectedEdge<>(6, 4);
    private static final DirectedEdge<Integer> D57 = new DirectedEdge<>(5, 7);
    private static final DirectedEdge<Integer> D76 = new DirectedEdge<>(7, 6);
}
