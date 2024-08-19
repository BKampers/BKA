package bka.math.graphs.finders;

import bka.math.graphs.*;
import bka.math.graphs.utils.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ShortestRoadTest {
    
    @BeforeEach
    public void setUp() {
        map = new ImmutableGraph<>(List.of(
            new Road("A", "B", 5.0),
            new Road("B", "C", 2.5),
            new Road("A", "C", 3.0),
            new Road("B", "D", 3.0),
            new Road("C", "D", 3.0)));
    }

     @Test
     public void test() {
        assertTrails(
            Set.of(
                List.of("A", "C", "D"),
                List.of("A", "B", "D"),
                List.of("A", "C", "B", "D")),
            ofMaxLength(10));
        assertTrails(
            Set.of(
                List.of("A", "C", "D"),
                List.of("A", "B", "D")),
            ofMaxLength(8.5)
        );
        assertTrails(
            Set.of(
                List.of("A", "C", "D")),
            ofMaxLength(7)
        );
    }

    private static Predicate<List<Road>> ofMaxLength(double max) {
        return path -> path.stream()
            .map(road -> road.getLength())
            .reduce(0.0, (total, length) -> total + length) < max;
    }
     
     private void assertTrails(Set<List<String>> expectedVertices, Predicate<List<Road>> restriction) {
        GraphExplorer<String, Road> explorer = GraphExplorer.undirected(restriction);
        Collection<List<Road>> paths = explorer.findAllPaths(map, "A", "D");
        Set<List<String>> vertexPaths = paths.stream()
            .map((path) -> GraphUtil.vertexPath(path, "A"))
            .collect(Collectors.toSet());
        assertEquals(expectedVertices, vertexPaths);
     }
    
    private class Road extends UndirectedEdge<String> {
        
        public Road(String name1, String name2, double length) {
            super(name1, name2);
            this.length = length;
        }

        public double getLength() {
            return length;
        }
        
        private final double length;
    }
    
    private ImmutableGraph<String, Road> map;

}
