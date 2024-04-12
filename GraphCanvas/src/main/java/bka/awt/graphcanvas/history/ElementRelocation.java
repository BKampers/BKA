/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas.history;

import bka.awt.graphcanvas.*;
import java.awt.*;
import java.util.*;


public class ElementRelocation extends Mutation.Symmetrical {

    public ElementRelocation(Collection<GraphComponent> elements, Point vector, Map<EdgeComponent, EdgeComponent.Excerpt> affectedEdges) {
        this.vertices = CollectionUtil.getVertices(elements);
        this.edges = CollectionUtil.getEdges(elements);
        this.vector = Objects.requireNonNull(vector);
        this.affectedEdges = CollectionUtil.unmodifiableMap(affectedEdges);
    }

    @Override
    public Mutation.Type getType() {
        return Mutation.Type.RELOCATION;
    }

    @Override
    public void revert() {
        vector.move(-vector.x, -vector.y);
        vertices.forEach(element -> element.move(vector));
        edges.forEach(element -> element.move(vector));
        affectedEdges.forEach((edge, historyShape) -> {
            EdgeComponent.Excerpt currentShape = edge.getExcerpt();
            edge.set(historyShape);
            historyShape.set(currentShape);
        });
    }

    @Override
    public String getBundleKey() {
        return ((vertices.size() == 1 && edges.isEmpty()) ? "Vertex" : "Selection") + getType().getBundleKey();
    }

    private final Collection<VertexComponent> vertices;
    private final Collection<EdgeComponent> edges;
    private final Point vector;
    private final Map<EdgeComponent, EdgeComponent.Excerpt> affectedEdges;

}
