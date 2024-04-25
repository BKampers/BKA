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
import java.util.*;


public class VertexResizeHandler extends CanvasEventHandler {

    public VertexResizeHandler(GraphCanvas canvas, VertexComponent draggingVertex, ResizeDirection direction) {
        super(canvas);
        this.draggingVertex = draggingVertex;
        this.originalDimension = draggingVertex.getDimension();
        this.direction = Objects.requireNonNull(direction);
    }

    @Override
    public CanvasUpdate mouseDragged(MouseEvent event) {
        draggingVertex.resize(event.getPoint(), direction);
        return CanvasUpdate.REPAINT;
    }

    @Override
    public CanvasUpdate mouseReleased(MouseEvent event) {
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
        return CanvasUpdate.NO_OPERATION;
    }

    private final VertexComponent draggingVertex;
    private final Dimension originalDimension;
    private final ResizeDirection direction;

}
