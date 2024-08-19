package bka.math.graphs.finders;

import bka.math.graphs.*;
import java.util.*;
import java.util.function.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BridgePuzzle {
    
    @Test
    public void testBridgesAsEdge() {
        Graph<String> map = new DefaultMutableGraph<>(List.of(
            new UndirectedEdge<>("North", "West"),
            new UndirectedEdge<>("North", "West"),
            new UndirectedEdge<>("North", "East"),
            new UndirectedEdge<>("West", "East"),
            new UndirectedEdge<>("South", "West"),
            new UndirectedEdge<>("South", "West"),
            new UndirectedEdge<>("South", "East")));
        map.getVertices().forEach(vertex -> {
            Collection<List<UndirectedEdge<String>>> trails = GraphExplorer.undirected().findAllTrails(map, vertex);
            assertTrue(trails.stream().noneMatch(containsAllEdges(map)));
        });
    }

    private static Predicate<List<UndirectedEdge<String>>> containsAllEdges(Graph<String> map) {
        return trail -> trail.size() == map.getEdges().size();
    }
     
    @Test void testBridgesAsVertices() {
        Graph<String> map = new DefaultMutableGraph<>(List.of(
            new UndirectedEdge<>("1", "2"),
            new UndirectedEdge<>("1", "3"),
            new UndirectedEdge<>("1", "5"),
            new UndirectedEdge<>("1", "6"),
            new UndirectedEdge<>("2", "3"),
            new UndirectedEdge<>("2", "4"),
            new UndirectedEdge<>("3", "4"),
            new UndirectedEdge<>("3", "5"),
            new UndirectedEdge<>("3", "7"),
            new UndirectedEdge<>("4", "7"),
            new UndirectedEdge<>("5", "6"),
            new UndirectedEdge<>("5", "7"),
            new UndirectedEdge<>("6", "7")
        ));
        map.getVertices().forEach(vertex -> {
            Collection<List<UndirectedEdge<String>>> paths = GraphExplorer.undirected().findAllPaths(map, vertex);
            assertTrue(paths.stream().noneMatch(containsAllVertices(map)));
        });         
    }

    private static Predicate<List<UndirectedEdge<String>>> containsAllVertices(Graph<String> map) {
        return trail -> trail.size() == map.getVertices().size();
    }
   
}
