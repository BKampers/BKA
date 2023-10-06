/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.awt.*;
import java.util.List;
import java.util.*;


public class ElementRelocation extends Mutation.Symmetrical {

    public ElementRelocation(Collection<Element> elements, Point vector, Map<EdgeRenderer, java.util.List<Point>> affectedEdges) {
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
        affectedEdges.forEach((edge, points) -> {
            List<Point> edgePoints = new ArrayList<>(edge.getPoints());
            edge.setPoints(points);
            points.clear();
            points.addAll(edgePoints);
        });
    }

    @Override
    public Collection<VertexRenderer> getVertices() {
        return vertices;
    }

    @Override
    public Collection<EdgeRenderer> getEdges() {
        return edges;
    }

    private final Collection<VertexRenderer> vertices;
    private final Collection<EdgeRenderer> edges;
    private final Point vector;
    private final Map<EdgeRenderer, java.util.List<Point>> affectedEdges;

}
