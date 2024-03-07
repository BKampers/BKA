/*
** © Bart Kampers
*/

package bka.awt.graphcanvas.history;

import bka.awt.graphcanvas.*;
import java.awt.*;
import java.util.*;


public class ElementRelocation extends Mutation.Symmetrical {

    public ElementRelocation(Collection<Element> elements, Point vector, Map<EdgeRenderer, EdgeRenderer.Excerpt> affectedEdges) {
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
            EdgeRenderer.Excerpt currentShape = edge.getExcerpt();
            edge.set(historyShape);
            historyShape.set(currentShape);
        });
    }

    @Override
    public String getBundleKey() {
        return ((vertices.size() == 1 && edges.isEmpty()) ? "Vertex" : "Selection") + getType().getBundleKey();
    }

    private final Collection<VertexRenderer> vertices;
    private final Collection<EdgeRenderer> edges;
    private final Point vector;
    private final Map<EdgeRenderer, EdgeRenderer.Excerpt> affectedEdges;

}
