/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.util.*;


public class ElementDeletion implements Mutation {

    public ElementDeletion(Collection<VertexRenderer> vertices, Collection<EdgeRenderer> edges, GraphCanvas graphCanvas) {
        this.graphCanvas = Objects.requireNonNull(graphCanvas);
        this.vertices = CollectionUtil.unmodifiableCollection(vertices);
        this.edges = CollectionUtil.unmodifiableCollection(edges);
    }

    @Override
    public Mutation.Type getType() {
        return Mutation.Type.DELETION;
    }

    @Override
    public void undo() {
        graphCanvas.insertRenderers(vertices, edges);
    }

    @Override
    public void redo() {
        graphCanvas.removeRenderers(vertices, edges);
    }

    @Override
    public String getBundleKey() {
        if (vertices.size() == 1) {
            return "Vertex" + getType().getBundleKey();
        }
        if (edges.size() == 1) {
            return "Edge" + getType().getBundleKey();
        }
        return "Selection" + getType().getBundleKey();
    }

    private final GraphCanvas graphCanvas;

    private final Collection<VertexRenderer> vertices;
    private final Collection<EdgeRenderer> edges;

}
