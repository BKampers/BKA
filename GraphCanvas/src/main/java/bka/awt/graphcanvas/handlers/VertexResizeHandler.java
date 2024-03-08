/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
 */
package bka.awt.graphcanvas.handlers;

import bka.awt.graphcanvas.*;
import bka.awt.graphcanvas.history.*;
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
