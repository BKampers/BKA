/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.demo.graphs.history.*;
import java.awt.*;
import java.awt.event.*;


public class VertexResizeHandler extends CanvasEventHandler {

    public VertexResizeHandler(GraphCanvas canvas, VertexRenderer draggingVertex) {
        super(canvas);
        this.draggingVertex = draggingVertex;
        this.originalDimension = draggingVertex.getDimension();
    }

    @Override
    public ComponentUpdate mouseDragged(MouseEvent event) {
        draggingVertex.resize(event.getPoint());
        return ComponentUpdate.REPAINT;
    }

    @Override
    public ComponentUpdate mouseReleased(MouseEvent event) {
        if (!originalDimension.equals(draggingVertex.getDimension())) {
            getCanvas().getEdges().stream()
                .filter(edge -> draggingVertex.equals(edge.getStart()) || draggingVertex.equals(edge.getEnd()))
                .forEach(edge -> CanvasUtil.cleanup(edge));
            getCanvas().addHistory(new PropertyMutation<>(
                Mutation.Type.VERTEX_RESIZE,
                draggingVertex::getDimension,
                draggingVertex::setDimension,
                originalDimension));
        }
        getCanvas().resetEventHandler();
        return ComponentUpdate.NO_OPERATION;
    }

    private final VertexRenderer draggingVertex;
    private final Dimension originalDimension;

}
