/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs;

import java.util.*;

public class DefaultMutableGraph<V> extends MutableGraph<V, Edge<V>> implements Graph<V> {

    public DefaultMutableGraph() {
        super();
    }

    public DefaultMutableGraph(GraphBase<V, ? extends Edge<V>> graph) {
        super(graph);
    }

    public DefaultMutableGraph(Collection<V> vertices, Collection<? extends Edge<V>> edges) {
        super(vertices, edges);
    }

    public DefaultMutableGraph(Collection<Edge<V>> edges) {
        super(edges);
    }

}
