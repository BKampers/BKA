/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.awt.*;
import java.util.List;
import java.util.*;


public class EdgeTransformation extends Mutation.Symmetrical {

    public EdgeTransformation(EdgeRenderer edge, List<Point> originalPoints) {
        this.edge = Objects.requireNonNull(edge);
        this.originalPoints = CollectionUtil.unmodifiableList(originalPoints);
    }

    @Override
    public Mutation.Type getType() {
        return Mutation.Type.SHAPE_CHANGE;
    }

    @Override
    protected void revert() {
        List<Point> currentPoints = new ArrayList<>(edge.getPoints());
        edge.setPoints(originalPoints);
        originalPoints = currentPoints;
    }

    @Override
    public Collection<EdgeRenderer> getEdges() {
        return List.of(edge);
    }

    private final EdgeRenderer edge;
    private List<Point> originalPoints;

}
