/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
 */
package bka.awt.graphcanvas.history;

import bka.awt.graphcanvas.*;
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
    public String getBundleKey() {
        return ((edges.isEmpty()) ? "Vertex" : "Edge") + getType().getBundleKey();
    }

    @Override
    public void undo() {
        graphCanvas.removeRenderers(vertices, edges);
    }

    @Override
    public void redo() {
        graphCanvas.insertRenderers(vertices, edges);
    }

    private final GraphCanvas graphCanvas;

    private final Collection<VertexRenderer> vertices;
    private final Collection<EdgeRenderer> edges;

}
