/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.awt.*;
import java.util.List;
import java.util.*;


public class SizeChange extends Mutation.Symmetrical {

    public SizeChange(VertexRenderer vertex, Dimension dimension) {
        this.vertex = Objects.requireNonNull(vertex);
        this.dimension = dimension;
    }

    @Override
    public Mutation.Type getType() {
        return Mutation.Type.SHAPE_CHANGE;
    }

    @Override
    protected void revert() {
        Dimension currentDimension = vertex.getDimension();
        vertex.setDimension(dimension);
        dimension = currentDimension;
    }

    @Override
    public Collection<VertexRenderer> getVertices() {
        return List.of(vertex);
    }

    private final VertexRenderer vertex;
    private Dimension dimension;

}
