/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.util.*;


public class ElementInsertion implements Mutation {

    public ElementInsertion(VertexRenderer vertex, GraphCanvas graphCanvas) {
        this.graphCanvas = Objects.requireNonNull(graphCanvas);
        vertices = List.of(vertex);
        edges = Collections.emptyList();
    }

    public ElementInsertion(EdgeRenderer edge, GraphCanvas graphCanvas) {
        this.graphCanvas = Objects.requireNonNull(graphCanvas);
        vertices = Collections.emptyList();
        edges = List.of(edge);
    }

    @Override
    public Mutation.Type getType() {
        return Mutation.Type.INSERTION;
    }

    @Override
    public void undo() {
        graphCanvas.removeRenderers(vertices, edges);
    }

    @Override
    public void redo() {
        graphCanvas.insertRenderers(vertices, edges);
    }

    @Override
    public Collection<VertexRenderer> getVertices() {
        return vertices;
    }

    @Override
    public Collection<EdgeRenderer> getEdges() {
        return edges;
    }

    private final GraphCanvas graphCanvas;

    private final Collection<VertexRenderer> vertices;
    private final Collection<EdgeRenderer> edges;

}
