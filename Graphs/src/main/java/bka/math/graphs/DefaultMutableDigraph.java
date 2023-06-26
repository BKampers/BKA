/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;

import java.util.*;

public class DefaultMutableDigraph<V> extends MutableGraph<V, DirectedEdge<V>> implements Digraph<V> {

    public DefaultMutableDigraph() {
        super();
    }

    public DefaultMutableDigraph(GraphBase<V, DirectedEdge<V>> graph) {
        super(graph);
    }

    public DefaultMutableDigraph(Collection<V> vertices, Collection<? extends DirectedEdge<V>> edges) {
        super(vertices, edges);
    }

    public DefaultMutableDigraph(Collection<DirectedEdge<V>> edges) {
        super(edges);
    }

}
