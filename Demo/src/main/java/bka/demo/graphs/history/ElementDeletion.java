/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.util.*;


public class ElementDeletion implements Mutation {

    public ElementDeletion(Collection<VertexRenderer> vertices, Collection<EdgeRenderer> edges, GraphCanvas graphCanvas) {
        this.graphCanvas = Objects.requireNonNull(graphCanvas);
        this.vertexRenderers = CollectionUtil.unmodifiableCollection(vertices);
        this.edgeRenderers = CollectionUtil.unmodifiableCollection(edges);
    }

    @Override
    public Mutation.Type getType() {
        return Mutation.Type.DELETION;
    }

    @Override
    public void undo() {
        graphCanvas.insertRenderers(vertexRenderers, edgeRenderers);
    }

    @Override
    public void redo() {
        graphCanvas.removeRenderers(vertexRenderers, edgeRenderers);
    }

    @Override
    public Collection<VertexRenderer> getVertices() {
        return vertexRenderers;
    }

    @Override
    public Collection<EdgeRenderer> getEdges() {
        return edgeRenderers;
    }

    private final GraphCanvas graphCanvas;

    private final Collection<VertexRenderer> vertexRenderers;
    private final Collection<EdgeRenderer> edgeRenderers;

}
